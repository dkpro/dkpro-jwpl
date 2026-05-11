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
import java.io.SequenceInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 * A {@link IDecompressor decompressor} implementation for archives in {@code bzip2} format.
 * Uses {@link IDecompressor#getInputStream(Path)} to set up the archive
 * path and returns the {@link InputStream} to read from.
 *
 * @see IDecompressor
 * @see AbstractDecompressor
 */
public final class BZip2Decompressor
    extends AbstractDecompressor implements IDecompressor
{

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream(String resource) throws IOException
    {
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
        return new BZip2CompressorInputStream(new BufferedInputStream(openStream(resource)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStreamSequence(List<Path> resources) throws IOException {
        if (resources == null || resources.isEmpty()) {
            throw new IllegalArgumentException("Can't process a 'null' or 'empty' resources list!");
        }
        resources.forEach(this::checkResource);
        // Decompress each part independently and concatenate the decompressed streams.
        // This mirrors the gzip impl and avoids depending on the compressed-side
        // multi-stream detection heuristic (which is sensitive to the underlying
        // stream's available() at part boundaries).
        final List<InputStream> streams = new ArrayList<>(resources.size());
        try {
            for (Path p : resources) {
                streams.add(new BZip2CompressorInputStream(new BufferedInputStream(openStream(p))));
            }
            return new SequenceInputStream(Collections.enumeration(streams));
        } catch (IOException | RuntimeException e) {
            closeQuietly(streams, e);
            throw e;
        }
    }
}
