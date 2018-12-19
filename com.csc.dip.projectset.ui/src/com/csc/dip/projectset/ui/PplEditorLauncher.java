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

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import com.csc.dip.projectset.PreferredLocationsUtil;
import com.csc.dip.projectset.ProjectSetPlugin;

/**
 * @author Dirk Baumann
 *
 * Opens the editor for the corresponding project set file 
 * or open error dialog if no such psf found
 */
public class PplEditorLauncher implements IEditorLauncher {

	/**
	 * @see org.eclipse.ui.IEditorLauncher#open(IFile)
	 */
	public void open(IPath pplFilePath) {
		String error = null;
		IFile[] foundFiles = ProjectSetPlugin.getWorkspace().getRoot().findFilesForLocation(pplFilePath);
		if ((foundFiles == null) ||(foundFiles.length == 0)) {
			// should never happen
			error = "error"; //$NON-NLS-1$
		} else {
			IFile psfFile = PreferredLocationsUtil.getCorrespondingPsfFile(foundFiles[0]);
			if (!psfFile.isAccessible()) {
				error = Messages.getString("PplEditorLauncher.Corresponding_PSF_file_not_found"); //$NON-NLS-1$
			} else {
				IWorkbenchPage page =
					ProjectSetUIPlugin
						.getDefault()
						.getWorkbench()
						.getActiveWorkbenchWindow()
						.getActivePage();
		
				try {
					page.openEditor(new FileEditorInput(psfFile), ProjectSetEditor.PSF_EDITOR_ID);
				} catch (PartInitException e) {
					error = e.getMessage();
				}
			}
		}
		if (error != null) {
			MessageDialog.openError(
				null,
				Messages.getString("PplEditorLauncher.Error"), //$NON-NLS-1$
				MessageFormat.format(Messages.getString("PplEditorLauncher.Error_while_opening_editor_for_file_{0}__n{1}"), new Object[] {pplFilePath.toOSString(),error}) //$NON-NLS-1$
			);
		}
	}

}
