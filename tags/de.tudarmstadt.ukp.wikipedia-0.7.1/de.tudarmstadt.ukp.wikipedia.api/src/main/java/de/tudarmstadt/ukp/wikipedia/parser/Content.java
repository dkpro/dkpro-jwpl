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
package de.tudarmstadt.ukp.wikipedia.parser;

import java.util.*;

/**
 * This is a main Interface used by nearly all classes of this package.<br/>
 * <br/>
 * Be aware, that all retured Spans refer to the String returned by getText()<br/>
 * this is true for any implementing class!<br/>
 * @author CJacobi
 *
 */
public interface Content{
	
	public enum FormatType {
			/** Bold Text */	
			BOLD, 
            /** Italic Text */  
			ITALIC, 
			/** The Content between Math Tags */	
			MATH,
            /** The Content between NoWiki Tags */
            NOWIKI,
            /** The begin and end position of an unkown Tag defined by &lt; and > */    
			TAG, 
		};
	
	/**
	 * Returns the Text of the Element
	 */
	public String getText();
	
	/**
	 * Content.getText().length() == Content.length()
	 */
	public int length();
	
	/**
	 * Returns true, if there is no content in the element.
	 */
	public boolean empty();

	/**
	 * returns the Format Spans of the Specified Type.
	 */
	public List<Span> getFormatSpans(FormatType t);
	
	/**
	 * returns the Format Spans of the Specified Type, in the Range from start to end.
	 */
	public List<Span> getFormatSpans(FormatType t, int start, int end );
	
	/**
	 * returns the Format Spans of the Specified Type, in the Range of s.
	 */
	public List<Span> getFormatSpans(FormatType t, Span s);
	
	/**
	 * returns the Formats uses in this element.
	 */
	public List<FormatType> getFormats();
	
	/**
	 * returns the Formats uses in this element, in the Range from start to end.
	 */
	public List<FormatType> getFormats(int start, int end);
	/**
	 * returns the Formats uses in this element, in the Range of s.
	 */
	public List<FormatType> getFormats(Span s);
	
	/**
	 * returns all Links of this element.
	 */
	public List<Link> getLinks();
	
	/**
	 * returns all Links of this element of the specified type.
	 */
	public List<Link> getLinks( Link.type t );
	
	/**
	 * returns all Links of this element of the specified type, in the Range from start to end.
	 */
	public List<Link> getLinks( Link.type t, int start, int end);
	
	/**
	 * returns all Links of this element of the specified type, in the Range of s
	 */
	public List<Link> getLinks( Link.type t, Span s);
	
	/**
	 * returns all Templates.
	 */
	public List<Template> getTemplates();
	
	/**
	 * returns all Templates, in the Range from start to end.
	 */
	public List<Template> getTemplates(int start, int end);
	
	/**
	 * returns all Templates, in the Range of s.
	 */
	public List<Template> getTemplates(Span s);	
}
