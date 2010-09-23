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
package de.tudarmstadt.ukp.wikipedia.timemachine.domain;

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.Configuration;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.ISnapshotGenerator;
import de.tudarmstadt.ukp.wikipedia.wikimachine.factory.IEnvironmentFactory;
import de.tudarmstadt.ukp.wikipedia.wikimachine.factory.SpringFactory;

/**
 * This is the main class of the DBMapping Tool of the JWPL.<br>
 * The <code>main</code> method gets the path of a configuration file as
 * argument<br>
 * <br>
 * 
 * Refactored on 16 April 2009 by Ivan Galkin .
 * 
 * @author Anouar
 * @author ivan.galkin
 * 
 * 
 */
public class JWPLTimeMachine {

	private static final IEnvironmentFactory environmentFactory = SpringFactory
			.getInstance();

	private static final long startTime = System.currentTimeMillis();
	private static final ILogger logger = environmentFactory.getLogger();

	/**
	 * Checks given arguments
	 * 
	 * @param args
	 * <br>
	 *            args[0] the settings file like described in
	 *            {@link SettingsXML}<br>
	 * 
	 * @return true if all necessary arguments are given and false otherwise
	 * 
	 * @see SettingsXML
	 */
	private static boolean checkArgs(String[] args) {
		boolean result = (args.length > 0);
		if (!result) {
			System.out
					.println("Usage: java -jar JWPLTimeMachine.jar <config-file>");
		}
		return result;
	}

	public static void main(String[] args) {

		try {
			if (checkArgs(args)) {
				logger.log("parsing configuration file....");
				Configuration config = SettingsXML.loadConfiguration(args[0],
						logger);
				TimeMachineFiles files = SettingsXML.loadFiles(args[0], logger);

				if (config != null && files != null) {
					if (files.checkAll() && config.checkTimeConfig()) {
						logger.log("processing data ...");

						ISnapshotGenerator generator = environmentFactory
								.getSnapshotGenerator();
						generator.setConfiguration(config);
						generator.setFiles(files);
						generator.start();

						logger.log("End of the application. Working time = "
								+ String.valueOf(System.currentTimeMillis()
										- startTime) + " ms");
					}
				}
			}
		} catch (Exception e) {
			logger.log(e);
		}
	}
}
