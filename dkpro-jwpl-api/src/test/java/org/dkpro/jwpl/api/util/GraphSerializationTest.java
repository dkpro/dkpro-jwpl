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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.dkpro.jwpl.api.BaseJWPLTest;
import org.dkpro.jwpl.api.CategoryGraph;
import org.dkpro.jwpl.api.CategoryGraphManager;
import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.Wikipedia;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the correctness of the Category graph construction and its serialization<br>
 * process.
 */
public class GraphSerializationTest extends BaseJWPLTest {

    final String serializationFileName = "testCategoryGraph.ser";

    private static Wikipedia wiki;

    /**
     * Made this static so that following tests don't run if assumption fails.
     * (With AT_Before, tests also would not be executed but marked as passed)
     * This could be changed back as soon as JUnit ignored tests after failed
     * assumptions
     */
    @BeforeAll
    public static void setupWikipedia() {
        DatabaseConfiguration db = obtainHSDLDBConfiguration();
        try {
            wiki = new Wikipedia(db);
        } catch (Exception e) {
            fail("Wikipedia could not be initialized.");
        }
    }


    @BeforeEach
    public void cleanupBeforeTest() {
        File serializationFile = new File(serializationFileName);
        serializationFile.delete();
    }

    @AfterEach
    public void cleanupAfterTest() {
        File serializationFile = new File(serializationFileName);
        serializationFile.delete();
    }


    /**
     * Creates a CategoryGraph object using the Wikipedia object as parameter.<br>
     * Tests the correctness of the constructed graph.
     */
    @Test
    public void testGraphSerialization() {
      assertDoesNotThrow(() -> {
          CategoryGraph sourceGraph = CategoryGraphManager
                  .getCategoryGraph(wiki);
          testGraph(sourceGraph.getGraph());
          sourceGraph.saveGraph(serializationFileName);

          CategoryGraph loadedGraph = new CategoryGraph(wiki, new File(
                  serializationFileName));
          testGraph(loadedGraph.getGraph());
        });
    }

    /**
     * Compares the given graph with the expected graph. Returns true only if both<br>
     * graphs are identical.
     * @param graph
     */
    private void testGraph(DefaultDirectedGraph<Integer,DefaultEdge> graph){
        //make sure all vertices are there
        for(int i=1;i<16;i++){
            if(!graph.containsVertex(i)) {
                fail("Graph does not contain vertex " + i);
            }
        }
        if(!graph.containsVertex(30)) {
            fail("Graph does not contain vertex " + 200);
        }
        if(!graph.containsVertex(200)) {
            fail("Graph does not contain vertex " + 200);
        }

        //make sure there are no supplemental vertices
        assertEquals(17, graph.vertexSet().size());

        //make sure all edges are there
        if(!graph.containsEdge(1,200)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(1,2)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(1,4)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(1,3)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(1,5)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(3,6)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(4,9)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(5,8)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(6,9)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(6,8)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(6,7)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(7,11)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(7,10)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(8,15)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(8,13)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(8,14)) {
			fail("Graph does not contain edge");
		}
        if(!graph.containsEdge(8,12)) {
			fail("Graph does not contain edge");
		}

        //make sure there no supplemental edges
        assertEquals(17, graph.edgeSet().size());
    }

}
