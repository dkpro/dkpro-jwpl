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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.writer;

import java.io.IOException;
import java.io.OutputStream;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.archivers.Bzip2Archiver;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.OutputType;

public class OutputFactory
{

	private static String PATH_PROGRAM_7ZIP = null;
	private static OutputType MODE_OUTPUT = null;
	private static ConfigurationManager config = null;

	static {
			try {
				config = ConfigurationManager.getInstance();
				MODE_OUTPUT = (OutputType) config.getConfigParameter(ConfigurationKeys.MODE_OUTPUT);
			}
			catch (ConfigurationException e) {
				e.printStackTrace();
				System.exit(-1);
			}
	}

	private static OutputStream compressWith7Zip(final String archivePath)
		throws ConfigurationException
	{

		PATH_PROGRAM_7ZIP = (String) config.getConfigParameter(ConfigurationKeys.PATH_PROGRAM_7ZIP);

		if (PATH_PROGRAM_7ZIP == null) {
			throw ErrorFactory
					.createConfigurationException(ErrorKeys.CONFIGURATION_PARAMETER_UNDEFINED);
		}

		try {
			Runtime runtime = Runtime.getRuntime();
			Process p = runtime.exec(PATH_PROGRAM_7ZIP + " a -t7z -si "
					+ archivePath);
			return p.getOutputStream();

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static OutputStream compressWithBZip2(final String archivePath)
		throws ConfigurationException
	{

		OutputStream output = null;
		try {
			output = new Bzip2Archiver().getCompressionStream(archivePath);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}

	public static OutputStream getOutputStream(final String archivePath)
		throws ConfigurationException
	{

		switch (MODE_OUTPUT) {
		case SEVENZIP:
			if((Boolean)config.getConfigParameter(ConfigurationKeys.MODE_DATAFILE_OUTPUT)){
				return compressWith7Zip(archivePath+ ".csv.7z");
			}else{
				return compressWith7Zip(archivePath+ ".sql.7z");
			}
		case BZIP2:
			if((Boolean)config.getConfigParameter(ConfigurationKeys.MODE_DATAFILE_OUTPUT)){
				return compressWithBZip2(archivePath+ ".csv.bz2");
			}else{
				return compressWithBZip2(archivePath+ ".sql.bz2");
			}
		default:
			throw ErrorFactory
					.createConfigurationException(ErrorKeys.DELTA_CONSUMERS_SQL_WRITER_OUTPUTFACTORY_ILLEGAL_OUTPUTMODE_VALUE);
		}
	}
}
