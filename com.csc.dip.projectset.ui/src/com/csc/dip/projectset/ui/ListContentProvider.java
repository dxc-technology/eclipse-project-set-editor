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

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Dirk Baumann
 *
 * A simple content provider for a List 
 */

public class ListContentProvider implements IStructuredContentProvider {
		protected List contents;

		public ListContentProvider() {
		}		
		public Object[] getElements(Object input) {
			if (contents != null && contents == input) {
				return contents.toArray();
			}
			return new Object[0];
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof List) {
				contents = (List) newInput;
			}
			else {
				contents = null;
			}
		}
		public void dispose() {
		}
	}
