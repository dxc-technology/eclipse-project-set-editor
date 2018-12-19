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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.ProjectSetSerializationContext;
import org.eclipse.team.internal.ui.UIProjectSetSerializationContext;

import com.csc.dip.projectset.ProjectSetContext;

public class ProjectSetUIContext extends ProjectSetContext {
	
	/**
	 * The shell to be used during the operation
	 */
	private final Shell shell;
	
	/**
	 * Construct a new instance
	 * 
	 * @param shell The shell to be used during the operation
	 */
	public ProjectSetUIContext(Shell shell) {
		this.shell = shell;
	}

	/**
	 * Create a new ProjectSetSerializationContext
	 * overwritten to return a UIProjectSetSerializationContext
	 * 
	 * @return ProjectSetSerializationContext
	 */
	protected ProjectSetSerializationContext createProjectSetSerializationContext(String projectSetFilename) {
		return new UIProjectSetSerializationContext(shell, projectSetFilename);
	}
	
	/**
	 * Display the status to the user.
	 * Log any errors.
	 * 
	 * @param status the status (not <code>null</code>)
	 * @see com.csc.dip.projectset.ProjectSetContext#reportStatus(IStatus)
	 */
	public void reportStatus(final IStatus status) {
		final String title;
		boolean log;
		switch (status.getSeverity()) {
			case IStatus.INFO:
				title = Messages.getString("ProjectSetUIContext.Information"); //$NON-NLS-1$
				log = false;
				break;
			case IStatus.WARNING:
				title = Messages.getString("ProjectSetUIContext.Warning"); //$NON-NLS-1$
				log = false;
				break;
			case IStatus.ERROR:
			default :
				title = Messages.getString("ProjectSetUIContext.Error"); //$NON-NLS-1$
				log = true;
				break;
		}
		
		// This method may be called from a non-GUI thread
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(shell, title, status.getMessage());
			}
		});
		if (log) {
			super.reportStatus(status);
		}
	}

}
