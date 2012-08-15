/*******************************************************************************
 * Copyright (c) 2011 Ubiquitous Knowledge Processing Lab
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Project Website:
 * 	http://jwpl.googlecode.com
 * 
 * Contributors:
 * 	Torsten Zesch
 * 	Simon Kulessa
 * 	Oliver Ferschke
 ******************************************************************************/
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
