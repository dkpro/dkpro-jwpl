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
package org.dkpro.jwpl.wikimachine.dump.sql;

/**
 * The single method {@link SQLEscape#escape} removes all unwanted escape characters from a string
 * to make is SQL conform. Maybe not thread-save.
 */
public class SQLEscape
{
    /** Marker returned by {@link #escapeCode(char)} for characters that are copied as they are. */
    private static final char NO_ESCAPE = '\uffff';

    private SQLEscape()
    {
    }

    /**
     * @param str The unescaped String.
     * @return String with escape characters.
     * @see SQLEscape
     */
    public static String escape(String str)
    {
        if (str == null || str.isBlank()) {
            return "";
        }
        final int len = str.length();

        int first = 0;
        while (first < len && escapeCode(str.charAt(first)) == NO_ESCAPE) {
            first++;
        }
        if (first == len) {
            // Nothing to escape: the input is already the result.
            return str;
        }

        // Most text has few escapable characters, so a small headroom avoids allocating twice
        // the input length; escape-heavy input simply grows the builder.
        final StringBuilder sql = new StringBuilder(len + (len >> 4) + 16);
        sql.append(str, 0, first);
        for (int i = first; i < len; i++) {
            final char c = str.charAt(i);
            final char code = escapeCode(c);
            if (code == NO_ESCAPE) {
                sql.append(c);
            }
            else {
                sql.append('\\').append(code);
            }
        }
        return sql.toString();
    }

    /**
     * @param c The character to check.
     * @return The character to emit after a backslash for {@code c}, or {@link #NO_ESCAPE} if
     *         {@code c} is copied as it is.
     */
    private static char escapeCode(char c)
    {
        switch (c) {
        case '\u0000':
            return '0';
        case '\n':
            return 'n';
        case '\t':
            return 't';
        case '\r':
            return 'r';
        case '\u001a':
            return 'Z';
        case '\'':
            return '\'';
        case '\"':
            return '"';
        case '\b':
            return 'b';
        case '\\':
            return '\\';
        default:
            return NO_ESCAPE;
        }
    }

    /**
     * Formats the specified {@code title}, that is, replaces whitespaces with {@code _}.
     * 
     * @param title The String to format.
     *              
     * @return The resulting String with all whitespaces replaced.
     */
    public static String titleFormat(String title)
    {
        return title.replace(' ', '_');
    }
}
