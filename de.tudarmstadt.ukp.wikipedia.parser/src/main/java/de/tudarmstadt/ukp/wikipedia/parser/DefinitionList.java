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
 * In a definition List exist a Defined Term with Zero or more Definitions.
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
