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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A parser for the 'revision' table.<br/>
 * Accessing the field values is only possible each time the method {@link #next()} returned {@code true}.
 *
 * @see AutoCloseable
 */
public abstract class RevisionParser
    implements AutoCloseable
{

    /** The value of the page column in the 'revision' table. */
    protected int revPage;
    /** The value of the text_id column in the 'revision' table. */
    protected int revTextId;
    /** The value of the timestamp column in the 'revision' table. */
    protected int revTimestamp;

    /** The underlying {@link java.io.DataOutputStream} that feeds in the table's data. */
    protected DataInputStream stream;

    /**
     * Sets an input stream to revision parse with.
     *
     * @param inputStream An open {@link InputStream} to set for revision parsing.
     */
    public void setInputStream(InputStream inputStream)
    {
        stream = new DataInputStream(inputStream);
    }

    /**
     * @return Returns the value of {@code revPage}.
     */
    public int getRevPage()
    {
        return revPage;
    }

    /**
     * @return Returns the value of {@code revTextId}.
     */
    public int getRevTextId()
    {
        return revTextId;
    }

    /**
     * @return Returns the value of {@code revTimestamp}.
     */
    public int getRevTimestamp()
    {
        return revTimestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        stream.close();
    }

    /**
     * @return {@code true} if the table has more rows, {@code false} otherwise.
     * @throws IOException Thrown if IO errors occurred.
     */
    public abstract boolean next() throws IOException;

}
