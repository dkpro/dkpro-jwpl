/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.jwpl.api.exception;

/**
 * Thrown when an exceptional situation occurs during parsing a page title to create a Title object.
 */
public class WikiTitleParsingException
    extends WikiApiException
{

    private static final long serialVersionUID = 7152744066557304950L;

    /**
     * Creates an empty message {@link WikiTitleParsingException}.
     */
    public WikiTitleParsingException()
    {
        super();
    }

    /**
     * Creates a {@link WikiTitleParsingException} detailed by {@code message}.
     *
     * @param message The textual notification for the cause or error information.
     */
    public WikiTitleParsingException(String message)
    {
        super(message);
    }

    /**
     * Creates a {@link WikiTitleParsingException} detailed by {@code message}.
     *
     * @param message The textual notification for the cause or error information.
     * @param cause The original {@link Throwable cause} that caused an exceptional situation.
     */
    public WikiTitleParsingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Creates a {@link WikiTitleParsingException} detailed by {@code message}.
     *
     * @param cause The original {@link Throwable cause} that caused an exceptional situation.
     */
    public WikiTitleParsingException(Throwable cause)
    {
        super(cause);
    }
}
