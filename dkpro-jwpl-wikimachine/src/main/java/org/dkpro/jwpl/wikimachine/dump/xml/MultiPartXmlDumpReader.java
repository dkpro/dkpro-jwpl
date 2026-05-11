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
package org.dkpro.jwpl.wikimachine.dump.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import org.dkpro.jwpl.mwdumper.importer.DumpWriter;
import org.dkpro.jwpl.mwdumper.importer.MultiPartDumpWriter;

/**
 * Parses a multi-file Wikipedia dump as a single logical document.
 * <p>
 * Each Wikimedia dump part (e.g. {@code pages-articles1.xml-p1p10.bz2}) is a standalone
 * XML document with its own {@code <mediawiki>} root and {@code <siteinfo>} preamble, so
 * the decompressed byte concatenation is not well-formed XML and cannot be fed to a
 * single {@link javax.xml.parsers.SAXParser#parse} call. This helper instead parses each
 * part with a fresh {@link AbstractXmlDumpReader} while routing all events to one
 * {@link MultiPartDumpWriter} — duplicate {@code writeStartWiki}/{@code writeSiteinfo}/
 * {@code writeEndWiki} events across parts are collapsed, and the underlying delegate
 * writer only sees one logical document.
 */
public final class MultiPartXmlDumpReader
{

    /**
     * Factory that produces a fresh {@link AbstractXmlDumpReader} (typically a concrete
     * subclass such as {@code SimpleXmlDumpReader} or a {@code timemachine} reader) for
     * a given part's {@link InputStream} and the shared {@link DumpWriter}. Callers
     * normally pass a constructor reference such as {@code SimpleXmlDumpReader::new}.
     */
    @FunctionalInterface
    public interface ReaderFactory
    {
        /**
         * @param in     The part's input stream.
         * @param writer The shared dump writer (already wrapped in a
         *               {@link MultiPartDumpWriter} by {@link #readDumps}).
         * @return A fresh reader bound to {@code in} and {@code writer}.
         */
        AbstractXmlDumpReader create(InputStream in, DumpWriter writer);
    }

    private MultiPartXmlDumpReader()
    {
        // static-only
    }

    /**
     * Parses every part in {@code parts} against the same {@code writer}. Events from
     * the individual parts are funnelled through a {@link MultiPartDumpWriter} so the
     * delegate observes the combined stream as a single {@code <mediawiki>} document.
     * <p>
     * This method takes ownership of the supplied streams: every element of {@code parts}
     * is {@link InputStream#close() closed} before {@code readDumps} returns (on both the
     * success and failure paths), and the delegate writer is closed via the wrapper's
     * {@code finish()} exactly once at the end. Exceptions raised by stream close or by
     * the wrapper's final flush are attached as suppressed to any primary error from the
     * parse.
     *
     * @param parts   Ordered list of decompressed XML {@link InputStream streams}. Must
     *                not be {@code null}, must not be empty, and must not contain
     *                {@code null} elements.
     * @param writer  The underlying {@link DumpWriter} to flush events into.
     * @param factory Instantiates a fresh reader per part — typically a method reference
     *                such as {@code SimpleXmlDumpReader::new}.
     * @throws IOException              Thrown on I/O or SAX errors encountered while parsing
     *                                  any part, or while closing the supplied streams.
     * @throws IllegalArgumentException If {@code parts} is null, empty, or contains a null
     *                                  element.
     */
    public static void readDumps(List<InputStream> parts, DumpWriter writer, ReaderFactory factory)
        throws IOException
    {
        if (parts == null || parts.isEmpty()) {
            throw new IllegalArgumentException("'parts' must not be null or empty.");
        }
        Objects.requireNonNull(writer, "'writer' must not be null.");
        Objects.requireNonNull(factory, "'factory' must not be null.");
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i) == null) {
                throw new IllegalArgumentException("'parts[" + i + "]' is null.");
            }
        }

        final MultiPartDumpWriter wrapper = new MultiPartDumpWriter(writer);
        Throwable primary = null;
        try {
            for (InputStream part : parts) {
                factory.create(part, wrapper).doParse();
            }
        }
        catch (IOException | RuntimeException e) {
            primary = e;
        }
        // We took ownership of every stream — close them all, regardless of outcome.
        for (InputStream part : parts) {
            primary = closeAndChain(part, primary);
        }
        // finish() is idempotent; emits a single writeEndWiki (if any startWiki was seen)
        // and closes the delegate writer.
        try {
            wrapper.finish();
        }
        catch (IOException e) {
            primary = chain(primary, e);
        }

        if (primary instanceof IOException) {
            throw (IOException) primary;
        }
        if (primary instanceof RuntimeException) {
            throw (RuntimeException) primary;
        }
    }

    private static Throwable closeAndChain(InputStream stream, Throwable primary)
    {
        try {
            stream.close();
            return primary;
        }
        catch (IOException e) {
            return chain(primary, e);
        }
    }

    private static Throwable chain(Throwable primary, Throwable next)
    {
        if (primary == null) {
            return next;
        }
        primary.addSuppressed(next);
        return primary;
    }
}
