/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 *     Samy Ateia - provided a patch via the JWPL mailing list
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.parser;

import java.util.ArrayList;
import java.util.List;

public class Link extends ParsedPageObject{

	private Content home_cc;
	private final type t;
	private final Span pos;
	private final String target;
	private final List<String> parameters;

	public enum type {EXTERNAL, INTERNAL, AUDIO, VIDEO, IMAGE, UNKNOWN};

	public Link( Content home_cc, Span linkPos, String target, type t, List<String> parameters ){
		this.home_cc = home_cc;
		this.pos = linkPos;
		this.target = target;
		this.t = t;
		this.parameters = (parameters==null?new ArrayList<String>():parameters);
	}

	/**
	 * Returns the Content Element in wich the Link occures.
	 */
	public Content getHomeElement(){ return home_cc; }
	public Link setHomeElement(Content home_cc){ this.home_cc = home_cc; return this; }

	/**
	 * Returns the Type of the Link.
	 */
	public type getType(){ return t; }

	/**
	 * Retruns the Position Span of the Link, wich refers to getHomeElement().getText().
	 */
	public Span getPos(){ return pos; }

	/**
	 * Retruns the Target of the Link.
	 */
	public String getTarget(){ return target; }

	/**
	 * Returns a List of Parameters for this Link, in most cases the size of the list will be 0.
	 */
	public List<String> getParameters(){ return parameters; }

	/**
	 * Retruns the Link text or link caption.
	 */
	public String getText(){
		if( home_cc == null ) {
			return null;
		}
		return pos.getText( home_cc.getText() );
	}

	/**
	 * Returns the Number of Words left and right of the Link, in the Bounds of the
	 * HomeElement of this Link.
	 */
	public String getContext(int wordsLeft, int wordsRight){
		final String text = home_cc.getText();
		int temp;

		// get the left start position
		int posLeft = pos.getStart();
		temp = posLeft-1;
		while( posLeft != 0 && wordsLeft > 0 ){
			while( temp > 0 && text.charAt( temp ) < 48  ) {
				temp--;
			}
			while( temp > 0 && text.charAt( temp ) >= 48 ) {
				temp--;
			}
			posLeft = ( temp>0 ? temp+1 : 0 );
			wordsLeft--;
		}

		// get the right end position
		int posRight = pos.getEnd();
		temp = posRight;
		while( posRight != text.length() && wordsRight > 0 ){
			while( temp < text.length() && text.charAt( temp ) < 48 ) {
				temp++;
			}
			while( temp < text.length() && text.charAt( temp ) >= 48 ) {
				temp++;
			}
			posRight = temp;
			wordsRight--;
		}

		// retrun a string...
		return
			text.substring(posLeft, pos.getStart() ) +
			text.substring(pos.getEnd(), posRight);
	}

	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append( "LI_TYPE: "+t );
		result.append("\nLI_TARGET: \""+ target + "\"");
		result.append("\nLI_TEXT: \""+ getText() +"\"");
		result.append("\nLI_POSITION: \""+ pos + "\"");
		result.append( "\nLI_PARAMETERS: "+parameters.size() );
		for( String s: parameters ) {
			result.append("\nLI_PARAMETER: \""+ s +"\"" );
		}
		return result.toString();
	}
}
