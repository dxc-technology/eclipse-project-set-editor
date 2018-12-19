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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

import com.csc.dip.projectset.IProjectSetConstants;
import com.csc.dip.projectset.ProjectSetNature;
import com.csc.dip.projectset.ProjectSetProjectDescription;

public class ProjectSetProjectPropertyPage extends PropertyPage {
	
	protected Combo psfFileCombo;
	protected ProjectSetProjectDescription projectSetProjectDescription;
	protected boolean isDirty;
	
	protected Control createContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		Label label = new Label(composite, SWT.NULL);
		label.setText(Messages.getString("ProjectSetProjectPropertyPage.Project_set_file")); //$NON-NLS-1$
		
		psfFileCombo = new Combo(composite, SWT.BORDER);
		psfFileCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		projectSetProjectDescription = getProjectSetProjectDescription();
		psfFileCombo.setItems(getPSFFileNamesInProject());
		psfFileCombo.setText(projectSetProjectDescription.getPsfFilename());
		psfFileCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				isDirty = true;
			}		
		});
				
		return composite;
	}
	
	private String[] getPSFFileNamesInProject() {
		final List<String> collectedPsfNames = new ArrayList<String>();
		try {
			getProject().accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					String fileExtension = resource.getFileExtension();
					if ((fileExtension != null) && 
						(IProjectSetConstants.PROJECT_SET_FILE_EXTENSION.equals(fileExtension.toLowerCase()))) {
						collectedPsfNames.add(resource.getProjectRelativePath().toString());
					}
					return true;
				}
			});
		} catch (CoreException e) {
			// ignore exception
			e.printStackTrace();
		}
		return collectedPsfNames.toArray(new String[collectedPsfNames.size()]);
	}
	
	protected IProject getProject() {
		return (IProject) getElement().getAdapter(IProject.class);
	}

	protected ProjectSetNature getProjectSetNature() {
		IProject project = getProject();
		try {
			return (ProjectSetNature)(project.getNature(ProjectSetNature.PS_PROJECT_NATURE_ID));
		} catch (CoreException e) {
			return null;
		}	
	}
	
	protected ProjectSetProjectDescription getProjectSetProjectDescription() {
		ProjectSetNature projectSetNature = getProjectSetNature();
		if (projectSetNature != null) {
			return projectSetNature.getProjectSetProjectDescription();
		} else {
			// create a new default psp description
			ProjectSetProjectDescription pspDescription = new ProjectSetProjectDescription();
			pspDescription.setPsfFilename(getDefaultProjectSetFileName());
			return pspDescription;
		}
	}
	
	public boolean performOk() {
		if (isDirty) {
			ProjectSetNature projectSetNature = getProjectSetNature();
			if (projectSetNature != null) {
				projectSetProjectDescription.setPsfFilename(psfFileCombo.getText());
				projectSetNature.createPSProjectDescriptionFile(projectSetProjectDescription);
			}
			isDirty = false;
		}
		return true;
	}
	
	protected String getDefaultProjectSetFileName() {
		return getProject().getName()+"."+IProjectSetConstants.PROJECT_SET_FILE_EXTENSION; //$NON-NLS-1$
	}
	
	protected void performDefaults() {
		psfFileCombo.setText(getDefaultProjectSetFileName());
		super.performDefaults();
	}
}
