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
 * Dialog area for selecting projects from the workspace
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.WorkbenchLabelProvider;


public class ProjectSelectionArea {

	private CheckboxTableViewer tableViewer;
	private IProject[] projects;
	boolean selectReferenced = false;
	
	/**
	 * Constructor for ProjectSelectionArea.
	 */
	public ProjectSelectionArea(Composite parent, IProject[] projects) {
		super();
		
		this.projects = projects;
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createListViewer(composite);
		createButtons(composite);		
	}

	/**
	 * Creates the list viewer showing the selectable projects
	 */
	protected void createListViewer(Composite parent) {
		tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		data.widthHint = 300;
		tableViewer.getControl().setLayoutData(data);
		
		tableViewer.setLabelProvider(new DecoratingLabelProvider(
			new WorkbenchLabelProvider(),
			ProjectSetUIPlugin.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator()));
		
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				checkChanged((IProject)event.getElement(), event.getChecked());
			}
		});
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(projects);
	}

	/**
	 * recursively add all referencing projects to the set
	 */
	protected void addAllReferencedProjects(IProject project, Set<IProject> projectSet) {
		IProject[] referencedProjects = null;
		try {
			if (project.isAccessible()) {
				referencedProjects = project.getReferencedProjects();
			}
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}
		if (referencedProjects != null) {
			for (int i = 0; i < referencedProjects.length; i++) {
				IProject refProject = referencedProjects[i];
				if (!projectSet.contains(refProject)) {
					projectSet.add(refProject);
					addAllReferencedProjects(refProject, projectSet);
				}
			}
		}
	}

	/**
	 * This method is called when the user checks an entry.
	 * It also checks recursively all referenced if specified
	 */		
	protected void checkChanged(IProject project, boolean checked) {
		if (selectReferenced && checked) {
			Set<IProject> allReferencingProjects = new HashSet<IProject>();
			addAllReferencedProjects(project, allReferencingProjects);
		
			Iterator<IProject> projectIterator = allReferencingProjects.iterator();
			while (projectIterator.hasNext()) {
				IProject referencedProject = projectIterator.next();
				tableViewer.setChecked(referencedProject, true);
			}
		}
	}
	
	
	/**
	 * Creates all buttons.
	 * 
	 * @param parent 
	 */
	protected void createButtons(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createSelectAllButton(composite);
		createSelectNoneButton(composite);
		createSelectAllFromWorkingSetButton(composite);
		createDeselectAllFromWorkingSetButton(composite);
		createSelectDependentCheckBox(composite);
	}

	/**
	 * Creates button for selection all entries
	 * 
	 * @param parent parent composite
	 */
	protected void createSelectAllButton(Composite parent) {
		Button selectAllButton = new Button(parent, SWT.PUSH);
		selectAllButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectAllButton.setText(Messages.getString("ProjectSelectionArea.Select_all")); //$NON-NLS-1$
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				tableViewer.setAllChecked(true);
			}
		});	
	}

	/**
	 * Creates button for deselecting all entries
	 * 
	 * @param parent parent composite
	 */
	protected void createSelectNoneButton(Composite parent) {
		Button selectNoneButton = new Button(parent, SWT.PUSH);
		selectNoneButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectNoneButton.setText(Messages.getString("ProjectSelectionArea.Select_none")); //$NON-NLS-1$
		selectNoneButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				tableViewer.setAllChecked(false);
			}
		});
	}

	/**
	 * Creates button for selection of working sets which entries are selected
	 * 
	 * @param parent parent composite
	 */	
	protected void createSelectAllFromWorkingSetButton (Composite parent) {		
		Button selectAllFromWorkingSet = new Button(parent, SWT.PUSH);
		selectAllFromWorkingSet.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectAllFromWorkingSet.setText(Messages.getString("ProjectSelectionArea.Select_all_from_working_set_")); //$NON-NLS-1$
		selectAllFromWorkingSet.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				IProject[] projects = selectProjectsFromWorkingSet();
				for (int i = 0; i < projects.length; i++) {
					IProject project = projects[i];
					tableViewer.setChecked(project, true);
					checkChanged(project, true);					
				}				
			}
		});		
	}

	/**
	 * Creates button for selection of working sets which entries are deselected
	 * 
	 * @param parent parent composite
	 */	
	protected void createDeselectAllFromWorkingSetButton (Composite parent) {
		Button deselectAllFromWorkingSet = new Button(parent, SWT.PUSH);
		deselectAllFromWorkingSet.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deselectAllFromWorkingSet.setText(Messages.getString("ProjectSelectionArea.Deselect_all_from_working_set_")); //$NON-NLS-1$
		deselectAllFromWorkingSet.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				IProject[] projects = selectProjectsFromWorkingSet();
				for (int i = 0; i < projects.length; i++) {
					IProject project = projects[i];
					tableViewer.setChecked(project, false);					
				}					
			}
		});
	}

	/**
	 * Creates checkbox button for specifying if all referenced projects should 
	 * automatically be checked
	 * 
	 * @param parent parent composite
	 */					
	protected void createSelectDependentCheckBox (Composite parent) {

		Button selectDependentCheckBox = new Button(parent, SWT.CHECK);
		selectDependentCheckBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectDependentCheckBox.setText(Messages.getString("ProjectSelectionArea.Select_referenced")); //$NON-NLS-1$
		selectDependentCheckBox.setSelection(selectReferenced);
		selectDependentCheckBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (selectReferenced) {
					selectReferenced = false;
				} else {
					selectReferenced = true;
				}
			}
		});							
	}
	
	/**
	 * Opens a dialog for selection working sets.
	 * Returns all projects in the selected working sets.
	 */
	public IProject[] selectProjectsFromWorkingSet() {
		WorkingSetSelectionDialog wsSelectionDialog = new WorkingSetSelectionDialog(tableViewer.getControl().getShell());
		wsSelectionDialog.open();
		
		List<IProject> projects = new ArrayList<IProject>();
		IWorkingSet[] workingSets = (IWorkingSet[])wsSelectionDialog.getResult();
		if (workingSets != null) {
			for (int i = 0; i < workingSets.length; i++) {
				IWorkingSet workingSet = workingSets[i];
				IAdaptable[] wsElements = workingSet.getElements();
				for (int j = 0; j < wsElements.length; j++) {
					IAdaptable adaptable = wsElements[j];
					IProject project = (IProject) adaptable.getAdapter(IProject.class);
					if (project != null) {
						projects.add(project);
					}
				}
			}
		}
		return projects.toArray(new IProject[projects.size()]);
	}
	
	/**
	 * Returns all checked projects
	 */
	public IProject[] getCheckedProjects() {
		Object[] checkedObjects = tableViewer.getCheckedElements();
		IProject[] checkedProjects = new IProject[checkedObjects.length];
		for (int i = 0; i < checkedProjects.length; i++) {
			checkedProjects[i] = (IProject)checkedObjects[i];
		}
		return checkedProjects;
	}

}
