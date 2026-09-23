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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.dkpro.jwpl.wikimachine.dump.sql.SQLEscape;
import org.dkpro.jwpl.wikimachine.util.UTFDataInputStream;
import org.junit.jupiter.api.Test;

/**
 * Checks that the TimeMachine readers and writers escape titles and texts from the XML dump
 * exactly once for the {@code mysqlimport} / {@code LOAD DATA INFILE} format.
 */
class XmlReaderEscapingTest
{

    private static InputStream dump(String title, String text)
    {
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
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
                + "        <username>Alice</username>\n"
                + "        <id>100</id>\n"
                + "      </contributor>\n"
                + "      <text>" + text + "</text>\n"
                + "    </revision>\n"
                + "  </page>\n"
                + "</mediawiki>\n";
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    private static String storedTitle(String title, String text) throws IOException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        new PageReader(dump(title, text), new PageWriter(out)).readDump();
        try (UTFDataInputStream in = new UTFDataInputStream(
                new ByteArrayInputStream(out.toByteArray()))) {
            assertEquals(1, in.readInt());
            assertEquals(0, in.readInt());
            return in.readUTFAsArray();
        }
    }

    private static String storedText(String title, String text) throws IOException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        new TextReader(dump(title, text), new TextWriter(out)).readDump();
        try (UTFDataInputStream in = new UTFDataInputStream(
                new ByteArrayInputStream(out.toByteArray()))) {
            assertEquals(10, in.readInt());
            return in.readUTFAsArray();
        }
    }

    @Test
    void backslashesAreEscapedExactlyOnce() throws IOException
    {
        assertEquals("Back\\\\slash", storedTitle("Back\\slash", "x"));
        assertEquals("a\\\\b and C:\\\\dir\\\\", storedText("T", "a\\b and C:\\dir\\"));
    }

    @Test
    void valuesWithoutBackslashesAreUnchanged() throws IOException
    {
        final String title = "It's a \"title\"";
        final String text = "Line one\nIt's a \"quote\"\tand a tab";

        assertEquals(SQLEscape.escape(SQLEscape.titleFormat(title)), storedTitle(title, text));
        assertEquals("It\\'s_a_\\\"title\\\"", storedTitle(title, text));
        assertEquals(SQLEscape.escape(text), storedText(title, text));
        assertEquals("Line one\\nIt\\'s a \\\"quote\\\"\\tand a tab", storedText(title, text));
    }
}
