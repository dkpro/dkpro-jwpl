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
package de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;

/**
 * A Parser for the sql file that defines the table pagelinks.
 * 
 * @author Anouar
 * 
 */
public class PagelinksParser extends SQLFileParser {

	/**
	 * The fields of the table pagelinks.<br>
	 * These fields are updated on each readen value.
	 */
	private int plFrom;
	private int plNamespace;
	private String plTo;

	/**
	 * Create a parser from an input stream
	 * 
	 * @param inputStream
	 * @throws IOException
	 * @author ivan.galkin
	 */
	public PagelinksParser(InputStream inputStream) throws IOException {
		init(inputStream);
	}

	public boolean next() throws IOException {
		if (EOF_reached)
			return false;
		// read '('
		st.nextToken();
		if (st.ttype == StreamTokenizer.TT_EOF) {
			// the end of the file is reached
			EOF_reached = true;
			return false;
		}
		// read pl_from
		st.nextToken();
		plFrom = (int) st.nval;
		// read ','
		st.nextToken();
		// read pl_namespace
		st.nextToken();
		plNamespace = (int) st.nval;
		// read ','
		st.nextToken();
		// read pl_to
		st.nextToken();
		plTo = st.sval;
		// read ')'
		st.nextToken();
		// read ',' or ';'. If ';' is found then skip statement or espect eof.
		st.nextToken();
		if (st.toString().substring(7, 8).equals(";"))
			skipStatements();
		return true;
	}

	/**
	 * @return Returns the pl_from.
	 */
	public int getPlFrom() {
		return plFrom;
	}

	/**
	 * @return Returns the pl_namespace.
	 */
	public int getPlNamespace() {
		return plNamespace;
	}

	/**
	 * @return Returns the pl_to.
	 */
	public String getPlTo() {
		return plTo;
	}

}
