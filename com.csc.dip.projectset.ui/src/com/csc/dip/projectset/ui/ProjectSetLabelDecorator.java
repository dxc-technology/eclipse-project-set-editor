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
package com.csc.dip.projectset.ui;

/**
 * @author Dirk Baumann
 * 
 * Label decorator for error and warning decorations in project set projects
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;

import com.csc.dip.projectset.IProjectSetConstants;

public class ProjectSetLabelDecorator extends LabelProvider
		implements
			ILightweightLabelDecorator {

	/**
	 * Constructor for ProjectSetLabelDecorator.
	 */
	public ProjectSetLabelDecorator() {
		super();
	}

	/**
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
	 *      org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object object, IDecoration decoration) {
		// Get the resource using the adaptable mechanism.
		IResource resource = getResource(object);

		if (resource == null) {
			return;
		} else {
			ImageDescriptor decoratingImageDescriptor = getDecoratingImageDescriptor(resource);
			if (decoratingImageDescriptor != null) {
				
				//DemoDecoratorManager.removeResource(objectResource);
				
				decoration.addOverlay(decoratingImageDescriptor);
			}
		}
	}
	
	protected ImageDescriptor getDecoratingImageDescriptor(IResource resource) {
		ImageDescriptor decoratingImageDescriptor = null;
		
		//IMarker[] directPsfMarkers = null;
		IMarker[] allPsfMarkers = null;	
		try {
			if ((resource.exists()) && (resource.isAccessible())) {
				//directPsfMarkers = resource.findMarkers(IProjectSetUIConstants.PSF_MARKER_ID, true, IResource.DEPTH_ZERO);
				allPsfMarkers = resource.findMarkers(IProjectSetConstants.PSF_MARKER_ID, true, IResource.DEPTH_INFINITE);
			}
		} catch (CoreException e) {
			//
		}
		if ((allPsfMarkers != null) &&(allPsfMarkers.length > 0)) {
			// check for conflict markers
			boolean hasErrorMarker = false;
			boolean hasTagConflictMarker = false;
			boolean hasPathConflictMarker = false;			
			boolean hasNotLoadedMarker = false;
			boolean hasNotLoadedToPreferredMarker = false;
			for (int i = 0; i < allPsfMarkers.length; i++) {
				IMarker marker = allPsfMarkers[i];
				String markerType = null;
				try {
					markerType = marker.getType();
				} catch (CoreException e) {
				}
				if (markerType != null) {
					if (markerType.equals(IProjectSetConstants.PSF_TAG_CONFLICT_MARKER_ID)) {
						hasTagConflictMarker = true;
					} else 
					if (markerType.equals(IProjectSetConstants.PSF_PATH_CONFLICT_MARKER_ID)) {
						hasPathConflictMarker = true;
					} else 					
					if (markerType.equals(IProjectSetConstants.PSF_NOT_LOADED_MARKER_ID)) {
						hasNotLoadedMarker = true;
					} else
					if (markerType.equals(IProjectSetConstants.PSF_ERROR_MARKER_ID)) {
						hasErrorMarker = true;
					} else
					if (markerType.equals(IProjectSetConstants.PSF_NOT_LOADED_TO_PREFERRED_MARKER_ID)) {
						hasNotLoadedToPreferredMarker = true;
					}				
				}
			}
			// get image for type of error
			if (hasErrorMarker) {
				decoratingImageDescriptor = ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/markererror.gif"); //$NON-NLS-1$
			} else 			
			if (hasTagConflictMarker || hasPathConflictMarker) {
				decoratingImageDescriptor = ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/markerconflict.gif"); //$NON-NLS-1$
			} else 
			if (hasNotLoadedMarker || hasNotLoadedToPreferredMarker) {
				decoratingImageDescriptor = ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/markernotloaded.gif"); //$NON-NLS-1$
			}
		} 
		return decoratingImageDescriptor;
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}
	
	/**
	 * Returns the resource for the given input object, or null if there is no
	 * resource associated with it.
	 * 
	 * @param object the object to find the resource for
	 * @return the resource for the given object, or null
	 */
	private IResource getResource(Object object) {
		if (object instanceof IResource) {
			return (IResource) object;
		}
		if (object instanceof IAdaptable) {
			return (IResource) ((IAdaptable) object)
					.getAdapter(IResource.class);
		}
		return null;
	}
	
	/**
	 * Updates the labeled resource  
	 */
	public void updateLabelsForResources(final IResource[] resources) {
		fireLabelEvent(new LabelProviderChangedEvent(this, resources));
	}
	
	/**
	 * Post the label event to the UI thread
	 * 
	 * @param events  the events to post
	 */
	private void fireLabelEvent(final LabelProviderChangedEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				fireLabelProviderChanged(event);
			}
		});
	}

}
