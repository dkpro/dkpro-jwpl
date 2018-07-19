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
package de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql;


/**
 * The single method {@link SQLEscape#escape} removes all unwished escape
 * characters from a string to make is SQL conform. Maybe not thread-save.
 *
 */
public class SQLEscape {
	private SQLEscape() {

	}

	/**
	 * @see SQLEscape
	 * @param str unescaped String
	 * @return String with with escape characters
	 */
	public static String escape(String str) {
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
			case '\'':
				sql.append('\\').append('\'');
				break;
			case '\"':
				sql.append('\\').append('"');
				break;
			case '\b':
				sql.append('\\').append('b');
				break;
			case '\\':
				sql.append('\\').append('\\');
				break;
//			case '%':
//				sql.append('[').append('%').append(']');
//				break;
//			case '_':
//				sql.append('[').append('_').append(']');
//				break;
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
