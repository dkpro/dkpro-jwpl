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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.article.reader.WikipediaXMLReader;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.TaskTransmitterInterface;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionDecoder;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionEncoder;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests that the revisions of a blanked page are stored with an empty text, from reading the dump
 * over the {@link DiffCalculator} to decoding the stored diffs.
 */
class DiffCalculatorEmptyTextTest
{

    private static final String ENCODING = StandardCharsets.UTF_8.toString();

    @BeforeAll
    static void setUpConfiguration()
    {
        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        new ConfigurationManager(settings);
    }

    @Test
    void blankedRevisionsAreStoredWithEmptyText() throws Exception
    {
        String xml = "<mediawiki><siteinfo><namespaces>"
                + "<namespace key=\"0\" case=\"first-letter\" /></namespaces></siteinfo>"
                + "\n  <page>\n    <title>Main Page</title>\n    <ns>0</ns>\n    <id>1</id>"
                + revision(10, "<text bytes=\"9\">Some text</text>")
                + revision(11, "<text bytes=\"0\" sha1=\"phoiac9h4m842xq45sp7s6u21eteeq1\" />")
                + revision(12, "<text bytes=\"9\">Some text</text>")
                + revision(13, "<text bytes=\"0\"></text>")
                + revision(14, "<text bytes=\"5\" deleted=\"deleted\" />")
                + revision(15, "<text bytes=\"5\">Other</text>") + "\n  </page>\n</mediawiki>";

        List<Diff> diffs = new ArrayList<>();
        DiffCalculator calculator = new DiffCalculator(new TaskTransmitterInterface()
        {
            @Override
            public void transmitDiff(Task<Diff> result)
            {
                diffs.addAll(result.getContainer());
            }

            @Override
            public void transmitPartialDiff(Task<Diff> result)
            {
                diffs.addAll(result.getContainer());
            }

            @Override
            public void close()
            {
            }
        });

        WikipediaXMLReader reader = new WikipediaXMLReader(new StringReader(xml));
        while (reader.hasNext()) {
            calculator.process(reader.next());
        }

        // The deleted text is skipped. The empty texts are stored, and so is the revision that
        // restores the text after the page was blanked.
        List<Integer> revisionIds = new ArrayList<>();
        List<String> texts = new ArrayList<>();
        RevisionEncoder encoder = new RevisionEncoder();
        String previous = null;
        for (Diff diff : diffs) {
            RevisionDecoder decoder = new RevisionDecoder(ENCODING);
            decoder.setInput(encoder.binaryDiff(diff.getCodecData(), diff));
            previous = decoder.decode().buildRevision(previous);

            revisionIds.add(diff.getRevisionID());
            texts.add(previous);
        }
        assertEquals(List.of(10, 11, 12, 13, 15), revisionIds);
        assertEquals(List.of("Some text", "", "Some text", "", "Other"), texts);
    }

    /**
     * Creates a revision laid out like in the dumps; the timestamp keeps the revisions in the order
     * of their ids.
     *
     * @param id
     *            the revision id, between 10 and 59
     * @param text
     *            the complete text element of the revision
     * @return the revision element
     */
    private static String revision(int id, String text)
    {
        return "\n    <revision>\n      <id>" + id + "</id>\n"
                + "      <timestamp>2009-03-16T01:13:" + id + "Z</timestamp>\n"
                + "      <contributor>\n        <username>Someone</username>\n"
                + "        <id>177</id>\n      </contributor>\n"
                + "      " + text + "\n    </revision>";
    }
}
