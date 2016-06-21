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

	/**
	 * Replaces all problematic characters from a String with their escaped
	 * versions to make it SQL conform.
	 *
	 * @param str unescaped String
	 * @return SQL safe escaped String
	 */
	public static String sqlEscape(String str) {
		final int len = str.length();

		StringBuilder sql = new StringBuilder(len * 2);

		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			switch (c) {
			case '\u0000':
				sql.append('\\').append('0');
				break;
			case '\n':
				sql.append('\\').append('n');
				break;
			case '\t':
				sql.append('\\').append('t');
				break;
			case '\r':
				sql.append('\\').append('r');
				break;
			case '\u001a':
				sql.append('\\').append('Z');
				break;
			case '\'':
				sql.append('\\').append('\'');
				break;
			case '\"':
				sql.append('\\').append('"');
				break;
			case '\b':
				sql.append('\\').append('b');
				break;
			case '\\':
				sql.append('\\').append('\\');
				break;
//			case '%':
//				sql.append('[').append('%').append(']');
//				break;
//			case '_':
//				sql.append('[').append('_').append(']');
//				break;
			default:
				sql.append(c);
				break;
			}
		}
		return sql.toString();
	}


}
