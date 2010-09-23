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
package de.tudarmstadt.ukp.wikipedia.api;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.util.GraphSerialization;

// TODO category graph manager implements real singletons for category graphs
// up to now, it is only used in LSR
// There should be no way to construct a category graph that circumvents the manager.
public class CategoryGraphManager {

	private final static Log logger = LogFactory.getLog(CategoryGraphManager.class);

    private static Map<String,CategoryGraph> catGraphMap;

    private final static String catGraphSerializationFilename = "catGraphSer";

    public static CategoryGraph getCategoryGraph(Wikipedia wiki) throws WikiApiException {
        return getCategoryGraph(wiki, null, true);
    }

    public static CategoryGraph getCategoryGraph(Wikipedia wiki, boolean serialize) throws WikiApiException {
        return getCategoryGraph(wiki, null, serialize);
    }

    public static CategoryGraph getCategoryGraph(Wikipedia wiki, Set<Integer> pageIds) throws WikiApiException {
        return getCategoryGraph(wiki, pageIds, true);
    }

    public static CategoryGraph getCategoryGraph(Wikipedia wiki, Set<Integer> pageIds, boolean serialize) throws WikiApiException {
        if (catGraphMap == null) {
            catGraphMap = new HashMap<String,CategoryGraph>();
        }

        String wikiID = wiki.getWikipediaId();
        if (catGraphMap.containsKey(wikiID)) {
            return catGraphMap.get(wikiID);
        }

        String size = "";
        if (pageIds != null) {
            size = new Integer(pageIds.size()).toString();
        }

        CategoryGraph catGraph;
        if (serialize) {
            catGraph = tryToLoadCategoryGraph(wiki, wikiID, size);
            if (catGraph != null) {
                catGraphMap.put(wikiID, catGraph);
                return catGraph;
            }
        }


        // could not be loaded (= no serialized category graph was written so far) => create it
        if (pageIds != null) {
            catGraph = new CategoryGraph(wiki, pageIds);
        }
        else {
            catGraph = new CategoryGraph(wiki);
        }

        catGraphMap.put(wikiID, catGraph);

        if (serialize) {
            saveCategoryGraph(catGraph, wikiID, size);
        }

        return catGraph;
    }

     private static CategoryGraph tryToLoadCategoryGraph(Wikipedia wiki, String wikiId, String size) throws WikiApiException {

         String defaultSerializedGraphLocation = getCategoryGraphSerializationFileName(wikiId, size);
         File defaulSerializedGraphFile = new File(defaultSerializedGraphLocation);
         if (defaulSerializedGraphFile.exists()) {
             try {
                 logger.info("Loading category graph from " + defaultSerializedGraphLocation);
                 return new CategoryGraph(wiki, GraphSerialization.loadGraph(defaultSerializedGraphLocation));
             } catch (IOException e) {
                 throw new WikiApiException(e);
             } catch (ClassNotFoundException e) {
                 throw new WikiApiException(e);
             }
         }
         else {
             return null;
         }
     }

      private static void saveCategoryGraph(CategoryGraph catGraph, String wikiId, String size) throws WikiApiException {
          String defaultSerializedGraphLocation = getCategoryGraphSerializationFileName(wikiId, size);
          try {
              logger.info("Saving category graph to " + defaultSerializedGraphLocation);
              GraphSerialization.saveGraph(catGraph.getGraph(), defaultSerializedGraphLocation);
          } catch (IOException e) {
              throw new WikiApiException(e);
          }
      }

      private static String getCategoryGraphSerializationFileName(String wikiId, String size) {
          return catGraphSerializationFilename + "_" + wikiId + size;
      }
}
