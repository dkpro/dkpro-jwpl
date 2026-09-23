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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.dkpro.jwpl.datamachine.dump.xml.XML2Binary;
import org.dkpro.jwpl.datamachine.factory.DefaultDataMachineEnvironmentFactory;
import org.dkpro.jwpl.wikimachine.dump.sql.SQLEscape;
import org.dkpro.jwpl.wikimachine.util.UTFDataInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks that titles and texts from the XML dump are escaped exactly once for the
 * {@code mysqlimport} / {@code LOAD DATA INFILE} format, so that the value stored in the database
 * after the import equals the value in the dump.
 */
class XML2BinaryEscapingTest
{

    private static String dump(String title, String text)
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
                + "        <username>Alice</username>\n"
                + "        <id>100</id>\n"
                + "      </contributor>\n"
                + "      <text>" + text + "</text>\n"
                + "    </revision>\n"
                + "  </page>\n"
                + "</mediawiki>\n";
    }

    private static String[] convert(Path dir, String title, String text) throws IOException
    {
        final DataMachineFiles files = new DataMachineFiles(
                DefaultDataMachineEnvironmentFactory.getInstance().getLogger());
        files.setDataDirectory(dir.toAbsolutePath().toString());
        try (InputStream in = new ByteArrayInputStream(
                dump(title, text).getBytes(StandardCharsets.UTF_8))) {
            new XML2Binary(in, files);
        }

        final String storedTitle;
        try (UTFDataInputStream page = open(files.getGeneratedPage())) {
            assertEquals(1, page.readInt());
            assertEquals(0, page.readInt());
            storedTitle = page.readUTFAsArray();
        }
        final String storedText;
        try (UTFDataInputStream texts = open(files.getGeneratedText())) {
            assertEquals(10, texts.readInt());
            storedText = texts.readUTFAsArray();
        }
        return new String[] { storedTitle, storedText };
    }

    private static UTFDataInputStream open(String file) throws IOException
    {
        return new UTFDataInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file))));
    }

    @Test
    void backslashesAreEscapedExactlyOnce(@TempDir Path dir) throws IOException
    {
        final String title = "Back\\slash";
        final String text = "a\\b and C:\\dir\\";

        final String[] stored = convert(dir, title, text);

        assertEquals("Back\\\\slash", stored[0]);
        assertEquals("a\\\\b and C:\\\\dir\\\\", stored[1]);
        // What LOAD DATA INFILE / mysqlimport stores in the database:
        assertEquals(title, loadDataUnescape(stored[0]));
        assertEquals(text, loadDataUnescape(stored[1]));
    }

    @Test
    void valuesWithoutBackslashesAreUnchanged(@TempDir Path dir) throws IOException
    {
        final String title = "It's a \"title\"";
        final String text = "Line one\nIt's a \"quote\"\tand a tab";

        final String[] stored = convert(dir, title, text);

        assertEquals(SQLEscape.escape(SQLEscape.titleFormat(title)), stored[0]);
        assertEquals("It\\'s_a_\\\"title\\\"", stored[0]);
        assertEquals(SQLEscape.escape(text), stored[1]);
        assertEquals("Line one\\nIt\\'s a \\\"quote\\\"\\tand a tab", stored[1]);
        assertEquals(SQLEscape.titleFormat(title), loadDataUnescape(stored[0]));
        assertEquals(text, loadDataUnescape(stored[1]));
    }

    /**
     * Mirrors how {@code LOAD DATA INFILE} (and thus {@code mysqlimport}) interprets a field with
     * the default {@code FIELDS ESCAPED BY '\\'}.
     */
    private static String loadDataUnescape(String field)
    {
        final StringBuilder sb = new StringBuilder(field.length());
        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (c != '\\' || i + 1 == field.length()) {
                sb.append(c);
                continue;
            }
            char next = field.charAt(++i);
            switch (next) {
            case '0':
                sb.append('\u0000');
                break;
            case 'b':
                sb.append('\b');
                break;
            case 'n':
                sb.append('\n');
                break;
            case 'r':
                sb.append('\r');
                break;
            case 't':
                sb.append('\t');
                break;
            case 'Z':
                sb.append('\u001a');
                break;
            default:
                sb.append(next);
                break;
            }
        }
        return sb.toString();
    }
}
