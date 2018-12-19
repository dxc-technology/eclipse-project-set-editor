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
package com.csc.dip.projectset.ui.resolution;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;

import com.csc.dip.projectset.ProjectSet;
import com.csc.dip.projectset.ui.ProjectSetUI;

/**
 * @author Dirk Baumann
 *
 */
public class AddProjectSetToWorkspaceResolution implements IMarkerResolution {
	
	boolean loadRecursively;

	/**
	 * Constructor for AddProjectSetToWorkspaceResolution.
	 */
	public AddProjectSetToWorkspaceResolution(boolean loadRecursively) {
		super();
		this.loadRecursively = loadRecursively;
	}

	/**
	 * Returns the label for the resolution
	 */
	public String getLabel() {
		if (loadRecursively) {
			return Messages.getString("AddProjectSetToWorkspaceResolution.Add/Replace_projects_recursively"); //$NON-NLS-1$
		} else {
			return Messages.getString("AddProjectSetToWorkspaceResolution.Add/Replace_projects"); //$NON-NLS-1$
		}
	}

	/**
	 * Add/replace the project set
	 */
	public void run(IMarker marker) {
		IFile psfFile = (IFile)marker.getResource();
		if (psfFile.isAccessible()) {
			ProjectSet projectSet = new ProjectSet(psfFile, true);
			ProjectSetUI.addAllToWorkspace(projectSet, null, loadRecursively);
		}
	}

}
