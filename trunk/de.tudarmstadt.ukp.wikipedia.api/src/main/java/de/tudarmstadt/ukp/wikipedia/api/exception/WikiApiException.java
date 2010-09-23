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


public class WikiApiException extends WikiException {

    static final long serialVersionUID = 1L;

    public WikiApiException() {
        super();
    }
    
    public WikiApiException(String txt) {
        super(txt);
    }
    
    public WikiApiException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public WikiApiException(Throwable cause) {
        super(cause);
    }

}
