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

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

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

    @Test
    void titleFormatReplacesBlanksWithUnderscores()
    {
        assertEquals("Main_Page", SQLEscape.titleFormat("Main Page"));
    }
}
