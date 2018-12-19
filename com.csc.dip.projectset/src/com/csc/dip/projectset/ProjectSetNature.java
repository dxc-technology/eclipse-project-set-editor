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
package com.csc.dip.projectset;

/**
 * @author Dirk Baumann
 * 
 *
 * This is the nature for a project set project
 */

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public class ProjectSetNature implements IProjectNature {

	public static final String PS_PROJECT_NATURE_ID = "com.csc.dip.projectset.ProjectSetNature"; //$NON-NLS-1$

	public static final String PS_PROJECT_DESCRIPTION_FILENAME = ".psproject"; //$NON-NLS-1$

	private static final QualifiedName CACHED_PS_PROJECT_DESCRIPTION_QUALIFIED_NAME = new QualifiedName(null, "psProjectDescription"); //$NON-NLS-1$
	private static final QualifiedName CACHED_PS_PROJECT_DESCRIPTION_MODIFICATION_STAMP_QUALIFIED_NAME = new QualifiedName(null, "psProjectDescriptionModificationStamp"); //$NON-NLS-1$

	private static final QualifiedName CACHED_PROJECT_SET_QUALIFIED_NAME = new QualifiedName(null, "projectSet"); //$NON-NLS-1$
	private static final QualifiedName CACHED_PROJECT_SET_MODIFICATION_STAMP_QUALIFIED_NAME = new QualifiedName(null, "projectSetModificationStamp"); //$NON-NLS-1$
		
	private IProject project;
	
	/**
	 * Constructor for ProjectSetNature.
	 */
	public ProjectSetNature() {
		super();
	}

	/**
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
	}

	/**
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
	}

	/**
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * @see org.eclipse.core.resources.IProjectNature#setProject(IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

	/**
	 * Returns the file for the project containing the location of the project set file
	 */
	protected IFile getPSProjectDescriptionFile() {
		return project.getFile(PS_PROJECT_DESCRIPTION_FILENAME);	
	}

	/**
	 * Creates the file for the project containing the location of the project set file
	 */	
	public void createPSProjectDescriptionFile(ProjectSetProjectDescription projectSetProjectDescription) {
		projectSetProjectDescription.writeToFile(getPSProjectDescriptionFile());	
	}

	/**
	 * Returns the description containing the location of the project set file
	 */	
	public ProjectSetProjectDescription getProjectSetProjectDescription() {
		IFile psDescriptionFile = getPSProjectDescriptionFile();
		if (psDescriptionFile == null) {
			return null;
		}
		// try to get cached description
		ProjectSetProjectDescription psProjectDescription = null;
		try {
			long currentModificationStamp = psDescriptionFile.getModificationStamp();	
			if (currentModificationStamp == IResource.NULL_STAMP) {
				// file either does not exist or exists as a closed project
				return null;
			}
			Long lastModificationStamp = (Long) psDescriptionFile.getSessionProperty(CACHED_PS_PROJECT_DESCRIPTION_MODIFICATION_STAMP_QUALIFIED_NAME);
			if (lastModificationStamp != null) {
				if (lastModificationStamp.equals(new Long(currentModificationStamp))) {
					psProjectDescription =(ProjectSetProjectDescription) psDescriptionFile.getSessionProperty(CACHED_PS_PROJECT_DESCRIPTION_QUALIFIED_NAME);
				}
			}
			if (psProjectDescription == null) {
				psProjectDescription = ProjectSetProjectDescription.readFromFile(getPSProjectDescriptionFile());
				psDescriptionFile.setSessionProperty(CACHED_PS_PROJECT_DESCRIPTION_QUALIFIED_NAME, psProjectDescription);
				psDescriptionFile.setSessionProperty(CACHED_PS_PROJECT_DESCRIPTION_MODIFICATION_STAMP_QUALIFIED_NAME, new Long(currentModificationStamp));
			}
		} catch (CoreException e) {
			// TODO: handle exception
			return null;
		}
		return psProjectDescription;
	}

	/**
	 * Returns the project set file for the project specified by the project description file
	 * or null if not exists
	 */
	public IFile getProjectSetFile() {
		ProjectSetProjectDescription psProjectDescription = getProjectSetProjectDescription();
		if (psProjectDescription == null) {
			return null;
		}
		return project.getFile(psProjectDescription.getPsfFilename());
	}

	/**
	 * Returns the project set for the project
	 * or null if not exists
	 */	
	public ProjectSet getProjectSet() {
		IFile psfFile = getProjectSetFile();
		if (psfFile == null) {
			return null;
		}
		// try to get cached project set
		ProjectSet projectSet = null;
		try {
			long currentModificationStamp = psfFile.getModificationStamp();	
			if (currentModificationStamp == IResource.NULL_STAMP) {
				// file either does not exist or exists as a closed project
				return null;
			}
			Long lastModificationStamp = (Long) psfFile.getSessionProperty(CACHED_PROJECT_SET_MODIFICATION_STAMP_QUALIFIED_NAME);
			if (lastModificationStamp != null) {
				if (lastModificationStamp.equals(new Long(currentModificationStamp))) {
					projectSet =(ProjectSet) psfFile.getSessionProperty(CACHED_PROJECT_SET_QUALIFIED_NAME);
				}
			}
			if (projectSet == null) {
				projectSet = new ProjectSet(psfFile, false);
				psfFile.setSessionProperty(CACHED_PROJECT_SET_QUALIFIED_NAME, projectSet);
				psfFile.setSessionProperty(CACHED_PROJECT_SET_MODIFICATION_STAMP_QUALIFIED_NAME, new Long(currentModificationStamp));
				projectSet.update();
			}
		} catch (CoreException e) {
			// TODO: handle exception
			return null;
		}
		return projectSet;
	}			
}
