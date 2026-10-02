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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.dump;

/**
 * The single method {@link SQLEscape#escape(String)} removes all unwanted escape characters from a
 * string to make is UNCOMPRESSED conform. Maybe not thread-save.
 * <p>
 * Copied from the WikiMachine to avoid having to add dependency.
 */
public class SQLEscape
{
    private SQLEscape()
    {

    }

    /**
     * @param str
     *            unescaped String
     * @return String with escape characters
     * @see SQLEscape
     */
    public static String escape(String str)
    {
        final int len = str.length();

        // maybe the StringBuffer would be safer?
        StringBuilder sql = new StringBuilder(len * 2);

        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            switch (c) {
            case '\u0000':
                sql.append('\\').append('0');
                break;
            case '\n':
                sql.append('\\').append('n');
                break;
            case '\t':
                sql.append('\\').append('t');
                break;
            case '\r':
                sql.append('\\').append('r');
                break;
            case '\u001a':
                sql.append('\\').append('Z');
                break;
            case '\'':
                sql.append('\\').append('\'');
                break;
            case '\"':
                sql.append('\\').append('"');
                break;
            case '\b':
                sql.append('\\').append('b');
                break;
            case '\\':
                sql.append('\\').append('\\');
                break;
            // case '%':
            // sql.append('[').append('%').append(']');
            // break;
            // case '_':
            // sql.append('[').append('_').append(']');
            // break;
            default:
                sql.append(c);
                break;
            }
        }
        return sql.toString();
    }

    /**
     * Reverses {@link #escape(String)}: every escape sequence that it creates is replaced by the
     * character it stands for, which is also how the server reads the escaped value when it is
     * embedded in a string literal. Values that are bound as parameters of a prepared statement
     * have to be unescaped this way, as they are stored exactly as given.
     *
     * @param str
     *            String with escape characters, may be {@code null}
     * @return unescaped String, or {@code null} if {@code str} is {@code null}
     */
    public static String unescape(String str)
    {
        if (str == null || str.indexOf('\\') < 0) {
            return str;
        }

        final int len = str.length();
        StringBuilder text = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c != '\\' || i + 1 == len) {
                text.append(c);
                continue;
            }

            char next = str.charAt(++i);
            switch (next) {
            case '0':
                text.append('\u0000');
                break;
            case 'n':
                text.append('\n');
                break;
            case 't':
                text.append('\t');
                break;
            case 'r':
                text.append('\r');
                break;
            case 'Z':
                text.append('\u001a');
                break;
            case 'b':
                text.append('\b');
                break;
            case '\'':
            case '"':
            case '\\':
                text.append(next);
                break;
            default:
                // not created by escape(): keep the sequence as it is
                text.append(c).append(next);
                break;
            }
        }
        return text.toString();
    }

}
