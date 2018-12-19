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
 * This builder checks for errors in a project set project
 * and update the problem markers
 */

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


public class ProjectSetBuilder extends IncrementalProjectBuilder {
	
	/**
	 * An array of zero or more listeners to be notified
	 * when a builder has changed a resource.
	 */
	private static Vector<IProjectSetBuilderListener> listeners = new Vector<IProjectSetBuilderListener>();

	/**
	 * Constructor for ProjectSetBuilder.
	 */
	public ProjectSetBuilder() {
		super();
	}

	/**
	 * @see IncrementalProjectBuilder#build(int, Map, IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
		throws CoreException {
				
		updateErrorMarkers(getProject());
		
		return new IProject[0];
	}

	/**
	 * Updates the project set markers for the project
	 */	
	protected void updateErrorMarkers(IProject project) {

		removePsfMarkers(project);		
		checkAndCreateMarkers(project);
		fireMarkersChanged(project);
	}

	/**
	 * Removes all project set markers for the project
	 */
	protected void removePsfMarkers(IProject project) {
		try {
			project.deleteMarkers(
				IProjectSetConstants.PSF_MARKER_ID,
				true,
				IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
		}
	}

	/**
	 * Checks for conflicts and create error markers for the project
	 */
	protected void checkAndCreateMarkers(IProject project) {
		ProjectSetNature psNature = null;
		try {
			psNature =
				(ProjectSetNature) project.getNature(
					ProjectSetNature.PS_PROJECT_NATURE_ID);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (psNature !=  null) {
			// check existence of .psproject file
			IFile psDescriptionFile = psNature.getPSProjectDescriptionFile();
			if (!(psDescriptionFile.exists() && psDescriptionFile.isAccessible())) {
				createErrorMarker(project, Messages.getString("ProjectSetBuilder.File_psproject_is_missing_or_not_valid")); //$NON-NLS-1$
			} else {
				// check project set state
				IFile projectSetFile = psNature.getProjectSetFile();
				if (projectSetFile == null) {
					createErrorMarker(
						psDescriptionFile,
						Messages.getString("ProjectSetBuilder.Invalid_project_set_project_description_file")); //$NON-NLS-1$
				} else {
					if (!(projectSetFile.exists() && projectSetFile.isAccessible())) {
						createErrorMarker(
							psDescriptionFile,
							MessageFormat.format(Messages.getString("ProjectSetBuilder.missing_PSF_file_named_{0}_1"), new Object[] {projectSetFile.getFullPath().toString()})); //$NON-NLS-1$
					} else {
						ProjectSet projectSet= psNature.getProjectSet();
						projectSet.updateEntries();
						ProjectSetEntry[] errorEntries = projectSet.updateErrors();
	
						for (int i = 0; i < errorEntries.length; i++) {
							ProjectSetEntry projectSetEntry = errorEntries[i];
							checkAndCreateMarker(
								projectSetFile,
								projectSetEntry,
								ProjectSetEntry.ERROR_TYPE_TAG_CONFLICT,
								IProjectSetConstants.PSF_TAG_CONFLICT_MARKER_ID,
								IMarker.SEVERITY_ERROR);
							checkAndCreateMarker(
								projectSetFile,
								projectSetEntry,
								ProjectSetEntry.ERROR_TYPE_PATH_VARIABLE_UNDEFINED,
								IProjectSetConstants.PSF_ERROR_MARKER_ID,
								IMarker.SEVERITY_ERROR);
							checkAndCreateMarker(
								projectSetFile,
								projectSetEntry,
								ProjectSetEntry.ERROR_TYPE_PATH_CONFLICT,
								IProjectSetConstants.PSF_PATH_CONFLICT_MARKER_ID,
								IMarker.SEVERITY_ERROR);
							checkAndCreateMarker(
								projectSetFile,
								projectSetEntry,
								ProjectSetEntry.ERROR_TYPE_NOT_LOADED_TO_PREFERRED_PATH,
								IProjectSetConstants.PSF_NOT_LOADED_TO_PREFERRED_MARKER_ID,
								IMarker.SEVERITY_WARNING);
							checkAndCreateMarker(
								projectSetFile,
								projectSetEntry,
								ProjectSetEntry.ERROR_TYPE_SAME_PATH_CONFLICT,
								IProjectSetConstants.PSF_PATH_CONFLICT_MARKER_ID,
								IMarker.SEVERITY_WARNING);								
						}
						
						if (!projectSet.isLoaded()) {
							createMarker(
								projectSetFile,
								IProjectSetConstants.PSF_NOT_LOADED_MARKER_ID,
								IMarker.SEVERITY_WARNING,
								Messages.getString("ProjectSetBuilder.Project_set_not_completely_loaded")); //$NON-NLS-1$
						}
					}
				}
			}
		}		
	}

	protected void checkAndCreateMarker(
		IResource resource,
		ProjectSetEntry projectSetEntry,
		int errorType,
		String markerType,
		int severity) {
		if (projectSetEntry.getErrorState(errorType) != ProjectSetEntry.ERROR_STATE_OK) {
			createMarker(
				resource,
				markerType,
				severity,
				MessageFormat.format(Messages.getString("ProjectSetBuilder.Problem_at_project_set_entry_{0}__{1}_2"), new Object[] {projectSetEntry.getTreePositionString(),projectSetEntry.getErrorDescription(errorType)})); //$NON-NLS-1$
		}		
	}

	/**
	 * Creates an error marker for the resource
	 */
	protected void createErrorMarker(IResource resource, String message) {
		createMarker(
			resource,
			IProjectSetConstants.PSF_ERROR_MARKER_ID,
			IMarker.SEVERITY_ERROR,
			message);
	}

	/**
	 * Creates a marker for the resource
	 */		
	protected void createMarker(IResource resource, String type, int severity, String message) {
		if ((resource != null) && (resource.exists())) {

			try {
				IMarker marker =
					resource.createMarker(type);
				marker.setAttribute(IMarker.SEVERITY, severity);
				marker.setAttribute(IMarker.MESSAGE, message);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Add a listener to be notified
	 * when a builder has changed a resource.
	 * 
	 * @param listener the listener object
	 */
	public static synchronized void addProjectSetBuilderListener(IProjectSetBuilderListener listener) {
		if (listener != null && !listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Remove a listener so that they are no longer notified
	 * when a builder has changed a resource.
	 * 
	 * @param listener the listener object
	 */
	public static synchronized void removeProjectSetBuilderListener(IProjectSetBuilderListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Fire an event indicating that the markers for a project have changed.
	 * 
	 * @param project the project whose markers have changed
	 */
	protected static synchronized void fireMarkersChanged(IProject project) {
		for (Iterator<IProjectSetBuilderListener> iter = listeners.iterator(); iter.hasNext();){
			iter.next().markersChanged(project);
		}
	}
	
}
