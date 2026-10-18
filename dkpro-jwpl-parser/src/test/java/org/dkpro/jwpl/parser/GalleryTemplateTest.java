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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.dkpro.jwpl.parser.mediawiki.ShowTemplateNamesAndParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests that templates in a {@code <gallery>} and in the caption of an image are part of the
 * parsed page, wherever they are in the gallery or the paragraph, and that their replacement ends
 * up at their place in the text.
 */
class GalleryTemplateTest
{

    private static MediaWikiParser parser(boolean showImageText)
    {
        MediaWikiParserFactory factory = new MediaWikiParserFactory(Language.english);
        factory.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
        factory.setShowImageText(showImageText);
        return factory.createParser();
    }

    private static List<String> names(List<Template> templates)
    {
        return templates.stream().map(Template::getName).toList();
    }

    private static List<String> paragraphTemplateNames(ParsedPage pp)
    {
        List<String> names = new ArrayList<>();
        for (Paragraph p : pp.getParagraphs()) {
            names.addAll(names(p.getTemplates()));
        }
        return names;
    }

    private static String gallery(String before, int entryWithTemplate)
    {
        StringBuilder sb = new StringBuilder(before).append("<gallery>\n");
        for (int i = 0; i < 3; i++) {
            sb.append("F").append(i).append(".jpg|")
                    .append(i == entryWithTemplate ? "{{T|x}}" : "cap" + i).append('\n');
        }
        return sb.append("</gallery>\nEnd.").toString();
    }

    static Stream<Arguments> galleries()
    {
        return Stream.of("Intro.\n\n== Gallery ==\n", "Intro.\n")
                .flatMap(before -> IntStream.range(0, 3)
                        .mapToObj(entry -> Arguments.of(gallery(before, entry))));
    }

    @ParameterizedTest
    @MethodSource("galleries")
    void keepsATemplateInAnyEntryOfAGallery(String wikitext)
    {
        ParsedPage pp = parser(false).parse(wikitext);

        assertEquals(of("T"), names(pp.getTemplates()));
        assertEquals(of("T"), paragraphTemplateNames(pp));
    }

    @Test
    void keepsATemplateInAGalleryOfOneEntry()
    {
        ParsedPage pp = parser(false).parse("Intro.\n<gallery>\nA.jpg|{{T|x}}\n</gallery>\nEnd.");

        assertEquals(of("T"), names(pp.getTemplates()));
        assertEquals("Intro.\nTEMPLATE[T, x] End.", pp.getText());
    }

    @Test
    void keepsATemplateNestedInAMultiLineTemplateInAGalleryEntry()
    {
        ParsedPage pp = parser(false).parse("""
                Intro.
                <gallery>
                A.jpg|{{Outer
                | a = {{Inner|x}}
                | b = y
                }}
                B.jpg|b
                </gallery>
                End.""");

        assertEquals(of("Outer", "Inner"), names(pp.getTemplates()));
        assertEquals(of("Outer", "Inner"), names(pp.getParagraph(0).getTemplates()));
        assertEquals("Intro.\nTEMPLATE[Outer, a = " + TEMPLATESPACER + ", b = y]\n End.",
                pp.getText());
    }

    @Test
    void putsTheReplacementOfATemplateInTheCaptionOfAGalleryAtItsPlace()
    {
        ParsedPage pp = parser(false)
                .parse("Intro.\n<gallery caption=\"Cap {{C|c}}\">\nA.jpg|a\n</gallery>\nEnd.");

        assertEquals(of("C"), names(pp.getTemplates()));
        assertEquals("Intro.\nCap TEMPLATE[C, c]\n End.", pp.getText());
    }

    @Test
    void putsTheReplacementOfATemplateInAGalleryAtItsPlaceWhenImageTextIsShown()
    {
        ParsedPage pp = parser(true).parse(gallery("Intro.\n", 1));

        assertEquals(of("T"), names(pp.getTemplates()));
        assertEquals("Intro.\ncap0\nTEMPLATE[T, x]\ncap2 End.", pp.getText());
    }

    @Test
    void dropsTheReplacementOfATemplateInTheGalleryTagOutsideTheCaption()
    {
        ParsedPage pp = parser(true).parse("Intro.\n<gallery {{T|x}}>\nA.jpg|a\n</gallery>\nEnd.");

        assertEquals("Intro.\na End.", pp.getText());
    }

    @ParameterizedTest
    @CsvSource(delimiterString = "=>", value = {
            "[[Image:A.jpg|thumb|A {{T|x}} B]] Text.        => TEMPLATE[T, x] Text.",
            "Intro [[Image:A.jpg|thumb|A {{T|x}} B]] more.  => Intro TEMPLATE[T, x] more.",
            "Text [[Image:A.jpg|thumb|A {{T|x}} B]]         => Text TEMPLATE[T, x]" })
    void keepsATemplateInTheCaptionOfAnImage(String wikitext, String text)
    {
        ParsedPage pp = parser(false).parse(wikitext);

        assertEquals(of("T"), names(pp.getTemplates()));
        assertEquals(of("T"), names(pp.getParagraph(0).getTemplates()));
        assertEquals(text, pp.getText());
    }

    @Test
    void keepsATemplateNestedInAMultiLineTemplateInTheCaptionOfAnImage()
    {
        ParsedPage pp = parser(false).parse("""
                [[Image:A.jpg|thumb|A {{Outer
                | a = {{Inner|x}}
                | b = y
                }} B]]
                Text.""");

        assertEquals(of("Outer", "Inner"), names(pp.getTemplates()));
        assertEquals(of("Outer", "Inner"), names(pp.getParagraph(0).getTemplates()));
        assertEquals("TEMPLATE[Outer, a = " + TEMPLATESPACER + ", b = y]\nText.", pp.getText());
    }
}
