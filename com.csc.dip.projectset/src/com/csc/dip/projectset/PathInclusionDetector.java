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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * @author RWI / DBU
 *
 *	This class is used by ProjectSet to detect path inclusions of project set entries 
 */
public class PathInclusionDetector {
	public static class PathNode {
		
		// contains an entry if its path is complete to this node
		private Set<ProjectSetEntry> projectSetEntries = new HashSet<ProjectSetEntry>();
		
		// key = File, value = PathNode  
		private HashMap<File, PathNode> children = new HashMap<File, PathNode>();
		
		private boolean hasLocalConflict = false;
		
		private String firstProjectName = null;
		
		public PathNode addChild(File path) {
			PathNode childNode = children.get(path);
			if(childNode == null) {
				childNode = new PathNode();
				children.put(path, childNode);
			}
			return childNode;
		}
		
		public void addEntry(ProjectSetEntry entry) {
			projectSetEntries.add(entry);
			if(firstProjectName == null) {
				firstProjectName = entry.getProjectName();
			} else {
				if(! firstProjectName.equals(entry.getProjectName())) {
					hasLocalConflict = true;
				}
			}
		}
		
		public Set<ProjectSetEntry> getEntries() {
			return projectSetEntries;
		}
		
		public Collection<PathNode> getChildNodes() {
			return children.values();
		}
		
		public boolean hasLocalConflict() {
			return hasLocalConflict;
		}

	}
	
	
	private Map<ProjectSetEntry, Collection<ProjectSetEntry>> conflictMap;
	private PathNode root = new PathNode();
	
	public void addPath(File resolvedPath, ProjectSetEntry entry) {
		List<File> splitPath = splitPath(resolvedPath);
		PathNode currentNode = root;
		for(Iterator<File> it = splitPath.iterator(); it.hasNext();) {
			File path = it.next();
			currentNode = currentNode.addChild(path);
		}
		// current node is "endpoint" of path, thus add entry
		currentNode.addEntry(entry);
	}
	
	public List<File> splitPath(File path) {
		List<File> list = new ArrayList<File>();
		File currentPath = path;
		while(currentPath != null) {
			list.add(0, currentPath);
			currentPath = currentPath.getParentFile();
		}
		return list;
	}
	
	
	
	// Returns a map with key = projectSetEntry and value = Set of conflicting projectSetEntries
	public Map<ProjectSetEntry, Collection<ProjectSetEntry>> getConflicts() {
		conflictMap = new HashMap<ProjectSetEntry, Collection<ProjectSetEntry>>();
		collectConflicts(root, new Stack<PathNode>());
		return conflictMap;
	}
	
	private void collectConflicts(PathNode node, Stack<PathNode> parentNodes) {
		// If node has entries, then all entries of the parentNodes are conflicts
		
		for(Iterator<ProjectSetEntry> nodeEntriesIt = node.getEntries().iterator(); nodeEntriesIt.hasNext();) {
			ProjectSetEntry nodeEntry = nodeEntriesIt.next();
			for(Iterator<PathNode> parentNodesIt = parentNodes.iterator(); parentNodesIt.hasNext();) {
				PathNode parentNode = parentNodesIt.next();
				for(Iterator<ProjectSetEntry> parentNodeEntriesIt = parentNode.getEntries().iterator(); parentNodeEntriesIt.hasNext();) {
					ProjectSetEntry parentEntry = parentNodeEntriesIt.next();
					// here's a conflict
					addConflict(nodeEntry, parentEntry);
					// a conflict is with both entries, thus add conflict the other way round
					addConflict(parentEntry, nodeEntry); 
				}
			}
		}
		
		// Now check for conflicts in a single node
		if(node.hasLocalConflict()) {
			for(Iterator<ProjectSetEntry> nodeEntriesIt = node.getEntries().iterator(); nodeEntriesIt.hasNext();) {
				ProjectSetEntry nodeEntry = nodeEntriesIt.next();
				for(Iterator<ProjectSetEntry> nodeEntriesIt2 = node.getEntries().iterator(); nodeEntriesIt2.hasNext();) {
					ProjectSetEntry nodeEntry2 = nodeEntriesIt2.next();
					// Entries for the same Project may have the same path, thus no conflict in this case.
					if(! nodeEntry.getProjectName().equals(nodeEntry2.getProjectName())) {
						// Since we compare all entries with each other, adding a single-sided conflict is enough here.
						addConflict(nodeEntry, nodeEntry2);
					}
				}
			}
		}
		
		// Recursively traverse the children
		
		parentNodes.push(node);
		for(Iterator<PathNode> it = node.getChildNodes().iterator(); it.hasNext();) {
			PathNode childNode = it.next();
			collectConflicts(childNode, parentNodes);
		} 
		parentNodes.pop();
	}
	
	private void addConflict(ProjectSetEntry entry, ProjectSetEntry conflictEntry) {
		Collection<ProjectSetEntry> existingConflicts = conflictMap.get(entry);
		if(existingConflicts == null) {
			existingConflicts = new HashSet<ProjectSetEntry>();
			conflictMap.put(entry, existingConflicts);
		}
		existingConflicts.add(conflictEntry);
	}

}
