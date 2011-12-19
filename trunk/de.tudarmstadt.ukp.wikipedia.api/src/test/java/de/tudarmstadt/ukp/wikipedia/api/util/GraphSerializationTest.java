/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNoException;

import java.io.File;

import junit.framework.JUnit4TestAdapter;
import junit.textui.TestRunner;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.CategoryGraph;
import de.tudarmstadt.ukp.wikipedia.api.CategoryGraphManager;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;

/**
 * Tests for the correctness of the Category graph construction and its serialization<br>
 * process.
 * @author Anouar 30/09/2007
 *
 *
 */
public class GraphSerializationTest {

    String serializationFileName = "testCategoryGraph.ser";


    private static Wikipedia wiki;

    /**
     * Creates a Wikipedia object.
     * Made this static so that following tests don't run if assumption fails.
     * (With AT_Before, tests would also not be executed but marked as passed)
     * @throws WikiInitializationException
     */
    @BeforeClass
    public static void initializeWikipedia(){
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setDatabase("wikiapi_test");
        dbConfig.setHost("bender.ukp.informatik.tu-darmstadt.de");
        dbConfig.setLanguage(Language._test);
        dbConfig.setPassword("student");
        dbConfig.setUser("student");
        try{
        	wiki = new Wikipedia(dbConfig);
        }catch(Exception e){
        	assumeNoException(e);
        }
    }


    @Before
    public void cleanupBeforeTest() {
        File serializationFile = new File(serializationFileName);
        serializationFile.delete();
    }

    @After
    public void cleanupAfterTest() {
        File serializationFile = new File(serializationFileName);
        serializationFile.delete();
    }


    /**
     * Creates a CategoryGraph object using the Wikipedia object as parameter.<br>
     * Tests the correctness of the constructed graph.
     * @throws WikiApiException
     */
	@Test
	public void testGraphSerialization()
	{
		try {
			CategoryGraph sourceGraph = CategoryGraphManager
					.getCategoryGraph(wiki);
			testGraph(sourceGraph.getGraph());
			sourceGraph.saveGraph(serializationFileName);

			CategoryGraph loadedGraph = new CategoryGraph(wiki, new File(
					serializationFileName));
			testGraph(loadedGraph.getGraph());
		}
		catch (Exception e) {
			assumeNoException(e);
		}

	}

    /**
     * Compares the given graph with the expected graph. Returns true only if both<br>
     * graphs are identical.
     * @param graph
     * @return
     */
    private void testGraph(DirectedGraph<Integer,DefaultEdge> graph){
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

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(GraphSerializationTest.class);
    }

    public static void main (String[] a){
        TestRunner.run(suite());
    }

}
