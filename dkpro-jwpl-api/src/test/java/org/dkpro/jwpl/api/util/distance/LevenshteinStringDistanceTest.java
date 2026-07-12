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
package org.dkpro.jwpl.api.util.distance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class LevenshteinStringDistanceTest
{

    private final LevenshteinStringDistance distance = new LevenshteinStringDistance();

    @Test
    public void testBothEmpty()
    {
        assertEquals(0, distance.distance("", ""));
    }

    @Test
    public void testFirstEmpty()
    {
        assertEquals(3, distance.distance("", "abc"));
    }

    @Test
    public void testSecondEmpty()
    {
        assertEquals(4, distance.distance("test", ""));
    }

    @Test
    public void testIdenticalStrings()
    {
        assertEquals(0, distance.distance("hello", "hello"));
        assertEquals(0, distance.distance("", ""));
        assertEquals(0, distance.distance("a", "a"));
    }

    @Test
    public void testSingleCharSubstitution()
    {
        assertEquals(1, distance.distance("a", "b"));
    }

    @Test
    public void testSingleInsertion()
    {
        assertEquals(1, distance.distance("abc", "abcd"));
    }

    @Test
    public void testSingleDeletion()
    {
        assertEquals(1, distance.distance("abcd", "abc"));
    }

    @Test
    public void testSubstitution()
    {
        assertEquals(1, distance.distance("abc", "abd"));
    }

    @Test
    public void testStandardExample()
    {
        assertEquals(3, distance.distance("kitten", "sitting"));
    }

    @Test
    public void testCompletelyDifferentStrings()
    {
        assertEquals(5, distance.distance("abcde", "fghij"));
    }

    @Test
    public void testCommonPrefix()
    {
        assertEquals(2, distance.distance("hello", "help"));
    }

    @Test
    public void testCommonSuffix()
    {
        assertEquals(1, distance.distance("hellp", "hello"));
    }

    @Test
    public void testMiddleSubstitution()
    {
        assertEquals(1, distance.distance("hello", "hellp"));
    }

    @Test
    public void testMultipleInsertions()
    {
        assertEquals(4, distance.distance("ab", "abcdef"));
    }

    @Test
    public void testMultipleDeletions()
    {
        assertEquals(4, distance.distance("abcdef", "ab"));
    }

    @Test
    public void testMinBranchA()
    {
        assertEquals(2, distance.distance("a", "bc"));
    }

    @Test
    public void testMinBranchB()
    {
        assertEquals(2, distance.distance("bc", "a"));
    }

    @Test
    public void testMinBranchC()
    {
        assertEquals(1, distance.distance("ab", "ac"));
    }

    @Test
    public void testLongerStrings()
    {
        assertEquals(0, distance.distance("abcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz"));
        assertEquals(26, distance.distance("", "abcdefghijklmnopqrstuvwxyz"));
    }

    @Test
    public void testIdenticalLongStrings()
    {
        assertEquals(0, distance.distance("abc", "abc"));
    }

    @Test
    public void testMinBoundaryFirstEqualsSecond()
    {
        assertEquals(2, distance.distance("ab", "cd"));
    }

    @Test
    public void testMinBoundaryAllThreeEqual()
    {
        assertEquals(3, distance.distance("abc", "def"));
    }

    @Test
    public void testMinBoundaryTwoEqual()
    {
        assertEquals(2, distance.distance("ab", "xy"));
    }

    @Test
    public void testMinBoundarySingleDifferentChar()
    {
        assertEquals(1, distance.distance("x", "y"));
    }

    @Test
    public void testMinBoundaryDiagonalEquality()
    {
        assertEquals(1, distance.distance("ab", "ax"));
    }

    @Test
    public void testMinBoundarySecondEqualsMin()
    {
        assertEquals(2, distance.distance("ac", "bd"));
    }

    @Test
    public void testMinBoundaryThirdEqualsMin()
    {
        assertEquals(3, distance.distance("abc", "xyz"));
    }

    @Test
    public void testMinBoundaryAllEqualPath()
    {
        assertEquals(4, distance.distance("abcd", "wxyz"));
    }
}
