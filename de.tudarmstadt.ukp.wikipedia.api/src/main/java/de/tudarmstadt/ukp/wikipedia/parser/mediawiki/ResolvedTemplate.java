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

import de.tudarmstadt.ukp.wikipedia.parser.Template;

public class ResolvedTemplate{

	public final static String TEMPLATESPACER = "(TEMPLATE)";

	private final Template template;
	private String preParseReplacement;
	private String postParseReplacement;

	/**
	 * is the Object which the Template Parser has been parsed, and will be
	 * integrated by the ContentElementParseing process. <br/>
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
	 * the TEXT which is within the bounds of the original template src. <br/>
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
	 * template code. MediaWiki code which is returned here, will be parsed.<br/>
	 * length()>0 ! empty stings would not be accepted.
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
	 * It can be a Template or any object the parser knows.<br/>
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
