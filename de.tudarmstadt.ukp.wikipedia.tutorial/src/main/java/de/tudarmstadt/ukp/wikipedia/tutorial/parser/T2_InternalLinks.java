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
package de.tudarmstadt.ukp.wikipedia.tutorial.parser;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

/**
 * This class shows how to get the internal links from a parsed page.<br>
 * Internal links point to other pages and categories in the current<br>
 * <pre>Wikipedia</pre>.
 *
 */
public class T2_InternalLinks {

	/**
	 * Prints the targets of the internal links found in the page <i>Germany</i>.
	 * @param args
	 * @throws WikiApiException
	 */
	public static void main(String[] args) throws WikiApiException {

        // load a sample document (the contents are equal to "DarmstadtWikipediaArticle.txt")
        String documentText = TestFile.getFileText();
		
		// get a ParsedPage object
		MediaWikiParserFactory pf = new MediaWikiParserFactory();
		MediaWikiParser parser = pf.createParser();
		ParsedPage pp = parser.parse(documentText);
		
        // only the links to other Wikipedia language editions
        for (Link language : pp.getLanguages()) {
            System.out.println(language.getTarget());
        }

        //get the internal links of each section
        for (Section section : pp.getSections()){
            System.out.println("Section: " + section.getTitle());

            for (Link link : section.getLinks(Link.type.INTERNAL)) {
                System.out.println("  " + link.getTarget());
            }
        }
    }
}
