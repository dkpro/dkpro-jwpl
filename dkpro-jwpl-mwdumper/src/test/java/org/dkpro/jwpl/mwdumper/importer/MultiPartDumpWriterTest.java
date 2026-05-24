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
package org.dkpro.jwpl.mwdumper.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class MultiPartDumpWriterTest
{

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
            events.add("startPage:" + page.Id);
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

    @Test
    void requiresNonNullDelegate()
    {
        assertThrows(NullPointerException.class, () -> new MultiPartDumpWriter(null));
    }

    @Test
    void collapsesLifecycleAndPassesThroughPageEvents() throws IOException
    {
        RecordingDumpWriter delegate = new RecordingDumpWriter();
        MultiPartDumpWriter sut = new MultiPartDumpWriter(delegate);

        // Part 1
        sut.writeStartWiki();
        sut.writeSiteinfo(new Siteinfo());
        Page page1 = new Page();
        page1.Id = 1;
        sut.writeStartPage(page1);
        Revision rev1 = new Revision();
        rev1.Id = 10;
        sut.writeRevision(rev1);
        sut.writeEndPage();
        sut.writeEndWiki(); // swallowed
        sut.close();        // swallowed

        // Part 2
        sut.writeStartWiki();                 // collapsed
        sut.writeSiteinfo(new Siteinfo());    // collapsed
        Page page2 = new Page();
        page2.Id = 2;
        sut.writeStartPage(page2);
        Revision rev2 = new Revision();
        rev2.Id = 20;
        sut.writeRevision(rev2);
        sut.writeEndPage();
        sut.writeEndWiki(); // swallowed
        sut.close();        // swallowed

        sut.finish(); // emits endWiki + close exactly once

        assertEquals(List.of(
                "startWiki",
                "siteinfo",
                "startPage:1",
                "revision:10",
                "endPage",
                "startPage:2",
                "revision:20",
                "endPage",
                "endWiki",
                "close"
        ), delegate.events);
    }

    @Test
    void finishWithoutStartWikiSkipsEndWikiButStillCloses() throws IOException
    {
        RecordingDumpWriter delegate = new RecordingDumpWriter();
        MultiPartDumpWriter sut = new MultiPartDumpWriter(delegate);

        sut.finish();

        assertEquals(List.of("close"), delegate.events);
    }

    @Test
    void finishIsIdempotent() throws IOException
    {
        RecordingDumpWriter delegate = new RecordingDumpWriter();
        MultiPartDumpWriter sut = new MultiPartDumpWriter(delegate);

        sut.writeStartWiki();
        sut.finish();
        sut.finish();
        sut.finish();

        assertEquals(List.of("startWiki", "endWiki", "close"), delegate.events);
    }
}
