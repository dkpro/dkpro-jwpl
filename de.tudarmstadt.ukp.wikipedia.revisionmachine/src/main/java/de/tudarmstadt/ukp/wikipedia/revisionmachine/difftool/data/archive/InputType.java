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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive;

/**
 * This class represents an enumeration of the input type.
 *
 *
 *
 */
public enum InputType
{

	/** Uncompressed XML Input */
	XML,

	/** SevenZip Compressed XML Input */
	SEVENZIP,

	/** BZip2 Compressed XML Input */
	BZIP2;

	/**
	 * Parses the string representation to the related InputType.
	 *
	 * @param s
	 *            String representation of the InputType.
	 * @return InputType Enumerator
	 *
	 * @throws IllegalArgumentException
	 *             if the parsed String does not match with one of the
	 *             enumerators
	 */
	public static InputType parse(final String s)
	{

		String t = s.toUpperCase();

		if (t.equals("XML")) {
			return XML;
		}
		else if (t.equals("SEVENZIP")) {
			return SEVENZIP;
		}
		else if (t.equals("BZIP2")) {
			return BZIP2;
		}

		throw new IllegalArgumentException("Unknown InputType : " + s);
	}
}
