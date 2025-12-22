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

/**
 * Uses an archive file path and returns an {@link InputStream}.
 */
public interface IDecompressor
{
    /**
     * Attempts to open an {@link InputStream} to a compressed archive.
     * In this context, external archives a referenced via a relative or absolute path, including
     * the actual file name of that resource.
     * In case only a plain file name is given and no directory or path elements are contained
     * in {@code resource}, an attempt is made to detect and load the resource from the classpath.
     *
     * @param resource References an archive via a path or by its file name only.
     *                 If {@code null}, this will result in an {@link IOException}.
     * @return An open {@link InputStream} or {@code null} if the archive could not be found.
     *
     * @throws IOException Thrown if IO errors occurred.
     */
    InputStream getInputStream(String resource) throws IOException;

    /**
     * Attempts to open an {@link InputStream} to a compressed archive.
     * In this context, external archives a referenced via a relative or absolute path, including
     * the actual file name of that resource.
     * In case only a plain file name is given and no directory or path elements are contained
     * in {@code resource}, an attempt is made to detect and load the resource from the classpath.
     *
     * @param resource References an archive via a {@link Path} or by its file name only.
     *                 If {@code null}, this will result in an {@link IOException}.
     * @return An open {@link InputStream} or {@code null} if the archive could not be found.
     *
     * @throws IOException Thrown if IO errors occurred.
     */
    InputStream getInputStream(Path resource) throws IOException;
}
