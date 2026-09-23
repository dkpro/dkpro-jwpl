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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableEnum;
import org.junit.jupiter.api.Test;

/**
 * Asserts that {@link XMLDumpTableInputStream#initializeRevisionAndPage(List, java.io.OutputStream)}
 * yields the very same revision and page tables as two separate passes over the dump (issue #543).
 */
class XMLDumpTableInputStreamRevisionAndPageTest
{

    private static final String HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.10/\" "
            + "version=\"0.10\" xml:lang=\"en\">\n"
            + "  <siteinfo>\n"
            + "    <sitename>Test Wiki</sitename>\n"
            + "    <namespaces>\n"
            + "      <namespace key=\"0\" case=\"first-letter\"/>\n"
            + "      <namespace key=\"2\" case=\"first-letter\">User</namespace>\n"
            + "      <namespace key=\"14\" case=\"first-letter\">Category</namespace>\n"
            + "    </namespaces>\n"
            + "  </siteinfo>\n";
    private static final String FOOTER = "</mediawiki>\n";

    /** The last revision is a redirect, the first one is not. */
    private static final String BECOMES_REDIRECT = page(1, "Beta",
            revision(10, "2020-01-01T00:00:00Z", "Beta is an article.")
            + revision(11, "2021-01-01T00:00:00Z", "#REDIRECT [[Alpha]]"));
    /** The first revision is a redirect, the last one is not. */
    private static final String STOPS_BEING_REDIRECT = page(2, "Category:Gamma",
            revision(20, "2020-01-01T00:00:00Z", "#REDIRECT [[Alpha]]")
            + revision(21, "2021-01-01T00:00:00Z", "Gamma is a category."));
    /** Dropped by the namespace filter. */
    private static final String USER_PAGE = page(3, "User:Bob",
            revision(30, "2020-01-01T00:00:00Z", "Bob."));

    private static String page(int id, String title, String revisions)
    {
        return "  <page>\n    <title>" + title + "</title>\n    <id>" + id + "</id>\n"
                + revisions + "  </page>\n";
    }

    private static String revision(int id, String timestamp, String text)
    {
        return "    <revision>\n      <id>" + id + "</id>\n      <timestamp>" + timestamp
                + "</timestamp>\n      <contributor><username>Alice</username><id>1</id>"
                + "</contributor>\n      <text>" + text + "</text>\n    </revision>\n";
    }

    private static InputStream stream(String xml)
    {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] drain(InputStream in) throws IOException
    {
        try (in) {
            return in.readAllBytes();
        }
    }

    private static byte[] separatePass(String xml, DumpTableEnum table) throws IOException
    {
        final XMLDumpTableInputStream sut = new XMLDumpTableInputStream();
        sut.initialize(List.of(stream(xml)), table);
        return drain(sut);
    }

    /**
     * Only compares the {@code (pageId, revisionId)} part of each 16-byte revision record, see
     * {@code XMLDumpTableInputStreamMultiPartTest} for the non-deterministic milliseconds.
     */
    private static void assertSameRevisionIds(byte[] expected, byte[] actual)
    {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i += 16) {
            assertArrayEquals(Arrays.copyOfRange(expected, i, i + 8),
                    Arrays.copyOfRange(actual, i, i + 8), "record offset " + i);
        }
    }

    @Test
    void singlePassMatchesSeparatePasses() throws IOException
    {
        final String partOne = BECOMES_REDIRECT + USER_PAGE;
        final String partTwo = STOPS_BEING_REDIRECT;
        final String whole = HEADER + partOne + partTwo + FOOTER;

        final ByteArrayOutputStream pages = new ByteArrayOutputStream();
        final XMLDumpTableInputStream sut = new XMLDumpTableInputStream();
        sut.initializeRevisionAndPage(List.of(stream(HEADER + partOne + FOOTER),
                stream(HEADER + partTwo + FOOTER)), pages);
        final byte[] revisions = drain(sut);
        sut.awaitCompletion();

        final byte[] expectedPages = separatePass(whole, DumpTableEnum.PAGE);
        assertTrue(expectedPages.length > 0);
        assertArrayEquals(expectedPages, pages.toByteArray());
        final byte[] expectedRevisions = separatePass(whole, DumpTableEnum.REVISION);
        assertEquals(4 * 16, expectedRevisions.length);
        assertSameRevisionIds(expectedRevisions, revisions);
    }

    @Test
    void pageOutputIsClosedWhenTheConversionIsComplete() throws IOException
    {
        final boolean[] closed = new boolean[1];
        final ByteArrayOutputStream pages = new ByteArrayOutputStream()
        {
            @Override
            public void close()
            {
                closed[0] = true;
            }
        };
        final XMLDumpTableInputStream sut = new XMLDumpTableInputStream();
        sut.initializeRevisionAndPage(List.of(stream(HEADER + BECOMES_REDIRECT + FOOTER)), pages);
        drain(sut);
        sut.awaitCompletion();

        assertTrue(closed[0]);
        assertTrue(pages.size() > 0);
    }

    @Test
    void awaitCompletionReportsAFailedConversion() throws IOException
    {
        final ByteArrayOutputStream pages = new ByteArrayOutputStream();
        final XMLDumpTableInputStream sut = new XMLDumpTableInputStream();
        sut.initializeRevisionAndPage(List.of(stream(HEADER + "  <page><title>Broken")), pages);
        drain(sut);

        assertThrows(IOException.class, sut::awaitCompletion);
    }
}
