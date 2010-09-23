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
package de.tudarmstadt.ukp.wikipedia.api;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

/**
 * A database configuration is used to establish a database connection and set various parameters.
 * @author zesch
 *
 */
public class DatabaseConfiguration {

    private String host;
    private String database;
    private String user;
    private String password;
    private Language language;
    
    public DatabaseConfiguration() {}
    
    public DatabaseConfiguration(String host, String database, String user, String password, Language language) {
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
        this.language = language;
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
}
