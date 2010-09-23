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
/**
 * @(#)SQLFileParser.java
 */
package de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 * This class defines common utilities for the classes CategorylinksParser <br>
 * and PagelinksParser.
 * 
 * @author Anouar
 * 
 * @version 0.2 <br>
 *          <code>SQLFileParser</code> don't create a BufferedReader by himself
 *          but entrust it to <code>BufferedReaderFactory</code>. Thereby
 *          BufferedReaders are created according to archive type and try to
 *          uncompress the file on the fly. (Ivan Galkin 15.01.2009)
 */
abstract class SQLFileParser {

	private static final String ENCODING = "UTF-8";
	protected InputStream stream;
	protected StreamTokenizer st;
	protected boolean EOF_reached;

	/**
	 * Init the SQLFileParser with the input stream
	 * 
	 * @param inputStream
	 * @throws IOException
	 * 
	 * @author ivan.galkin
	 */
	protected void init(InputStream inputStream) throws IOException {
		stream = inputStream;
		st = new StreamTokenizer(new BufferedReader(new InputStreamReader(
				stream, ENCODING)));

		EOF_reached = false;
		skipStatements();

	}

	/**
	 * Skip the sql statements for table creation and the prefix <br>
	 * INSERT INTO TABLE .... VALUES for values insertion.<br>
	 * Read tokens until the word 'VALUES' is reached or the EOF.
	 * 
	 * @throws IOException
	 * 
	 */
	protected void skipStatements() throws IOException {
		while (true) {
			st.nextToken();
			if (null != st.sval && st.sval.equalsIgnoreCase("VALUES")) {
				// the next token is the begin of a value
				break;
			}
			if (st.ttype == StreamTokenizer.TT_EOF) {
				// the end of the file is reached
				EOF_reached = true;
				break;
			}
		}
	}

	public void close() throws IOException {
		stream.close();
	}

	/**
	 * This method must be implemented by the PagelinksParser and the
	 * CategorylinksParser<br>
	 * classes.
	 * 
	 * @return Returns true if a new value is now available und false otherwise.
	 * @throws IOException
	 */
	abstract boolean next() throws IOException;
}
