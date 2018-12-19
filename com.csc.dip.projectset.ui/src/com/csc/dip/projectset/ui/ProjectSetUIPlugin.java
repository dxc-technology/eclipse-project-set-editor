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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.csc.dip.projectset.IProjectSetBuilderListener;
import com.csc.dip.projectset.ProjectSetBuilder;

/**
 * Plugin containing the UI portion of the Project Sets functionality.
 *
 */

public class ProjectSetUIPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.csc.dip.projectset.ui"; //$NON-NLS-1$

	//The shared instance.
	private static ProjectSetUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * A listener for updating labels when a builder has changed a resource.
	 */
	private final IProjectSetBuilderListener builderListener = new IProjectSetBuilderListener() {

	    public void markersChanged(IProject project) {
			  ProjectSetUI.updateLabels(project);
		}
	};


	/**
	 * The constructor.
	 */
	public ProjectSetUIPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("com.csc.dip.projectset.ui.ProjectSetUIPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static ProjectSetUIPlugin getDefault() {
		return plugin;
	}

    public ImageDescriptor getImageDescriptor(String name){
    	ImageRegistry imageRegistry = getImageRegistry(); 
		ImageDescriptor descriptor = imageRegistry.getDescriptor(name);
		if(descriptor == null) {
			descriptor = createAndRegisterDescriptor(name);			
		}    	
    	return descriptor;
    } 	
	
    public Image getImage(String name) {
    	Image image = getImageRegistry().get(name);
    	if(image == null) {
    		createAndRegisterDescriptor(name);
    		image = getImageRegistry().get(name);
    	}
    	return image;
    }
    
	private ImageDescriptor createAndRegisterDescriptor(String path) {   
		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(ProjectSetUIPlugin.PLUGIN_ID, path);
		getImageRegistry().put(path, descriptor);
		return descriptor;
	}


	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ProjectSetBuilder.addProjectSetBuilderListener(builderListener);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		ProjectSetBuilder.removeProjectSetBuilderListener(builderListener);
		super.stop(context);
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ProjectSetUIPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
