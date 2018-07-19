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
import de.tudarmstadt.ukp.wikipedia.parser.Template;

/**
 * Because template parsing is a special task, it is usesfull to use
 * a special parser.
 *
 */
public interface MediaWikiTemplateParser {
	
	/**
	 * Takes a Template and do whatever is required for handling this Template.
	 * It is possible to delete this template, to parse it to e.g a Link or
	 * to return MediaWiki code which can be parsed by a MediaWiki parser.<br>
	 * If you are interested how this works, you shoud read the documentation
	 * of ResolvedTemplate.
	 */
	public ResolvedTemplate parseTemplate(Template t, ParsedPage pp);
	
	/**
	 * Returns some information about what the TemplateParser does am how
	 * it is configurated.
	 */
	public String configurationInfo();
}
