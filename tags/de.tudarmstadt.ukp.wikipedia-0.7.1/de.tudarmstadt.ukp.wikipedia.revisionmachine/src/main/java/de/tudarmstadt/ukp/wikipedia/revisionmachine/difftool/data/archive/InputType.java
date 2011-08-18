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
