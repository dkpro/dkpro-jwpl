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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.dkpro.jwpl.api.Page;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.api.Wikipedia;
import org.dkpro.jwpl.parser.mediawiki.FlushTemplates;
import org.dkpro.jwpl.parser.mediawiki.GermanTemplateParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiTemplateParser;
import org.dkpro.jwpl.parser.mediawiki.ShowTemplateNamesAndParameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests that {@link MediaWikiParser#parseTemplatesOnly(String)} returns the template names of a
 * full {@link MediaWikiParser#parse(String)}, plus those the full parse drops.
 */
class TemplatesOnlyTest
    extends BaseJWPLTest
{

    private static final Map<String, String> PAGES = new LinkedHashMap<>();

    @BeforeAll
    static void loadPages() throws Exception
    {
        PAGES.put("nested", "Text {{Outer|a={{Inner|x}}|b}} and {{Other}} more.\n");
        PAGES.put("masked", """
                Before <!-- {{InComment}} --> after.
                <nowiki>{{InNowiki}}</nowiki> and <pre>{{InPre}}</pre>
                <math>{{InMath}}</math> but {{Visible}}.
                """);
        PAGES.put("links", """
                {{Infobox|name=[[Link|Text]]}}
                Some [[link]] and [[Image:Pic.jpg|thumb|{{InImage}}]].

                [[Category:Test|{{InCategory}}]]
                [[de:Test]] {{OnLanguageLine}}
                """);
        PAGES.put("gallery-and-table", """
                == Section {{InHeading}} ==
                <gallery>
                File:A.jpg|{{InGallery}}
                </gallery>
                {| class="wikitable"
                |-
                | {{InCell}} || cell
                |}
                * item {{InList}}
                <ref>{{Cite web|url=x}}</ref>
                """);
        PAGES.put("nested-multiline", """
                {{Outer
                | a = {{Inner|x}}
                | b = y
                }}
                Text {{After}}
                """);
        PAGES.put("unclosed", "{{Open|a {{Closed}} and text\n");
        PAGES.put("americium", Files.readString(getResource("pages/Wiki-Article-Americium.txt"),
                StandardCharsets.UTF_8));

        Wikipedia wiki = new Wikipedia(obtainHSQLDBConfiguration());
        for (Page page : wiki.getPages()) {
            PAGES.put("db:" + page.getPageId(), page.getText());
        }
    }

    static Stream<Arguments> parsers()
    {
        List<Arguments> arguments = new ArrayList<>();
        for (Language language : List.of(Language.english, Language.german)) {
            for (Class<? extends MediaWikiTemplateParser> templateParser : List.of(
                    ShowTemplateNamesAndParameters.class, FlushTemplates.class,
                    GermanTemplateParser.class)) {
                arguments.add(Arguments.of(language, templateParser));
            }
        }
        return arguments.stream();
    }

    private static MediaWikiParser createParser(Language language,
            Class<? extends MediaWikiTemplateParser> templateParser)
    {
        MediaWikiParserFactory factory = new MediaWikiParserFactory(language);
        factory.setTemplateParserClass(templateParser);
        return factory.createParser();
    }

    private static Set<String> names(List<Template> templates)
    {
        Set<String> names = new TreeSet<>();
        for (Template template : templates) {
            names.add(template.getName());
        }
        return names;
    }

    private static Set<String> namesOfFullParse(MediaWikiParser parser, String text)
    {
        ParsedPage pp = parser.parse(text);
        return pp == null ? Set.of() : names(pp.getTemplates());
    }

    /**
     * The full parse drops some templates, as it does not attach them to any content element of
     * the page: those nested in a template whose remaining text is long enough that the nested
     * span collapses to an empty one when the outer template is replaced, those in the text of a
     * category link, and those in a gallery. Every other template is returned by both.
     */
    @ParameterizedTest
    @MethodSource("parsers")
    void returnsTheTemplateNamesOfTheFullParse(Language language,
            Class<? extends MediaWikiTemplateParser> templateParser)
    {
        MediaWikiParser parser = createParser(language, templateParser);
        for (Map.Entry<String, String> page : PAGES.entrySet()) {
            Set<String> full = namesOfFullParse(parser, page.getValue());
            Set<String> templatesOnly = names(parser.parseTemplatesOnly(page.getValue()));

            assertTrue(templatesOnly.containsAll(full),
                    "Templates of the full parse missing for page " + page.getKey());
        }
    }

    @Test
    void returnsTheTemplatesTheFullParseDrops()
    {
        MediaWikiParser english = createParser(Language.english,
                ShowTemplateNamesAndParameters.class);
        MediaWikiParser german = createParser(Language.german,
                ShowTemplateNamesAndParameters.class);

        String nested = PAGES.get("nested-multiline");
        assertEquals(Set.of("Outer", "After"), namesOfFullParse(english, nested));
        assertEquals(Set.of("Outer", "Inner", "After"),
                names(english.parseTemplatesOnly(nested)));

        String links = PAGES.get("links");
        assertEquals(Set.of("Infobox", "InImage", "OnLanguageLine"),
                namesOfFullParse(english, links));
        assertEquals(Set.of("Infobox", "InImage", "InCategory", "OnLanguageLine"),
                names(english.parseTemplatesOnly(links)));

        String gallery = PAGES.get("gallery-and-table");
        assertEquals(Set.of("InHeading", "InCell", "InList", "Cite_web"),
                namesOfFullParse(german, gallery));
        assertEquals(Set.of("InHeading", "InGallery", "InCell", "InList", "Cite_web"),
                names(german.parseTemplatesOnly(gallery)));
    }

    @Test
    void masksCommentsNowikiPreAndMath()
    {
        MediaWikiParser parser = createParser(Language.english,
                ShowTemplateNamesAndParameters.class);

        Set<String> names = names(parser.parseTemplatesOnly(PAGES.get("masked")));

        assertEquals(Set.of("Visible"), names);
    }

    @Test
    void returnsNestedTemplatesAndTemplatesInLinksAndGalleries()
    {
        MediaWikiParser parser = createParser(Language.english,
                ShowTemplateNamesAndParameters.class);

        assertEquals(Set.of("Outer", "Inner", "Other"),
                names(parser.parseTemplatesOnly(PAGES.get("nested"))));
        assertEquals(Set.of("Infobox", "InImage", "InCategory", "OnLanguageLine"),
                names(parser.parseTemplatesOnly(PAGES.get("links"))));
        assertEquals(Set.of("InHeading", "InGallery", "InCell", "InList", "Cite_web"),
                names(parser.parseTemplatesOnly(PAGES.get("gallery-and-table"))));
    }

    @Test
    void theTestPagesContainTemplates()
    {
        MediaWikiParser parser = createParser(Language.english,
                ShowTemplateNamesAndParameters.class);

        assertFalse(names(parser.parseTemplatesOnly(PAGES.get("americium"))).isEmpty());
        long dbPagesWithTemplates = PAGES.entrySet().stream()
                .filter(page -> page.getKey().startsWith("db:"))
                .filter(page -> !parser.parseTemplatesOnly(page.getValue()).isEmpty()).count();
        assertTrue(dbPagesWithTemplates > 0);
    }

    @Test
    void returnsAnEmptyListForEmptyText()
    {
        MediaWikiParser parser = createParser(Language.english,
                ShowTemplateNamesAndParameters.class);

        assertTrue(parser.parseTemplatesOnly(null).isEmpty());
        assertTrue(parser.parseTemplatesOnly("").isEmpty());
    }

    @Test
    void defaultMethodReturnsTheTemplatesOfTheFullParse()
    {
        MediaWikiParser delegate = createParser(Language.english,
                ShowTemplateNamesAndParameters.class);
        MediaWikiParser parser = new MediaWikiParser()
        {
            @Override
            public ParsedPage parse(String src)
            {
                return delegate.parse(src);
            }

            @Override
            public String configurationInfo()
            {
                return delegate.configurationInfo();
            }

            @Override
            public String getLineSeparator()
            {
                return delegate.getLineSeparator();
            }
        };

        assertEquals(Set.of("Outer", "Inner", "Other"),
                names(parser.parseTemplatesOnly(PAGES.get("nested"))));
        assertTrue(parser.parseTemplatesOnly("").isEmpty());
    }

    private static Path getResource(String r) throws Exception
    {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(r);
        return Path.of(resource.toURI());
    }
}
