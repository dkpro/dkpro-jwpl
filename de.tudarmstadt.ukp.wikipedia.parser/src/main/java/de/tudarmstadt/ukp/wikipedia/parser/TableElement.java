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
package de.tudarmstadt.ukp.wikipedia.parser;

import java.util.List;

/**
 * This Class implements a Field in a Table...<br>
 * it simply has an int for col and row, and a SectionContainer for the Content.<br>
 * This implementation is needed, because a Table in MediaWiki can contain neary
 * everything.
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
