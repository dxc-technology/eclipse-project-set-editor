package com.csc.dip.projectset;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

/**
 * @author dbu
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ProjectSet {

	static final String XML_ELEM_PSF = "psf";
	static final String XML_ATTR_VERSION = "version";
		
	static final String XML_ELEM_PROVIDER = "provider";
	static final String XML_ATTR_ID = "id";

	static final String XML_ELEM_PROJECT = "project";
	static final String XML_ATTR_REFERENCE = "reference";
	
	// map where key is the provider name and value ist a list of IProjectVersion objects 
	Map map = new HashMap();
	
	/**
	 * Constructor for ProjectSet.
	 */
	public ProjectSet() {
		super();
	}

	public ProjectSet(IFile projectSetFile) {
		super();
		
	}
	
	public void writeToFile(IFile projectSetFile) {
	}
}
