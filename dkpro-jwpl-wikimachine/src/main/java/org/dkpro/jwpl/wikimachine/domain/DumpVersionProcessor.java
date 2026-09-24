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
package org.dkpro.jwpl.wikimachine.domain;

import java.io.IOException;

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;

/**
 * A processor of Wikipedias dump related revisions.
 *
 * @see IDumpVersion
 */
public class DumpVersionProcessor
{

    /** The default number of rows handed to the worker threads at once. */
    public static final int DEFAULT_BATCH_SIZE = 4096;

    private static ILogger logger;

    private Integer step2Log = 100000;
    private Integer step2GC = step2Log * 10;
    private Integer step2Flush = step2GC;
    private int versionThreads = 0;
    private int batchSize = DEFAULT_BATCH_SIZE;
    private IDumpVersion[] versions;

    /**
     * Instantiates a {@link DumpVersionProcessor} with a specified {@link ILogger}.
     *
     * @param initialLogger The {@link ILogger} to use initially.
     */
    public DumpVersionProcessor(ILogger initialLogger)
    {
        logger = initialLogger;
    }

    /**
     * Sets the collection of {@link IDumpVersion versions} to process (next).
     * @param versions A non-empty array of {@link IDumpVersion versions}.
     */
    public void setDumpVersions(IDumpVersion[] versions)
    {
        this.versions = versions;
    }

    /**
     * Configures the parameter {@code step2Log}.
     * @param step2Log A positive value for the number of steps to log.
     */
    public void setStep2Log(Integer step2Log)
    {
        this.step2Log = step2Log;
    }

    /**
     * Configures the parameter {@code step2GC}.
     * @param step2GC A positive value for the number of steps until garbage collection (GC) is triggered.
     */
    public void setStep2GC(Integer step2GC)
    {
        this.step2GC = step2GC;
    }

    /**
     * Configures the parameter {@code step2Flush}.
     * @param step2Flush A positive value for the number of steps until resources are flushed.
     */
    public void setStep2Flush(Integer step2Flush)
    {
        this.step2Flush = step2Flush;
    }

    /**
     * Configures the number of worker threads that process the rows of the {@code page},
     * {@code categorylinks} and {@code pagelinks} tables for several versions concurrently. The
     * number of threads never exceeds the number of versions.
     * <p>
     * Every row is handed to every {@link IDumpVersion version}, in the order the rows appear in
     * the dump. With more than one thread, the rows are copied into batches (see
     * {@link #setBatchSize(int)}) and the versions process each batch concurrently, while the next
     * batch is read from the dump. A single version is still called from one thread at a time and
     * sees the rows in dump order, so its output does not change. The parsers it receives on a
     * worker thread are read-only views of a copied row and must not be retained. The revision
     * and text tables are always processed on the thread reading the dump.
     *
     * @param versionThreads {@code 0} (the default) to use as many threads as there are
     *                       processors available, {@code 1} to process all versions on the thread
     *                       reading the dump, or any greater value to use at most that many.
     */
    public void setVersionThreads(int versionThreads)
    {
        if (versionThreads < 0) {
            throw new IllegalArgumentException(
                    "versionThreads must not be negative: " + versionThreads);
        }
        this.versionThreads = versionThreads;
    }

    /**
     * Configures the number of rows handed to the worker threads at once, see
     * {@link #setVersionThreads(int)}. At most two batches are held in memory at a time: one being
     * processed by the versions and one being filled from the dump.
     *
     * @param batchSize A positive number of rows; {@value #DEFAULT_BATCH_SIZE} by default.
     */
    public void setBatchSize(int batchSize)
    {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be positive: " + batchSize);
        }
        this.batchSize = batchSize;
    }

    /**
     * Processes a revision row.
     *
     * @param revisionParser A valid {@link RevisionParser} instance.
     *                       
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    public void processRevision(RevisionParser revisionParser) throws IOException
    {
        try (revisionParser) {
            for (IDumpVersion version : versions) {
                version.initRevisionParsing();
            }
            int counter = 0;
            while (revisionParser.next()) {
                for (IDumpVersion version : versions) {
                    version.processRevisionRow(revisionParser);
                }

                logAndClear(++counter, "Revision");
            }

            for (IDumpVersion version : versions) {
                version.exportAfterRevisionParsing();
                version.freeAfterRevisionParsing();
            }
        }
    }

    /**
     * Processes a page row.
     *
     * @param pageParser A valid {@link PageParser} instance.
     *                   
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    public void processPage(PageParser pageParser) throws IOException
    {
        try (pageParser; RowDispatcher<PageParser> dispatcher = RowDispatcher.create(
                versions, getWorkerThreads(), batchSize, RowBatch.Pages::new,
                IDumpVersion::processPageRow)) {
            for (IDumpVersion version : versions) {
                version.initPageParsing();
            }

            int counter = 0;
            while (pageParser.next()) {
                dispatcher.accept(pageParser);
                logAndClear(++counter, "Pages");
            }
            dispatcher.finish();

            for (IDumpVersion version : versions) {
                version.exportAfterPageParsing();
                version.freeAfterPageParsing();
            }
        }
    }

    /**
     * Processes a category link row.
     *
     * @param categorylinksParser A valid {@link CategorylinksParser} instance.
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    public void processCategorylinks(CategorylinksParser categorylinksParser) throws IOException
    {
        try (categorylinksParser;
                RowDispatcher<CategorylinksParser> dispatcher = RowDispatcher.create(versions,
                        getWorkerThreads(), batchSize, RowBatch.Categorylinks::new,
                        IDumpVersion::processCategoryLinksRow)) {
            for (IDumpVersion version : versions) {
                version.initCategoryLinksParsing();
            }

            int counter = 0;
            while (categorylinksParser.next()) {
                dispatcher.accept(categorylinksParser);
                logAndClear(++counter, "Categorylinks");
            }
            dispatcher.finish();
            categorylinksParser.checkPostConditions();

            for (IDumpVersion version : versions) {
                version.exportAfterCategoryLinksParsing();
                version.freeAfterCategoryLinksParsing();
            }
        }
    }

    /**
     * Processes a page link row.
     *
     * @param pagelinksParser A valid {@link PagelinksParser} instance.
     *
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    public void processPagelinks(PagelinksParser pagelinksParser) throws IOException
    {
        try (pagelinksParser; RowDispatcher<PagelinksParser> dispatcher = RowDispatcher.create(
                versions, getWorkerThreads(), batchSize, RowBatch.Pagelinks::new,
                IDumpVersion::processPageLinksRow)) {
            for (IDumpVersion version : versions) {
                version.initPageLinksParsing();
            }

            int counter = 0;
            while (pagelinksParser.next()) {
                dispatcher.accept(pagelinksParser);
                logAndClear(++counter, "Pagelinks");
            }
            dispatcher.finish();
            pagelinksParser.checkPostConditions();

            for (IDumpVersion version : versions) {
                version.exportAfterPageLinksParsing();
                version.freeAfterPageLinksParsing();
            }
        }
    }

    /**
     * Processes a text row.
     *
     * @param textParser A valid {@link TextParser} instance.
     *
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    public void processText(TextParser textParser) throws IOException
    {
        try (textParser) {
            for (IDumpVersion version : versions) {
                version.initTextParsing();
            }

            int counter = 0;
            while (textParser.next()) {
                for (IDumpVersion version : versions) {
                    version.processTextRow(textParser);
                }
                if (step2Flush != 0 && counter % step2Flush == 0) {
                    for (IDumpVersion version : versions) {
                        version.flushByTextParsing();
                    }
                }
                logAndClear(++counter, "Text");

            }

            for (IDumpVersion version : versions) {
                version.exportAfterTextParsing();
                version.freeAfterTextParsing();
            }
        }
    }

    /**
     * Exports the (current) {@link MetaData} for all versions.
     *
     * @throws IOException Thrown if IO errors occurred during export.
     */
    public void writeMetaData() throws IOException
    {
        for (IDumpVersion version : versions) {
            version.writeMetaData();
        }
    }

    /**
     * @return The number of worker threads to process the versions with; {@code 1} means that
     *         the versions are processed on the thread reading the dump.
     */
    int getWorkerThreads()
    {
        final int limit = versionThreads > 0 ? versionThreads
                : Runtime.getRuntime().availableProcessors();
        return Math.max(1, Math.min(limit, versions.length));
    }

    private void logAndClear(int counter, String event)
    {
        if (step2Log != 0 && counter % step2Log == 0) {
            String message = event + " " + counter;
            logger.log(message);
        }
    }

}
