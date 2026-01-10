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
package org.dkpro.jwpl.wikimachine.dump.xml;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.dkpro.jwpl.wikimachine.util.UTFDataInputStream;

/**
 * A parser for the 'page' table.<br/>
 * Accessing the field values is only possible each time the method {@link #next()} returned {@code true}.
 *
 * @see AutoCloseable
 */
public class PageParser
    implements AutoCloseable
{
    /*
     * Corresponding fields of the 'page' table.
     */
    private int pageId;
    private int pageNamespace;
    private String pageTitle;
    private boolean pageIsRedirect;

    private UTFDataInputStream stream;

    /**
     * Sets an input stream to page parse with.
     *
     * @param inputStream An open {@link InputStream} to set for page parsing.
     */
    public void setInputStream(InputStream inputStream)
    {
        stream = new UTFDataInputStream(inputStream);
    }

    /**
     * @return Returns the value of {@code page_id}.
     */
    public int getPageId()
    {
        return pageId;
    }

    /**
     * @return Returns the value of {@code page_is_redirect}.
     */
    public boolean getPageIsRedirect()
    {
        return pageIsRedirect;
    }

    /**
     * @return Returns the value of {@code page_namespace}.
     */
    public int getPageNamespace()
    {
        return pageNamespace;
    }

    /**
     * @return Returns the value of {@code page_title}.
     */
    public String getPageTitle()
    {
        return pageTitle;
    }

    /**
     * @return {@code true} if the table has more rows, {@code false} otherwise.
     * @throws IOException  Thrown if IO errors occurred.
     */
    public boolean next() throws IOException
    {
        boolean hasNext = true;
        try {
            pageId = stream.readInt();
            pageNamespace = stream.readInt();
            pageTitle = stream.readUTFAsArray();
            pageIsRedirect = stream.readBoolean();
        }
        catch (EOFException e) {
            hasNext = false;
        }
        return hasNext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        if (stream != null) {
            stream.close();
        }
    }
}
