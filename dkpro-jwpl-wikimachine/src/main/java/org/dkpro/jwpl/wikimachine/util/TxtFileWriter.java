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
package org.dkpro.jwpl.wikimachine.util;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Writes the dumps of tables as txt files.
 *
 * @see PrintStream
 */
public class TxtFileWriter
    extends PrintStream
{

    private static final boolean AUTOFLUSH = false;

    /**
     * Instantiates a new {@link TxtFileWriter} object.
     *
     * @param filename The name of the file to write to.
     *                 
     * @throws IOException Thrown if IO errors occurred.
     */
    public TxtFileWriter(String filename) throws IOException
    {
        super(new BufferedOutputStream(new FileOutputStream(filename)), AUTOFLUSH,
                StandardCharsets.UTF_8);
    }

    /**
     * Add one or more rows to the dump of the table.
     *
     * @param row  The (text) data to add.
     */
    public void addRow(Object... row)
    {
        super.print(Strings.join(row, "\t") + "\n");
    }

    /**
     * Exports the accumulated (text) rows to the output file.
     * <p>
     * Note:<br/>
     * After calling {@code export()}, the underlying stream is closed, that is,
     * calling {@link #addRow(Object...)} has no effect.
     */
    public void export()
    {
        super.flush();
        super.close();
    }

}
