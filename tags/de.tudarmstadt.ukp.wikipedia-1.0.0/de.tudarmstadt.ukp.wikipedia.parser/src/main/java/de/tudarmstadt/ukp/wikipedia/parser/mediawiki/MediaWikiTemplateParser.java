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
import de.tudarmstadt.ukp.wikipedia.parser.Template;

/**
 * Because template parsing is a special task, it is usesfull to use
 * a special parser.
 * @author CJacobi
 *
 */
public interface MediaWikiTemplateParser {
	
	/**
	 * Takes a Template and do whatever is required for handling this Template.
	 * It is possible to delete this template, to parse it to e.g a Link or 
	 * to return MediaWiki code which can be parsed by a MediaWiki parser.<br/>
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
