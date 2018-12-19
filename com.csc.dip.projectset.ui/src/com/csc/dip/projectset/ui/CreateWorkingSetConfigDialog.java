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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

/**
 * @author dbaumann
 */
public class CreateWorkingSetConfigDialog extends Dialog {

    private String workingSetName = ""; //$NON-NLS-1$
    private boolean recursive;
    private Label errorLabel;
    private List<IFile> psfFiles = new ArrayList<IFile>(); // List with IFile objects

    
    /**
     * @param parentShell
     */
    protected CreateWorkingSetConfigDialog(Shell parentShell) {
        super(parentShell);
    }

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("CreateWorkingSetConfigDialog.Create_Working_Set")); //$NON-NLS-1$
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        
        createHeader(composite);
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText(Messages.getString("CreateWorkingSetConfigDialog.Working_set_name")); //$NON-NLS-1$
        
        final Text nameText = new Text(composite, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.setText(workingSetName);
        nameText.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                workingSetName = nameText.getText();
                validate();
            }
        });
        
        final Button recursiveButton = new Button(composite, SWT.CHECK);
        GridData buttonGridData = new GridData(GridData.FILL_HORIZONTAL);
        buttonGridData.horizontalSpan = 2;
        recursiveButton.setLayoutData(buttonGridData);
        recursiveButton.setSelection(recursive);
        recursiveButton.setText(Messages.getString("CreateWorkingSetConfigDialog.Use_project_sets_recursively__with_currently_loaded_sub_project_sets_")); //$NON-NLS-1$
        recursiveButton.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	            recursive = recursiveButton.getSelection();
	        }
        });
               
		errorLabel = new Label(composite, SWT.NONE);
		GridData errorLabelGridData = new GridData(GridData.FILL_HORIZONTAL);
		errorLabelGridData.horizontalSpan = 2;
		errorLabel.setLayoutData(errorLabelGridData);
		errorLabel.setText(""); //$NON-NLS-1$
		errorLabel.setForeground(errorLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
        
        validate();
   	 	return composite;
    }
    
	/**
     * @param composite
     */
    private void createHeader(Composite composite) {
        Label headerLabel = new Label(composite, SWT.NONE);
        GridData headerLabelGridData = new GridData(GridData.FILL_HORIZONTAL);
        headerLabelGridData.horizontalSpan = 2;
        headerLabel.setLayoutData(headerLabelGridData);
        headerLabel.setText(Messages.getString("CreateWorkingSetConfigDialog.Create_a_working_set_with_the_projects_specified_in_the_project_set_file_s__")); //$NON-NLS-1$
        
        Iterator<IFile> psfFileIterator = psfFiles.iterator();
        while (psfFileIterator.hasNext()) {
            IFile psfFile = psfFileIterator.next();
            Label psfLabel = new Label(composite, SWT.NONE);
            GridData psfLabelGridData = new GridData(GridData.FILL_HORIZONTAL);
            psfLabelGridData.horizontalSpan = 2;
            psfLabel.setLayoutData(psfLabelGridData);
            psfLabel.setText(psfFile.getFullPath().toString());
        }
    }

    private void validate() {
	    
	    String error = ""; //$NON-NLS-1$
	    // check for valid working set name
	    if (workingSetName.trim().length() == 0) {
	        error = Messages.getString("CreateWorkingSetConfigDialog.Please_insert_name_for_the_working_set"); //$NON-NLS-1$
	    } else {
	        IWorkingSetManager workingSetManager = ProjectSetUIPlugin.getDefault().getWorkbench().getWorkingSetManager();
	        IWorkingSet[] workingSets = workingSetManager.getWorkingSets();
	        for (int i = 0; i < workingSets.length; i++) {
                if (workingSetName.equals(workingSets[i].getName())) {
                    error = Messages.getString("CreateWorkingSetConfigDialog.A_working_set_with_this_name_already_exists"); //$NON-NLS-1$
                }    
            }
	    }
		// set error text and state of OK button
	    if (errorLabel != null) {
	        errorLabel.setText(error);
	    }
		Button okButton = getButton(OK);
		if (okButton != null) {
		    okButton.setEnabled(error.length() == 0);
		}
	}

    public String getWorkingSetName() {
        return workingSetName;
    }
    
    public void setWorkingSetName(String workingSetName) {
        this.workingSetName = workingSetName;
    }
    
    public boolean isRecursive() {
        return recursive;
    }
    
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }
    
    public List<IFile> getPsfFiles() {
        return psfFiles;
    }
    
    public void setPsfFiles(List<IFile> psfFiles) {
        this.psfFiles = psfFiles;
    }
}
