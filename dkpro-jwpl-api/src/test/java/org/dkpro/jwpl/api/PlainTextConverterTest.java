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
package org.dkpro.jwpl.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dkpro.jwpl.api.sweble.PlainTextConverter;
import org.junit.jupiter.api.Test;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.nodes.WtNode;

/**
 * Unit tests for {@link PlainTextConverter}. The converter turns a parsed Sweble AST into plain
 * text; every test here builds its own wikitext, parses it with the Sweble engine, and asserts on
 * the concrete rendered String. No database fixture is required.
 */
public class PlainTextConverterTest
{

    private static final WikiConfig CONFIG = DefaultConfigEnWp.generate();

    /** Parse wikitext into an AST using the Sweble engine. */
    private WtNode parse(String markup) throws Exception
    {
        WtEngineImpl engine = new WtEngineImpl(CONFIG);
        PageTitle pageTitle = PageTitle.make(CONFIG, "Test");
        PageId pageId = new PageId(pageTitle, -1);
        EngProcessedPage cp = engine.postprocess(pageId, markup, null);
        return cp.getPage();
    }

    /** Render wikitext to plain text with the given line wrap column. */
    private String convert(String markup, int wrapCol) throws Exception
    {
        return (String) new PlainTextConverter(CONFIG, false, wrapCol).go(parse(markup));
    }

    /** Render wikitext to plain text without wrapping (columns unbounded). */
    private String convert(String markup) throws Exception
    {
        return convert(markup, Integer.MAX_VALUE);
    }

    // ---- Links -------------------------------------------------------------

    /** A postfix immediately following a link ([[UKP]]postfix) is appended to the link text. */
    @Test
    public void testInternalLinkPostfixIsAppendedToLinkText() throws Exception
    {
        assertEquals("See UKPpostfix here.", convert("See [[UKP]]postfix here."));
    }

    /** For a piped link the display text is rendered and the target is dropped. */
    @Test
    public void testInternalLinkRendersDisplayTextNotTarget() throws Exception
    {
        String result = convert("Intro [[anchor link|display text]] follows.");
        assertEquals("Intro display text follows.", result);
        assertFalse(result.contains("anchor link"), "Link target must not appear: " + result);
    }

    /** Category links contribute no text to the output. */
    @Test
    public void testCategoryLinksAreOmitted() throws Exception
    {
        String result = convert("[[Category:Biologie]] Body text here.");
        assertEquals("Body text here.", result);
        assertFalse(result.contains("Biologie"), "Category target must not appear: " + result);
    }

    // ---- Tables ------------------------------------------------------------

    /** Table cells are emitted row by row, joined by a pipe, one row per line. */
    @Test
    public void testTableRowCellsAreJoinedByPipe() throws Exception
    {
        String markup = "{|\n| Studiengang || 1979\n|-\n| Foo || 2014\n|}";
        assertEquals("Studiengang|1979\nFoo|2014", convert(markup));
    }

    // ---- Sections ----------------------------------------------------------

    /** Section headings are rendered on their own lines, in document order. */
    @Test
    public void testSectionHeadingsRenderInOrder() throws Exception
    {
        String markup = "This is intro.\n\n== First Section ==\n\nBody one.\n\n"
                + "=== Nested Subsection ===\n\nBody nested.\n\n== Second Section ==\n\nBody two.";
        String result = convert(markup);
        assertEquals("This is intro.\n\nFirst Section\nBody one.\n\n"
                + "Nested Subsection\nBody nested.\n\nSecond Section\nBody two.", result);
    }

    /** With section enumeration enabled, headings are prefixed by their running number. */
    @Test
    public void testEnumeratedSectionsArePrefixedWithNumbers() throws Exception
    {
        String markup = "== Section One ==\n\nAlpha.\n\n== Section Two ==\n\nBeta.";
        String result = (String) new PlainTextConverter(CONFIG, true, Integer.MAX_VALUE)
                .go(parse(markup));
        assertTrue(result.contains("1. Section One"), "First heading must be numbered: " + result);
        assertTrue(result.contains("2. Section Two"), "Second heading must be numbered: " + result);
    }

    /** A heading is never wrapped, even when it is longer than the wrap column. */
    @Test
    public void testHeadingIsNotWrappedByWrapColumn() throws Exception
    {
        String markup = "Intro paragraph text.\n\n=== A Fairly Long Heading Title ===\n\nBody.";
        String result = convert(markup, 12);
        assertTrue(result.contains("A Fairly Long Heading Title"),
                "Heading must stay on one line: " + result);
    }

    // ---- Lists and paragraphs ---------------------------------------------

    /** List item words are separated by single spaces, collapsing extra whitespace. */
    @Test
    public void testListItemWordsAreSingleSpaced() throws Exception
    {
        String markup = "* First list item\n* Second list item  with extra spaces\n"
                + "* Third list item";
        assertEquals("First list item Second list item with extra spaces Third list item",
                convert(markup));
    }

    /** Two paragraphs separated by a blank line render on separate lines. */
    @Test
    public void testConsecutiveParagraphsRenderOnSeparateLines() throws Exception
    {
        assertEquals("Line one.\nLine two.", convert("Line one.\n\nLine two."));
    }

    /** Content wrapped in nested XML tags (small/center) still contributes its text. */
    @Test
    public void testXmlElementBodyTextIsEmitted() throws Exception
    {
        String result = convert("Body <small><center>Quellen here</center></small> end.");
        assertEquals("Body\nQuellen here end.", result);
    }

    // ---- Line wrapping -----------------------------------------------------

    /**
     * When the running line plus the next word (including the separating space) reaches wrapCol, the
     * word moves to a new line; words that still fit share the line.
     */
    @Test
    public void testWordsThatFitShareLineOverflowingWordWraps() throws Exception
    {
        // wrapCol=13: "abc def ghi" fits (11 chars); "jkl" would make 15 >= 13, so it wraps.
        assertEquals("abc def ghi\njkl mno", convert("abc def ghi jkl mno", 13));
    }

    /** The separating space counts toward the wrap column when deciding to break. */
    @Test
    public void testSeparatingSpaceCountsTowardWrapColumn() throws Exception
    {
        // wrapCol=9: "abcd"(4) + space + "efgh"(4) = 9 >= 9, so each word starts a new line.
        assertEquals("abcd\nefgh\nijkl", convert("abcd efgh ijkl", 9));
    }

    /** Words that fill exactly up to the wrap column each start their own line. */
    @Test
    public void testWordsWrapAtExactColumnBoundary() throws Exception
    {
        // wrapCol=17: each subsequent 8-char word plus a space reaches 17, forcing a wrap.
        assertEquals("abcdefgh\nabcdefgh\nabcdefgh\nabcdefgh",
                convert("abcdefgh abcdefgh abcdefgh abcdefgh", 17));
    }

    /** Words stay space-separated on one line while they fit within the wrap column. */
    @Test
    public void testWordsRemainSpaceSeparatedWithinColumn() throws Exception
    {
        // wrapCol=20: three words fit (17 chars); the fourth overflows to the next line.
        assertEquals("word1 word2 word3\nword4", convert("word1 word2 word3 word4", 20));
    }

    /** A word that fits shares the line; the following word that overflows wraps. */
    @Test
    public void testTwoWordsShareLineThirdWraps() throws Exception
    {
        // wrapCol=14: "hello world" fits (11 chars); "again" overflows to a new line.
        assertEquals("hello world\nagain", convert("hello world again", 14));
    }

    /** With a very narrow column every word ends up on its own line. */
    @Test
    public void testNarrowColumnWrapsEveryWord() throws Exception
    {
        assertEquals("Hello\nworld\ntest\nhere", convert("Hello world test here", 8));
    }
}
