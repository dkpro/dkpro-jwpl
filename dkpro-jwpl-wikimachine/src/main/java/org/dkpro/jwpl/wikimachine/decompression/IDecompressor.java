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
import java.nio.file.Path;
import java.util.List;

/**
 * Uses an archive file path and returns an {@link InputStream}.
 */
public interface IDecompressor
{
    /**
     * Attempts to open an {@link InputStream} to a compressed archive.
     * In this context, external archives are referenced via a relative or absolute path,
     * including the actual file name of that resource.
     * In case only a plain file name is given and no directory or path elements are contained
     * in {@code resource}, an attempt is made to detect and load the resource from the classpath.
     *
     * @param resource References an archive via a path or by its file name only.
     *                 Must not be {@code null} and not be blank.
     * @return An open {@link InputStream} or {@code null} if the archive could not be found.
     *
     * @throws IllegalArgumentException Thrown if parameter {@code resource} is invalid.
     * @throws IOException Thrown if (other) IO errors occurred.
     */
    InputStream getInputStream(String resource) throws IOException;

    /**
     * Attempts to open an {@link InputStream} to a compressed archive.
     * In this context, external archives are referenced via a relative or absolute path,
     * including the actual file name of that resource.
     * In case only a plain file name is given and no directory or path elements are contained
     * in {@code resource}, an attempt is made to detect and load the resource from the classpath.
     *
     * @param resource References an archive via a {@link Path} or by its file name only.
     *                 Must not be {@code null} and not refer to a directory.
     * @return An open {@link InputStream} or {@code null} if the archive could not be found.
     *
     * @throws IllegalArgumentException Thrown if parameter {@code resource} is invalid.
     * @throws IOException Thrown if (other) IO errors occurred.
     */
    InputStream getInputStream(Path resource) throws IOException;


    /**
     * Attempts to open an {@link InputStream} to a compressed archive in a multiple files format.
     * These archives a combined over a sequence of files in a logical order, that is, via
     * page numbers in ascending order.
     * <p>
     * In this context, external archives are referenced via a relative or absolute path,
     * including the actual file names of all multi-file resources.
     * In case only a plain file names are given and no directory or path elements are contained
     * in {@code resource}, an attempt is made to detect and load the resources from the classpath.
     *
     * @param resources References an archive via an ordered list of {@link Path paths} of all
     *                  relevant files. Must not be {@code null}, not be {@code empty} and not
     *                  refer to directories. All elements in {@code resources} must not
     *                  be {@code null}.
     * @return An open {@link InputStream} for a sequence of resources (multi-file)
     *         or {@code null} if the archive could not be found.
     *
     * @throws IllegalArgumentException Thrown if parameter {@code resource} is invalid.
     * @throws IOException Thrown if (other) IO errors occurred.
     */
    InputStream getInputStreamSequence(List<Path> resources) throws IOException;
}
