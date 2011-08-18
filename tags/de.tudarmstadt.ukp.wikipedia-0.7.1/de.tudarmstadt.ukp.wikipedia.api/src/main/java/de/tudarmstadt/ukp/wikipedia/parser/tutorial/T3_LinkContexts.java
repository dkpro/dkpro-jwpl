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

import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

/**
 * This is a little demo, to show how the parsedpage and parsedpage.parser package
 * works.
 * @author CJacobi
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
