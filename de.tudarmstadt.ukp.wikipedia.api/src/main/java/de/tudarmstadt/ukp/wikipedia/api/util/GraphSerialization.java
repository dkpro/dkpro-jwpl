/*******************************************************************************
 * Copyright 2016
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
     * @param file
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
     * @param file
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
