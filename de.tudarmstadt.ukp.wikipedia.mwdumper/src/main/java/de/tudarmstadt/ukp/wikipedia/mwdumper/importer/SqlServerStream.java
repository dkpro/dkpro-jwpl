/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.mwdumper.importer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class SqlServerStream implements SqlStream {
	private Connection connection;
	
	public SqlServerStream(Connection conn) {
		connection = conn; // TODO
	}
	
	public void writeComment(CharSequence sql) {
		// do nothing
	}
	
	public void writeStatement(CharSequence sql) throws IOException {
		Statement statement;
		try {
			statement = connection.createStatement();
			statement.setEscapeProcessing(false);
			statement.execute(sql.toString());
		} catch (SQLException e) {
			throw new IOException(e.toString());
		}
	}
	
	public void close() throws IOException {
		try {
			connection.close();
		} catch (SQLWarning e) {
			e.printStackTrace();
		} catch (SQLException e) {
			throw new IOException(e.toString());
		}
	}

}
