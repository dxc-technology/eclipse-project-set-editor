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
package com.csc.dip.projectset.svn;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.csc.dip.projectset.AbstractProjectReferenceAnalyser;

/*Subclipse/Subversive*/

public class SVNProjectReferenceAnalyser extends AbstractProjectReferenceAnalyser {

	/**
	 * Constructor for SVNProjectReferenceAnalyser.
	 */
	public SVNProjectReferenceAnalyser() {
		super();
	}

	/**
	 * Because Subversive generates different project references per workspace this method
	 * only compares the project name, tag and location to determine equality (it also assumes
	 * that the same provider is used)
	 * 
	 * @see com.csc.dip.projectset.IProjectReferenceAnalyser#isEqual(String, String)
	 */
	public boolean areEqual(String projectReference1, String projectReference2) {
		return getProjectName(projectReference1).equals(getProjectName(projectReference2)) &&
			   getTag(projectReference1).equals(getTag(projectReference2)) &&
			   getLocation(projectReference1).equals(getLocation(projectReference2));
	}

	/**
	 * @see com.csc.dip.projectset.IProjectReferenceAnalyser#getProjectName(String)
	 */
	public String getProjectName(String projectReference) {
		StringTokenizer tokenizer = new StringTokenizer(projectReference, ","); //$NON-NLS-1$
		tokenizer.nextToken();
		tokenizer.nextToken();
		return tokenizer.nextToken();
	}

	/**
	 * @see com.csc.dip.projectset.IProjectReferenceAnalyser#getVersionName(String)
	 */
	public String getTag(String projectReference) {
		IPath url = new Path(getLocation(projectReference));
		String name = url.lastSegment();
		if(name.equalsIgnoreCase("trunk")){
			return "Trunk";
		}else{
			url = url.removeLastSegments(1);
			String structureNode = url.lastSegment();
			if (structureNode.equalsIgnoreCase("tags")){
				return name + " (Tag)";
			}else if (structureNode.equalsIgnoreCase("branches")) {
				return name + " (Branch)";
			}
		}
		return "Not found (Make sure you're using a conventional SVN repository structure)";
	}

	/**
	 * @see com.csc.dip.projectset.IProjectReferenceAnalyser#getLocation(String)
	 */
	public String getLocation(String projectReference){
		StringTokenizer tokenizer = new StringTokenizer(projectReference, ","); //$NON-NLS-1$
		tokenizer.nextToken();
		return tokenizer.nextToken();
	}

	/**
	 * @see com.csc.dip.projectset.IProjectReferenceAnalyser#getProviderName()
	 */
	public String getProviderName() {
		if("org.eclipse.team.svn.core.svnnature".equals(getProvider())){
			return "SVN Subversive";
		}else if("org.tigris.subversion.subclipse.core.svnnature".equals(getProvider())){
			return "SVN Subclipse";
		}			
		return "SVN"; //$NON-NLS-1$
	}

}
