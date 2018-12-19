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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.csc.dip.projectset.PreferredLocationsUtil;
import com.csc.dip.projectset.ProjectSetEntry;

/**
 * @author Dirk Baumann
 * 
 * A dialog for selection of one of the defined path variables
 *
 */
public class PathVariableSelectionDialog extends Dialog {

	private String selectedPathVariable = null;
	private ProjectSetEntry psEntry;
	
	/**
	 * Constructor for PathVariableSelectionDialog.
	 * @param parentShell
	 */
	public PathVariableSelectionDialog(Shell parentShell, ProjectSetEntry psEntry) {
		super(parentShell);	
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		this.psEntry = psEntry;
	}

	protected void configureShell(Shell shell) {
		shell.setText(Messages.getString("PathVariableSelectionDialog.Select_Path_Variable")); //$NON-NLS-1$
		super.configureShell(shell);
	}	
	
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		PathVariableTable pvTable = new PathVariableTable(composite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);

		GridData tableGD = new GridData(GridData.FILL_BOTH);
		tableGD.widthHint = 300;
		tableGD.heightHint =150;
		pvTable.getTable().setLayoutData(tableGD);
		
		TableViewer tableViewer = pvTable.getTableViewer();
				
		tableViewer.setInput(getPathEntries());

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) (event.getSelection());
				if (selection.isEmpty()) {
					selectedPathVariable = null;
				} else {
					selectedPathVariable = ((PathVariableEntry)selection.getFirstElement()).getName();
				}
			}
		});
		
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		
		// sort by key
		tableViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((PathVariableEntry) e1).getName().compareTo(
						((PathVariableEntry) e2).getName());
			}
		});

		return composite;
	}
	
	protected List<PathVariableEntry> getPathEntries() {
		List<PathVariableEntry> pathEntries = new ArrayList<PathVariableEntry>();		
		Iterator<Map.Entry<String, String>> mapEntryIterator = PreferredLocationsUtil.getCompletePathVariableMap(psEntry).entrySet().iterator();
		while (mapEntryIterator.hasNext()) {
			Map.Entry<String, String> mapEntry = mapEntryIterator.next();
			pathEntries.add(new PathVariableEntry(mapEntry.getKey(), mapEntry.getValue()));
		}
		return pathEntries;
	}
	

	public String getSelectedPathVariable() {
		return selectedPathVariable;
	}

}
