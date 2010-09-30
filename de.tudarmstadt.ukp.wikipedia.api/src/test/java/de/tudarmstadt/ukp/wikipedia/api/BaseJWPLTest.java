package de.tudarmstadt.ukp.wikipedia.api;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

/**
 * Simple test base class to inject the same hsqldb test context into every test
 * class to avoid duplicated code and efforts. Also shutsdown the
 * hibernate/hsqldb context properly.
 * 
 * @author mwiesner, wiesner@hs-heilbronn.de
 * 
 */
public abstract class BaseJWPLTest {

	protected Wikipedia wiki;

	protected final DatabaseConfiguration obtainHSDLDBConfiguration() {
		DatabaseConfiguration db = new DatabaseConfiguration();
		db.setDatabase("wikiapi_test");
		db.setHost("localhost");
		db.setUser("sa");
		db.setPassword("");
		db.setLanguage(Language._test);
		db.setJdbcURL("jdbc:hsqldb:file:./db/wikiapi_test");
		db.setDatabaseDriver("org.hsqldb.jdbcDriver");
		return db;
	}
}
