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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.IntPredicate;

import org.dkpro.jwpl.mwdumper.importer.DumpWriter;
import org.dkpro.jwpl.mwdumper.importer.MultiWriter;
import org.dkpro.jwpl.mwdumper.importer.NamespaceFilter;
import org.dkpro.jwpl.wikimachine.dump.xml.AbstractXmlDumpReader;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableEnum;
import org.dkpro.jwpl.wikimachine.dump.xml.MultiPartXmlDumpReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread for converting of XML stream to SQL stream.
 */
class XMLDumpTableInputStreamThread
    extends Thread
{

    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Enable the main and category pages as well as discussions.
     */
    private static final String ENABLED_NAMESPACES = "NS_MAIN,NS_TALK,NS_CATEGORY";

    /**
     * Size of the write buffer in front of the pipe. The writers flush it in
     * {@code writeEndWiki()} and on {@code close()}, which also closes the pipe.
     */
    private static final int WRITE_BUFFER_SIZE = 1 << 16;

    /** Parses the bound input into SQL. May throw {@link IOException}. */
    @FunctionalInterface
    private interface ParseTask
    {
        void parse() throws IOException;
    }

    private final ParseTask parseTask;
    private final Runnable abortAction;

    /** completion flag for the conversion process */
    private boolean isComplete;

    /** the error that ended the conversion, if any */
    private volatile Throwable failure;

    /**
     * Drive the conversion of a single-file dump.
     *
     * @param iStream XML input stream.
     * @param oStream SQL output stream.
     * @param table   Kind of table output expected.
     */
    public XMLDumpTableInputStreamThread(InputStream iStream, OutputStream oStream,
            DumpTableEnum table)
    {
        super("xml2sql");
        final AbstractXmlDumpReader reader = readerFactoryFor(table)
                .create(iStream, createWriter(oStream, table));
        this.parseTask = reader::readDump;
        this.abortAction = reader::abort;
    }

    /**
     * Drive the conversion of a multi-part dump. Each element of {@code iStreams} is a
     * self-contained XML document; SAX events across parts are collapsed into a single
     * logical document by {@link MultiPartXmlDumpReader}.
     *
     * @param iStreams Ordered list of XML part input streams (ascending page-range).
     * @param oStream  SQL output stream.
     * @param table    Kind of table output expected.
     */
    public XMLDumpTableInputStreamThread(List<InputStream> iStreams, OutputStream oStream,
            DumpTableEnum table)
    {
        super("xml2sql");
        final DumpWriter writer = createWriter(oStream, table);
        final MultiPartXmlDumpReader.ReaderFactory factory = readerFactoryFor(table);
        this.parseTask = () -> MultiPartXmlDumpReader.readDumps(iStreams, writer, factory);
        // Abort is a best-effort signal to the single-file reader; the multi-part pipeline
        // has no equivalent per-part hook, so it is a no-op here.
        this.abortAction = () -> { /* no-op */ };
    }

    /**
     * Drive the conversion of the revision and the page table of a (multi-part) dump in a single
     * pass. Both writers share one {@link NamespaceFilter}, so they see the very same pages.
     *
     * @param iStreams       Ordered list of XML part input streams (ascending page-range).
     * @param revisionStream Output stream for the revision table.
     * @param pageStream     Output stream for the page table.
     */
    public XMLDumpTableInputStreamThread(List<InputStream> iStreams, OutputStream revisionStream,
            OutputStream pageStream)
    {
        super("xml2sql");
        // The page writer comes first, so that it is flushed and closed before the revision
        // stream is, i.e. before a reader of the revision table observes the end of its stream.
        final MultiWriter tee = new MultiWriter();
        tee.add(new PageWriter(new BufferedOutputStream(pageStream, WRITE_BUFFER_SIZE)));
        tee.add(new RevisionWriter(new BufferedOutputStream(revisionStream, WRITE_BUFFER_SIZE)));
        final DumpWriter writer = new NamespaceFilter(tee, ENABLED_NAMESPACES);
        // PageReader and RevisionReader handle the very same elements.
        this.parseTask = () -> MultiPartXmlDumpReader.readDumps(iStreams, writer,
                RevisionReader::new);
        this.abortAction = () -> { /* no-op, see the other multi-part constructor */ };
    }

    /**
     * Drive the conversion of the text table of a (multi-part) dump, restricted to the text of
     * the revisions accepted by {@code wantedTextIds}.
     *
     * @param iStreams      Ordered list of XML part input streams (ascending page-range).
     * @param oStream       Output stream for the text table.
     * @param wantedTextIds Accepts the ids of the revisions whose text is written.
     */
    public XMLDumpTableInputStreamThread(List<InputStream> iStreams, OutputStream oStream,
            IntPredicate wantedTextIds)
    {
        super("xml2sql");
        final DumpWriter writer = new NamespaceFilter(new TextWriter(
                new BufferedOutputStream(oStream, WRITE_BUFFER_SIZE), wantedTextIds),
                ENABLED_NAMESPACES);
        this.parseTask = () -> MultiPartXmlDumpReader.readDumps(iStreams, writer,
                TextReader::new);
        this.abortAction = () -> { /* no-op, see the other multi-part constructor */ };
    }

    private static DumpWriter createWriter(OutputStream oStream, DumpTableEnum table)
    {
        final OutputStream buffered = new BufferedOutputStream(oStream, WRITE_BUFFER_SIZE);
        switch (table) {
        case PAGE:
            return new NamespaceFilter(new PageWriter(buffered), ENABLED_NAMESPACES);
        case REVISION:
            return new NamespaceFilter(new RevisionWriter(buffered), ENABLED_NAMESPACES);
        case TEXT:
            return new NamespaceFilter(new TextWriter(buffered), ENABLED_NAMESPACES);
        default:
            throw new IllegalArgumentException("Unsupported table type: " + table);
        }
    }

    private static MultiPartXmlDumpReader.ReaderFactory readerFactoryFor(DumpTableEnum table)
    {
        switch (table) {
        case PAGE:
            return PageReader::new;
        case REVISION:
            return RevisionReader::new;
        case TEXT:
            return TextReader::new;
        default:
            throw new IllegalArgumentException("Unsupported table type: " + table);
        }
    }

    @Override
    public synchronized void run()
    {
        try {
            isComplete = false;
            parseTask.parse();
            isComplete = true;
        }
        catch (IOException e) {
            failure = e;
            // The rethrown exception dies with this thread - unless a caller uses
            // awaitCompletion(), the consumer only observes a broken pipe. Hence this log is
            // the only record of the original cause and must not be removed as 'redundant'.
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        catch (RuntimeException e) {
            failure = e;
            throw e;
        }
    }

    /**
     * Waits until the conversion has finished and all of its output streams are closed.
     *
     * @throws IOException Thrown if the conversion failed or waiting for it was interrupted.
     */
    public void awaitCompletion() throws IOException
    {
        try {
            join();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted while waiting for the XML conversion.");
        }
        if (failure != null) {
            throw new IOException("The XML conversion failed.", failure);
        }
    }

    /**
     * Abort a conversion.
     * <p>
     * Only supported in single-file mode. In multi-part mode the abort flag is recorded but
     * does not interrupt an in-flight SAX parse — callers must let the current part finish.
     */
    public synchronized void abort()
    {
        if (!isComplete) {
            abortAction.run();
            isComplete = true;
        }
    }
}
