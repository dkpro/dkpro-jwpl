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

    private InputStream inputStream = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(InputStream inputStream, DumpTableEnum table) throws IOException
    {
        // just read from the stream without any data manipulations
        this.inputStream = inputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        return inputStream.read();
    }

}
