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
package de.tudarmstadt.ukp.wikipedia.datamachine.dump.xml;

import java.io.IOException;
import java.io.InputStream;

import de.tudarmstadt.ukp.wikipedia.datamachine.domain.DataMachineFiles;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.NamespaceFilter;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.XmlDumpReader;

/**
 *
 * Use org.mediawiki.importer engine to parse the XML-Dump (only useful fields)
 * and store it to binary file. Compression of the output files is possible.
 *
 *
 *
 */
public class XML2Binary {
	/**
	 * Enable the main and category pages as well as discussions
	 */
	private static final String ENABLED_NAMESPACES = "NS_MAIN,NS_TALK,NS_CATEGORY";

	private static boolean USE_MODIFED_PARSER = true;

	public XML2Binary(InputStream iStream, DataMachineFiles files)
			throws IOException {
		if (USE_MODIFED_PARSER) {
			// modified parser, skips faulty tags
			new SimpleXmlDumpReader(iStream, new NamespaceFilter(
					new SimpleBinaryDumpWriter(files), ENABLED_NAMESPACES))
					.readDump();
		} else {
			// original MWDumper parser, very sensible to not closed tags
			new XmlDumpReader(iStream, new NamespaceFilter(
					new SimpleBinaryDumpWriter(files), ENABLED_NAMESPACES))
					.readDump();
		}
	}

}
