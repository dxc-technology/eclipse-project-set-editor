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
 * Action for a project set file for adding content to the workspace
 */

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;

import com.csc.dip.projectset.ProjectSet;

public class AddProjectSetToWorkspaceAction extends AbstractProjectSetAction {


	/**
	 * Constructor for ProjectSetLoadAction.
	 */
	public AddProjectSetToWorkspaceAction() {
		super();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		Iterator<IFile> fileIterator = getPSFFiles(selection).iterator();
		while (fileIterator.hasNext()) {
			IFile psfFile = fileIterator.next();
			addProjectsToWorkspace(psfFile);
		}		
	}

	/**
	 * Recursively adds/replaces all projects specified by the project set file
	 * @param resource
	 */
	protected void addProjectsToWorkspace(IFile psfFile) {
		ProjectSet projectSet = new ProjectSet(psfFile, true);
		if (hasUndefinedPathVariable(projectSet)) {
			MessageDialog.openError(
				shell,
				Messages.getString("AddProjectSetToWorkspaceAction.Error"), //$NON-NLS-1$
				MessageFormat.format(Messages.getString("AddProjectSetToWorkspaceAction.{0}_has_entries_with_undefined_path_variables"), new Object[] {psfFile.getName().toString()})); //$NON-NLS-1$
				
		} else {
			addProjectSetToWorkspace(projectSet);
		}
	}

	protected boolean hasUndefinedPathVariable(ProjectSet projectSet) {
		return projectSet.hasUndefinedPathVariable(false);
	}
	
	protected void addProjectSetToWorkspace(ProjectSet projectSet) {
		ProjectSetUI.addAllToWorkspace(projectSet, shell, false);
	}

}
