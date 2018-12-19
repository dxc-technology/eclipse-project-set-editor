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

import java.text.MessageFormat;

/**
 * @author dbu
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class UnknownProjectReferenceAnalyser extends AbstractProjectReferenceAnalyser
	implements IProjectReferenceAnalyser {
		
	private String providerName;

	

	/**
	 * Constructor for UnknownProjectReferenceAnalyser.
	 */
	public UnknownProjectReferenceAnalyser(String providerName) {
		this.providerName = providerName;
	}

	/**
	 * @see com.csc.dip.projectset.IProjectReferenceAnalyser#areEqual(String, String)
	 */
	public boolean areEqual(
		String projectReference1,
		String projectReference2) {
		return false;
	}

	/**
	 * @see com.csc.dip.projectset.IProjectReferenceAnalyser#getProjectName(String)
	 */
	public String getProjectName(String projectReference) {
		return getErrorString();
	}

	/**
	 * @see com.csc.dip.projectset.IProjectReferenceAnalyser#getTag(String)
	 */
	public String getTag(String projectReference) {
		return getErrorString();
	}

	/**
	 * @see com.csc.dip.projectset.IProjectReferenceAnalyser#getLocation(String)
	 */
	public String getLocation(String projectReference) {
		return getErrorString();
	}

	/**
	 * @see com.csc.dip.projectset.IProjectReferenceAnalyser#getProviderName()
	 */
	public String getProviderName() {
		return providerName;
	}
	
	private String getErrorString() {
		return MessageFormat.format(Messages.getString("UnknownProjectReferenceAnalyser.Team_provider_{0}_not_supported_1"), new Object[] {providerName,}); //$NON-NLS-1$
	}

}
