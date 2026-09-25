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
 * The kind of membership a {@code categorylinks} row describes, as recorded in the
 * {@code cl_type} column MediaWiki introduced with the {@code categorylinks} table revision
 * of MediaWiki 1.17.
 * <p>
 * Dumps that predate that column - and dumps without a parseable {@code CREATE TABLE} header -
 * yield {@link #UNKNOWN}, in which case consumers have to fall back to inferring the membership
 * kind from the namespace of the member page.
 */
public enum CategoryLinkType
{

    /** The member of the category is an ordinary page. */
    PAGE,
    /** The member of the category is another category. */
    SUBCAT,
    /** The member of the category is a file. */
    FILE,
    /** The dump does not carry a {@code cl_type} column. */
    UNKNOWN;

    /**
     * @param value The raw value of the {@code cl_type} column, may be {@code null}.
     * @return The matching {@link CategoryLinkType}, or {@link #UNKNOWN} if {@code value} is
     *         {@code null} or not one of the values MediaWiki defines.
     */
    public static CategoryLinkType fromDumpValue(String value)
    {
        if (value == null) {
            return UNKNOWN;
        }
        if (equalsAsciiIgnoreCase(value, "page")) {
            return PAGE;
        }
        if (equalsAsciiIgnoreCase(value, "subcat")) {
            return SUBCAT;
        }
        if (equalsAsciiIgnoreCase(value, "file")) {
            return FILE;
        }
        return UNKNOWN;
    }

    /**
     * Compares {@code value} to a lower case ASCII {@code expected} without allocating, folding
     * only the ASCII letters {@code A-Z} of {@code value}.
     * <p>
     * This yields exactly the result of {@code value.toLowerCase(Locale.ROOT).equals(expected)}
     * for the values MediaWiki defines: the only non-ASCII characters that {@code toLowerCase}
     * maps into ASCII are the Kelvin sign (to {@code k}) and the dotted capital I (to {@code i}
     * followed by a combining dot, which changes the length), and neither can produce
     * {@code page}, {@code subcat} or {@code file}. {@link String#equalsIgnoreCase(String)} would
     * in contrast also accept, for example, the long s {@code U+017F} for {@code s}.
     *
     * @param value    The value to test.
     * @param expected The expected value, lower case ASCII only.
     * @return {@code true} if {@code value} equals {@code expected} ignoring ASCII case.
     */
    private static boolean equalsAsciiIgnoreCase(String value, String expected)
    {
        final int length = expected.length();
        if (value.length() != length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c = (char) (c + ('a' - 'A'));
            }
            if (c != expected.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}
