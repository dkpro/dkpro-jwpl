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
 * Provides a Start and End Position...
 */
public class Span extends ParsedPageObject{
	
	private int start;
	private int end;
	
	public Span(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	public int getStart(){
		return start;
	}
	
	public Span setStart( int start ){
		this.start = start;
		return this;
	}
	
	public Span adjustStart( int n ){
		start+=n;
		return this;
	}
	
	public int getEnd(){
		return end;
	}
	
	public Span setEnd( int end ){
		this.end = end;
		return this;
	}
	
	public Span adjustEnd( int n ){
		end+=n;
		return this;
	}
	
	public Span adjust( int n ){ return adjust(0,n); }
	
	/**
	 * Adjusts the start and end Position of the Span, if they are
	 * larger than the offset.
	 */
	public Span adjust(int offset, int n){
		if( offset < 0 )return this; //null
		
		if( offset < end ){
			end += n;
			if( end<offset )end = offset;
		}
		else return this; //null
		
		if( offset < start ){
			start += n;
			if( start<offset )start = offset;
		}
		return this;	
	}
	
	public boolean equals(int start, int end){
		return ((this.start == start )&&( this.end == end));
	}
	
	public boolean equals(Span s){
		return ((this.start == s.getStart() )&&( this.end == s.getEnd() ));
	}

	/**
	 * returns true if this Span is in the range of the Span s.
	 */
	public boolean hits( Span s ){
		return start < s.getEnd() && s.getStart() < end;
	}	
	
	public String toString(){
		return "("+start+", "+end+")";
	}
	
	/**
	 * simply src.substring( this.getStart(), this.getEnd );
	 */
	public String getText(String src){
	    if (end > src.length()) {
	        end = src.length();
	    }
	    return src.substring(start, end);
	}
	
	/**
	 * A defined ErrorChar which will be returnd when an error occures.<br>
	 * An ErrorChar seems to be more easy to handle than e.g. an IndexOutOfBoundsException.
	 */
	public static final char ERRORCHAR = 0;
	
	public char charAt(int pos, CharSequence cs){
		if( pos + start < end ) return cs.charAt( start + pos );
		else return ERRORCHAR;
	}
	
	public int nonWSCharPos(CharSequence cs){
		int pos=0;
		while( charAt(pos, cs)==' ' )pos++;
		return pos;
	}
	
	/**
	 * Returns the Span, with trailing whitespaces omitted.
	 */
	public Span trimTrail(CharSequence src){
		if( start<end ){
			while(src.charAt( end-1 ) == 32){
				end--;
				if( start==end )break;
			}
		}
		return this;
	}
	
	/**
	 * Returns the Span, with leading and trailing whitespaces omitted.
	 */
	public Span trim( CharSequence src ){
		if( start<end )
			while(src.charAt( end-1 ) == 32){
				end--;
				if( start==end )break;
			}
			
		if( start<end )
			while( src.charAt( start ) == 32){
				start++;
				if( start==end)break;
			}
		
		return this;
	}
	
	/**
	 * returns this.getEnd()-this.getStart()
	 */
	public int length(){
		return end-start;
	}
	
	public Span clone(){
		Span result = new Span( start, end );
		result.setSrcSpan( this.getSrcSpan() );
		return result;
	}
}
