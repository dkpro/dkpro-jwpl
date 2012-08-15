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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigSettings;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.ConfigEnum;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.OutputType;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.SurrogateModes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.ArchiveDescription;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.InputType;

/**
 * This Reader reads the xml-configuration files for the DiffTool.
 *
 *
 *
 */
public class ConfigurationReader
{

	/** XML tree root node */
	private final Element root;

	/** Section identifier - Mode */
	private final String SECTION_MODE = "VALUES";

	/** Key identifier - Mode >> Minimum longest common substring */
	private final String KEY_VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING = "VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING";

	/** Key identifier - Mode >> full revision counter */
	private final String KEY_COUNTER_FULL_REVISION = "COUNTER_FULL_REVISION";

	/** Section identifier - Externals */
	private final String SECTION_EXTERNALS = "EXTERNALS";

	/** Key identifier - Externals >> SevenZip */
	private final String KEY_SEVENZIP = "SEVENZIP";

	/** Section identifier - Input */
	private final String SECTION_INPUT = "INPUT";

	/** Key identifier - Input >> Surrogates Mode */
	private final String KEY_MODE_SURROGATES = "MODE_SURROGATES";

	/** Key identifier - Input >> Wikipedia Encoding */
	private final String KEY_WIKIPEDIA_ENCODING = "WIKIPEDIA_ENCODING";

	/** Subsection identifier - Input -> Archive */
	private final String SUBSECTION_ARCHIVE = "ARCHIVE";

	/** Key identifier - Input -> Archive >> Type */
	private final String KEY_TYPE = "TYPE";

	/** Key identifier - Input -> Archive >> Path */
	private final String KEY_PATH = "PATH";

	/** Key identifier - Input -> Archive >> Start */
	private final String KEY_START = "START";

	/** Section identifier - Output */
	private final String SECTION_OUTPUT = "OUTPUT";

	/** Key identifier - Output >> MODE */
	private final String KEY_OUTPUT_MODE = "OUTPUT_MODE";

	/** Key identifier - Output >> MODE >> UNCOMPRESSED File Size */
	private final String KEY_LIMIT_SQL_FILE_SIZE = "LIMIT_SQL_FILE_SIZE";

	/** Key identifier - Output >> Enable Datafile */
	private final String KEY_OUTPUT_DATAFILE = "MODE_DATAFILE_OUTPUT";

	/** Key identifier - Output >> MODE >> UNCOMPRESSED Archive Size */
	private final String KEY_LIMIT_SQL_ARCHIVE_SIZE = "LIMIT_SQL_ARCHIVE_SIZE";

	/** Key identifier - Output >> MODE >> Zip-Compression enabled */
	private final String KEY_MODE_ZIP_COMPRESSION_ENABLED = "MODE_ZIP_COMPRESSION_ENABLED";

	/** Key identifier - Output >> MODE >> Binary output enabled */
	private final String KEY_MODE_BINARY_OUTPUT_ENABLED = "MODE_BINARY_OUTPUT_ENABLED";

	/** Subsection identifier - Output -> UNCOMPRESSED */
	private final String SUBSECTION_SQL = "UNCOMPRESSED";

	/** Key identifier - Output -> UNCOMPRESSED >> Host */
	private final String KEY_HOST = "HOST";

	/** Key identifier - Output -> UNCOMPRESSED >> Database */
	private final String KEY_DATABASE = "DATABASE";

	/** Key identifier - Output -> UNCOMPRESSED >> User */
	private final String KEY_USER = "USER";

	/** Key identifier - Output -> UNCOMPRESSED >> Password */
	private final String KEY_PASSWORD = "PASSWORD";

	/** Section identifier - Cache */
	private final String SECTION_CACHE = "CACHE";

	/** Key identifier - Cache >> Task Size Revisions */
	private final String KEY_LIMIT_TASK_SIZE_REVISIONS = "LIMIT_TASK_SIZE_REVISIONS";

	/** Key identifier - Cache >> Task Size Diff */
	private final String KEY_LIMIT_TASK_SIZE_DIFFS = "LIMIT_TASK_SIZE_DIFFS";

	/** Key identifier - Cache >> SQLProducer MAXALLOWEDPACKET */
	private final String KEY_LIMIT_SQLSERVER_MAX_ALLOWED_PACKET = "LIMIT_SQLSERVER_MAX_ALLOWED_PACKET";

	/** Section identifier - Logging */
	private final String SECTION_LOGGING = "LOGGING";

	/** Section identifier - Logging >> Root folder */
	private final String KEY_ROOT_FOLDER = "ROOT_FOLDER";

	/** Subsection identifier - Logging -> DiffTool */
	private final String SUBSUBSECTION_DIFF_TOOL = "DIFF_TOOL";

	/** Key identifier - Logging -> ... >> Level */
	private final String KEY_LOG_LEVEL = "LEVEL";

	/** Key identifier - Logging -> ... >> Path */
	private final String KEY_LOG_PATH = "PATH";

	/** Section identifier - Debug */
	private final String SECTION_DEBUG = "DEBUG";

	/** Key identifier - Debug -> Output >> Verification Diff */
	private final String KEY_VERIFICATION_DIFF = "VERIFICATION_DIFF";

	/** Key identifier - Debug -> Output >> Verification Encoding */
	private final String KEY_VERIFICATION_ENCODING = "VERIFICATION_ENCODING";

	/** Key identifier - Debug -> Output >> Statistical */
	private final String KEY_STATISTICAL_OUTPUT = "STATISTICAL_OUTPUT";

	/** Subsection identifier - Debug -> Output */
	private final String SUBSECTION_DEBUG_OUTPUT = "DEBUG_OUTPUT";

	/** Key identifier - Debug -> Output >> Enabled */
	private final String KEY_DEBUG_ENABLED = "ENABLED";

	/** Key identifier - Debug -> Output >> Path */
	private final String KEY_DEBUG_PATH = "PATH";

	/** Section identifier - filter */
	private final String SECTION_FILTER = "FILTER";

	/** Subsection identifier - filter -> namespaces */
	private final String SUBSECTION_FILTER_NAMESPACES = "NAMESPACES";

	/** Key identifier - filter -> namespaces >> ns */
	private final String NAMESPACE_TO_KEEP = "NS";

	/**
	 * (Constructor) Creates a new ConfigurationReader object.
	 *
	 * @param path
	 *
	 * @throws IOException
	 *             if an error occurs while reading the file
	 * @throws SAXException
	 *             if an error occurs while building the document
	 *
	 * @throws ParserConfigurationException
	 *             if an error occurs while parsing the document
	 */
	public ConfigurationReader(final String path)
		throws IOException, SAXException, ParserConfigurationException
	{

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder loader = factory.newDocumentBuilder();

		Document document = loader.parse(path);
		root = document.getDocumentElement();
	}

	/**
	 * Reads the input of the configuration file and parses the into the
	 * ConfigSettings object.
	 *
	 * @return ConfigSettings
	 */
	public ConfigSettings read()
	{

		ConfigSettings config = new ConfigSettings(ConfigEnum.IMPORT);

		String name;
		Node node;
		NodeList list = root.getChildNodes();

		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			node = list.item(i);

			name = node.getNodeName().toUpperCase();

			if (name.equals(SECTION_MODE)) {
				parseModeConfig(node, config);
			}
			else if (name.equals(SECTION_EXTERNALS)) {
				parseExternalsConfig(node, config);
			}
			else if (name.equals(SECTION_INPUT)) {
				parseInputConfig(node, config);
			}
			else if (name.equals(SECTION_OUTPUT)) {
				parseOutputConfig(node, config);
			}
			else if (name.equals(SECTION_CACHE)) {
				parseCacheConfig(node, config);
			}
			else if (name.equals(SECTION_LOGGING)) {
				parseLoggingConfig(node, config);
			}
			else if (name.equals(SECTION_DEBUG)) {
				parseDebugConfig(node, config);
			}
			else if (name.equals(SECTION_FILTER)) {
				parseFilterConfig(node, config);
			}
		}

		return config;
	}


	/**
	 * Parses the filter parameter section.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 */
	private void parseFilterConfig(final Node node, final ConfigSettings config)
	{

		String name;
		Node nnode;
		final NodeList list = node.getChildNodes();
		final int length = list.getLength();

		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();

			if (name.equals(SUBSECTION_FILTER_NAMESPACES)) {
				parseNamespaceFilterConfig(nnode, config);
			}

		}
	}

	/**
	 * Parses the namespaces parameter section. This is the subsection of filter.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 */
	private void parseNamespaceFilterConfig(final Node node, final ConfigSettings config) {
		String name;
		Integer value;
		Node nnode;
		final NodeList list = node.getChildNodes();
		final int length = list.getLength();
		final Set<Integer> namespaces = new HashSet<Integer>();

		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();
			if (name.equals(NAMESPACE_TO_KEEP)) {

				value = Integer.parseInt(nnode.getChildNodes().item(0)
						.getNodeValue());
				namespaces.add(value);


			}

		}

		config.setConfigParameter(
				ConfigurationKeys.NAMESPACES_TO_KEEP,
				namespaces);

	}


	/**
	 * Parses the mode parameter section.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 */
	private void parseModeConfig(final Node node, final ConfigSettings config)
	{

		String name;
		Integer value;
		Node nnode;
		NodeList list = node.getChildNodes();

		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();
			if (name.equals(KEY_VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING)) {

				value = Integer.parseInt(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(
						ConfigurationKeys.VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING,
						value);

			}
			else if (name.equals(KEY_COUNTER_FULL_REVISION)) {

				value = Integer.parseInt(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(
						ConfigurationKeys.COUNTER_FULL_REVISION, value);

			}
		}
	}

	/**
	 * Parses the externals parameter section.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 */
	private void parseExternalsConfig(final Node node,
			final ConfigSettings config)
	{

		String name, value;
		Node nnode;
		NodeList list = node.getChildNodes();

		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();
			if (name.equals(KEY_SEVENZIP)) {

				value = nnode.getChildNodes().item(0).getNodeValue();
				value = value.substring(1, value.length() - 1);

				config.setConfigParameter(ConfigurationKeys.PATH_PROGRAM_7ZIP,
						value);

			}
		}
	}

	/**
	 * Parses the input parameter section.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 */
	private void parseInputConfig(final Node node, final ConfigSettings config)
	{

		String name, value;
		Node nnode;
		NodeList list = node.getChildNodes();

		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();
			if (name.equals(KEY_WIKIPEDIA_ENCODING)) {

				value = nnode.getChildNodes().item(0).getNodeValue();
				config.setConfigParameter(ConfigurationKeys.WIKIPEDIA_ENCODING,
						value);

			}
			else if (name.equals(KEY_MODE_SURROGATES)) {

				SurrogateModes oValue = SurrogateModes.parse(nnode
						.getChildNodes().item(0).getNodeValue());
				config.setConfigParameter(ConfigurationKeys.MODE_SURROGATES,
						oValue);

			}
			else if (name.equals(SUBSECTION_ARCHIVE)) {

				parseInputArchive(nnode, config);

			}
		}
	}

	/**
	 * Parses the input archive subsection.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 */
	private void parseInputArchive(final Node node, final ConfigSettings config)
	{

		String name;

		InputType type = null;
		String path = null;
		long startPosition = 0;

		Node nnode;
		NodeList list = node.getChildNodes();

		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();
			if (name.equals(KEY_TYPE)) {

				type = InputType.parse(nnode.getChildNodes().item(0)
						.getNodeValue());

			}
			else if (name.equals(KEY_PATH)) {

				path = nnode.getChildNodes().item(0).getNodeValue();
				path = path.substring(1, path.length() - 1);

			}
			else if (name.equals(KEY_START)) {

				startPosition = Long.parseLong(nnode.getChildNodes().item(0)
						.getNodeValue());

			}
		}

		if (type == null || path == null) {
			throw new IllegalArgumentException("Illegal Archive Description");
		}

		ArchiveDescription archive = new ArchiveDescription(type, path);
		if (startPosition > 0) {
			archive.setStartPosition(startPosition);
		}

		config.add(archive);
	}

	/**
	 * Parses the output parameter section.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 */
	private void parseOutputConfig(final Node node, final ConfigSettings config)
	{

		String name;
		Long lValue;
		Boolean bValue;
		Node nnode;
		NodeList list = node.getChildNodes();

		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();
			if (name.equals(KEY_OUTPUT_MODE)) {

				OutputType oValue = OutputType.parse(nnode.getChildNodes()
						.item(0).getNodeValue());
				config.setConfigParameter(ConfigurationKeys.MODE_OUTPUT, oValue);

			}
			else if (name.equals(KEY_PATH)) {

				String path = nnode.getChildNodes().item(0).getNodeValue();
				path = path.substring(1, path.length() - 1);

				config.setConfigParameter(
						ConfigurationKeys.PATH_OUTPUT_SQL_FILES, path);

			}
			else if (name.equals(KEY_OUTPUT_DATAFILE)) {
				bValue = Boolean.parseBoolean(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(
						ConfigurationKeys.MODE_DATAFILE_OUTPUT, bValue);
			}
			else if (name.equals(KEY_LIMIT_SQL_FILE_SIZE)) {

				lValue = Long.parseLong(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(
						ConfigurationKeys.LIMIT_SQL_FILE_SIZE, lValue);

			}
			else if (name.equals(KEY_LIMIT_SQL_ARCHIVE_SIZE)) {

				lValue = Long.parseLong(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(
						ConfigurationKeys.LIMIT_SQL_ARCHIVE_SIZE, lValue);

			}
			else if (name.equals(KEY_MODE_ZIP_COMPRESSION_ENABLED)) {

				bValue = Boolean.parseBoolean(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(
						ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED, bValue);

			}
			else if (name.equals(KEY_MODE_BINARY_OUTPUT_ENABLED)) {

				bValue = Boolean.parseBoolean(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(
						ConfigurationKeys.MODE_BINARY_OUTPUT_ENABLED, bValue);

			}
			else if (name.equals(SUBSECTION_SQL)) {

				parseSQLConfig(nnode, config);

			}
		}
	}

	/**
	 * Parses the sql parameter section.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 */
	private void parseSQLConfig(final Node node, final ConfigSettings config)
	{

		String name, value;
		Node nnode;
		NodeList list = node.getChildNodes();

		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();
			if (name.equals(KEY_HOST)) {

				value = nnode.getChildNodes().item(0).getNodeValue();
				config.setConfigParameter(ConfigurationKeys.SQL_HOST, value);

			}
			else if (name.equals(KEY_DATABASE)) {

				value = nnode.getChildNodes().item(0).getNodeValue();
				config.setConfigParameter(ConfigurationKeys.SQL_DATABASE, value);

			}
			else if (name.equals(KEY_USER)) {

				value = nnode.getChildNodes().item(0).getNodeValue();
				config.setConfigParameter(ConfigurationKeys.SQL_USERNAME, value);

			}
			else if (name.equals(KEY_PASSWORD)) {

				value = nnode.getChildNodes().item(0).getNodeValue();
				config.setConfigParameter(ConfigurationKeys.SQL_PASSWORD, value);
			}
		}
	}

	/**
	 * Parses the cache parameter section.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 */
	private void parseCacheConfig(final Node node, final ConfigSettings config)
	{

		String name;
		Long lValue;
		Node nnode;
		NodeList list = node.getChildNodes();

		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();
			if (name.equals(KEY_LIMIT_TASK_SIZE_REVISIONS)) {

				lValue = Long.parseLong(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(
						ConfigurationKeys.LIMIT_TASK_SIZE_REVISIONS, lValue);

			}
			else if (name.equals(KEY_LIMIT_TASK_SIZE_DIFFS)) {

				lValue = Long.parseLong(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(
						ConfigurationKeys.LIMIT_TASK_SIZE_DIFFS, lValue);

			}
			else if (name.equals(KEY_LIMIT_SQLSERVER_MAX_ALLOWED_PACKET)) {

				lValue = Long.parseLong(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(
						ConfigurationKeys.LIMIT_SQLSERVER_MAX_ALLOWED_PACKET,
						lValue);

			}
		}
	}

	/**
	 * Parses the logging parameter section.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 */
	private void parseLoggingConfig(final Node node, final ConfigSettings config)
	{

		String name;
		String value;
		Node nnode;
		NodeList list = node.getChildNodes();

		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();
			if (name.equals(KEY_ROOT_FOLDER)) {

				value = nnode.getChildNodes().item(0).getNodeValue();
				value = value.substring(1, value.length() - 1);

				config.setConfigParameter(
						ConfigurationKeys.LOGGING_PATH_DIFFTOOL, value);

			}
			else if (name.equals(SUBSUBSECTION_DIFF_TOOL)) {

				parseLoggerConfig(nnode, config, null,
						ConfigurationKeys.LOGGING_LOGLEVEL_DIFFTOOL);

			}
		}
	}

	/**
	 * Parses the information for a logger.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 * @param logPath
	 *            Key for the path of this logger.
	 * @param logLevel
	 *            Key for the level of this logger.
	 */
	private void parseLoggerConfig(final Node node,
			final ConfigSettings config, final ConfigurationKeys logPath,
			final ConfigurationKeys logLevel)
	{

		String name, value;
		Level level;
		Node nnode;
		NodeList list = node.getChildNodes();

		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();
			if (name.equals(KEY_LOG_PATH)) {

				value = nnode.getChildNodes().item(0).getNodeValue();
				value = value.substring(1, value.length() - 1);
				config.setConfigParameter(logPath, value);

			}
			else if (name.equals(KEY_LOG_LEVEL)) {

				level = Level.parse(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(logLevel, level);
			}
		}
	}

	/**
	 * Parses the debug parameter section.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 */
	private void parseDebugConfig(final Node node, final ConfigSettings config)
	{

		String name;
		Boolean value;
		Node nnode;
		NodeList list = node.getChildNodes();

		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();
			if (name.equals(KEY_VERIFICATION_DIFF)) {

				value = Boolean.parseBoolean(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(ConfigurationKeys.VERIFICATION_DIFF,
						value);

			}
			else if (name.equals(KEY_VERIFICATION_ENCODING)) {

				value = Boolean.parseBoolean(nnode.getChildNodes().item(0)
						.getNodeValue());

				config.setConfigParameter(
						ConfigurationKeys.VERIFICATION_ENCODING, value);

			}
			else if (name.equals(KEY_STATISTICAL_OUTPUT)) {

				value = Boolean.parseBoolean(nnode.getChildNodes().item(0)
						.getNodeValue());
				config.setConfigParameter(
						ConfigurationKeys.MODE_STATISTICAL_OUTPUT, value);

			}
			else if (name.equals(SUBSECTION_DEBUG_OUTPUT)) {

				parseDebugOutputConfig(nnode, config);
			}
		}
	}

	/**
	 * Parses the debug output parameter subsection.
	 *
	 * @param node
	 *            Reference to the current used xml node
	 * @param config
	 *            Reference to the ConfigSettings
	 */
	private void parseDebugOutputConfig(final Node node,
			final ConfigSettings config)
	{

		String name, value;
		Node nnode;
		NodeList list = node.getChildNodes();

		int length = list.getLength();
		for (int i = 0; i < length; i++) {
			nnode = list.item(i);

			name = nnode.getNodeName().toUpperCase();
			if (name.equals(KEY_DEBUG_PATH)) {

				value = nnode.getChildNodes().item(0).getNodeValue();
				value = value.substring(1, value.length() - 1);

				config.setConfigParameter(ConfigurationKeys.LOGGING_PATH_DEBUG,
						value);

			}
			else if (name.equals(KEY_DEBUG_ENABLED)) {

				Boolean enabled = Boolean.parseBoolean(nnode.getChildNodes()
						.item(0).getNodeValue());
				config.setConfigParameter(ConfigurationKeys.MODE_DEBUG_OUTPUT,
						enabled);
			}
		}
	}
}
