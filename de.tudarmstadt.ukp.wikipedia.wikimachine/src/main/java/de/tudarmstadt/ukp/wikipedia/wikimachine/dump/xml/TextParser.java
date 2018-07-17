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
