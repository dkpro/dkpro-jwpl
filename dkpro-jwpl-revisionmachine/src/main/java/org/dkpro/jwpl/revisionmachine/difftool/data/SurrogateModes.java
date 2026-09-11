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
package org.dkpro.jwpl.revisionmachine.difftool.data;

import java.util.Locale;

/**
 * This Enumerator lists the different methods of how to handle revisions whose text contains
 * surrogate characters, i.e. UTF-16 code units in the range {@code U+D800} to {@code U+DFFF}. They
 * encode the characters outside the Basic Multilingual Plane, such as emoji, and also occur
 * unpaired in malformed text.
 */
public enum SurrogateModes
{

    /**
     * Replace each surrogate character with {@code '?'}, so that a character outside the Basic
     * Multilingual Plane becomes {@code "??"}.
     */
    REPLACE,

    /**
     * Stop with an error as soon as a revision contains a surrogate character.
     */
    THROW_ERROR,

    /**
     * Keep only the text of a revision that precedes its first surrogate character.
     */
    DISCARD_REST,

    /**
     * Discard revisions which contain surrogate characters (default setting)
     */
    DISCARD_REVISION;

    /**
     * Parses the given string, ignoring case.
     *
     * @param s
     *            string
     * @return SurrogateModes
     * @throws IllegalArgumentException
     *             if the string names no surrogate mode
     */
    public static SurrogateModes parse(final String s)
    {
        return switch (s.toUpperCase(Locale.ROOT)) {
            case "REPLACE" -> REPLACE;
            case "THROW_ERROR" -> THROW_ERROR;
            case "DISCARD_REST" -> DISCARD_REST;
            case "DISCARD_REVISION" -> DISCARD_REVISION;
            default -> throw new IllegalArgumentException("Unknown SurrogateModes : " + s);
        };
    }
}
