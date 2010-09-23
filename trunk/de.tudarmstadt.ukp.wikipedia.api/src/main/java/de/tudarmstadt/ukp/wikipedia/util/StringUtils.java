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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StringUtils {

	private static final Log logger = LogFactory.getLog(StringUtils.class);

    /**
     * Joins the elements of a collection into a string.
     * @param c The collection which elements should be joined.
     * @param delimiter String that is introduced between two joined elements.
     * @return The joined string.
     */
    public static String join(Collection c, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

    public static String getFileContent(String filename, String encoding) {

        File file = new File(filename);

        InputStream is;
        String textContents = "";
        try {
            is = new FileInputStream(file);
            // as the whole file is read at once -> buffering not necessary
            // InputStream is = new BufferedInputStream(new FileInputStream(file));
            byte[] contents = new byte[(int) file.length()];
            is.read(contents);
            textContents = new String(contents, encoding);
        } catch (FileNotFoundException e) {
            logger.error("File " + file.getAbsolutePath() + " not found.");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("IO exception while reading file " + file.getAbsolutePath());
            e.printStackTrace();
        }

        return textContents;
    }

}
