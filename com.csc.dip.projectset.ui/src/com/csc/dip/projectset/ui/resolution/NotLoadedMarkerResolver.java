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
package com.csc.dip.projectset.ui.resolution;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;


/**
 * @author Dirk Baumann
 *
 * This class return the resolutions for a NotLoadedMarker
 */

public class NotLoadedMarkerResolver implements IMarkerResolutionGenerator {

	/**
	 * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(IMarker)
	 */
	public IMarkerResolution[] getResolutions(IMarker marker) {
		return new IMarkerResolution[] {
			new AddProjectSetToWorkspaceResolution(false),
			new AddProjectSetToWorkspaceResolution(true)};
	}

}
