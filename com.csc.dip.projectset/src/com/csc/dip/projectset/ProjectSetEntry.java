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
 * A Project set entry is an item of a project set and
 * represents a project reference
 */

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;

public class ProjectSetEntry implements IAdaptable {

	// constants for state
	public static final int STATE_LOADED = 0;
	public static final int STATE_NOT_LOADED = 1;
	public static final int STATE_LOADED_NOT_SHARED = 2;
	public static final int STATE_LOADED_DIFFERENT = 3;

	// constants for error types
	public static final int NUMBER_OF_ERROR_TYPES =5;
	public static final int ERROR_TYPE_TAG_CONFLICT = 0;
	public static final int ERROR_TYPE_PATH_CONFLICT = 1;
	public static final int ERROR_TYPE_PATH_VARIABLE_UNDEFINED = 2;
	public static final int ERROR_TYPE_NOT_LOADED_TO_PREFERRED_PATH = 3;
	public static final int ERROR_TYPE_SAME_PATH_CONFLICT = 4;
	
	// constants for error state	
	public static final int ERROR_STATE_OK = 0;
	public static final int ERROR_STATE_ERROR_ONLY_IN_SUB_PS = 1;	
	public static final int ERROR_STATE_ERROR = 2;
	public static final int ERROR_STATE_ERROR_ALSO_IN_SUB_PS = 3;

		
	private ProjectSet projectSet; // the project set this instance is part of
	private String provider;
	private String projectReference;
	
	private String unresolvedPreferredLocation;
	
	private int state;
	private ProjectSet subProjectSet;
	
	private int[] errorStates = {ERROR_STATE_OK, ERROR_STATE_OK, ERROR_STATE_OK, ERROR_STATE_OK, ERROR_STATE_OK};
	private String[] errorDescriptions = {"","","","",""}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	
	
	/**
	 * Constructor for ProjectSetEntry.
	 */
	public ProjectSetEntry(ProjectSet projectSet, String provider, String projectReference) {
		super();
		this.projectSet = projectSet;
		this.provider = provider;
		this.projectReference = projectReference;
	}

	/**
	 * Creates an entry for the project The project must be loaded and shared !
	 * 
	 * @throws NewProjectSetEntryInvTargetException
	 * @throws NewProjectSetEntryException
	 */
	public ProjectSetEntry(ProjectSet projectSet, IProject loadedAndSharedProject) throws NewProjectSetEntryInvTargetException, NewProjectSetEntryException {
		if (loadedAndSharedProject == null) {
			throw new NewProjectSetEntryException(Messages.getString("ProjectSetEntry.Project_null"));
		}
		if(!loadedAndSharedProject.isAccessible()) {
			throw new NewProjectSetEntryException(Messages.getString("ProjectSetEntry.Project_not_accessible") + loadedAndSharedProject.getName());
		}
		
		RepositoryProvider providerOfLoadedProject = RepositoryProvider.getProvider(loadedAndSharedProject);
		if (providerOfLoadedProject == null) {
			throw new NewProjectSetEntryException(Messages.getString("ProjectSetEntry.Project_not_shared") + loadedAndSharedProject.getName());
		}
		provider = providerOfLoadedProject.getID();
		
		IProjectReferenceAnalyser prAnalyser = ProjectSetUtil.getProjectReferenceAnalyser(provider);
		if (prAnalyser == null) {
			throw new NewProjectSetEntryException(Messages.getString("ProjectSetEntry.Project_reference_analyser_not_found_for_provider") + provider);
		}	
		IProject[] projects = new IProject[1];
		projects[0] = loadedAndSharedProject;
		
		String loadedReferences[] = null;
		String projectSetFilename = null;
		if (projectSet != null) {
			projectSetFilename = projectSet.getProjectSetFilename();
		}
		try {
			loadedReferences = prAnalyser.asReference(projects, new ProjectSetContext(), new NullProgressMonitor(), projectSetFilename);
			if (loadedReferences != null) {
				projectReference = loadedReferences[0];
			}
		} catch (TeamException e) {
			throw new NewProjectSetEntryInvTargetException(e);
		}
	}

	/**
	 * Returns the loaded project if adapter is IProject or IResource
	 */	
	public Object getAdapter(Class adapter) {
		if ((adapter == IProject.class) ||
		   (adapter == IResource.class)) {
			return getProjectInWorkspace();
		} else {
			return null;
		}
	}

	/**
	 * Returns a copy of this ProjectSetEntry
	 * No deep copy, copy only reference to sub project set
	 */
	public ProjectSetEntry copy() {
		ProjectSetEntry copy = new ProjectSetEntry(projectSet, provider, projectReference);
		
		copy.unresolvedPreferredLocation = unresolvedPreferredLocation;
		copy.state = state;
		copy.subProjectSet = subProjectSet;

		System.arraycopy(errorStates, 0, copy.errorStates, 0, errorStates.length);
		System.arraycopy(errorDescriptions, 0, copy.errorDescriptions, 0, errorDescriptions.length);
		
		return copy;
	}

	/**
	 * Updates the state and the sub project set recursively
	 */
	public void update() {
		update(new HashSet<IFile>());
	}

	/**
	 * Updates the state and the sub project set recursively
	 */	
	protected void update(Set<IFile> updatedProjectSetFiles) {
		updateState();
		updateSubProjectSet(updatedProjectSetFiles);
	}

	/** 
	 * Returns the project with the entry project name that is loaded 
	 * in the workspace or null if it is not loaded
	 */
	public IProject getProjectInWorkspace() {
		IProject project = ProjectSetPlugin.getWorkspace().getRoot().getProject(getProjectName());
		if (project.isAccessible()) {
			return project;
		} else {
			return null;
		}
	}

	/** 
	 * Updates the state of the project
	 */
	protected void updateState() {
		IProject loadedProject = getProjectInWorkspace();
		if (loadedProject == null) {
			state = STATE_NOT_LOADED;
		} else {		
			RepositoryProvider providerOfLoadedProject = RepositoryProvider.getProvider(loadedProject);
			if (providerOfLoadedProject == null) {
				state =STATE_LOADED_NOT_SHARED;
			} else {
				if (!providerOfLoadedProject.getID().equals(getProvider())) {
					state = STATE_LOADED_DIFFERENT;
				} else {
					IProjectReferenceAnalyser prAnalyser = ProjectSetUtil.getProjectReferenceAnalyser(providerOfLoadedProject.getID());
					IProject[] projects = new IProject[1];
					projects[0] = loadedProject;
					String loadedReferences[] = null;
					try {
						loadedReferences = prAnalyser.asReference(projects, new ProjectSetContext(), new NullProgressMonitor(), projectSet.getProjectSetFilename());
					} catch (TeamException e) {
					}
					
					if (loadedReferences ==  null) {
						// ?? 
						state = STATE_LOADED_NOT_SHARED;
					} else {
						if (prAnalyser.areEqual(getProjectReference(), loadedReferences[0])) {
							state = STATE_LOADED;
						} else { 
							state = STATE_LOADED_DIFFERENT;
						}
					}
				}
			}
		}
	}
	
	/** 
	 * Updates the sub project set recursively
	 * 
	 * pre: state must be up to date 
	 */
	protected void updateSubProjectSet(Set<IFile> updatedProjectSetFiles) {
		subProjectSet = null;
		if (state == STATE_LOADED) {
			// check a project set project
			IProject project = getProjectInWorkspace();
			ProjectSetNature psNature = null;
			try {
				if (project != null) {
					psNature =(ProjectSetNature) project.getNature(
								 ProjectSetNature.PS_PROJECT_NATURE_ID);
				}
			} catch (CoreException e) {
				e.printStackTrace();
				return;
			}
			if (psNature != null) {
				// get project set from project
				IFile psFile = psNature.getProjectSetFile();
				if ((psFile != null) && 
					(psFile.isAccessible()) &&
					(!updatedProjectSetFiles.contains(psFile))) {
					//subProjectSet = new ProjectSet(psFile, false);
					subProjectSet = psNature.getProjectSet();
					subProjectSet.setParentProjectSetEntry(this);				
					subProjectSet.update(updatedProjectSetFiles);
				}
			}
		}
	}
	
	/**
	 * Compares the content with an other project set entry.
	 * 
	 * Don't overwrite equals, because instances of ProjectSetEntry should be
	 * distinguishable from each other, also if they have the same content
	 * (e.g. instances are used as items in the tree view of the PSF Editor)
	 */
	public boolean contentEqualsIgnorePreferredLocation(Object o) {
		if (o == null)
			return false;
			
		if (o == this) 
			return true;
		
		if (!(o instanceof ProjectSetEntry))
			return false;
		
		ProjectSetEntry compareEntry = (ProjectSetEntry)o;
		
		if(!provider.equals(compareEntry.getProvider()))
			return false;
		if(!projectReference.equals(compareEntry.getProjectReference()))
			return false;
		return true;
	}
	
	/**
	 * Returns a string representation of this entry
	 */
	public String toString() {
		return "ProjectSetEntry (provider="+provider+", reference="+projectReference+")";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * Sets the data form an other entry
	 * Do not overwrite preferred project location
	 */
	public void setDataFrom(ProjectSetEntry sourceEntry) {
		provider = sourceEntry.getProvider();
		projectReference = sourceEntry.getProjectReference();		
	}
		
	/**
	 * Returns a short name for the team provider 
	 */
	public String getProviderName() {
		return getProjectReferenceAnalyser().getProviderName();
	}

	/**
	 * Returns the team provider 
	 */		
	public String getProvider() {
		return provider;
	}

	/**
	 * Sets the team provider 
	 */	
	public void setProvider(String provider) {
		this.provider = provider;
	}

	/**
	 * Returns the project reference
	 */		
	public String getProjectReference() {
		return projectReference;
	}

	/**
	 * Sets the project reference
	 */	
	public void setProjectReference(String projectReference) {
		this.projectReference = projectReference;
	}

	/**
	 * Returns the project name retrieved from the project reference
	 */			
	public String getProjectName() {
		return getProjectReferenceAnalyser().getProjectName(projectReference);
	}

	/**
	 * Returns the server location retrieved from the project reference
	 */	
	public String getLocation() {
		return getProjectReferenceAnalyser().getLocation(projectReference);
	}

	/**
	 * Returns the tag retrieved from the project reference
	 */		
	public String getTag() {
		return getProjectReferenceAnalyser().getTag(projectReference);
	}

	/**
	 * Returns the project reference analyser for the provider
	 */
	private IProjectReferenceAnalyser getProjectReferenceAnalyser() {
		return ProjectSetUtil.getProjectReferenceAnalyser(provider);
	}
	
	/**
	 * Returns the projectSet.
	 * @return ProjectSet
	 */
	public ProjectSet getProjectSet() {
		return projectSet;
	}

	/**
	 * Sets the projectSet.
	 * @param projectSet The projectSet to set
	 */
	protected void setProjectSet(ProjectSet projectSet) {
		this.projectSet = projectSet;
	}

	/** 
	 * Returns a string representing the position of
	 * this entry in the project set tree
	 */
	public String getTreePositionString() {
		return getTreePositionString(new HashSet<File>());
	}

	/** 
	 * Returns a string representing the position of
	 * this entry in the project set tree
	 * Uses a set to avoid infinite recursion
	 */
	private String getTreePositionString(Set<File> parentProjectSetFiles) {
		ProjectSet ps = getProjectSet();
		if (ps != null)  {
			if (parentProjectSetFiles.add(ps.getProjectSetFile().getFullPath().toFile())) {
				ProjectSetEntry parentEntry = ps.getParentProjectSetEntry();
				if (parentEntry != null) {
					return parentEntry.getTreePositionString(parentProjectSetFiles)+"/"+getProjectName(); //$NON-NLS-1$
				}
				return getProjectName();
			}else {
				// infinite recursion
				return ".../"; //$NON-NLS-1$
			}		
		}
		return getProjectName();
	}
	
	/**
	 * Returns the subProjectSet.
	 * @return ProjectSet
	 */
	public ProjectSet getSubProjectSet() {
		return subProjectSet;
	}

	/** 
	 * Returns the state of the entry.
	 * Possible return values:
	 *
	 * STATE_LOADED
	 * STATE_NOT_LOADED 
	 * STATE_LOADED_NOT_SHARED
	 * STATE_LOADED_DIFFERENT 
	 * 
	 * pre: state should be updated by method updateState
	 *      before this method
	 */
	public int getState() {
		return state;
	}
		
	/**
	 * Returns the errorDescription.
	 * 
	 * 	Possible error types:
	 * 
	 * 	ERROR_TYPE_TAG_CONFLICT
	 *  ERROR_TYPE_PATH_CONFLICT
	 *  ERROR_TYPE_PATH_VARIABLE_UNDEFINED
	 *  ERROR_TYPE_NOT_LOADED_TO_PREFERRED_PATH
	 *  ERROR_TYPE_SAME_PATH_CONFLICT
	 * 
	 * @return String
	 * 
	 * pre: updateErrors of the set that contains this entry should be called
	 *      before this method
	 */
	public String getErrorDescription(int errorType) {
		return errorDescriptions[errorType];
	}

	/**
	 * Sets the errorDescription.
	 * 
	 * 	Possible error types:
	 *    ERROR_TYPE_TAG_CONFLICT
	 *    ERROR_TYPE_PATH_CONFLICT
	 *    ERROR_TYPE_PATH_VARIABLE_UNDEFINED
	 *    ERROR_TYPE_NOT_LOADED_TO_PREFERRED_PATH
	 *    ERROR_TYPE_SAME_PATH_CONFLICT
	 * 
	 * @param errorDescription The errorDescription to set
	 */
	public void setErrorDescription(int errorType, String errorDescription) {
		this.errorDescriptions[errorType] = errorDescription;
	}

	/**
	 * Returns the errorState.
	 * 
	 * 	Possible error types:
	 *    ERROR_TYPE_TAG_CONFLICT
	 *    ERROR_TYPE_PATH_CONFLICT
	 *    ERROR_TYPE_PATH_VARIABLE_UNDEFINED
	 *    ERROR_TYPE_NOT_LOADED_TO_PREFERRED_PATH
	 *    ERROR_TYPE_SAME_PATH_CONFLICT
	 * 
	 * Possible return values:
	 *    ERROR_STATE_OK
	 *    ERROR_STATE_ERROR
	 *    ERROR_STATE_ERROR_ONLY_IN_SUB_PS	
	 *    ERROR_STATE_ERROR_ALSO_IN_SUB_PS
	 *
	 * pre: updateErrors of the set that contains this entry should be called
	 *      before this method
	 */
	public int getErrorState(int errorType) {
		return errorStates[errorType];
	}
	
	/**
	 * Sets the errorState.
	 * 
	 * 	Possible error types:
	 *    ERROR_TYPE_TAG_CONFLICT
	 *    ERROR_TYPE_PATH_CONFLICT
	 *    ERROR_TYPE_PATH_VARIABLE_UNDEFINED
	 *    ERROR_TYPE_NOT_LOADED_TO_PREFERRED_PATH
	 *    ERROR_TYPE_SAME_PATH_CONFLICT
	 * 
	 * Possible return values:
	 *    ERROR_STATE_OK
	 *    ERROR_STATE_ERROR
	 *    ERROR_STATE_ERROR_ONLY_IN_SUB_PS	
	 *    ERROR_STATE_ERROR_ALSO_IN_SUB_PS
	 */
	public void setErrorState(int errorType, int errorState) {
		this.errorStates[errorType] = errorState;
	}


		
	/**
	 * resets the error states and descriptions
	 */
	public void resetError() {
		for (int i = 0; i < NUMBER_OF_ERROR_TYPES; i++) {
			errorStates[i] = ERROR_STATE_OK;
			errorDescriptions[i] = ""; //$NON-NLS-1$
		}
	}

	/** 
	 * return the worst error state of the following error types
	 * 	ERROR_TYPE_PATH_CONFLICT
	 *  ERROR_TYPE_PATH_VARIABLE_UNDEFINED
	 *  ERROR_TYPE_NOT_LOADED_TO_PREFERRED_PATH
	 *  ERROR_TYPE_SAME_PATH_CONFLICT
	 * 
	 * Error order: (best to worst)
	 * ERROR_STATE_OK
	 * ERROR_STATE_ERROR_ONLY_IN_SUB_PS	
	 * ERROR_STATE_ERROR
	 * ERROR_STATE_ERROR_ALSO_IN_SUB_PS
	 */
	public int getWorstPreferredPathLocationErrorState() {
		int worstState = ERROR_STATE_OK;

		int varUndefinedErrorState = getErrorState(ERROR_TYPE_PATH_VARIABLE_UNDEFINED);
		int pathConflictErrorState = getErrorState(ERROR_TYPE_PATH_CONFLICT);
		int notLoadedToPreferredErrorState = getErrorState(ERROR_TYPE_NOT_LOADED_TO_PREFERRED_PATH);
		int samePathErrorState = getErrorState(ERROR_TYPE_SAME_PATH_CONFLICT);
		
		if (varUndefinedErrorState > worstState) {
			worstState = varUndefinedErrorState;
		}
		if (pathConflictErrorState > worstState) {
			worstState = pathConflictErrorState;
		}
		if (notLoadedToPreferredErrorState > worstState) {
			worstState = notLoadedToPreferredErrorState;
		}
		if (samePathErrorState > worstState) {
			worstState = samePathErrorState;
		}					
		return worstState;		
	}

	/** 
	 * return collected error descriptions for
	 * 	ERROR_TYPE_PATH_CONFLICT
	 *  ERROR_TYPE_PATH_VARIABLE_UNDEFINED
	 *  ERROR_TYPE_NOT_LOADED_TO_PREFERRED_PATH
	 *  ERROR_TYPE_SAME_PATH_CONFLICT
	*/
	public String getPreferredPathLocationErrorDescriptions() {
		
		String varUndefinedError = getErrorDescription(ERROR_TYPE_PATH_VARIABLE_UNDEFINED);
		String pathConflictError = getErrorDescription(ERROR_TYPE_PATH_CONFLICT);
		String notLoadedToPreferredError = getErrorDescription(ERROR_TYPE_NOT_LOADED_TO_PREFERRED_PATH);
		String samePathError = getErrorDescription(ERROR_TYPE_SAME_PATH_CONFLICT);
		
		StringBuffer sb = new StringBuffer();
		sb.append(varUndefinedError);
		if ((pathConflictError.length() > 0) && (sb.length() >0)) {
			sb.append(", "); //$NON-NLS-1$
		}
		sb.append(pathConflictError);
		if ((notLoadedToPreferredError.length() > 0) && (sb.length() >0)) {
			sb.append(", "); //$NON-NLS-1$
		}
		sb.append(notLoadedToPreferredError);
		if ((samePathError.length() > 0) && (sb.length() >0)) {
			sb.append(", "); //$NON-NLS-1$
		}
		sb.append(samePathError);
				
		return sb.toString();	
	}
		
	/**
	 * Returns the unresolvedPreferredLocation.
	 * @return String
	 */
	public String getUnresolvedPreferredLocation() {
		return unresolvedPreferredLocation;
	}

	/**
	 * Sets the unresolvedPreferredLocation.
	 * @param unresolvedPreferredLocation The unresolvedPreferredLocation to set
	 */
	public void setUnresolvedPreferredLocation(String unresolvedPreferredLocation) {
		this.unresolvedPreferredLocation = unresolvedPreferredLocation;
	}

	public String getResolvedPreferredLocation() {
		return getResolvedPreferredLocation(null);
	}
		
	public String getResolvedPreferredLocation(Map<String, String> cachedPathVariableMap) {
		return PreferredLocationsUtil.resolve(getUnresolvedPreferredLocation(), cachedPathVariableMap, this);
	}

}
