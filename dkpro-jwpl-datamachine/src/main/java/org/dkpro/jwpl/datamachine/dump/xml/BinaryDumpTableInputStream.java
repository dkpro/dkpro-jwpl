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
package org.dkpro.jwpl.datamachine.dump.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableEnum;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableInputStream;

/**
 * A binary implementation of {@link DumpTableInputStream} for Wikipedia table dumps
 * of three {@link DumpTableEnum types}.
 *
 * @see DumpTableEnum
 * @see DumpTableInputStream
 */
public class BinaryDumpTableInputStream
    extends DumpTableInputStream
{

    /**
     * Size of the read buffer placed below this wrapper if the given stream is not buffered yet.
     */
    private static final int BUFFER_SIZE = 1 << 16;

    private InputStream inputStream = null;

    /**
     * Initializes this stream to read from the given {@link InputStream} without any data
     * manipulations. Unbuffered sources (e.g. a {@code GZIPInputStream}) are wrapped in a
     * {@link BufferedInputStream} so that single-byte reads do not hit the source per byte.
     *
     * @param inputStream   The input stream to read from. Must not be {@code null}.
     * @param table         The {@link DumpTableEnum table type} as additional context information.
     * @throws IOException  Thrown if IO errors occurred.
     * @throws IllegalArgumentException Thrown if {@code inputStream} is {@code null}.
     */
    @Override
    public void initialize(InputStream inputStream, DumpTableEnum table) throws IOException
    {
        if (inputStream == null) {
            throw new IllegalArgumentException("'inputStream' must not be null.");
        }
        if (inputStream instanceof BufferedInputStream) {
            this.inputStream = inputStream;
        }
        else {
            this.inputStream = new BufferedInputStream(inputStream, BUFFER_SIZE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        return inputStream.read();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        return inputStream.read(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long skip(long n) throws IOException
    {
        return inputStream.skip(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException
    {
        return inputStream.available();
    }

    /**
     * Closes the underlying stream. Does nothing if this stream was never initialized.
     */
    @Override
    public void close() throws IOException
    {
        if (inputStream != null) {
            inputStream.close();
        }
    }

}
