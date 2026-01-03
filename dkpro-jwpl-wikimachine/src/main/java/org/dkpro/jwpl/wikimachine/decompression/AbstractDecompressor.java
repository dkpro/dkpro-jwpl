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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A common base {@link IDecompressor} implementation that provides methods to open
 * resources via a specified {@link Path}.
 *
 * @see IDecompressor
 */
public abstract class AbstractDecompressor implements IDecompressor {

    /**
     * Attempts to open an {@link InputStream} to an external or internal resource.
     * In this context, external resources are referenced via a relative or absolute path,
     * including the actual file name of that resource.
     * In case only a plain file name is given and no directory or path elements are contained
     * in {@code resource}, an attempt is made to detect and load the resource from the classpath.
     *
     * @param resource References a resource via a {@link Path} or by its file name only.
     *                 Must not be {@code null} and not refer to a directory.
     * @return An open {@link InputStream} or {@code null} if {@code resource} could not be found.
     * 
     * @throws IllegalArgumentException Thrown if the parameters were invalid.
     * @throws IOException Thrown if (other) IO errors occurred.
     */
    protected InputStream openStream(Path resource) throws IOException
    {
        checkResource(resource);
        final InputStream in;
        if (Files.exists(resource)) {
            in = Files.newInputStream(resource);
        } else {
            in = getContextClassLoader().getResourceAsStream(resource.toString());
        }
        return in;
    }

    /**
     * Attempts to open a {@link SeekableByteChannel} to an external or internal resource.
     * In this context, external resources are referenced via a relative or absolute path,
     * including the actual file name of that resource.
     * In case only a plain file name is given and no directory or path elements are contained
     * in {@code resource}, an attempt is made to detect and load the resource from the classpath.
     *
     * @param resource References a resource via a {@link Path} or by its file name only.
     *                 Must not be {@code null} and not refer to a directory.
     * @return An open {@link SeekableByteChannel} or {@code null} if {@code resource}
     *         could not be found.
     *
     * @throws IllegalArgumentException Thrown if the parameters were invalid.
     * @throws IOException Thrown if (other) IO errors occurred.
     */
    protected SeekableByteChannel openChannel(Path resource) throws IOException {
        checkResource(resource);
        if (Files.exists(resource)) {
            return Files.newByteChannel(resource, StandardOpenOption.READ);
        } else {
            final URL in = getContextClassLoader().getResource(resource.toString());
            try {
                if (in != null) {
                    return FileChannel.open(Path.of(in.toURI()), StandardOpenOption.READ);
                } else {
                    return null;
                }
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Checks if the specified (file) resource is not {@code null} and not blank.
     *
     * @param resource The resource to check for. Must not be {@code null} and not be blank.
     * @throws IllegalArgumentException Thrown if the parameter {@code resource} was invalid.
     */
    protected void checkResource(String resource) {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("Can't load a 'null' or 'empty' file resource!");
        }
    }

    /**
     * Checks if the specified (file) resource is not {@code null} and not blank.
     *
     * @param resource The resource to check for. Must not be {@code null} and not be blank.
     * @throws IllegalArgumentException Thrown if the parameter {@code resource} was invalid.
     * @throws InvalidPathException Thrown if the parameter {@code resource} referred to a directory.
     */
    protected void checkResource(Path resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Can't load a 'null' or 'empty' file resource!");
        }
        checkResource(resource.toString());
        if (Files.isDirectory(resource)) {
            throw new InvalidPathException(resource.toString(), "Can't load a 'directory' as resource!");
        }
    }

    private ClassLoader getContextClassLoader()
    {
       return Thread.currentThread().getContextClassLoader();
    }
}
