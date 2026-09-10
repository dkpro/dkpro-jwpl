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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests the detection, replacement and truncation of surrogate characters (see issue #282).
 */
class SurrogatesTest
{

    // U+1F600 (grinning face) is encoded as the surrogate pair D83D DE00.
    private static final String PAIR = "\uD83D\uDE00";
    private static final String LONE_HIGH = "\uD83D";
    private static final String LONE_LOW = "\uDE00";

    // Characters outside the Latin range, but inside the Basic Multilingual Plane.
    private static final String PLAIN = "K\u00E4se \u65E5\u672C \uD7FF\uE000";

    @Test
    void scanFindsNoSurrogateInTextWithoutOne()
    {
        assertFalse(Surrogates.scan(PLAIN.toCharArray()));
        assertFalse(Surrogates.scan(new char[0]));
    }

    @Test
    void scanFindsSurrogatePairsAndLoneSurrogates()
    {
        assertTrue(Surrogates.scan(("a" + PAIR + "b").toCharArray()));
        assertTrue(Surrogates.scan(("a" + LONE_HIGH + "b").toCharArray()));
        assertTrue(Surrogates.scan(("a" + LONE_LOW + "b").toCharArray()));
    }

    @Test
    void replaceTurnsEachSurrogateIntoAQuestionMark()
    {
        assertEquals("a??b", new String(Surrogates.replace(("a" + PAIR + "b").toCharArray())));
        assertEquals("a?b", new String(Surrogates.replace(("a" + LONE_HIGH + "b").toCharArray())));
        assertEquals("a?b", new String(Surrogates.replace(("a" + LONE_LOW + "b").toCharArray())));
    }

    @Test
    void replaceKeepsTextWithoutSurrogates()
    {
        assertEquals(PLAIN, new String(Surrogates.replace(PLAIN.toCharArray())));
    }

    @Test
    void discardRestKeepsTheTextBeforeTheFirstSurrogate()
    {
        assertEquals("ab",
                new String(Surrogates.discardRest(("ab" + PAIR + "cd" + PAIR).toCharArray())));
        assertEquals("ab",
                new String(Surrogates.discardRest(("ab" + LONE_LOW + "cd").toCharArray())));
        assertEquals("", new String(Surrogates.discardRest((LONE_HIGH + "cd").toCharArray())));
    }

    @Test
    void discardRestKeepsTextWithoutSurrogates()
    {
        assertEquals(PLAIN, new String(Surrogates.discardRest(PLAIN.toCharArray())));
    }
}
