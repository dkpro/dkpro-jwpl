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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Template;

/**
 * This is the TemplateParser for the german language, with special treatment
 * for all the german templates, like "Dieser Artikel" or "Deutschlandlastig".
 * @author CJacobi
 *
 */
public class GermanTemplateParser implements MediaWikiTemplateParser {

	private final Log logger = LogFactory.getLog(getClass());

	private final String templatePrefix = "TEMPLATE[";
	private final String templatePostfix = "]";
	private final String parameterDivisor = ", ";
	private final String templateNotImplementedPrefix = "TEMPLATE NOT IMPLEMENTED[";
	private final String templateNotImplementedPostfix = "]";
	private final String emptyLinkText = "[ ]";

//	private MediaWikiContentElementParser parser;
	private List<String> deleteTemplates;
	private List<String> parseTemplates;

	public GermanTemplateParser(MediaWikiContentElementParser parser, List<String> deleteTemplates, List<String> parseTemplates){
		this.deleteTemplates = deleteTemplates;
		this.parseTemplates = parseTemplates;
//		this.parser = parser;
	}

	public String configurationInfo(){
		StringBuilder result = new StringBuilder();
		result.append("Standard Template treatment: ShowNameAndParameters");
		result.append("\nDelete Templates: ");
		for( String s: deleteTemplates ) {
			result.append( "\""+s+"\" ");
		}
		result.append("\nParse Templates: ");
		for( String s: parseTemplates ) {
			result.append( "\""+s+"\" ");
		}
		return result.toString();
	}

	public ResolvedTemplate parseTemplate(Template t, ParsedPage pp) {

		final String templateName = t.getName();

		//Show Name and Parameters as Standart treatment.
		ResolvedTemplate result = new ResolvedTemplate( t );
		result.setPreParseReplacement( ResolvedTemplate.TEMPLATESPACER );
		StringBuilder sb = new StringBuilder();
		sb.append(templatePrefix);
		sb.append( t.getName() + parameterDivisor );
		for( String s: t.getParameters()) {
			sb.append( s + parameterDivisor );
		}
		sb.delete( sb.length() - parameterDivisor.length(), sb.length() );
		sb.append(templatePostfix);
		result.setPostParseReplacement( sb.toString() );

		result.setParsedObject( t );

		//Delete Template if it is in the List
		for( String s: deleteTemplates ){
			if( s.equals(templateName) ){
				result.setPostParseReplacement( "" );
				result.setParsedObject( null );
				return result;
			}
		}

		//Parse Template if it is in the List
		for( String s: parseTemplates ){
			List<String> templateParameters = t.getParameters();

			if( s.equals(templateName)){
				logger.info("ParseTemplate: " + templateName);
				if( templateName.equals("Dieser Artikel")){

// I removed that from the core API, as it is not likely to be present in most non-German articles. (TZ)
//					pp.setAboutArticle( parser.parseContentElement( templateParameters.get(0) ));

					result.setPostParseReplacement("");
					result.setParsedObject( null );
					return result;
				}
				else if( templateName.equals("Audio") || templateName.equals("Audio genau")){
					if( templateParameters.size() == 0 ) {
						break;
					}
					if( templateParameters.size() == 1 ) {
						templateParameters.add( emptyLinkText );
					}
					result.setPostParseReplacement( t.getParameters().get(1) );
					result.setParsedObject( new Link(null, t.getPos() , templateParameters.get(0), Link.type.AUDIO, null ) );

					return result;
				}
				else if( templateName.equals("Video")){
					if( templateParameters.size() == 0 ) {
						break;
					}
					if( templateParameters.size() == 1 ) {
						templateParameters.add( emptyLinkText );
					}
					result.setPostParseReplacement(t.getParameters().get(1));
					result.setParsedObject( new Link(null, t.getPos(), t.getParameters().get(0), Link.type.VIDEO, null ) );
					return result;
				}
				else{
					result.setPostParseReplacement( templateNotImplementedPrefix+  templateName + templateNotImplementedPostfix );
					return result;
				}
			}
		}

		return result;
	}
}
