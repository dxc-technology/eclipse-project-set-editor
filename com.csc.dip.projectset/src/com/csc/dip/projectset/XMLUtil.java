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
 * 
 * This is a utility class with methods for XML processing
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XMLUtil {
	
	public static final String ENCODING = "UTF-8"; //$NON-NLS-1$

	/**
	 * Constructor for XMLUtil.
	 */
	public XMLUtil() {
		super();
	}
	
	/**
	 * Writes a XML document to a file
	 */
	public static void writeDocument(Document document, IFile file) throws IOException, CoreException {
		// write out temporary in Byte array and use file.setcontent/create to support watch/edit mode
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(outStream, ENCODING);
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING);
			transformer.transform(new DOMSource(document), new StreamResult(writer));
		} catch (TransformerConfigurationException e) {
			throw (IOException) new IOException(e.toString()).initCause(e);
		} catch (TransformerException e) {
			throw (IOException) new IOException(e.toString()).initCause(e);
		} finally {
			outStream.close();
		}
		
		InputStream inputStream = new ByteArrayInputStream(outStream.toByteArray());
		if(file.exists()) {
			file.setContents(inputStream, true, true, null);
		} else {
			file.create(inputStream, true, null);
		}
	}

	/**
	 * Reads a XML document to a file
	 */
	public static Document readDocument(File file)
		throws 
		ParserConfigurationException, SAXException,IOException{
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		//factory.setValidating(validating);
		DocumentBuilder builder = factory.newDocumentBuilder();
		/*
		builder.setErrorHandler(new ErrorHandler() {
		 ...   
		});
		*/
		
		return builder.parse(file);
	}
	
	/**
	 * Creates a new XML document
	 */	
	public static Document createNewDocument() throws ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		//factory.setValidating(validating);
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		return builder.newDocument();
	}
}
