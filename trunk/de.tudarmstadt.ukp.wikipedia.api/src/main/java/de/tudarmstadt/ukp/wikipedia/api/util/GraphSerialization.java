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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Utility for serializing and deserializing DirectedGraph objects, that are<br>
 * wrapped into SerializableDirectedGraph objects.
 * @author Anouar
 *
 */
public final class GraphSerialization {

    /**
     * This class cannot be instantiated.
     *
     */
    private GraphSerialization() {}
    

    
    /**
     * Serializes the given DirectedGraph object to the given location.
     * @param graph
     * @param location
     * @throws IOException 
     */
    public static void saveGraph(DirectedGraph<Integer,DefaultEdge> graph, String location) throws IOException {
        File file = new File(location);
        file.createNewFile();
        if (!file.canWrite()) {
            throw new IOException("Cannot write to file " + location);
        }
        GraphSerialization.saveGraph(graph, file);
    }

    /**
     * Serializes the given DirectedGraph object to the given location.
     * @param graph
     * @param location
     * @throws IOException 
     */
    public static void saveGraph(DirectedGraph<Integer,DefaultEdge> graph, File file) throws IOException{
        SerializableDirectedGraph serialGraph = new SerializableDirectedGraph(graph);
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(serialGraph);
        out.close();
        
    }
    
    /**
     * Deserializes a SerializableDirectedGraph object that is stored in the given<br>
     * location. This method returns the DirectedGraph object, that is wrapped in <br>
     * the SerializableDirectedGraph.
     * @param location
     * @return The DirectedGraph object, that is wrapped in the SerializableDirectedGraph.
     * @throws IOException 
     * @throws ClassNotFoundException 
     * @throws ClassNotFoundException 
     */
    public static DirectedGraph<Integer, DefaultEdge> loadGraph(String location) throws IOException, ClassNotFoundException  {
        File file = new File(location);
        if (!file.canWrite()) {
            throw new IOException("Cannot read from file " + location);
        }
        return GraphSerialization.loadGraph(file);
    }
 
        /**
     * Deserializes a SerializableDirectedGraph object that is stored in the given<br>
     * location. This method returns the DirectedGraph object, that is wrapped in <br>
     * the SerializableDirectedGraph.
     * @param location
     * @return The DirectedGraph object, that is wrapped in the SerializableDirectedGraph.
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public static DirectedGraph<Integer, DefaultEdge> loadGraph(File file) throws IOException, ClassNotFoundException{
        SerializableDirectedGraph serialGraph = null;
        FileInputStream fin = null;
        ObjectInputStream in = null;
        fin = new FileInputStream(file);
        in = new ObjectInputStream(fin);
        serialGraph = (SerializableDirectedGraph) in.readObject();
        in.close();
        return serialGraph.getGraph();
    }
}
