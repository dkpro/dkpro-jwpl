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
package org.dkpro.jwpl.wikimachine.dump.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.nio.charset.StandardCharsets;

/**
 * This class defines common utilities for the classes {@link CategorylinksParser}
 * and {@link PagelinksParser}.
 *
 * @version 0.2 <br>
 *          <code>SQLFileParser</code> don't create a BufferedReader by himself but entrust it to
 *          <code>BufferedReaderFactory</code>. Thereby, BufferedReaders are created according to
 *          archive type and try to uncompress the file on the fly. (Ivan Galkin 15.01.2009)
 */
abstract class SQLFileParser implements AutoCloseable
{

    /** The stream associated with the SQL content to parse. */
    protected InputStream stream;
    /** The tokenizer instance used to parse the underlying {@link #stream}.*/
    protected StreamTokenizer st;
    /** Whether the end of file has been reached. */
    protected boolean EOF_reached;

    /**
     * Init a {@link SQLFileParser} via an input stream.
     *
     * @param inputStream A valid {@link InputStream} to read SQL content from.
     *                    
     * @throws IOException Thrown if IO errors occurred during initialization.
     */
    protected void init(InputStream inputStream) throws IOException
    {
        stream = inputStream;
        st = new StreamTokenizer(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));

        EOF_reached = false;
        skipStatements();

    }

    /**
     * Skip the SQL statements for table creation and the prefix <br>
     * INSERT INTO TABLE .... VALUES for values insertion.<br>
     * Read tokens until the word 'VALUES' is reached or the EOF.
     *
     * @throws IOException
     *             Thrown if IO errors occurred.
     */
    protected void skipStatements() throws IOException
    {
        while (true) {
            st.nextToken();
            if (null != st.sval && st.sval.equalsIgnoreCase("VALUES")) {
                // the next token is the start of a value
                break;
            }
            if (st.ttype == StreamTokenizer.TT_EOF) {
                // the end of the file is reached
                EOF_reached = true;
                break;
            }
        }
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
     * Must be implemented by the {@link PagelinksParser} and the {@link CategorylinksParser} classes.
     *
     * @return {@code true} if a new value is now available, {@code false} otherwise.
     * @throws IOException
     *             Thrown if IO errors occurred.
     */
    abstract boolean next() throws IOException;
}
