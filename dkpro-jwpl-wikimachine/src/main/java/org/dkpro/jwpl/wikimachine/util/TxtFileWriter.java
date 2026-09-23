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
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes the dumps of tables as txt files.
 * <p>
 * Rows are written as tab-separated values, terminated by {@code \n} and encoded as UTF-8.
 * As {@link PrintStream} does not propagate {@link IOException IO errors}, {@link #close()}
 * and {@link #export()} check the error state of the stream and throw an
 * {@link UncheckedIOException} if any write failed.
 *
 * @see PrintStream
 */
public class TxtFileWriter
    extends PrintStream
{

    private static final boolean AUTOFLUSH = false;

    /** Reusable buffer for assembling a single row; guarded by {@code this}. */
    private final StringBuilder rowBuffer = new StringBuilder(256);

    private boolean closed = false;

    /**
     * Instantiates a new {@link TxtFileWriter} object.
     *
     * @param filename The name of the file to write to.
     *                 
     * @throws IOException Thrown if IO errors occurred.
     */
    public TxtFileWriter(String filename) throws IOException
    {
        this(new FileOutputStream(filename));
    }

    /**
     * Instantiates a new {@link TxtFileWriter} object writing to the given stream.
     *
     * @param out The stream to write to.
     */
    TxtFileWriter(OutputStream out)
    {
        super(new BufferedOutputStream(out), AUTOFLUSH, StandardCharsets.UTF_8);
    }

    /**
     * Add a row consisting of two integer values to the dump of the table.
     *
     * @param first  The value of the first column.
     * @param second The value of the second column.
     */
    public synchronized void addRow(int first, int second)
    {
        rowBuffer.setLength(0);
        rowBuffer.append(first).append('\t').append(second).append('\n');
        super.append(rowBuffer);
    }

    /**
     * Add one or more rows to the dump of the table.
     *
     * @param row  The (text) data to add.
     */
    public synchronized void addRow(Object... row)
    {
        rowBuffer.setLength(0);
        for (int i = 0; i < row.length; i++) {
            if (i > 0) {
                rowBuffer.append('\t');
            }
            rowBuffer.append(row[i]);
        }
        rowBuffer.append('\n');
        super.append(rowBuffer);
    }

    /**
     * Exports the accumulated (text) rows to the output file.
     * <p>
     * Note:<br/>
     * After calling {@code export()}, the underlying stream is closed, that is,
     * calling {@link #addRow(Object...)} has no effect.
     *
     * @throws UncheckedIOException Thrown if writing to the output file failed.
     */
    public void export()
    {
        close();
    }

    /**
     * Flushes and closes the stream. Subsequent calls have no effect.
     *
     * @throws UncheckedIOException Thrown if writing to or closing the output file failed.
     */
    @Override
    public synchronized void close()
    {
        if (closed) {
            return;
        }
        closed = true;
        // checkError() flushes the stream before reporting the error state
        boolean failed = checkError();
        super.close();
        if (failed || checkError()) {
            throw new UncheckedIOException(
                    new IOException("Failed to write table dump; output is incomplete"));
        }
    }

}
