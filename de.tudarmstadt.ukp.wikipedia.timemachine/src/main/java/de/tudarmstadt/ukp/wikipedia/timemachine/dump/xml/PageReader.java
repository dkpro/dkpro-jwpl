/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.timemachine.dump.xml;

import java.io.InputStream;

import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.DumpWriter;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.AbstractXmlDumpReader;

/**
 * This class is a specified variant of XmlDumpReader. Please see its source for more
 * information about a functionality and a license.<br>
 *
 *
 */
public class PageReader extends AbstractXmlDumpReader {

	public PageReader(InputStream inputStream, DumpWriter writer) {
		super(inputStream, writer);
	}

	@Override
	protected void setupStartElements() {
		startElements.put(REVISION, REVISION);
		startElements.put(CONTRIBUTOR, CONTRIBUTOR);
		startElements.put(PAGE, PAGE);
		startElements.put(SITEINFO, SITEINFO);
		startElements.put(NAMESPACES, NAMESPACES);
		startElements.put(NAMESPACE, NAMESPACE);
	}

	@Override
	protected void setupEndElements() {
		endElements.put(REVISION, REVISION);
		endElements.put(TIMESTAMP, TIMESTAMP);
		endElements.put(TEXT, TEXT);
		endElements.put(CONTRIBUTOR, CONTRIBUTOR);
		endElements.put(ID, ID);
		endElements.put(PAGE, PAGE);
		endElements.put(TITLE, TITLE);
		endElements.put(SITEINFO, SITEINFO);
		endElements.put(NAMESPACES, NAMESPACES);
		endElements.put(NAMESPACE, NAMESPACE);
	}
}
