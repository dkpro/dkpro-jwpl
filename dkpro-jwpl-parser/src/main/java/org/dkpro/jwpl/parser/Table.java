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
 * A Table has a Title and contains TableElements.<br>
 * This Class provides all needed functions simmilar to the other classes in
 * this package.
 *
 */
public class Table extends ContentContainer{
	
	private List<TableElement> tableElements;
	private ContentElement title;
	
	public Table(){
		ccl = new ArrayList<Content>();
		tableElements = new ArrayList<TableElement>();
	}
		
	public String toString(){
		StringBuilder result = new StringBuilder();
		
		result.append( "TB_TableElements: "+tableElements.size() );
		for( TableElement td: tableElements ) result.append( "\n"+td );
		
		return result.toString();
	}
	
	public void addTableElement( TableElement te ){
		tableElements.add( te );
		ccl.add( te );
	}
	
	public void removeTableElement( TableElement te ){
		tableElements.remove( te );
		ccl.remove( te );
	}
	
	public TableElement getTableElement( int i ){
		return tableElements.get(i);
	}
	
	public ContentElement getTitleElement( ){
		return this.title;
	}
	
	public void setTitleElement( ContentElement title ){
		if( title != null ){
			if( this.title == null )	ccl.add( 0, title );
			else 						ccl.set( 0, title );
		}
		else if( this.title != null )	ccl.remove( this.title );
		
		this.title = title;	
	}
	
	public int nrOfTableElements(){
		return tableElements.size();
	}
	
	public List<Content> getContentList(){
		return new ArrayList<Content>( ccl );
	}
	
	public int nrOfParagraphs(){
		int result = 0;
		for( TableElement td: tableElements ) result+= td.nrOfParagraphs();
		return result;
	}
	
	public Paragraph getParagraph(int i){
		int nr = 0;
		int offset = 0;
		for( TableElement td: tableElements ){
			nr = td.nrOfParagraphs();
			if( nr+offset > i )return td.getParagraph(i-offset);	
			offset += nr;
		}
		return null;	
	}
		
	public List<Paragraph> getParagraphs(){
		List<Paragraph> result = new ArrayList<Paragraph>();
		for( TableElement td: tableElements ) result.addAll( td.getParagraphs() );
		return result;
	}
	
	public int nrOfTables(){
		int result = 0;
		for( TableElement td: tableElements ) result+= td.nrOfTables();
		return result;
	}
	
	public Table getTable(int i){
		int nr = 0;
		int offset = 0;
		for( TableElement td: tableElements ){
			nr = td.nrOfTables();
			if( nr+offset > i )return td.getTable(i-offset);	
			offset += nr;
		}
		return null;
	}
	
	public List<Table> getTables(){
		List<Table> result = new ArrayList<Table>();
		for( TableElement td: tableElements ) result.addAll( td.getTables() );
		return result;
	}
	
	public int nrOfNestedLists(){
		int result = 0;
		for( TableElement td: tableElements )result += td.nrOfNestedLists();
		return result;
	}
	
	public NestedList getNestedList(int i){
		int nr = 0;
		int offset = 0;
		for( TableElement td: tableElements ){
			nr = td.nrOfNestedLists();
			if( nr+offset > i )return td.getNestedList(i-offset);	
			offset += nr;
		}
		return null;
	}
	
	public List<NestedList> getNestedLists(){
		List<NestedList> result = new ArrayList<NestedList>();
		for( TableElement td: tableElements ) result.addAll( td.getNestedLists() );
		return result;
	}
	
	public int nrOfDefinitionLists(){
		int result = 0;
		for( TableElement td: tableElements ) result+= td.nrOfDefinitionLists();
		return result;
	}
	
	public DefinitionList getDefinitionList(int i){
		int nr = 0;
		int offset = 0;
		for( TableElement td: tableElements ){
			nr = td.nrOfDefinitionLists();
			if( nr+offset > i )return td.getDefinitionList(i-offset);	
			offset += nr;
		}
		return null;
	}
	
	public List<DefinitionList> getDefinitionLists(){
		List<DefinitionList> result = new ArrayList<DefinitionList>();
		for( TableElement td: tableElements ) result.addAll( td.getDefinitionLists() );
		return result;
	}
}
