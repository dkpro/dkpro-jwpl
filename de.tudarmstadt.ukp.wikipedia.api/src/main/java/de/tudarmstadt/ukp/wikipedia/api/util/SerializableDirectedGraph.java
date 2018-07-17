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
package de.tudarmstadt.ukp.wikipedia.api.util;

import java.io.Serializable;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Serializable Wrapper for a DirectedGraph object, that has Integer objects as vertices and DefaultEdge objects as edges.<br>
 * There is no need in this case to serializale vertices and edges separately, because they already implement the interface Serializable.
 *
 *
 */
public final class SerializableDirectedGraph implements Serializable {

    /**
     * Generated serial ID.
     */
    private static final long serialVersionUID = -8298189410676038723L;

    private DirectedGraph<Integer,DefaultEdge> graph;

    /**
     * This Constructor is intended to be used before the serialization of the <br>
     * directed graph.
     * @param graph
     */
    public SerializableDirectedGraph(DirectedGraph<Integer,DefaultEdge> graph){
        this.graph = graph;
    }

    /**
     * Returns the graph.
     * @return
     */
    public DirectedGraph<Integer,DefaultEdge> getGraph(){
        return graph;
    }
}
