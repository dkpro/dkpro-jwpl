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
 * Thrown when a requested page or category could not be found in Wikipedia.
 * @author zesch
 *
 */
public class WikiPageNotFoundException extends WikiApiException {

    static final long serialVersionUID = 1L;

    public WikiPageNotFoundException() {
        super();
    }
    
    public WikiPageNotFoundException(String txt) {
        super(txt);
    }
    
    public WikiPageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public WikiPageNotFoundException(Throwable cause) {
        super(cause);
    }
}
