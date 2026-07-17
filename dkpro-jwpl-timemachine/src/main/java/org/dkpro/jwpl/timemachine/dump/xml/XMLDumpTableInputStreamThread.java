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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;

import org.dkpro.jwpl.mwdumper.importer.DumpWriter;
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

    private static DumpWriter createWriter(OutputStream oStream, DumpTableEnum table)
    {
        switch (table) {
        case PAGE:
            return new NamespaceFilter(new PageWriter(oStream), ENABLED_NAMESPACES);
        case REVISION:
            return new NamespaceFilter(new RevisionWriter(oStream), ENABLED_NAMESPACES);
        case TEXT:
            return new NamespaceFilter(new TextWriter(oStream), ENABLED_NAMESPACES);
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
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
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
