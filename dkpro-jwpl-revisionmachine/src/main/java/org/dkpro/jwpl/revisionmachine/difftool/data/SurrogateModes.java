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

/**
 * This Enumerator lists the different method of how to handle surrogates.
 * <p>
 * TODO: The surrogate mode implementations need a work over. TODO Add documentation for surrogates
 */
public enum SurrogateModes
{

    /**
     * Replace the surrogate TODO COULD BE FAULTY. CHECK BEFORE USING!!! DISABLED FOR NOW!
     */
    REPLACE,

    /**
     * Throw an error if a surrogate is detected TODO COULD BE FAULTY. CHECK BEFORE USING!!!
     * DISABLED FOR NOW!
     */
    THROW_ERROR,

    /**
     * Discard the rest of the article after a surrogate is detected TODO COULD BE FAULTY. CHECK
     * BEFORE USING!!! DISABLED FOR NOW!
     */
    DISCARD_REST,

    /**
     * Discard revisions which contain surrogates (java default setting)
     */
    DISCARD_REVISION;

    /**
     * Parses the given string.
     *
     * @param s
     *            string
     * @return SurrogateModes
     */
    public static SurrogateModes parse(final String s)
    {

        String t = s.toUpperCase();

        final String msg = "This mode is currently not supported. " +
                "Please check the implementation first. For now, you can use the default mode DISCARD_REVISION";
        // return REPLACE;
        // return THROW_ERROR;
        // return DISCARD_REST;
        return switch (t) {
            case "REPLACE", "THROW_ERROR", "DISCARD_REST" -> throw new UnsupportedOperationException(msg);
            case "DISCARD_REVISION" -> DISCARD_REVISION;
            default -> throw new IllegalArgumentException("Unknown SurrogateModes : " + s);
        };

    }
}
