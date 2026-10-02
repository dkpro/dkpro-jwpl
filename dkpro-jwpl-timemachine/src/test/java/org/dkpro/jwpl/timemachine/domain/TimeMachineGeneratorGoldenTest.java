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
package org.dkpro.jwpl.timemachine.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.dkpro.jwpl.timemachine.factory.DefaultTimeMachineEnvironmentFactory;
import org.dkpro.jwpl.wikimachine.debug.Slf4JLogger;
import org.dkpro.jwpl.wikimachine.decompression.IDecompressor;
import org.dkpro.jwpl.wikimachine.domain.Configuration;
import org.dkpro.jwpl.wikimachine.domain.DumpVersionProcessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Runs the complete TimeMachine on a tiny hand-written dump set and compares every generated
 * file byte for byte with a golden copy that was produced before the revision and page tables
 * were read in a single pass over the meta-history dump (issue #543).
 * <p>
 * The fixture covers the namespace filter (a {@code User:} page), pages created after the first
 * snapshot, and the redirect flag of the {@code page} table, which is derived from the text of a
 * page's <i>last</i> revision: {@code Beta} turns into a redirect, {@code Gamma} stops being one.
 */
class TimeMachineGeneratorGoldenTest
{

    private static final String INPUT = "/golden/input/";
    private static final String EXPECTED = "/golden/expected/";

    private static TimeZone defaultTimeZone;

    /**
     * Snapshot timestamps are computed and formatted in the default time zone, which would make
     * the names of the snapshot directories depend on the machine running the test.
     */
    @BeforeAll
    static void useUtc()
    {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterAll
    static void restoreTimeZone()
    {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    void singlePartDumpMatchesGoldenOutput(@TempDir Path output) throws Exception
    {
        run(List.of("golden-pages-meta-history.xml"), output);
        assertMatchesGolden(output);
    }

    @Test
    void multiPartDumpMatchesGoldenOutput(@TempDir Path output) throws Exception
    {
        run(List.of("golden-pages-meta-history1.xml", "golden-pages-meta-history2.xml"),
                output);
        assertMatchesGolden(output);
    }

    /**
     * The snapshots are processed on worker threads, in batches of rows (issue #613). Neither the
     * number of threads nor the size of the batches may change the output.
     */
    @ParameterizedTest(name = "{0} version threads, batches of {1} rows")
    @CsvSource({ "1, 4096", "2, 1", "3, 1", "3, 2", "3, 4096" })
    void outputDoesNotDependOnTheVersionThreads(int threads, int batchSize,
            @TempDir Path output)
        throws Exception
    {
        final DumpVersionProcessor processor = new DumpVersionProcessor(new Slf4JLogger());
        processor.setVersionThreads(threads);
        processor.setBatchSize(batchSize);
        run(List.of("golden-pages-meta-history1.xml", "golden-pages-meta-history2.xml"),
                output, processor);
        assertMatchesGolden(output);
    }

    @Test
    void metaHistoryIsDecompressedTwicePerPart(@TempDir Path output) throws Exception
    {
        final List<String> parts = List.of("golden-pages-meta-history1.xml",
                "golden-pages-meta-history2.xml");
        final AtomicInteger opened = run(parts, output);
        // One pass for the revision and page tables, one for the text table.
        assertEquals(2 * parts.size(), opened.get());
    }

    /**
     * @return The number of streams opened on a meta-history part.
     */
    private static AtomicInteger run(List<String> metaHistoryParts, Path output) throws Exception
    {
        return run(metaHistoryParts, output, null);
    }

    /**
     * @param processor The processor to use, or {@code null} for the default one.
     * @return The number of streams opened on a meta-history part.
     */
    private static AtomicInteger run(List<String> metaHistoryParts, Path output,
            DumpVersionProcessor processor)
        throws Exception
    {
        final Slf4JLogger logger = new Slf4JLogger();

        final Configuration config = new Configuration(logger);
        config.setFromTimestamp(Timestamp.valueOf("2020-06-01 00:00:00"));
        config.setToTimestamp(Timestamp.valueOf("2022-01-01 00:00:00"));
        config.setEach(200);
        config.setLanguage("english");
        config.setMainCategory("Letters");
        config.setDisambiguationCategory("n/a");

        final List<String> metaHistoryFiles = new ArrayList<>();
        for (String part : metaHistoryParts) {
            metaHistoryFiles.add(input(part));
        }
        final TimeMachineFiles files = new TimeMachineFiles(logger);
        files.setMetaHistoryFiles(metaHistoryFiles);
        files.setCategoryLinksFile(input("golden-categorylinks.sql"));
        files.setPageLinksFile(input("golden-pagelinks.sql"));
        files.setLinkTargetFile(input("golden-linktarget.sql"));
        files.setOutputDirectory(output.toString());

        final CountingEnvironmentFactory factory = new CountingEnvironmentFactory(
                metaHistoryFiles, processor);
        final TimeMachineGenerator generator = new TimeMachineGenerator(factory);
        generator.setConfiguration(config);
        generator.setFiles(files);
        generator.start();
        return factory.metaHistoryStreams;
    }

    private static void assertMatchesGolden(Path output) throws Exception
    {
        final Path expected = resource(EXPECTED);
        final List<Path> expectedFiles = relativeFiles(expected);
        assertEquals(expectedFiles, relativeFiles(output));
        for (Path file : expectedFiles) {
            assertArrayEquals(Files.readAllBytes(expected.resolve(file)),
                    Files.readAllBytes(output.resolve(file)), file.toString());
        }
    }

    private static List<Path> relativeFiles(Path root) throws IOException
    {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile).map(root::relativize).sorted().toList();
        }
    }

    private static String input(String name) throws URISyntaxException
    {
        return resource(INPUT + name).toString();
    }

    private static Path resource(String name) throws URISyntaxException
    {
        return Path.of(TimeMachineGeneratorGoldenTest.class.getResource(name).toURI());
    }

    /**
     * Counts how often a meta-history part is opened, delegating to the default decompressor.
     */
    private static final class CountingEnvironmentFactory
        extends DefaultTimeMachineEnvironmentFactory
    {
        private final AtomicInteger metaHistoryStreams = new AtomicInteger();
        private final List<String> metaHistoryFiles;
        private final DumpVersionProcessor processor;

        CountingEnvironmentFactory(List<String> metaHistoryFiles, DumpVersionProcessor processor)
        {
            this.metaHistoryFiles = metaHistoryFiles;
            this.processor = processor;
        }

        @Override
        public DumpVersionProcessor getDumpVersionProcessor()
        {
            return processor != null ? processor : super.getDumpVersionProcessor();
        }

        @Override
        public IDecompressor getDecompressor()
        {
            final IDecompressor delegate = super.getDecompressor();
            return new IDecompressor()
            {
                @Override
                public InputStream getInputStream(String resource) throws IOException
                {
                    if (metaHistoryFiles.contains(resource)) {
                        metaHistoryStreams.incrementAndGet();
                    }
                    return delegate.getInputStream(resource);
                }

                @Override
                public InputStream getInputStream(Path resource) throws IOException
                {
                    return getInputStream(resource.toString());
                }

                @Override
                public InputStream getInputStreamSequence(List<Path> resources)
                    throws IOException
                {
                    return delegate.getInputStreamSequence(resources);
                }
            };
        }
    }
}
