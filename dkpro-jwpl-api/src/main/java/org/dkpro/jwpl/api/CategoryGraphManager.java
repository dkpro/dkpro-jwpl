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

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.util.GraphSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides ways to create and retrieve {@link CategoryGraph graphs} from the Wikipedia backend.
 *
 * @see Wikipedia
 * @see CategoryGraph
 */
// TODO category graph manager implements real singletons for category graphs
//  up to now, it is only used in LSR.
//  There should be no way to construct a category graph that circumvents the manager.
public class CategoryGraphManager
{

    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Caches the graphs built so far, keyed by {@link #getCategoryGraphKey(String, Set)}, so that
     * the full graph and each subset of the pages of a Wikipedia have entries of their own.
     */
    private static final Map<String, CategoryGraph> catGraphMap = new ConcurrentHashMap<>();

    private final static String catGraphSerializationFilename = "catGraphSer";

    /**
     * The algorithm of the digest over the page ids in the key of a graph over a subset of the
     * pages.
     */
    private static final String KEY_DIGEST_ALGORITHM = "SHA-256";

    /**
     * Whether {@link #KEY_DIGEST_ALGORITHM} is available, resolved once when the class is
     * initialized so that the key of a subset is not built only to find out that it cannot be
     * digested.
     */
    private static final boolean KEY_DIGEST_AVAILABLE = isAlgorithmAvailable(KEY_DIGEST_ALGORITHM);

    /**
     * Retrieves a {@link CategoryGraph} instance for all categories in a {@link Wikipedia} instance.
     * Additionally, the graph is persisted if it could be constructed successfully.
     *
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @return A graph representation of all categories in {@code wiki}.
     *
     * @throws WikiApiException Thrown if errors occurred.
     */
    public static CategoryGraph getCategoryGraph(Wikipedia wiki) throws WikiApiException
    {
        return getCategoryGraph(wiki, null, true);
    }

    /**
     * Retrieves a {@link CategoryGraph} instance for all categories in a {@link Wikipedia} instance.
     * Additionally, the graph is persisted if it could be constructed successfully.
     * 
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @param serialize If {@code true}, attempts to load a serialized version of the graph,
     *                  {@code false} otherwise. If {@code true} and no previous version of exists,
     *                  a completely new graph instance is created and retrieved via {@code wiki}.
     * @return A graph representation of all categories in {@code wiki}.
     *
     * @throws WikiApiException Thrown if errors occurred.
     */
    public static CategoryGraph getCategoryGraph(Wikipedia wiki, boolean serialize)
        throws WikiApiException
    {
        return getCategoryGraph(wiki, null, serialize);
    }


    /**
     * Retrieves a {@link CategoryGraph} instance for all categories in a {@link Wikipedia} instance.
     * Additionally, the graph is persisted if it could be constructed successfully.
     * 
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @param pageIds A set of page ids (of category pages) that should be used to build the category
     *                graph from.
     * @return A graph representation of all categories in {@code wiki}.
     *
     * @throws WikiApiException Thrown if errors occurred.
     */
    public static CategoryGraph getCategoryGraph(Wikipedia wiki, Set<Integer> pageIds)
        throws WikiApiException
    {
        return getCategoryGraph(wiki, pageIds, true);
    }

    /**
     * Retrieves a {@link CategoryGraph} instance for all categories in a {@link Wikipedia} instance.
     * Additionally, the graph is persisted if it could be constructed successfully.
     *
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @param pageIds A set of page ids (of category pages) that should be used to build the category
     *                graph from.
     * @param serialize If {@code true}, attempts to load a serialized version of the graph,
     *                  {@code false} otherwise. If {@code true} and no previous version of exists,
     *                  a completely new graph instance is created and retrieved via {@code wiki}.
     * @return A graph representation of all categories in {@code wiki}.
     *
     * @throws WikiApiException Thrown if errors occurred.
     */
    public static CategoryGraph getCategoryGraph(Wikipedia wiki, Set<Integer> pageIds,
            boolean serialize)
        throws WikiApiException
    {
        String key = getCategoryGraphKey(wiki.getWikipediaId(), pageIds);
        CategoryGraph catGraph = catGraphMap.get(key);
        if (catGraph != null) {
            return catGraph;
        }

        if (serialize) {
            catGraph = tryToLoadCategoryGraph(wiki, key);
            if (catGraph != null) {
                return cache(key, catGraph);
            }
        }

        // could not be loaded (= no serialized category graph was written so far) => create it
        if (pageIds != null) {
            catGraph = new CategoryGraph(wiki, pageIds);
        }
        else {
            catGraph = new CategoryGraph(wiki);
        }

        if (serialize) {
            saveCategoryGraph(catGraph, key);
        }

        return cache(key, catGraph);
    }

    /**
     * Puts {@code catGraph} into the cache unless another thread has cached a graph for
     * {@code key} in the meantime, in which case that graph is returned instead.
     */
    private static CategoryGraph cache(String key, CategoryGraph catGraph)
    {
        CategoryGraph previous = catGraphMap.putIfAbsent(key, catGraph);
        return previous != null ? previous : catGraph;
    }

    /**
     * Builds the key under which a graph is cached and persisted. The full graph of a Wikipedia is
     * keyed by its id alone, as before. A graph over a subset of the pages is keyed by the id, the
     * size of the subset and a digest of its sorted page ids, so that different subsets, even of
     * the same size, are told apart.
     *
     * @param wikiId The id of the Wikipedia the graph is built from.
     * @param pageIds The page ids the graph is built from, or {@code null} for the full graph.
     * @return The key for the graph.
     */
    static String getCategoryGraphKey(String wikiId, Set<Integer> pageIds)
    {
        if (pageIds == null) {
            return wikiId;
        }
        if (!KEY_DIGEST_AVAILABLE) {
            // every Java platform is required to support SHA-256
            throw new IllegalStateException(KEY_DIGEST_ALGORITHM + " is not available");
        }
        int[] ids = pageIds.stream().mapToInt(Integer::intValue).sorted().toArray();
        ByteBuffer buffer = ByteBuffer.allocate(ids.length * Integer.BYTES);
        for (int id : ids) {
            buffer.putInt(id);
        }
        byte[] digest = newKeyDigest().digest(buffer.array());
        return wikiId + "_" + ids.length + "_" + HexFormat.of().formatHex(digest, 0, 8);
    }

    private static boolean isAlgorithmAvailable(String algorithm)
    {
        try {
            MessageDigest.getInstance(algorithm);
            return true;
        }
        catch (NoSuchAlgorithmException e) {
            logger.warn("{} is not available, graphs over a subset of the pages cannot be keyed",
                    algorithm, e);
            return false;
        }
    }

    /**
     * Creates a new digest for each key, as {@link MessageDigest} instances are not thread-safe.
     * Must only be called if {@link #KEY_DIGEST_AVAILABLE} is {@code true}.
     */
    private static MessageDigest newKeyDigest()
    {
        try {
            return MessageDigest.getInstance(KEY_DIGEST_ALGORITHM);
        }
        catch (NoSuchAlgorithmException e) {
            // cannot happen, the availability was checked when the class was initialized
            throw new IllegalStateException(e);
        }
    }

    private static CategoryGraph tryToLoadCategoryGraph(Wikipedia wiki, String key)
        throws WikiApiException
    {

        String defaultSerializedGraphLocation = getCategoryGraphSerializationFileName(key);
        File defaulSerializedGraphFile = new File(defaultSerializedGraphLocation);
        if (defaulSerializedGraphFile.exists()) {
            try {
                logger.info("Loading category graph from {}", defaultSerializedGraphLocation);
                return new CategoryGraph(wiki,
                        GraphSerialization.loadGraph(defaultSerializedGraphLocation));
            }
            catch (IOException | ClassNotFoundException e) {
                throw new WikiApiException(e);
            }
        }
        else {
            return null;
        }
    }

    private static void saveCategoryGraph(CategoryGraph catGraph, String key)
        throws WikiApiException
    {
        String defaultSerializedGraphLocation = getCategoryGraphSerializationFileName(key);
        try {
            logger.info("Saving category graph to {}", defaultSerializedGraphLocation);
            GraphSerialization.saveGraph(catGraph.getGraph(), defaultSerializedGraphLocation);
        }
        catch (IOException e) {
            throw new WikiApiException(e);
        }
    }

    private static String getCategoryGraphSerializationFileName(String key)
    {
        return catGraphSerializationFilename + "_" + key;
    }
}
