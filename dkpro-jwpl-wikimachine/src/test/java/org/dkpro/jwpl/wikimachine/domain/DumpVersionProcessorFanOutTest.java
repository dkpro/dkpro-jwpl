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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Checks that handing the rows of the page and link tables to several versions on worker
 * threads delivers exactly what the serial processing delivers: every row, in dump order, to
 * every version, and nothing before the initialisation or after the export of a pass.
 */
class DumpVersionProcessorFanOutTest
{

    private static final ILogger LOGGER = message -> {
        // quiet
    };

    private static final int ROWS = 1000;
    private static final int VERSIONS = 5;

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 7, 4096 })
    void workerThreadsDeliverTheSameRowsAsTheSerialProcessing(int batchSize) throws Exception
    {
        final List<RecordingVersion> serial = run(1, batchSize);
        final List<RecordingVersion> parallel = run(3, batchSize);

        final List<String> expected = serial.get(0).events;
        // init + rows + export + free for each of the three passes
        assertEquals(3 * (ROWS + 3), expected.size());
        for (int i = 0; i < VERSIONS; i++) {
            assertEquals(expected, serial.get(i).events);
            assertEquals(expected, parallel.get(i).events, "version " + i);
            assertFalse(parallel.get(i).overlapped.get(), "version " + i);
        }
    }

    @Test
    void rowsAreProcessedOnWorkerThreads() throws Exception
    {
        final Set<String> threads = new HashSet<>();
        for (RecordingVersion version : run(3, 7)) {
            threads.addAll(version.rowThreads);
        }
        assertFalse(threads.contains(Thread.currentThread().getName()), threads.toString());
        assertTrue(threads.stream().allMatch(name -> name.startsWith("dump-version-")),
                threads.toString());
    }

    @Test
    void serialProcessingStaysOnTheCallingThread() throws Exception
    {
        for (RecordingVersion version : run(1, 7)) {
            assertEquals(Set.of(Thread.currentThread().getName()), version.rowThreads);
        }
    }

    @Test
    void anIOExceptionOfAVersionIsPropagated()
    {
        final IOException failure = new IOException("broken version");
        final RecordingVersion[] versions = versions();
        versions[2].failure = failure;
        versions[2].failAtRow = 500;
        final DumpVersionProcessor processor = processor(versions, 3, 7);

        final IOException thrown = assertThrows(IOException.class,
                () -> processor.processCategorylinks(categorylinksParser()));
        assertSame(failure, thrown);
        assertFalse(versions[2].events.contains("categorylinks:export"));
    }

    @Test
    void aRuntimeExceptionOfAVersionIsPropagated()
    {
        final IllegalStateException failure = new IllegalStateException("broken version");
        final RecordingVersion[] versions = versions();
        versions[4].failure = failure;
        versions[4].failAtRow = ROWS - 1;
        final DumpVersionProcessor processor = processor(versions, 2, 16);

        final IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> processor.processPage(pageParser()));
        assertSame(failure, thrown);
    }

    @Test
    void theNumberOfWorkerThreadsIsBoundByTheNumberOfVersions()
    {
        final int processors = Runtime.getRuntime().availableProcessors();
        assertEquals(1, processor(new RecordingVersion[] { new RecordingVersion() }, 0, 1)
                .getWorkerThreads());
        assertEquals(Math.min(processors, VERSIONS), processor(versions(), 0, 1)
                .getWorkerThreads());
        assertEquals(2, processor(versions(), 2, 1).getWorkerThreads());
        assertEquals(VERSIONS, processor(versions(), 64, 1).getWorkerThreads());
        assertEquals(1, processor(versions(), 1, 1).getWorkerThreads());
        assertThrows(IllegalArgumentException.class,
                () -> new DumpVersionProcessor(LOGGER).setVersionThreads(-1));
    }

    private static List<RecordingVersion> run(int threads, int batchSize) throws IOException
    {
        final RecordingVersion[] versions = versions();
        final DumpVersionProcessor processor = processor(versions, threads, batchSize);
        processor.processPage(pageParser());
        processor.processCategorylinks(categorylinksParser());
        processor.processPagelinks(pagelinksParser());
        return List.of(versions);
    }

    private static RecordingVersion[] versions()
    {
        final RecordingVersion[] versions = new RecordingVersion[VERSIONS];
        for (int i = 0; i < versions.length; i++) {
            versions[i] = new RecordingVersion();
        }
        return versions;
    }

    private static DumpVersionProcessor processor(IDumpVersion[] versions, int threads,
            int batchSize)
    {
        final DumpVersionProcessor processor = new DumpVersionProcessor(LOGGER);
        processor.setDumpVersions(versions);
        processor.setVersionThreads(threads);
        processor.setBatchSize(batchSize);
        return processor;
    }

    private static PageParser pageParser() throws IOException
    {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            for (int i = 0; i < ROWS; i++) {
                final byte[] title = ("Title_" + i + "_ä").getBytes(StandardCharsets.UTF_8);
                out.writeInt(i + 1);
                out.writeInt(i % 16);
                out.writeInt(title.length);
                out.write(title);
                out.writeBoolean(i % 3 == 0);
            }
        }
        final PageParser parser = new PageParser();
        parser.setInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        return parser;
    }

    private static CategorylinksParser categorylinksParser() throws IOException
    {
        final StringBuilder sql = new StringBuilder("""
                CREATE TABLE `categorylinks` (
                  `cl_from` int(8) unsigned NOT NULL DEFAULT 0,
                  `cl_to` varbinary(255) NOT NULL DEFAULT '',
                  `cl_type` enum('page','subcat','file') NOT NULL DEFAULT 'page'
                ) ENGINE=InnoDB;
                INSERT INTO `categorylinks` VALUES """);
        final String[] types = { "page", "subcat", "file" };
        for (int i = 0; i < ROWS; i++) {
            sql.append(i == 0 ? "" : ",").append('(').append(i + 1).append(",'Category_")
                    .append(i % 37).append("','").append(types[i % 3]).append("')");
        }
        sql.append(";\n");
        return new CategorylinksParser(stream(sql));
    }

    private static PagelinksParser pagelinksParser() throws IOException
    {
        final StringBuilder sql = new StringBuilder("""
                CREATE TABLE `pagelinks` (
                  `pl_from` int(8) unsigned NOT NULL DEFAULT 0,
                  `pl_namespace` int(11) NOT NULL DEFAULT 0,
                  `pl_to` varbinary(255) NOT NULL DEFAULT ''
                ) ENGINE=InnoDB;
                INSERT INTO `pagelinks` VALUES """);
        for (int i = 0; i < ROWS; i++) {
            sql.append(i == 0 ? "" : ",").append('(').append(i + 1).append(',').append(i % 4)
                    .append(",'Target_").append(i).append("')");
        }
        sql.append(";\n");
        return new PagelinksParser(stream(sql));
    }

    private static ByteArrayInputStream stream(CharSequence sql)
    {
        return new ByteArrayInputStream(sql.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Records the calls it receives and whether two of them ever overlapped.
     */
    private static final class RecordingVersion
        implements IDumpVersion
    {
        private final List<String> events = new ArrayList<>();
        private final Set<String> rowThreads = Collections.synchronizedSet(new HashSet<>());
        private final AtomicBoolean busy = new AtomicBoolean();
        private final AtomicBoolean overlapped = new AtomicBoolean();

        private Exception failure;
        private int failAtRow = -1;
        private int rows;

        private void row(String event) throws IOException
        {
            if (!busy.compareAndSet(false, true)) {
                overlapped.set(true);
            }
            try {
                rowThreads.add(Thread.currentThread().getName());
                if (rows++ == failAtRow) {
                    if (failure instanceof IOException io) {
                        throw io;
                    }
                    throw (RuntimeException) failure;
                }
                events.add(event);
            }
            finally {
                busy.set(false);
            }
        }

        private void uncheckedRow(String event)
        {
            try {
                row(event);
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void initPageParsing()
        {
            events.add("page:init");
        }

        @Override
        public void processPageRow(PageParser parser)
        {
            uncheckedRow("page:" + parser.getPageId() + ":" + parser.getPageNamespace() + ":"
                    + parser.getPageTitle() + ":" + parser.getPageIsRedirect());
        }

        @Override
        public void exportAfterPageParsing()
        {
            events.add("page:export");
        }

        @Override
        public void freeAfterPageParsing()
        {
            events.add("page:free");
        }

        @Override
        public void initCategoryLinksParsing()
        {
            events.add("categorylinks:init");
        }

        @Override
        public void processCategoryLinksRow(CategorylinksParser parser) throws IOException
        {
            row("categorylinks:" + parser.getClFrom() + ":" + parser.getClTo() + ":"
                    + parser.getClTargetId() + ":" + parser.getClType());
        }

        @Override
        public void exportAfterCategoryLinksParsing()
        {
            events.add("categorylinks:export");
        }

        @Override
        public void freeAfterCategoryLinksParsing()
        {
            events.add("categorylinks:free");
        }

        @Override
        public void initPageLinksParsing()
        {
            events.add("pagelinks:init");
        }

        @Override
        public void processPageLinksRow(PagelinksParser parser)
        {
            uncheckedRow("pagelinks:" + parser.getPlFrom() + ":" + parser.getPlNamespace() + ":"
                    + parser.getPlTo() + ":" + parser.getPlTargetId());
        }

        @Override
        public void exportAfterPageLinksParsing()
        {
            events.add("pagelinks:export");
        }

        @Override
        public void freeAfterPageLinksParsing()
        {
            events.add("pagelinks:free");
        }

        @Override
        public void setLogger(ILogger logger)
        {
        }

        @Override
        public void setCategoryRedirectsSkip(boolean skipCategory)
        {
        }

        @Override
        public void setPageRedirectsSkip(boolean skipPage)
        {
        }

        @Override
        public void initialize(Timestamp timestamp)
        {
        }

        @Override
        public void setMetaData(MetaData commonMetaData)
        {
        }

        @Override
        public void setFiles(Files versionFiles)
        {
        }

        @Override
        public void initRevisionParsing()
        {
        }

        @Override
        public void processRevisionRow(RevisionParser revisionParser)
        {
        }

        @Override
        public void exportAfterRevisionParsing()
        {
        }

        @Override
        public void freeAfterRevisionParsing()
        {
        }

        @Override
        public void initTextParsing()
        {
        }

        @Override
        public void processTextRow(TextParser textParser)
        {
        }

        @Override
        public void flushByTextParsing()
        {
        }

        @Override
        public void exportAfterTextParsing()
        {
        }

        @Override
        public void freeAfterTextParsing()
        {
        }

        @Override
        public void writeMetaData()
        {
        }
    }
}
