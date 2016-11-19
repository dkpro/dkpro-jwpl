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
package de.tudarmstadt.ukp.wikipedia.parser.html;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.parser.tutorial.TestFile;

/**
 * This Class shows how to use the HtmlTools.class...<br>
 * Mainly, you can create an HtmlFile of a ParsedPage.
 *
 */
public class HtmlFileDemo {
	
	public static void main( String[] argv ) throws Exception{
		
        // load a sample document (the contents are equal to "DarmstadtWikipediaArticle.txt")
        String documentText = TestFile.getFileText();

		// set up an individually parametrized MediaWikiParser
		MediaWikiParserFactory pf = new MediaWikiParserFactory();
		pf.getImageIdentifers().add("Image");
		MediaWikiParser parser = pf.createParser();
		
		ParsedPage pp = parser.parse( documentText );
		
        String outFileName = "htmlFileDemo.html";
		HtmlWriter.writeFile(outFileName, "UTF8", HtmlWriter.parsedPageToHtml(pp));

        System.out.println("Writing output to file: " + outFileName);
	}
}
