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
 * Thrown, when the Wikipedia object could not be properly initialized.
 * @author zesch
 *
 */
public class WikiInitializationException extends WikiApiException {

    static final long serialVersionUID = 1L;

    public WikiInitializationException() {
        super();
    }
    
    public WikiInitializationException(String txt) {
        super(txt);
    }
    
    public WikiInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public WikiInitializationException(Throwable cause) {
        super(cause);
    }
}
