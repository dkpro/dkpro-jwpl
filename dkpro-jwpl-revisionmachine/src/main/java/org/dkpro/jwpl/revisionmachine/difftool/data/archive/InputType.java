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
package org.dkpro.jwpl.revisionmachine.difftool.data.archive;

/**
 * This class represents an enumeration of the input type.
 */
public enum InputType
{

    /**
     * Uncompressed XML Input
     */
    XML,

    /**
     * SevenZip Compressed XML Input
     */
    SEVENZIP,

    /**
     * BZip2 Compressed XML Input
     */
    BZIP2;

    /**
     * Parses the string representation to the related InputType.
     *
     * @param s
     *            String representation of the InputType.
     * @return InputType Enumerator
     * @throws IllegalArgumentException
     *             if the parsed String does not match with one of the enumerators
     */
    public static InputType parse(final String s)
    {

        String t = s.toUpperCase();
        return switch (t) {
            case "XML" -> XML;
            case "SEVENZIP" -> SEVENZIP;
            case "BZIP2" -> BZIP2;
            default -> throw new IllegalArgumentException("Unknown InputType : " + s);
        };

    }

    /**
     * Derives the InputType of a dump from the extension of its file name. Setting the type by
     * hand is easily overlooked, and a compressed dump left at the default of an uncompressed one
     * fails only once the DiffTool is running (see issue #105).
     *
     * @param path
     *            The path or name of a dump file. May be {@code null}.
     * @return The InputType matching the extension of the file, or {@code null} if the extension
     *         does not identify one of the supported types.
     */
    public static InputType fromPath(final String path)
    {
        if (path == null) {
            return null;
        }

        String name = path.trim().toLowerCase();
        if (name.endsWith(".bz2")) {
            return BZIP2;
        }
        if (name.endsWith(".7z")) {
            return SEVENZIP;
        }
        if (name.endsWith(".xml")) {
            return XML;
        }
        return null;
    }
}
