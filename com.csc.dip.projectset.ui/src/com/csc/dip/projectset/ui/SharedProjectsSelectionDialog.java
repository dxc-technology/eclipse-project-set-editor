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
 * Dialog for selection of shared and loaded projects
 */

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.csc.dip.projectset.ProjectSetUtil;

/**
 * Show a selection dialog with all shared projects in the workspace
 */

public class SharedProjectsSelectionDialog extends SelectionDialog {

	private ProjectSelectionArea selectionArea;
	private List<String> excludeProjectNames;
	
	public SharedProjectsSelectionDialog(Shell parent) {
		this(parent,"","",null); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public SharedProjectsSelectionDialog(Shell parent, String title, String message, List<String> excludeProjectNames) {
		super(parent);
		this.excludeProjectNames = excludeProjectNames;
		setTitle(title);
		setMessage(message);
	}

	/** 
	 * Creates the area for selection of the projects
	 */
	protected Control createDialogArea(Composite parent) {
		// create composite 
		Composite dialogArea = (Composite)super.createDialogArea(parent);
		
		Label label = new Label(dialogArea, SWT.LEFT);
		label.setText(getMessage());
		
		selectionArea = new ProjectSelectionArea(
			dialogArea,
			ProjectSetUtil.getSharedProjects(excludeProjectNames));
		
		return dialogArea;
	}
	
	/**
	 * Sets the selection from the selection area
	 */
	protected void okPressed() {
		setSelectionResult(selectionArea.getCheckedProjects());
		super.okPressed();
	}
}
