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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

/**
 * @author Dirk Baumann
 * 
 * 
 * This interface is responsible for analysing a project reference string
 * from a project set file. 
 * 
 * For every team provider that should be used for this project set plugin
 * this interface must be implemented and made accessible by using the
 * extension point com.csc.dip.projectset.projectReferenceAnalyser
 * 
 * 
 * Example:
 * 
 * <extension
 *    id="CVSProjectReferenceAnalyser"
 *    name="CVS Project Reference Analyser"
 *    point="com.csc.dip.projectset.projectReferenceAnalyser">
 *    <projectReferenceAnalyser
 *       class="com.csc.dip.projectset.ccvs.CVSProjectReferenceAnalyser"
 *       provider="org.eclipse.team.cvs.core.cvsnature">
 *    </projectReferenceAnalyser>
 * </extension>
 */


public interface IProjectReferenceAnalyser {
	
	/**
	 * Returns whether the project references are equal
	 * 
	 * @param projectReference1 project reference for comparison
	 * @param projectReference2 other project reference to comparison
	 * @return result of the comparison
	 */
	public boolean areEqual(String projectReference1, String projectReference2);

	
	/**
	 * Returns the name of the project for the reference.
	 * This is the name the project has in the workspace and
	 * not the name in the repository (which may be different)
	 * 
	 * @param projectReference the project reference string
	 * @return the project name
	 */
	public String getProjectName(String projectReference);

	
	/**
	 * Returns the name of the version or branch in the
	 * reference string. (For CVS this also my be 'HEAD')
	 * 
	 * @param projectReference the project reference string
	 * @return the tag
	 */
	public String getTag(String projectReference);


	/**
	 * Returns the complete location described in the project reference
	 * including the server location and the path in the repository
	 * 
	 * @param projectReference the project reference string
	 * @return the location
	 */
	public String getLocation(String projectReference);


	/**
	 * Returns a short name for the team provider
	 * 
	 * @return short provider name
	 */
	public String getProviderName();
	
	/**
	 * Answer the unique identifier for the provider.
	 * Subclasses may override.
	 * 
	 * @return the provider's id
	 */
	public String getProvider();
	
	/**
	 * For every IProject in providerProjects, return an opaque
	 * UTF-8 encoded String to act as a reference to that project.
	 * The format of the String is specific to the provider.
	 * The format of the String must be such that
	 * IProjectSetSerializer.addToWorskpace() will be able to
	 * consume it and recreate a corresponding project.
	 * 
	 * @param providerProjects  an array of projects that the serializer should create
	 *   text references for
	 * @param context  a UI context object. This object will either be a 
	 *                 com.ibm.swt.widgets.Shell or it will be null.
	 * @param monitor  a progress monitor
	 * @return String[] an array of serialized reference strings uniquely identifying the projects
	 * @throws TeamException
	 * @see #addToWorkspace(String[] referenceStrings, String filename, ProjectSetContext context, IProgressMonitor monitor)
	 */
	public String[] asReference(IProject[] providerProjects, ProjectSetContext context, IProgressMonitor monitor, String projectSetFilename) throws TeamException;
	
	/**
	 * For every String in referenceStrings, create in the workspace a
	 * corresponding IProject.  Return an Array of the resulting IProjects.
	 * Result is unspecified in the case where an IProject of that name
	 * already exists. In the case of failure, a TeamException must be thrown.
	 * The opaque strings in referenceStrings are guaranteed to have been previously
	 * produced by IProjectSetSerializer.asReference().
	 * 
	 * @param referenceStrings  an array of reference strings uniquely identifying the projects
	 * @param context  a UI context object. This object will either be a 
	 *                 com.ibm.swt.widgets.Shell or it will be null.
	 * @param monitor  a progress monitor
	 * @return IProject[]  an array of projects that were created
	 * @throws TeamException
	 * @see #asReference(IProject[] providerProjects, ProjectSetContext context, IProgressMonitor monitor)
	 */
	public IProject[] addToWorkspace(String[] referenceStrings, ProjectSetContext context, IProgressMonitor monitor, String projectSetFilename) throws TeamException;

}
