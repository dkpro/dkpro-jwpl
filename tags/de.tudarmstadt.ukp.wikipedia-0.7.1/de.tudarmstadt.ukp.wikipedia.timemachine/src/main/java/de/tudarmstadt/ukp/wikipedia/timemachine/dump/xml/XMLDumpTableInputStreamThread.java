/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.timemachine.dump.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.NamespaceFilter;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.AbstractXmlDumpReader;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.DumpTableEnum;

/**
 * Thread for converting of XML stream to SQL stream.
 *
 * @author ivan.galkin
 *
 */
class XMLDumpTableInputStreamThread extends Thread {
	/**
	 * Enable the main and category pages as well as discussions
	 */
	private static final String ENABLED_NAMESPACES = "NS_MAIN,NS_TALK,NS_CATEGORY";

	/**
	 * Generalization <code>org.mediawiki.importer.XmlDumpReader</code> that
	 * parses the XML dump
	 */
	private AbstractXmlDumpReader xmlReader;

	/**
	 * completion flag for a conversion process
	 */
	private boolean isComplete;

	private static final Logger log4j = Logger
			.getLogger(XMLDumpTableInputStreamThread.class);

	/**
	 * Initiate input and output streams
	 *
	 * @param iStream
	 *            XML input stream
	 * @param oStream
	 *            SQL output stream
	 * @throws IOException
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
			log4j.error(e.getMessage());
			RuntimeException rte = new RuntimeException(e);
			throw rte;
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
