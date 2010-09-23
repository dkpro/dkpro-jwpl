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
 * @author i_galkin
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
