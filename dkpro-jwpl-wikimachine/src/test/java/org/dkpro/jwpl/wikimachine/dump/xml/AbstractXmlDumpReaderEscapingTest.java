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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.dkpro.jwpl.mwdumper.importer.DumpWriter;
import org.dkpro.jwpl.mwdumper.importer.Page;
import org.dkpro.jwpl.mwdumper.importer.Revision;
import org.dkpro.jwpl.mwdumper.importer.Siteinfo;
import org.junit.jupiter.api.Test;

/**
 * Checks that {@link AbstractXmlDumpReader} hands the character data of the XML dump to its
 * {@link DumpWriter} unchanged. Escaping for the import format is the job of the writers
 * (via {@link org.dkpro.jwpl.wikimachine.dump.sql.SQLEscape#escape(String)}), so the reader must
 * not escape anything itself; otherwise backslashes end up escaped twice.
 */
class AbstractXmlDumpReaderEscapingTest
{

    private static String dump(String title, String comment, String username, String text)
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.10/\" "
                + "version=\"0.10\" xml:lang=\"en\">\n"
                + "  <siteinfo>\n"
                + "    <sitename>Test Wiki</sitename>\n"
                + "    <namespaces>\n"
                + "      <namespace key=\"0\" case=\"first-letter\"/>\n"
                + "      <namespace key=\"1\" case=\"first-letter\">Talk</namespace>\n"
                + "    </namespaces>\n"
                + "  </siteinfo>\n"
                + "  <page>\n"
                + "    <title>" + title + "</title>\n"
                + "    <id>1</id>\n"
                + "    <revision>\n"
                + "      <id>10</id>\n"
                + "      <timestamp>2020-01-01T00:00:00Z</timestamp>\n"
                + "      <contributor>\n"
                + "        <username>" + username + "</username>\n"
                + "        <id>100</id>\n"
                + "      </contributor>\n"
                + "      <comment>" + comment + "</comment>\n"
                + "      <text>" + text + "</text>\n"
                + "    </revision>\n"
                + "  </page>\n"
                + "</mediawiki>\n";
    }

    private static CapturingDumpWriter read(String xml) throws IOException
    {
        final CapturingDumpWriter writer = new CapturingDumpWriter();
        new WikiXMLDumpReader(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                writer).readDump();
        assertNotNull(writer.page, "no page was read");
        assertNotNull(writer.revision, "no revision was read");
        return writer;
    }

    @Test
    void backslashesArePassedOnUnchanged() throws IOException
    {
        final CapturingDumpWriter writer = read(dump("Back\\slash", "fix a\\b",
                "User\\Name", "a\\b and \\\\ and C:\\dir\\"));

        assertEquals("Back\\slash", writer.page.Title.Text);
        assertEquals("fix a\\b", writer.revision.Comment);
        assertEquals("User\\Name", writer.revision.Contributor.Username);
        assertEquals("a\\b and \\\\ and C:\\dir\\", writer.revision.Text);
    }

    @Test
    void textWithoutBackslashesIsPassedOnUnchanged() throws IOException
    {
        final String text = "Line one\nIt's a \"quote\"\tand a tab";
        final CapturingDumpWriter writer = read(dump("Plain title", "plain comment", "Alice",
                text));

        assertEquals("Plain title", writer.page.Title.Text);
        assertEquals("plain comment", writer.revision.Comment);
        assertEquals("Alice", writer.revision.Contributor.Username);
        assertEquals(text, writer.revision.Text);
    }

    private static final class CapturingDumpWriter
        implements DumpWriter
    {
        Page page;
        Revision revision;

        @Override
        public void close()
        {
        }

        @Override
        public void writeStartWiki()
        {
        }

        @Override
        public void writeEndWiki()
        {
        }

        @Override
        public void writeSiteinfo(Siteinfo info)
        {
        }

        @Override
        public void writeStartPage(Page aPage)
        {
            page = aPage;
        }

        @Override
        public void writeEndPage()
        {
        }

        @Override
        public void writeRevision(Revision aRevision)
        {
            revision = aRevision;
        }
    }
}
