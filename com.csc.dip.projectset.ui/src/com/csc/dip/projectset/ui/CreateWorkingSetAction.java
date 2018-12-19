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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

import com.csc.dip.projectset.ProjectSet;
import com.csc.dip.projectset.ProjectSetEntry;
import com.csc.dip.projectset.ProjectSetPlugin;

/**
 * @author dbaumann
 */
public class CreateWorkingSetAction extends AbstractProjectSetAction {

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        CreateWorkingSetConfigDialog dialog = new CreateWorkingSetConfigDialog(shell);
        dialog.setPsfFiles(getPSFFiles(selection));
        int buttonPressed = dialog.open();
        if (buttonPressed == Window.OK) {
            String workingSetName = dialog.getWorkingSetName();
            boolean recursive = dialog.isRecursive();
            // create working set
	        IWorkingSetManager workingSetManager = ProjectSetUIPlugin.getDefault().getWorkbench().getWorkingSetManager();
	        IWorkingSet workingSet = workingSetManager.createWorkingSet(workingSetName, getProjects(recursive));
	        workingSetManager.addWorkingSet(workingSet);
	        workingSetManager.addRecentWorkingSet(workingSet);
        }
    }
    
    protected IProject[] getProjects(boolean recursive) {
        List<IProject> collectedProjects = new ArrayList<IProject>();
		Iterator<IFile> fileIterator = getPSFFiles(selection).iterator();
		while (fileIterator.hasNext()) {
			IFile psfFile = fileIterator.next();
			ProjectSet projectSet = new ProjectSet(psfFile, true);
			ProjectSetEntry[] psEntries;
			if (recursive) {
			    psEntries = projectSet.getProjectSetEntriesRecursively();
			} else {
				psEntries = projectSet.getProjectSetEntries();
			}
			for (int i = 0; i < psEntries.length; i++) {
                ProjectSetEntry psEntry = psEntries[i];
                String projectName = psEntry.getProjectName();
                collectedProjects.add(
                        ProjectSetPlugin.getWorkspace().getRoot().getProject(projectName));
            }
		}	
		return collectedProjects.toArray(new IProject[collectedProjects.size()]);
    }
}
