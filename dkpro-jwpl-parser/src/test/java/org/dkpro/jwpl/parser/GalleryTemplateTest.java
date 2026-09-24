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

import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.dkpro.jwpl.parser.mediawiki.ShowTemplateNamesAndParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests that templates in a {@code <gallery>} and in the caption of an image are part of the
 * parsed page, wherever they are in the gallery or the paragraph, and that their replacement ends
 * up at their place in the text (see issue #733).
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

    private static List<String> templateNames(ParsedPage pp)
    {
        return pp.getTemplates().stream().map(Template::getName).toList();
    }

    private static List<String> paragraphTemplateNames(ParsedPage pp)
    {
        List<String> names = new ArrayList<>();
        for (Paragraph p : pp.getParagraphs()) {
            p.getTemplates().forEach(t -> names.add(t.getName()));
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

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    void keepsATemplateInAnyEntryOfAGalleryAfterAHeading(int entry)
    {
        ParsedPage pp = parser(false).parse(gallery("Intro.\n\n== Gallery ==\n", entry));

        assertEquals(of("T"), templateNames(pp));
        assertEquals(of("T"), paragraphTemplateNames(pp));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    void keepsATemplateInAnyEntryOfAGalleryAfterALineOfText(int entry)
    {
        ParsedPage pp = parser(false).parse(gallery("Intro.\n", entry));

        assertEquals(of("T"), templateNames(pp));
        assertEquals(of("T"), paragraphTemplateNames(pp));
    }

    @Test
    void keepsATemplateInAGalleryOfOneEntry()
    {
        ParsedPage pp = parser(false).parse("Intro.\n<gallery>\nA.jpg|{{T|x}}\n</gallery>\nEnd.");

        assertEquals(of("T"), templateNames(pp));
        assertEquals("Intro.\nTEMPLATE[T, x] End.", pp.getText());
    }

    @Test
    void putsTheReplacementOfATemplateInTheCaptionOfAGalleryAtItsPlace()
    {
        ParsedPage pp = parser(false)
                .parse("Intro.\n<gallery caption=\"Cap {{C|c}}\">\nA.jpg|a\n</gallery>\nEnd.");

        assertEquals(of("C"), templateNames(pp));
        assertEquals("Intro.\nCap TEMPLATE[C, c]\n End.", pp.getText());
    }

    @Test
    void putsTheReplacementOfATemplateInAGalleryAtItsPlaceWhenImageTextIsShown()
    {
        ParsedPage pp = parser(true).parse(gallery("Intro.\n", 1));

        assertEquals(of("T"), templateNames(pp));
        assertEquals("Intro.\ncap0\nTEMPLATE[T, x]\ncap2 End.", pp.getText());
    }

    @Test
    void keepsATemplateInTheCaptionOfAnImageAtTheStartOfAParagraph()
    {
        ParsedPage pp = parser(false).parse("[[Image:A.jpg|thumb|A {{T|x}} B]]\nText.");

        assertEquals(of("T"), templateNames(pp));
        assertEquals("TEMPLATE[T, x]\nText.", pp.getText());
    }

    @Test
    void keepsTheTextOfAnImageWithATemplateInsideAParagraph()
    {
        ParsedPage pp = parser(false).parse("Intro [[Image:A.jpg|thumb|A {{T|x}} B]] more.");

        assertEquals(of("T"), templateNames(pp));
        assertEquals("Intro TEMPLATE[T, x] more.", pp.getText());
    }
}
