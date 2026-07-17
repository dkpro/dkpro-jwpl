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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

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
    public InputStream getInputStream(Path resource) throws IOException {
        checkResource(resource);
        final SeekableByteChannel sbc = openChannel(resource);
        if (sbc == null || !sbc.isOpen()) {
            return null;
        }
        try {
            final SevenZFile archive = SevenZFile.builder().setSeekableByteChannel(sbc).get();
            if (archive != null) {
                // Assuming mediawiki 7z dump has only one XML entry...
                final SevenZArchiveEntry entry = archive.getNextEntry();
                if (entry != null && entry.hasStream()) {
                    return new SevenZipInputStreamWrapper(archive, archive.getInputStream(entry));
                } else {
                    archive.close();
                    if (sbc.isOpen()) {
                        sbc.close();
                    }
                    return null;
                }
            }
        } catch (IOException e) {
            try {
                sbc.close();
            } catch (IOException suppressed) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }
        if (sbc.isOpen()) {
            sbc.close();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStreamSequence(List<Path> resources) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class SevenZipInputStreamWrapper extends FilterInputStream {
        private final SevenZFile archive;

        protected SevenZipInputStreamWrapper(SevenZFile archive, InputStream delegate) {
            super(delegate);
            this.archive = archive;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                archive.close();
            }
        }
    }
}
