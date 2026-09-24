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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ArticleReaderException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ConfigurationException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.calculation.DiffCalculator;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(strings = { "<text>", "<text xml:space=\"preserve\">",
            // Current dumps (export-0.10 and later) put the size and hash before xml:space.
            "<text bytes=\"9\" sha1=\"abc\" xml:space=\"preserve\">",
            "<text xml:space=\"preserve\" bytes=\"9\">" })
    public void testReadsTextWithStartTag(String startTag) throws Exception
    {
        String xml = SITEINFO + page("Main Page", "0", 1, startTag + "Some text</text>")
                + page("Talk:Main Page", "1", 2, startTag + "Other</text>") + "</mediawiki>";

        assertEquals(List.of("Some text", "Other"), readTexts(xml));
    }

    @ParameterizedTest
    @ValueSource(strings = { "<text/>", "<text />", "<text bytes=\"0\" sha1=\"phoiac9\" />",
            "<text deleted=\"deleted\" />", "<text xml:space=\"preserve\" />" })
    public void testLeavesSelfClosingTextUnset(String text) throws Exception
    {
        // Empty and deleted texts are written as self-closing elements; they must neither fail
        // nor swallow the text of the following revision.
        String xml = SITEINFO + page("Main Page", "0", 1, text)
                + page("Talk:Main Page", "1", 2,
                        "<text bytes=\"9\" xml:space=\"preserve\">Some text</text>")
                + "</mediawiki>";

        assertEquals(Arrays.asList(null, "Some text"), readTexts(xml));
    }

    @ParameterizedTest
    @ValueSource(strings = { "<text", "<text bytes=\"1\"", "<text bytes=\"0\" /" })
    public void testFailsIfInputEndsWithinTextStartTag(String truncatedTag) throws Exception
    {
        String page = page("Main Page", "0", 1, truncatedTag);
        String xml = SITEINFO
                + page.substring(0, page.indexOf(truncatedTag) + truncatedTag.length());

        WikipediaXMLReader reader = new WikipediaXMLReader(new StringReader(xml));
        assertTrue(reader.hasNext());
        // The reader must not wait for the end of the tag forever
        ArticleReaderException e = assertTimeoutPreemptively(Duration.ofSeconds(10),
                () -> assertThrows(ArticleReaderException.class, reader::next));
        assertEquals(
                ErrorKeys.DELTA_CONSUMERS_TASK_READER_WIKIPEDIAXMLREADER_UNEXPECTED_END_OF_FILE
                        .toString(),
                e.getMessage());
    }

    /**
     * Creates a page with a single revision whose text is {@code Some text}.
     *
     * @param title
     *            the page title
     * @param namespace
     *            the content of the {@code <ns>} element, or {@code null} to omit it
     * @param id
     *            the page id; the revision id is derived from it
     * @return the page element
     */
    private static String page(String title, String namespace, int id)
    {
        return page(title, namespace, id, "<text xml:space=\"preserve\">Some text</text>");
    }

    /**
     * Creates a page with a single revision, laid out like in the dumps: the reader relies on the
     * whitespace between the elements.
     *
     * @param title
     *            the page title
     * @param namespace
     *            the content of the {@code <ns>} element, or {@code null} to omit it
     * @param id
     *            the page id; the revision id is derived from it
     * @param text
     *            the complete text element of the revision
     * @return the page element
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

    /**
     * Reads all revisions of the given dump and passes each one to the {@link DiffCalculator}.
     *
     * @param xml
     *            the dump, holding pages with a single revision each
     * @return for each revision the text the DiffCalculator stores, or {@code null} if it skips
     *         the revision because it has no text
     */
    private static List<String> readTexts(String xml) throws Exception
    {
        WikipediaXMLReader reader = new WikipediaXMLReader(new StringReader(xml));
        TextCalculator calculator = new TextCalculator();
        List<String> texts = new ArrayList<>();
        while (reader.hasNext()) {
            Task<Revision> task = reader.next();
            assertEquals(1, task.size());
            texts.add(calculator.storedText(task.getContainer().get(0)));
        }
        return texts;
    }

    /**
     * Exposes {@link DiffCalculator#processRevision(Revision)}, which returns {@code null} for a
     * revision without text.
     */
    private static final class TextCalculator
        extends DiffCalculator
    {
        TextCalculator() throws ConfigurationException
        {
            super(null);
        }

        String storedText(Revision revision) throws Exception
        {
            // processRevision() does not advance the revision counter, so this is a full revision
            Diff diff = processRevision(revision);
            return diff == null ? null : diff.buildRevision((String) null);
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
