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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.DumpTableInputStream;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.DumpTableEnum;

/**
 * Decorator for an <cod>InputStream</code>. Converts an XML source to SQL
 * result in a separated thread via
 * <code>org.mediawiki.importer.XmlDumpReader</code>
 * 
 * @update 18.11.2009 : constructor is replaced by initialize method
 * 
 * @author ivan.galkin
 * 
 */

public class XMLDumpTableInputStream extends DumpTableInputStream {

	private static final int BUFFERSIZE = 8192;
	/**
	 * output stream where the conversion thread
	 * <code>XMLInputStreamThread</code> is writing in
	 */
	private PipedOutputStream decodedStream;
	/**
	 * piped stream, that allows to read from a <code>decodedStream</code>
	 */
	private PipedInputStream unbufferedResult;
	/**
	 * piped result stream, that is buffered for better performance
	 */
	private BufferedInputStream result;
	/**
	 * thread where the conversion algorithm should run
	 */
	private XMLDumpTableInputStreamThread xmlInputThread;

	/**
	 * Decorator for InputStream, which allows to convert an XML input stream to
	 * SQL
	 * 
	 * @param inputStream
	 *            XML input stream
	 * @throws IOException
	 */
	public void initialize(InputStream inputStream, DumpTableEnum table)
			throws IOException {

		unbufferedResult = new PipedInputStream();
		decodedStream = new PipedOutputStream(unbufferedResult);
		result = new BufferedInputStream(unbufferedResult, BUFFERSIZE);

		xmlInputThread = new XMLDumpTableInputStreamThread(inputStream,
				decodedStream, table);
		xmlInputThread.start();

	}

	@Override
	public int read() throws IOException {
		return result.read();
	}

	@Override
	public int available() throws IOException {
		return result.available();
	}

	@Override
	public void close() throws IOException {
		result.close();
		xmlInputThread.abort();
	}

	@Override
	public void mark(int readlimit) {
		result.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		result.reset();
	}

	@Override
	public boolean markSupported() {
		return result.markSupported();
	}

}
