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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class ApiUtilities {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * DOTS - print progress dots.
     * TEXT - print a message with progress in percent.
     *
     */
    public enum ProgressInfoMode { DOTS, TEXT };

    /**
     * Prints a progress counter.
     * @param counter Indicates the position in the task.
     * @param size Size of the overall task.
     * @param step How many parts should the progress counter have?
     * @param mode Sets the output mode.
     * @param text The text that should be print along with the progress indicator.
     */
    public static void printProgressInfo(int counter, int size, int step, ProgressInfoMode mode, String text) {
        if (size < step) {
            return;
        }

        if (counter % (size / step) == 0) {
            double progressPercent = counter * 100 / size;
            progressPercent = 1 + Math.round(progressPercent * 100) / 100.0;
            if (mode.equals(ApiUtilities.ProgressInfoMode.TEXT)) {
                logger.info(text + ": " + progressPercent + " - " + OS.getUsedMemory() + " MB");
            }
            else if (mode.equals(ApiUtilities.ProgressInfoMode.DOTS)) {
                System.out.print(".");
                if (progressPercent >= 100) {
                    System.out.println();
                }
            }
        }
    }


//    /**
//     * Serialize an instance of CategoryGraph.
//     *
//     * @param fileName
//     *            Complete path and file name.
//     */
//    public static void saveCategoryGraph(String fileName, CategoryGraph catGraph) {
//        try {
//            ObjectOutputStream file = new ObjectOutputStream(
//                    new FileOutputStream(fileName));
//            file.writeObject(catGraph);
//            file.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Load a serialized instance of CategoryGraph.
//     * @param fileName Complete path and file name.
//     */
//    public static CategoryGraph loadCategoryGraph(String fileName) {
//        CategoryGraph catGraph;
//        try {
//            ObjectInputStream file = new ObjectInputStream(new FileInputStream(
//                    fileName));
//            catGraph = (CategoryGraph) file.readObject();
//            file.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return catGraph;
//    }
}
