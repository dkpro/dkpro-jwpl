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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.article.reader;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.archivers.Bzip2Archiver;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ArticleReaderException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.article.ArticleReaderInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.ArchiveDescription;

/**
 * This factory class contains methods to access a input medium.
 *
 * TODO: Add support for alternative commandlines
 *
 *
 *
 */
public class InputFactory
{

	/** Configuration parameter - Path to the 7Zip executable */
	private static String PATH_PROGRAM_7ZIP = null;

	/** Configuration parameter - Charset name of the input data */
	private static String WIKIPEDIA_ENCODING = null;

	private static ConfigurationManager config = null;

	/**
	 * Configuration parameter - Flag, that indicates whether the statistical
	 * output is enabled or not
	 */
	private static boolean MODE_STATISTICAL_OUTPUT = false;

	static {
		try {
			config = ConfigurationManager.getInstance();

			WIKIPEDIA_ENCODING = (String) config
					.getConfigParameter(ConfigurationKeys.WIKIPEDIA_ENCODING);
			MODE_STATISTICAL_OUTPUT = (Boolean) config
					.getConfigParameter(ConfigurationKeys.MODE_STATISTICAL_OUTPUT);

		}
		catch (ConfigurationException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/** No object - Utility class */
	private InputFactory()
	{
	}

	/**
	 * Starts a decompression process using the 7Zip program.
	 *
	 * @param archivePath
	 *            path to the archive
	 * @return InputStreamReader
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 */
	private static InputStreamReader decompressWith7Zip(final String archivePath)
		throws ConfigurationException
	{
		PATH_PROGRAM_7ZIP = (String) config
					.getConfigParameter(ConfigurationKeys.PATH_PROGRAM_7ZIP);

		if (PATH_PROGRAM_7ZIP == null) {
			throw ErrorFactory
					.createConfigurationException(ErrorKeys.CONFIGURATION_PARAMETER_UNDEFINED);
		}

		try {
			Runtime runtime = Runtime.getRuntime();
			Process p = runtime.exec(PATH_PROGRAM_7ZIP + " e " + archivePath
					+ " -so");

			return new InputStreamReader(p.getInputStream(), WIKIPEDIA_ENCODING);

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Starts a decompression process using the BZip2 program.
	 *
	 * @param archivePath
	 *            path to the archive
	 * @return InputStreamReader
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 */
	private static InputStreamReader decompressWithBZip2(
			final String archivePath)
		throws ConfigurationException
	{

		Bzip2Archiver archiver = new Bzip2Archiver();
		InputStreamReader reader = null;
		try {
			reader = archiver.getDecompressionStream(archivePath, WIKIPEDIA_ENCODING);
		}
		catch (IOException e) {

			e.printStackTrace();
		}

		return reader;
	}

	/**
	 * Creates a reader for the xml file.
	 *
	 * @param archivePath
	 *            path to the xml file
	 * @return InputStreamReader
	 */
	private static InputStreamReader readXMLFile(final String archivePath)
	{

		try {
			return new InputStreamReader(new BufferedInputStream(new FileInputStream(archivePath)),
					WIKIPEDIA_ENCODING);

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns an ArticleReader which reads the specified input file.
	 *
	 * @param archive
	 *            input file
	 * @return ArticleReaderInterface
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws ArticleReaderException
	 *             if an error occurred while parsing the file
	 */
	public static ArticleReaderInterface getTaskReader(
			final ArchiveDescription archive)
		throws ConfigurationException, ArticleReaderException
	{
		Reader reader = null;

		switch (archive.getType()) {
		case XML:
			reader = readXMLFile(archive.getPath());
			break;
		case SEVENZIP:
			reader = decompressWith7Zip(archive.getPath());
			break;
		case BZIP2:
			reader = decompressWithBZip2(archive.getPath());
			break;
		default:
			throw ErrorFactory
					.createArticleReaderException(ErrorKeys.DELTA_CONSUMERS_TASK_READER_INPUTFACTORY_ILLEGAL_INPUTMODE_VALUE);
		}

		if (MODE_STATISTICAL_OUTPUT) {
			return new TimedWikipediaXMLReader(reader);
		}
		return new WikipediaXMLReader(reader);
	}

	/**
	 * Returns an ArticleReader which reads the specified input file.
	 *
	 * @param archive
	 *            input file
	 * @param checker
	 *            the article filter
	 * @return ArticleReaderInterface
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws ArticleReaderException
	 *             if an error occurred while parsing the file
	 */
	public static ArticleReaderInterface getTaskReader(
			final ArchiveDescription archive, final ArticleFilter checker)
		throws ConfigurationException, ArticleReaderException
	{
		Reader reader = null;

		//TODO add support for (compressed) XMLdumps that are stored in multiple archives
		switch (archive.getType()) {
		case XML:
			reader = readXMLFile(archive.getPath());
			break;
		case SEVENZIP:
			reader = decompressWith7Zip(archive.getPath());
			break;
		case BZIP2:
			reader = decompressWithBZip2(archive.getPath());
			break;
		default:
			throw ErrorFactory
					.createArticleReaderException(ErrorKeys.DELTA_CONSUMERS_TASK_READER_INPUTFACTORY_ILLEGAL_INPUTMODE_VALUE);
		}

		if (MODE_STATISTICAL_OUTPUT) {
			return new TimedWikipediaXMLReader(reader, checker);
		}
		return new WikipediaXMLReader(reader, checker);
	}
}
