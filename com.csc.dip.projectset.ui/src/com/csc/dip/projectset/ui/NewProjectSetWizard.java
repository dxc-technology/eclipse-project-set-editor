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

/**
 * @author Dirk Baumann
 * 
 * 
 * Wizard for creating a new project set file
 */

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.csc.dip.projectset.IProjectSetConstants;
import com.csc.dip.projectset.NewProjectSetEntryException;
import com.csc.dip.projectset.NewProjectSetEntryInvTargetException;
import com.csc.dip.projectset.ProjectSet;

public class NewProjectSetWizard extends Wizard implements INewWizard {

	static final String FILE_CREATION_PAGE_ID = "fileCreationPage"; //$NON-NLS-1$
	static final String PROJECT_SET_PAGE_ID = "projectSetPage"; //$NON-NLS-1$
	
	private IStructuredSelection selection;
	private WizardNewFileCreationPage newFileCreationPage;
	private NewProjectSetWizardPage projectSetWizardPage;
	
	
	/**
	 * Constructor for NewProjectSetWizard.
	 */
	public NewProjectSetWizard() {
		super();
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		boolean accepted = true;
		IFile file = newFileCreationPage.createNewFile();		
		IProject[] selectedProjects = projectSetWizardPage.getSelectedProjects();
		
		ProjectSet projectSet;
		try {
			projectSet = new ProjectSet(file, selectedProjects);
			projectSet.write();
		} catch (NewProjectSetEntryInvTargetException | NewProjectSetEntryException e) {
			openErrorDialog(getShell(), e);
			return false;
		} finally {
			try {
				file.refreshLocal(IResource.DEPTH_ZERO, null);
			} catch (CoreException e) {
				openErrorDialog(getShell(), e);
				accepted = false;
			}
		}
		
		return accepted;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	} 

	/**
	 * Adds the wizard pages for the new wizard
	 */
	public void addPages() {
		super.addPages();
		newFileCreationPage = new WizardNewFileCreationPage(FILE_CREATION_PAGE_ID, selection);
		newFileCreationPage.setFileName("."+IProjectSetConstants.PROJECT_SET_FILE_EXTENSION); //$NON-NLS-1$
		newFileCreationPage.setDescription(Messages.getString("NewProjectSetWizard.Create_a_new_project_set_file")); //$NON-NLS-1$
		newFileCreationPage.setTitle(Messages.getString("NewProjectSetWizard.Project_set_file")); //$NON-NLS-1$
		newFileCreationPage.setImageDescriptor(ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/psffilewiz.gif")); //$NON-NLS-1$
		addPage(newFileCreationPage);		
		
		projectSetWizardPage = new NewProjectSetWizardPage(PROJECT_SET_PAGE_ID, Messages.getString("NewProjectSetWizard.Project_Set_Content"), null); //$NON-NLS-1$
		projectSetWizardPage.setImageDescriptor(ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/psffilewiz.gif")); //$NON-NLS-1$
		addPage(projectSetWizardPage);
	}

	/**
	 * Returns the title for the wizard window
	 */
	public String getWindowTitle() {
		return Messages.getString("NewProjectSetWizard.New_Project_Set_Wizard"); //$NON-NLS-1$
	}

	/**
	 * Return the title for error dialog in case of fail of create of ProjectSetEntry
	 * 
	 * @return returns the title for error dialog
	 */
	private String getErrorDialogTitle() {
		return Messages.getString("NewProjectSetWizard.Error");
	}

	/**
	 * Open error dialog in case of fail of create of ProjectSetEntry
	 */
	private void openErrorDialog(Shell shell, Exception e) {
		String errorMessage = Messages.getString("NewProjectSetWizard.Error_PSF_file_creation"); // $NON-NLS-1$

		if (e instanceof NewProjectSetEntryInvTargetException) {
			Throwable target = ((NewProjectSetEntryInvTargetException) e).getTargetException();
			if ((target instanceof TeamException) && (target.getMessage() != null)) {
				errorMessage = errorMessage
						+ MessageFormat.format(Messages.getString("NewProjectSetWizard.,_reason__{0}"), new Object[] { target.getMessage() }); // $NON-NLS-1$
			}
		} else {
			errorMessage = errorMessage + MessageFormat.format(Messages.getString("NewProjectSetWizard.,_reason__{0}"), new Object[] { e.getMessage() }); // $NON-NLS-1$
		}
		MessageDialog.openError(shell, getErrorDialogTitle(), errorMessage);
	}
}
