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

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ui.TeamOperation;
import org.eclipse.ui.IDecoratorManager;

import com.csc.dip.projectset.ProjectSet;

public class ProjectSetUI {

	/**
	 * No instances
	 */
	private ProjectSetUI() {
	}

	/**
	 * Adds/replaces projects specified by the entries of this project set.
	 */		
	public static void addAllToWorkspace(ProjectSet projSet, Shell shell, boolean recursive) {
		addToWorkspace(projSet, shell, null, recursive);
	}

	/**
	 * Adds/replaces projects listed in projectNames and 
	 * specified by the entries of this project set.
	 * 
	 * If projectNames is null, add/replace all entries.
	 * 
	 * The list projectNames is only used for direct entries.
	 * When loading recursively all entries of sub project sets
	 * are added/replaced independently from the projectNames
	 */		
	public static void addToWorkspace(final ProjectSet projSet, final Shell shell, final List<String> projectNames, final boolean recursive) {
	
		final ProjectSet projectSetCopy = projSet.copy();
		
		TeamOperation operation = new TeamOperation((IRunnableContext) null) {
			
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				projectSetCopy.addToWorkspace(new ProjectSetUIContext(shell), monitor, projectNames, recursive);
			}
			
			@Override
			protected boolean canRunAsJob() {
				return true;
			}
			
			@Override
			protected String getJobName() {
				return Messages.getString("ProjectSetUI.CheckingOutProjects"); //$NON-NLS-1$
			}
			
		};
		
		try {
			operation.run();
		} catch (InvocationTargetException e) {
			String errorMessage = MessageFormat.format(Messages.getString("ProjectSetUI.Error_while_loading_project_set_{0}"), new Object[] {projSet.getProjectSetFile().getLocation().toOSString()}); //$NON-NLS-1$
			if (e.getTargetException().getMessage() != null) {
				errorMessage = errorMessage + MessageFormat.format(Messages.getString("ProjectSetUI.,_reason__{0}"), new Object[] {e.getTargetException().getMessage()}); //$NON-NLS-1$
			}
			MessageDialog.openError(shell, Messages.getString("ProjectSetUI.Error"), errorMessage);//$NON-NLS-1$
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the labels of the project set project
	 */			
	public static void updateLabels(IProject psfProject) {
		IDecoratorManager decoratorManager = ProjectSetUIPlugin.getDefault().getWorkbench().getDecoratorManager();
		ProjectSetLabelDecorator psDecorator =(ProjectSetLabelDecorator) decoratorManager.getBaseLabelProvider(IProjectSetUIConstants.PROJECT_SET_LABEL_DECORATOR_ID);
		if (psDecorator != null) {
			final List<IResource> psfResources = new ArrayList<IResource>();
			// currently update all psf resources
			try {
				psfProject.accept(new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						if ((resource.exists()) && (resource.isAccessible())) {
							psfResources.add(resource);
						}
						return true;
					}
				});
			} catch (CoreException e) {
			}
			psDecorator.updateLabelsForResources(
				psfResources.toArray(new IResource[psfResources.size()]));
		}
	}

}
