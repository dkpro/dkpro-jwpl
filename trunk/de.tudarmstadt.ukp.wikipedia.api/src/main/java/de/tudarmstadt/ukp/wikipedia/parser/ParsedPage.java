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

import de.tudarmstadt.ukp.wikipedia.parser.Content.FormatType;

/**
 * Provides access to structured information about a MediaWiki article page.
 * @author  CJacobi
 * @author  TZ
 */
public class ParsedPage{	
	
	private String name;
	private int pageId;
	private SectionContainer superSection;
	private ContentElement categories;
	private ContentElement languages;
	
	private int firstParagraphNr;
//	private ContentElement aboutArticle;
	
	/**
	 * Constructor for a blank ParsedPage.<br>
     * Only needed, if you want to create a Wikipedia article from scratch.
     * 
     * Creating a ParsedPage from a Wikipedia article requires to create a parser object first.
     * 
	 */
	public ParsedPage(){
		this.superSection = new SectionContainer(null,0);
	}
	
	/**
     * Sets the name of a parsed page.
	 * @param name A name for the parsed page.
	 */
	public void setName( String name ){
		this.name = name;
	}
	
	/**
     * The name of a parsed page.
	 * @return The name of a parsed page.
	 */
	public String getName(){ 
		return name; 
	}
	
	/**
     * Set the pageId of a parsed page.
	 * @param pageId A pageId for the parsed page.
	 */
	public void setPageId( int pageId ){
		this.pageId = pageId;
	}
	
	/**
     * The pageId of a parsed page.
	 * @return The pageId of a parsed page.
	 */
	public int getPageId(){
		return pageId;
	}
	
	/**
     * Sets the category element of a parsed page.
	 * @param categories A ContentElement containg the categories of a page.
	 */
	public void setCategoryElement( ContentElement categories ){
		this.categories = categories;
	}
	
	/**
     * The ContentElement with links to a page's categories.
	 * @return The ContentElement with links to a page's categories.
     */
	public ContentElement getCategoryElement(){
		return this.categories;
	}
	
	/**
     * Returns a list of category Link objects.
     * This is a shortcut for writing getCategoryElemement.getLinks();
	 * @return A list of category links.
	 */
	public List<Link> getCategories(){ 
		if (categories == null) {
            return new ArrayList<Link>();
		}

		return categories.getLinks(); 
	}
	
	/**
	 * Sets the number of the first paragraph.
	 * @param nr The number of the first paragraph.
	 */
	public void setFirstParagraphNr(int nr) {
		this.firstParagraphNr = nr;
	}
	
	/**
	 * Returns the number of the first paragraph.
     * @return The number of the first paragraph.
	 */
	public int getFirstParagraphNr(){
		return firstParagraphNr;
	}
	
	/**
	 * Returns the first paragraph.<br>
	 * This is a shortcut for getParagraph( getFirstParagraphNr() ).
     * It is <b>not</b> the same as getParagraph( 0 ), because the physically first paragraph often contain tables etc.
	 */
	public Paragraph getFirstParagraph() {
		return this.getParagraph(firstParagraphNr);
	}
	
    /**
     * Sets the language element of a parsed page.
     * @param languages A ContentElement containg the languages of a page.
     */
	public void setLanguagesElement( ContentElement languages ){
		this.languages = languages;
	}
	
	/**
	 * Returns a ContentElement containing the languages that are linked inside the article.
     * @return A ContentElement containing the languages that are linked inside the article.
	 */
	public ContentElement getLanguagesElement(){
		return languages;
	}
	
	/**
	 * Returns a list of language Link objects.
     * This is a shortcut for writing getLanguagesElement().getLinks();
	 */
	public List<Link> getLanguages(){ 
		return languages.getLinks(); 
	}
	
//// I do not think that this should be a core api method, as it is language and template dependend. (TZ)
//	/**
//	 * Returns a ContentElement with the Content of "Dieser Artikel" Template
//	 */
//	public ContentElement aboutArticle(){ 
//		return aboutArticle; 
//	}
//
//	/**
//	 * See aboutArticle() for Details...
//	 */
//	public void setAboutArticle(ContentElement aboutArticle){ 
//		this.aboutArticle = aboutArticle; 
//	}


    /**
     * Sets the Sections of a ParsedPage.
	 * @param sections A list of sections.
	 */
	public void setSections( List<Section> sections ){
		for( Section s: sections ) superSection.addSection(s); 
	}
	
	/**
	 * Set the Sections of the ParsedPage.<br>
	 * This function is used to upgrade a SectionContainer to a ParsedPage.
	 * @param s A sectionContainer.
	 */
	public void setSections( SectionContainer s ){ 
		superSection = s; 
	}
	
// TODO What means lowest level? => TZ: I think it means "highest" semantically and "lowest" in numbering (e.g. <h1>).
	/**
	 * Returns the requested Section of the lowest level.
	 * @param i The number of the section.
	 * @return The section with number i.
	 */
	public Section getSection(int i){ 
		return superSection.getSubSection(i); 
	}
	
	/**
	 * Retruns a list of all Sections of the lowest level.
     * @return A list of sections.
	 */
	public List<Section> getSections(){
		return superSection.getSubSections();
	}
	
	/*
	 * Returns pageId and name in a String
	 */
	public String toString(){
		return "ParsedPage " + pageId + " " + name;
	}
	
	/**
     * Returns the number of paragraphs.
	 * @return The number of paragraphs.
	 */
	public int nrOfParagraphs(){ return superSection.nrOfParagraphs(); }

	/**
     * Returns the paragraph indicated by the parameter i.
	 * @param i The number of the paragraph to return.
	 * @return The paragraph with number i.
	 */
	public Paragraph getParagraph(int i){return superSection.getParagraph(i); }
	
    /**
     * Returns a list of paragraphs.
	 * @return A list of paragraphs.
	 */
	public List<Paragraph> getParagraphs(){	return superSection.getParagraphs(); }
	
    /**
     * Returns the number of tables.
     * @return The number of tables.
     */
	public int nrOfTables(){ return superSection.nrOfTables(); }
    
    /**
     * Returns the table indicated by the parameter i.
     * @param i The number of the table to return.
     * @return The table with number i.
     */
	public Table getTable(int i){ return superSection.getTable(i); }

    /**
     * Returns a list of tables.
     * @return A list of tables.
     */
	public List<Table> getTables(){ return superSection.getTables(); }
	
    /**
     * Returns the number of nested lists.
     * @return The number of nested lists.
     */
	public int nrOfNestedLists(){ return superSection.nrOfNestedLists(); }

    /**
     * Returns the nested list indicated by the parameter i.
     * @param i The number of the nested list to return.
     * @return The nested list with number i.
     */
	public NestedList getNestedList(int i){ return superSection.getNestedList(i); }

    /**
     * Returns a list of nested lists.
     * @return A list of nested lists.
     */
    public List<NestedListContainer> getNestedLists(){ return superSection.getNestedLists(); }

    /**
     * Returns the number of definition lists.
     * @return The number of definition lists.
     */
	public int nrOfDefinitionLists(){ return superSection.nrOfDefinitionLists(); }

    /**
     * Returns the definition list indicated by the parameter i.
     * @param i The number of the definition list to return.
     * @return The definition list with number i.
     */
	public DefinitionList getDefinitionList(int i){ return superSection.getDefinitionList(i); }

    /**
     * Returns a list of definition lists.
     * @return A list of definition lists.
     */
	public List<DefinitionList> getDefinitionLists(){ return superSection.getDefinitionLists(); }
	
	/**
	 * Return the plain text.
     * @return The plain text.
	 */
	public String getText(){ return superSection.getText(); }

//// TODO we should not need that as we could call getText on the span itself.    
//    /**
//	 * Look at the SAME function in SectionContainer for Details...
//	 */
//	public String getText( List<Span> sl ){ return superSection.getText( sl ); }


    /**
     * Returns the length of the text in characters.
     * @return The length of the text in characters.
     */
    public int length(){ return superSection.length(); }
    
    public List<FormatType> getFormats(){ return superSection.getFormats(); }   

////I do not know what these are for and they are never used (TZ).    
//    public List<FormatType> getFormats(int begin, int end){ return superSection.getFormats(begin,end); }  
//    public List<FormatType> getFormats(Span s){ return superSection.getFormats(s); }

    public List<Span> getFormatSpans(FormatType t){ return superSection.getFormatSpans(t); }
    
////I do not know what these are for and they are never used (TZ).    
//    public List<Span> getFormatSpans(FormatType t, int start, int end ){ return superSection.getFormatSpans(t, start, end); }
//    public List<Span> getFormatSpans(FormatType t, Span s){ return superSection.getFormatSpans(t, s); }
	

    public List<Link> getLinks(){ return superSection.getLinks(); }

////I do not know what these are for and they are never used (TZ).    
//  public List<Link> getLinks(Link.type t){ return superSection.getLinks(t); }	
//	public List<Link> getLinks(Link.type t, int begin, int end){ return superSection.getLinks(t, begin, end); }	
//	public List<Link> getLinks(Link.type t, Span s){ return superSection.getLinks(t, s); }		
    
	/**
     * Returns a list of templates that are used in the page.
	 * @return A list of templates that are used in the page.
	 */
	public List<Template> getTemplates(){ return superSection.getTemplates(); }

//// I do not know what these are for and they are never used (TZ).    
//	public List<Template> getTemplates(int start, int end){ return superSection.getTemplates(start, end); }
//	public List<Template> getTemplates(Span s){ return superSection.getTemplates(s); }
}
