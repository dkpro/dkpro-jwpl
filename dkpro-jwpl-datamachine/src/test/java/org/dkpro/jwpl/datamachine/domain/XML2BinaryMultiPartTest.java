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
package org.dkpro.jwpl.datamachine.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.dkpro.jwpl.datamachine.dump.xml.XML2Binary;
import org.dkpro.jwpl.datamachine.factory.DefaultDataMachineEnvironmentFactory;
import org.dkpro.jwpl.wikimachine.factory.IEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Exercises the multi-part {@link XML2Binary#XML2Binary(java.util.List, DataMachineFiles)}
 * constructor end-to-end: two self-contained XML parts fed through the multi-part pipeline
 * must produce the same {@code page.bin} / {@code revision.bin} / {@code text.bin} bytes as a
 * single-document dump containing the same pages.
 */
class XML2BinaryMultiPartTest
{

    private static final IEnvironmentFactory FACTORY =
            DefaultDataMachineEnvironmentFactory.getInstance();

    private static final String PART_HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.10/\" "
            + "version=\"0.10\" xml:lang=\"en\">\n"
            + "  <siteinfo>\n"
            + "    <sitename>Test Wiki</sitename>\n"
            + "    <base>http://test.example/</base>\n"
            + "    <generator>MediaWiki-test</generator>\n"
            + "    <case>first-letter</case>\n"
            + "    <namespaces>\n"
            + "      <namespace key=\"0\" case=\"first-letter\"/>\n"
            + "      <namespace key=\"1\" case=\"first-letter\">Talk</namespace>\n"
            + "    </namespaces>\n"
            + "  </siteinfo>\n";
    private static final String PART_FOOTER = "</mediawiki>\n";

    private static String pageBlock(int pageId, int revisionId, String title)
    {
        return "  <page>\n"
                + "    <title>" + title + "</title>\n"
                + "    <id>" + pageId + "</id>\n"
                + "    <revision>\n"
                + "      <id>" + revisionId + "</id>\n"
                + "      <timestamp>2020-01-01T00:00:00Z</timestamp>\n"
                + "      <contributor>\n"
                + "        <username>Alice</username>\n"
                + "        <id>100</id>\n"
                + "      </contributor>\n"
                + "      <text>Body of " + title + "</text>\n"
                + "    </revision>\n"
                + "  </page>\n";
    }

    private static InputStream stream(String xml)
    {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    private static DataMachineFiles filesFor(Path dir)
    {
        DataMachineFiles files = new DataMachineFiles(FACTORY.getLogger());
        files.setDataDirectory(dir.toAbsolutePath().toString());
        return files;
    }

    @Test
    void multiPartProducesSameBinariesAsEquivalentSingleDocument(
            @TempDir Path singleDir, @TempDir Path multiDir) throws IOException
    {
        // Two pages.
        final String pageOne = pageBlock(1, 10, "Page One");
        final String pageTwo = pageBlock(2, 20, "Page Two");

        // Single-document reference: one <mediawiki> containing both pages.
        final String singleDoc = PART_HEADER + pageOne + pageTwo + PART_FOOTER;
        DataMachineFiles singleFiles = filesFor(singleDir);
        try (InputStream in = stream(singleDoc)) {
            new XML2Binary(in, singleFiles);
        }

        // Multi-part input: same two pages as two self-contained XML documents.
        final String partA = PART_HEADER + pageOne + PART_FOOTER;
        final String partB = PART_HEADER + pageTwo + PART_FOOTER;
        DataMachineFiles multiFiles = filesFor(multiDir);
        new XML2Binary(List.of(stream(partA), stream(partB)), multiFiles);

        assertBinariesEqual(singleFiles, multiFiles);
    }

    @Test
    void multiPartWithSingleElementListIsEquivalentToSingleStream(
            @TempDir Path singleDir, @TempDir Path multiDir) throws IOException
    {
        final String doc = PART_HEADER + pageBlock(7, 77, "Solo") + PART_FOOTER;

        DataMachineFiles singleFiles = filesFor(singleDir);
        try (InputStream in = stream(doc)) {
            new XML2Binary(in, singleFiles);
        }

        DataMachineFiles multiFiles = filesFor(multiDir);
        new XML2Binary(List.of(stream(doc)), multiFiles);

        assertBinariesEqual(singleFiles, multiFiles);
    }

    private static void assertBinariesEqual(DataMachineFiles a, DataMachineFiles b)
        throws IOException
    {
        final Path pageA = Path.of(a.getGeneratedPage());
        final Path pageB = Path.of(b.getGeneratedPage());
        assertTrue(Files.exists(pageA) && Files.size(pageA) > 0,
                "page.bin missing or empty (single)");
        assertTrue(Files.exists(pageB) && Files.size(pageB) > 0,
                "page.bin missing or empty (multi)");
        assertArrayEquals(Files.readAllBytes(pageA), Files.readAllBytes(pageB),
                "page.bin mismatch between multi-part and equivalent single document");

        final Path revA = Path.of(a.getGeneratedRevision());
        final Path revB = Path.of(b.getGeneratedRevision());
        assertArrayEquals(Files.readAllBytes(revA), Files.readAllBytes(revB),
                "revision.bin mismatch");

        final Path textA = Path.of(a.getGeneratedText());
        final Path textB = Path.of(b.getGeneratedText());
        assertArrayEquals(Files.readAllBytes(textA), Files.readAllBytes(textB),
                "text.bin mismatch");
    }
}
