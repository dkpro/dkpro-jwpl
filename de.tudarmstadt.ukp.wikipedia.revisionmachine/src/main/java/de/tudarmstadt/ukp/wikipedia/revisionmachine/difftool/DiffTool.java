/*******************************************************************************
 * Copyright (c) 2011 Ubiquitous Knowledge Processing Lab
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Project Website:
 * 	http://jwpl.googlecode.com
 *
 * Contributors:
 * 	Torsten Zesch
 * 	Simon Kulessa
 * 	Oliver Ferschke
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationReader;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigSettings;

/**
 * This class contains the start method for the DiffTool application.
 *
 *
 *
 */
public class DiffTool
{

	/**
	 * Starts the DiffTool application.
	 *
	 * @param args
	 *            program arguments args[0] has to be the path to the
	 *            configuration file
	 */
	public static void main(final String[] args)
	{

		if (args.length != 1) {
			throw new IllegalArgumentException(
					"Configuration File ist missing.");
		}

		try {

			// Reads the configuration
			ConfigSettings config = readConfiguration(args[0]);
			new DiffToolThread(config).run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads and parses the configuration file.
	 *
	 * @param path
	 *            path to the configuration file
	 * @return ConfigurationSettings
	 *
	 * @throws IOException
	 *             if an error occured while reading the configuration file
	 * @throws SAXException
	 *             if an error occured while using the xml parser
	 * @throws ParserConfigurationException
	 *             if the initialization of the xml parser failed
	 */
	private static ConfigSettings readConfiguration(final String path)
		throws IOException, SAXException, ParserConfigurationException
	{

		ConfigurationReader reader = new ConfigurationReader(path);
		return reader.read();
	}

	/** No object - Utility class */
	private DiffTool()
	{
	}
}
