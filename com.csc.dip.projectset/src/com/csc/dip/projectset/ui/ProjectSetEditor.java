package com.csc.dip.projectset.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.IProjectSetSerializer;
import org.eclipse.team.core.Team;

/**
 * @author dbu
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ProjectSetEditor extends Dialog {

	IFile projectSetFile;
	
	/**
	 * Constructor for ProjectSetEditor.
	 * @param parentShell
	 */
	public ProjectSetEditor(Shell parentShell) {
		super(parentShell);
	}
	
	public ProjectSetEditor(Shell parentShell, IFile projectSetFile) {
		super(parentShell);
		this.projectSetFile = projectSetFile;
	}	

}
