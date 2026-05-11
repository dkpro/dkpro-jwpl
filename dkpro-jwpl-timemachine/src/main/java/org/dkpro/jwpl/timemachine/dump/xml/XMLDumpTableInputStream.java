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
package org.dkpro.jwpl.timemachine.dump.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableEnum;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableInputStream;

/**
 * Decorator for an {@link InputStream}. Converts an XML source to SQL result in a separated thread
 * via {@code org.mediawiki.importer.XmlDumpReader}
 *
 * @see DumpTableInputStream
 */
public class XMLDumpTableInputStream
    extends DumpTableInputStream
{

    private static final int BUFFERSIZE = 8192;
    /**
     * piped result stream, that is buffered for better performance
     */
    private BufferedInputStream result;
    /**
     * thread where the conversion algorithm should run
     */
    private XMLDumpTableInputStreamThread xmlInputThread;

    /**
     * Decorator for InputStream, which allows to convert an XML input stream to SQL.
     *
     * @param inputStream The XML input stream to process.
     * @param table The type of table to dump.
     * @throws IOException Thrown if IO errors occurred.
     */
    @Override
    public void initialize(InputStream inputStream, DumpTableEnum table) throws IOException
    {
        final PipedOutputStream decodedStream = openPipe();
        xmlInputThread = new XMLDumpTableInputStreamThread(inputStream, decodedStream, table);
        xmlInputThread.start();
    }

    /**
     * Multi-part equivalent of {@link #initialize(InputStream, DumpTableEnum)}. Each element of
     * {@code inputStreams} is a self-contained Wikipedia XML dump part; SAX events across parts
     * are collapsed into a single logical document before being written to the SQL sink.
     *
     * @param inputStreams Ordered list of XML part streams (ascending page-range). Must not be
     *                     {@code null} or empty and must not contain {@code null} elements.
     * @param table        The type of table to dump.
     * @throws IOException Thrown if IO errors occurred while setting up the pipe.
     */
    public void initialize(List<InputStream> inputStreams, DumpTableEnum table) throws IOException
    {
        final PipedOutputStream decodedStream = openPipe();
        xmlInputThread = new XMLDumpTableInputStreamThread(inputStreams, decodedStream, table);
        xmlInputThread.start();
    }

    private PipedOutputStream openPipe() throws IOException
    {
        /*
         * piped input stream, that allows to read from a <code>decodedStream</code>
         */
        PipedInputStream unbufferedResult = new PipedInputStream();
        /*
         * piped output stream where the conversion thread <code>XMLInputStreamThread</code> is
         * writing in
         */
        PipedOutputStream decodedStream = new PipedOutputStream(unbufferedResult);
        result = new BufferedInputStream(unbufferedResult, BUFFERSIZE);
        return decodedStream;
    }

    @Override
    public int read() throws IOException
    {
        return result.read();
    }

    @Override
    public int available() throws IOException
    {
        return result.available();
    }

    @Override
    public void close() throws IOException
    {
        result.close();
        xmlInputThread.abort();
    }

    @Override
    public void mark(int readlimit)
    {
        result.mark(readlimit);
    }

    @Override
    public void reset() throws IOException
    {
        result.reset();
    }

    @Override
    public boolean markSupported()
    {
        return result.markSupported();
    }

}
