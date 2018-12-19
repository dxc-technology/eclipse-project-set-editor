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
 * Preference page for the project set plugin
 */

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ProjectSetEditorPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public ProjectSetEditorPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		// Set the preference store for the preference page.
		Preferences store = ProjectSetUIPlugin.getDefault().getPluginPreferences();
		setPreferenceStore(new PreferenceStoreAdapter(store));
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		BooleanFieldEditor editorSortEntries = new BooleanFieldEditor(
			IProjectSetUIConstants.EDITOR_SORT_ENTRIES,
			Messages.getString("ProjectSetPreferencePage.sort_entries"), //$NON-NLS-1$
			getFieldEditorParent());
		addField(editorSortEntries);
		
		BooleanFieldEditor editorLoadRecursive = new BooleanFieldEditor(
			IProjectSetUIConstants.EDITOR_LOAD_RECURSIVE_PREFERENCE,
			Messages.getString("ProjectSetPreferencePage.&load/replace_recursively_in_project_set_editor"), //$NON-NLS-1$
			getFieldEditorParent());
		addField(editorLoadRecursive);
		
		BooleanFieldEditor editorEntryDetailsExpanded = new BooleanFieldEditor(
			IProjectSetUIConstants.EDITOR_EXPAND_ENTRY_DETAILS_PREFERENCE,
			Messages.getString("ProjectSetPreferencePage.Details_of_selected_entry_expanded_in_project_set_editor"),  //$NON-NLS-1$
			getFieldEditorParent());
		addField(editorEntryDetailsExpanded);
		
		BooleanFieldEditor editorLoadedDetailsExpanded = new BooleanFieldEditor(
			IProjectSetUIConstants.EDITOR_EXPAND_LOADED_DETAILS_PREFERENCE,
			Messages.getString("ProjectSetPreferencePage.Details_of_loaded_expanded_in_project_set_editor"),  //$NON-NLS-1$
			getFieldEditorParent());
		addField(editorLoadedDetailsExpanded);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// nothing to do
	}

}
