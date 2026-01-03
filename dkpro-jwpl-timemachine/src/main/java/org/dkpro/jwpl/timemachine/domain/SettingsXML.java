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
package org.dkpro.jwpl.timemachine.domain;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.domain.Configuration;
import org.dkpro.jwpl.wikimachine.util.TimestampUtil;

/**
 * This is a utility class that generates a template for the configuration file<br>
 * The template must be edited prior to be used for the DBMapping tool.<br>
 */
public class SettingsXML
{

    public static final String OUTPUT_DIRECTORY = "outputDirectory";
    public static final String CATEGORY_LINKS_FILE = "categoryLinksFile";
    public static final String PAGE_LINKS_FILE = "pageLinksFile";
    public static final String META_HISTORY_FILE = "metaHistoryFile";
    public static final String EACH = "each";
    public static final String TO_TIMESTAMP = "toTimestamp";
    public static final String FROM_TIMESTAMP = "fromTimestamp";
    public static final String DISAMBIGUATION_CATEGORY = "disambiguationCategory";
    public static final String MAIN_CATEGORY = "mainCategory";
    public static final String LANGUAGE = "language";

    private static final String DESCRIPTION = "This a configuration formular for the DBMapping Tool of the JWPL";
    private static final String PLACEHOLDER = "to be edited";

    public static void generateSample(String outputFileName) throws IOException
    {
        final Properties p = new Properties();
        p.put(LANGUAGE, PLACEHOLDER);
        p.put(MAIN_CATEGORY, PLACEHOLDER);
        p.put(DISAMBIGUATION_CATEGORY, PLACEHOLDER);
        p.put(FROM_TIMESTAMP, PLACEHOLDER);
        p.put(TO_TIMESTAMP, PLACEHOLDER);
        p.put(EACH, PLACEHOLDER);
        p.put(META_HISTORY_FILE, PLACEHOLDER);
        p.put(PAGE_LINKS_FILE, PLACEHOLDER);
        p.put(CATEGORY_LINKS_FILE, PLACEHOLDER);
        p.put(OUTPUT_DIRECTORY, PLACEHOLDER);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFileName))) {
            p.storeToXML(os, DESCRIPTION);
        }
    }

    public static Configuration loadConfiguration(String configFile, ILogger logger)
    {
        final Properties properties = new Properties();
        Configuration result;
        try (InputStream in = new BufferedInputStream(new FileInputStream(configFile))) {
            properties.loadFromXML(in);
            result = new Configuration(logger);
            result.setLanguage(properties.get(LANGUAGE).toString());
            result.setMainCategory(properties.get(MAIN_CATEGORY).toString());
            result.setDisambiguationCategory(properties.get(DISAMBIGUATION_CATEGORY).toString());
            result.setFromTimestamp(TimestampUtil.parse(properties.get(FROM_TIMESTAMP).toString()));
            result.setToTimestamp(TimestampUtil.parse(properties.get(TO_TIMESTAMP).toString()));
            result.setEach(Integer.parseInt(properties.get(EACH).toString()));
        } catch (IOException ioe) {
            logger.log("Could not find config file " + configFile);
            result = null;
        } catch (NumberFormatException nfe) {
            logger.log("Could not read 'each' parameter - check the config file!");
            result = null;
        }
        return result;
    }

    public static TimeMachineFiles loadFiles(String configFile, ILogger logger)
    {
        final Properties properties = new Properties();
        TimeMachineFiles result;
        try (InputStream in = new BufferedInputStream(new FileInputStream(configFile))) {
            properties.loadFromXML(in);
            result = new TimeMachineFiles(logger);
            result.setMetaHistoryFile(properties.get(META_HISTORY_FILE).toString());
            result.setPageLinksFile(properties.get(PAGE_LINKS_FILE).toString());
            result.setCategoryLinksFile(properties.get(CATEGORY_LINKS_FILE).toString());
            result.setOutputDirectory(properties.get(OUTPUT_DIRECTORY).toString());
        }
        catch (IOException e) {
            logger.log("Could not find config file " + configFile);
            result = null;
        }
        return result;
    }

    public static void main(String[] args)
    {
        if (args.length > 0) {
            try {
                generateSample(args[0]);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
