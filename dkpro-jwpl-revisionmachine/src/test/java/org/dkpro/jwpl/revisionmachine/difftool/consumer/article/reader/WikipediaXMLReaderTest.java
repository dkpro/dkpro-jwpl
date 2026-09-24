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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.article.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WikipediaXMLReaderTest
{

    private static final String SITEINFO = "<mediawiki><siteinfo><namespaces>"
            + "<namespace key=\"0\" case=\"first-letter\" />"
            + "<namespace key=\"1\" case=\"first-letter\">Talk</namespace>"
            + "<namespace key=\"4\" case=\"first-letter\">Wikipedia</namespace>"
            + "</namespaces></siteinfo>";

    @BeforeAll
    public static void setUpConfiguration()
    {
        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        new ConfigurationManager(settings);
    }

    @Test
    public void testReadsNamespaceFromNsElement() throws Exception
    {
        String xml = SITEINFO + page("Main Page", "0", 1) + page("Talk:Main Page", "1", 2)
                + page("Wikipedia:About", "4", 3) + "</mediawiki>";

        assertEquals(List.of(0, 1, 4), readNamespaces(xml));
    }

    @Test
    public void testDerivesNamespaceFromTitlePrefixWithoutNsElement() throws Exception
    {
        // Older dumps have no <ns> element, so the prefix of the title has to do.
        String xml = SITEINFO + page("Main Page", null, 1) + page("Talk:Main Page", null, 2)
                + page("Wikipedia:About", null, 3) + "</mediawiki>";

        assertEquals(List.of(0, 1, 4), readNamespaces(xml));
    }

    @Test
    public void testReadsTextWithAttributes() throws Exception
    {
        // Current dumps (export-0.10 and later) put the size and hash before xml:space.
        String xml = SITEINFO
                + page("Main Page", "0", 1,
                        "<text bytes=\"9\" sha1=\"abc\" xml:space=\"preserve\">Some text</text>")
                + page("Talk:Main Page", "1", 2,
                        "<text xml:space=\"preserve\" bytes=\"5\">Other</text>")
                + "</mediawiki>";

        assertEquals(Arrays.asList("Some text", "Other"), readTexts(xml));
    }

    @Test
    public void testLeavesSelfClosingTextUnset() throws Exception
    {
        // Empty and deleted texts are written as self-closing elements; they must neither fail
        // nor swallow the text of the following revision.
        String xml = SITEINFO
                + page("Main Page", "0", 1, "<text bytes=\"0\" sha1=\"phoiac9\" />")
                + page("Talk:Main Page", "1", 2, "<text deleted=\"deleted\" />")
                + page("Wikipedia:About", "4", 3, "<text xml:space=\"preserve\" />")
                + page("Main page", "0", 4,
                        "<text bytes=\"9\" xml:space=\"preserve\">Some text</text>")
                + "</mediawiki>";

        assertEquals(Arrays.asList(null, null, null, "Some text"), readTexts(xml));
    }

    private static String page(String title, String namespace, int id)
    {
        return page(title, namespace, id, "<text xml:space=\"preserve\">Some text</text>");
    }

    /**
     * Creates a page with a single revision, laid out like in the dumps: the reader relies on the
     * whitespace between the elements.
     */
    private static String page(String title, String namespace, int id, String text)
    {
        return "\n  <page>\n    <title>" + title + "</title>\n"
                + (namespace == null ? "" : "    <ns>" + namespace + "</ns>\n")
                + "    <id>" + id + "</id>\n"
                + "    <revision>\n      <id>" + (id * 10) + "</id>\n"
                + "      <timestamp>2009-03-16T01:13:23Z</timestamp>\n"
                + "      <contributor>\n        <username>Someone</username>\n"
                + "        <id>177</id>\n      </contributor>\n"
                + "      " + text + "\n"
                + "    </revision>\n  </page>";
    }

    private static List<String> readTexts(String xml) throws Exception
    {
        WikipediaXMLReader reader = new WikipediaXMLReader(new StringReader(xml));
        List<String> texts = new ArrayList<>();
        while (reader.hasNext()) {
            Task<Revision> task = reader.next();
            assertEquals(1, task.size());
            texts.add(textOf(task.getContainer().get(0)));
        }
        return texts;
    }

    /**
     * Returns the text read for a revision, or {@code null} if none was read. Revision falls back
     * to loading an unset text through the RevisionApi, which fails without one; DiffCalculator
     * likewise treats that as a revision without text.
     */
    private static String textOf(Revision revision)
    {
        try {
            return revision.getRevisionText();
        }
        catch (NullPointerException e) {
            return null;
        }
    }

    private static List<Integer> readNamespaces(String xml) throws Exception
    {
        WikipediaXMLReader reader = new WikipediaXMLReader(new StringReader(xml));
        List<Integer> namespaces = new ArrayList<>();
        while (reader.hasNext()) {
            Task<Revision> task = reader.next();
            namespaces.add(task.getHeader().getNamespace());
        }
        return namespaces;
    }
}
