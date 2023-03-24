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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class AddProjectSetCollectionToWorkspaceAction extends AddProjectSetRecursiveToWorkspaceAction {
	
	public void run(IAction action) {
		final List<IFile> collectedPsfFiles = new ArrayList<IFile>();
		
		try {
			Iterator<IFile> psfcFilesiterator = getPSFCFiles().iterator();
			while (psfcFilesiterator.hasNext()) {
				IFile psfcFile = (IFile) psfcFilesiterator.next();
				addPSFFiles(psfcFile, collectedPsfFiles);
			}
		} catch (FileNotFoundException e) {
			MessageDialog.openError(shell, Messages.getString("AddProjectSetCollectionToWorkspaceAction.File_not_found_exception_while_loading"), e.getMessage()); //$NON-NLS-1$
			return;
		} catch (IOException e) {
			MessageDialog.openError(shell, Messages.getString("AddProjectSetCollectionToWorkspaceAction.IO_exception_while_loading"), e.getMessage()); //$NON-NLS-1$
			return;
		}
		
		WorkspaceModifyOperation loadOperation = new WorkspaceModifyOperation() {
			
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				monitor.beginTask(Messages.getString("AddProjectSetCollectionToWorkspaceAction.Load_Project_Set_File_Collection") , collectedPsfFiles.size()); //$NON-NLS-1$
				if (collectedPsfFiles != null && (!collectedPsfFiles.isEmpty())) {
					for (IFile psfFile : collectedPsfFiles) {
						monitor.subTask(MessageFormat.format(Messages.getString("AddProjectSetCollectionToWorkspaceAction.Load_0_."), new Object[]{psfFile.getLocation().toString()})); //$NON-NLS-1$
						addProjectsToWorkspace(psfFile);
						monitor.worked(1);
						if (monitor.isCanceled()) {
							break;
						}
					}
				}
				monitor.done();
			}
		};
		
		ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(shell);
		try {
			progressMonitorDialog.run(false, true, loadOperation);
		} catch (InvocationTargetException e) {
			MessageDialog.openError(shell, Messages.getString("AddProjectSetCollectionToWorkspaceAction.InvocationTargetException_while_loading"), e.getMessage()); //$NON-NLS-1$
		} catch (InterruptedException e) {
			MessageDialog.openError(shell, Messages.getString("AddProjectSetCollectionToWorkspaceAction.Loading_cancelled"), e.getMessage()); //$NON-NLS-1$
		}
	}
	
	protected List<IFile> getPSFCFiles() {
		List<IFile> psfcFiles = new ArrayList<IFile>();
		
		if (selection instanceof IStructuredSelection) {			
			Iterator<?> selectionIterator = ((IStructuredSelection)selection).iterator();
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
					// add psfc files
					if ((resource.getType() == IResource.FILE) && (resource.exists()) && (resource.getFileExtension().toLowerCase().equals("psfc"))) { //$NON-NLS-1$
						psfcFiles.add((IFile) resource);
				 	}
				}			
			}
		}
		return psfcFiles;
	}

	protected void addPSFFiles(IFile psfcFile, List<IFile> collectedPsfFiles)
			throws FileNotFoundException, IOException {
		try (FileReader fr = new FileReader(psfcFile.getLocation().toFile())) {
			try (BufferedReader bufferedReader = new BufferedReader(fr)) {
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					String trimmedLine = line.trim();
					if (!(trimmedLine.startsWith("#") || trimmedLine.startsWith("!"))) {
						IFile file = getFileForString(trimmedLine);
						if (file == null || (!file.exists())) {
							throw new FileNotFoundException(MessageFormat.format(
									Messages.getString("AddProjectSetCollectionToWorkspaceAction.File_0_not_found"), //$NON-NLS-1$
									new Object[] { trimmedLine }));
						}
						if (trimmedLine.toLowerCase().endsWith("psfc")) {
							addPSFFiles(file, collectedPsfFiles);
						} else {
							if (trimmedLine.toLowerCase().endsWith("psf")) {
								collectedPsfFiles.add(file);
							}
						}
					}
				}
			}
		}
	}
	
	protected IFile getFileForString(String fileName) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		return workspaceRoot.getFile(new Path(fileName));
	}
}
