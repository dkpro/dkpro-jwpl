/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api.exception;


/**
 * Thrown when an exceptional situation occurs during parsing a page title to create a Title object.
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
