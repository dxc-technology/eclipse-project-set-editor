package com.csc.dip.projectset;

/**
 * @author dbu
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class ProjectVersion implements IProjectVersion {

	protected String serializerString;
	
	/**
	 * Constructor for ProjectVersion.
	 */
	public ProjectVersion() {
		super();
	}

	/**
	 * @see com.csc.dip.projectset.IProjectVersion#getProjectName()
	 */
	public String getProjectName() {
		return null;
	}

	/**
	 * @see com.csc.dip.projectset.IProjectVersion#getVersionName()
	 */
	public String getVersionName() {
		return null;
	}

	/**
	 * @see com.csc.dip.projectset.IProjectVersion#setSerializerString(String)
	 */
	public void setSerializerString(String serializerString) {
		this.serializerString = serializerString;
	}

}
