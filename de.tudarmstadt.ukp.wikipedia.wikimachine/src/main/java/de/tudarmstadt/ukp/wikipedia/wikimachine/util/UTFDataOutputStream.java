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
package de.tudarmstadt.ukp.wikipedia.wikimachine.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * The standard <code>DataOutputStream.writeUTF(String)</code> limits the string
 * with 65536 byte sized buffer. To avoid this limitation there is two methods
 * to use:
 * <ul>
 * <li>{@link UTFDataOutputStream#writeFragmentedUTF(String)}</li>
 * <li>{@link UTFDataOutputStream#writeUTFAsArray}</li>
 * </ul>
 *
 * @see DataOutputStream#writeUTF(String)
 *
 */
public class UTFDataOutputStream extends DataOutputStream {

	final static int MAX_LENGTH = 16384;
	final static boolean END_REACHED = true;
	final static boolean FRAGMENTS_FOLLOW = false;

	public UTFDataOutputStream(OutputStream out) {
		super(out);
	}

	private void writeUTFFragment(String str) throws IOException {
		super.writeUTF(str);
		super.writeBoolean(FRAGMENTS_FOLLOW);
	}

	private void writeLastUTFFragment(String str) throws IOException {
		super.writeUTF(str);
		super.writeBoolean(END_REACHED);
	}

	/**
	 * The UTF-8 encoding uses sequences of 1, 2, or 3 bytes per character. With
	 * he maximal length of the fragment we want to ensure, that there are no
	 * overflow of 65536 byte sized buffer
	 *
	 * @param str
	 *            String to be written in the output stream
	 * @throws IOException
	 */
	public void writeFragmentedUTF(String str) throws IOException {
		if (str.length() <= MAX_LENGTH) {
			writeLastUTFFragment(str);
		} else {
			writeUTFFragment(str.substring(0, MAX_LENGTH));
			writeFragmentedUTF(str.substring(MAX_LENGTH));
		}
	}

	/**
	 * This method uses the {@link String#getBytes(String)} method to write
	 * <ol>
	 * <li>the size of the byte array</li>
	 * <li>the unicode byte sequence of this string</li>
	 * </ol>
	 *
	 * @param str
	 *            String to be written in the output stream
	 * @throws IOException
	 */
	public void writeUTFAsArray(String str) throws IOException {
		byte[] buffer = str.getBytes("UTF-8");
		super.writeInt(buffer.length);
		super.write(buffer, 0, buffer.length);
	}

}
