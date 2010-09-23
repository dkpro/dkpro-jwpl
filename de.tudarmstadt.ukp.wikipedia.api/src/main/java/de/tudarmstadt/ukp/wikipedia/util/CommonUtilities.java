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
