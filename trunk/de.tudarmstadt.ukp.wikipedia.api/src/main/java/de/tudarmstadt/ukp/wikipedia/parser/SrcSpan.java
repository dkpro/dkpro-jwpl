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
 * @author CJacobi
 *
 */
public class SrcSpan {
	private int start;
	private int end;
	
	/**
	 * @param start is the startposition of the Object in the original MediaWikiSource
	 * @param end is the endposition of the Object in the original MediaWikiSource
	 */
	public SrcSpan(int start, int end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Look at Constructor for Details...
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Look at Constructor for Details...
	 */
	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * Look at Constructor for Details...
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Look at Constructor for Details...
	 */
	public void setStart(int start) {
		this.start = start;
	}
	
	public String toString(){
		return "("+start+", "+end+")";
	}
}
