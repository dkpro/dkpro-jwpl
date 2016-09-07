/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.parser.tutorial;

import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

/**
 * This is a little demo, to show how the parsedpage and parsedpage.parser package
 * works.
 *
 */

public class T3_LinkContexts {

    public static void main(String[] args){
		
        // load a sample document (the contents are equal to "DarmstadtWikipediaArticle.txt")
        String documentText = TestFile.getFileText();

        // get a ParsedPage object
        MediaWikiParserFactory pf = new MediaWikiParserFactory();
        MediaWikiParser parser = pf.createParser();
        ParsedPage pp = parser.parse(documentText);
		
		// Link Context (return 1 token left, 2 token right of the link)
        for (Link link : pp.getLinks()) {
            System.out.println(
                link.getContext(1, 0) + "<" +
                link.getText().toString().toUpperCase() + ">" +
				link.getContext(0, 2)
            );
        }
    }
}
