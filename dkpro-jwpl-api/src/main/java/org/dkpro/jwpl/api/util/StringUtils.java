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

import java.util.Collection;
import java.util.Iterator;

/**
 * Provides methods to manipulate strings for special use cases.
 */
public class StringUtils
{

    private static final StringBuilder buffer = new StringBuilder(10_000_000);

    /**
     * Joins the elements of a collection into a string.
     *
     * @param c
     *            The collection which elements should be joined.
     * @param delimiter
     *            String that is introduced between two joined elements.
     * @return The joined string.
     */
    public static String join(Collection<?> c, String delimiter)
    {
        buffer.setLength(0);
        Iterator<?> iter = c.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

    /**
     * Replaces all problematic characters from a String with their escaped versions to make it SQL
     * conform.
     *
     * @param str
     *            unescaped String
     * @return SQL safe escaped String
     */
    public static String sqlEscape(String str)
    {
        final int len = str.length();
        buffer.setLength(0);
        StringBuilder sql = buffer;

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

}
