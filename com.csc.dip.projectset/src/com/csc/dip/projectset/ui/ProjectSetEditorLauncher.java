package com.csc.dip.projectset.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorLauncher;

import com.csc.dip.projectset.ProjectSetPlugin;

/**
 * @author dbu
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ProjectSetEditorLauncher implements IEditorLauncher {

	/**
	 * Constructor for ProjectSetEditorLauncher.
	 */
	public ProjectSetEditorLauncher() {
		super();
	}

	/**
	 * @see org.eclipse.ui.IEditorLauncher#open(IFile)
	 */
	public void open(IFile file) {
		// currently use first workbench window to get the shell
		// change this, when you find a better solution		
		Shell shell = ProjectSetPlugin.getDefault().getWorkbench().getWorkbenchWindows()[0].getShell();
		
		ProjectSetEditor editor = new ProjectSetEditor(shell, file);
		editor.open();
	}

}
