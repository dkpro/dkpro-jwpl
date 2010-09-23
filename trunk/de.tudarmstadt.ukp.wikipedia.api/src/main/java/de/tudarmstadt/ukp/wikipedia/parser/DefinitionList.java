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
 * In a definition List exist a Defined Term with Zero or more Definitions.
 * @author  CJacobi
 */
public class DefinitionList extends ContentContainer{
	
	private ContentElement definedTerm;
	private List<ContentElement> definitions;
	
	public DefinitionList(){
		this.ccl = new ArrayList<Content>();
		this.definedTerm = null;
		this.definitions = new ArrayList<ContentElement>();
	}
	
	public DefinitionList( ContentElement definedTerm, List<ContentElement> definitions){
		this.ccl = new ArrayList<Content>();
		this.definedTerm = definedTerm;
		this.definitions = definitions;
		ccl.add( definedTerm );
		ccl.addAll( definitions );
	}
	
	/**
	 * content = definedTerm[+definition]*
	 */
	public DefinitionList( List<ContentElement> content ){
		this.ccl = new ArrayList<Content>( content );
		this.definitions = new ArrayList<ContentElement>();
		
		if( content.size()>0 ){
			this.definedTerm = content.get(0);
			if( content.size()>1){
				this.definitions.addAll(content);
				this.definitions.remove(0);
			}
		}
		else this.definedTerm = null;
	}
	
	public String toString(){
		StringBuilder result = new StringBuilder();
		
		result.append( "DL_DEFINEDTERM:\n");
		result.append( definedTerm );
		
		if( definitions.size() != 0 ){
			result.append( "\nDL_DEFINITIONS:");
			for( ContentElement ce: definitions) result.append( "\n"+ce );
		}
		
		return result.toString();
	}
	
	public ContentElement getDefinedTerm(){	
		return definedTerm; 
	}
	
	public void setDefinedTerm( ContentElement definedTerm ){ 
		if( definedTerm != null ){
			if( this.definedTerm == null )	ccl.add( 0, definedTerm );
			else 							ccl.set( 0, definedTerm );
		}
		else if( this.definedTerm != null )	ccl.remove( this.definedTerm );
		
		this.definedTerm = definedTerm;	
	}
	
	public int nrOfDefinitions(){
		return definitions.size();
	}
	
	public void removeDefinition( ContentElement ce ){
		definitions.remove(ce);
		ccl.remove(ce);
	}
	
	public void addDefiniton( ContentElement ce ){
		definitions.add(ce);
		ccl.add(ce);
	}
	
	public ContentElement getDefinition(int i){
		if( definitions.size()>i ) return definitions.get(i);
		else return null;
	}
	
	public List<ContentElement> getDefinitions(){ 
		return new ArrayList<ContentElement>( definitions ); 
	}
}
