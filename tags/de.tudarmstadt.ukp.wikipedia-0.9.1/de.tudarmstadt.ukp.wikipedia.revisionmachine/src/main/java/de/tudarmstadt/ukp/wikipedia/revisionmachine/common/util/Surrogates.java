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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util;

/**
 * This utitly class contains some surrogate related methods.
 * 
 * 
 * 
 */
public class Surrogates
{

	/** No object - utility class */
	private Surrogates()
	{
	}

	/**
	 * Returns whether a surrogate character was contained in the specified
	 * input.
	 * 
	 * @param input
	 *            input
	 * @return if a surrogate character was contained or not
	 */
	public static boolean scan(final char[] input)
	{

		int surLow = 0xD800;
		int surHgh = 0xDFFF;

		int end = input.length;
		for (int i = 0; i < end; i++) {
			if ((int) input[i] >= surLow && input[i] <= surHgh) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Replaces all surrogates characters with '?'.
	 * 
	 * @param input
	 *            input
	 * @return input with '?' instead of surrogates characters
	 */
	public static char[] replace(final char[] input)
	{

		int surLow = 0xD800;
		int surHgh = 0xDFFF;

		int end = input.length;
		char[] output = new char[end];

		for (int i = 0; i < end; i++) {
			if ((int) input[i] >= surLow && input[i] <= surHgh) {
				output[i] = '?';
			}
			else {
				output[i] = input[i];
			}
		}

		return output;
	}
}
