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
package de.tudarmstadt.ukp.wikipedia.parser;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

/**
 * Simple test base class to inject the same hsqldb test context into every test
 * class to avoid duplicated code and efforts. Also shuts down the
 * hibernate/hsqldb context properly.
 * 
 * @author mawiesne
 */
public abstract class BaseJWPLTest {

	protected static Wikipedia wiki;

	protected static final DatabaseConfiguration obtainHSQLDBConfiguration() {
		DatabaseConfiguration db = new DatabaseConfiguration();
		db.setDatabase("wikiapi_test");
		db.setHost("localhost");
		db.setUser("sa");
		db.setPassword("");
		db.setLanguage(Language._test);
		db.setJdbcURL("jdbc:hsqldb:file:./src/test/resources/db/wikiapi_test");
		db.setDatabaseDriver("org.hsqldb.jdbcDriver");
		return db;
	}
}
