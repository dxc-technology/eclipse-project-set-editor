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
 * Wizard page for selecting the content of a project set
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.csc.dip.projectset.ProjectSetUtil;

public class NewProjectSetWizardPage extends WizardPage {

	private ProjectSelectionArea selectionArea;

	/**
	 * Constructor for NewProjectSetWizardPage.
	 * @param pageName
	 */
	public NewProjectSetWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * Constructor for NewProjectSetWizardPage.
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	public NewProjectSetWizardPage(
		String pageName,
		String title,
		ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));		
				
		createProjectSelectionArea(composite);
		
		setControl(composite);
	}

	/**
	 * Creates the area for selecting the projects
	 */		
	public void createProjectSelectionArea(Composite parent) {
		Label selectLabel = new Label(parent, SWT.LEFT);
		selectLabel.setText(Messages.getString("NewProjectSetWizardPage.Select_shared_projects"));  //$NON-NLS-1$
						
		selectionArea = new ProjectSelectionArea(
			parent, 
			ProjectSetUtil.getSharedProjects());
	}
	
	/**
	 * Returns the selected projects
	 */	
	public IProject[] getSelectedProjects() {	
		return selectionArea.getCheckedProjects();
	}

}
