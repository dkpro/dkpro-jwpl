/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml;

import java.io.InputStream;

import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.DumpWriter;

/**
 * Universal XML Dump Parser. Set of start and end xml tags comply with XmlDumpReader.
 *
 *
 */
public class WikiXMLDumpReader extends AbstractXmlDumpReader {

	public WikiXMLDumpReader(InputStream inputStream, DumpWriter writer) {
		super(inputStream, writer);
	}

	@Override
	protected void setupEndElements() {
		endElements.put(THREAD_SUBJECT, THREAD_SUBJECT);
		endElements.put(THREAD_PARENT, THREAD_PARENT);
		endElements.put(THREAD_ANCESTOR, THREAD_ANCESTOR);
		endElements.put(THREAD_PAGE, THREAD_PAGE);
		endElements.put(THREAD_ID, THREAD_ID);
		endElements.put(THREAD_SUMMARY_PAGE, THREAD_SUMMARY_PAGE);
		endElements.put(THREAD_AUTHOR, THREAD_AUTHOR);
		endElements.put(THREAD_EDIT_STATUS, THREAD_EDIT_STATUS);
		endElements.put(THREAD_TYPE, THREAD_TYPE);
		endElements.put(BASE, BASE);
		endElements.put(CASE, CASE);
		endElements.put(COMMENT, COMMENT);
		endElements.put(CONTRIBUTOR, CONTRIBUTOR);
		endElements.put(GENERATOR, GENERATOR);
		endElements.put(ID, ID);
		endElements.put(IP, IP);
		endElements.put(MEDIAWIKI, MEDIAWIKI);
		endElements.put(MINOR, MINOR);
		endElements.put(NAMESPACES, NAMESPACES);
		endElements.put(NAMESPACE, NAMESPACE);
		endElements.put(PAGE, PAGE);
		endElements.put(RESTRICTIONS, RESTRICTIONS);
		endElements.put(REVISION, REVISION);
		endElements.put(SITEINFO, SITEINFO);
		endElements.put(SITENAME, SITENAME);
		endElements.put(TEXT, TEXT);
		endElements.put(TIMESTAMP, TIMESTAMP);
		endElements.put(TITLE, TITLE);
		endElements.put(USERNAME, USERNAME);
	}

	@Override
	protected void setupStartElements() {
		startElements.put(REVISION, REVISION);
		startElements.put(CONTRIBUTOR, CONTRIBUTOR);
		startElements.put(PAGE, PAGE);
		startElements.put(MEDIAWIKI, MEDIAWIKI);
		startElements.put(SITEINFO, SITEINFO);
		startElements.put(NAMESPACES, NAMESPACES);
		startElements.put(NAMESPACE, NAMESPACE);
	}

}
