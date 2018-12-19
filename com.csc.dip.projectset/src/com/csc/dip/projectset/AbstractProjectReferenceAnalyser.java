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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.TeamException;

public abstract class AbstractProjectReferenceAnalyser
	implements IProjectReferenceAnalyser
{
	/**
	 * The provider's unique identifier
	 */
	private String provider;

	/**
	 * Return the unique identifier for the provider.
	 * Subclasses may override.
	 * 
	 * @return the provider's id (not <code>null</code>)
	 * @throws RuntimeException if the provider's id cannot be determined
	 */
	public String getProvider() {
		if (provider != null)
			return provider;
		for (Iterator<Map.Entry<String, IProjectReferenceAnalyser>> iter = ProjectSetUtil.getProjectReferenceAnalyserMap().entrySet().iterator(); iter.hasNext();) {
			Map.Entry<String, IProjectReferenceAnalyser> entry = iter.next();
			if (entry.getValue() == this) {
				provider = entry.getKey();
				return provider;
			}
		}
		// Shouldn't ever happen
		throw new RuntimeException(Messages.getString("AbstractProjectReferenceAnalyser.Failed_to_determine_provider")); //$NON-NLS-1$
	}
	
	protected RepositoryProviderType getRepositoryProviderType() throws TeamException {
		RepositoryProviderType repProviderType = RepositoryProviderType.getProviderType(getProvider());
		if (repProviderType == null)
			throw new TeamException(Messages.getString("AbstractProjectReferenceAnalyser.Failed_to_locate_RepositoryProviderType_for_provider_") + getProvider()); //$NON-NLS-1$
		return repProviderType;
	}
	
	protected ProjectSetCapability getProjectSetCapability() throws TeamException {
		return getRepositoryProviderType().getProjectSetCapability();
	}
	
	/**
	 * Redirects the request 
	 * 
	 * @see com.csc.dip.projectset.IProjectReferenceAnalyser#asReference(IProject[], ProjectSetContext, IProgressMonitor)
	 */
	public String[] asReference(IProject[] providerProjects, ProjectSetContext context, IProgressMonitor monitor, String projectSetFilename) throws TeamException {
		return getProjectSetCapability().asReference(
					providerProjects,
					context.getProjectSetSerializationContext(projectSetFilename),
					monitor);
	}

	/**
	 * Redirects the request
	 * 
	 * @see com.csc.dip.projectset.IProjectReferenceAnalyser#addToWorkspace(String[], String, ProjectSetContext, IProgressMonitor)
	 */
	public IProject[] addToWorkspace(String[] referenceStrings, ProjectSetContext context, IProgressMonitor monitor, String projectSetFilename) throws TeamException {
		return getProjectSetCapability().addToWorkspace(
					referenceStrings,
					context.getProjectSetSerializationContext(projectSetFilename),
					monitor);
	}

}
