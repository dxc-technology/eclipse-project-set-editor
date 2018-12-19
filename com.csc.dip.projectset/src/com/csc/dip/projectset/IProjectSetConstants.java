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
 * This class contains some constants used by various classes in the plugin
 * 
 */

public interface IProjectSetConstants {

	// file extension
	public static final String PROJECT_SET_FILE_EXTENSION = "psf"; //$NON-NLS-1$

	// name for preferences
	public static final String PSF_PATH_VARIABLES_PREFERENCE = "psfPathVariables"; //$NON-NLS-1$
	public static final String REFRESH_BEFORE_LOADING_PREFERENCE = "refreshBeforeLoadingVariables"; //$NON-NLS-1$

	// marker ids
	public static final String PSF_MARKER_ID = "com.csc.dip.projectset.PsfMarker"; //$NON-NLS-1$
	public static final String PSF_TAG_CONFLICT_MARKER_ID = "com.csc.dip.projectset.PsfTagConflictMarker"; //$NON-NLS-1$
	public static final String PSF_PATH_CONFLICT_MARKER_ID = "com.csc.dip.projectset.PsfPathConflictMarker"; //$NON-NLS-1$
	public static final String PSF_NOT_LOADED_MARKER_ID = "com.csc.dip.projectset.PsfNotLoadedMarker"; //$NON-NLS-1$
	public static final String PSF_NOT_LOADED_TO_PREFERRED_MARKER_ID = "com.csc.dip.projectset.PsfNotLoadedToPreferredMarker"; //$NON-NLS-1$
	public static final String PSF_ERROR_MARKER_ID = "com.csc.dip.projectset.PsfErrorMarker"; //$NON-NLS-1$
	
	// id of the project set builder
	public static final String PROJECT_SET_BUILDER_ID = "com.csc.dip.projectset.ProjectSetBuilder"; //$NON-NLS-1$
}
