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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Utility for serializing and deserializing {@link DefaultDirectedGraph} objects, that are<br>
 * wrapped into {@link SerializableDirectedGraph} objects.
 *
 */
public final class GraphSerialization {

	/**
	 * This class cannot be instantiated.
	 */
	private GraphSerialization() {
	}

	/**
	 * Serializes the given {@link DefaultDirectedGraph} object to the given location.
	 * 
	 * @param graph Must not be {@code null}.
	 * @param location Must not be {@code null} and a valid file path.
	 * @throws IOException Thrown if errors occurred on the IO level.
	 */
	public static void saveGraph(DefaultDirectedGraph<Integer, DefaultEdge> graph, String location) throws IOException {
		File file = new File(location);
		file.createNewFile();
		if (!file.canWrite()) {
			throw new IOException("Cannot write to file " + location);
		}
		GraphSerialization.saveGraph(graph, file);
	}

	/**
	 * Serializes the given {@link DefaultDirectedGraph} object to the given location.
	 *
	 * @param graph Must not be {@code null}.
	 * @param file Must not be {@code null} and valid {@link File}.
	 * @throws IOException Thrown if errors occurred on the IO level.
	 */
	public static void saveGraph(DefaultDirectedGraph<Integer, DefaultEdge> graph, File file) throws IOException {
		SerializableDirectedGraph serialGraph = new SerializableDirectedGraph(graph);
		BufferedOutputStream fos;
		ObjectOutputStream out;
		fos = new BufferedOutputStream(new FileOutputStream(file));
		out = new ObjectOutputStream(fos);
		out.writeObject(serialGraph);
		out.close();

	}

	/**
	 * Deserializes a {@link SerializableDirectedGraph} object that is stored in the
	 * given location. This method returns the {@link DefaultDirectedGraph} object, that is wrapped
	 * in the {@link SerializableDirectedGraph}.
	 * 
	 * @param location Must not be {@code null} and a valid file path.
	 * @return The {@link DefaultDirectedGraph} object, that is wrapped in the
	 *         {@link SerializableDirectedGraph}.
	 * @throws IOException Thrown if errors occurred on the IO level.
	 * @throws ClassNotFoundException Thrown if a class could not be find while deserialization.
	 */
	public static DefaultDirectedGraph<Integer, DefaultEdge> loadGraph(String location)
			throws IOException, ClassNotFoundException {
		File file = new File(location);
		if (!file.canWrite()) {
			throw new IOException("Cannot read from file " + location);
		}
		return GraphSerialization.loadGraph(file);
	}

	/**
	 * Deserializes a {@link SerializableDirectedGraph} object that is stored in the
	 * given location. This method returns the {@link DefaultDirectedGraph}  object, that is wrapped
	 * in the {@link SerializableDirectedGraph}.
	 *
	 * @param file Must not be {@code null} and valid {@link File}.
	 * @return The {@link DefaultDirectedGraph} object, that is wrapped in the
	 *         {@link SerializableDirectedGraph}.
	 * @throws IOException Thrown if errors occurred on the IO level.
	 * @throws ClassNotFoundException Thrown if a class could not be find while deserialization.
	 */
	public static DefaultDirectedGraph<Integer, DefaultEdge> loadGraph(File file) throws IOException, ClassNotFoundException {
		SerializableDirectedGraph serialGraph;
		BufferedInputStream fin;
		ObjectInputStream in;
		fin = new BufferedInputStream(new FileInputStream(file));
		in = new ObjectInputStream(fin);
		serialGraph = (SerializableDirectedGraph) in.readObject();
		in.close();
		return serialGraph.getGraph();
	}
}
