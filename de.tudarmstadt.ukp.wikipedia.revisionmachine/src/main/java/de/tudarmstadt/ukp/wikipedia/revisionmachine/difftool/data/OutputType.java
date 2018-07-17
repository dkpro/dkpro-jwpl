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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data;

/**
 * This Enumerator list the possible output values.
 *
 *
 *
 */
public enum OutputType
{

	/** The output will consist of a single or multiple sql files */
	UNCOMPRESSED,

	/** The output will consist of a single or multiple 7z archives */
	SEVENZIP,

	/** The output will consist of a single or multiple bzip2 archives */
	BZIP2,

	/** The output will consist of a single or multiple alternate archives */
	ALTERNATE,

	/** The output will be directly written into a database */
	DATABASE;

	/**
	 * Parses the given string.
	 *
	 * @param s
	 *            string
	 * @return OutputTypes
	 */
	public static OutputType parse(final String s)
	{

		String t = s.toUpperCase();

		if (t.equals("UNCOMPRESSED")) {
			return OutputType.UNCOMPRESSED;
		}
		else if (t.equals("SEVENZIP")) {
			return OutputType.SEVENZIP;
		}
		else if (t.equals("BZIP2")) {
			return OutputType.BZIP2;
		}
		else if (t.equals("DATABASE")) {
			return OutputType.DATABASE;
		}
		else if (t.equals("ALTERNATE")) {
			return OutputType.ALTERNATE;
		}

		throw new IllegalArgumentException("Unknown OutputType : " + s);
	}
}
