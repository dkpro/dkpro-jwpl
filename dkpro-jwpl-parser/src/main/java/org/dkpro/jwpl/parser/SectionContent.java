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
package org.dkpro.jwpl.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a Implementation of the Content of a Section.<br>
 * For every content accsess function exists a setter and a remover function.<br>
 * For a description of the inherited functions of Section, take a
 * look at the Documentation of Section.
 */
public class SectionContent extends Section{
	
	private List<Paragraph> paragraphs;
	private List<Table> tables;
	private List<NestedListContainer> nestedLists;
	private List<DefinitionList> definitionLists;
	
	public SectionContent(int level){
		super( null, level );
		init();
	}
	
	public SectionContent(ContentElement title, int level){
		super( title, level );
		init();
	}
	
	private void init(){
		paragraphs = new ArrayList<>();
		tables = new ArrayList<>();
		nestedLists = new ArrayList<>();
		definitionLists = new ArrayList<>();
	}
	
	public List<Content> getContentList(){
		return new ArrayList<>(ccl);
	}
	
	public int nrOfParagraphs(){ return paragraphs.size(); }
	
	public void addParagraph( Paragraph p ){
		paragraphs.add( p );
		ccl.add( p );
	}
	
	public void removeParagraph( Paragraph p ){
		paragraphs.remove( p );
		ccl.remove( p );
	}
	
	public Paragraph getParagraph(int i){
		if( paragraphs.size()> i) return paragraphs.get(i);
		else return null;
	}
	
	public List<Paragraph> getParagraphs(){
		return new ArrayList<>(paragraphs);
	}
	
	public int nrOfTables(){
		return tables.size();
	}
	
	public void addTable( Table t ){
		tables.add( t );
		ccl.add( t );
	}
	
	public void removeTable( Table t ){
		tables.remove( t );
		ccl.remove( t );
	}
	
	public Table getTable(int i){
		if( tables.size()>i) return tables.get(i);
		else return null;
	}
	
	public List<Table> getTables(){
		return new ArrayList<>(tables);
	}
	
	public int nrOfNestedLists(){
		return nestedLists.size();
	}
	
	public void addNestedList( NestedListContainer nl ){
		nestedLists.add( nl );
		ccl.add( nl );
	}
	
	public void removeNestedList( NestedListContainer nl ){
		nestedLists.remove( nl );
		ccl.remove( nl );
	}
	
	public NestedListContainer getNestedList(int i){
		if( nestedLists.size() > i ) return nestedLists.get(i);
		else return null;
	}
	
	public List<NestedListContainer> getNestedLists(){
		return new ArrayList<>(nestedLists);
	}
	
	public int nrOfDefinitionLists(){
		return definitionLists.size();
	}
	
	public void addDefinitionList( DefinitionList dl ){
		definitionLists.add( dl );
		ccl.add( dl );
	}
	
	public void removeDefinitionList( DefinitionList dl ){
		definitionLists.remove( dl );
		ccl.remove( dl );
	}
	
	public DefinitionList getDefinitionList(int i){
		if( definitionLists.size() > i ) return definitionLists.get(i);
		else return null;
	}
	
	public List<DefinitionList> getDefinitionLists(){ return new ArrayList<>(definitionLists); }
	
	public String toString(){
		StringBuilder result = new StringBuilder();
		
		result.append( "SC_TITLE:\n"+this.getTitleElement() );
		result.append( "\nSC_LEVEL: "+this.getLevel());
		
		result.append("\nSC_PARAGRAPHS: "+paragraphs.size());
		for( Paragraph p: paragraphs) result.append( "\nSC_PARAGRAPH:\n"+p );
		result.append("\nSC_TABLES: "+tables.size());
		for( Table t: tables) result.append("\nSC_TABLE:\n"+ t);
		result.append("\nSC_NESTED_LISTS: "+nestedLists.size());
		for( NestedList nl: nestedLists) result.append("\nSC_NESTED_LIST:\n"+nl);
		result.append("\nSC_DEFINITON_LISTS: "+definitionLists.size());
		for( DefinitionList dl: definitionLists)result.append("\nSC_DEFINITION_LIST:\n"+dl);
		
		return result.toString();	
	}
}
