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
package de.tudarmstadt.ukp.wikipedia.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class CommonUtilities {

    /** Debug output an internal set structure.
     * @param s
     */
    public static String getSetContents(Set s) {
        StringBuffer sb = new StringBuffer(1000);

        Object[] sortedArray = s.toArray();
        Arrays.sort(sortedArray);

        int counter = 0;
        int elementsPerRow = 10;
        for (Object element : sortedArray) {
            sb.append(element.toString() + " ");
            counter++;
            if ((counter % elementsPerRow) == 0) {
                sb.append(System.getProperty("line.separator"));
            }
        }
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }

    /** Debug output an internal map structure as key-value pairs.
     * @param m
     */
    public static String getMapContents(Map m) {
        StringBuffer sb = new StringBuffer(1000);
        Object[] sortedArray = m.keySet().toArray();
        Arrays.sort(sortedArray);

        for (Object element : sortedArray) {
            sb.append(element.toString() + " - " + m.get(element) + System.getProperty("line.separator"));
        }
        return sb.toString();
    }

}
