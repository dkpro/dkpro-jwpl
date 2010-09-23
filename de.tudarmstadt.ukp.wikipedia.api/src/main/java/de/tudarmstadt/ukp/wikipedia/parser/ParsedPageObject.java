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
 * All clases in parsedpage package, which can be created by a 
 * parser, extending this class. So it is possible for these 
 * classes to refer to a SourceCode.
 * @author CJacobi
 *
 */
public abstract class ParsedPageObject {
	private SrcSpan srcSpan;

	/**
	 * Returns a Span refering to a SourceCode.
	 */
	public SrcSpan getSrcSpan() {
		return srcSpan;
	}

	public void setSrcSpan(SrcSpan srcSpan) {
		this.srcSpan = srcSpan;
	}
}
