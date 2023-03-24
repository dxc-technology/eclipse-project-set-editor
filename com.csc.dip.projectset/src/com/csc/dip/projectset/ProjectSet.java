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
 * This class represents a list of project references.
 */

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class ProjectSet {

	// constants for reading and writing a projects set from/to a XML file
	private static final String XML_ELEM_PSF = "psf"; //$NON-NLS-1$
	private static final String XML_ATTR_VERSION = "version"; //$NON-NLS-1$		
	private static final String XML_ELEM_PROVIDER = "provider"; //$NON-NLS-1$
	private static final String XML_ATTR_ID = "id"; //$NON-NLS-1$
	private static final String XML_ELEM_PROJECT = "project"; //$NON-NLS-1$
	private static final String XML_ATTR_REFERENCE = "reference"; //$NON-NLS-1$
	private static final String PSF_VERSION = "2.0"; //$NON-NLS-1$
	
	private List<ProjectSetEntry> projectSetEntries = new ArrayList<ProjectSetEntry>();
	private IFile projectSetFile;
	private boolean isFileCorrupted = false;
	private ProjectSetEntry parentProjectSetEntry;		

	protected class ProjectSetEntryComparator implements Comparator<ProjectSetEntry> {
		public int compare(ProjectSetEntry projectSetEntry1, ProjectSetEntry projectSetEntry2) {
			String pse1ProjectName =  projectSetEntry1.getProjectName();
			//TODO: what happens when pse2ProjectName == null? what happens if both project names are null?
			if (pse1ProjectName == null) {
				return -1;
			} else {
					return pse1ProjectName.compareTo(projectSetEntry2.getProjectName());
			}
		}
	}
	
	/**
	 * Creates a project set and load the content if the param load is true
	 * 
	 * @param projectSetFile the project set file
	 * @param updateFromFile if true load content from file. Use false if you want to create a new project set from scratch
	 */
	public ProjectSet(IFile projectSetFile, boolean updateFromFile) {
		super();
			this.projectSetFile = projectSetFile;
			if (updateFromFile) {
				update();
			}
		}
	
	/**
	 * Creates a project set and set the projects as content of the project set.
	 * 
	 * The projects must be loaded and shared !
	 * 
	 * @param projectSetFile
	 *            the project set file
	 * @param sharedAndLoadedProjects
	 *            the projects that will be the content of the project set
	 * @throws NewProjectSetEntryInvTargetException
	 * @throws NewProjectSetEntryException
	 */
	public ProjectSet(IFile projectSetFile, IProject[] sharedAndLoadedProjects) throws NewProjectSetEntryInvTargetException, NewProjectSetEntryException {
		this(projectSetFile, false);
		for (int i = 0; i < sharedAndLoadedProjects.length; i++) {
			IProject project = sharedAndLoadedProjects[i];
			ProjectSetEntry psEntry = new ProjectSetEntry(this, project);
			addProjectSetEntry(psEntry);
		}
	}

	/** 
	 * Returns a copy of this project set
	 * -makes copies of projectSetEntries (non recursive!)
	 * -copies only references to parent project set entry and project set file
	 */
	public ProjectSet copy() {
				
		ProjectSet copy = new ProjectSet(projectSetFile, false);
		copy.setParentProjectSetEntry(parentProjectSetEntry);

		Iterator<ProjectSetEntry> entryIterator = projectSetEntries.iterator();
		while (entryIterator.hasNext()) {
			ProjectSetEntry psEntry = entryIterator.next();
			copy.addProjectSetEntry(psEntry.copy());
		}
		return copy;
	}

	/** 
	 * Updates the project set entries from the project set file
	 * and update the entries recursively 
	 * 
	 * This method do not check for conflicts !
	 */
	public void update() {
		update(new HashSet<IFile>());
	}


	/**
	 * Updates the project set entries from the project set file
	 * and update the entries recursively 
	 * 
	 * Add the project set file to the list updatedProjectSetFiles
	 * and check for endless recursion.
	 * 
	 * This method do not check for conflicts ! 
	 */
	protected void update(Set<IFile> updatedProjectSetFiles) {	
		// check for endless recursion
		if (updatedProjectSetFiles.add(getProjectSetFile())) {
			
			// load set entries from file
			projectSetEntries = new ArrayList<ProjectSetEntry>();
			Map<String, List<String>> loadedMap = loadProjectSetReferenceMap(projectSetFile);
			if (loadedMap != null) {		
				addProjectSetReferenceMap(loadedMap);
			}		

			// update preferred project locations
			PreferredLocationsUtil.updatePreferredProjectLocations(this);

			// recursively update loaded entries
			updateEntries(updatedProjectSetFiles);
		}
	}

	/**
	 * Updates the current project set entries and update them recursively.
	 * Do not load the list of entries for THIS project set from file,
	 * but uses the current entries !
	 * (If an entry has a sub project set it will be updated from its project set file)
	 * 
	 * This method do not check for conflicts ! 
	 */
	public void  updateEntries() {
		updateEntries(new HashSet<IFile>());
	}
	
	/**
	 * Updates the current project set entries and update them recursively.
	 * Do not load the list of entries for THIS project set from file,
	 * but uses the current entries !
	 * (If an entry has a sub project set it will be updated from its project set file)
	 * 
	 * Add the project set file to the list updatedProjectSetFiles
	 * and check for endless recursion.
	 * 
	 * This method do not check for conflicts ! 
	 */
	protected void updateEntries(Set<IFile> updatedProjectSetFiles) {
		updatedProjectSetFiles.add(getProjectSetFile());	
		Iterator<ProjectSetEntry> entryIterator = projectSetEntries.iterator();
		while (entryIterator.hasNext()) {
			ProjectSetEntry psEntry = entryIterator.next();
			psEntry.update(updatedProjectSetFiles);
		}		
	}
	
	/** 
	 * Loads a project set file and return a map where key is the provider name 
	 * and value is a list of project reference strings	
	 */
	protected Map<String, List<String>> loadProjectSetReferenceMap(IFile projectSetFile) {
		if(!(projectSetFile.getFullPath().toFile().exists() && projectSetFile.getFullPath().toFile().canRead()) && !(projectSetFile.exists() && projectSetFile.isAccessible())){
			return null;
		}
		// map where key is the provider name and value is a list of project reference strings
		Map<String, List<String>> map = new HashMap<String, List<String>>();

		Document document = null;

		try {
			if(projectSetFile.getLocation() == null) {
				File psFile = projectSetFile.getFullPath().toFile();
				document = XMLUtil.readDocument(psFile);
			}else {
				document = XMLUtil.readDocument(projectSetFile.getLocation().toFile());	
			}
		} catch (ParserConfigurationException e) {
			isFileCorrupted = true;
			return null;
		} catch (SAXException e) {
			isFileCorrupted = true;
			return null;
		} catch (IOException e) {
			isFileCorrupted = true;
			return null;
		}
	
        Element psfElement = document.getDocumentElement();
        if (!psfElement.getNodeName().equals(XML_ELEM_PSF)) {
        	// error: root element must be psf
        	isFileCorrupted = true;
			return null;
        }
        // version currently unused
        //String psfVersion = psfElement.getAttribute(XML_ATTR_VERSION);

		NodeList providerNodes = psfElement.getElementsByTagName(XML_ELEM_PROVIDER);
		for (int i = 0; i < providerNodes.getLength(); i++) {
			Node providerNode = providerNodes.item(i);
			Node providerIDNode = providerNode.getAttributes().getNamedItem(XML_ATTR_ID);
			if (providerIDNode == null) {
				// error: attribute id required
			} else {
				List<String> referenceList = new ArrayList<String>();
				String providerID = providerIDNode.getNodeValue();
				NodeList projectNodes = providerNode.getChildNodes();
				
				for (int j = 0; j < projectNodes.getLength(); j++) {
					Node projectNode = projectNodes.item(j);
					if (projectNode.getNodeName().equals(XML_ELEM_PROJECT)) {
						Node projectReferenceNode = projectNode.getAttributes().getNamedItem(XML_ATTR_REFERENCE);
						if (projectReferenceNode == null) {
							// error: attribute reference required
						} else {
							String projectReference = projectReferenceNode.getNodeValue();
							referenceList.add(projectReference);
						}
					}
				}
				map.put(providerID, referenceList);
			}
		}
        		
		return map;		
	}


	/**
	 * Writes the content to the project set file
	 */
	public void write() {
		
		Document document = null;
		try {
			document = XMLUtil.createNewDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}
		
		Element psfElement = document.createElement(XML_ELEM_PSF);
		document.appendChild(psfElement);
		psfElement.setAttribute(XML_ATTR_VERSION, PSF_VERSION);
		
		Map<String, List<ProjectSetEntry>> map = getProjectSetMap();		
		
		Iterator<Map.Entry<String, List<ProjectSetEntry>>> mapIterator = map.entrySet().iterator();
		while (mapIterator.hasNext()) {
			Map.Entry<String, List<ProjectSetEntry>> mapEntry = mapIterator.next();
			String provider = mapEntry.getKey();
			List<ProjectSetEntry> projectRefList = mapEntry.getValue();
			
			Element providerElement = document.createElement(XML_ELEM_PROVIDER);
			psfElement.appendChild(providerElement);
			providerElement.setAttribute(XML_ATTR_ID, provider);
			
			Iterator<ProjectSetEntry> projectEntryIterator = projectRefList.iterator();
			while (projectEntryIterator.hasNext()) {
				ProjectSetEntry psEntry = projectEntryIterator.next();
				Element projectElement = document.createElement(XML_ELEM_PROJECT);
				providerElement.appendChild(projectElement);
				projectElement.setAttribute(XML_ATTR_REFERENCE, psEntry.getProjectReference());				
			}
			
		}		
		
		// write XML document to file
		try {
			XMLUtil.writeDocument(document, projectSetFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			projectSetFile.refreshLocal(IResource.DEPTH_ZERO, null);				
		} catch (CoreException e) {
			// do nothing
		}
		
		//write preferred project locations
		PreferredLocationsUtil.writePreferredProjectLocations(this);
	}


	/** 
	 * Adds entries retrieved from map where key is the provider name 
	 * and value is a list of project reference strings
	 */
	protected void addProjectSetReferenceMap(Map<String, List<String>> projectSetReferenceMap) {		
		Iterator<Map.Entry<String, List<String>>> mapEntryIterator = projectSetReferenceMap.entrySet().iterator();
		while(mapEntryIterator.hasNext()) {
			Map.Entry<String, List<String>> mapEntry = mapEntryIterator.next();
			String providerName = mapEntry.getKey();
			Iterator<String> projectIterator = mapEntry.getValue().iterator();
			while (projectIterator.hasNext()) {
				String projectReference = projectIterator.next();
				ProjectSetEntry psEntry = new ProjectSetEntry(this, providerName, projectReference);
				addProjectSetEntry(psEntry);
			}
		}
	}
	
	/**
	 * Adds the project entry
	 */
	public void addProjectSetEntry(ProjectSetEntry projectSetEntry) {
		projectSetEntry.setProjectSet(this);
		projectSetEntries.add(projectSetEntry);
	}

	/**
	 * Removes the project entry
	 */	
	public void removeProjectSetEntry(ProjectSetEntry projectSetEntry) {
		projectSetEntry.setProjectSet(null);
		projectSetEntries.remove(projectSetEntry);
	}	
	
	/** 
	 * Checks if this project set contains the entry
	 * Uses equals (not contentEquals) for comparison of entries !
	 */
	public boolean includesProjectSetEntry(ProjectSetEntry projectSetEntry) {
		return (projectSetEntries.indexOf(projectSetEntry) != -1);
	}
	
	/**
	 * Returns all direct project set entries (not recursively)
	 */
	public ProjectSetEntry[] getProjectSetEntries() {
		return projectSetEntries.toArray(new ProjectSetEntry[projectSetEntries.size()]);
	}

	/**
	 * Returns all direct project set entries sorted by project name (not recursively)
	 */
	public ProjectSetEntry[] getSortedProjectSetEntries() {
		ProjectSetEntry[] entries = getProjectSetEntries();
		Arrays.sort(entries, new ProjectSetEntryComparator());
		return entries;
	}
	
	/** 
	 * Collects recursively all project set entries.
	 * 
	 * Entries of sub projects are only added if the sub project is loaded
	 * (This method not load/replace projects in workspace)
	 */
	public ProjectSetEntry[] getProjectSetEntriesRecursively() {
		List<ProjectSetEntry> collectedEntries = new ArrayList<ProjectSetEntry>();
		addProjectSetEntriesRecursively(collectedEntries);		
		return collectedEntries.toArray(new ProjectSetEntry[collectedEntries.size()]);
	}
	
	/**
	 * Adds recursively all project set entries to the list.
	 * 
	 * Entries of sub projects are only added if the sub project is loaded
	 * (This method not load/replace projects in workspace)
	 */ 
	protected void addProjectSetEntriesRecursively(List<ProjectSetEntry> entryList) {
		Iterator<ProjectSetEntry> entryIterator = projectSetEntries.iterator();
		while (entryIterator.hasNext()) {			
			ProjectSetEntry psEntry = entryIterator.next();
			if (!entryList.contains(psEntry)) {
				entryList.add(psEntry);
				ProjectSet subPS = psEntry.getSubProjectSet();
				if (subPS != null) {
					subPS.addProjectSetEntriesRecursively(entryList);
				}
			}
		}
	}
	
	/**
	 * Returns the Entry with the project name or null if no entry found
	 */	
	public ProjectSetEntry getEntryForProject(String projectName) {
		Iterator<ProjectSetEntry> projectEntryIterator = projectSetEntries.iterator();
		ProjectSetEntry foundEntry = null;
		while (projectEntryIterator.hasNext() && (foundEntry == null)) {
			ProjectSetEntry entry = projectEntryIterator.next();
			if (entry.getProjectName().equals(projectName)) {
				foundEntry = entry;
			}
		}
		return foundEntry;
	}

	/** 
	 * Returns the content of this project set as a map where
	 * key = providerName
	 * value = list of project set entries
	 * 
	 * Changing the map does not change the project set content
	 */
	public Map<String, List<ProjectSetEntry>> getProjectSetMap() {
		Map<String, List<ProjectSetEntry>> map = new HashMap<String, List<ProjectSetEntry>>();
		Iterator<ProjectSetEntry> setEntryIterator = projectSetEntries.iterator();
		while (setEntryIterator.hasNext()) {
			ProjectSetEntry projectSetEntry = setEntryIterator.next();
			String provider = projectSetEntry.getProvider();
			List<ProjectSetEntry> referencesForProvider = map.get(provider);
			if (referencesForProvider == null) {
				referencesForProvider = new ArrayList<ProjectSetEntry>();
				map.put(provider, referencesForProvider);
			}
			referencesForProvider.add(projectSetEntry);
		}
		return map;
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
	public void addToWorkspace(ProjectSetContext context, IProgressMonitor monitor, List<String> projectNames, boolean recursive) {
		addToWorkspace(context, monitor, projectNames, recursive, new HashMap<String, ProjectSetEntry>());
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
	protected void addToWorkspace(final ProjectSetContext context, IProgressMonitor monitor, List<String> projectNames, final boolean recursive, final Map<String, ProjectSetEntry> loadedEntries) {
		
		Map<String, List<ProjectSetEntry>> map;
		
		if (projectNames == null) {
			map = getProjectSetMap();
		} else {			
			map = getProjectSetMap(projectNames);
		}
		
		monitor.beginTask(MessageFormat.format(Messages.getString("ProjectSet.Loading_project_set__{0}_1"), new Object[] {getProjectSetFile().getName()}), map.size() + 1); //$NON-NLS-1$

		Iterator<Map.Entry<String, List<ProjectSetEntry>>> mapIterator = map.entrySet().iterator();
		while (mapIterator.hasNext()) {			
			Map.Entry<String, List<ProjectSetEntry>> mapEntry = mapIterator.next();
			String provider = mapEntry.getKey();
			List<ProjectSetEntry> psEntryList = mapEntry.getValue();
			
			addToWorkspace(context, SubMonitor.convert(monitor, 1), recursive, loadedEntries, provider, psEntryList);
			monitor.worked(1);
		}
		monitor.worked(1);

		monitor.done();
	}

	/**
	 * Adds/replaces projects listed for the specified provider.
	 */	
	protected void addToWorkspace(final ProjectSetContext context, IProgressMonitor monitor, final boolean recursive, final Map<String, ProjectSetEntry> loadedEntries, String provider, List<ProjectSetEntry> psEntryList) {
			
		if (!ProjectSetUtil.isProviderSupported(provider)) {
			reportStatus(
				context,
				MessageFormat.format(Messages.getString("ProjectSet.Error_while_loading_project_set,_reason__Team_provider_{0}_not_supported_2"), new Object[] {provider}) //$NON-NLS-1$
			);
			return;
		}

		// prune entries that are loaded during the recursive load
		pruneLoaded(psEntryList, loadedEntries);
		if (psEntryList.isEmpty()) {
			return;
		}

		// collect project references
		final List<ProjectSetEntry> entriesWithPreferredLocations = new ArrayList<ProjectSetEntry>();
		Iterator<ProjectSetEntry> prunedEntriesIterator = psEntryList.iterator();
		while (prunedEntriesIterator.hasNext()) {
			ProjectSetEntry psEntry = prunedEntriesIterator.next();
			
			// get preferred project locations
			if (
			  (psEntry.getUnresolvedPreferredLocation() != null) &&
			  (psEntry.getUnresolvedPreferredLocation().length() > 0) &&
			  (psEntry.getState() == ProjectSetEntry.STATE_NOT_LOADED)) {
			  	entriesWithPreferredLocations.add(psEntry);
			}
		}
		
		monitor.beginTask(MessageFormat.format(Messages.getString("ProjectSet.Loading_project_sets_for_provider__{0}_3"), new Object[] {provider}), entriesWithPreferredLocations.size() + 2 * psEntryList.size() + 1); //$NON-NLS-1$

		try {

			// create empty projects for project locations
			Iterator<ProjectSetEntry> entryIterator = entriesWithPreferredLocations.iterator();
			while (entryIterator.hasNext()) {
				createEmptyProjectIfNotExists(entryIterator.next(), SubMonitor.convert(monitor, 1));
				monitor.worked(1);
			}
			
			if (Platform.getPreferencesService().getBoolean(ProjectSetPlugin.PLUGIN_ID, IProjectSetConstants.REFRESH_BEFORE_LOADING_PREFERENCE, false, null)) {
				// refresh all existing projects before loading (to suppress out of sync warnings)
				Iterator<ProjectSetEntry> entriesIterator = psEntryList.iterator();
				while (entriesIterator.hasNext()) {
					ProjectSetEntry psEntry = entriesIterator.next();
					IProject projectInWorkspace = psEntry.getProjectInWorkspace();
					if (projectInWorkspace != null) {
						projectInWorkspace.refreshLocal(IResource.DEPTH_INFINITE, SubMonitor.convert(monitor, 1));
					}
				}
			}
			
			List<ProjectSetEntry> problematicProjects = psEntryList.stream()
					.filter(psEntry -> {
						try {
							IProject project = psEntry.getProjectInWorkspace();
							if(project != null) {
								// check only for errors in constructor
								new ProjectSetEntry(psEntry.getProjectSet(), project);
							}
							return false;
						} catch (NewProjectSetEntryInvTargetException | NewProjectSetEntryException e) {
							return true;
						}
					})
					.collect(Collectors.toList());

			psEntryList.removeAll(problematicProjects);
			// add projects to workspace
			IProjectReferenceAnalyser prAnalyser = ProjectSetUtil.getProjectReferenceAnalyser(provider);
			String[] projectReferences = psEntryList.stream().map(projectSet -> projectSet.getProjectReference()).toArray(String[]::new);
			IProject[] projects = prAnalyser.addToWorkspace(projectReferences, context, SubMonitor.convert(monitor, projectReferences.length), getProjectSetFilename());
			monitor.worked(projectReferences.length);
			
			// createdProjects = null, if cancel pressed
			if (projects != null && projects.length > 0) {
				addToLoaded(provider, projects, loadedEntries);
				if (recursive) {
					addSubProjectSetsForCreated(context, projects, SubMonitor.convert(monitor, projectReferences.length), loadedEntries);
				}
			}
			// perform recursive load for projects that already existed in the workspace (Those are not in projects array returned by "addToWorkspace")
			if (recursive) {
				List<IProject> existingProjects = new ArrayList<IProject>();
				for (String projectReference : projectReferences) {
					String projectName = prAnalyser.getProjectName(projectReference);
					if (projectName != null) {
						// check if project is in list of created (=> recursive load is handled above)
						boolean isHandled = false;
						if (projects != null) {
							for (int i = 0; i < projects.length && !isHandled; i++) {
								IProject handledProject = projects[i];
								if (projectName.equals(handledProject.getName())) {
									isHandled = true;	
								}
							}
						}
						if (!isHandled) {
							// check if project exists
							IProject projectInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
							if (projectInWorkspace.exists()) {
								existingProjects.add(projectInWorkspace);
							}
						}
					}
				}
				if (!existingProjects.isEmpty()) {
					IProject[] existingProjectsArray = (IProject[]) existingProjects.toArray(new IProject[existingProjects.size()]);
					addToLoaded(provider, existingProjectsArray, loadedEntries);
					addSubProjectSetsForCreated(context, existingProjectsArray , SubMonitor.convert(monitor, existingProjectsArray.length), loadedEntries);				
				}
			}
			
			monitor.worked(projectReferences.length + 1);
			
			if(!problematicProjects.isEmpty()) {
				String problematicProjectsNames = problematicProjects.stream()
						.map(psEntry -> psEntry.getProjectName())
						.reduce((a, b) -> a + ", " + b) //$NON-NLS-1$
						.orElse(""); //$NON-NLS-1$
				throw new TargetValidationException(
						MessageFormat.format(Messages.getString("ProjectSetEntry.Project(s)_0_cannot_be_loaded"), new Object[]{problematicProjectsNames})); //$NON-NLS-1$
			}
			
		} catch (Exception e) {
			if (!(e instanceof OperationCanceledException)) {	
				String errorMessage = Messages.getString("ProjectSet.Error_while_loading_project_set"); //$NON-NLS-1$
				if (e.getMessage() != null) {
					errorMessage = errorMessage
						+ Messages.getString("ProjectSet.reason") //$NON-NLS-1$
						+ e.getMessage();
					reportStatus(context, errorMessage);
				}
			}
		}

		monitor.done();
			
		// Preserve original behavior where cancel only stops loading the projects 
		// from the current provider, but loading of the project set continues
		monitor.setCanceled(false);
	}

	protected void reportStatus(ProjectSetContext context, String errorMessage) {
		context.reportStatus(new Status(IStatus.ERROR, ProjectSetPlugin.PLUGIN_ID, IStatus.OK, errorMessage, null));
	}
	
	
	protected void createEmptyProjectIfNotExists(ProjectSetEntry projectSetEntry, IProgressMonitor monitor) throws CoreException {
		IWorkspace workspace = ProjectSetPlugin.getWorkspace();
		String projectName = projectSetEntry.getProjectName();
		IProject project = workspace.getRoot().getProject(projectName);
		if (!project.exists()) {
			final IProjectDescription description = workspace.newProjectDescription(projectName);
			String resolvedPreferredLocation = projectSetEntry.getResolvedPreferredLocation();
			if (resolvedPreferredLocation == null) {
				// undefined path variable !!
				// should be checked before loading
				throw new CoreException(new Status(Status.ERROR, ProjectSetPlugin.PLUGIN_ID, Status.OK, Messages.getString("ProjectSet.Undefined_path_variable"), null)); //$NON-NLS-1$
			} else {
				description.setLocation(new Path(resolvedPreferredLocation));
				project.create(description, monitor);
			}
		}
	}
	
	/** 
	 * Checks if projects are already loaded inside a recursive load process.
	 * remove project reference from list if already loaded .
	 * @param psEntryList list of entries for the provider
	 * @param loadedEntries temporary Map that hold the entries that are already loaded during
	 *                       a recursive load (key = projectName, value = project set entry)
	 */
	protected void pruneLoaded(List<ProjectSetEntry> psEntryList, Map<String, ProjectSetEntry> loadedEntries) {
		Iterator<ProjectSetEntry> fullListIterator = new ArrayList<ProjectSetEntry>(psEntryList).iterator();
		while (fullListIterator.hasNext()) {
			ProjectSetEntry psEntry = fullListIterator.next();
			String projectName = psEntry.getProjectName();
			ProjectSetEntry loadedPsEntry = loadedEntries.get(projectName);
			if (loadedPsEntry != null) {
				if (psEntry.contentEqualsIgnorePreferredLocation(loadedPsEntry)){
					// System.out.println("WARNING: Project "+projectName+" already loaded in the SAME version while recursive load process");
				} else {					
					// System.out.println("WARNING: Project "+projectName+" already loaded in DIFFERENT version while recursive load process");
				}
				psEntryList.remove(psEntry);
			}
		}
	}
	
	/** 
	 * Adds the projects to the map of the loaded projects 
	 */
	protected void addToLoaded(String provider, IProject[] projects, Map<String, ProjectSetEntry> loadedEntries) {
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			try {
				loadedEntries.put(project.getName(), new ProjectSetEntry(this, project));
			} catch (NewProjectSetEntryInvTargetException | NewProjectSetEntryException e) {
				//TODO: Log the exception and later on do something else if a problem occurs.
				e.printStackTrace();
			}
		}
	}
	
	/** 
	 * Add/replaces recursively project sets from project set projects 
	 * for the creates projects
	 */
	protected void addSubProjectSetsForCreated(ProjectSetContext context, IProject[] createdProjects, IProgressMonitor monitor, Map<String, ProjectSetEntry> loadedEntries) {
		if (createdProjects.length == 0)
			return;

		monitor.beginTask(MessageFormat.format(Messages.getString("ProjectSet.Loading_{0,number,integer}_project_sets_4"), new Object[] {new Integer(createdProjects.length)}), createdProjects.length); //$NON-NLS-1$
		
		Iterator<ProjectSetEntry> entryIterator = projectSetEntries.iterator();
		while (entryIterator.hasNext()) {			
			ProjectSetEntry psEntry = entryIterator.next();
			// check if project of entry is in list of the created projects
			boolean inListOfCreated = false;
			for (int i = 0;((i < createdProjects.length) && (!inListOfCreated)); i++) {
				if (createdProjects[i].getName().equals(psEntry.getProjectName())) {
					inListOfCreated = true;
				}
			}
			if (inListOfCreated) {
				psEntry.update();
				ProjectSet subPS = psEntry.getSubProjectSet();
				if (subPS != null) {
					subPS.addToWorkspace(context, SubMonitor.convert(monitor, 1), null, true, loadedEntries);
				}
			}
			monitor.worked(1);
		}
		monitor.done();
	}
	
	protected Map<String, List<ProjectSetEntry>> getProjectSetMap(List<String> projectNames) {
		Map<String, List<ProjectSetEntry>> prunedMap = new HashMap<String, List<ProjectSetEntry>>();
		Iterator<Map.Entry<String, List<ProjectSetEntry>>> mapEntryIterator = getProjectSetMap().entrySet().iterator();
		while (mapEntryIterator.hasNext()) {
			Map.Entry<String, List<ProjectSetEntry>> mapEntry = mapEntryIterator.next();
			String provider = mapEntry.getKey();
			List<ProjectSetEntry> prunedList = new ArrayList<ProjectSetEntry>();						
			Iterator<ProjectSetEntry> fullListIterator = mapEntry.getValue().iterator();
			while (fullListIterator.hasNext()) {
				ProjectSetEntry psEntry = fullListIterator.next();
				String projectName = psEntry.getProjectName();
				if (projectNames.indexOf(projectName) != -1) {
					prunedList.add(psEntry);
				}
			}
			if (prunedList.size() > 0) {
				prunedMap.put(provider, prunedList);
			}
		}
		return prunedMap;
	}
	
	/**
	 * Returns the projectSetFile.
	 * @return IFile
	 */
	public IFile getProjectSetFile() {
		return projectSetFile;
	}

	/**
	 * Sets the projectSetFile.
	 * @param projectSetFile The projectSetFile to set
	 */
	public void setProjectSetFile(IFile projectSetFile) {
		this.projectSetFile = projectSetFile;
	}

	/**
	 * Returns the absolute projectSetFile.
	 * @return String
	 */
	public String getProjectSetFilename() {
		return projectSetFile.getLocation().toOSString();
	}	

	/**
	 * Returns the parentProjectSetEntry.
	 * @return ProjectSetEntry
	 */
	public ProjectSetEntry getParentProjectSetEntry() {
		return parentProjectSetEntry;
	}

	/**
	 * Sets the parentProjectSetEntry.
	 * @param parentProjectSetEntry The parentProjectSetEntry to set
	 */
	public void setParentProjectSetEntry(ProjectSetEntry parentProjectSetEntry) {
		this.parentProjectSetEntry = parentProjectSetEntry;
	}


	/**
	 * Checks for conflicts and return ROOT entries that have errors
	 */
	public ProjectSetEntry[] updateErrors() {
		// reset errors, collect conflicts and set errors for undefined path variables
		Map<String, String> cachedPathVariableMap = PreferredLocationsUtil.getProjectSetEntryIndependentPathVariableMap();
		// maps for finding conflicts
		Map<String, List<ProjectSetEntry>> projectNameToEntryListMap = new HashMap<String, List<ProjectSetEntry>>(); // key=project name, value=list of ps entries with the project name
		Map<ProjectSetEntry, String> resolvedPreferredLocations = new HashMap<ProjectSetEntry, String>(); // key=ps entry, value = resolved location
		
		// maps and lists for entries with conflicts
		Map<ProjectSetEntry, Collection<ProjectSetEntry>> tagConflictMap = new HashMap<ProjectSetEntry, Collection<ProjectSetEntry>>();
		Map<ProjectSetEntry, Collection<ProjectSetEntry>> pathConflictMap = new HashMap<ProjectSetEntry, Collection<ProjectSetEntry>>();
		Map<ProjectSetEntry, Collection<ProjectSetEntry>> samePathConflictMap = new HashMap<ProjectSetEntry, Collection<ProjectSetEntry>>();
		List<ProjectSetEntry> entriesWithUndefinedVariables = new ArrayList<ProjectSetEntry>();
		List<ProjectSetEntry> entriesLoadedToDifferentPath = new ArrayList<ProjectSetEntry>();
		PathInclusionDetector pathInclusionDetector = new PathInclusionDetector();
		
		ProjectSetEntry[] allEntries = getProjectSetEntriesRecursively();
		for (int i = 0; i < allEntries.length; i++) {
			ProjectSetEntry projectSetEntry = allEntries[i];
			// reset errors
			projectSetEntry.resetError();
			
			// check for undefined path variable in preferred projects locations
			String resolvedPrefLocation = projectSetEntry.getResolvedPreferredLocation(cachedPathVariableMap);
			
			if (resolvedPrefLocation == null) {
				entriesWithUndefinedVariables.add(projectSetEntry);
			} else {
				// check if project is loaded in different location
				if (projectSetEntry.getState() != ProjectSetEntry.STATE_NOT_LOADED) {
					IProject projectInWorkspace = projectSetEntry.getProjectInWorkspace();
					if ((projectInWorkspace != null) &&
						(!ProjectSetUtil.pathEquals(resolvedPrefLocation, projectInWorkspace.getLocation().toOSString())) &&
						ProjectSetUtil.isProviderSupportsPreferredLocalDirectory(projectSetEntry.getProvider())) {
						entriesLoadedToDifferentPath.add(projectSetEntry);
					}
				}
			}
			
			// search for conflicts with other projects
			
			// compare with project set entries for the same project name
			List<ProjectSetEntry> projectEntriesForSameProject = projectNameToEntryListMap.get(projectSetEntry.getProjectName());
			if (projectEntriesForSameProject == null) {
				// first entry for the project name, create a new List and put it in the map
				List<ProjectSetEntry> newEntryList = new ArrayList<ProjectSetEntry>();
				newEntryList.add(projectSetEntry);
				projectNameToEntryListMap.put(projectSetEntry.getProjectName(), newEntryList);
			} else {
				// check that project is only used with one tag and one repository location
				Iterator<ProjectSetEntry> psEntryIterator = projectEntriesForSameProject.iterator();
				while (psEntryIterator.hasNext()) {
					ProjectSetEntry compareEntry = psEntryIterator.next();
					if (!projectSetEntry.contentEqualsIgnorePreferredLocation(compareEntry)) {
						// conflict in tag or repository location
						// add entry to conflict map
						Collection<ProjectSetEntry> entryList = tagConflictMap.get(projectSetEntry);
						if (entryList == null) {
							entryList = new ArrayList<ProjectSetEntry>();
							tagConflictMap.put(projectSetEntry, entryList);
						}
						entryList.add(compareEntry);
						// add compared Entry to conflict map
						Collection<ProjectSetEntry> entryListFromCompared = tagConflictMap.get(compareEntry);
						if (entryListFromCompared == null) {
							entryListFromCompared = new ArrayList<ProjectSetEntry>();
							tagConflictMap.put(compareEntry, entryListFromCompared);
						}
						entryListFromCompared.add(projectSetEntry);
					}
					// check for conflicts in preferred projects locations
					String compareResolvedPrefLocation = resolvedPreferredLocations.get(compareEntry);
					if ((resolvedPrefLocation != null) &&
					   (!ProjectSetUtil.pathEquals(resolvedPrefLocation, compareResolvedPrefLocation))&&
					   ProjectSetUtil.isProviderSupportsPreferredLocalDirectory(projectSetEntry.getProvider())&&
					   ProjectSetUtil.isProviderSupportsPreferredLocalDirectory(compareEntry.getProvider())) {
						// preferred path conflict !
						Collection<ProjectSetEntry> pathEntryList = pathConflictMap.get(projectSetEntry);
						if (pathEntryList == null) {
							pathEntryList = new ArrayList<ProjectSetEntry>();
							pathConflictMap.put(projectSetEntry, pathEntryList);
						}
						pathEntryList.add(compareEntry);
						Collection<ProjectSetEntry> pathEntryListFromCompared = pathConflictMap.get(compareEntry);
						if (pathEntryListFromCompared == null) {
							pathEntryListFromCompared = new ArrayList<ProjectSetEntry>();
							pathConflictMap.put(compareEntry, pathEntryListFromCompared);
						}
						pathEntryListFromCompared.add(projectSetEntry);				    	
					}					
				}
			}
			
			// check for entries with same preferred path but different projects
			if(resolvedPrefLocation != null) {
				// A null resolvedPrefLocation already causes an error at another point. 
				pathInclusionDetector.addPath(new File(resolvedPrefLocation), projectSetEntry);
			}
			resolvedPreferredLocations.put(projectSetEntry, resolvedPrefLocation);
		}
		
		samePathConflictMap = pathInclusionDetector.getConflicts();
		
		// set errors for conflicts
		setErrorsForConflictMap(
			tagConflictMap,
			ProjectSetEntry.ERROR_TYPE_TAG_CONFLICT,
			Messages.getString("ProjectSet.Tag_Conflict_with"), //$NON-NLS-1$
			Messages.getString("ProjectSet.Tag_Conflict_in"), //$NON-NLS-1$
			Messages.getString("ProjectSet.AND_Tag_Conflict_in")); //$NON-NLS-1$
		// set errors for path conflicts
		setErrorsForConflictMap(
			pathConflictMap,
			ProjectSetEntry.ERROR_TYPE_PATH_CONFLICT,
			Messages.getString("ProjectSet.Path_Conflict_with"), //$NON-NLS-1$
			Messages.getString("ProjectSet.Path_Conflict_in"), //$NON-NLS-1$
			Messages.getString("ProjectSet.AND_Path_Conflict_in")); //$NON-NLS-1$
		setErrorsForConflictMap(
			samePathConflictMap,
			ProjectSetEntry.ERROR_TYPE_SAME_PATH_CONFLICT,
			Messages.getString("ProjectSet.Duplicate_path_segment_with"),  //$NON-NLS-1$
			Messages.getString("ProjectSet.Duplicate_path_segment_in"),  //$NON-NLS-1$
			Messages.getString("ProjectSet.and_duplicate_path_segment_in")); 		 //$NON-NLS-1$
		setErrorsForList(
			entriesWithUndefinedVariables,
			ProjectSetEntry.ERROR_TYPE_PATH_VARIABLE_UNDEFINED,
			Messages.getString("ProjectSet.Undefined_variable"), //$NON-NLS-1$
			Messages.getString("ProjectSet.Undefined_variable_in"), //$NON-NLS-1$
			Messages.getString("ProjectSet.and_in")); //$NON-NLS-1$
		setErrorsForList(
			entriesLoadedToDifferentPath,
			ProjectSetEntry.ERROR_TYPE_NOT_LOADED_TO_PREFERRED_PATH,
			Messages.getString("ProjectSet.Not_loaded_to_preferred_path"), //$NON-NLS-1$
			Messages.getString("ProjectSet.Not_loaded_to_preferred_path_in"), //$NON-NLS-1$
			Messages.getString("ProjectSet.and_not_loaded_to_preferred_path_in")); //$NON-NLS-1$
		
		Set<ProjectSetEntry> rootEntriesWithErrors = new HashSet<ProjectSetEntry>();
		filterAndAddRootEntriesToSet(rootEntriesWithErrors, tagConflictMap.keySet());
		filterAndAddRootEntriesToSet(rootEntriesWithErrors, pathConflictMap.keySet());
		filterAndAddRootEntriesToSet(rootEntriesWithErrors, samePathConflictMap.keySet());
		filterAndAddRootEntriesToSet(rootEntriesWithErrors, entriesWithUndefinedVariables);
		filterAndAddRootEntriesToSet(rootEntriesWithErrors, entriesLoadedToDifferentPath);
		
		return rootEntriesWithErrors.toArray(new ProjectSetEntry[rootEntriesWithErrors.size()]);
	}

	protected void filterAndAddRootEntriesToSet(Set<ProjectSetEntry> rootEntrySet, Collection<ProjectSetEntry> psEntries) {
		Iterator<ProjectSetEntry> entryIterator = psEntries.iterator();
		while (entryIterator.hasNext()) {
			ProjectSetEntry entry = entryIterator.next();
			if (includesProjectSetEntry(entry)) {
				rootEntrySet.add(entry);
			}
		}
	}

	protected void setErrorsForConflictMap(Map<ProjectSetEntry, Collection<ProjectSetEntry>> conflictMap, int errorType, String errorText, String subErrorText, String andErrorText) {
			
		Iterator<Map.Entry<ProjectSetEntry, Collection<ProjectSetEntry>>> conflictMapIterator = conflictMap.entrySet().iterator();
		while (conflictMapIterator.hasNext()) {
			Map.Entry<ProjectSetEntry, Collection<ProjectSetEntry>> mapEntry = conflictMapIterator.next();
			ProjectSetEntry psEntry = mapEntry.getKey();
			Iterator<ProjectSetEntry> conflictEntryIterator = mapEntry.getValue().iterator();
			// set error state
			psEntry.setErrorState(errorType, ProjectSetEntry.ERROR_STATE_ERROR);
			// calculate and set conflict description
			StringBuffer errorDescrBuffer = new StringBuffer();
			errorDescrBuffer.append(errorText);
			errorDescrBuffer.append(" "); //$NON-NLS-1$
			while (conflictEntryIterator.hasNext()) {
				ProjectSetEntry conflictEntry = conflictEntryIterator.next();
				errorDescrBuffer.append(conflictEntry.getTreePositionString());
				if (conflictEntryIterator.hasNext()) {
					errorDescrBuffer.append(", "); //$NON-NLS-1$
				}
			}
			psEntry.setErrorDescription(errorType, errorDescrBuffer.toString());
			// set errors for parent entries
			setErrorInParentEntries(
				psEntry,
				errorType,
				subErrorText,
				andErrorText);
		}		
	}

	protected void setErrorsForList(List<ProjectSetEntry> entriesWithErrors, int errorType, String errorText, String subErrorText, String andErrorText) {
		Iterator<ProjectSetEntry> entryIterator = entriesWithErrors.iterator();
		while (entryIterator.hasNext()) {
			ProjectSetEntry psEntry = entryIterator.next();

			psEntry.setErrorState(errorType, ProjectSetEntry.ERROR_STATE_ERROR);
			psEntry.setErrorDescription(errorType, errorText);
			
			setErrorInParentEntries(psEntry, errorType, subErrorText, andErrorText);
		}		
	}
	
	protected void setErrorInParentEntries(ProjectSetEntry psEntry, int errorType, String subErrorText, String andErrorText) {
			ProjectSetEntry currentPSEntry = psEntry;
			do {
				ProjectSet parentPS = currentPSEntry.getProjectSet();
				if (parentPS != null) {
					currentPSEntry = parentPS.getParentProjectSetEntry();
					if (currentPSEntry != null) {
						if (currentPSEntry.getErrorState(errorType) == ProjectSetEntry.ERROR_STATE_OK) {
							currentPSEntry.setErrorState(errorType, ProjectSetEntry.ERROR_STATE_ERROR_ONLY_IN_SUB_PS);
							currentPSEntry.setErrorDescription(
								errorType,
								subErrorText + " " + //$NON-NLS-1$
								psEntry.getTreePositionString());
						} else
						if ((currentPSEntry.getErrorState(errorType) == ProjectSetEntry.ERROR_STATE_ERROR_ONLY_IN_SUB_PS) ||
							(currentPSEntry.getErrorState(errorType) == ProjectSetEntry.ERROR_STATE_ERROR_ALSO_IN_SUB_PS)) {
							currentPSEntry.setErrorDescription(
							    errorType,
								currentPSEntry.getErrorDescription(errorType) +
								", " + psEntry.getTreePositionString());  //$NON-NLS-1$
						} else 						 
						if (currentPSEntry.getErrorState(errorType) == ProjectSetEntry.ERROR_STATE_ERROR) {
							currentPSEntry.setErrorState(errorType, ProjectSetEntry.ERROR_STATE_ERROR_ALSO_IN_SUB_PS);
							currentPSEntry.setErrorDescription(
								errorType,
								currentPSEntry.getErrorDescription(errorType) +
								" " + andErrorText + " " + //$NON-NLS-1$ //$NON-NLS-2$
								psEntry.getTreePositionString());
						} 
					}
				} else{
					currentPSEntry = null;
				}			
			} while (currentPSEntry != null);		
	}

		
	/**
	 * Checks recursively whether all projects are loaded in
	 * the workspace with the specified tag
	 */
	public boolean isLoaded() {
		boolean isLoaded = true;
		Iterator<ProjectSetEntry> entryIterator = projectSetEntries.iterator();
		while (entryIterator.hasNext() && isLoaded) {
			ProjectSetEntry psEntry = entryIterator.next();
			if (psEntry.getState() != ProjectSetEntry.STATE_LOADED) {
				isLoaded = false;
			} else {
				ProjectSet subPS = psEntry.getSubProjectSet();
				if (subPS != null) {
					isLoaded = subPS.isLoaded();
				}
			}
		}
		return isLoaded;
	}	 

	/**
	 * Checks if any entry (or sub entry) has an undefined path variable
	 */
	public boolean hasUndefinedPathVariable(boolean checkSubEntries) {
		boolean hasUndefinedPathVar = false;
		ProjectSetEntry[] errorEntries = updateErrors();
		for (int i = 0; i < errorEntries.length; i++) {
			ProjectSetEntry psEntry = errorEntries[i];
			int pathErrorState = psEntry.getErrorState(ProjectSetEntry.ERROR_TYPE_PATH_VARIABLE_UNDEFINED) ;
			if(checkSubEntries) {
				if (pathErrorState != ProjectSetEntry.ERROR_STATE_OK) {
				    hasUndefinedPathVar = true;
				}
			} else {
				if ((pathErrorState == ProjectSetEntry.ERROR_STATE_ERROR) ||
					(pathErrorState == ProjectSetEntry.ERROR_STATE_ERROR_ALSO_IN_SUB_PS)) {
					hasUndefinedPathVar = true;
				}
			}
		}
		return hasUndefinedPathVar;
	}
	
	public boolean isFileCorrupted() {
		return isFileCorrupted;
	}
}
