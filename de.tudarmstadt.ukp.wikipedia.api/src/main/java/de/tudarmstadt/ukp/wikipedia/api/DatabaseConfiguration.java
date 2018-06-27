/*******************************************************************************
 * Copyright 2017
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
package de.tudarmstadt.ukp.wikipedia.api;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

/**
 * A database configuration is used to establish a database connection and set various parameters.
 *
 */
public class DatabaseConfiguration {

    private String host;
    private String database;
    private String user;
    private String password;
    private Language language;
	private String jdbcURL;
	private String databaseDriver;

    public DatabaseConfiguration() {}

    public DatabaseConfiguration(String host, String database, String user, String password, Language language) {
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
        this.language = language;


        // static mysql usecase - default by revision 49
        this.setDatabaseDriver("com.mysql.jdbc.Driver");
        this.setJdbcURL("jdbc:mysql://" + host + "/" + database);
    }

    /**
     * Allows explicit DMBS type specific configuration for hsqldb from junit tests context
     */
    public DatabaseConfiguration(String databaseDriver, String jdbcURL, String host, String database, String user, String password, Language language) {
    	this(host, database, user, password, language);
        this.setDatabaseDriver(databaseDriver);
        this.setJdbcURL(jdbcURL);
    }

    /**
     * @param database The name of the database.
     */
    public void setDatabase(String database) {
        this.database = database;
    }
    /**
     * @param host The host where the database is running. Set to "localhost", if the database is running locally.
     */
    public void setHost(String host) {
        this.host = host;
    }
    /**
     * @param password The password to access the database.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /**
     * @param user The database user.
     */
    public void setUser(String user) {
        this.user = user;
    }
    /**
     * @param language The language of the Wikipedia data.
     */
    public void setLanguage(Language language) {
        this.language = language;
    }


    /**
     * @return The name of the database.
     */
    public String getDatabase() {
        return database;
    }
    /**
     * @return The host where the database is running.
     */
    public String getHost() {
        return host;
    }
    /**
     * @return The password to access the database.
     */
    public String getPassword() {
        return password;
    }
    /**
     * @return The database user.
     */
    public String getUser() {
        return user;
    }
    /**
     * @return The language of the Wikipedia data.
     */
    public Language getLanguage() {
        return language;
    }

	/**
	 * @param databaseDriver the databaseDriver to set
	 */
	public void setDatabaseDriver(String databaseDriver) {
		this.databaseDriver = databaseDriver;
	}

	/**
	 * @return the databaseDriver
	 */
	public String getDatabaseDriver() {
		return databaseDriver;
	}

	/**
	 * @param jdbcURL the jdbcURL to set
	 */
	public void setJdbcURL(String jdbcURL) {
		this.jdbcURL = jdbcURL;
	}

	/**
	 * @return the jdbcURL
	 */
	public String getJdbcURL() {
		return jdbcURL;
	}
}
