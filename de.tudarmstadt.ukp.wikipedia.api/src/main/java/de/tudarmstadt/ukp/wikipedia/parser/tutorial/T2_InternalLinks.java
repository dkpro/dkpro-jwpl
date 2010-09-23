/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.parser.tutorial;

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
