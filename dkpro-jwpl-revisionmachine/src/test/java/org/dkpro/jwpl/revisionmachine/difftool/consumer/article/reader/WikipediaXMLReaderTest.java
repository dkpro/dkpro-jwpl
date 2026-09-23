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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
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
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.TaskTypes;
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
     * A small dump in the layout of a real meta-history export, with multibyte text, entities,
     * several revisions, a redirect, an IP contributor, a minor edit and a filtered namespace.
     */
    private static final String DUMP = "<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.10/\""
            + " version=\"0.10\" xml:lang=\"de\">\n"
            + "  <siteinfo>\n    <sitename>Wikipedia</sitename>\n    <namespaces>\n"
            + "      <namespace key=\"-1\" case=\"first-letter\">Spezial</namespace>\n"
            + "      <namespace key=\"0\" case=\"first-letter\" />\n"
            + "      <namespace key=\"1\" case=\"first-letter\">Diskussion</namespace>\n"
            + "      <namespace key=\"4\" case=\"first-letter\">Wikipedia</namespace>\n"
            + "    </namespaces>\n  </siteinfo>\n"
            + "  <page>\n    <title>Größe</title>\n    <ns>0</ns>\n    <id>7</id>\n"
            + "    <revision>\n      <id>70</id>\n"
            + "      <timestamp>2009-03-16T01:13:23Z</timestamp>\n"
            + "      <contributor>\n        <username>Jürgen</username>\n        <id>177</id>\n"
            + "      </contributor>\n      <comment>Neu &amp; frisch</comment>\n"
            + "      <model>wikitext</model>\n      <format>text/x-wiki</format>\n"
            + "      <text xml:space=\"preserve\">Größe – 東京 😀 &lt;b&gt;fett&lt;/b&gt; &amp;amp;</text>\n"
            + "      <sha1>abc</sha1>\n    </revision>\n"
            + "    <revision>\n      <id>71</id>\n      <parentid>70</parentid>\n"
            + "      <timestamp>2009-03-17T10:00:00Z</timestamp>\n"
            + "      <contributor>\n        <ip>192.0.2.1</ip>\n      </contributor>\n"
            + "      <minor />\n      <comment>O'Brien</comment>\n"
            + "      <text xml:space=\"preserve\">Москва\nzweite Zeile</text>\n    </revision>\n"
            + "    <revision>\n      <id>72</id>\n      <parentid>71</parentid>\n"
            + "      <timestamp>2009-03-18T12:30:45Z</timestamp>\n"
            + "      <contributor>\n        <username>Ärger &amp; Co</username>\n"
            + "        <id>9</id>\n      </contributor>\n"
            + "      <text xml:space=\"preserve\">&quot;قاهرة&quot;</text>\n    </revision>\n"
            + "  </page>\n"
            + "  <page>\n    <title>Wikipedia:Über</title>\n    <ns>4</ns>\n    <id>8</id>\n"
            + "    <revision>\n      <id>80</id>\n"
            + "      <timestamp>2010-01-01T00:00:00Z</timestamp>\n"
            + "      <contributor>\n        <ip>192.0.2.2</ip>\n      </contributor>\n"
            + "      <text xml:space=\"preserve\">verworfen</text>\n    </revision>\n"
            + "  </page>\n"
            + "  <page>\n    <title>Groesse</title>\n    <ns>0</ns>\n    <id>9</id>\n"
            + "    <redirect title=\"Größe\" />\n"
            + "    <revision>\n      <id>90</id>\n"
            + "      <timestamp>2011-05-05T05:05:05Z</timestamp>\n"
            + "      <contributor>\n        <username>Bot</username>\n        <id>1</id>\n"
            + "      </contributor>\n"
            + "      <text xml:space=\"preserve\">#WEITERLEITUNG [[Größe]]</text>\n"
            + "    </revision>\n  </page>\n"
            + "  <page>\n    <title>Diskussion:Größe</title>\n    <ns>1</ns>\n    <id>10</id>\n"
            + "    <revision>\n      <id>100</id>\n"
            + "      <timestamp>2012-12-12T12:12:12Z</timestamp>\n"
            + "      <contributor>\n        <username>Jürgen</username>\n        <id>177</id>\n"
            + "      </contributor>\n"
            + "      <text xml:space=\"preserve\">Frage zu &lt;ref&gt;</text>\n"
            + "    </revision>\n  </page>\n"
            + "</mediawiki>\n";

    @Test
    public void testParsesDump() throws Exception
    {
        WikipediaXMLReader reader = new WikipediaXMLReader(utf8Reader(DUMP),
                new ArticleFilter(List.of(0, 1)));

        assertTrue(reader.hasNext());
        Task<Revision> task = reader.next();
        assertEquals("7|Größe|0|1|" + TaskTypes.TASK_FULL, describe(task));
        assertEquals(3, task.size());
        assertEquals("0|70|2009-03-16 01:13:23.0|false|Jürgen|true|177|Neu &amp; frisch|"
                + "Größe – 東京 😀 <b>fett</b> &amp;", describe(task.get(0)));
        assertEquals("1|71|2009-03-17 10:00:00.0|true|192.0.2.1|false|null|O\\'Brien|"
                + "Москва\nzweite Zeile", describe(task.get(1)));
        assertEquals("2|72|2009-03-18 12:30:45.0|false|Ärger &amp; Co|true|9|null|"
                + "\"قاهرة\"", describe(task.get(2)));
        assertEquals(DUMP.indexOf("</page>") + "</page>".length(), reader.getBytePosition());

        // The page from namespace 4 is read, but rejected by the filter
        assertTrue(reader.hasNext());
        assertNull(reader.next());

        assertTrue(reader.hasNext());
        task = reader.next();
        assertEquals("9|Groesse|0|1|" + TaskTypes.TASK_FULL, describe(task));
        assertEquals(1, task.size());
        assertEquals("0|90|2011-05-05 05:05:05.0|false|Bot|true|1|null|#WEITERLEITUNG [[Größe]]",
                describe(task.get(0)));

        assertTrue(reader.hasNext());
        task = reader.next();
        assertEquals("10|Diskussion:Größe|1|1|" + TaskTypes.TASK_FULL, describe(task));
        assertEquals(1, task.size());
        assertEquals("0|100|2012-12-12 12:12:12.0|false|Jürgen|true|177|null|Frage zu <ref>",
                describe(task.get(0)));

        assertFalse(reader.hasNext());
        // Every char has been counted, plus the read that hit the end of the stream
        assertEquals(DUMP.length() + 1, reader.getBytePosition());
    }

    @Test
    public void testParsesTextSpanningSeveralReadBuffers() throws Exception
    {
        String text = "ä".repeat(40000) + "東京".repeat(30000) + "😀".repeat(20000);
        String xml = SITEINFO + page("Main Page", "0", 1).replace("Some text", text)
                + "</mediawiki>";

        WikipediaXMLReader reader = new WikipediaXMLReader(utf8Reader(xml));
        assertTrue(reader.hasNext());
        Task<Revision> task = reader.next();
        assertEquals(1, task.size());
        assertEquals(text, task.get(0).getRevisionText());
        assertEquals("Someone", task.get(0).getContributorName());
        assertFalse(reader.hasNext());
        assertEquals(xml.length() + 1, reader.getBytePosition());
    }

    private static Reader utf8Reader(String xml)
    {
        return new InputStreamReader(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }

    private static String describe(Task<Revision> task)
    {
        return task.getHeader().getArticleId() + "|" + task.getHeader().getArticleName() + "|"
                + task.getHeader().getNamespace() + "|" + task.getPartCounter() + "|"
                + task.getTaskType();
    }

    private static String describe(Revision revision)
    {
        return revision.getRevisionCounter() + "|" + revision.getRevisionID() + "|"
                + revision.getTimeStamp() + "|" + revision.isMinor() + "|"
                + revision.getContributorName() + "|" + revision.contributorIsRegistered() + "|"
                + revision.getContributorId() + "|" + revision.getComment() + "|"
                + revision.getRevisionText();
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
