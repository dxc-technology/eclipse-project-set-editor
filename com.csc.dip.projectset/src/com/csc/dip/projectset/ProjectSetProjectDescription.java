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

/**
 * @author Dirk Baumann
 * 
 * This class holds the metadata for a project set project,
 * that is stored in the file .psproject
 * 
 */

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class ProjectSetProjectDescription {

	public static final String DEFAULT_PSF_FILENAME = "projectset.psf"; //$NON-NLS-1$

	public static final String XML_ENCODING = "UTF-8"; //$NON-NLS-1$
	public static final String XML_ELEM_PS_PROJECT_DESCRIPTION = "psProjectDescription"; //$NON-NLS-1$
	public static final String XML_ELEM_PSF_FILENAME = "psfFilename"; //$NON-NLS-1$
	
	protected String psfFilename = DEFAULT_PSF_FILENAME;
	
	/**
	 * Constructor for ProjectSetProjectDescription.
	 */
	public ProjectSetProjectDescription() {
		super();
	}

	/**
	 * Creates a ProjectSetProjectDescription from a file
	 */
	public static ProjectSetProjectDescription readFromFile(IFile file) {
		return readFromFile(file.getLocation().toOSString());
	}

	/**
	 * Creates a ProjectSetProjectDescription from a file
	 * @param filename absolute filename
	 */
	public static ProjectSetProjectDescription readFromFile(String filename) {
		if (filename == null) {
			return null;
		}
		
		ProjectSetProjectDescription psProjectDescription = new ProjectSetProjectDescription();
		
		Document document = null;

		try {
			document = XMLUtil.readDocument(new File(filename));
		} catch (ParserConfigurationException e) {
			return null;
		} catch (SAXException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	
        Element pspDescrRootElem = document.getDocumentElement();
        NodeList pspDescrNodes = pspDescrRootElem.getElementsByTagName(XML_ELEM_PSF_FILENAME);
        if (pspDescrNodes.getLength() != 1) {
        	return null;
        }
        NodeList psfFilenameNodeChildren = pspDescrNodes.item(0).getChildNodes();
        String psfFileName = null;
        for (int i = 0; (i < psfFilenameNodeChildren.getLength()) && (psfFileName == null); i++) {
			Node psfValueNode = psfFilenameNodeChildren.item(i);
	        if (psfValueNode.getNodeType() == Node.TEXT_NODE) {
	        	psfFileName = psfValueNode.getNodeValue();
	        }			
		}
		if (psfFileName == null) {
			return null;
		}		
		psProjectDescription.setPsfFilename(psfFileName);
		
		return  psProjectDescription; 
	}

	/**
	 * Writes the Project Set Project description to the file
	 * 
	 * @param filename absolute filename
	 */	
	public void writeToFile(IFile file) {
		// create XML document
		Document document = null;
		try {
			document = XMLUtil.createNewDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		Element psProjectDescriptionElement = document.createElement(XML_ELEM_PS_PROJECT_DESCRIPTION);
		document.appendChild(psProjectDescriptionElement);
		Element psfFilenameElement = document.createElement(XML_ELEM_PSF_FILENAME);
		psfFilenameElement.appendChild(document.createTextNode(getPsfFilename()));
		psProjectDescriptionElement.appendChild(psfFilenameElement);
		
		
		// write XML document to file
		try {
			XMLUtil.writeDocument(document, file);
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
	/**
	 * Returns the psfFilename.
	 * @return String
	 */
	public String getPsfFilename() {
		return psfFilename;
	}

	/**
	 * Sets the psfFilename.
	 * @param psfFilename The psfFilename to set
	 */
	public void setPsfFilename(String psfFilename) {
		this.psfFilename = psfFilename;
	}

}
