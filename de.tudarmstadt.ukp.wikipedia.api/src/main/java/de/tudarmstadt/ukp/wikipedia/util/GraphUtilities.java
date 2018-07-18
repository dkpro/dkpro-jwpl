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
package de.tudarmstadt.ukp.wikipedia.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.wikipedia.api.Page;

public class GraphUtilities {

	private static final Log logger = LogFactory.getLog(GraphUtilities.class);

    public static Set<Integer> getRandomPageSubset(Iterable<Page> pages, int pResultSetSize) {
        Set<Integer> pageIDs = new HashSet<Integer>();
        while (pages.iterator().hasNext()) {
            pageIDs.add(pages.iterator().next().getPageId());
        }
        return getRandomPageSubset(pageIDs, pResultSetSize);
    }

    /** Get a random subset (of size pSize) of the page set passed to the method.
     * @param pPageIDs The pages.
     * @param pResultSetSize The size of the result set.
     * @return A random subset of the original page set of the given size or null, if the requested subset size is larger than the original page set.
     */
    public static Set<Integer> getRandomPageSubset(Set<Integer> pPageIDs, int pResultSetSize) {

        Set<Integer> uniqueRandomSet = new HashSet<Integer>();

        if (pPageIDs.size() < pResultSetSize) {
            logger.error("Requested subset size is larger than the original page set size.");
            return null;
        }

        Random rand = new Random();

        Object[] pageIdArray = pPageIDs.toArray();

        // If pSize is quite close to the size of the original pageSet the probability of generating the offset of the last missing pageIDs is quite low, with the consequence of unpredictable run-time.
        // => if more than the half of pages should be included in the result set, better remove random numbers than adding them
        if (pResultSetSize > (pPageIDs.size() / 2)) {
            uniqueRandomSet.addAll(pPageIDs);
            while (uniqueRandomSet.size() > pResultSetSize) {
                int randomOffset = rand.nextInt(pPageIDs.size());
                if (uniqueRandomSet.contains(pageIdArray[randomOffset])) {
                    uniqueRandomSet.remove(pageIdArray[randomOffset]);
                }
            }
        }
        else {
            while (uniqueRandomSet.size() < pResultSetSize) {
                int randomOffset = rand.nextInt(pPageIDs.size());
                if (!uniqueRandomSet.contains(pageIdArray[randomOffset])) {
                    uniqueRandomSet.add((Integer)pageIdArray[randomOffset]);
                }
            }
        }

        return uniqueRandomSet;
    }
}
