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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.tudarmstadt.ukp.wikipedia.util.OS;

public class ApiUtilities {

	private static final Log logger = LogFactory.getLog(ApiUtilities.class);

    /**
     * DOTS - print progress dots.
     * TEXT - print a message with progress in percent.
     * @author zesch
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
