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

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link IDecompressor decompressor} implementation for archives in {@code 7z} format.
 * Uses {@link IDecompressor#getInputStream(Path)} to set up the archive
 * path and returns the first valid {@link InputStream} to read from.
 *
 * @see IDecompressor
 * @see AbstractDecompressor
 */
public final class SevenZipDecompressor
    extends AbstractDecompressor implements IDecompressor
{

    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public InputStream getInputStream(String resource) throws IOException {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("Can't load a 'null' or 'empty' file resource!");
        }
        return getInputStream(Path.of(resource));
    }

    @Override
    public InputStream getInputStream(Path resource) throws IOException {
        final SeekableByteChannel sbc = openChannel(resource);
        if (sbc != null && sbc.isOpen()) {
            final SevenZFile archive = SevenZFile.builder().setSeekableByteChannel(sbc).get();
            // ensure the processed archive file is properly closed silently on JVM shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    archive.close();
                } catch (IOException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }));
            if (archive != null) {
                // Assuming mediawiki 7z dump has only one XML entry...
                final SevenZArchiveEntry entry = archive.getNextEntry();
                if (entry != null && entry.hasStream()) {
                    return archive.getInputStream(entry);
                } else {
                    return null;
                }
            }
        }
        return null;
    }
}
