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

import java.io.IOException;
import java.io.InputStream;

/**
 * A Parser for the sql file that defines the table categorylinks.
 * 
 * @author Anouar
 * 
 */
public class CategorylinksParser extends SQLFileParser {

	/**
	 * The fields of the table categorylinks.<br>
	 * These fields are updated on each readen value.
	 */
	private int clFrom;
	private String clTo;

	/**
	 * Create a parser from an input stream
	 * 
	 * @param inputStream
	 * @throws IOException
	 * 
	 * @author ivan.galkin
	 */
	public CategorylinksParser(InputStream inputStream) throws IOException {
		init(inputStream);
	}

	/**
	 * @return Returns the cl_from.
	 */
	public int getClFrom() {
		return clFrom;
	}

	/**
	 * @return Returns the cl_to.
	 */
	public String getClTo() {
		return clTo;
	}

	public boolean next() throws IOException {
		if (EOF_reached)
			return false;
		// read '('
		st.nextToken();
		// read cl_from
		st.nextToken();
		clFrom = (int) st.nval;
		// read ','
		st.nextToken();
		// read cl_to
		st.nextToken();
		clTo = st.sval;
		// read ','
		st.nextToken();
		// read cl_sortkey
		st.nextToken();
		// read ','
		st.nextToken();
		// read cl_timestamp
		st.nextToken();
		// read ')'
		st.nextToken();
		// read ',' or ';'. If ';' is found then skip statement or espect eof.
		st.nextToken();
		if (st.toString().substring(7, 8).equals(";"))
			skipStatements();
		return true;
	}
}
