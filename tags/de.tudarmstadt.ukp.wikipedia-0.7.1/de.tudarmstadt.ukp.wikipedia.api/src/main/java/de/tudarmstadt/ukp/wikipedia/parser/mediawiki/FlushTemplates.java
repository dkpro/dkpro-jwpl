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
 * This TemplateParser will delete ALL templates, whitout any exception!
 * @author CJacobi
 *
 */
public final class FlushTemplates implements MediaWikiTemplateParser {

	public ResolvedTemplate parseTemplate(Template t, ParsedPage pp) {
		ResolvedTemplate result = new ResolvedTemplate( t );
		result.setPreParseReplacement( ResolvedTemplate.TEMPLATESPACER );
		result.setPostParseReplacement( "" );
		result.setParsedObject( null );
		return result;
	}
	
	public String configurationInfo(){
		return "All Templates will be Deleted";
	}
}
