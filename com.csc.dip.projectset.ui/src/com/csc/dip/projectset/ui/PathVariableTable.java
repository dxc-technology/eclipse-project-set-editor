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

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @author Dirk Baumann
 *
 * A table for path variables entries
 */
public class PathVariableTable {

	private TableViewer tableViewer;

	private class PathVariableTableLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			PathVariableEntry entry = (PathVariableEntry)element;
			
			if (columnIndex == 0) {
				return entry.getName();
			} else {
				return entry.getPath();
			}
		}

		public boolean isLabelProperty(Object element, String property){
			return true;
		}
		public void addListener(ILabelProviderListener listener) {
		}	
		public void removeListener(ILabelProviderListener listener){
		}
		public void dispose() {
		}
	}
	
	public PathVariableTable(Composite parent) {
		this(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
	}

	public PathVariableTable(Composite parent, int style) {
		tableViewer = createTableViewer(parent, style);	
	}	

	protected TableViewer createTableViewer(Composite parent, int style) {
		TableViewer tabViewer = new TableViewer(parent, style);

		Table table = tabViewer.getTable();
		table.setHeaderVisible(true);

		TableColumn varNameCol = new TableColumn(table, SWT.NONE);
		varNameCol.setText(Messages.getString("PathVariableTable.Name")); //$NON-NLS-1$
		TableColumn pathCol = new TableColumn(table, SWT.NONE);
		pathCol.setText(Messages.getString("PathVariableTable.Path"));  //$NON-NLS-1$

		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(25, 50, true));
		layout.addColumnData(new ColumnWeightData(75, 150, true));
		table.setLayout(layout);		
		
		tabViewer.setContentProvider(new ListContentProvider());
		tabViewer.setLabelProvider(new PathVariableTableLabelProvider());
	
		return tabViewer;
	}
	
	public TableViewer getTableViewer() {
		return tableViewer;
	}
	
	public Table getTable() {
		return tableViewer.getTable();
	}	

}
