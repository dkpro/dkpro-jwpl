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
 * This Enumerator lists the different method of how to handle surrogates.
 *
 * TODO: The surrogate mode implementations need a work over.
 * TODO Add documentation for surrogates
 */
public enum SurrogateModes
{

	/**
	 * Replace the surrogate
	 * TODO COULD BE FAULTY. CHECK BEFORE USING!!! DISABLED FOR NOW!
	 */
	REPLACE,

	/**
	 * Throw an error if a surrogate is detected
	 * TODO COULD BE FAULTY. CHECK BEFORE USING!!! DISABLED FOR NOW!
	 */
	THROW_ERROR,

	/**
	 * Discard the rest of the article after a surrogate is detected
	 * TODO COULD BE FAULTY. CHECK BEFORE USING!!! DISABLED FOR NOW!
	 */
	DISCARD_REST,

	/** Discard revisions which contain surrogates (java default setting) */
	DISCARD_REVISION;

	/**
	 * Parses the given string.
	 *
	 * @param s
	 *            string
	 * @return SurrogateModes
	 */
	public static SurrogateModes parse(final String s)
	{

		String t = s.toUpperCase();

		if (t.equals("REPLACE")) {
			// return REPLACE;
			throw new UnsupportedOperationException(
					"This mode is currently not supported. Please check the implementation first. For now, you can use the default mode DISCARD_REVISION");
		}
		else if (t.equals("THROW_ERROR")) {
			// return THROW_ERROR;
			throw new UnsupportedOperationException(
					"This mode is currently not supported. Please check the implementation first. For now, you can use the default mode DISCARD_REVISION");
		}
		else if (t.equals("DISCARD_REST")) {
			// return DISCARD_REST;
			throw new UnsupportedOperationException(
					"This mode is currently not supported. Please check the implementation first. For now, you can use the default mode DISCARD_REVISION");
		}
		else if (t.equals("DISCARD_REVISION")) {
			return DISCARD_REVISION;
		}

		throw new IllegalArgumentException("Unknown SurrogateModes : " + s);
	}
}
