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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SingleKeywordTreeTest
{

    private static List<String> matches(SingleKeywordTree<String> tree, String input)
    {
        List<String> found = new ArrayList<>();
        tree.reset();
        for (char c : input.toCharArray()) {
            if (tree.check(c)) {
                found.add(tree.getValue());
                tree.reset();
            }
        }
        return found;
    }

    @Test
    public void testFindsKeywords()
    {
        SingleKeywordTree<String> tree = new SingleKeywordTree<>();
        tree.addKeyword("<page>", "page");
        tree.addKeyword("</page>", "/page");
        tree.addKeyword("<id>", "id");

        assertEquals(List.of("page", "id", "/page"), matches(tree, "x<page>ä東<id>1</id></page>"));
    }

    @Test
    public void testDoesNotRetestMismatchingChar()
    {
        SingleKeywordTree<String> tree = new SingleKeywordTree<>();
        tree.addKeyword("<page>", "page");

        // After a mismatch the tree restarts at the root with the next char
        assertEquals(List.of(), matches(tree, "<<page>"));
        assertEquals(List.of(), matches(tree, "<pa<page>"));
        assertEquals(List.of("page"), matches(tree, "<pa <page>"));
    }

    @Test
    public void testSupportsNonAsciiKeywords()
    {
        SingleKeywordTree<String> tree = new SingleKeywordTree<>();
        tree.addKeyword("<größe>", "größe");
        tree.addKeyword("東京", "tokyo");

        assertEquals(List.of("größe", "tokyo"), matches(tree, "a<größe>b東 東京"));
        assertEquals(List.of(), matches(tree, "<grösse>東東京"));
    }
}
