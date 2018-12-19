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
 * Label provider for tree viewer displaying a project set
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;

import com.csc.dip.projectset.ProjectSetEntry;


public class ProjectSetTreeLabelProvider extends LabelProvider implements ITableLabelProvider, ILabelProviderListener {
	
	private static Image stateLoadedImage;
	private static Image stateNotLoadedImage;
	private static Image stateLoadedNotSharedImage;
	private static Image stateLoadedDifferentImage;

	private static Image errorStateConflictImage;
	private static Image errorStateConflictInSubPsImage;
					
	static {
		// initialize images
		stateLoadedImage = ProjectSetUIPlugin.getDefault().getImage("icons/loaded.png"); //$NON-NLS-1$
		stateNotLoadedImage = ProjectSetUIPlugin.getDefault().getImage("icons/notloaded.png"); //$NON-NLS-1$
		stateLoadedNotSharedImage = ProjectSetUIPlugin.getDefault().getImage("icons/notshared.png"); //$NON-NLS-1$
		stateLoadedDifferentImage = ProjectSetUIPlugin.getDefault().getImage("icons/differentloaded.png"); //$NON-NLS-1$

		errorStateConflictImage = ProjectSetUIPlugin.getDefault().getImage("icons/conflict.png"); //$NON-NLS-1$
		errorStateConflictInSubPsImage = ProjectSetUIPlugin.getDefault().getImage("icons/conflictsub.png"); //$NON-NLS-1$
	}
	
	/**
	 * Creates a label provider and its images
	 */
	public ProjectSetTreeLabelProvider() {
		super();
		// register as listener for forwarding label changes of lightweight label decorators 
		ProjectsetUIUtil.DECORATING_WORKBENCH_LABEL_PROVIDER.addListener(this);
	}
	
	public void dispose() {
		ProjectsetUIUtil.DECORATING_WORKBENCH_LABEL_PROVIDER.removeListener(this);
		super.dispose();
	}
	
	/**
	 * Returns the label image for the given column of the given element.
	 *
	 * @param element the object representing the entire row, or 
	 *    <code>null</code> indicating that no input object is set
	 *    in the viewer
	 * @param columnIndex the zero-based index of the column in which
	 *   the label appears
	 */
	public Image getColumnImage(Object element, int columnIndex) {

		switch (columnIndex) {
			case 0 :
				return getLoadedProjectImage(element);
			case 2 :
				return getPathErrorImage(element);
			case 3 :
				return getErrorImage(element);												
			case 4 :
				return getStateImage(element);
			default :
				return null;
		}
	}
	
	/**
	 * Returns the decorated image for the loaded project 
	 */
	private Image getLoadedProjectImage(Object element) {
		IProject project = ((ProjectSetEntry)element).getProjectInWorkspace();
		if (project != null) {
			return ProjectsetUIUtil.DECORATING_WORKBENCH_LABEL_PROVIDER.getImage(project);
		} else {
			return null;
		}		
	}

	/**
	 * Returns the image for the state of the project set entry
	 */
	private Image getStateImage(Object element) {
		Image stateImage;
		switch (((ProjectSetEntry)element).getState()) {
			case ProjectSetEntry.STATE_LOADED : 
				stateImage = stateLoadedImage;
				break;
			case ProjectSetEntry.STATE_NOT_LOADED :
				stateImage = stateNotLoadedImage;
				break;
			case ProjectSetEntry.STATE_LOADED_NOT_SHARED :
				stateImage = stateLoadedNotSharedImage;
				break;
			case ProjectSetEntry.STATE_LOADED_DIFFERENT :
				stateImage = stateLoadedDifferentImage;
				break;
			default : stateImage = null;
		}
		return stateImage;
	}

	/**
	 * Returns the image for error of the project set entry
	 */
	protected Image getErrorImage(Object element) {
		Image errorImage;
		switch (((ProjectSetEntry)element).getErrorState(ProjectSetEntry.ERROR_TYPE_TAG_CONFLICT)) {
			case ProjectSetEntry.ERROR_STATE_ERROR : 
				errorImage = errorStateConflictImage;
				break;
			case ProjectSetEntry.ERROR_STATE_ERROR_ALSO_IN_SUB_PS : 
				errorImage = errorStateConflictImage;
				break;
			case ProjectSetEntry.ERROR_STATE_ERROR_ONLY_IN_SUB_PS : 
				errorImage = errorStateConflictInSubPsImage;
				break;									
			default : errorImage = null;
		}
		return errorImage;
	}

	/**
	 * Returns the image for error of the project set entry
	 */
	protected Image getPathErrorImage(Object element) {
		ProjectSetEntry psEntry = (ProjectSetEntry)element;
		Image errorImage;
		switch (psEntry.getWorstPreferredPathLocationErrorState()) {
			case ProjectSetEntry.ERROR_STATE_ERROR : 
				errorImage = errorStateConflictImage;
				break;
			case ProjectSetEntry.ERROR_STATE_ERROR_ALSO_IN_SUB_PS : 
				errorImage = errorStateConflictImage;
				break;
			case ProjectSetEntry.ERROR_STATE_ERROR_ONLY_IN_SUB_PS : 
				errorImage = errorStateConflictInSubPsImage;
				break;									
			default : errorImage = null;
		}
		return errorImage;
	}
		
				
	/**
	 * Returns the label text for the given column of the given element.
	 *
	 * @param element the object representing the entire row, or
	 *   <code>null</code> indicating that no input object is set
	 *   in the viewer
	 * @param columnIndex the zero-based index of the column in which the label appears
	 */
	public String getColumnText(Object element, int columnIndex) {
		ProjectSetEntry entry = (ProjectSetEntry)element;
		String displayText = null;
		switch (columnIndex) {
			case 0 :
				displayText = ProjectsetUIUtil.getLoadedProjectText(entry);
				break;			
			case 1 :
				displayText =  entry.getTag();
				break;
			case 2 :
				displayText =  entry.getPreferredPathLocationErrorDescriptions();
				break;								
			case 3 :
				displayText =  entry.getErrorDescription(ProjectSetEntry.ERROR_TYPE_TAG_CONFLICT);				
				break;								
			case 4 :
				displayText =  ProjectsetUIUtil.getStateDescription(entry);				
				break;
			case 5 :
				displayText =  entry.getProviderName();
				break;
			case 6 :
				displayText =  entry.getLocation();
				break;			
				
			default :
				displayText = ""; //$NON-NLS-1$
				break;
		}
		return displayText;
	}

	public void labelProviderChanged(LabelProviderChangedEvent event) {
		// forward change event
		fireLabelProviderChanged(event);
	}
}
