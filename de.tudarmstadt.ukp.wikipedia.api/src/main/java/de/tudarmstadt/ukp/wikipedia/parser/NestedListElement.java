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
 * This is a simple ContentElement, wich occures in a NestedList.
 * @author CJacobi
 *
 */
public class NestedListElement extends ContentElement implements NestedList{

	public String toString(){ return "NLC_IS_CONTENT: true\n"+ super.toString(); }
}
