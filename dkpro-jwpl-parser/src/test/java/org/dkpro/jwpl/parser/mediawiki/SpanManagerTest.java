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
package org.dkpro.jwpl.parser.mediawiki;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.dkpro.jwpl.parser.Span;
import org.junit.jupiter.api.Test;

/**
 * Tests the range-bounded {@link SpanManager#indexOf(String, int, int)}, which must return the same
 * result as an unbounded search followed by a check of the start position against the upper bound
 * (see issue #542).
 */
class SpanManagerTest
{

    /**
     * The former implementation of {@link SpanManager#indexOf(String, int, int)}.
     */
    private static int referenceIndexOf(String src, String str, int fromIndex, int toIndex)
    {
        int result = new StringBuilder(src).indexOf(str, fromIndex);
        if (result >= toIndex) {
            return -1;
        }
        return result;
    }

    @Test
    void findsMatchInsideTheRange()
    {
        SpanManager sm = new SpanManager("foo '''bar''' baz");
        assertEquals(4, sm.indexOf("'''", 0, 17));
        assertEquals(10, sm.indexOf("'''", 5, 17));
        assertEquals(4, sm.indexOf("'''", new Span(0, 17)));
    }

    @Test
    void ignoresMatchStartingAtOrAfterTheUpperBound()
    {
        SpanManager sm = new SpanManager("abc\ndef http://x");
        assertEquals(-1, sm.indexOf("http://", 0, 3));
        assertEquals(-1, sm.indexOf("http://", 0, 8));
        assertEquals(8, sm.indexOf("http://", 0, 9));
    }

    @Test
    void acceptsMatchThatStartsBeforeButEndsAfterTheUpperBound()
    {
        SpanManager sm = new SpanManager("ab'''cd");
        assertEquals(2, sm.indexOf("'''", 2, 3));
        assertEquals(2, sm.indexOf("'''", 0, 4));
    }

    @Test
    void treatsNegativeFromIndexAsZero()
    {
        SpanManager sm = new SpanManager("xyz");
        assertEquals(0, sm.indexOf("x", -5, 3));
        assertEquals(1, sm.indexOf("yz", -1, 2));
        assertEquals(-1, sm.indexOf("x", -5, 0));
    }

    @Test
    void capsUpperBoundAtTheLength()
    {
        SpanManager sm = new SpanManager("abcab");
        assertEquals(3, sm.indexOf("ab", 1, 100));
        assertEquals(-1, sm.indexOf("abc", 1, Integer.MAX_VALUE));
        assertEquals(-1, sm.indexOf("b", 10, 100));
    }

    @Test
    void handlesEmptySearchString()
    {
        SpanManager sm = new SpanManager("abc");
        assertEquals(0, sm.indexOf("", -2, 1));
        assertEquals(2, sm.indexOf("", 2, 3));
        assertEquals(-1, sm.indexOf("", 2, 2));
        assertEquals(3, sm.indexOf("", 7, 4));
        assertEquals(-1, sm.indexOf("", 7, 3));
    }

    @Test
    void handlesEmptyOrInvertedRange()
    {
        SpanManager sm = new SpanManager("aaaa");
        assertEquals(-1, sm.indexOf("a", 2, 2));
        assertEquals(-1, sm.indexOf("a", 3, 1));
        assertEquals(-1, sm.indexOf("a", 0, -1));
        assertEquals(-1, sm.indexOf("a", 0, Integer.MIN_VALUE));
    }

    @Test
    void matchesFormerImplementationOnRandomInput()
    {
        Random random = new Random(542);
        String alphabet = "ab'|]";
        for (int run = 0; run < 20000; run++) {
            String src = randomString(random, alphabet, random.nextInt(20));
            String str = randomString(random, alphabet, random.nextInt(4));
            int fromIndex = random.nextInt(src.length() + 6) - 3;
            int toIndex = random.nextInt(src.length() + 6) - 3;
            SpanManager sm = new SpanManager(src);
            assertEquals(referenceIndexOf(src, str, fromIndex, toIndex),
                    sm.indexOf(str, fromIndex, toIndex), "src=\"" + src + "\", str=\"" + str
                            + "\", from=" + fromIndex + ", to=" + toIndex);
        }
    }

    private static String randomString(Random random, String alphabet, int length)
    {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
