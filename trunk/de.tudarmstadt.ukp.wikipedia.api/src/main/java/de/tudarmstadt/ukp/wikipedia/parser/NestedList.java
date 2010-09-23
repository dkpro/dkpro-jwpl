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
 * A NestedList can contain ContentElements or other NestedLists, 
 * for this purpose and to avoid a improper use, this interface has been created.<br/>
 * 
 * Now, we got a NestedListContainer wich contains NestedLists<br/>
 * A NestedList can be a NestedListContainer or a NestedListElement.
 * 
 * @author CJacobi
 *
 */
public interface NestedList extends Content {
	public SrcSpan getSrcSpan();
}
