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

        // JDBC connection pool (use the built-in) -->
        p.setProperty("hibernate.connection.pool_size","1");

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

//    private static SessionFactory sessionFactoryCzech;
//    private static SessionFactory sessionFactoryGerman;
//    private static SessionFactory sessionFactoryUkrainian;
//    private static SessionFactory sessionFactoryEnglish;
//
//    public static SessionFactory getSessionFactory(Language language, DatabaseConfiguration config) {
//        if (language.equals(Language.czech)) {
//            if (sessionFactoryCzech == null) {
//                try {
////                    sessionFactoryCzech = new Configuration().configure("org/tud/sir/wiki/hibernate/config/wiki_cs.cfg.xml").buildSessionFactory();
//                    sessionFactoryCzech = getConfiguration(config).buildSessionFactory();
//                } catch (Throwable ex) {
//                    System.err.println("Initial SessionFactory creation failed." + ex);
//                    throw new ExceptionInInitializerError(ex);
//                }
//            }
//            return sessionFactoryCzech;
//        }
//        else if (language.equals(Language.english)) {
//            if (sessionFactoryEnglish == null) {
//                try {
////                    sessionFactoryEnglish = new Configuration().configure("org/tud/sir/wiki/hibernate/config/wiki_en.cfg.xml").buildSessionFactory();
//                    sessionFactoryEnglish = getConfiguration(config).buildSessionFactory();
//                } catch (Throwable ex) {
//                    System.err.println("Initial SessionFactory creation failed." + ex);
//                    throw new ExceptionInInitializerError(ex);
//                }
//            }
//            return sessionFactoryEnglish;
//        }
//        else if (language.equals(Language.german)) {
//            if (sessionFactoryGerman == null) {
//                try {
////                    sessionFactoryGerman = new Configuration().configure("org/tud/sir/wiki/hibernate/config/wiki_de.cfg.xml").buildSessionFactory();
//                    sessionFactoryGerman = getConfiguration(config).buildSessionFactory();
//                } catch (Throwable ex) {
//                    System.err.println("Initial SessionFactory creation failed." + ex);
//                    throw new ExceptionInInitializerError(ex);
//                }
//            }
//            return sessionFactoryGerman;
//        }
//        else if (language.equals(Language.ukrainian)) {
//            if (sessionFactoryUkrainian == null) {
//                try {
////                    sessionFactoryUkrainian = new Configuration().configure("org/tud/sir/wiki/hibernate/config/wiki_uk.cfg.xml").buildSessionFactory();
//                    sessionFactoryUkrainian = getConfiguration(config).buildSessionFactory();
//                } catch (Throwable ex) {
//                    System.err.println("Initial SessionFactory creation failed." + ex);
//                    throw new ExceptionInInitializerError(ex);
//                }
//            }
//            return sessionFactoryUkrainian;
//        }
//        else {
//            throw new ExceptionInInitializerError("Could not get session factory. Unknwon language " + language);
//        }
//    }
//
//
//    private static Properties getProperties(DatabaseConfiguration config) {
//        String host     = config.getHost();
//        String db       = config.getDatabase();
//        String user     = config.getUser();
//        String password = config.getPassword();
//
//        Properties p = new Properties();
//
//        // Database connection settings
//        p.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
//        p.setProperty("hibernate.connection.url", "jdbc:mysql://" + host + "/" + db);
//        p.setProperty("hibernate.connection.characterEncoding", "UTF-8");
//        p.setProperty("hibernate.connection.hibernate.connection.useUnicode","true");
//        p.setProperty("hibernate.connection.username", user);
//        p.setProperty("hibernate.connection.password", password);
//
//        // JDBC connection pool (use the built-in) -->
//        p.setProperty("hibernate.connection.pool_size","1");
//
//        // SQL dialect
//        p.setProperty("hibernate.dialect","org.hibernate.dialect.MySQLDialect");
//
//        // Enable Hibernate's automatic session context management
//        p.setProperty("hibernate.current_session_context_class","thread");
//
//        // Disable the second-level cache
//        p.setProperty("hibernate.cache.provider_class","org.hibernate.cache.NoCacheProvider");
//
//        // Echo all executed SQL to stdout
//        p.setProperty("hibernate.show_sql","false");
//
//        // Do only update schema on changes
//        p.setProperty("hibernate.hbm2ddl.auto","update");
//
//        return p;
//    }
//
//    private static Configuration getConfiguration(DatabaseConfiguration config) {
//        Configuration cfg = new Configuration()
//        .addClass(Category.class)
//        .addClass(MetaData.class)
//        .addClass(Page.class)
//        .addClass(PageMapLine.class)
//        .addClass(RelatednessCacheLine.class)
//        .addProperties(getProperties(config));
//        return cfg;
//    }

//    private static final SessionFactory sessionFactoryCzech;
//    private static final SessionFactory sessionFactoryGerman;
//    private static final SessionFactory sessionFactoryUkrainian;
//    private static final SessionFactory sessionFactoryEnglish;
//
//    static {
//        try {
//            sessionFactoryCzech     = new Configuration().configure("org/tud/sir/wiki/hibernate/config/wiki_cs.cfg.xml").buildSessionFactory();
//            sessionFactoryEnglish   = new Configuration().configure("org/tud/sir/wiki/hibernate/config/wiki_en.cfg.xml").buildSessionFactory();
//            sessionFactoryGerman    = new Configuration().configure("org/tud/sir/wiki/hibernate/config/wiki_de.cfg.xml").buildSessionFactory();
//            sessionFactoryUkrainian = new Configuration().configure("org/tud/sir/wiki/hibernate/config/wiki_uk.cfg.xml").buildSessionFactory();
//        } catch (Throwable ex) {
//            System.err.println("Initial SessionFactory creation failed." + ex);
//            throw new ExceptionInInitializerError(ex);
//        }
//    }
//
//    public static SessionFactory getSessionFactory(Language language) {
//        if (language.equals(Language.czech)) {
//            return sessionFactoryCzech;
//        }
//        else if (language.equals(Language.english)) {
//            return sessionFactoryEnglish;
//        }
//        else if (language.equals(Language.german)) {
//            return sessionFactoryGerman;
//        }
//        else if (language.equals(Language.ukrainian)) {
//            return sessionFactoryUkrainian;
//        }
//        else {
//            throw new ExceptionInInitializerError("Could not get session factory. Unknwon language " + language);
//        }
//    }
}
