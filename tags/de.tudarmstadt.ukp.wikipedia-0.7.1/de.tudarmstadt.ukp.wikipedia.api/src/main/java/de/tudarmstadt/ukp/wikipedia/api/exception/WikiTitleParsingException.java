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
package de.tudarmstadt.ukp.wikipedia.api.exception;


/**
 * Thrown when an exceptional situation occurs during parsing a page title to create a Title object. 
 * @author zesch
 *
 */
public class WikiTitleParsingException extends WikiApiException {

    static final long serialVersionUID = 1L;

    public WikiTitleParsingException() {
        super();
    }
    
    public WikiTitleParsingException(String txt) {
        super(txt);
    }
    
    public WikiTitleParsingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public WikiTitleParsingException(Throwable cause) {
        super(cause);
    }
}
