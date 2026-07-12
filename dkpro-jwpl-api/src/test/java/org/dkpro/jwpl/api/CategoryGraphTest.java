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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CategoryGraphTest
    extends BaseJWPLTest
{

    private static CategoryGraph catGraph;

    /**
     * Made this static so that following tests don't run if assumption fails. (With AT_Before,
     * tests also would not be executed but marked as passed) This could be changed back as soon as
     * JUnit ignored tests after failed assumptions
     */
    @BeforeAll
    public static void setupWikipedia()
    {
        DatabaseConfiguration db = obtainDbConfiguration();

        try {
            wiki = new Wikipedia(db);
        }
        catch (Exception e) {
            fail("Wikipedia could not be initialized: " + e.getLocalizedMessage());
        }

        try {
            catGraph = CategoryGraphManager.getCategoryGraph(wiki, false);
        }
        catch (WikiApiException e) {
            fail("CategoryGraph could not be initialized: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testDepth()
    {
        try {
            double depth = catGraph.getDepth();
            assertEquals(4, depth, 0.00001);
        }
        catch (WikiApiException e) {
            fail("Getting depth of the CategoryGraph throws exception.");
        }
    }

    @Test
    public void testGetPathLength() throws WikiApiException
    {
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
    public void testHyponymCountMap() throws WikiApiException
    {
        Map<Integer, Integer> hyponymCountMap = catGraph.getHyponymCountMap();
        int numberOfNodes = catGraph.getNumberOfNodes();
        for (Integer key : hyponymCountMap.keySet()) {
            assertTrue(hyponymCountMap.get(key) < numberOfNodes);
        }

        for (Integer key : catGraph.getGraph().vertexSet()) {
            assertTrue(hyponymCountMap.containsKey(key));
        }

        assertEquals(16, hyponymCountMap.get(1).intValue());
        assertEquals(0, hyponymCountMap.get(2).intValue());
        assertEquals(10, hyponymCountMap.get(3).intValue());
        assertEquals(1, hyponymCountMap.get(4).intValue());
        assertEquals(5, hyponymCountMap.get(5).intValue());
        assertEquals(9, hyponymCountMap.get(6).intValue());
        assertEquals(2, hyponymCountMap.get(7).intValue());
        assertEquals(4, hyponymCountMap.get(8).intValue());
        assertEquals(0, hyponymCountMap.get(9).intValue());
        assertEquals(0, hyponymCountMap.get(10).intValue());
        assertEquals(0, hyponymCountMap.get(11).intValue());
        assertEquals(0, hyponymCountMap.get(12).intValue());
        assertEquals(0, hyponymCountMap.get(13).intValue());
        assertEquals(0, hyponymCountMap.get(14).intValue());
        assertEquals(0, hyponymCountMap.get(15).intValue());
        assertEquals(0, hyponymCountMap.get(200).intValue());
    }

    @Test
    public void testGraphMetrics() throws WikiApiException
    {
        // Asserts the graph-wide metrics exposed by the public API: average shortest path length,
        // diameter, average degree, and the degree distribution, for the shared fixture graph.
        double avgSP = catGraph.getAverageShortestPathLength();
        double diameter = catGraph.getDiameter();
        double avgDegree = catGraph.getAverageDegree();
        Map<Integer, Integer> degreeDist = catGraph.getDegreeDistribution();

        assertEquals(17, catGraph.getNumberOfNodes());
        assertEquals(17, catGraph.getNumberOfEdges());
        assertEquals(5.0, diameter, 0.00001);
        assertEquals(2.3823529411764706, avgSP, 0.00001);
        assertEquals(2.0, avgDegree, 0.00001);
        assertEquals(17, degreeDist.values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(7, degreeDist.size());
    }

    @Test
    public void testGraphParametersWithFreshGraph() throws WikiApiException
    {
        CategoryGraph freshGraph = new CategoryGraph(wiki);
        double freshAvgSP = freshGraph.getAverageShortestPathLength();
        double freshDiameter = freshGraph.getDiameter();
        double freshAvgDegree = freshGraph.getAverageDegree();
        Map<Integer, Integer> freshDegreeDist = freshGraph.getDegreeDistribution();

        assertEquals(5.0, freshDiameter, 0.00001);
        assertEquals(2.3823529411764706, freshAvgSP, 0.00001);
        assertEquals(2.0, freshAvgDegree, 0.00001);
        assertEquals(17, freshGraph.getNumberOfNodes());
        assertEquals(17, freshGraph.getNumberOfEdges());
        assertEquals(17, freshDegreeDist.values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(7, freshDegreeDist.size());
    }

    @Test
    public void testDepthWithFreshGraph() throws WikiApiException
    {
        CategoryGraph freshGraph = new CategoryGraph(wiki);
        double depth = freshGraph.getDepth();
        assertEquals(4, depth, 0.00001);
    }

    @Test
    public void testDiameterGreaterThanZeroWithFreshGraph() throws WikiApiException
    {
        CategoryGraph freshGraph = new CategoryGraph(wiki);
        assertTrue(freshGraph.getDiameter() > 0);
    }

    @Test
    public void testAverageShortestPathGreaterThanZeroWithFreshGraph() throws WikiApiException
    {
        CategoryGraph freshGraph = new CategoryGraph(wiki);
        assertTrue(freshGraph.getAverageShortestPathLength() > 0);
    }

    @Test
    public void testDepthGreaterThanZeroWithFreshGraph() throws WikiApiException
    {
        CategoryGraph freshGraph = new CategoryGraph(wiki);
        assertTrue(freshGraph.getDepth() > 0);
    }

    @Test
    public void testGraphInfoContainsExpectedValues() throws WikiApiException
    {
        CategoryGraph freshGraph = new CategoryGraph(wiki);
        String graphInfo = freshGraph.getGraphInfo();
        assertTrue(graphInfo.contains("Number of Nodes:"));
        assertTrue(graphInfo.contains("Number of Edges:"));
        assertTrue(graphInfo.contains("Diameter:"));
        assertTrue(graphInfo.contains("Avg. path length:"));
    }

    @Test
    public void testPathLengthsConsistentWithGraphParameters() throws WikiApiException
    {
        CategoryGraph freshGraph = new CategoryGraph(wiki);
        double avgSP = freshGraph.getAverageShortestPathLength();
        double diameter = freshGraph.getDiameter();
        assertTrue(avgSP > 0);
        assertTrue(diameter >= avgSP);
        assertEquals(5.0, diameter, 0.00001);
        assertEquals(2.3823529411764706, avgSP, 0.00001);
    }

    @Test
    public void testClusterCoefficientWithFreshGraph() throws WikiApiException
    {
        CategoryGraph freshGraph = new CategoryGraph(wiki);
        double cc = freshGraph.getClusterCoefficient();
        assertTrue(cc >= 0);
        assertTrue(cc <= 1);
    }
}
