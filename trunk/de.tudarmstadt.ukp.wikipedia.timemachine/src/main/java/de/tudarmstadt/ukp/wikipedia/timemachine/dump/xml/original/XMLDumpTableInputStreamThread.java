/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.timemachine.dump.xml.original;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.mediawiki.importer.DumpWriter;
import org.mediawiki.importer.XmlDumpReader;

import de.tudarmstadt.ukp.wikipedia.timemachine.dump.xml.PageWriter;
import de.tudarmstadt.ukp.wikipedia.timemachine.dump.xml.RevisionWriter;
import de.tudarmstadt.ukp.wikipedia.timemachine.dump.xml.TextWriter;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.DumpTableEnum;

/**
 * Thread for converting of XML stream to SQL stream.
 * 
 * @author ivan.galkin
 * 
 */
class XMLDumpTableInputStreamThread extends Thread {
	/**
	 * stream where XML data should be read from
	 */
	private InputStream iStream;

	/**
	 * <code>org.mediawiki.importer.XmlDumpReader</code> that completes all work
	 */
	private XmlDumpReader xmlReader;

	/**
	 * dump writer, that defines how to transform a XML dump relating to asked
	 * table
	 */
	private DumpWriter dumpWriter;

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
		this.iStream = iStream;

		this.dumpWriter = createWriter(oStream, table);
	}

	/**
	 * Create a <code>DumpWriter</code> from the <code>OutputStream</code>
	 * relating to defined table name
	 * 
	 * @param oStream
	 *            where the transformed XML data should be written in
	 * @param table
	 *            table name, that should provide an indication of
	 *            transformation algorithm / output data format
	 * @return the <code>DumpWriter</code>
	 * @throws IOException
	 *             if creation is unsuccessful
	 */
	protected DumpWriter createWriter(OutputStream oStream, DumpTableEnum table)
			throws IOException {

		DumpWriter writer = null;

		// writer = new SqlWriter15(new MySQLTraits(),new
		// SqlFileStream(oStream));

		switch (table) {
		case PAGE: {
			writer = new PageWriter(oStream);
			break;
		}
		case REVISION: {
			writer = new RevisionWriter(oStream);
			break;
		}
		case TEXT: {
			writer = new TextWriter(oStream);
			break;
		}
		}

		return writer;
	}

	/**
	 * Read XML Data from XML stream, convert it to SQL 1.5 format and write it
	 * to output stream
	 */
	@Override
	public synchronized void run() {
		try {
			isComplete = false;

			xmlReader = new XmlDumpReader(iStream, dumpWriter);
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
