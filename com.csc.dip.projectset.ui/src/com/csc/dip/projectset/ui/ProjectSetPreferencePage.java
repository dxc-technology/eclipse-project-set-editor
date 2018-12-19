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

import com.csc.dip.projectset.IProjectSetConstants;
import com.csc.dip.projectset.ProjectSetPlugin;

public class ProjectSetPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public ProjectSetPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		// Set the preference store for the preference page.
		Preferences store = ProjectSetPlugin.getDefault().getPluginPreferences();
		setPreferenceStore(new PreferenceStoreAdapter(store));
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		PathVariablesFieldEditor pathEditor = new PathVariablesFieldEditor(
			IProjectSetConstants.PSF_PATH_VARIABLES_PREFERENCE,
			Messages.getString("ProjectSetPreferencePage.Path_Variables"), //$NON-NLS-1$
			getFieldEditorParent());		
		addField(pathEditor);
		
		BooleanFieldEditor performRefreshEditor = new BooleanFieldEditor(
				IProjectSetConstants.REFRESH_BEFORE_LOADING_PREFERENCE,
				Messages.getString("ProjectSetPreferencePage.Refresh_project_before_replacing"), //$NON-NLS-1$
				getFieldEditorParent()
				);
		addField(performRefreshEditor);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	
	public void init(IWorkbench workbench) {
		// nothing to do
	}

    public boolean performOk() {
		boolean ok = super.performOk();
		ProjectSetPlugin.getDefault().savePluginPreferences();
		return ok;
	} 
}
