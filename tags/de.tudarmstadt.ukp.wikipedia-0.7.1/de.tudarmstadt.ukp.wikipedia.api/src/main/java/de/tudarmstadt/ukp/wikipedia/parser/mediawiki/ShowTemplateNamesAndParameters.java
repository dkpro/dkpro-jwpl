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
 * This TemplateParser simply shows the name of the Template with all
 * parameters, without any exception.
 * @author CJacobi
 *
 */
public class ShowTemplateNamesAndParameters implements MediaWikiTemplateParser {

	private final String templatePrefix = "TEMPLATE[";
	private final String templatePostfix = "]";
	private final String parameterDivisor = ", ";
	
	public ResolvedTemplate parseTemplate(Template t, ParsedPage pp) {
		ResolvedTemplate result = new ResolvedTemplate( t );
		result.setPreParseReplacement( ResolvedTemplate.TEMPLATESPACER );
		
		StringBuilder sb = new StringBuilder();
		sb.append(templatePrefix);
		sb.append( t.getName()+parameterDivisor );
		for( String s: t.getParameters()){
			sb.append( s +parameterDivisor );
		}
		sb.delete( sb.length()-parameterDivisor.length(), sb.length() );
		sb.append(templatePostfix);
		result.setPostParseReplacement( sb.toString() );
		
		result.setParsedObject( t );
		return result;
	}
	
	public String configurationInfo(){
		return "shows the Template names and all parameters";
	}
}
