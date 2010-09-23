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

import de.tudarmstadt.ukp.wikipedia.parser.ContentElement;

/**
 * This Interface makes it possible to parse a single content element.
 * Some TemplateParses might uses this Feauture.
 * @author CJacobi
 *
 */
interface MediaWikiContentElementParser {
	/**
	 * Parses a ContentElement from a String.
	 */
	ContentElement parseContentElement( String src );
}
