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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util;

/**
 * Mathematic functions
 *
 *
 *
 */
public class MathUtilities
{

	/** No object - utility class */
	private MathUtilities()
	{
	}

	/**
	 * Rounds the given number to a precision of two after digit numbers.
	 *
	 * @param v
	 *            number
	 * @return rounded number
	 */
	public static double round(final double v)
	{
		return ((long) (v * 100.)) / 100.;
	}

	/**
	 * Rounds the result of a / (a + b) to a precision of two after digit
	 * numbers.
	 *
	 * @param a
	 *            value a
	 * @param b
	 *            value b
	 * @return xx.xx
	 */
	public static double percentPlus(final double a, final double b)
	{
		return round((double) a / (double) (a + b));
	}

	/**
	 * Rounds the result of a / (a + b) to a precision of two after digit
	 * numbers.
	 *
	 * @param a
	 *            value a
	 * @param b
	 *            value b
	 * @return xx.xx
	 */
	public static double percRoundPlus(final double a, final double b)
	{
		return ((long) ((a / (a + b)) * 10000) / 100.);
	}

	/**
	 * Rounds the result of a / b to a precision of two after digit numbers.
	 *
	 * @param a
	 *            value a
	 * @param b
	 *            value b
	 * @return xx.xx
	 */
	public static double percentDiv(final double a, final double b)
	{
		return ((long) ((a / b) * 10000) / 100.);
	}

	/**
	 * Returns the result of (a / b) as a percentage string
	 *
	 * @param a
	 *            value a
	 * @param b
	 *            value b
	 * @return xx.xx%
	 */
	public static String percentFrom(final double a, final double b)
	{

		double bVal = b;

		if (bVal == 0.) {
			bVal = 1.;
		}

		StringBuilder rep = new StringBuilder();
		double d = ((long) ((a / bVal) * 10000) / 100.);
		if (d < 10.0) {
			rep.append('0');
		}

		rep.append(d);
		while (rep.length() < 5) {
			rep.append('0');
		}

		return rep + "%";
	}
}
