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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.ProjectSetSerializationContext;

public class ProjectSetContext {

	ProjectSetSerializationContext projectSetSerializationContext;

	/**
	 * 
	 */
	public ProjectSetContext() {
		super();
	}

	/**
	 * Return the ProjectSetSerializationContext to be used in the context
	 * 
	 * @return ProjectSetSerializationContext
	 */
	public ProjectSetSerializationContext getProjectSetSerializationContext(String projectSetFilename) {
		if ((projectSetSerializationContext == null) ||
			(!projectSetSerializationContext.getFilename().equals(projectSetFilename))) {
			projectSetSerializationContext = createProjectSetSerializationContext(projectSetFilename);
		}
		return projectSetSerializationContext;
	}

	/**
	 * Create a new ProjectSetSerializationContext
	 * Can be overwritten in subclasses.
	 * 
	 * @return ProjectSetSerializationContext
	 */
	protected ProjectSetSerializationContext createProjectSetSerializationContext(String projectSetFilename) {
		return new ProjectSetSerializationContext(projectSetFilename);
	}
	
	/**
	 * Report the specified status.
	 * This default implementation appends the status to the log.
	 * Subclasses should override this method as desired.
	 * 
	 * @param status the status (not <code>null</code>)
	 */
	public void reportStatus(IStatus status) {
		ProjectSetPlugin.getDefault().getLog().log(status);
	}
	
}
