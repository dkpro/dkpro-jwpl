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
package de.tudarmstadt.ukp.wikipedia.parser.mediawiki;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;

/**
 * This is an Interface for MediaWiki Parsers. Which simply "converts" 
 * MediaWiki Source, given as a String, to a ParsedPage
 * @author CJacobi
 *
 */
public interface MediaWikiParser {
	/**
	 * Parses MediaWiki Source, given as parameter src,  and returns a ParsedPage.
	 */
	public ParsedPage parse(String src);
	
	/**
	 * Retruns information abour the configuration of the parser.
	 */
	public String configurationInfo();
	
	/**
	 * Retruns the String which is uses as line separator, usually it 
	 * will be "\n" or "\r\n"
	 */
	public String getLineSeparator();
}
