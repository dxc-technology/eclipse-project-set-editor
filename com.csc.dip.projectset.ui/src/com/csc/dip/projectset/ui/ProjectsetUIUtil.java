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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.csc.dip.projectset.ProjectSetEntry;

public class ProjectsetUIUtil {
	
	public static final ILabelProvider DECORATING_WORKBENCH_LABEL_PROVIDER = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
	
	public static String getPropertyValueAsString(ProjectSetEntry projectSetEntry, int projectSetEntryProperty) {
		switch (projectSetEntryProperty) {
			case IProjectSetUIConstants.PROJECT_SET_ENTRY_PROJECT_NAME:
				return getLoadedProjectText(projectSetEntry);
				
			case IProjectSetUIConstants.PROJECT_SET_ENTRY_TAG:
				return projectSetEntry.getTag();
				
			case IProjectSetUIConstants.PROJECT_SET_ENTRY_PREFERRED_PATH_PROBLEMS:
				return projectSetEntry.getPreferredPathLocationErrorDescriptions();
				
			case IProjectSetUIConstants.PROJECT_SET_ENTRY_TAG_CONFLICTS:
				return projectSetEntry.getErrorDescription(ProjectSetEntry.ERROR_TYPE_TAG_CONFLICT);
				
			case IProjectSetUIConstants.PROJECT_SET_ENTRY_STATE:
				return getStateDescription(projectSetEntry);
				
			case IProjectSetUIConstants.PROJECT_SET_ENTRY_PROVIDER:
				return projectSetEntry.getProviderName();
				
			case IProjectSetUIConstants.PROJECT_SET_ENTRY_SERVER_LOCATION:
				return projectSetEntry.getLocation();
				
			default:
				return ""; //$NON-NLS-1$
		}
	}

	
	/**
	 * Returns the decorated Text for the loaded project 
	 */
	public static String getLoadedProjectText(ProjectSetEntry psEntry ) {
		IProject project = psEntry.getProjectInWorkspace();
		if (project != null) {
			return DECORATING_WORKBENCH_LABEL_PROVIDER.getText(project);
		} else {
			return psEntry.getProjectName();
		}		
	}
	
	/**
	 * Return the text describing the load state of the entry
	 */	
	public static String getStateDescription(ProjectSetEntry projectSetEntry) {
		switch (projectSetEntry.getState()) {
			case ProjectSetEntry.STATE_LOADED : return Messages.getString("ProjectSetTreeLabelProviderLoaded"); //$NON-NLS-1$
			case ProjectSetEntry.STATE_NOT_LOADED : return Messages.getString("ProjectSetTreeLabelProviderNot_loaded"); //$NON-NLS-1$
			case ProjectSetEntry.STATE_LOADED_NOT_SHARED : return Messages.getString("ProjectSetTreeLabelProviderLoaded_not_shared"); //$NON-NLS-1$
			case ProjectSetEntry.STATE_LOADED_DIFFERENT : return Messages.getString("ProjectSetTreeLabelProviderLoaded_different"); //$NON-NLS-1$
			default : return Messages.getString("ProjectSetTreeLabelProviderunknown_state"); //$NON-NLS-1$
		}
	}
}
