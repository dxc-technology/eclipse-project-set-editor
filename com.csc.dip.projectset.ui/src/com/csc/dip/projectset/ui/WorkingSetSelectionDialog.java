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


/**
 * @author Dirk Baumann
 * 
 * 
 * Dialog for selection of working sets
 */

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.SelectionDialog;


public class WorkingSetSelectionDialog extends SelectionDialog {


	class WorkingSetLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((IWorkingSet)element).getName();
		}
	}
	 
	CheckboxTableViewer tableViewer;
	/**
	 * Constructor for WorkingSetSelectionDialog.
	 * @param parentShell
	 */
	public WorkingSetSelectionDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Creates the main dialog area
	 */
	protected Control createDialogArea(Composite parent) {
		// create composite 
		Composite dialogArea = (Composite)super.createDialogArea(parent);
		
		Label label = new Label(dialogArea, SWT.LEFT);
		label.setText(Messages.getString("WorkingSetSelectionDialog.Select_working_sets"));  //$NON-NLS-1$
		
		createTableViewer(dialogArea);
		
		return dialogArea;
	}
	
	/**
	 * Creates the table viewer for selection of the working sets
	 */
	protected void createTableViewer(Composite parent) {
		tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		data.widthHint = 300;
		tableViewer.getControl().setLayoutData(data);
		
		tableViewer.setLabelProvider(new WorkingSetLabelProvider());
		
		tableViewer.add(getWorkingSets());		
	}
	
	/**
	 * Returns all working sets in the workspace
	 */
	protected IWorkingSet[] getWorkingSets() {
		return ProjectSetUIPlugin.getDefault().getWorkbench().getWorkingSetManager().getWorkingSets();
	}

	/**
	 * Sets the selection to the checked working sets
	 */
	protected void okPressed() {
		setSelectionResult(getCheckedWorkingSets());
		super.okPressed();
	}
	
	/**
	 * Returns the checked working sets
	 */	
	protected IWorkingSet[] getCheckedWorkingSets() {
		Object[] checkedObjects = tableViewer.getCheckedElements();
		IWorkingSet[] checkedWorkingSets = new IWorkingSet[checkedObjects.length];
		for (int i = 0; i < checkedObjects.length; i++) {
			checkedWorkingSets[i] = (IWorkingSet)checkedObjects[i];
		}
		return checkedWorkingSets;
	}
}
