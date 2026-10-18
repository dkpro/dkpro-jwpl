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
package org.dkpro.jwpl.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link CategoryGraphManager} caches the full graph and the graphs over subsets of the
 * category pages separately (see issue #598).
 */
class CategoryGraphManagerTest
    extends BaseJWPLTest
{

    @BeforeAll
    static void setupWikipedia() throws WikiApiException
    {
        wiki = new Wikipedia(obtainDbConfiguration());
    }

    @Test
    void returnsTheGraphOfASubsetAfterTheFullGraphWasCached() throws WikiApiException
    {
        CategoryGraph fullGraph = CategoryGraphManager.getCategoryGraph(wiki, false);
        List<Integer> nodes = new ArrayList<>(fullGraph.getGraph().vertexSet());
        Set<Integer> subset = new HashSet<>(nodes.subList(0, 3));

        CategoryGraph subGraph = CategoryGraphManager.getCategoryGraph(wiki, subset, false);

        assertEquals(3, subGraph.getNumberOfNodes());
        assertEquals(subset, subGraph.getGraph().vertexSet());
        assertSame(fullGraph, CategoryGraphManager.getCategoryGraph(wiki, false));
        assertSame(subGraph, CategoryGraphManager.getCategoryGraph(wiki, subset, false));
    }

    @Test
    void returnsDifferentGraphsForDifferentSubsetsOfTheSameSize() throws WikiApiException
    {
        CategoryGraph fullGraph = CategoryGraphManager.getCategoryGraph(wiki, false);
        List<Integer> nodes = new ArrayList<>(fullGraph.getGraph().vertexSet());
        assertTrue(nodes.size() >= 4);
        Set<Integer> first = new HashSet<>(nodes.subList(0, 2));
        Set<Integer> second = new HashSet<>(nodes.subList(2, 4));

        CategoryGraph firstGraph = CategoryGraphManager.getCategoryGraph(wiki, first, false);
        CategoryGraph secondGraph = CategoryGraphManager.getCategoryGraph(wiki, second, false);

        assertEquals(first, firstGraph.getGraph().vertexSet());
        assertEquals(second, secondGraph.getGraph().vertexSet());
    }

    @Test
    void derivesTheKeyFromTheSortedPageIds()
    {
        assertEquals("wiki", CategoryGraphManager.getCategoryGraphKey("wiki", null));
        assertEquals(CategoryGraphManager.getCategoryGraphKey("wiki", Set.of(1, 2, 3)),
                CategoryGraphManager.getCategoryGraphKey("wiki", new HashSet<>(List.of(3, 1, 2))));
        assertNotEquals(CategoryGraphManager.getCategoryGraphKey("wiki", Set.of(1, 2)),
                CategoryGraphManager.getCategoryGraphKey("wiki", Set.of(1, 3)));
        assertNotEquals(CategoryGraphManager.getCategoryGraphKey("wiki", null),
                CategoryGraphManager.getCategoryGraphKey("wiki", Set.of()));
    }
}
