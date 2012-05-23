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
package de.tudarmstadt.ukp.wikipedia.parser.mediawiki;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.parser.Span;


/**
 * A Class which manages Spans which are related to a StringBuilder.
 * With the SpanManager it is possible to work on a String (delete, insert, replace)
 * with no need to adjust the Spans related to the StringBuilder manually.
 * @author CJacobi
 *
 */
public class SpanManager implements CharSequence {

	private final StringBuilder sb;
	private final List< List<Span> > managedLists;
	
	private List<Integer> ib;
	private boolean calculateSrcPositions;
	
	/**
	 * Creates a new SpanManager with src as base.
	 * @param src
	 */
	public SpanManager(String src){
		sb  = new StringBuilder(src);
		managedLists = new ArrayList< List<Span> >();
		calculateSrcPositions = false;
	}
	
	/**
	 * Enables the Calculation of Src Position. The base for these position
	 * will be the aktual, not the initial, String wich is uses as Base for
	 * the SpanManager.
	 */
	public void enableSrcPosCalculation(){
		calculateSrcPositions = true;
		final int len = sb.length();
		ib = new ArrayList<Integer>(len);
		for( int i=0; i<len; i++) ib.add( i );
	}
	
	/**
	 * Retruns a SrcPos for the index of the aktual SpanManager base.
	 * @return the Position the index has, when enableSrcPosCaulation() has been called, 
	 * or -1 if it is not possible.
	 */
	public int getSrcPos( int index ){
		if( calculateSrcPositions ){
			return ib.get( index );
		}
		else{
			System.err.println("SrcSpanCalculation not enabled!");
			return -1;
		}
	}
	
	/**
	 * Adds a List of Spans, which should be managed.
	 */
	public void manageList( List<Span> spans ){ 	
		managedLists.add( spans ); 
	}

	/**
	 * Removes a List of Spans (not the Spans in the List), which shouldn�t be managed anymore.
	 * @param spans
	 */
	public void removeManagedList( List<Span> spans ){ 
		final Span listIdentifer = new Span(Integer.MAX_VALUE, Integer.MIN_VALUE);	
		spans.add( listIdentifer );	
		managedLists.remove( spans );
		spans.remove( listIdentifer );
	}
	
	private void adjustLists(int offset, int n){
		for( List<Span> list: managedLists )
			for( Span s: list )s.adjust(offset, n);
	}

	/**
	 * Deletes the content between s.getStart() (included) and s.getEnd() (excluded).
	 */
	public SpanManager delete(Span s){ return delete(s.getStart(), s.getEnd() ); }
	
	/**
	 * Deletes the content between start (included) and end (excluded).
	 */
	public SpanManager delete(int start, int end){
		sb.delete(start, end);
		adjustLists( start, start-end );
		
		if(calculateSrcPositions) for( int i = 0; i<end-start; i++) ib.remove( start );
		
		return this;
	}
	
	/**
	 * Insterts a String at the position offset.
	 */
	public SpanManager insert(int offset, String str){
		sb.insert(offset, str);
		adjustLists( offset, str.length() );
		
		if( calculateSrcPositions ) for( int i=0; i<str.length(); i++ ) ib.add( offset, -1 );
		
		return this;
	}
	
	/**
	 * Replaces the content between s.getStart() (included) and s.getEnd() (excluded) with 
	 * a String
	 */
	public SpanManager replace(Span s, String str){	return replace( s.getStart(), s.getEnd(), str); }
	
	/**
	 * Replaces the content between start (included) and end (excluded) with a String
	 */
	public SpanManager replace(int start, int end, String str){
		sb.replace(start, end, str);
		
		if( calculateSrcPositions ){
			for( int i=0; i<end-start; i++) ib.remove( start );
			for( int i=0; i<str.length(); i++) ib.add( start, -1 );
		}
		
		adjustLists(start, str.length()-(end-start) );
		return this;
	}
	
	public int indexOf(String str){ return this.indexOf(str, 0); }
	public int indexOf(String str, int fromIndex){ return sb.indexOf(str, fromIndex); }
	public int indexOf(String str, Span s){	return indexOf(str, s.getStart(), s.getEnd() ); }
	
	public int indexOf(String str, int fromIndex, int toIndex){
		int result = sb.indexOf(str, fromIndex);
		if( result >= toIndex ) return -1 ;
		return result;
	}
	
	public String substring(int start){
	    if (start < 0) {
	        start = 0;
	    }
	    return this.sb.substring(start);
	}
	
	public String substring(int start, int end){
	    if (start < 0) {
	        start = 0;
	    }
	    if (start > end) {
	        return "";
	    }
	    
	    return sb.substring(start, end);
	}
	
	public String substring( Span s ) {
	    if (s.getStart() < s.getEnd()) {
	        return sb.substring( s.getStart(), s.getEnd() );
	    }
	    else {
	        return "";
	    }
	}
	
	/**
	 * <font color="#ff0000">This function is not implemented !!!</font>
	 */
	public CharSequence subSequence(int start, int end){
		//TODO Implementieren
		System.err.println("CharSequence subSequence(int start, int end)\nSorry, not Implemented");
		sb.charAt(-1); //causes an error
		return null;
	}
	
	public int length(){ 
		return sb.length(); 
	}
	
	public SpanManager setCharAt(int index, char c){
		sb.setCharAt( index, c );
		if( calculateSrcPositions ) ib.set( index, -1 );
		return this;
	}
	
	public char charAt(int index){ 
		return sb.charAt(index); 
	}
	
	@Override
    public String toString(){ 
		return sb.toString(); 
	}
	
	/**
	 * Returnes some information about the content of the SpanManager an it�s manages
	 * Spans
	 */
	public String info(){
		StringBuilder result = new StringBuilder();
		
		result.append("\n-=SPANMANAGER=----------------------------------------------------------------\n");
				
		result.append("TEXT:");
		result.append( "\""+ sb.toString() + "\"");
		result.append("\n");		
			
		result.append("\nMANAGED SPAN LISTS:");
		if( managedLists.isEmpty() )
			result.append(" NONE\n");
		else{	
			result.append("\n");
			for( int k=0; k<managedLists.size(); k++ ){
				List<Span> sl = managedLists.get(k);
				result.append("{");
				if( sl.size() != 0 ){
					for( int i=1; i<sl.size()-1; i++ ) result.append(sl.get(i)+", ");
					result.append(sl.get( sl.size()-1));
				}
				result.append("}\n");
			}
		}
		
		result.append("------------------------------------------------------------------------------");
		
		return result.toString();
	}	
}
