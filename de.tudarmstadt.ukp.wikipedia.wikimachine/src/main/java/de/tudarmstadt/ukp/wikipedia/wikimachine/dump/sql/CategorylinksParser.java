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

import java.io.IOException;
import java.io.InputStream;

/**
 * A Parser for the sql file that defines the table categorylinks.
 *
 * A fix for Issue 102 has been provided by Google Code user astronautguo
 *
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

	@Override
    public boolean next() throws IOException {
		if (EOF_reached) {
            return false;
		}
		// read '('
		st.nextToken();
		// read cl_from
		st.nextToken();
		clFrom = (int) st.nval;
		// read ','
		st.nextToken();
		// read cl_to
		st.nextToken();
		clTo = SQLEscape.escape(st.sval);
		// read ','
		st.nextToken();
		// read cl_sortkey
		st.nextToken();
		// read ','
		st.nextToken();
		// read cl_timestamp
		st.nextToken();

		boolean EOE = false;  // end of entry
		while (!EOE) {
	        st.nextToken();
	        // corresponds to closing parenthesis
	        if (st.ttype == 41) {
	            EOE = true;
	        }
		}

		// read ',' or ';'. If ';' is found then skip statement or expect eof.
		st.nextToken();

		if (st.toString().substring(7, 8).equals(";")) {
            skipStatements();
		}
		return true;
	}
}
