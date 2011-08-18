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


import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for Strings.
 * @author Anouar
 * 
 */
public abstract class Strings {

	// nobody could instantiate it
	private Strings() {
	}

	public static boolean find(String str, String regex) {
		return Pattern.compile(regex).matcher(str).find();
	}

	public static boolean lookingAt(String str, String regex) {
		return Pattern.compile(regex).matcher(str).lookingAt();
	}

	public static boolean matches(String str, String regex) {
		return Pattern.compile(regex).matcher(str).matches();
	}
	public static boolean endsWithRegex(String str, String regex) {
		return matches(str, "(.)*" + regex);
	}
	@SuppressWarnings("unchecked")
	public static String join(Collection c, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			buffer.append(iter.next());
			if (iter.hasNext()) {
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}
	
	/**
	 * Uppercases the first character of a string.
	 * 
	 * @param s
	 *            a string to capitalize
	 * @return a capitalized version of the string
	 */
	public static String capitalize(String s) {
		if (Character.isLowerCase(s.charAt(0))) {
			return Character.toUpperCase(s.charAt(0)) + s.substring(1);
		} else {
			return s;
		}
	}

	public static boolean isEmpty(String s) {
		return s.length() == 0;
	}
	public static boolean isCapitalized(String s) {
		return Character.isUpperCase(s.charAt(0));
	}
	public static String searchAndReplace(String text, String from_regex,
			String to) {
		from_regex = escapeString(from_regex, new char[]{'.', '[', ']', '\\'},
				'\\'); // special chars in regex
		Pattern p = Pattern.compile(from_regex);
		Matcher m = p.matcher(text);
		return m.replaceAll(to);
	}
	public static String escapeString(String s, char[] charsToEscape,
			char escapeChar) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == escapeChar) {
				result.append(escapeChar);
			} else {
				for (int j = 0; j < charsToEscape.length; j++) {
					if (c == charsToEscape[j]) {
						result.append(escapeChar);
						break;
					}
				}
			}
			result.append(c);
		}
		return result.toString();
	}
	public static String toAscii(String s) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > 127) {
				String result = "?";
				if (c >= 0x00c0 && c <= 0x00c5) {
					result = "A";
				} else if (c == 0x00c6) {
					result = "AE";
				} else if (c == 0x00c7) {
					result = "C";
				} else if (c >= 0x00c8 && c <= 0x00cb) {
					result = "E";
				} else if (c >= 0x00cc && c <= 0x00cf) {
					result = "F";
				} else if (c == 0x00d0) {
					result = "D";
				} else if (c == 0x00d1) {
					result = "N";
				} else if (c >= 0x00d2 && c <= 0x00d6) {
					result = "O";
				} else if (c == 0x00d7) {
					result = "x";
				} else if (c == 0x00d8) {
					result = "O";
				} else if (c >= 0x00d9 && c <= 0x00dc) {
					result = "U";
				} else if (c == 0x00dd) {
					result = "Y";
				} else if (c >= 0x00e0 && c <= 0x00e5) {
					result = "a";
				} else if (c == 0x00e6) {
					result = "ae";
				} else if (c == 0x00e7) {
					result = "c";
				} else if (c >= 0x00e8 && c <= 0x00eb) {
					result = "e";
				} else if (c >= 0x00ec && c <= 0x00ef) {
					result = "i";
				} else if (c == 0x00f1) {
					result = "n";
				} else if (c >= 0x00f2 && c <= 0x00f8) {
					result = "o";
				} else if (c >= 0x00f9 && c <= 0x00fc) {
					result = "u";
				} else if (c >= 0x00fd && c <= 0x00ff) {
					result = "y";
				} else if (c >= 0x2018 && c <= 0x2019) {
					result = "\'";
				} else if (c >= 0x201c && c <= 0x201e) {
					result = "\"";
				} else if (c >= 0x0213 && c <= 0x2014) {
					result = "-";
				} else if (c >= 0x00A2 && c <= 0x00A5) {
					result = "$";
				} else if (c == 0x2026) {
					result = ".";
				}
				b.append(result);
			} else {
				b.append(c);
			}
		}
		return b.toString();
	}

	public static String group(String str) {
		return "(" + str + ")";
	}
	public static String repeat(String s, int times) {
		if (times == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < times; i++) {
			sb.append(s);
		}
		return sb.toString();
	}
	public static String join(Object[] elements, String glue) {
		return (join(java.util.Arrays.asList(elements), glue));
	}

}
