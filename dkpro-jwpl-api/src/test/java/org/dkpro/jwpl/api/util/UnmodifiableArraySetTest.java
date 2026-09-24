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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class UnmodifiableArraySetTest
{
    private static Set<Integer> empty()
    {
        return new UnmodifiableArraySet<>(new Integer[0]);
    }

    private static Set<Integer> oneTwoThree()
    {
        return new UnmodifiableArraySet<>(new Integer[] { 1, 2, 3 });
    }

    @Test
    void testEmptySet()
    {
        Set<Integer> set = empty();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        assertFalse(set.contains(1));
        assertFalse(set.iterator().hasNext());
        assertEquals(0, set.toArray().length);
    }

    @Test
    void testEmptySetFromSet()
    {
        Set<Integer> set = new UnmodifiableArraySet<>(new HashSet<>());
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test
    void testNonEmptySet()
    {
        Set<Integer> set = oneTwoThree();
        assertFalse(set.isEmpty());
        assertEquals(3, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
        assertFalse(set.contains(4));
        assertFalse(set.contains(null));
        assertFalse(set.contains("1"));
    }

    @Test
    void testNonEmptySetFromSet()
    {
        Set<Integer> source = new HashSet<>(List.of(1, 2, 3));
        Set<Integer> set = new UnmodifiableArraySet<>(source);
        assertFalse(set.isEmpty());
        assertEquals(3, set.size());
        assertTrue(set.containsAll(source));
    }

    @Test
    void testContainsAll()
    {
        Set<Integer> set = oneTwoThree();
        assertTrue(set.containsAll(Collections.emptySet()));
        assertTrue(set.containsAll(List.of(1, 3)));
        assertFalse(set.containsAll(List.of(1, 4)));
        assertTrue(empty().containsAll(Collections.emptySet()));
        assertFalse(empty().containsAll(List.of(1)));
    }

    @Test
    void testIteration()
    {
        Set<Integer> seen = new HashSet<>();
        for (Integer i : oneTwoThree()) {
            seen.add(i);
        }
        assertEquals(Set.of(1, 2, 3), seen);
    }

    @Test
    void testEqualsAndHashCodeAgainstHashSet()
    {
        Set<Integer> expected = new HashSet<>(List.of(1, 2, 3));
        Set<Integer> set = oneTwoThree();
        assertEquals(expected, set);
        assertEquals(set, expected);
        assertEquals(expected.hashCode(), set.hashCode());
        assertEquals(set, new UnmodifiableArraySet<>(new Integer[] { 3, 2, 1 }));
        assertEquals(set, set);

        assertNotEquals(set, new HashSet<>(List.of(1, 2)));
        assertNotEquals(set, new HashSet<>(List.of(1, 2, 4)));
        assertNotEquals(set, List.of(1, 2, 3));
        assertNotEquals(null, set);

        assertEquals(Collections.emptySet(), empty());
        assertEquals(empty(), Collections.emptySet());
        assertEquals(Collections.emptySet().hashCode(), empty().hashCode());
    }

    @Test
    void testToArray()
    {
        Set<Integer> set = oneTwoThree();
        Object[] array = set.toArray();
        assertArrayEquals(new Object[] { 1, 2, 3 }, array);

        // Modifying the returned array must not modify the set
        array[0] = 42;
        assertTrue(set.contains(1));
        assertFalse(set.contains(42));
        assertNotSame(array, set.toArray());
    }

    @Test
    void testToArrayTyped()
    {
        Set<Integer> set = oneTwoThree();

        Integer[] exact = new Integer[3];
        assertSame(exact, set.toArray(exact));
        assertArrayEquals(new Integer[] { 1, 2, 3 }, exact);

        Integer[] tooSmall = new Integer[0];
        Integer[] allocated = set.toArray(tooSmall);
        assertNotSame(tooSmall, allocated);
        assertArrayEquals(new Integer[] { 1, 2, 3 }, allocated);

        Integer[] tooLarge = new Integer[] { 9, 9, 9, 9, 9 };
        assertSame(tooLarge, set.toArray(tooLarge));
        assertArrayEquals(new Integer[] { 1, 2, 3 }, Arrays.copyOf(tooLarge, 3));
        assertNull(tooLarge[3]);
    }

    @Test
    void testDefensiveCopyOfConstructorArray()
    {
        Integer[] source = new Integer[] { 1, 2, 3 };
        Set<Integer> set = new UnmodifiableArraySet<>(source);
        source[0] = 42;
        assertTrue(set.contains(1));
        assertFalse(set.contains(42));
    }

    @Test
    void testImmutability()
    {
        Set<Integer> set = oneTwoThree();
        assertThrows(UnsupportedOperationException.class, () -> set.add(4));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(1));
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(List.of(4)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeAll(List.of(1)));
        assertThrows(UnsupportedOperationException.class, () -> set.retainAll(List.of(1)));
        assertThrows(UnsupportedOperationException.class, set::clear);

        Iterator<Integer> it = set.iterator();
        it.next();
        assertThrows(UnsupportedOperationException.class, it::remove);

        assertEquals(3, set.size());
    }
}
