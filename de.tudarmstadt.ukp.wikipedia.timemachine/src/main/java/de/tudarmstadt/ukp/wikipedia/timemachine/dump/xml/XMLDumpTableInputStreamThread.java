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
package de.tudarmstadt.ukp.wikipedia.timemachine.dump.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;

import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.NamespaceFilter;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.AbstractXmlDumpReader;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.DumpTableEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread for converting of XML stream to SQL stream.
 */
class XMLDumpTableInputStreamThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Enable the main and category pages as well as discussions
	 */
	private static final String ENABLED_NAMESPACES = "NS_MAIN,NS_TALK,NS_CATEGORY";

	/**
	 * Generalization {@link de.tudarmstadt.ukp.wikipedia.mwdumper.importer.XmlDumpReader}
	 * that parses the XML dump
	 */
	private AbstractXmlDumpReader xmlReader;

	/**
	 * completion flag for a conversion process
	 */
	private boolean isComplete;

	/**
	 * Initiate input and output streams
	 *
	 * @param iStream
	 *            XML input stream
	 * @param oStream
	 *            SQL output stream
	 * @throws IOException Thrown in case errors occurred.
	 */
	public XMLDumpTableInputStreamThread(InputStream iStream,
			OutputStream oStream, DumpTableEnum table) throws IOException {
		super("xml2sql");

		switch (table) {
		case PAGE:
			xmlReader = new PageReader(iStream, new NamespaceFilter(
					new PageWriter(oStream), ENABLED_NAMESPACES));
			break;
		case REVISION:
			xmlReader = new RevisionReader(iStream, new NamespaceFilter(
					new RevisionWriter(oStream), ENABLED_NAMESPACES));
			break;
		case TEXT:
			xmlReader = new TextReader(iStream, new NamespaceFilter(
					new TextWriter(oStream), ENABLED_NAMESPACES));
			break;

		}

	}

	@Override
	public synchronized void run() {
		try {
			isComplete = false;
			xmlReader.readDump();
			isComplete = true;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Abort a conversion
	 */
	public synchronized void abort() {
		if (!isComplete) {
			xmlReader.abort();
			isComplete = true;
		}
	}
}
