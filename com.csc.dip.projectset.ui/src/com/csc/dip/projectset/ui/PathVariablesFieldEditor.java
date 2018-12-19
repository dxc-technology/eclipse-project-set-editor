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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.csc.dip.projectset.PreferredLocationsUtil;

/**
 * @author Dirk Baumann
 *
 * A preference editor for project set path variables
 */
public class PathVariablesFieldEditor extends FieldEditor {

	private PathVariableTable pathVariableTable;
	
	private Composite buttonBox;
	private Button removeButton;
	private Button editButton;

	private List<PathVariableEntry> pathEntries = new ArrayList<PathVariableEntry>();
	
	protected PathVariablesFieldEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) pathVariableTable.getTable().getLayoutData()).horizontalSpan = numColumns - 1;
	}


	/**
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(Composite, int)
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);
	
		pathVariableTable = createPathVariableTable(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns - 1;
		gd.heightHint = 150;
		gd.grabExcessHorizontalSpace = true;
		pathVariableTable.getTable().setLayoutData(gd);
	
		buttonBox = createButtonBox(parent);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		buttonBox.setLayoutData(gd);
		
		pathVariableTable.getTableViewer().setSelection(StructuredSelection.EMPTY);
	}

	protected PathVariableTable createPathVariableTable(Composite parent) {
		
		PathVariableTable pvTable = new PathVariableTable(parent);
		TableViewer tabViewer = pvTable.getTableViewer();
		
		tabViewer.setInput(pathEntries);

		tabViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) (event.getSelection());
				newSelection(selection);
			}
		});
		
		tabViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editPressed();
			}
		});		
		return pvTable;
	}

	protected Composite createButtonBox(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		comp.setLayout(layout);

		createPushButton(
			comp,
			Messages.getString("PathVariablesFieldEditor.add"), //$NON-NLS-1$
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					addPressed();
				}
			});
		
		removeButton = createPushButton(
			comp,
			Messages.getString("PathVariablesFieldEditor.remove"), //$NON-NLS-1$
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					removedPressed();
				}
			});
			
		editButton = createPushButton(
			comp,
			Messages.getString("PathVariablesFieldEditor.edit"), //$NON-NLS-1$
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					editPressed();
				}
			});

//		comp.addDisposeListener(new DisposeListener() {
//			public void widgetDisposed(DisposeEvent event) {
//				addButton = null;
//				removeButton = null;
//				buttonBox = null;
//			}
//		});
		return comp;	
	}


	private Button createPushButton(Composite parent, String buttonLabel, SelectionListener selectListener) {
		Button button = new Button(parent, SWT.PUSH);		
		button.setText(buttonLabel);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		//data.heightHint = convertVerticalDLUsToPixels(button, IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(selectListener);		
		return button;
	}

	protected void addPressed() {
		PathVariableEntryDialog entryDialog = new PathVariableEntryDialog(getPage().getShell(), new PathVariableEntry("",""), getNames(null)); //$NON-NLS-1$ //$NON-NLS-2$
			
		int buttonPressed = entryDialog.open();
		
		if (buttonPressed == Window.OK) {		
			pathEntries.add(entryDialog.getEntry());
			getTableViewer().refresh();
		}
	}
	
	protected List<String> getNames(String excludeName) {
		List<String> collectedNames = new ArrayList<String>();
		Iterator<PathVariableEntry> entryIterator = pathEntries.iterator();
		while (entryIterator.hasNext()) {
			PathVariableEntry entry = entryIterator.next();
			String name = entry.getName();
			if ((excludeName == null) || (!name.equals(excludeName))) {
				collectedNames.add(name);
			}
		}
		return collectedNames;
	}
	
	protected void removedPressed() {
		pathEntries.removeAll(getSelectedEntries());
		getTableViewer().refresh();
	}
	
	protected void editPressed() {
		PathVariableEntry selectedEntry = getSelectedEntry();
		if (selectedEntry != null) {
			PathVariableEntryDialog entryDialog = new PathVariableEntryDialog(getPage().getShell(), getSelectedEntry(), getNames(selectedEntry.getName()));
				
			int buttonPressed = entryDialog.open();
			
			if (buttonPressed == Window.OK) {
				getTableViewer().refresh(selectedEntry);
			}
		}	
	}

	protected List getSelectedEntries() {
		return ((IStructuredSelection)getTableViewer().getSelection()).toList();
	}
	
	protected PathVariableEntry getSelectedEntry() {
		return (PathVariableEntry)((IStructuredSelection)getTableViewer().getSelection()).getFirstElement();
	}	
			
	protected void newSelection(IStructuredSelection selection) {

		removeButton.setEnabled(!selection.isEmpty());
		editButton.setEnabled(selection.size() == 1);
	}
		
	/**
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	protected void doLoad() {
		pathEntries.clear();
		Map<String, String> userDefinedPathVariables = new HashMap<String, String>();
		PreferredLocationsUtil.addUserDefinedPathVariables(userDefinedPathVariables);
		Iterator<Map.Entry<String, String>> mapEntryIterator = userDefinedPathVariables.entrySet().iterator();
		while (mapEntryIterator.hasNext()) {
			Map.Entry<String, String> mapEntry = mapEntryIterator.next();
			pathEntries.add(new PathVariableEntry(mapEntry.getKey(), mapEntry.getValue()));
		}
		getTableViewer().refresh();
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() {
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() {
		Map<String, String> map = new HashMap<String, String>();
		Iterator<PathVariableEntry> entryIterator = pathEntries.iterator();
		while (entryIterator.hasNext()) {
			PathVariableEntry entry = entryIterator.next();
			map.put(entry.getName(), entry.getPath());
		}
		PreferredLocationsUtil.storePathVariableMap(map);
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	public int getNumberOfControls() {
		return 2;
	}

	protected TableViewer getTableViewer() {
		return pathVariableTable.getTableViewer();
	}
}
