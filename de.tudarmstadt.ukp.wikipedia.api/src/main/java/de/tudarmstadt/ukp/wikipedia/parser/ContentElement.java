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
 * This is the Simple implementation of the Content Inteface, and is used 
 * for nearly all content containing classes...
 * 
 * Be aware, that all retured Spans refer to the String returned by getText()<br/>
 * @author CJacobi
 *
 */
public class ContentElement extends ParsedPageObject implements Content {
		
	private String text;
	private List<Span> boldSpans;
	private List<Span> italicSpans;
	private List<Link> links;
	private List<Template> templates;
	private List<Span> tags;
	private List<Span> mathSpans;
	private List<Span> noWikiSpans;
	
	public ContentElement(){
		text = "";
		links = new ArrayList<Link>();
		templates = new ArrayList<Template>();
		boldSpans = new ArrayList<Span>();
		italicSpans = new ArrayList<Span>();
		tags = new ArrayList<Span>();
		mathSpans = new ArrayList<Span>();
		noWikiSpans = new ArrayList<Span>();
	}
	
	/**
	 * Look at getText() for Details...
	 */
	public void setText(String text){ 
		this.text = text; 
	}
	
	/**
	 * Returns the Text on wich all elements of this ContentElement are bases on.
	 */
	public String getText(){ 
		return text; 
	}
	
	/**
	 * Returns the Text defined with the Spans in the List divided by a WS
	 */
	public String getText( List<Span> sl ){
		StringBuilder result = new StringBuilder();
		
		for( Span s: sl ){
			result.append( s.getText( text )+' ');
		}
		int delChar = result.length()-1;
		if(delChar>0) result.deleteCharAt( delChar );
		
		return result.toString();
	}
	
	/**
	 * Retruns the length of the Text. Alternativ you can use getText().length()
	 */
	public int length(){ 
		return text.length(); 
	}
	
	/**
	 * Returns true if there is no Content in this ContentElement.
	 */
	public boolean empty(){
		return 
			text.length() == 0 && 
			links.size() == 0 && 
			templates.size() == 0 &&
			tags.size() == 0 &&
			mathSpans.size() == 0;	
	}
	
	/**
	 * Look at getFormatSpans for Details...
	 */
	public void setFormatSpans(FormatType t, List<Span> spans){
		switch( t ){
			case BOLD: boldSpans = spans;
			break;
			
			case ITALIC: italicSpans = spans;
			break;
			
			case TAG: tags = spans;
			break;
			
			case MATH: mathSpans = spans;
			break;
			
			case NOWIKI: noWikiSpans = spans;
			break;
		}
	}
	
	/**
	 * Returns all the Spans of the Format type t.
	 */
	public List<Span> getFormatSpans(FormatType t){
		switch( t ){
			case BOLD: return boldSpans;
			case ITALIC: return italicSpans;
			case TAG: return tags;
			case MATH: return mathSpans;
			case NOWIKI: return noWikiSpans;
			default: return null;
		}
	}
	
	/**
	 * Returns all the Spans of the Format type t in the Range of start to end
	 */
	public List<Span> getFormatSpans(FormatType t, int start, int end ){
		return getFormatSpans( t, new Span(start, end));
	}	
	
	/**
	 * Returns all the Spans of the Format type t in the Range of the Span s
	 */
	public List<Span> getFormatSpans(FormatType t, Span s){
		List<Span> result = new ArrayList<Span>();
		for( Span s2: getFormatSpans(t) )
			if( s2.hits(s) )result.add( s2 );
		return result;
	}
	
	/**
	 * Returns the Formats wich are used in this ContentElement in a List.
	 */
	public List<FormatType> getFormats(){
		List<FormatType> ftl= new ArrayList<FormatType>();
		if( boldSpans.size() != 0 ) ftl.add(FormatType.BOLD);
		if( italicSpans.size() != 0) ftl.add(FormatType.ITALIC);
		if( tags.size() != 0 ) ftl.add( FormatType.TAG );
		if( mathSpans.size() != 0 ) ftl.add( FormatType.MATH );
		if( noWikiSpans.size() != 0 ) ftl.add( FormatType.NOWIKI );
		return ftl;
	}
	
	/**
	 * Returns the Formats wich are used in this ContentElement, in the Range from start to end, in a List.
	 */
	public List<FormatType> getFormats(int start, int end){
		return getFormats(new Span(start, end));
	}
	
	/**
	 * Returns the Formats wich are used in this ContentElement, in the Range of the Span s, in a List.
	 */
	public List<FormatType> getFormats(Span s){
		List<FormatType> result= new ArrayList<FormatType>();
		for(Span s2: boldSpans)
			if( s.hits(s2) ){
				result.add( FormatType.BOLD );
				break;
			}
		
		for(Span s2: italicSpans)
			if( s.hits(s2) ){
				result.add( FormatType.ITALIC );
				break;
			}
		
		return result;
	}
		
	/**
	 * Look at getLinks() for Details...
	 */
	public void setLinks(List<Link> links){
		this.links = links;
	}
	
	/** 
	 * Retruns a List of the Links of this ContentElement
	 */
	public List<Link> getLinks(){
		return links;
	}
	
	/**
	 * Returns a List of the Links of this ContentElement of the Specified Link.type t
	 */
	public List<Link> getLinks( Link.type t ){
		List<Link> result = new ArrayList<Link>();
		for( Link l: links )
			if( l.getType()==t )result.add(l);
		return result;
	}
	
	/**
	 * Returns a List of the Links of this ContentElement of the Specified Link.type t in the Range of s
	 */
	public List<Link> getLinks( Link.type t, Span s){
		List<Link> result = new ArrayList<Link>();
		for( Link l: links)
			if( l.getType()==t && l.getPos().hits(s) ) result.add(l);	
		return result;
	}
	
	/**
	 * Returns a List of the Links of this ContentElement of the Specified Link.type t in the Range of start to end
	 */
	public List<Link> getLinks(  Link.type t, int begin, int end){
		return getLinks( t, new Span(begin, end) );
	}
	
	/**
	 * Look at getTemplates for Details...
	 */
	public void setTemplates( List<Template> templates){
		this.templates = templates;
	}
	
	/**
	 * Returns a List of the Templates of this ContentElement.
	 */
	public List<Template> getTemplates(){
		return templates;
	}
	
	/**
	 * Returns a List of the Templates of this ContentElement in the Range from start to end
	 */
	public List<Template> getTemplates(int start, int end){
		return getTemplates( new Span(start, end) );
	}
	
	/**
	 * Returns a List of the Templates of this ContentElement in the Range of s
	 */
	public List<Template> getTemplates(Span s){
		List<Template> result = new ArrayList<Template>();
		for( Template t: templates)
			if( t.getPos().hits( s ) ) result.add( t );
		return result;
	}
	
	/**
	 * Try and find out ;-)
	 */
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("CE_TEXT: \"" + text + "\"" );
		
		result.append("\nCE_BOLD_SPANS: ");
		if( boldSpans != null ){
			result.append( boldSpans.size() );
			for( Span s: boldSpans ) result.append("\n\t"+ s+ " : \""+ s.getText(text) + "\"");
		}
		else result.append("ERROR: boldSpans == null");
			
		result.append("\nCE_ITALIC_SPANS: ");
		if( italicSpans != null ){
			result.append( italicSpans.size() );
			for( Span s: italicSpans ) result.append("\n\t"+s+" : \""+s.getText(text)+"\"");	
		}
		else result.append("ERROR: italicSpans == null");

		result.append("\nCE_LINKS: ");
		if( links != null ){
			result.append( links.size() );
			for( Link l: links)	result.append("\n"+ l );
		}
		else result.append("ERROR: links == null");
		
		result.append("\nCE_TEMPLATES: ");
		if( templates != null ){
			result.append( templates.size() );
			for( Template t: templates)	result.append("\n"+ t );
		}
		else result.append("ERROR: templates == null");
			
		result.append("\nCE_TAGS: ");
		if( templates != null ){
			result.append( tags.size() );
			for( Span s: tags)	result.append("\n"+ s );
		}
		else result.append("ERROR: templates == null");
		
		return result.toString();
	}
}
