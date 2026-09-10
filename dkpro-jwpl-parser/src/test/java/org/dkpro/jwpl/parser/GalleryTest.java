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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests that the files listed in a {@code <gallery>} are parsed as images. The lines of a gallery
 * name files without the namespace prefix a link outside a gallery needs, which used to make every
 * one of them an internal link to an article of that name (see issue #151).
 */
class GalleryTest
{

    private static MediaWikiParser parserFor(Language language)
    {
        return new MediaWikiParserFactory(language).createParser();
    }

    @Test
    void parsesTheEntriesOfAGalleryAsImages()
    {
        String text = """
                A staircase.

                == Gallery ==
                <gallery heights="150" widths="150">
                Jordantreppe Petersburg Eremitage 02.JPG
                Hermitage staicase.jpg|The staircase seen from below
                </gallery>
                """;

        List<Link> links = parserFor(Language.english).parse(text).getLinks();

        assertEquals(2, links.size());
        for (Link link : links) {
            assertEquals(Link.type.IMAGE, link.getType(),
                    "'" + link.getTarget() + "' is not parsed as an image");
        }
        assertEquals("Image:Jordantreppe_Petersburg_Eremitage_02.JPG", links.get(0).getTarget());
        assertEquals("Image:Hermitage_staicase.jpg", links.get(1).getTarget());
    }

    @Test
    void keepsTheNamespaceOfAnEntryThatCarriesOne()
    {
        String text = """
                <gallery>
                File:With prefix.jpg
                Without prefix.jpg
                </gallery>
                """;

        List<Link> links = parserFor(Language.english).parse(text).getLinks();

        assertEquals(2, links.size());
        assertEquals("File:With_prefix.jpg", links.get(0).getTarget());
        assertEquals("Image:Without_prefix.jpg", links.get(1).getTarget());
        assertTrue(links.stream().allMatch(l -> l.getType() == Link.type.IMAGE));
    }

    @Test
    void prefixesTheEntriesOfAGalleryWithTheIdentifierOfTheLanguage()
    {
        String text = """
                <gallery>
                Ohne Präfix.jpg
                Datei:Mit Präfix.jpg
                </gallery>
                """;

        List<Link> links = parserFor(Language.german).parse(text).getLinks();

        assertEquals(2, links.size());
        assertEquals("Bild:Ohne_Präfix.jpg", links.get(0).getTarget());
        assertEquals("Datei:Mit_Präfix.jpg", links.get(1).getTarget());
        assertTrue(links.stream().allMatch(l -> l.getType() == Link.type.IMAGE));
    }
}
