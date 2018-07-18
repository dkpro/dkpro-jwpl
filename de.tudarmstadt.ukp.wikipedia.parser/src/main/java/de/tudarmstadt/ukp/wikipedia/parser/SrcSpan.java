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

/**
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
