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
 * This TemplateParser will delete ALL templates, whitout any exception!
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
