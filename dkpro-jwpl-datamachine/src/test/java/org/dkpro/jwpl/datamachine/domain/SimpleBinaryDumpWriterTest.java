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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.datamachine.dump.xml.SimpleBinaryDumpWriter;
import org.dkpro.jwpl.mwdumper.importer.NamespaceSet;
import org.dkpro.jwpl.mwdumper.importer.Page;
import org.dkpro.jwpl.mwdumper.importer.Revision;
import org.dkpro.jwpl.mwdumper.importer.Title;
import org.dkpro.jwpl.wikimachine.debug.Slf4JLogger;
import org.dkpro.jwpl.wikimachine.dump.sql.SQLEscape;
import org.dkpro.jwpl.wikimachine.util.UTFDataInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks the {@code text.bin} records written by {@link SimpleBinaryDumpWriter}: one record per
 * page, keyed by the page id and holding the text of the last revision, and none for category
 * pages.
 */
class SimpleBinaryDumpWriterTest
{

    private static final NamespaceSet NAMESPACES = new NamespaceSet();

    @TempDir
    Path dataDirectory;

    @Test
    void writesOneTextRecordPerPageKeyedByPageId() throws IOException
    {
        DataMachineFiles files = new DataMachineFiles(new Slf4JLogger());
        files.setDataDirectory(dataDirectory.toAbsolutePath().toString());

        SimpleBinaryDumpWriter writer = new SimpleBinaryDumpWriter(files);
        writer.writeStartWiki();
        // an article with two revisions: only the last one is kept
        writePage(writer, 1, 0, "Main Page", revision(11, "Old text"),
                revision(12, "New\ttext"));
        // a category page: no text record
        writePage(writer, 5, 14, "Top Level", revision(51, "Category text"));
        // a discussion page
        writePage(writer, 2, 1, "Main Page", revision(21, "Let us talk"));
        // a page without any revision: no record at all
        writePage(writer, 3, 0, "Empty");
        writer.writeEndWiki();
        writer.close();

        List<String> records = readTextRecords(Path.of(files.getGeneratedText()));
        assertEquals(List.of("1:" + SQLEscape.escape("New\ttext"), "2:Let us talk"), records);
    }

    private static Revision revision(int id, String text)
    {
        Revision revision = new Revision();
        revision.Id = id;
        revision.Text = text;
        return revision;
    }

    private static void writePage(SimpleBinaryDumpWriter writer, int id, int namespace,
            String title, Revision... revisions)
        throws IOException
    {
        Page page = new Page();
        page.Id = id;
        page.Title = new Title(namespace, title, NAMESPACES);
        writer.writeStartPage(page);
        for (Revision revision : revisions) {
            writer.writeRevision(revision);
        }
        writer.writeEndPage();
    }

    private static List<String> readTextRecords(Path textFile) throws IOException
    {
        List<String> records = new ArrayList<>();
        try (UTFDataInputStream in = new UTFDataInputStream(
                new BufferedInputStream(Files.newInputStream(textFile)))) {
            while (true) {
                int id;
                try {
                    id = in.readInt();
                }
                catch (EOFException e) {
                    break;
                }
                records.add(id + ":" + in.readUTFAsArray());
            }
        }
        assertFalse(records.isEmpty(), "text.bin must not be empty");
        return records;
    }
}
