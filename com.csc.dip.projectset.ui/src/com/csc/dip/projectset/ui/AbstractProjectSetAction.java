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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.csc.dip.projectset.ProjectSetNature;

/**
 * @author dbaumann
 */
public abstract class AbstractProjectSetAction implements IObjectActionDelegate {

	protected Shell shell;

	protected ISelection selection;
	
	/**
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	/**
	 * Extracts the project set files from the selection
	 * @param selection
	 * @return List with IFile objects
	 */
	protected List<IFile> getPSFFiles(ISelection selection) {
		List<IFile> psfFiles = new ArrayList<IFile>();
		
		if (selection instanceof IStructuredSelection) {			
			Iterator selectionIterator = ((IStructuredSelection)selection).iterator();
			while (selectionIterator.hasNext()) {
				Object next = selectionIterator.next();
				IResource resource = null;
				if (next instanceof IResource) {
					resource = (IResource)next;
				} else
				if (next instanceof IAdaptable) {
					resource = (IResource)(((IAdaptable) next).getAdapter(IResource.class));
				}
				if (resource != null) {
					// add psf files
					if ((resource.getType() == IResource.FILE) && (resource.exists()) && (resource.getFileExtension().toLowerCase().equals("psf"))) { //$NON-NLS-1$
						psfFiles.add((IFile) resource);
				 	}
				 	// add psf files of project set projects
					if ((resource.getType() == IResource.PROJECT) && 
					 	resource.isAccessible()) {
					 	IProject project = (IProject)resource;
						ProjectSetNature psNature = null;
						try {
							psNature = (ProjectSetNature) project.getNature(ProjectSetNature.PS_PROJECT_NATURE_ID);	
						} catch (CoreException e) {
							// do nothing
						}
					 	if (psNature != null) {
					 		IFile psfFile = psNature.getProjectSetFile();
					 		if ((psfFile != null) && (psfFile.isAccessible())) {
					 			psfFiles.add(psfFile);
					 		}
					 	}
					}
				}			
			}
		}
		return psfFiles;
	}
}
