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
package org.dkpro.jwpl.api;

/**
 * Matches names against the title pattern of a {@link PageQuery} the way {@code LIKE} does on a
 * binary collation: character by character and case-sensitively.
 * <p>
 * The database selects the candidates with {@code name like :pattern} in the collation of the
 * column, so the name index is used. On a case- or accent-insensitive collation, that also selects
 * names which differ from the pattern in case or accents. This matcher removes them, so that a
 * title pattern selects the same pages on every backend and collation.
 * <p>
 * The matcher accepts every name that {@code LIKE} on a binary collation accepts on HSQLDB, MySQL,
 * MariaDB, and PostgreSQL, so it never removes a name the database matched exactly:
 * <ul>
 * <li>{@code %} matches any sequence of characters, {@code _} any single character, be it one
 * UTF-16 unit or a surrogate pair.</li>
 * <li>MySQL, MariaDB, and PostgreSQL treat a backslash as escape character, so {@code \_} matches
 * only {@code _}, while HSQLDB has no default escape character, so it matches a backslash followed
 * by any character. A backslash followed by a character therefore matches either.</li>
 * </ul>
 */
final class TitlePatternMatcher
{
    private static final byte UNKNOWN = 0;
    private static final byte MATCH = 1;
    private static final byte NO_MATCH = 2;

    private final String pattern;

    /**
     * @param pattern The title pattern, must not be {@code null}.
     */
    TitlePatternMatcher(String pattern)
    {
        this.pattern = pattern;
    }

    /**
     * @param name A name the database selected with the pattern, must not be {@code null}.
     * @return Whether the name matches the pattern case- and accent-sensitively.
     */
    boolean matches(String name)
    {
        byte[] memo = new byte[(pattern.length() + 1) * (name.length() + 1)];
        return matches(name, 0, 0, memo);
    }

    private boolean matches(String name, int p, int n, byte[] memo)
    {
        int key = p * (name.length() + 1) + n;
        if (memo[key] != UNKNOWN) {
            return memo[key] == MATCH;
        }
        boolean result = computeMatch(name, p, n, memo);
        memo[key] = result ? MATCH : NO_MATCH;
        return result;
    }

    private boolean computeMatch(String name, int p, int n, byte[] memo)
    {
        if (p == pattern.length()) {
            return n == name.length();
        }
        boolean more = n < name.length();
        char c = pattern.charAt(p);
        switch (c) {
        case '%':
            return matches(name, p + 1, n, memo) || more && matches(name, p, n + 1, memo);
        case '_':
            return more && matches(name, p + 1, n + 1, memo)
                    || n + 1 < name.length() && Character.isSurrogatePair(name.charAt(n),
                            name.charAt(n + 1)) && matches(name, p + 1, n + 2, memo);
        case '\\':
            // the escaped character as literal (MySQL, MariaDB, PostgreSQL) ...
            if (p + 1 < pattern.length() && more && name.charAt(n) == pattern.charAt(p + 1)
                    && matches(name, p + 2, n + 1, memo)) {
                return true;
            }
            // ... or a literal backslash (HSQLDB)
            return more && name.charAt(n) == '\\' && matches(name, p + 1, n + 1, memo);
        default:
            return more && name.charAt(n) == c && matches(name, p + 1, n + 1, memo);
        }
    }
}
