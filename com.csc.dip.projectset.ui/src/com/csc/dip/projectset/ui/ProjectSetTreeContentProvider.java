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
 * Content provider for tree viewer displaying a project set
 */

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.csc.dip.projectset.ProjectSet;
import com.csc.dip.projectset.ProjectSetEntry;


public class ProjectSetTreeContentProvider
	implements ITreeContentProvider {

	/**
	 * Constructor for ProjectSetTableContentProvider.
	 */
	public ProjectSetTreeContentProvider() {
		super();
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (sortEntries()) {
			return ((ProjectSet)inputElement).getSortedProjectSetEntries();
		} else {
			return ((ProjectSet)inputElement).getProjectSetEntries();
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * Returns the entries of the sub project set if
	 * the parent element has on, otherwise returns
	 * an empty array
	 */
	public Object[] getChildren(Object parentElement) {
		ProjectSetEntry entry = (ProjectSetEntry)parentElement;
		ProjectSet subProjectSet = entry.getSubProjectSet();
		if (subProjectSet != null) {
			if (sortEntries()) {
				return subProjectSet.getSortedProjectSetEntries();
			} else {
				return subProjectSet.getProjectSetEntries();
			}
		} else {
			return new Object[0];
		}
	}

	/**
	 * Returns whether the element has children
	 */
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}
	
	/**
	 * Returns the project set entry whichs project set
	 * containing the entry of null if no such parent
	 * set exists
	 */
	public Object getParent(Object element) {
		ProjectSetEntry entry = (ProjectSetEntry)element;
		ProjectSet ps = entry.getProjectSet();
		if (ps != null) {
			return ps.getParentProjectSetEntry();
		} else {
			return null;
		}
	}

	protected boolean sortEntries() {
		return Platform.getPreferencesService().getBoolean(ProjectSetUIPlugin.PLUGIN_ID, IProjectSetUIConstants.EDITOR_SORT_ENTRIES, true, null);
	}
}
