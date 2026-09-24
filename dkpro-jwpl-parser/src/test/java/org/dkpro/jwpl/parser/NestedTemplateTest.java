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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.dkpro.jwpl.parser.mediawiki.ShowTemplateNamesAndParameters;
import org.junit.jupiter.api.Test;

/**
 * Tests that templates nested in another template are part of the parsed page, no matter whether
 * the enclosing template spans one line or several lines.
 */
class NestedTemplateTest
{

    private static MediaWikiParser parser(Language language)
    {
        MediaWikiParserFactory factory = new MediaWikiParserFactory(language);
        factory.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
        return factory.createParser();
    }

    private static List<String> templateNames(ParsedPage pp)
    {
        return pp.getTemplates().stream().map(Template::getName).toList();
    }

    @Test
    void keepsATemplateNestedInASingleLineTemplate()
    {
        ParsedPage pp = parser(Language.english).parse("{{Outer|a={{Inner|x}}|b=y}}\nText.");

        assertEquals(of("Outer", "Inner"), templateNames(pp));
    }

    @Test
    void keepsATemplateNestedInAMultiLineTemplate()
    {
        ParsedPage pp = parser(Language.english).parse("""
                {{Outer
                | a = {{Inner|x}}
                | b = y
                }}
                Text.""");

        assertEquals(of("Outer", "Inner"), templateNames(pp));
        assertEquals(of("Outer", "Inner"),
                pp.getParagraph(0).getTemplates().stream().map(Template::getName).toList());
        assertEquals("TEMPLATE[Outer, a = (TEMPLATE), b = y]\nText.", pp.getText());
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

        assertEquals(of("A", "B", "C"), templateNames(pp).stream().sorted().toList());
    }

    @Test
    void keepsTheTemplatesNestedInTheInfoboxOfAnArticle() throws Exception
    {
        String text;
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("pages/Wiki-Article-Americium.txt")) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<String> names = templateNames(parser(Language.german).parse(text));

        for (String nested : of("CASRN", "ZahlExp", "NIST-ASD", "Webelements", "GHS-Piktogramme",
                "H-Sätze", "EUH-Sätze", "P-Sätze")) {
            assertTrue(names.contains(nested), nested + " is missing in " + names);
        }
        assertEquals(10, names.stream().filter("ZahlExp"::equals).count());
        assertEquals(9,
                names.stream().filter("Infobox_Chemisches_Element/Isotop"::equals).count());
        assertEquals(38, names.size());
    }
}
