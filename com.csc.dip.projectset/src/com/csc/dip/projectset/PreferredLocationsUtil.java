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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Dirk Baumann
 * 
 * Additional contributions by Martin Zvieger
 *
 * This class holds settings for the destination folders to be used
 * for the first loading of the projects from the repository 
 */

public class PreferredLocationsUtil {

	public static final String PPL_FILE_EXTENSION = "ppl"; //$NON-NLS-1$

	public static final String PATH_VARIABLE_START_STRING="${"; //$NON-NLS-1$
	public static final String PATH_VARIABLE_END_STRING="}"; //$NON-NLS-1$

	public static final String WORKSPACE_PATH_VARIABLE = "workspace"; //$NON-NLS-1$
	public static final String USERHOME_PATH_VARIABLE = "home"; //$NON-NLS-1$
	public static final String INSTALLROOT_PATH_VARIABLE = "installroot"; //$NON-NLS-1$
	public static final String PROJECTNAME_PATH_VARIABLE = "projectname"; //$NON-NLS-1$
	public static final String PROJECTTAG_PATH_VARIABLE = "projecttag"; //$NON-NLS-1$
	
	// constants for reading and writing the preferred project folders
	private static final String XML_ELEM_PREFERRED_PROJECT_LOCATIONS = "preferredProjectLocations"; //$NON-NLS-1$
	private static final String XML_ATTR_VERSION = "version"; //$NON-NLS-1$
	private static final String PPL_VERSION = "1.0"; //$NON-NLS-1$

	private static final String XML_ELEM_MAPPING = "mapping"; //$NON-NLS-1$
	private static final String XML_ATTR_PROJECT_NAME = "projectName"; //$NON-NLS-1$		
	private static final String XML_ATTR_LOCATION = "location"; //$NON-NLS-1$		

	/**
	 * Loads the content to the ppl file
	 * 
	 * returns Map 
	 *    key = project name
	 *    value = (unresolved) preferred destination folder 
	 */
	public static Map<String, String> loadMap(IFile pplFile) {
		if (!(pplFile.exists() && pplFile.isAccessible())) {
			return null;
		}
		// key = project name, value = destination folder 
		Map<String, String> map = new HashMap<String, String>();

		Document document = null;

		try {
			document = XMLUtil.readDocument(pplFile.getLocation().toFile());
		} catch (ParserConfigurationException e) {
			return null;
		} catch (SAXException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		Element pplElement = document.getDocumentElement();
		if (!pplElement
			.getNodeName()
			.equals(XML_ELEM_PREFERRED_PROJECT_LOCATIONS)) {
			// error: root element must be ppl
			return null;
		}
		// version currently unused
		//String pplVersion = pplElement.getAttribute(XML_ATTR_VERSION);

		NodeList mappingNodes =
			pplElement.getElementsByTagName(XML_ELEM_MAPPING);
		for (int i = 0; i < mappingNodes.getLength(); i++) {
			Node mappingNode = mappingNodes.item(i);
			String projectName =	mappingNode	.getAttributes().getNamedItem(XML_ATTR_PROJECT_NAME).getNodeValue();
			String location = mappingNode.getAttributes().getNamedItem(XML_ATTR_LOCATION).getNodeValue();
			map.put(projectName, location);
		}

		return map;
	}

	/**
	 * Writes the content to the ppl file.
	 * If map is empty write to file only if file already exists
	 * 
	 * preferredFolderMap: 
	 *    key = project name
	 *    value = (unresolved) preferred destination folder 
	 */
	public static void write(Map<String, String> preferredFolderMap, IFile pplFile) {

		if ((!preferredFolderMap.isEmpty()) || pplFile.exists()) {
			Document document = null;
			try {
				document = XMLUtil.createNewDocument();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
	
			Element psfElement = document.createElement(XML_ELEM_PREFERRED_PROJECT_LOCATIONS);
			document.appendChild(psfElement);
			psfElement.setAttribute(XML_ATTR_VERSION, PPL_VERSION);
	
			Iterator<Map.Entry<String, String>> mapIterator = preferredFolderMap.entrySet().iterator();
			while (mapIterator.hasNext()) {
				Map.Entry<String, String> mapEntry = mapIterator.next();
				String projectName = mapEntry.getKey();
				String location = mapEntry.getValue();
	
				Element providerElement = document.createElement(XML_ELEM_MAPPING);
				psfElement.appendChild(providerElement);
				providerElement.setAttribute(XML_ATTR_PROJECT_NAME, projectName);
				providerElement.setAttribute(XML_ATTR_LOCATION, location);
			}
	
			// write XML document to file
			try {
				XMLUtil.writeDocument(document, pplFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				pplFile.refreshLocal(IResource.DEPTH_ZERO, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	public static Map<String, String> getMapFromProjectSet(ProjectSet projectSet) {
		ProjectSetEntry[] entries = projectSet.getProjectSetEntries();
		Map<String, String> resultMap = new HashMap<String, String>();
		for (int i = 0; i < entries.length; i++) {
			ProjectSetEntry projectSetEntry = entries[i];
			String unresolvedPreferredLocation = projectSetEntry.getUnresolvedPreferredLocation();
			if ((unresolvedPreferredLocation != null)	&& (unresolvedPreferredLocation.trim().length() > 0)) {
				resultMap.put(projectSetEntry.getProjectName(), unresolvedPreferredLocation);
			}
		}
		return resultMap;
	}

	public static void setMapToProjectSet(ProjectSet projectSet, Map<String, String> preferredFolderMap) {
				
		ProjectSetEntry[] entries = projectSet.getProjectSetEntries();
	
		for (int i = 0; i < entries.length; i++) {
			ProjectSetEntry projectSetEntry = entries[i];
			String preferredLocation = null;
			if (preferredFolderMap != null) {
				preferredLocation = preferredFolderMap.get(projectSetEntry.getProjectName());
			}
			projectSetEntry.setUnresolvedPreferredLocation(preferredLocation);
		}
	}

	public static void updatePreferredProjectLocations(ProjectSet projectSet) {
		Map<String, String> pplMap = loadMap(getPreferredProjectLocationsFile(projectSet));
		setMapToProjectSet(projectSet, pplMap);
	}

	public static void writePreferredProjectLocations(ProjectSet projectSet) {
		Map<String, String> pplMap = getMapFromProjectSet(projectSet);
		write(pplMap, getPreferredProjectLocationsFile(projectSet));
	}

	public static IFile getPreferredProjectLocationsFile(ProjectSet projectSet) {
		IFile psfFile = projectSet.getProjectSetFile();
		return getCorrespondingPplFile(psfFile);
	}

	public static IFile getCorrespondingPplFile(IFile psfFile) {
		IPath psfPath = psfFile.getProjectRelativePath();
		IPath pplPath = psfPath.removeFileExtension().addFileExtension(PPL_FILE_EXTENSION);
		return psfFile.getProject().getFile(pplPath);		
	}

	public static IFile getCorrespondingPsfFile(IFile pplFile) {
		IPath pplPath = pplFile.getProjectRelativePath();
		IPath psfPath = pplPath.removeFileExtension().addFileExtension(IProjectSetConstants.PROJECT_SET_FILE_EXTENSION);
		return pplFile.getProject().getFile(psfPath);		
	}
	
	public static String getDefaultLocation(ProjectSetEntry psEntry) {
		return ProjectSetPlugin.getWorkspace().getRoot().getLocation().append(psEntry.getProjectName()).toOSString();
	}
	
	/**
	 * return a map containing user defined path variables, global implicit variables
	 * and project set entry specific path variables
	 */
	public static Map<String, String> getCompletePathVariableMap(ProjectSetEntry psEntry) {
		Map<String, String> resultMap = getProjectSetEntryIndependentPathVariableMap();
		addProjectSetEntrySpecificImplizitPathVariables(resultMap, psEntry);
		return resultMap;
	}
	
	/**
	 * return a map containing user defined path variables and global implicit variables
	 * Not contains project set entry specific path variables
	 */
	public static Map<String, String> getProjectSetEntryIndependentPathVariableMap() {
		Map<String, String> resultMap = new HashMap<String, String>();
		addUserDefinedPathVariables(resultMap);
		addGlobalImplizitPathVariables(resultMap);
		return resultMap;
	}
	
	/** return default location if unresolvedLocation is null or empty
	 * Replace path variables in unresolvedLocation
	 * Return null if unresolvedLocation has undefined path variables
	 * 
	 * For better performance a path variable map can be cached outside
	 * this method (by calling getProjectSetEntryIndependentPathVariableMap()) and given to this 
	 * method by the parameter cachedPathVariableMap. If cachedPathVariableMap is null
	 * this method calls getProjectSetEntryIndependentPathVariableMap() itself.
	 */
	public static String resolve(String unresolvedLocation, Map<String,String> projectSetEntryIndependentPathVariableMap, ProjectSetEntry psEntry) {
		if ((unresolvedLocation == null)
			|| (unresolvedLocation.length() == 0)) {
			// return default path for project
			return getDefaultLocation(psEntry);
		} else {
			// resolve path variables
			StringBuffer resolvedLocation = new StringBuffer();
			// parse String
			int start = 0;
			while (start != -1) {
				int varStart = unresolvedLocation.indexOf(
					PATH_VARIABLE_START_STRING,
					start);

				if (varStart == -1) {
					// no variable any more, so append rest of unresolved
					resolvedLocation.append(unresolvedLocation.substring(start));	
					start = -1;
				} else {
					// append unresolved until start of variable
					resolvedLocation.append(unresolvedLocation.substring(start, varStart));
					// get variable name
					int varEnd = unresolvedLocation.indexOf(PATH_VARIABLE_END_STRING, varStart);
					if (varEnd == -1) {
						// var begin without end !!
						resolvedLocation.append(unresolvedLocation.substring(varStart));
						start = -1;
					} else {
						// append value of variable
						String name = unresolvedLocation.substring(
							varStart + PATH_VARIABLE_START_STRING.length(),
							varEnd).trim();
						
						// use cached map or create temporary map
						if (projectSetEntryIndependentPathVariableMap == null) {
							projectSetEntryIndependentPathVariableMap = getProjectSetEntryIndependentPathVariableMap();
						}
						
						String pathVariableValue = projectSetEntryIndependentPathVariableMap.get(name);
						
						if (pathVariableValue == null) {
							if (psEntry != null) {
								// check for project set entry specific path variable
								Map<String, String> psEntrySpecificImplizitPathVariablesMap = new HashMap<String, String>();
								addProjectSetEntrySpecificImplizitPathVariables(psEntrySpecificImplizitPathVariablesMap, psEntry);
								pathVariableValue = psEntrySpecificImplizitPathVariablesMap.get(name);
							}
						}
							
						if (pathVariableValue == null) {
							// used path variable is undefined
							return null;
						} else {
							// append value of variable
							resolvedLocation.append(pathVariableValue);
						}
						// set start for next iteration behind end of variable
						start = varEnd + PATH_VARIABLE_END_STRING.length();
					}
				}
			}
			return new Path(resolvedLocation.toString()).toOSString();
		}
	}
	
	/**
	 * Add global path variables to map.
	 * Don't overwrite already defined entries in map.
	 */	
	public static void addGlobalImplizitPathVariables(Map<String, String> pathVariableMap) {
		if (pathVariableMap.get(WORKSPACE_PATH_VARIABLE) == null) {
			pathVariableMap.put(WORKSPACE_PATH_VARIABLE, getWorkspacePathVariableDefaultValue());	
		}
		if (pathVariableMap.get(USERHOME_PATH_VARIABLE) == null) {
			// define user home if it is defined on the platform
			final String tmpUserHomePath= getUserHomePathVariableDefaultValue();
			if (tmpUserHomePath != null) {
				pathVariableMap.put(USERHOME_PATH_VARIABLE, tmpUserHomePath);
			}
		}
		if (pathVariableMap.get(INSTALLROOT_PATH_VARIABLE) == null) {
			pathVariableMap.put(INSTALLROOT_PATH_VARIABLE, getInstallRootPathVariableDefaultValue());
		}
	}
	
	/**
	 * Add project set entry specific path variables to map.
	 * Don't overwrite already defined entries in map
	 */	
	public static void addProjectSetEntrySpecificImplizitPathVariables(Map<String, String> pathVariableMap, ProjectSetEntry psEntry) {
		// entry specific path variables
		if (psEntry != null) {
			if (pathVariableMap.get(PROJECTNAME_PATH_VARIABLE) == null) {
				pathVariableMap.put(PROJECTNAME_PATH_VARIABLE, psEntry.getProjectName());
			}
			if (pathVariableMap.get(PROJECTTAG_PATH_VARIABLE) == null) {
				pathVariableMap.put(PROJECTTAG_PATH_VARIABLE, psEntry.getTag());
			}
		}
	}

	/**
	 * Add user defined path variables to map
	 */	
	public static void addUserDefinedPathVariables(Map<String, String> pathVariableMap) {
		String codedMap = Platform.getPreferencesService().getString(ProjectSetPlugin.PLUGIN_ID, IProjectSetConstants.PSF_PATH_VARIABLES_PREFERENCE, null, null);
		if (codedMap != null) {
			// parse String
			int varStart = codedMap.indexOf(PATH_VARIABLE_START_STRING);
			while (varStart != -1) {
				int varEnd = codedMap.indexOf(PATH_VARIABLE_END_STRING, varStart);
				String name = codedMap.substring(varStart + PATH_VARIABLE_START_STRING.length(), varEnd);
				varStart = codedMap.indexOf(PATH_VARIABLE_START_STRING, varEnd + PATH_VARIABLE_END_STRING.length());
				String path = null;
				if (varStart == -1) {
					path = codedMap.substring(varEnd + PATH_VARIABLE_END_STRING.length());
				} else {
					path = codedMap.substring(varEnd + PATH_VARIABLE_END_STRING.length(), varStart);				
				}
				pathVariableMap.put(name, path);
			}
		}
	}

	
	/**
	 * @return the default value of the workspace variable
	 */
	private static String getWorkspacePathVariableDefaultValue() {
		return ProjectSetPlugin.getWorkspace().getRoot().getLocation().toOSString() +
			File.separatorChar;
	}
	
	/**
	 * @return the default value of the user home variable
	 */
	private static String getUserHomePathVariableDefaultValue() {
		final String tmpUserHome= System.getProperty("user.home"); //$NON-NLS-1$
		return tmpUserHome == null ? null : (tmpUserHome + java.io.File.separatorChar);
	}

	/**
	 * @return the default value of the installroot variable
	 */
	private static String getInstallRootPathVariableDefaultValue() {
		return (new File(Platform.getInstallLocation().getURL().getPath())).getParent() + java.io.File.separatorChar;
	}

	/**
	 * pathVariableMap must be a map where
	 *   key = variable name
	 *   value = path
	 */	
	public static void storePathVariableMap(Map<String, String> pathVariableMap) {
		StringBuffer stringBuffer = new StringBuffer();
		Iterator<Map.Entry<String, String>> entryIterator = pathVariableMap.entrySet().iterator();
		while (entryIterator.hasNext()) {
			Map.Entry<String, String> entry = entryIterator.next();
			String name = entry.getKey();
			String path = entry.getValue();
			
			stringBuffer.append(PATH_VARIABLE_START_STRING);
			stringBuffer.append(name);
			stringBuffer.append(PATH_VARIABLE_END_STRING);
			stringBuffer.append(path);
		}
		InstanceScope.INSTANCE.getNode(ProjectSetPlugin.PLUGIN_ID).put(IProjectSetConstants.PSF_PATH_VARIABLES_PREFERENCE, stringBuffer.toString());
	}

}
