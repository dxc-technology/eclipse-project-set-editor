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
 * 
 * 
 * This is a utility class with methods used by various classes in the plugin
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.core.RepositoryProvider;

public class ProjectSetUtil {

	// for caching
	private static Map<String, IProjectReferenceAnalyser> projectReferenceAnalyserMap;
	private static List<String> providersNotSupportingPreferredLocalDir;
	
	/**
	 * Constructor for ProjectSetUtil.
	 */
	public ProjectSetUtil() {
		super();
	}

	/**
	 * Returns all shared projects in the workspace excluding the projects
	 * with the names in excludeProjectNames
	 */
	public static IProject[] getSharedProjects(List<String> excludeProjectNames) {
		IWorkspaceRoot root = ProjectSetPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		List<IProject> sharedProjects = new ArrayList<IProject>();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if (
			     ( 
			       (excludeProjectNames == null)
			          ||
				   (excludeProjectNames.indexOf(project.getName()) == -1)
			     ) 
				&&
		         (RepositoryProvider.getProvider(project) != null)
		       ) {
				sharedProjects.add(project);
			}
		}
		return sharedProjects.toArray(new IProject[sharedProjects.size()]);
	} 

	/**
	 * Returns all shared projects in the workspace 
	 */	
	public static IProject[] getSharedProjects() {
		return getSharedProjects(null);
	} 
	
	/**
	 * Returns whether a project with the name is loaded and shared
	 */
	public static boolean projectIsLoadedAndShared(String projectName) {
		IProject project = ProjectSetPlugin.getWorkspace().getRoot().getProject(projectName);
		if ((project == null) || (!project.isAccessible())) {
			return false;
		} else {		
			RepositoryProvider providerOfLoadedProject = RepositoryProvider.getProvider(project);
			if (providerOfLoadedProject == null) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	/**
	 * Returns the project reference analyzer for the provider 
	 */
	public static IProjectReferenceAnalyser getProjectReferenceAnalyser(String providerName) {
		IProjectReferenceAnalyser analyser = getProjectReferenceAnalyserMap().get(providerName);
		if (analyser == null) {
			analyser = new UnknownProjectReferenceAnalyser(providerName);
			getProjectReferenceAnalyserMap().put(providerName, analyser);
		}
		return analyser;
	}

	public static boolean isProviderSupported(String providerName) {
		IProjectReferenceAnalyser analyser = getProjectReferenceAnalyser(providerName);
		return !(analyser instanceof UnknownProjectReferenceAnalyser);
	}
	
	public static boolean isProviderSupportsPreferredLocalDirectory(String providerName){
		getProjectReferenceAnalyserMap();
		return !providersNotSupportingPreferredLocalDir.contains(providerName);
	}
	
	/**
	 * Returns a map where the key is the provider name and
	 * the value is the project reference analyzer for the provider
	 */	
	protected static Map<String, IProjectReferenceAnalyser> getProjectReferenceAnalyserMap() {
		if (projectReferenceAnalyserMap == null) {
			List<String> installedProviders = Arrays.asList(RepositoryProvider.getAllProviderTypeIds());
			projectReferenceAnalyserMap = new HashMap<String, IProjectReferenceAnalyser>();
			providersNotSupportingPreferredLocalDir = new ArrayList<String>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint point = registry.getExtensionPoint("com.csc.dip.projectset.projectReferenceAnalyser"); //$NON-NLS-1$
			if (point != null) {
				IExtension[] extensions = point.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					String provider = null;
					IExtension extension = extensions[i];
					IConfigurationElement[] configElements = extension.getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						IConfigurationElement configElement = configElements[j];
						if (configElement.getName().equals("projectReferenceAnalyser")) { //$NON-NLS-1$
							provider = configElement.getAttribute("provider"); //$NON-NLS-1$
							if (installedProviders.contains(provider)) {
								try {
									IProjectReferenceAnalyser refAnalyser = (IProjectReferenceAnalyser) (configElement.createExecutableExtension("class")); //$NON-NLS-1$
									projectReferenceAnalyserMap.put(provider, refAnalyser);
									String preferredLocalDirNotSupported = configElement.getAttribute("preferredLocalDirNotSupported");//$NON-NLS-1$
									if (preferredLocalDirNotSupported != null && preferredLocalDirNotSupported.equalsIgnoreCase("true")) {//$NON-NLS-1$
										providersNotSupportingPreferredLocalDir.add(provider);
									}
								} catch (CoreException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		return projectReferenceAnalyserMap;
	}	

	public static boolean pathEquals(String path, String otherPath) {
		if (path == null) {
			return (otherPath == null);
		} else {
			if (otherPath == null) {
				return false;
			} else {
				return new File(path).equals(new File(otherPath));
			}
		}
	}
	
}
