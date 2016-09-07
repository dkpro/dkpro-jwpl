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
