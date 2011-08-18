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
 * Take a Look a NestedList description first.
 * @author CJacobi
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
