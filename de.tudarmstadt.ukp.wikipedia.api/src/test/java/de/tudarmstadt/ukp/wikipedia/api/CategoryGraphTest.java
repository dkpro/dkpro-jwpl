/*******************************************************************************
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNoException;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

public class CategoryGraphTest extends BaseJWPLTest{

	private CategoryGraph catGraph;

	@Before
	public void setupWikipedia() {
		DatabaseConfiguration db = obtainHSDLDBConfiguration();

		try {
			wiki = new Wikipedia(db);
		} catch (Exception e) {
			fail("Wikipedia could not be initialized.");
		}

        try {
            catGraph = CategoryGraphManager.getCategoryGraph(wiki, false);
        } catch (WikiApiException e) {
            fail("CategoryGraph could not be initialized.");
        }
    }

	@Test
	public void testDepth(){
        try {
            double depth = catGraph.getDepth();
            assertEquals(4, depth, 0.00001);
        } catch (WikiApiException e) {
            fail("Getting depth of the CategoryGraph throws exception.");
        }
    }

    @Test
    public void testGetPathLength() throws WikiApiException{
        String catString = "UKP";
        String neighborCatString = "Projects of UKP";
        String twoStepsAwayCatString = "SIR";

        Category cat = wiki.getCategory(catString);
        Category neighborCat = wiki.getCategory(neighborCatString);
        Category twoStepsAwayCat = wiki.getCategory(twoStepsAwayCatString);

        int equalNodes = catGraph.getPathLengthInNodes(cat, cat);
        int oneStepNodes = catGraph.getPathLengthInNodes(cat, neighborCat);
        int twoStepNodes = catGraph.getPathLengthInNodes(cat, twoStepsAwayCat);

        int equalEdges = catGraph.getPathLengthInEdges(cat, cat);
        int oneStepEdges = catGraph.getPathLengthInEdges(cat, neighborCat);
        int twoStepEdges = catGraph.getPathLengthInEdges(cat, twoStepsAwayCat);

        assertEquals(0, equalNodes);
        assertEquals(0, oneStepNodes);
        assertEquals(1, twoStepNodes);

        assertEquals(0, equalEdges);
        assertEquals(1, oneStepEdges);
        assertEquals(2, twoStepEdges);
    }

    @Test
    // each value within the map must be higher than the number of nodes in the category graph
    public void testHyponymCountMap() throws WikiApiException{
        Map<Integer,Integer> hyponymCountMap = catGraph.getHyponymCountMap();
        int numberOfNodes = catGraph.getNumberOfNodes();
        for (Integer key : hyponymCountMap.keySet()) {
            assertTrue(hyponymCountMap.get(key) < numberOfNodes);
        }

        for (Integer key : catGraph.getGraph().vertexSet()) {
            assertTrue(hyponymCountMap.containsKey(key));
        }

        assertEquals(16, hyponymCountMap.get(1).intValue());
        assertEquals(0,  hyponymCountMap.get(2).intValue());
        assertEquals(10, hyponymCountMap.get(3).intValue());
        assertEquals(1,  hyponymCountMap.get(4).intValue());
        assertEquals(5,  hyponymCountMap.get(5).intValue());
        assertEquals(9,  hyponymCountMap.get(6).intValue());
        assertEquals(2,  hyponymCountMap.get(7).intValue());
        assertEquals(4,  hyponymCountMap.get(8).intValue());
        assertEquals(0,  hyponymCountMap.get(9).intValue());
        assertEquals(0,  hyponymCountMap.get(10).intValue());
        assertEquals(0,  hyponymCountMap.get(11).intValue());
        assertEquals(0,  hyponymCountMap.get(12).intValue());
        assertEquals(0,  hyponymCountMap.get(13).intValue());
        assertEquals(0,  hyponymCountMap.get(14).intValue());
        assertEquals(0,  hyponymCountMap.get(15).intValue());
        assertEquals(0,  hyponymCountMap.get(200).intValue());
    }
}
