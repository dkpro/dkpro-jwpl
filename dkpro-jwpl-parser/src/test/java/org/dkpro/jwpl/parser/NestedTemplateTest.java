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

import static java.util.List.of;
import static org.dkpro.jwpl.parser.mediawiki.ResolvedTemplate.TEMPLATESPACER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.dkpro.jwpl.parser.mediawiki.ShowTemplateNamesAndParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests that templates nested in another template are part of the parsed page, no matter whether
 * the enclosing template spans one line or several lines.
 */
class NestedTemplateTest
{

    /** Templates of the Americium page which are not nested in another template. */
    private static final int AMERICIUM_TOP_LEVEL_TEMPLATES = 13;

    /** Templates nested in the infobox and in the isotope table of the Americium page. */
    private static final int AMERICIUM_NESTED_TEMPLATES = 25;

    /** {@code ZahlExp} is used ten times inside the infobox of the Americium page. */
    private static final int AMERICIUM_ZAHLEXP_TEMPLATES = 10;

    /** The isotope table of the Americium page uses one isotope template and eight nested ones. */
    private static final int AMERICIUM_ISOTOPE_TEMPLATES = 1 + 8;

    private static MediaWikiParser parser(Language language)
    {
        MediaWikiParserFactory factory = new MediaWikiParserFactory(language);
        factory.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
        return factory.createParser();
    }

    private static List<String> names(List<Template> templates)
    {
        return templates.stream().map(Template::getName).toList();
    }

    static Stream<Arguments> issueReproductions()
    {
        return Stream.of(
                Arguments.of("single-line", "{{Outer|a={{Inner|x}}|b=y}}\nText."),
                Arguments.of("multi-line at the start of a paragraph", """
                        {{Outer
                        | a = {{Inner|x}}
                        | b = y
                        }}
                        Text."""),
                Arguments.of("multi-line in the middle of a paragraph", """
                        Some text {{Outer
                        | a = {{Inner|x}}
                        | b = y
                        }} more."""));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("issueReproductions")
    void keepsANestedTemplate(String description, String wikitext)
    {
        ParsedPage pp = parser(Language.english).parse(wikitext);

        assertEquals(of("Outer", "Inner"), names(pp.getTemplates()));
        assertEquals(of("Outer", "Inner"), names(pp.getParagraph(0).getTemplates()));
    }

    @Test
    void keepsTheTextOfAMultiLineTemplateWithANestedTemplate()
    {
        ParsedPage pp = parser(Language.english).parse("""
                {{Outer
                | a = {{Inner|x}}
                | b = y
                }}
                Text.""");

        assertEquals("TEMPLATE[Outer, a = " + TEMPLATESPACER + ", b = y]\nText.", pp.getText());
    }

    @Test
    void keepsTemplatesNestedOnSeveralLevelsOfMultiLineTemplates()
    {
        ParsedPage pp = parser(Language.english).parse("""
                {{A
                | x = {{B
                  | y = {{C|z}}
                  | w = v
                  }}
                | u = t
                }}
                Text.""");

        assertEquals(of("A", "B", "C"), names(pp.getTemplates()));
    }

    @Test
    void doesNotTreatTheNextTemplateAsNested()
    {
        ParsedPage pp = parser(Language.english).parse("""
                {{Outer
                | a = {{Inner|x}}
                | b = y
                }} {{Next}}
                Text.""");

        assertEquals(of("Outer", "Inner", "Next"), names(pp.getTemplates()));
        assertEquals(
                "TEMPLATE[Outer, a = " + TEMPLATESPACER + ", b = y] TEMPLATE[Next]\nText.",
                pp.getText());
    }

    @Test
    void keepsTheTemplatesNestedInTheInfoboxOfAnArticle() throws Exception
    {
        String text = BaseJWPLTest.readResource("pages/Wiki-Article-Americium.txt");

        List<String> names = names(parser(Language.german).parse(text).getTemplates());

        for (String nested : of("CASRN", "ZahlExp", "NIST-ASD", "Webelements", "GHS-Piktogramme",
                "H-Sätze", "EUH-Sätze", "P-Sätze")) {
            assertTrue(names.contains(nested), nested + " is missing in " + names);
        }
        assertEquals(AMERICIUM_ZAHLEXP_TEMPLATES,
                names.stream().filter("ZahlExp"::equals).count());
        assertEquals(AMERICIUM_ISOTOPE_TEMPLATES,
                names.stream().filter("Infobox_Chemisches_Element/Isotop"::equals).count());
        assertEquals(AMERICIUM_TOP_LEVEL_TEMPLATES + AMERICIUM_NESTED_TEMPLATES, names.size());
    }
}
