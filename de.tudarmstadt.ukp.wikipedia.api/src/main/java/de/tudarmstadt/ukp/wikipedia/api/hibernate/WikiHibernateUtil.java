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
package de.tudarmstadt.ukp.wikipedia.api.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;

public class WikiHibernateUtil implements WikiConstants {

    private static Map<String, SessionFactory> sessionFactoryMap = new HashMap<String, SessionFactory>();

    public static SessionFactory getSessionFactory(DatabaseConfiguration config) {

        if (config.getLanguage() == null) {
            throw new ExceptionInInitializerError("Database configuration error. 'Language' is empty.");
        }
        else if (config.getHost() == null) {
            throw new ExceptionInInitializerError("Database configuration error. 'Host' is empty.");
        }
        else if (config.getDatabase() == null) {
            throw new ExceptionInInitializerError("Database configuration error. 'Database' is empty.");
        }


        String uniqueSessionKey = config.getLanguage().toString() + config.getHost() + config.getDatabase();
        if (!sessionFactoryMap.containsKey(uniqueSessionKey)) {
            SessionFactory sessionFactory = getConfiguration(config).buildSessionFactory();
            sessionFactoryMap.put(uniqueSessionKey, sessionFactory);
        }
        return sessionFactoryMap.get(uniqueSessionKey);
    }


    private static Properties getProperties(DatabaseConfiguration config) {
        String host     = config.getHost();
        String db       = config.getDatabase();
        String user     = config.getUser();
        String password = config.getPassword();

        Properties p = new Properties();

        // Database connection settings
        p.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        p.setProperty("hibernate.connection.url", "jdbc:mysql://" + host + "/" + db);

        p.setProperty("hibernate.connection.characterEncoding", "UTF-8");

        p.setProperty("hibernate.connection.useUnicode","true");
        p.setProperty("hibernate.connection.username", user);
        p.setProperty("hibernate.connection.password", password);

        // SQL dialect
        p.setProperty("hibernate.dialect","org.hibernate.dialect.MySQLDialect");

        // Enable Hibernate's automatic session context management
        p.setProperty("hibernate.current_session_context_class","thread");

        // Disable the second-level cache
        p.setProperty("hibernate.cache.provider_class","org.hibernate.cache.NoCacheProvider");

        // Echo all executed SQL to stdout
        p.setProperty("hibernate.show_sql","false");

        // Update schema
        p.setProperty("hibernate.hbm2ddl.auto","update");

        //Configure Connection Pool
        p.setProperty("hibernate.c3p0.acquire_increment","3");
        p.setProperty("hibernate.c3p0.idle_test_period","300");
        p.setProperty("hibernate.c3p0.min_size","3");
        p.setProperty("hibernate.c3p0.max_size","15");
        p.setProperty("hibernate.c3p0.max_statements","10");
        p.setProperty("hibernate.c3p0.timeout","1000");

        return p;
    }

    private static Configuration getConfiguration(DatabaseConfiguration config) {
        Configuration cfg = new Configuration()
        .addClass(Category.class)
        .addClass(MetaData.class)
        .addClass(Page.class)
        .addClass(PageMapLine.class)
//        .addClass(RelatednessCacheLine.class)
        .addProperties(getProperties(config));
        return cfg;
    }

}
