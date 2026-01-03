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
package org.dkpro.jwpl.wikimachine.decompression;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

/**
 * A {@link IDecompressor decompressor} implementation for archives in {@code gzip} format.
 * Uses {@link IDecompressor#getInputStream(Path)} to set up the archive
 * path and returns the {@link InputStream} to read from.
 *
 * @see IDecompressor
 * @see AbstractDecompressor
 */
public final class GZipDecompressor
    extends AbstractDecompressor implements IDecompressor
{

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream(String resource) throws IOException {
        checkResource(resource);
        return getInputStream(Path.of(resource));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream(Path resource) throws IOException
    {
        checkResource(resource);
        return new GZIPInputStream(new BufferedInputStream(openStream(resource)));
    }

}
