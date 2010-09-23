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

/**
 * This is a simple ContentElement extende with a Paragraph Type.
 * @author CJacobi
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
