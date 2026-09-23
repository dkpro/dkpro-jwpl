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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Checks that the source positions tracked by {@link SpanManager} match those of the former
 * per-char {@code ArrayList<Integer>} implementation (see issue #587).
 */
class SpanManagerSrcPosTest
{

    /**
     * Copy of the former source-position logic of {@link SpanManager}, which edits a boxed list
     * one char at a time.
     */
    private static final class ReferenceSrcPos
    {
        private final List<Integer> ib;

        ReferenceSrcPos(int len)
        {
            ib = new ArrayList<>(len);
            for (int i = 0; i < len; i++)
                ib.add(i);
        }

        void delete(int start, int end)
        {
            for (int i = 0; i < end - start; i++)
                ib.remove(start);
        }

        void insert(int offset, String str)
        {
            for (int i = 0; i < str.length(); i++)
                ib.add(offset, -1);
        }

        void replace(int start, int end, String str)
        {
            for (int i = 0; i < end - start; i++)
                ib.remove(start);
            for (int i = 0; i < str.length(); i++)
                ib.add(start, -1);
        }

        void setCharAt(int index)
        {
            ib.set(index, -1);
        }

        int size()
        {
            return ib.size();
        }

        int get(int index)
        {
            return ib.get(index);
        }
    }

    private static void assertSamePositions(ReferenceSrcPos expected, SpanManager actual)
    {
        assertEquals(expected.size(), actual.length());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.getSrcPos(i), "src pos at index " + i);
        }
    }

    @Test
    void fixedEditSequenceMatchesReference()
    {
        String src = "'''Bold''' [[Link|text]] {{Template|a=b}} <!-- c --> <nowiki>x</nowiki>";
        SpanManager sm = new SpanManager(src);
        sm.enableSrcPosCalculation();
        ReferenceSrcPos ref = new ReferenceSrcPos(src.length());

        sm.delete(0, 3);
        ref.delete(0, 3);
        sm.delete(4, 7);
        ref.delete(4, 7);
        sm.replace(26, 42, "TEMPLATE_0");
        ref.replace(26, 42, "TEMPLATE_0");
        sm.insert(0, "prefix ");
        ref.insert(0, "prefix ");
        sm.insert(sm.length(), " suffix");
        ref.insert(ref.size(), " suffix");
        sm.replace(3, 3, "ins");
        ref.replace(3, 3, "ins");
        sm.replace(10, 12, "");
        ref.replace(10, 12, "");
        sm.setCharAt(5, '_');
        ref.setCharAt(5);
        sm.delete(2, 2);
        ref.delete(2, 2);

        assertSamePositions(ref, sm);
    }

    @Test
    void randomEditSequencesMatchReference()
    {
        Random random = new Random(587);
        for (int run = 0; run < 200; run++) {
            StringBuilder src = new StringBuilder();
            int srcLen = random.nextInt(200);
            for (int i = 0; i < srcLen; i++)
                src.append((char) ('a' + random.nextInt(26)));

            SpanManager sm = new SpanManager(src.toString());
            sm.enableSrcPosCalculation();
            ReferenceSrcPos ref = new ReferenceSrcPos(srcLen);

            for (int op = 0; op < 100; op++) {
                int len = sm.length();
                int a = random.nextInt(len + 1);
                int b = a + random.nextInt(len - a + 1);
                String str = "x".repeat(random.nextInt(20));
                switch (random.nextInt(4)) {
                case 0:
                    sm.delete(a, b);
                    ref.delete(a, b);
                    break;
                case 1:
                    sm.insert(a, str);
                    ref.insert(a, str);
                    break;
                case 2:
                    sm.replace(a, b, str);
                    ref.replace(a, b, str);
                    break;
                default:
                    if (len > 0) {
                        int index = random.nextInt(len);
                        sm.setCharAt(index, 'y');
                        ref.setCharAt(index);
                    }
                    break;
                }
                assertSamePositions(ref, sm);
            }
        }
    }

    @Test
    void getSrcPosOutOfRangeThrows()
    {
        SpanManager sm = new SpanManager("abcdef");
        sm.enableSrcPosCalculation();
        sm.delete(2, 4);

        assertThrows(IndexOutOfBoundsException.class, () -> sm.getSrcPos(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> sm.getSrcPos(4));
    }
}
