package com.csc.dip.projectset;

/**
 * @author dbu
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface IProjectVersion {
	
	public String getProjectName();
	
	public String getVersionName();
	
	public void setSerializerString(String serializerString);
}
