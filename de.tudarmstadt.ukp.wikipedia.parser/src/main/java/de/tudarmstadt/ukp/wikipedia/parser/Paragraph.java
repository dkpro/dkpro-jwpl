/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.parser;

/**
 * This is a simple ContentElement extende with a Paragraph Type.
 *
 */
public class Paragraph extends ContentElement {
	
	public enum type {NORMAL, BOXED, INDENTED}
	
	private type t;
	
	public Paragraph(){
		super();
	}
	
	public Paragraph( type t){
		super();
		this.t = t;
	}
		
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append( super.toString() );
		result.append( System.getProperty("line.separator") + "PA_TYPE: " + t);
		return result.toString();
	}	

	public void setType( type t ){
		this.t = t;
	}
	
	public type getType(){
		return t;
	}
}
