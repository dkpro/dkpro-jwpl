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

import java.util.ArrayList;
import java.util.List;

/**
 * This is the structure implementation of Section.<br>
 * A SectionContainer contains SubSections of type Section, which
 * can be either, a SectionContent or anoter SectionContainer.<br>
 * For a description of the inherited functions of Section, take a
 * look at the Documentation of Section.
 *
 */
public class SectionContainer extends Section {
	
	private List<Section> sections;
	
	public SectionContainer(int level){
		super( null, level );
		sections = new ArrayList<Section>();
	}
	
	public SectionContainer(ContentElement title, int level){
		super( title, level );
		sections = new ArrayList<Section>();
	}
	
	/**
	 * Returns the Number of SubSection of this Section.
	 */
	public int nrOfSubSections(){
		return sections.size();
	}
	
	/**
	 * Adds a SubSection after the last SubSection.
	 */
	public void addSection( Section s ){
		sections.add( s );
		ccl.add( s );
	}
	
	/**
	 * Removes the specified Section.
	 */
	public void removeSection( Section s ){
		sections.remove( s );
		ccl.remove( s );
	}
	
	/**
	 * Returns the i�th SubSection of this Section.
	 */
	public Section getSubSection(int i){
		if( sections.size() > i ) return sections.get(i);
		else return null;
	}
	
	/**
	 * Returns a List of all SubSections of the next level.
	 */
	public List<Section> getSubSections(){
		return new ArrayList<Section>( sections );
	}
	
	/* (non-Javadoc)
	 * @see org.tud.ukp.wikipedia.api.pageparser.Section#getContentList()
	 */
	public List<Content> getContentList(){
		return new ArrayList<Content>( ccl );
	}
	
	public int nrOfParagraphs(){
		int result = 0;
		for( Section s: sections ) result+= s.nrOfParagraphs();
		return result;
	}
	
	public Paragraph getParagraph(int i){
		int nr = 0;
		int offset = 0;
		for( Section s: sections ){
			nr = s.nrOfParagraphs();
			if( nr+offset > i )return s.getParagraph(i-offset);	
			offset += nr;
		}
		return null;	
	}
		
	public List<Paragraph> getParagraphs(){
		List<Paragraph> result = new ArrayList<Paragraph>();
		for( Section s: sections ) result.addAll( s.getParagraphs() );
		return result;
	}
	
	public int nrOfTables(){
		int result = 0;
		for( Section s: sections ) result+= s.nrOfTables();
		return result;
	}
	
	public Table getTable(int i){
		int nr = 0;
		int offset = 0;
		for( Section s: sections ){
			nr = s.nrOfTables();
			if( nr+offset > i )return s.getTable(i-offset);	
			offset += nr;
		}
		return null;
	}
	
	public List<Table> getTables(){
		List<Table> result = new ArrayList<Table>();
		for( Section s: sections ) result.addAll( s.getTables() );
		return result;
	}
	
	public int nrOfNestedLists(){
		int result = 0;
		for( Section s: sections )result += s.nrOfNestedLists();
		return result;
	}
	
	public NestedListContainer getNestedList(int i){
		int nr = 0;
		int offset = 0;
		for( Section s: sections ){
			nr = s.nrOfNestedLists();
			if( nr+offset > i )return s.getNestedList(i-offset);	
			offset += nr;
		}
		return null;
	}
	
	public List<NestedListContainer> getNestedLists(){
		List<NestedListContainer> result = new ArrayList<NestedListContainer>();
		for( Section s: sections ) result.addAll( s.getNestedLists() );
		return result;
	}
	
	public int nrOfDefinitionLists(){
		int result = 0;
		for( Section s: sections ) result+= s.nrOfDefinitionLists();
		return result;
	}
	
	public DefinitionList getDefinitionList(int i){
		int nr = 0;
		int offset = 0;
		for( Section s: sections ){
			nr = s.nrOfDefinitionLists();
			if( nr+offset > i )return s.getDefinitionList(i-offset);	
			offset += nr;
		}
		return null;
	}
	
	public List<DefinitionList> getDefinitionLists(){
		List<DefinitionList> result = new ArrayList<DefinitionList>();
		for( Section s: sections ) result.addAll( s.getDefinitionLists() );
		return result;
	}
	
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append( "SS_TITLE:\n"+ this.getTitleElement() );
		result.append( "\nSS_LEVEL: "+this.getLevel());
		result.append( "\nSS_SUBSECTIONS: "+ sections.size() );
		for( Section s: sections )
			result.append("\nSS_SUBSECTION:\n"+s.toString());
		
		return result.toString();
	}
}
