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
package org.dkpro.jwpl.wikimachine.debug;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An {@link InputStream} variant that transparently copies its content over
 * to an {@link OutputStream} while being read.
 */
public class InputStreamSpy
    extends InputStream
{

    private final InputStream iStream;
    private final OutputStream oStream;

    /**
     * Instantiates a {@link InputStreamSpy} with the specified parameters.
     * @param iStream  An open {@link InputStream} to consume from.
     * @param oStream  An open {@link OutputStream} to produce to.
     */
    public InputStreamSpy(InputStream iStream, OutputStream oStream)
    {
        this.iStream = iStream;
        this.oStream = oStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        int result = iStream.read();
        oStream.write(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException
    {
        return iStream.available();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        iStream.close();
        oStream.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mark(int readlimit)
    {
        iStream.mark(readlimit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() throws IOException
    {
        iStream.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean markSupported()
    {
        return iStream.markSupported();
    }

}
