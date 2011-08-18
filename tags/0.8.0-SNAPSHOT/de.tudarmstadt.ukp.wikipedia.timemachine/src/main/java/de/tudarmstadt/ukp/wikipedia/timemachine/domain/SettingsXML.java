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
/**
 * @(#)ConfigFormularGenerator.java
 */
package de.tudarmstadt.ukp.wikipedia.timemachine.domain;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.Configuration;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.TimestampUtil;

/**
 * This is a utility class that generates a template for the configuration file<br>
 * The template must be edited prior to be used for the DBMapping tool.<br>
 * 
 * @author Anouar
 * 
 */
public class SettingsXML {

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

	/**
	 * @param args
	 * @throws IOException
	 * @throws
	 */
	public static void generateSample(String outputFileName) throws IOException {

		Properties p = new Properties();
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
		p.storeToXML(new FileOutputStream(outputFileName), DESCRIPTION);

	}

	public static Configuration loadConfiguration(String configFile,
			ILogger logger) {

		Configuration result;
		try {
			result = new Configuration(logger);
			Properties properties = new Properties();
			properties.loadFromXML(new FileInputStream(configFile));

			result.setLanguage(properties.get(LANGUAGE).toString());
			result.setMainCategory(properties.get(MAIN_CATEGORY).toString());
			result.setDisambiguationCategory(properties.get(
					DISAMBIGUATION_CATEGORY).toString());
			result.setFromTimestamp(TimestampUtil.parse(properties.get(
					FROM_TIMESTAMP).toString()));
			result.setToTimestamp(TimestampUtil.parse(properties.get(
					TO_TIMESTAMP).toString()));
			result.setEach(Integer.parseInt(properties.get(EACH).toString()));
		} catch (Exception e) {
			result = null;
		}
		return result;
	}

	public static TimeMachineFiles loadFiles(String configFile, ILogger logger) {
		TimeMachineFiles result;
		try {
			Properties properties = new Properties();
			properties.loadFromXML(new FileInputStream(configFile));
			result = new TimeMachineFiles(logger);

			result.setMetaHistoryFile(properties.get(META_HISTORY_FILE)
					.toString());
			result.setPageLinksFile(properties.get(PAGE_LINKS_FILE).toString());
			result.setCategoryLinksFile(properties.get(CATEGORY_LINKS_FILE)
					.toString());
			result.setOutputDirectory(properties.get(OUTPUT_DIRECTORY)
					.toString());
		} catch (Exception e) {
			logger.log("Could not load config file " + configFile);
			result = null;
		}
		return result;
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			try {
				generateSample(args[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
