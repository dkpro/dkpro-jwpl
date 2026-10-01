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
package org.dkpro.jwpl.api.util;

/**
 * Builds {@code LIKE} patterns from literal strings such as titles, so that {@code _} and
 * {@code %} in them match only themselves.
 * <p>
 * The patterns are to be used with an explicit {@link #ESCAPE_CLAUSE}, e.g.
 * {@code "name like ?" + LikePatterns.ESCAPE_CLAUSE}. The escape character is {@code !} rather
 * than the backslash, which is the implicit escape character of MySQL and MariaDB, but not of
 * HSQLDB, and whose meaning in a string literal depends on the {@code NO_BACKSLASH_ESCAPES} SQL
 * mode of MySQL and MariaDB. With an explicit {@code escape '!'}, the patterns behave the same on
 * HSQLDB, MySQL, MariaDB, and PostgreSQL, in native SQL as well as in HQL.
 */
public final class LikePatterns
{
    /** The character escaping {@code %}, {@code _}, and itself in the patterns of this class. */
    public static final char ESCAPE_CHAR = '!';

    /**
     * The {@code ESCAPE} clause to append to every {@code LIKE} predicate whose pattern is built by
     * this class, including the leading blank.
     */
    public static final String ESCAPE_CLAUSE = " escape '" + ESCAPE_CHAR + "'";

    private LikePatterns()
    {
        // utility class
    }

    /**
     * Escapes the wildcards {@code %} and {@code _} as well as {@link #ESCAPE_CHAR} itself.
     *
     * @param literal The string to match literally, must not be {@code null}.
     * @return A pattern matching exactly {@code literal}, when used with {@link #ESCAPE_CLAUSE}.
     */
    public static String escape(String literal)
    {
        StringBuilder pattern = new StringBuilder(literal.length() + 8);
        appendEscaped(pattern, literal);
        return pattern.toString();
    }

    /**
     * @param literalPrefix The prefix to match literally, must not be {@code null}.
     * @return A pattern matching every string that starts with {@code literalPrefix}, when used
     *         with {@link #ESCAPE_CLAUSE}.
     */
    public static String prefix(String literalPrefix)
    {
        StringBuilder pattern = new StringBuilder(literalPrefix.length() + 9);
        appendEscaped(pattern, literalPrefix);
        return pattern.append('%').toString();
    }

    private static void appendEscaped(StringBuilder pattern, String literal)
    {
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            if (c == ESCAPE_CHAR || c == '%' || c == '_') {
                pattern.append(ESCAPE_CHAR);
            }
            pattern.append(c);
        }
    }
}
