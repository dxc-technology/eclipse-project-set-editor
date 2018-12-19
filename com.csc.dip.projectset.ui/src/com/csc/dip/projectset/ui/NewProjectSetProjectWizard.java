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
 * Wizard for creating a new project set project
 */

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;

import com.csc.dip.projectset.IProjectSetConstants;
import com.csc.dip.projectset.NewProjectSetEntryException;
import com.csc.dip.projectset.NewProjectSetEntryInvTargetException;
import com.csc.dip.projectset.ProjectSet;
import com.csc.dip.projectset.ProjectSetNature;
import com.csc.dip.projectset.ProjectSetProjectDescription;


public class NewProjectSetProjectWizard extends Wizard implements INewWizard {

	private WizardNewProjectCreationPage mainPage;
	private WizardNewProjectReferencePage referencePage;
	private NewProjectSetWizardPage projectSetWizardPage;

	// cache of newly-created project
	private IProject newProject;
		
	/**
	 * Constructor for NewProjectSetProjectWizard.
	 */
	public NewProjectSetProjectWizard() {
		super();
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		createNewProject();
	
		if (newProject == null)
			return false;
	
		return true;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/**
	 * Adds the wizard pages for the new wizard
	 */
	public void addPages() {
		super.addPages();
		
		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage"); //$NON-NLS-1$
		mainPage.setTitle(Messages.getString("NewProjectSetProjectWizard.NewProjectPage.title"));  //$NON-NLS-1$
		mainPage.setDescription(Messages.getString("NewProjectSetProjectWizard.NewProjectPage.description"));  //$NON-NLS-1$
		mainPage.setImageDescriptor(ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/psprojectwiz.gif")); //$NON-NLS-1$
		this.addPage(mainPage);

		projectSetWizardPage = new NewProjectSetWizardPage("psfpage"); //$NON-NLS-1$
		projectSetWizardPage.setTitle(Messages.getString("NewProjectSetProjectWizard.PSF_defintion")); //$NON-NLS-1$
		projectSetWizardPage.setDescription(Messages.getString("NewProjectSetProjectWizard.PSF_defintion_description")); //$NON-NLS-1$
		projectSetWizardPage.setImageDescriptor(ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/psprojectwiz.gif")); //$NON-NLS-1$
		this.addPage(projectSetWizardPage);
		
		// only add page if there are already projects in the workspace
		if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
			referencePage = new WizardNewProjectReferencePage("basicReferenceProjectPage"); //$NON-NLS-1$
			referencePage.setTitle(Messages.getString("NewProjectSetProjectWizard.ReferencePage.referenceTitle"));  //$NON-NLS-1$
			referencePage.setDescription(Messages.getString("NewProjectSetProjectWizard.ReferencePage.referenceDescription"));  //$NON-NLS-1$
			referencePage.setImageDescriptor(ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/psprojectwiz.gif")); //$NON-NLS-1$
			this.addPage(referencePage);
		}
	}

	/**
	 * Creates a new project resource with the selected name.
	 * <p>
	 * In normal usage, this method is invoked after the user has pressed Finish on
	 * the wizard; the enablement of the Finish button implies that all controls
	 * on the pages currently contain valid values.
	 * </p>
	 * <p>
	 * Note that this wizard caches the new project once it has been successfully
	 * created; subsequent invocations of this method will answer the same
	 * project resource without attempting to create it again.
	 * </p>
	 *
	 * @return the created project resource, or <code>null</code> if the project
	 *    was not created
	 */
	private IProject createNewProject() {
		if (newProject != null)
			return newProject;
	
		// get a project handle
		final IProject newProjectHandle = mainPage.getProjectHandle();
	
		// get a project descriptor
		IPath defaultPath = Platform.getLocation();
		IPath newPath = mainPage.getLocationPath();
		if (defaultPath.equals(newPath))
			newPath = null;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setLocation(newPath);
	
		// update the referenced project if provided
		if (referencePage != null) {
			IProject[] refProjects = referencePage.getReferencedProjects();
			if (refProjects.length > 0)
				description.setReferencedProjects(refProjects);
		}

		// set project nature
		description.setNatureIds(getNaturesIds());
		
		// set project set builder
		setBuildSpec(description);
			
		// create the new project operation
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				createProject(description, newProjectHandle, monitor);
			}
		};
	
		// run the new project creation operation
		try {
			getContainer().run(true, true, op);
		}
		catch (InterruptedException e) {
			return null;
		}
		catch (InvocationTargetException e) {
			// ie.- one of the steps resulted in a core exception	
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				if (((CoreException)t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
					MessageDialog.openError(
						getShell(), 
						Messages.getString("NewProjectSetProjectWizard.NewProject.errorMessage"),  //$NON-NLS-1$
						Messages.getString("NewProjectSetProjectWizard.NewProject.caseVariantExistsError")  //$NON-NLS-1$
						);	
				} else {
					ErrorDialog.openError(
						getShell(), 
						Messages.getString("NewProjectSetProjectWizard.NewProject.errorMessage"), //$NON-NLS-1$
						null, // no special message
				 		((CoreException) t).getStatus());
				}
			} else {
				// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
				Platform.getLog(Platform.getBundle(PlatformUI.PLUGIN_ID)).log(
					new Status(
						Status.ERROR, 
						PlatformUI.PLUGIN_ID, 
						0, 
						t.toString(),
						t));
				MessageDialog.openError(
					getShell(),
					Messages.getString("NewProjectSetProjectWizard.NewProject.errorMessage"),  //$NON-NLS-1$
					Messages.getString("NewProjectSetProjectWizard.NewProject.internalError"));  //$NON-NLS-1$
			}
			return null;
		}
	
		// store in variable so project can be used in subclasses
		// for further processing (e.g. add files)
        newProject = newProjectHandle;

		try {
			createAdditionalProjectResources(newProject);
		} catch (NewProjectSetEntryInvTargetException | NewProjectSetEntryException e) {
			openErrorDialog(getShell(), e);
			return null;
		} finally {
			// refresh new created project files
			try {
				newProject.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
				
		return newProjectHandle;
	}
	
	/**
	 * Returns the nature ids for a project set project
	 */
	protected String[] getNaturesIds() {
		String[] natureIds = new String[1];
		natureIds[0] = ProjectSetNature.PS_PROJECT_NATURE_ID;
		return natureIds;
	}

	/**
	 * Creates a project resource given the project handle and description.
	 *
	 * @param description the project description to create a project resource for
	 * @param projectHandle the project handle to create a project resource for
	 * @param monitor the progress monitor to show visual progress with
	 *
	 * @exception CoreException if the operation fails
	 * @exception OperationCanceledException if the operation is canceled
	 */
	private void createProject(IProjectDescription description, IProject projectHandle, IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		try {
			monitor.beginTask("",2000);//$NON-NLS-1$
	
			projectHandle.create(description, new SubProgressMonitor(monitor,1000));
	
			if (monitor.isCanceled())
				throw new OperationCanceledException();
	
			projectHandle.open(new SubProgressMonitor(monitor,1000));
	
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Creates subfolders and files in the newly created project.
	 * 
	 * @param project
	 *            The project in which the folders/files should be created.
	 * @throws NewProjectSetEntryInvTargetException
	 * @throws NewProjectSetEntryException
	 */
	protected void createAdditionalProjectResources(IProject project) throws NewProjectSetEntryInvTargetException, NewProjectSetEntryException {
		createPSProjectDescriptionFile(project);
		createPsfFile(project);				
	}

	/**
	 * Creates the project set project description file
	 * (that contains the name of the project set file)
	 */
	protected void createPSProjectDescriptionFile(IProject project) {
		ProjectSetNature psNature = null;
		try {
			psNature = (ProjectSetNature)project.getNature(ProjectSetNature.PS_PROJECT_NATURE_ID);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (psNature != null) {
			ProjectSetProjectDescription pspDescr = new ProjectSetProjectDescription();
			pspDescr.setPsfFilename(getPSFFileName(project));
			psNature.createPSProjectDescriptionFile(pspDescr);
		}
	}

	/**
	 * Creates the project set file
	 * 
	 * @throws NewProjectSetEntryInvTargetException
	 * @throws NewProjectSetEntryException
	 */
	protected void createPsfFile(IProject project) throws NewProjectSetEntryInvTargetException, NewProjectSetEntryException {
		
		IFile psfFile = project.getFile(getPSFFileName(project));		
		IProject[] selectedProjects = projectSetWizardPage.getSelectedProjects();
		
		ProjectSet projectSet = new ProjectSet(psfFile, selectedProjects);

		projectSet.write();
	}

	/**
	 * Returns the name of project set file
	 * that is by default the name of the project
	 */	
	protected String getPSFFileName(IProject project) {
		return project.getName()+"."+IProjectSetConstants.PROJECT_SET_FILE_EXTENSION; //$NON-NLS-1$
	}
	
	/**
	 * Returns the newly created project.
	 *
	 * @return the created project, or <code>null</code>
	 *   if project not created
	 */
	public IProject getNewProject() {
		return newProject;
	}

	/**
	 * Sets the build specification for a project set project
	 */	
	protected void setBuildSpec(IProjectDescription description) {
		ICommand projectSetBuilder = description.newCommand();
		projectSetBuilder.setBuilderName(IProjectSetConstants.PROJECT_SET_BUILDER_ID);
		ICommand[] commands = new ICommand[1];	
		commands[0] = projectSetBuilder;	
		description.setBuildSpec(commands);
	}
	
	public String getWindowTitle() {
		return Messages.getString("NewProjectSetProjectWizard.New_Project_Set_Project");  //$NON-NLS-1$
	}		

	/**
	 * Return the title for error dialog in case of fail of create of ProjectSetEntry
	 * 
	 * @return returns the title for error dialog
	 */
	private String getErrorDialogTitle() {
		return Messages.getString("NewProjectSetProjectWizard.Error");
	}

	/**
	 * Open error dialog in case of fail of create of ProjectSetEntry
	 */
	private void openErrorDialog(Shell shell, Exception e) {
		String errorMessage = Messages.getString("NewProjectSetProjectWizard.Error_PSF_file_creation"); // $NON-NLS-1$
		
		if (e instanceof NewProjectSetEntryInvTargetException) {
			Throwable target = ((NewProjectSetEntryInvTargetException) e).getTargetException();
			if ((target instanceof TeamException) && (target.getMessage() != null)) {
				errorMessage = errorMessage
						+ MessageFormat.format(Messages.getString("NewProjectSetProjectWizard.,_reason__{0}"), new Object[] { target.getMessage() }); // $NON-NLS-1$
			}
		} else {
			errorMessage = errorMessage + MessageFormat.format(Messages.getString("NewProjectSetProjectWizard.,_reason__{0}"), new Object[] { e.getMessage() }); // $NON-NLS-1$
		}
		MessageDialog.openError(shell, getErrorDialogTitle(), errorMessage);
	}

}
