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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableEnum;
import org.junit.jupiter.api.Test;

/**
 * Feeds two self-contained XML parts through the multi-part overload of
 * {@link XMLDumpTableInputStream#initialize(List, DumpTableEnum)} and asserts the piped SQL
 * output bytes equal the output of a single-document dump with the same pages.
 */
class XMLDumpTableInputStreamMultiPartTest
{

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

    private static byte[] runSingle(String xml, DumpTableEnum table) throws IOException
    {
        XMLDumpTableInputStream sut = new XMLDumpTableInputStream();
        sut.initialize(stream(xml), table);
        return drain(sut);
    }

    private static byte[] runMulti(List<InputStream> parts, DumpTableEnum table) throws IOException
    {
        XMLDumpTableInputStream sut = new XMLDumpTableInputStream();
        sut.initialize(parts, table);
        return drain(sut);
    }

    private static byte[] drain(XMLDumpTableInputStream sut) throws IOException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buf = new byte[4096];
        int n;
        while ((n = sut.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, n);
        }
        sut.close();
        return out.toByteArray();
    }

    private static void assertMultiEqualsSingle(DumpTableEnum table) throws IOException
    {
        final String pageOne = pageBlock(1, 10, "Page One");
        final String pageTwo = pageBlock(2, 20, "Page Two");

        final byte[] single = runSingle(
                PART_HEADER + pageOne + pageTwo + PART_FOOTER, table);
        final byte[] multi = runMulti(
                List.of(stream(PART_HEADER + pageOne + PART_FOOTER),
                        stream(PART_HEADER + pageTwo + PART_FOOTER)),
                table);

        assertTrue(single.length > 0, "empty single-doc output for " + table);
        assertArrayEquals(single, multi,
                "multi-part output diverges from single-document output for " + table);
    }

    @Test
    void multiPartPageTableMatchesSingleDocument() throws IOException
    {
        assertMultiEqualsSingle(DumpTableEnum.PAGE);
    }

    @Test
    void multiPartRevisionTableMatchesSingleDocumentOnIdFields() throws IOException
    {
        // RevisionWriter also persists the revision timestamp's millisecond component, which
        // AbstractXmlDumpReader derives from a GregorianCalendar whose millis field is seeded
        // with System.currentTimeMillis() at construction time — a pre-existing non-determinism
        // that is orthogonal to multi-part handling. Compare the deterministic (pageId, revId)
        // part of each 16-byte record instead.
        final String pageOne = pageBlock(1, 10, "Page One");
        final String pageTwo = pageBlock(2, 20, "Page Two");

        final byte[] single = runSingle(
                PART_HEADER + pageOne + pageTwo + PART_FOOTER, DumpTableEnum.REVISION);
        final byte[] multi = runMulti(
                List.of(stream(PART_HEADER + pageOne + PART_FOOTER),
                        stream(PART_HEADER + pageTwo + PART_FOOTER)),
                DumpTableEnum.REVISION);

        assertTrue(single.length > 0 && single.length == multi.length,
                "revision binaries differ in length");
        // Each record: int pageId (4) + int revId (4) + long millis (8) = 16 bytes.
        for (int i = 0; i < single.length; i += 16) {
            assertArrayEquals(
                    java.util.Arrays.copyOfRange(single, i, i + 8),
                    java.util.Arrays.copyOfRange(multi, i, i + 8),
                    "pageId/revId diverge at record offset " + i);
        }
    }

    @Test
    void multiPartTextTableMatchesSingleDocument() throws IOException
    {
        assertMultiEqualsSingle(DumpTableEnum.TEXT);
    }

    @Test
    void singleElementListMatchesSingleStream() throws IOException
    {
        final String doc = PART_HEADER + pageBlock(7, 77, "Solo") + PART_FOOTER;

        final byte[] fromSingle = runSingle(doc, DumpTableEnum.PAGE);
        final byte[] fromList = runMulti(List.of(stream(doc)), DumpTableEnum.PAGE);

        assertArrayEquals(fromSingle, fromList);
    }
}
