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

