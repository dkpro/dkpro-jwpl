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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer;

import java.io.IOException;
import java.io.OutputStream;

import org.dkpro.jwpl.revisionmachine.archivers.Bzip2Archiver;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ConfigurationException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorFactory;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.data.OutputType;

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
			Process p = runtime.exec(PATH_PROGRAM_7ZIP + " a -t7z -si " + archivePath);
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
