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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.dkpro.jwpl.mwdumper.importer.DumpWriter;
import org.dkpro.jwpl.mwdumper.importer.Page;
import org.dkpro.jwpl.mwdumper.importer.Revision;
import org.dkpro.jwpl.mwdumper.importer.Siteinfo;
import org.junit.jupiter.api.Test;

class MultiPartXmlDumpReaderTest
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

    private static String part(int pageId, int revisionId, String title)
    {
        return PART_HEADER
                + "  <page>\n"
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
                + "  </page>\n"
                + PART_FOOTER;
    }

    private static InputStream stream(String xml)
    {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void parsesTwoPartsAsSingleLogicalDocument() throws IOException
    {
        final RecordingDumpWriter delegate = new RecordingDumpWriter();

        MultiPartXmlDumpReader.readDumps(
                List.of(stream(part(1, 10, "Page One")), stream(part(2, 20, "Page Two"))),
                delegate,
                WikiXMLDumpReader::new);

        assertEquals(List.of(
                "startWiki",
                "siteinfo",
                "startPage:1:Page One",
                "revision:10",
                "endPage",
                "startPage:2:Page Two",
                "revision:20",
                "endPage",
                "endWiki",
                "close"
        ), delegate.events);
    }

    @Test
    void singlePartBehavesLikeSingleDocument() throws IOException
    {
        final RecordingDumpWriter delegate = new RecordingDumpWriter();

        MultiPartXmlDumpReader.readDumps(
                List.of(stream(part(7, 77, "Solo"))),
                delegate,
                WikiXMLDumpReader::new);

        assertEquals(List.of(
                "startWiki",
                "siteinfo",
                "startPage:7:Solo",
                "revision:77",
                "endPage",
                "endWiki",
                "close"
        ), delegate.events);
    }

    @Test
    void rejectsNullPartsList()
    {
        assertThrows(IllegalArgumentException.class, () ->
                MultiPartXmlDumpReader.readDumps(null, new RecordingDumpWriter(), WikiXMLDumpReader::new));
    }

    @Test
    void rejectsEmptyPartsList()
    {
        assertThrows(IllegalArgumentException.class, () ->
                MultiPartXmlDumpReader.readDumps(Collections.emptyList(),
                        new RecordingDumpWriter(), WikiXMLDumpReader::new));
    }

    @Test
    void rejectsNullElementInPartsList()
    {
        final List<InputStream> parts = new ArrayList<>();
        parts.add(stream(part(1, 10, "Page One")));
        parts.add(null);
        assertThrows(IllegalArgumentException.class, () ->
                MultiPartXmlDumpReader.readDumps(parts, new RecordingDumpWriter(), WikiXMLDumpReader::new));
    }

    @Test
    void closesDelegateEvenIfParsingFails()
    {
        final RecordingDumpWriter delegate = new RecordingDumpWriter();
        final InputStream malformed = stream("<?xml version=\"1.0\"?><not-mediawiki>oops");

        assertThrows(IOException.class, () ->
                MultiPartXmlDumpReader.readDumps(List.of(malformed), delegate, WikiXMLDumpReader::new));

        // Even on failure, the delegate writer must be closed — nothing else leaked.
        assertEquals(List.of("close"), delegate.events);
    }

    @Test
    void closesEveryPartStreamOnSuccess() throws IOException
    {
        final CountingInputStream a = new CountingInputStream(stream(part(1, 10, "A")));
        final CountingInputStream b = new CountingInputStream(stream(part(2, 20, "B")));

        MultiPartXmlDumpReader.readDumps(List.<InputStream>of(a, b),
                new RecordingDumpWriter(), WikiXMLDumpReader::new);

        // SAXParser.parse also closes the stream internally in some JDKs; close() is
        // idempotent on InputStream so we only require that each part was closed at least
        // once (ownership transferred, no leak).
        assertTrue(a.closed.get() >= 1, "first part stream not closed");
        assertTrue(b.closed.get() >= 1, "second part stream not closed");
    }

    @Test
    void closesEveryPartStreamOnFailure()
    {
        // Second part is malformed so parsing fails mid-list; both parts must still be closed.
        final CountingInputStream good = new CountingInputStream(stream(part(1, 10, "A")));
        final CountingInputStream bad = new CountingInputStream(
                stream("<?xml version=\"1.0\"?><not-mediawiki>oops"));

        assertThrows(IOException.class, () ->
                MultiPartXmlDumpReader.readDumps(List.<InputStream>of(good, bad),
                        new RecordingDumpWriter(), WikiXMLDumpReader::new));

        assertTrue(good.closed.get() >= 1, "good part stream not closed");
        assertTrue(bad.closed.get() >= 1, "bad part stream not closed");
    }

    private static final class CountingInputStream extends FilterInputStream
    {
        final AtomicInteger closed = new AtomicInteger();

        CountingInputStream(InputStream delegate)
        {
            super(delegate);
        }

        @Override
        public void close() throws IOException
        {
            closed.incrementAndGet();
            super.close();
        }
    }

    private static final class RecordingDumpWriter
        implements DumpWriter
    {
        final List<String> events = new ArrayList<>();

        @Override
        public void close()
        {
            events.add("close");
        }

        @Override
        public void writeStartWiki()
        {
            events.add("startWiki");
        }

        @Override
        public void writeEndWiki()
        {
            events.add("endWiki");
        }

        @Override
        public void writeSiteinfo(Siteinfo info)
        {
            events.add("siteinfo");
        }

        @Override
        public void writeStartPage(Page page)
        {
            events.add("startPage:" + page.Id + ":" + page.Title);
        }

        @Override
        public void writeEndPage()
        {
            events.add("endPage");
        }

        @Override
        public void writeRevision(Revision revision)
        {
            events.add("revision:" + revision.Id);
        }
    }
}
