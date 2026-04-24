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
package org.dkpro.jwpl.mwdumper.importer;

import java.io.IOException;
import java.util.Objects;

/**
 * A {@link DumpWriter} decorator that collapses the per-part lifecycle events of a
 * multi-file Wikipedia dump into a single logical document.
 * <p>
 * Wikimedia ships large dumps split across several files (e.g. {@code pages-articles1.xml-p1p10.bz2},
 * {@code pages-articles1.xml-p11p20.bz2}); each file is a self-contained XML document with its own
 * {@code <mediawiki>} root and {@code <siteinfo>} preamble. When those parts are parsed
 * sequentially against the same {@link DumpWriter}, the delegate would otherwise receive repeated
 * {@link #writeStartWiki()}/{@link #writeEndWiki()}/{@link #writeSiteinfo(Siteinfo)} events.
 * This wrapper:
 * <ul>
 *   <li>forwards {@link #writeStartWiki()} and {@link #writeSiteinfo(Siteinfo)} only on the first
 *       invocation;</li>
 *   <li>swallows {@link #writeEndWiki()} and {@link #close()} so that the caller controls the
 *       true end-of-document via {@link #finish()};</li>
 *   <li>passes {@link #writeStartPage(Page)}, {@link #writeEndPage()}, and
 *       {@link #writeRevision(Revision)} through verbatim.</li>
 * </ul>
 * Call {@link #finish()} exactly once after all parts have been parsed to emit the single
 * {@code writeEndWiki()} and close the delegate.
 * <p>
 * <b>Thread-safety:</b> not thread-safe. Mirroring the {@link DumpWriter} contract, instances
 * are intended for single-threaded use — events from one parser at a time. In the multi-part
 * pipeline that means all parts are parsed sequentially against the same wrapper.
 */
public final class MultiPartDumpWriter
    implements DumpWriter
{

    private final DumpWriter delegate;
    private boolean wikiStarted;
    private boolean siteinfoWritten;
    private boolean finished;

    public MultiPartDumpWriter(DumpWriter delegate)
    {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    public void writeStartWiki() throws IOException
    {
        if (!wikiStarted) {
            delegate.writeStartWiki();
            wikiStarted = true;
        }
    }

    @Override
    public void writeEndWiki()
    {
        // Deferred until finish() — each part emits </mediawiki> but we only want one
        // logical end-of-wiki event for the combined document.
    }

    @Override
    public void writeSiteinfo(Siteinfo info) throws IOException
    {
        if (!siteinfoWritten) {
            delegate.writeSiteinfo(info);
            siteinfoWritten = true;
        }
    }

    @Override
    public void writeStartPage(Page page) throws IOException
    {
        delegate.writeStartPage(page);
    }

    @Override
    public void writeEndPage() throws IOException
    {
        delegate.writeEndPage();
    }

    @Override
    public void writeRevision(Revision revision) throws IOException
    {
        delegate.writeRevision(revision);
    }

    @Override
    public void close()
    {
        // Deferred until finish() so per-part parses can reuse the same underlying writer.
    }

    /**
     * Emits the final {@code writeEndWiki()} to the delegate (only if at least one
     * {@code writeStartWiki()} was observed) and closes it. Idempotent.
     *
     * @throws IOException Thrown on delegate I/O errors during end-of-wiki or close.
     */
    public void finish() throws IOException
    {
        if (finished) {
            return;
        }
        finished = true;
        if (wikiStarted) {
            delegate.writeEndWiki();
        }
        delegate.close();
    }
}
