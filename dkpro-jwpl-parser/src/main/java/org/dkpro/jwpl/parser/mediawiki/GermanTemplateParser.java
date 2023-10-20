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
package org.dkpro.jwpl.parser.mediawiki;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.dkpro.jwpl.parser.Link;
import org.dkpro.jwpl.parser.ParsedPage;
import org.dkpro.jwpl.parser.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the TemplateParser for the german language, with special treatment
 * for all the german templates, like "Dieser Artikel" or "Deutschlandlastig".
 *
 */
public class GermanTemplateParser implements MediaWikiTemplateParser {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final String templatePrefix = "TEMPLATE[";
	private final String templatePostfix = "]";
	private final String parameterDivisor = ", ";
	private final String templateNotImplementedPrefix = "TEMPLATE NOT IMPLEMENTED[";
	private final String templateNotImplementedPostfix = "]";
	private final String emptyLinkText = "[ ]";

//	private MediaWikiContentElementParser parser;
	private final List<String> deleteTemplates;
	private final List<String> parseTemplates;

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
				logger.info("ParseTemplate: {}", templateName);
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
