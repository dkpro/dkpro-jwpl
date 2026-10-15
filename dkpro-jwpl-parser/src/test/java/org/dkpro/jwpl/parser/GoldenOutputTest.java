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
package org.dkpro.jwpl.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.parser.Content.FormatType;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.dkpro.jwpl.parser.mediawiki.ModularParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Compares the complete output of the parser on a corpus of pages with the output recorded in
 * {@code src/test/resources/golden/expected}. The dump covers the section, paragraph, table and
 * list structure, the text of every content element, its format spans, links and templates with
 * their positions, and the source spans where they are calculated. It guards changes to the
 * parser that must not change its output (see issue #586).
 * <p>
 * Run with {@code -Dgolden.update=true} from the module directory to record the current output as
 * the expected one.
 */
class GoldenOutputTest
{

    private static final String GOLDEN = "golden/";

    private static final List<String> PAGES = List.of("pages/Wiki-Article-Americium.txt",
            GOLDEN + "pages/links-and-templates.txt", GOLDEN + "pages/tables-and-lists.txt",
            GOLDEN + "pages/tags-and-markup.txt");

    private static final String GENERATED_PAGE = "generated-page";

    static Stream<Arguments> cases()
    {
        List<Arguments> cases = new ArrayList<>();
        List<String> pages = new ArrayList<>(PAGES);
        pages.add(GENERATED_PAGE);
        for (String page : pages) {
            for (String config : List.of("en", "en-alt", "de")) {
                cases.add(Arguments.of(page, config));
            }
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("cases")
    void producesTheRecordedOutput(String page, String config) throws IOException
    {
        String text = page.equals(GENERATED_PAGE) ? generatePage() : readResource(page);
        String name = page.substring(page.lastIndexOf('/') + 1).replace(".txt", "");
        String expectedFile = GOLDEN + "expected/" + name + "." + config + ".txt";

        String actual = dump(parserFor(config), text, !config.equals("en-alt"));

        if (Boolean.getBoolean("golden.update")) {
            Path out = Path.of("src/test/resources", expectedFile);
            Files.createDirectories(out.getParent());
            Files.writeString(out, actual, StandardCharsets.UTF_8);
            return;
        }
        assertEquals(readResource(expectedFile), actual);
    }

    private static MediaWikiParser parserFor(String config)
    {
        MediaWikiParserFactory factory;
        switch (config) {
        case "en":
            factory = new MediaWikiParserFactory(Language.english);
            break;
        case "en-alt":
            factory = new MediaWikiParserFactory(Language.english);
            factory.setCalculateSrcSpans(true);
            factory.setShowImageText(true);
            factory.setDeleteTags(false);
            factory.setShowMathTagContent(false);
            break;
        case "de":
            factory = new MediaWikiParserFactory(Language.german);
            break;
        default:
            throw new IllegalArgumentException(config);
        }
        return factory.createParser();
    }

    /**
     * A page with many lines that each carry links and templates, a large table and a long list,
     * so that many content elements are matched against long page-wide lists.
     */
    private static String generatePage()
    {
        StringBuilder sb = new StringBuilder();
        for (int s = 0; s < 2; s++) {
            sb.append("== Section ").append(s).append(" ==\n");
            for (int i = 0; i < 30; i++) {
                sb.append("Line ").append(i).append(" links [[Target ").append(s).append('.')
                        .append(i).append("|text ").append(i).append("]] and {{tpl|")
                        .append(i).append("|[[Inner ").append(i).append("]]}} and ''it ")
                        .append(i).append("'' <span>[[Tagged ").append(i).append("]]</span>");
                if (i % 7 == 0) {
                    sb.append(" <math>x_").append(i).append("</math> <nowiki>[[n").append(i)
                            .append("]]</nowiki>");
                }
                sb.append('\n');
                if (i % 10 == 9) {
                    sb.append('\n');
                }
            }
            sb.append("{| class=\"wikitable\"\n");
            for (int r = 0; r < 12; r++) {
                sb.append("|-\n");
                for (int c = 0; c < 5; c++) {
                    sb.append(c == 0 ? "| " : " || ").append("[[Cell ").append(r).append('/')
                            .append(c).append("]] {{c|").append(c).append("}}");
                }
                sb.append('\n');
            }
            sb.append("|}\n");
            for (int i = 0; i < 20; i++) {
                sb.append(i % 3 == 2 ? "** " : "* ").append("Item [[Item ").append(i)
                        .append("]] {{i|").append(i).append("}}\n");
            }
            sb.append('\n');
        }
        sb.append("[[Category:Generated]] [[de:Generiert]]\n");
        return sb.toString();
    }

    private static String readResource(String name) throws IOException
    {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(name)) {
            if (in == null) {
                throw new IOException("Missing test resource: " + name);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String dump(MediaWikiParser parser, String text, boolean parseContentElement)
    {
        StringBuilder sb = new StringBuilder();
        ParsedPage pp = parser.parse(text);
        sb.append("FIRST_PARAGRAPH: ").append(pp.getFirstParagraphNr()).append('\n');
        sb.append("CATEGORIES:\n");
        dumpElement(sb, pp.getCategoryElement(), 1);
        sb.append("LANGUAGES:\n");
        dumpElement(sb, pp.getLanguagesElement(), 1);
        for (Section section : pp.getSections()) {
            dumpSection(sb, section, 0);
        }
        if (parseContentElement && parser instanceof ModularParser ceParser) {
            sb.append("CONTENT_ELEMENT:\n");
            dumpElement(sb, ceParser.parseContentElement(text), 1);
        }
        return sb.toString();
    }

    private static void dumpSection(StringBuilder sb, Section section, int depth)
    {
        line(sb, depth, section.getClass().getSimpleName() + " level=" + section.getLevel()
                + srcSpan(section));
        line(sb, depth + 1, "TITLE:");
        dumpElement(sb, section.getTitleElement(), depth + 2);
        if (section instanceof SectionContainer container) {
            for (Section sub : container.getSubSections()) {
                dumpSection(sb, sub, depth + 1);
            }
        }
        else {
            for (Content content : section.getContentList()) {
                dumpContent(sb, content, depth + 1);
            }
        }
    }

    private static void dumpContent(StringBuilder sb, Content content, int depth)
    {
        if (content instanceof ContentElement element) {
            dumpElement(sb, element, depth);
        }
        else if (content instanceof Table table) {
            line(sb, depth, "Table" + srcSpan(table));
            line(sb, depth + 1, "TITLE:");
            dumpElement(sb, table.getTitleElement(), depth + 2);
            for (int i = 0; i < table.nrOfTableElements(); i++) {
                TableElement te = table.getTableElement(i);
                line(sb, depth + 1, "TableElement row=" + te.getRow() + " col=" + te.getCol()
                        + srcSpan(te));
                dumpSection(sb, te.getSectionContainer(), depth + 2);
            }
        }
        else if (content instanceof NestedListContainer container) {
            line(sb, depth, "NestedListContainer numbered=" + container.isNumbered()
                    + srcSpan(container));
            for (NestedList nl : container.getNestedLists()) {
                dumpContent(sb, nl, depth + 1);
            }
        }
        else if (content instanceof DefinitionList dl) {
            line(sb, depth, "DefinitionList" + srcSpan(dl));
            line(sb, depth + 1, "TERM:");
            dumpElement(sb, dl.getDefinedTerm(), depth + 2);
            for (ContentElement definition : dl.getDefinitions()) {
                line(sb, depth + 1, "DEFINITION:");
                dumpElement(sb, definition, depth + 2);
            }
        }
        else {
            line(sb, depth, "UNKNOWN " + content.getClass().getName());
        }
    }

    private static void dumpElement(StringBuilder sb, ContentElement ce, int depth)
    {
        if (ce == null) {
            line(sb, depth, "null");
            return;
        }
        line(sb, depth, ce.getClass().getSimpleName() + srcSpan(ce) + " \"" + ce.getText() + "\"");
        for (FormatType type : FormatType.values()) {
            List<Span> spans = ce.getFormatSpans(type);
            if (!spans.isEmpty()) {
                StringBuilder f = new StringBuilder(type.toString()).append(':');
                for (Span s : spans) {
                    f.append(' ').append(s).append(srcSpan(s));
                }
                line(sb, depth + 1, f.toString());
            }
        }
        for (Link l : ce.getLinks()) {
            line(sb, depth + 1, "LINK " + l.getType() + " \"" + l.getTarget() + "\" "
                    + l.getPos() + srcSpan(l) + " \"" + l.getText() + "\" " + l.getParameters()
                    + (l.getHomeElement() == ce ? "" : " FOREIGN_HOME"));
        }
        for (Template t : ce.getTemplates()) {
            line(sb, depth + 1, "TEMPLATE \"" + t.getName() + "\" " + t.getPos() + srcSpan(t)
                    + " " + t.getParameters());
        }
    }

    private static String srcSpan(ParsedPageObject o)
    {
        return o.getSrcSpan() == null ? "" : " src=" + o.getSrcSpan();
    }

    private static void line(StringBuilder sb, int depth, String text)
    {
        sb.append("  ".repeat(depth)).append(text).append('\n');
    }
}
