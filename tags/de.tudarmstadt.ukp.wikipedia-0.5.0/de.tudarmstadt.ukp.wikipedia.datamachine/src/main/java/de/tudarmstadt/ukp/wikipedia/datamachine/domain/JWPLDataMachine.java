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
package de.tudarmstadt.ukp.wikipedia.datamachine.domain;

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.Configuration;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.ISnapshotGenerator;
import de.tudarmstadt.ukp.wikipedia.wikimachine.factory.IEnvironmentFactory;
import de.tudarmstadt.ukp.wikipedia.wikimachine.factory.SpringFactory;

/**
 * Starts the transformation from Mediawiki dump format to JWPL dump format.
 */
public class JWPLDataMachine {

	private static final int LANG_ARG = 0;
	private static final int MAINCATEGORY_ARG = 1;
	private static final int DISAMBIGUATION_ARG = 2;
	private static final int DATADIR_ARG = 3;

	private static final String USAGE = "Please use\n"
			+ "\tjava -jar JWPLDataMachine.jar <LANGUAGE> <TOP_CATEGORY_NAME> <DISAMBIGUATION_CATEGORY_NAME> <SOURCE_DIRECTORY>\n\n"
			+ "The source directory must contain files\n"
			+ "\tpagelinks.sql\n"
			+ "\tpages-articles.xml\n"
			+ "\tcategorylinks.sql\n"
			+ "GZip or BZip2 compressed archives of above-named files are also allowed.\n"
			+ "Please set up a decompressor.xml for a usage of other external archive utilities (see documentation for more help).\n";

	private static final long startTime = System.currentTimeMillis();

	private static final IEnvironmentFactory environmentFactory = SpringFactory
			.getInstance();

	private static final ILogger logger = environmentFactory.getLogger();

	public static void main(String[] args) {
		if (args.length > 3) {
			Configuration config = getConfigFromArgs(args);
			DataMachineFiles files = new DataMachineFiles(logger);
			files.setDataDirectory(args[DATADIR_ARG]);
			if (files.checkAll()) {
				try {

					ISnapshotGenerator generator = environmentFactory
							.getSnapshotGenerator();
					generator.setConfiguration(config);
					generator.setFiles(files);
					generator.start();

					logger.log("End of the application. Working time = "
							+ String.valueOf(System.currentTimeMillis()
									- startTime) + " ms");
				} catch (Exception e) {
					logger.log(e);
				}
			} else {
				logger.log("Not all necessary source files could be found in "
						+ args[DATADIR_ARG]);
			}

		} else {
			System.out.println(USAGE);
		}

	}

	private static Configuration getConfigFromArgs(String[] args) {
		Configuration config = new Configuration(logger);
		config.setLanguage(args[LANG_ARG]);
		config.setMainCategory(args[MAINCATEGORY_ARG]);
		config.setDisambiguationCategory(args[DISAMBIGUATION_ARG]);

		return config;
	}

}
