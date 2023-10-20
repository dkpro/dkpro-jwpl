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

import org.dkpro.jwpl.parser.Template;

public class ResolvedTemplate{

	public final static String TEMPLATESPACER = "(TEMPLATE)";

	private final Template template;
	private String preParseReplacement;
	private String postParseReplacement;

	/**
	 * is the Object which the Template Parser has been parsed, and will be
	 * integrated by the ContentElementParseing process. <br>
	 * If parsedObject == null, the template will be discarded...
	 */
	private Object parsedObject;

	/**
	 * Creates a new ResolvedTemplate linked to the original template.
	 * @param template the original template
	 */
	public ResolvedTemplate(Template template){
		this.template = template;
		this.postParseReplacement = "";
		checkPreParseReplacement();
	}

	private void checkPreParseReplacement(){
		if( preParseReplacement==null || preParseReplacement.length()==0 ) {
			preParseReplacement = TEMPLATESPACER;
		}
	}

	/**
	 * Will be called by the parser after the parsing process and will replace
	 * the TEXT which is within the bounds of the original template src. <br>
	 * If NULL is returned, the parser won't do anything.
	 */
	public String getPostParseReplacement() {
		return postParseReplacement;
	}

	/**
	 * Look at getPostParseReplacement...
	 */
	public void setPostParseReplacement(String postParseReplacement) {
		this.postParseReplacement = postParseReplacement;
	}

	/**
	 * will be called by the parser before the Parsing process and replaces the original
	 * template code. MediaWiki code which is returned here, will be parsed.<br>
	 * length() &gt; 0 ! empty stings would not be accepted.
	 */
	public String getPreParseReplacement() {
		return preParseReplacement;
	}

	/**
	 * Look at getPreParseReplacement...
	 */
	public void setPreParseReplacement(String preParseReplacement) {
		this.preParseReplacement = preParseReplacement;
		checkPreParseReplacement();
	}

	/**
	 * In case of an Error the Parser will use the Original Template
	 * as parsed object.
	 */
	public Template getTemplate() {
		return template;
	}

	/**
	 * Returns the Object which is representative for the Template Code.
	 * It can be a Template or any object the parser knows.<br>
	 * If the Template is e.g. a Link the Link will be returned here.
	 */
	public Object getParsedObject() {
		return parsedObject;
	}

	/**
	 * Look at getParsedObject for Details.
	 * @param parsedObject
	 */
	public void setParsedObject(Object parsedObject) {
		this.parsedObject = parsedObject;
	}

}
