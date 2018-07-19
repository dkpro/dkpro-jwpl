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
import java.io.StreamTokenizer;

/**
 * A Parser for the sql file that defines the table pagelinks.
 *
 * A fix for Issue 102 has been provided by Google Code user astronautguo
 *
 *
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
		 */
	public PagelinksParser(InputStream inputStream) throws IOException {
		init(inputStream);
	}

	@Override
	public boolean next() throws IOException {
		if (EOF_reached) {
			return false;
		}
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
		plTo = SQLEscape.escape(st.sval);
		// pre July 2014 dumpo: read ')' / post July 2014 dump read ','
		st.nextToken();
		if(st.toString().substring(7, 8).equals(",")){
			//we have a post July 2014 dump and thus have to skip the pl_from_namespace field
			st.nextToken(); // skip pl_from_namespace value
			st.nextToken(); // skip ')'			
		}
		// read ',' or ';'. If ';' is found then skip statement or espect eof.
		st.nextToken();
		if (st.toString().substring(7, 8).equals(";")) {
			skipStatements();
		}
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
