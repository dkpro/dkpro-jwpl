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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.dkpro.jwpl.wikimachine.dump.sql.SQLEscape;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableEnum;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;
import org.junit.jupiter.api.Test;

/**
 * Round-trips revision texts through the pipe of {@link XMLDumpTableInputStream} and
 * {@link TextParser}, covering texts larger than the reader-side buffer and the delivery of
 * the last record at the end of the stream.
 */
class XMLDumpTableInputStreamTest
{

    private static final String HEADER =
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
            + "    </namespaces>\n"
            + "  </siteinfo>\n";
    private static final String FOOTER = "</mediawiki>\n";

    private static String pageBlock(int pageId, int revisionId, String text)
    {
        return "  <page>\n"
                + "    <title>Page " + pageId + "</title>\n"
                + "    <id>" + pageId + "</id>\n"
                + "    <revision>\n"
                + "      <id>" + revisionId + "</id>\n"
                + "      <timestamp>2020-01-01T00:00:00Z</timestamp>\n"
                + "      <contributor>\n"
                + "        <username>Alice</username>\n"
                + "        <id>100</id>\n"
                + "      </contributor>\n"
                + "      <text>" + text + "</text>\n"
                + "    </revision>\n"
                + "  </page>\n";
    }

    private static String largeText(int length)
    {
        final StringBuilder sb = new StringBuilder(length);
        final String pattern = "Lorem ipsum äöü ß ";
        while (sb.length() < length) {
            sb.append(pattern);
        }
        return sb.toString();
    }

    @Test
    void textTableRoundTripsLargeTextsAndLastRecord() throws IOException
    {
        final String large = largeText(200_000);
        final String small = "last";
        final String xml = HEADER
                + pageBlock(1, 10, large)
                + pageBlock(2, 20, large)
                + pageBlock(3, 30, small)
                + FOOTER;

        final XMLDumpTableInputStream sut = new XMLDumpTableInputStream();
        sut.initialize(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                DumpTableEnum.TEXT);

        try (TextParser parser = new TextParser()) {
            parser.setInputStream(sut);

            assertTrue(parser.next());
            assertEquals(10, parser.getOldId());
            assertEquals(SQLEscape.escape(large), parser.getOldText());

            assertTrue(parser.next());
            assertEquals(20, parser.getOldId());
            assertEquals(SQLEscape.escape(large), parser.getOldText());

            assertTrue(parser.next());
            assertEquals(30, parser.getOldId());
            assertEquals(SQLEscape.escape(small), parser.getOldText());

            assertFalse(parser.next());
        }
    }
}
