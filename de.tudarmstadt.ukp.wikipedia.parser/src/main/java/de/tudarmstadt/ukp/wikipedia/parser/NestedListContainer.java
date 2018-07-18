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
 * Take a Look a NestedList description first.
 *
 */
public class NestedListContainer extends ContentContainer implements NestedList{
	
	private List<NestedList> lists;
	private boolean numbered;
	
	public NestedListContainer(boolean numbered){
		this.ccl = new ArrayList<Content>();
		this.lists = new ArrayList<NestedList>();
		this.numbered = numbered;
	}
	
	/**
	 * Returns if the NestedList is a numbered or a unnumbered/pointed NestedList
	 */
	public boolean isNumbered(){ return numbered; }
	
	/**
	 * Returns the NestedListContainer or NestedListElement at Positon i.
	 */
	public NestedList getNestedList(int i){
		if( i<lists.size() ) return lists.get(i);
		else return null;
	}
	
	public void add( NestedList nl ){
		lists.add(nl);
		ccl.add(nl);
	}
	
	public void remove( NestedList nl ){
		lists.remove( nl );
		ccl.remove( nl );
	}
	
	public List<NestedList> getNestedLists(){
		return new ArrayList<NestedList>( lists );
	}
		
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("NLS_NUMBERD: "+ numbered);
		result.append("\nNLS_CONTENT: false");
		result.append("\nNLS_NESTEDTLISTS: "+lists.size());
		for( NestedList l: lists ) result.append( "\nNLS_NESTEDLIST:\n"+ l);
		return result.toString();
	}
}
