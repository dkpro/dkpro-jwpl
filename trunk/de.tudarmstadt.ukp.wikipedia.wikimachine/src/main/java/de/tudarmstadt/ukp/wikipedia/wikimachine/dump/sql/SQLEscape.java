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
package de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql;

/**
 * The single method {@link SQLEscape#removeEscapes} removes all unwished escape
 * characters from a string to make is SQL conform. Maybe not thread-save.
 * 
 * @author ivan.galkin
 */
public class SQLEscape {
	private SQLEscape() {

	}

	/**
	 * @see SQLEscape
	 * @param str
	 *            String with with escape characters
	 * @return String with with escape characters
	 */
	public static String removeEscapes(String str) {
		final int len = str.length();

		// maybe the StringBuffer would be safer?
		StringBuilder sql = new StringBuilder(len * 2);

		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			switch (c) {
			case '\u0000':
				sql.append('\\').append('0');
				break;
			case '\n':
				sql.append('\\').append('n');
				break;
			case '\t':
				sql.append('\\').append('t');
				break;
			case '\r':
				sql.append('\\').append('r');
				break;
			case '\u001a':
				sql.append('\\').append('Z');
				break;
			default:
				sql.append(c);
				break;
			}
		}
		return sql.toString();
	}

	public static String titleFormat(String title) {
		return title.replace(' ', '_');
	}
}
