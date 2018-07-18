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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is a inverse class of {@link UTFDataOutputStream} witch goal is to
 * reminder the length limitation of written UTF-8 strings
 *
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

		//String result = super.readUTF();
		StringBuffer result = new StringBuffer(super.readUTF());
		boolean fragmentFlag = super.readBoolean();
		while (fragmentFlag != END_REACHED) {
			//result = result.concat(super.readUTF());
			result.append(super.readUTF());
			fragmentFlag = super.readBoolean();
		}
		return result.toString();

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
