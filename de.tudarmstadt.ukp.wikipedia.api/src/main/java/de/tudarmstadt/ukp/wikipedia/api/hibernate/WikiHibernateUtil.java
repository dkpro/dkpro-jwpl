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
package de.tudarmstadt.ukp.wikipedia.api.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
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
        	Configuration configuration = getConfiguration(config);
            StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
            SessionFactory sessionFactory = configuration.buildSessionFactory(ssrb.build());
            sessionFactoryMap.put(uniqueSessionKey, sessionFactory);
        }
        return sessionFactoryMap.get(uniqueSessionKey);
    }


    private static Properties getProperties(DatabaseConfiguration config) {
        String user     = config.getUser();
        String password = config.getPassword();

        /*
         * Ensures explicit DMBS type specific configuration for hsqldb from junit tests context
         */
        String jdbcURL  = config.getJdbcURL();
        String databaseDriverClass = config.getDatabaseDriver();

        Properties p = new Properties();
        boolean useMySQL = false;
        boolean useHSQL = false;
        // XXX other dialects might be interesting here as well...
        if(jdbcURL.toLowerCase().contains("mysql")) {
            useMySQL = true;
        }
        else if(jdbcURL.toLowerCase().contains("hsql")) {
            useHSQL = true;
        }

        // SQL dialect
        if(useMySQL) {
            p.setProperty("hibernate.dialect","org.hibernate.dialect.MySQLDialect");
        }
        if(useHSQL) {
            p.setProperty("hibernate.dialect","org.hibernate.dialect.HSQLDialect");
        }

        // Database connection settings
        p.setProperty("hibernate.connection.driver_class", databaseDriverClass);
        p.setProperty("hibernate.connection.url", jdbcURL);
        /*
         *  Needed to ensure working hsqldb queries - don't remove it...!
         */
        p.setProperty("hibernate.connection.useUnicode","true");
        p.setProperty("hibernate.connection.characterEncoding", "UTF-8");

        p.setProperty("hibernate.connection.username", user);
        p.setProperty("hibernate.connection.password", password);

        // JDBC connection pool (use the built-in) -->
        p.setProperty("hibernate.connection.pool_size","1");

        // Enable Hibernate's automatic session context management
        p.setProperty("hibernate.current_session_context_class","thread");

        // Disable the second-level cache
        p.setProperty("hibernate.cache.provider_class","org.hibernate.cache.NoCacheProvider");

        // Echo all executed SQL to stdout
        p.setProperty("hibernate.show_sql","false");

        // Do only update schema on changes
        if(useMySQL) {
            p.setProperty("hibernate.hbm2ddl.auto","validate");
        }
        if(useHSQL) {
            p.setProperty("hibernate.hbm2ddl.auto","none");
        }

        // Avoid long running connection acquisition:
        // Important performance fix to obtain jdbc connections a lot faster by avoiding metadata fetching
        p.setProperty("hibernate.temp.use_jdbc_metadata_defaults","false");

        if(useMySQL) {
            // Set C3P0 Connection Pool in case somebody wants to use it in production settings
            // if no C3P0 is available at runtime, related warnings can be ignored safely as the built-in CP will be used.
            p.setProperty("hibernate.c3p0.acquire_increment","3");
            p.setProperty("hibernate.c3p0.idle_test_period","300");
            p.setProperty("hibernate.c3p0.min_size","3");
            p.setProperty("hibernate.c3p0.max_size","15");
            p.setProperty("hibernate.c3p0.max_statements","100");
            p.setProperty("hibernate.c3p0.timeout","1000");

        }
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
