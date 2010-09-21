/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.wikimachine.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is a inverse class of {@link UTFDataOutputStream} witch goal is to
 * reminder the length limitation of written UTF-8 strings
 * 
 * @author i_galkin
 * 
 * @see UTFDataOutputStream
 * 
 */
public class UTFDataInputStream extends DataInputStream {

	final static boolean END_REACHED = true;

	public UTFDataInputStream(InputStream in){
		super(in);
	}

	/**
	 * Read a fragmented UTF-8 String
	 * 
	 * @return a String written with
	 *         {@link UTFDataOutputStream#writeFragmentedUTF(String)}
	 * @throws IOException
	 * 
	 * @see UTFDataOutputStream#writeFragmentedUTF(String)
	 */
	public String readFragmentedUTF() throws IOException {

		// FIXME could it give more performance to use StringBuffer or
		// StringBulder? Is the count of large strings greater as a count of
		// short strings?

		String result = super.readUTF();
		boolean fragmentFlag = super.readBoolean();
		while (fragmentFlag != END_REACHED) {
			result = result.concat(super.readUTF());
			fragmentFlag = super.readBoolean();
		}
		return result;

	}

	/**
	 * Read a byte array formed UTF-8 String
	 * 
	 * @return a String written with
	 *         {@link UTFDataOutputStream#writeUTFAsArray(String)}
	 * @throws IOException
	 * 
	 * @see UTFDataOutputStream#writeUTFAsArray(String)
	 */
	public String readUTFAsArray() throws IOException {
		byte[] buffer = new byte[super.readInt()];
		super.read(buffer, 0, buffer.length);
		return new String(buffer, "UTF-8");
	}

}
