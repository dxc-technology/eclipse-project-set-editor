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
 * This class contains some constants used by various classes in the plugin
 * 
 */

public interface IProjectSetUIConstants {

	// name for preferences
	public static final String EDITOR_SORT_ENTRIES = "editorSortEntries"; //$NON-NLS-1$	
	public static final String EDITOR_LOAD_RECURSIVE_PREFERENCE = "editorLoadRecursive"; //$NON-NLS-1$
	public static final String EDITOR_EXPAND_ENTRY_DETAILS_PREFERENCE = "editorExpandEntryDetails"; //$NON-NLS-1$
	public static final String EDITOR_EXPAND_LOADED_DETAILS_PREFERENCE = "editorExpandLoadedDetails"; //$NON-NLS-1$
	
	// id of the project set decorator
	public static final String PROJECT_SET_LABEL_DECORATOR_ID ="com.csc.dip.projectset.ui.ProjectSetLabelDecorator"; //$NON-NLS-1$
	
	// constants for properties of ProjectSetEntry
	public static final int PROJECT_SET_ENTRY_PROJECT_NAME = 1;
	public static final int PROJECT_SET_ENTRY_TAG = 2;
	public static final int PROJECT_SET_ENTRY_PREFERRED_PATH_PROBLEMS = 3;
	public static final int PROJECT_SET_ENTRY_TAG_CONFLICTS = 4;
	public static final int PROJECT_SET_ENTRY_STATE = 5;
	public static final int PROJECT_SET_ENTRY_PROVIDER = 6;
	public static final int PROJECT_SET_ENTRY_SERVER_LOCATION = 7;

}
