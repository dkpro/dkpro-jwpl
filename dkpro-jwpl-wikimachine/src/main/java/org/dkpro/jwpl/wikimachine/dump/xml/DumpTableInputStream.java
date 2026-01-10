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

import java.io.IOException;
import java.io.InputStream;

/**
 * An abstraction of an {@link InputStream} for Wikipedia table dumps of three {@link DumpTableEnum types}.
 *
 * @see DumpTableEnum
 */
public abstract class DumpTableInputStream
    extends InputStream
{
    /**
     * Initializes the specified {@link InputStream} depending on its {@link DumpTableEnum table type}.
     *
     * @param inputStream   The input stream to read from.
     * @param table         The {@link DumpTableEnum table type} as additional context information.
     * @throws IOException  Thrown if IO errors occurred.
     */
    public abstract void initialize(InputStream inputStream, DumpTableEnum table)
        throws IOException;
}
