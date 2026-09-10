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
package org.dkpro.jwpl.revisionmachine.common.util;

import java.io.File;

/**
 * Joins the path fragments of a configuration with the file names built from them.
 * <p>
 * The ConfigGUI appends a trailing separator to every directory it writes, but a configuration
 * written by hand needs not, and appending a file name to such a directory used to produce a path
 * like {@code /data/dumpsdewiki.sql} (see issue #14).
 */
public final class FilePaths
{

    private FilePaths()
    {
        // static-only
    }

    /**
     * Joins path fragments, inserting the separator of the platform between two fragments that
     * carry none and collapsing the separators where both of them carry one. A fragment that is
     * {@code null} or empty is skipped, so a configuration that leaves an optional sub directory
     * empty yields the path without it.
     *
     * @param first The first fragment, typically a directory from the configuration.
     * @param more  The remaining fragments, of which the last is typically a file name.
     * @return The joined path.
     */
    public static String resolve(String first, String... more)
    {
        StringBuilder path = new StringBuilder(first == null ? "" : first);
        for (String fragment : more) {
            if (fragment == null || fragment.isEmpty()) {
                continue;
            }
            int from = 0;
            if (path.isEmpty()) {
                // nothing to join to yet, so the fragment is the start of the path
            }
            else if (endsWithSeparator(path)) {
                // skip the separators the fragment carries, rather than doubling them
                while (from < fragment.length() && isSeparator(fragment.charAt(from))) {
                    from++;
                }
            }
            else if (!startsWithSeparator(fragment)) {
                path.append(File.separatorChar);
            }
            path.append(fragment, from, fragment.length());
        }
        return path.toString();
    }

    private static boolean endsWithSeparator(CharSequence path)
    {
        return isSeparator(path.charAt(path.length() - 1));
    }

    private static boolean startsWithSeparator(String fragment)
    {
        return isSeparator(fragment.charAt(0));
    }

    private static boolean isSeparator(char c)
    {
        // a configuration written on one platform is regularly read on the other
        return c == File.separatorChar || c == '/' || c == '\\';
    }
}
