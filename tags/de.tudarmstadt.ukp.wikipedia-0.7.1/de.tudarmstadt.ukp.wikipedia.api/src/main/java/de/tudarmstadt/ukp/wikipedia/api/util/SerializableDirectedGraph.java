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

import java.io.Serializable;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Serializable Wrapper for a DirectedGraph object, that has Integer objects as vertices and DefaultEdge objects as edges.<br>
 * There is no need in this case to serializale vertices and edges separately, because they already implement the interface Serializable.
 * 
 * @author Anouar
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
