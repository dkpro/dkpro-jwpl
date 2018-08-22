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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.*;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

public class PerformanceIT implements WikiConstants {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Wikipedia wiki;
    private static int retrievedNumberOfPages;

    // The system under test
    private static PerformanceTest pt;

    @BeforeClass
    public static void setupWikipedia() throws WikiApiException {
        Properties configuration = loadConfiguration();
        retrievedNumberOfPages = Integer.parseInt(configuration.getProperty("performance.pages.retrieved"));
        DatabaseConfiguration dbConfig = obtainITDBConfiguration(configuration);
        wiki = new Wikipedia(dbConfig);
        int maxiCycles = Integer.parseInt(configuration.getProperty("performance.cycles.maxi"));
        int pageCycles = Integer.parseInt(configuration.getProperty("performance.cycles.page"));
        pt = new PerformanceTest(wiki, maxiCycles, pageCycles);
    }

    private static DatabaseConfiguration obtainITDBConfiguration(Properties configuration) {
        String name = configuration.getProperty("database.name");
        String host = configuration.getProperty("database.host");
        String user = configuration.getProperty("database.user");
        String password = configuration.getProperty("database.password");
        // String host, String database, String user, String password, Language language
        return new DatabaseConfiguration(host, name, user, password, Language.english);
    }

    private static Properties loadConfiguration() {
        Properties dbConfigProperties = new Properties();
        InputStream resourceStream = checkResourceExists("jwpl-env-configuration.properties");
        if(resourceStream == null) {
            throw new RuntimeException("Can't find JWPL IT DB configuration in the classpath!");
        }
        else {
            try (BufferedInputStream stream = new BufferedInputStream(resourceStream)) {
                dbConfigProperties.load(stream);
            } catch(IOException e) {
                logger.error(e.getLocalizedMessage(), e);
                throw new RuntimeException("Can't load JWPL IT DB configuration!");
            }
            return dbConfigProperties;
        }
    }

    private static InputStream checkResourceExists(String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
    }

    @Before
    public void setup() throws WikiApiException {

    }

    @Test
    public void testPerformanceLoadPagesIntern() throws WikiApiException {
        logger.debug("intern page loading");
        pt.loadPagesTest("intern");

    }

    @Test
    public void testPerformanceLoadPagesExtern() throws WikiApiException {
        logger.debug("extern page loading");
        pt.loadPagesTest("extern");
    }

    @Test
    public void testPerformanceLoadPagesAndAccessFieldsIntern() throws WikiApiException {
        logger.debug("intern page loading and field accessing");
        pt.loadPagesAndAccessFieldsTest("intern");
    }

    @Test
    public void testPerformanceLoadPagesAndAccessFieldsExtern() throws WikiApiException {
        logger.debug("extern page loading and field accessing");
    	pt.loadPagesAndAccessFieldsTest("extern");
    }

    @Test
    public void testPerformancePageIteratorBuffer1() throws WikiApiException {
        logger.debug("Test: retrieve 4000 pages - buffer = '{}' ...", retrievedNumberOfPages, 1);
        pt.loadPageAndIterate(retrievedNumberOfPages, 1, wiki);
    }

    @Test
    public void testPerformancePageIteratorBuffer10() throws WikiApiException {
        logger.debug("Test: retrieve 4000 pages - buffer = '{}' ...", retrievedNumberOfPages, 10);
        pt.loadPageAndIterate(retrievedNumberOfPages, 10, wiki);
    }

    @Test
    public void testPerformancePageIteratorBuffer50() throws WikiApiException {
        logger.debug("Test: retrieve 4000 pages - buffer = '{}' ...", retrievedNumberOfPages, 50);
        pt.loadPageAndIterate(retrievedNumberOfPages, 50, wiki);
    }

    @Test
    public void testPerformancePageIteratorBuffer100() throws WikiApiException {
        logger.debug("Test: retrieve 4000 pages - buffer = '{}' ...", retrievedNumberOfPages, 100);
        pt.loadPageAndIterate(retrievedNumberOfPages, 100, wiki);
    }

    @Test
    public void testPerformancePageIteratorBuffer200() throws WikiApiException {
        logger.debug("Test: retrieve 4000 pages - buffer = '{}' ...", retrievedNumberOfPages, 200);
        pt.loadPageAndIterate(retrievedNumberOfPages, 200, wiki);
    }

    @Test
    public void testPerformancePageIteratorBuffer500() throws WikiApiException {
        logger.debug("Test: retrieve 4000 pages - buffer = '{}' ...", retrievedNumberOfPages, 500);
        pt.loadPageAndIterate(retrievedNumberOfPages, 500, wiki);
    }

}
