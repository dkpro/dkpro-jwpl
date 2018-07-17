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
package de.tudarmstadt.ukp.wikipedia.api;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

/**
 * An instance of {@link DatabaseConfiguration} is used to establish a database connection and set various parameters.
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

    /**
     * A constructor for MySQL backends, i.e. the default production setting.
     *
     * @param host           The hostname the machine the database is hosted on.
     * @param database       The name of the database to connect to.
     * @param user           The username as part of the credentials used for for authentication.
     * @param password       The password as part of the credentials used for for authentication.
     * @param language       The {@link Language} used for the underlying connection.
     */
    public DatabaseConfiguration(String host, String database, String user, String password, Language language) {

        this("com.mysql.jdbc.Driver", "jdbc:mysql://" + host + "/" + database,
                host, database, user, password, language);
    }

    /**
     * A constructor for an explicit DBMS specific configuration, e.g. for HSQLDB in tests contexts.
     *
     * @param databaseDriver The fully qualified name of the JDBC driver.
     * @param jdbcURL        A valid JDBC url used to open connections.
     * @param host           The hostname the machine the database is hosted on.
     * @param database       The name of the database to connect to.
     * @param user           The username as part of the credentials used for for authentication.
     * @param password       The password as part of the credentials used for for authentication.
     * @param language       The {@link Language} used for the underlying connection.
     */
    public DatabaseConfiguration(String databaseDriver, String jdbcURL, String host, String database, String user,
                                 String password, Language language) {
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
        this.language = language;

        this.setDatabaseDriver(databaseDriver);
        this.setJdbcURL(jdbcURL);
    }

    /**
     * @return {@code True} if collation is supported by the database backend, else {@code false}.
     */
    boolean supportsCollation() {
        if(databaseDriver!=null) {
            return databaseDriver.contains("mysql");
        } else {
            return false;
        }
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
