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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Pins the escape round trips the SQL parsers rely on: a value is decoded by
 * {@link StreamTokenizer} while reading the dump and re-encoded by {@link SQLEscape} before it is
 * handed on, so both steps together have to be the identity for everything MediaWiki emits.
 */
class SQLEscapeTest
{

    /** Decodes a single quoted SQL literal exactly the way the parsers do. */
    private static String roundTrip(String sqlLiteral) throws IOException
    {
        final StreamTokenizer st = new StreamTokenizer(new StringReader(sqlLiteral));
        st.wordChars('_', '_');
        st.nextToken();
        return SQLEscape.escape(st.sval);
    }

    @Test
    void newlineRoundTrips() throws Exception
    {
        assertEquals("SJ\\nSJ", roundTrip("'SJ\\nSJ'"));
    }

    @Test
    void backslashRoundTrips() throws Exception
    {
        assertEquals("a\\\\b", roundTrip("'a\\\\b'"));
    }

    @Test
    void apostropheRoundTrips() throws Exception
    {
        assertEquals("D\\'Arcy", roundTrip("'D\\'Arcy'"));
    }

    @Test
    void nulRoundTrips() throws Exception
    {
        assertEquals("a\\0b", roundTrip("'a\\0b'"));
    }

    @Test
    void tabAndCarriageReturnRoundTrip() throws Exception
    {
        assertEquals("a\\tb", roundTrip("'a\\tb'"));
        assertEquals("a\\rb", roundTrip("'a\\rb'"));
    }

    @Test
    void nullAndBlankCollapseToTheEmptyString()
    {
        assertEquals("", SQLEscape.escape(null));
        assertEquals("", SQLEscape.escape(""));
        assertEquals("", SQLEscape.escape("   "));
    }

    @Test
    void substituteEscapeIsKnownToBeLossy() throws Exception
    {
        // StreamTokenizer has no '\Z' escape, so 0x1A is never recovered from a dump. Pinned
        // here so that a change to this behaviour is a deliberate one. Encoding the character
        // itself still works.
        assertEquals("Z", roundTrip("'\\Z'"));
        assertEquals("a\\Zb", SQLEscape.escape("a" + (char) 0x1A + "b"));
    }

    /** Every character the escape table handles, including the backslash. */
    private static final String ESCAPABLE = "\u0000\n\t\r\u001a'\"\b\\";

    /**
     * The implementation of {@link SQLEscape#escape} before it gained the no-escape fast path and
     * the smaller buffer, kept as the oracle that the current output must match char for char.
     */
    private static String referenceEscape(String str)
    {
        if (str == null || str.isBlank()) {
            return "";
        }
        final int len = str.length();
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
            default:
                sql.append(c);
                break;
            }
        }
        return sql.toString();
    }

    @Test
    void everyEscapableCharacterMatchesTheReference()
    {
        for (int i = 0; i < ESCAPABLE.length(); i++) {
            final char c = ESCAPABLE.charAt(i);
            for (String input : List.of("a" + c, c + "a", "a" + c + "b", c + "" + c + "x")) {
                assertEquals(referenceEscape(input), SQLEscape.escape(input),
                        "escape of U+" + Integer.toHexString(c));
            }
        }
        assertEquals(referenceEscape("x" + ESCAPABLE + "y"), SQLEscape.escape("x" + ESCAPABLE + "y"));
    }

    @Test
    void inputWithoutEscapableCharactersIsReturnedAsIs()
    {
        final String title = "Albert_Einstein_(\u00e9t\u00e9)_%_\uffff";
        assertEquals(referenceEscape(title), SQLEscape.escape(title));
        assertSame(title, SQLEscape.escape(title));
    }

    @Test
    void leadingAndTrailingEscapesMatchTheReference()
    {
        for (String input : List.of("'leading", "trailing'", "\\", "\n\n\n", " a\n", "a\n ")) {
            assertEquals(referenceEscape(input), SQLEscape.escape(input));
        }
    }

    @Test
    void randomMixedTextMatchesTheReference()
    {
        final String alphabet = ESCAPABLE + " abcXYZ019[]{}|=_%\u00e4\u4e2d\uffff";
        final Random random = new Random(614);
        for (int n = 0; n < 2000; n++) {
            final int len = random.nextInt(n % 10 == 0 ? 20000 : 200);
            final StringBuilder text = new StringBuilder(len);
            // Vary the escape density from none at all to escape-only text.
            final int escapePercent = n % 5 == 0 ? 0 : random.nextInt(101);
            for (int i = 0; i < len; i++) {
                if (random.nextInt(100) < escapePercent) {
                    text.append(ESCAPABLE.charAt(random.nextInt(ESCAPABLE.length())));
                }
                else {
                    text.append(alphabet.charAt(ESCAPABLE.length()
                            + random.nextInt(alphabet.length() - ESCAPABLE.length())));
                }
            }
            final String input = text.toString();
            assertEquals(referenceEscape(input), SQLEscape.escape(input));
        }
    }

    @Test
    void titleFormatReplacesBlanksWithUnderscores()
    {
        assertEquals("Main_Page", SQLEscape.titleFormat("Main Page"));
    }
}
