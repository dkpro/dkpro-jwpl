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

public class Template extends ParsedPageObject{
	
	private Span pos;
	private String name;
	private List<String> parameters;	
	
	public Template(Span pos, String name, List<String> parameters) {
		this.pos = pos;
		this.name = name;
		this.parameters = parameters;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Returns the Position Span of this Template refering to the ContentElement
	 * in which the Template occures. This is mainly the same like Link.getPos(),
	 * but a Template does�n know it�s HomeElement.
	 */
	public Span getPos() {
		return pos;
	}

	/**
	 * Look at getPos for Details...
	 */
	public void setPos(Span pos) {
		this.pos = pos;
	}
	
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("TE_NAME: \""+name+"\"");
		result.append("\nTE_PARAMETERS: "+parameters.size());
		for( String parameter: parameters) result.append("\nTE_PARAMETER: \""+ parameter +"\"");
		result.append("\nTE_POS: "+ pos);		
		return result.toString();
	}
}

