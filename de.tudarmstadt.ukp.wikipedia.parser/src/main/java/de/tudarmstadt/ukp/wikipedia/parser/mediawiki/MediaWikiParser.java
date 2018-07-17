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
package de.tudarmstadt.ukp.wikipedia.parser.mediawiki;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;

/**
 * This is an Interface for MediaWiki Parsers. Which simply "converts"
 * MediaWiki Source, given as a String, to a ParsedPage
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
