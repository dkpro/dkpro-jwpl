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
 * A parser for the 'text' table.<br/>
 * Accessing the field values is only possible each time the method {@link #next()} returned {@code true}.
 *
 * @see AutoCloseable
 */
public class TextParser
    implements AutoCloseable
{

    /*
     * Corresponding fields of the 'text' table.
     */
    private int oldId;
    private String oldText;
    private UTFDataInputStream stream;

    /**
     * Sets an input stream to text parse with.
     *
     * @param inputStream An open {@link InputStream} to set for text parsing.
     */
    public void setInputStream(InputStream inputStream)
    {
        stream = new UTFDataInputStream(inputStream);
    }

    /**
     * @return Returns the value of {@code old_id}.
     */
    public int getOldId()
    {
        return oldId;
    }

    /**
     * @return Returns the value of {@code old_text}.
     */
    public String getOldText()
    {
        return oldText;
    }

    /**
     * @return {@code true} if the table has more rows, {@code false} otherwise.
     * @throws IOException  Thrown if IO errors occurred.
     */
    public boolean next() throws IOException
    {
        boolean hasNext = true;
        try {
            oldId = stream.readInt();
            oldText = stream.readUTFAsArray();
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
        stream.close();
    }
}
