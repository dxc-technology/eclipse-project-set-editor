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
 * Action for a project set file for adding content recursively to the workspace
 */

import com.csc.dip.projectset.ProjectSet;


public class AddProjectSetRecursiveToWorkspaceAction
	extends AddProjectSetToWorkspaceAction {

	/**
	 * Constructor for AddProjectSetRecursiveToWorkspaceAction.
	 */
	public AddProjectSetRecursiveToWorkspaceAction() {
		super();
	}

	//overwrite superclass method
	protected boolean hasUndefinedPathVariable(ProjectSet projectSet) {
		return projectSet.hasUndefinedPathVariable(true);
	}
	
	//overwrite superclass method
	protected void addProjectSetToWorkspace(ProjectSet projectSet) {
		ProjectSetUI.addAllToWorkspace(projectSet, shell, true);
	}	
}
