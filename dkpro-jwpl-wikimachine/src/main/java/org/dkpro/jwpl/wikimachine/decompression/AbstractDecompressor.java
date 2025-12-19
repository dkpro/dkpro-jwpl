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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public abstract class AbstractDecompressor implements IDecompressor {

    /**
     * Attempts to open an {@link InputStream} to an external or internal resource.
     * In this context, external resources a referenced via a relative or absolute path, including
     * the actual file name of that resource.
     * In case only a plain file name is given and no directory or path elements are contained
     * in {@code resource}, an attempt is made to detect and load the resource from the classpath.
     *
     * @param resource References a resource via a path or by its file name only.
     *                 If {@code null}, this will result in an {@link IOException}.
     * @return An open {@link InputStream} or {@code null} if {@code resource} could not be found.
     * 
     * @throws IOException Thrown if IO errors occurred.
     */
    protected InputStream openStream(String resource) throws IOException
    {
        if (resource == null) {
            throw new IOException("Can't load a 'null' resource!");
        }
        final InputStream in;
        final Path file = Paths.get(resource).toAbsolutePath();
        if (Files.exists(file)) {
            in = Files.newInputStream(file);
        } else {
            in = getContextClassLoader().getResourceAsStream(resource);
        }
        return in;
    }

    /**
     * Attempts to open a {@link SeekableByteChannel} to an external or internal resource.
     * In this context, external resources a referenced via a relative or absolute path, including
     * the actual file name of that resource.
     * In case only a plain file name is given and no directory or path elements are contained
     * in {@code resource}, an attempt is made to detect and load the resource from the classpath.
     *
     * @param resource References a resource via a path or by its file name only.
     *                 If {@code null}, this will result in an {@link IOException}.
     * @return An open {@link SeekableByteChannel} or {@code null} if {@code resource}
     *         could not be found.
     *
     * @throws IOException Thrown if IO errors occurred.
     */
    protected SeekableByteChannel openChannel(String resource) throws IOException {
        if (resource == null) {
            throw new IOException("Can't load a 'null' resource!");
        }
        final Path file = Paths.get(resource).toAbsolutePath();
        if (Files.exists(file)) {
            return Files.newByteChannel(file, StandardOpenOption.READ);
        } else {
            URL in = getContextClassLoader().getResource(resource);
            try {
                return FileChannel.open(Path.of(in.toURI()), StandardOpenOption.READ);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }
    }

    private ClassLoader getContextClassLoader()
    {
       return Thread.currentThread().getContextClassLoader();
    }
}
