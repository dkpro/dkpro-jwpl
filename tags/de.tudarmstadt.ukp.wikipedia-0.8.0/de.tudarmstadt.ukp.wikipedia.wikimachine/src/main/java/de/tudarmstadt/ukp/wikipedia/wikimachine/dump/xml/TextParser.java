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
/**
 * @(#)TextParser.java
 */
package de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import de.tudarmstadt.ukp.wikipedia.wikimachine.util.UTFDataInputStream;

public class TextParser {

	/**
	 * Need fields from the table text.<br>
	 * These fields are updated each time the method next() returns true.
	 */
	private int oldId;
	private String oldText;
	private UTFDataInputStream stream;

	/**
	 * Create a parser from an input stream
	 * 
	 * @param inputStream
	 * @throws IOException
	 * @author ivan.galkin
	 */
	public void setInputStream(InputStream inputStream){
		stream = new UTFDataInputStream(inputStream);
	}

	/**
	 * @return Returns the old_id.
	 */
	public int getOldId() {
		return oldId;
	}

	/**
	 * @return Returns the old_text.
	 */
	public String getOldText() {
		return oldText;
	}

	/**
	 * Returns true if the table has more rows.
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean next() throws IOException {
		boolean hasNext = true;
		try {
			oldId = stream.readInt();
			oldText = stream.readUTFAsArray();
		} catch (EOFException e) {
			hasNext = false;
		}
		return hasNext;
	}

	public void close() throws IOException {
		stream.close();
	}
}
