/*******************************************************************************
 * Copyright (C) 2018 DXC Technology
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.csc.dip.projectset.ui;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.csc.dip.projectset.PreferredLocationsUtil;

/**
 * @author Dirk Baumann
 * 
 * A dialog for editing a path variable
 *
 */
public class PathVariableEntryDialog extends Dialog {


	PathVariableEntry entry;

	Label errorLabel;
	Text nameField;
	Text pathField;
	List<String> existingVariableNames;
	
	/**
	 * Constructor for PathVariableEntryDialog.
	 * @param parentShell
	 */
	public PathVariableEntryDialog(Shell parentShell, PathVariableEntry entry, List<String> existingVariableNames) {
		super(parentShell);
		
		this.entry = entry;
		this.existingVariableNames = existingVariableNames;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("PathVariableEntryDialog.Path_Variable")); //$NON-NLS-1$
	}
	
	protected Control createDialogArea(Composite parent) {
 
 		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2,false));
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 300;
		composite.setLayoutData(gridData);
		
		createErrorArea(composite);
		createNameArea(composite);
		createPathArea(composite);
				
 		return composite;
	}

	private void createErrorArea(Composite parent) {
		errorLabel = new Label(parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		errorLabel.setLayoutData(gridData);
		errorLabel.setText(""); //$NON-NLS-1$
	}

	private void createNameArea(Composite parent) {		
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("PathVariableEntryDialog.Name")); //$NON-NLS-1$
		
		nameField = new Text(parent, SWT.BORDER);
		nameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameField.setText(getEntry().getName());
		
		nameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
	}

	private void validate() {
		// validate name
		String name = nameField.getText().trim();
		String error = ""; //$NON-NLS-1$
		if (name.length() == 0) {
			error = Messages.getString("PathVariableEntryDialog.Name_required"); //$NON-NLS-1$
		} else
		if (existingVariableNames.contains(name)){
			error = Messages.getString("PathVariableEntryDialog.Name_already_exists"); //$NON-NLS-1$
		} else
		if (name.indexOf(PreferredLocationsUtil.PATH_VARIABLE_START_STRING) != -1) {
			error = MessageFormat.format(Messages.getString("PathVariableEntryDialog.{0}_not_allowed_in_name"), new Object[] {PreferredLocationsUtil.PATH_VARIABLE_START_STRING}); //$NON-NLS-1$
		} else
		if (name.indexOf(PreferredLocationsUtil.PATH_VARIABLE_END_STRING) != -1) {
			error = MessageFormat.format(Messages.getString("PathVariableEntryDialog.{0}_not_allowed_in_name"), new Object[] {PreferredLocationsUtil.PATH_VARIABLE_END_STRING}); //$NON-NLS-1$
		}
		// validate path
		String path = pathField.getText();
		if (path.indexOf(PreferredLocationsUtil.PATH_VARIABLE_START_STRING) != -1) {
			error = MessageFormat.format(Messages.getString("PathVariableEntryDialog.{0}_not_allowed_in_path"), new Object[] {PreferredLocationsUtil.PATH_VARIABLE_START_STRING}); //$NON-NLS-1$
		}		
		// set error text and state of OK button
		errorLabel.setText(error);
		getButton(OK).setEnabled(error.length() == 0);
	}


	private void createPathArea(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("PathVariableEntryDialog.Path")); //$NON-NLS-1$

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(3,false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		pathField = new Text(composite, SWT.BORDER);
		pathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pathField.setText(getEntry().getPath());
		pathField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});

		Button browseButton = new Button(composite, SWT.NONE);
		browseButton.setText(Messages.getString("PathVariableEntryDialog.Browse")); //$NON-NLS-1$
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
				dialog.setText(Messages.getString("PathVariableEntryDialog.Select_Directory")); //$NON-NLS-1$
				dialog.setMessage(Messages.getString("PathVariableEntryDialog.Select_directory_for_path_variable")); //$NON-NLS-1$
				String selectedDirectory = dialog.open();
				if (selectedDirectory != null) {
					pathField.setText(selectedDirectory);
				}
			}
		});		
	}
		
	/**
	 * Returns the entry.
	 * @return PathVariableEntry
	 */
	public PathVariableEntry getEntry() {
		return entry;
	}

	/**
	 * Sets the entry.
	 * @param entry The entry to set
	 */
	public void setEntry(PathVariableEntry entry) {
		this.entry = entry;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		entry.setName(nameField.getText().trim());
		entry.setPath(pathField.getText().trim());
		super.okPressed();
	}

}
