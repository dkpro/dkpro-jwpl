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
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.function.IntPredicate;

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
     * capacity of the pipe between the conversion thread and the reader; the JDK default of
     * 1 KB forces a thread hand-off roughly once per kilobyte
     */
    private static final int PIPE_SIZE = 1 << 20;
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

    /**
     * Reads the revision and the page table in a single pass over a (multi-part) dump. This
     * stream delivers the revision table, while the page table is written to {@code pageOutput},
     * which is closed at the end of the conversion. Call {@link #awaitCompletion()} before
     * reading what was written to {@code pageOutput}.
     *
     * @param inputStreams Ordered list of XML part streams (ascending page-range). Must not be
     *                     {@code null} or empty and must not contain {@code null} elements.
     * @param pageOutput   The sink for the page table. Must not be {@code null}.
     * @throws IOException Thrown if IO errors occurred while setting up the pipe.
     */
    public void initializeRevisionAndPage(List<InputStream> inputStreams, OutputStream pageOutput)
        throws IOException
    {
        final PipedOutputStream decodedStream = openPipe();
        xmlInputThread = new XMLDumpTableInputStreamThread(inputStreams, decodedStream,
                pageOutput);
        xmlInputThread.start();
    }

    /**
     * Reads the text table of a (multi-part) dump like
     * {@link #initialize(List, DumpTableEnum)} with {@link DumpTableEnum#TEXT} does, but delivers
     * only the rows of the revisions accepted by {@code wantedTextIds}. The text of all other
     * revisions is neither escaped nor encoded nor sent through the pipe.
     *
     * @param inputStreams  Ordered list of XML part streams (ascending page-range). Must not be
     *                      {@code null} or empty and must not contain {@code null} elements.
     * @param wantedTextIds Accepts the ids of the revisions whose text is delivered. Must not be
     *                      {@code null}.
     * @throws IOException Thrown if IO errors occurred while setting up the pipe.
     */
    public void initializeText(List<InputStream> inputStreams, IntPredicate wantedTextIds)
        throws IOException
    {
        final PipedOutputStream decodedStream = openPipe();
        xmlInputThread = new XMLDumpTableInputStreamThread(inputStreams, decodedStream,
                wantedTextIds);
        xmlInputThread.start();
    }

    /**
     * Waits until the conversion started by one of the {@code initialize} methods has finished
     * and closed all of its output streams.
     *
     * @throws IOException Thrown if the conversion failed or waiting for it was interrupted.
     */
    public void awaitCompletion() throws IOException
    {
        xmlInputThread.awaitCompletion();
    }

    private PipedOutputStream openPipe() throws IOException
    {
        /*
         * piped input stream, that allows to read from a <code>decodedStream</code>
         */
        PipedInputStream unbufferedResult = new PipedInputStream(PIPE_SIZE);
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
    public int read(byte[] b, int off, int len) throws IOException
    {
        return result.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException
    {
        return result.skip(n);
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
