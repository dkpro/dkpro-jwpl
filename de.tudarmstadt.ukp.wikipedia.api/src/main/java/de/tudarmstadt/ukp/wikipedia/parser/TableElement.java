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

import java.util.List;

/**
 * This Class implements a Field in a Table...<br/>
 * it simply has an int for col and row, and a SectionContainer for the Content.<br/>
 * This implementation is needed, because a Table in MediaWiki can contain neary
 * everything.
 * @author  CJacobi
 */
public class TableElement extends ContentContainer{
	
	private int col;
	private int row;
	private SectionContainer s;
	
	public TableElement( SectionContainer s, int row, int col ){ 		
		this.ccl = s.ccl;
		this.s = s;
		this.row = row;
		this.col = col;	
	}
	
	public int getCol(){ return col; }
	public int getRow(){ return row; }
	
	public int nrOfSections(){ return s.nrOfSubSections(); }
	public Section getSection(int i){ return s.getSubSection(i); }
	public void removeSection( Section s ){ this.s.removeSection( s );}
	public List<Section> getSubSections(){ return s.getSubSections(); }
	
	public List<Content> getContentList(){ return s.getContentList(); }
	
	public int nrOfParagraphs(){ return s.nrOfParagraphs(); }
	public Paragraph getParagraph(int i){ return s.getParagraph(i); }
	public List<Paragraph> getParagraphs(){ return s.getParagraphs(); }
	public int nrOfTables(){ return s.nrOfTables(); }
	public Table getTable(int i){ return s.getTable(i); }
	public List<Table> getTables(){ return s.getTables(); }
	public int nrOfNestedLists(){ return s.nrOfNestedLists(); }
	public NestedList getNestedList(int i){ return s.getNestedList(i); }	
	public List<NestedListContainer> getNestedLists(){ return s.getNestedLists(); }
	public int nrOfDefinitionLists(){ return s.nrOfDefinitionLists(); }
	public DefinitionList getDefinitionList(int i){ return s.getDefinitionList(i); }
	public List<DefinitionList> getDefinitionLists(){ return s.getDefinitionLists(); }
	
	public SectionContainer getSectionContainer(){ return s; }

	public String toString(){ 
		return "TABLE_DATA: \n"+  s.toString(); 
	}
}
