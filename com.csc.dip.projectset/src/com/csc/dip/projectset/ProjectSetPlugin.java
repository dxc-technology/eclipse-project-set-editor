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
package com.csc.dip.projectset;

/**
 * @author Dirk Baumann
 */

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class ProjectSetPlugin extends Plugin implements IResourceChangeListener {

	public static final String PLUGIN_ID = "com.csc.dip.projectset.ProjectSetPlugin"; //$NON-NLS-1$
	
	//The shared instance.
	private static ProjectSetPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public ProjectSetPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("com.csc.dip.projectset.ProjectSetPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static ProjectSetPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= ProjectSetPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
	
	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		/*
		final List dummyList = new ArrayList();
		System.out.println("RESOURCE CHANGED -> build all psf ");
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) throws CoreException {
						boolean updateRequired = false;
						if (delta.getResource().getType() == IResource.PROJECT) {
							updateRequired = true;
						} else 
						if (delta.getResource().getType() == IResource.FILE) {
							IFile file = (IFile)delta.getResource().getAdapter(IFile.class);
							// check if file is part of ps project
							// ...
							String extension = file.getFileExtension().toLowerCase();
							if ((extension.equals("psf")) || (extension.equals("psproject"))) {
								updateRequired = true;
							}
						}
						
						if (updateRequired) {
							dummyList.add("42");
							return false;
						} else {
							return true;
						}
					}					
			});		
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		
		if (!dummyList.isEmpty()) {
		*/
		if (true) {
			buildAllProjectSetProjects();
		}
	}

	protected void buildAllProjectSetProjects() {
		IProject[] projects = ProjectSetPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {			
			IProject project = projects[i];
			if ((project.exists()) && (project.isAccessible())) {
				ProjectSetNature psNature = null;
				try {
					psNature = (ProjectSetNature)project.getNature(ProjectSetNature.PS_PROJECT_NATURE_ID);				
				} catch (CoreException e) {
				}
				if (psNature != null) {
					new ProjectSetBuilder().updateErrorMarkers(project);
				}
			}			
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_BUILD);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		getWorkspace().removeResourceChangeListener(this);
		super.stop(context);
	}
}
