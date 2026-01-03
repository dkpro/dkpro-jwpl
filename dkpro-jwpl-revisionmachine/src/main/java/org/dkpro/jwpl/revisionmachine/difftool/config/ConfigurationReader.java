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
package org.dkpro.jwpl.revisionmachine.difftool.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.data.ConfigEnum;
import org.dkpro.jwpl.revisionmachine.difftool.data.OutputType;
import org.dkpro.jwpl.revisionmachine.difftool.data.SurrogateModes;
import org.dkpro.jwpl.revisionmachine.difftool.data.archive.ArchiveDescription;
import org.dkpro.jwpl.revisionmachine.difftool.data.archive.InputType;
import org.slf4j.event.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This Reader reads the XML-configuration files for the DiffTool.
 */
public class ConfigurationReader
{

    /**
     * XML tree root node
     */
    private final Element root;

    /**
     * Section identifier - Mode
     */
    private static final String SECTION_MODE = "VALUES";

    /**
     * Key identifier - Mode >> Minimum longest common substring
     */
    private static final String KEY_VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING = "VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING";

    /**
     * Key identifier - Mode >> full revision counter
     */
    private static final String KEY_COUNTER_FULL_REVISION = "COUNTER_FULL_REVISION";

    /**
     * Section identifier - Externals
     */
    private static final String SECTION_EXTERNALS = "EXTERNALS";

    /**
     * Key identifier - Externals >> SevenZip
     */
    private static final String KEY_SEVENZIP = "SEVENZIP";

    /**
     * Section identifier - Input
     */
    private static final String SECTION_INPUT = "INPUT";

    /**
     * Key identifier - Input >> Surrogates Mode
     */
    private static final String KEY_MODE_SURROGATES = "MODE_SURROGATES";

    /**
     * Key identifier - Input >> Wikipedia Encoding
     */
    private static final String KEY_WIKIPEDIA_ENCODING = "WIKIPEDIA_ENCODING";

    /**
     * Subsection identifier - Input -> Archive
     */
    private static final String SUBSECTION_ARCHIVE = "ARCHIVE";

    /**
     * Key identifier - Input -> Archive >> Type
     */
    private static final String KEY_TYPE = "TYPE";

    /**
     * Key identifier - Input -> Archive >> Path
     */
    private static final String KEY_PATH = "PATH";

    /**
     * Key identifier - Input -> Archive >> Start
     */
    private static final String KEY_START = "START";

    /**
     * Section identifier - Output
     */
    private static final String SECTION_OUTPUT = "OUTPUT";

    /**
     * Key identifier - Output >> MODE
     */
    private static final String KEY_OUTPUT_MODE = "OUTPUT_MODE";

    /**
     * Key identifier - Output >> MODE >> UNCOMPRESSED File Size
     */
    private static final String KEY_LIMIT_SQL_FILE_SIZE = "LIMIT_SQL_FILE_SIZE";

    /**
     * Key identifier - Output >> Enable Datafile
     */
    private static final String KEY_OUTPUT_DATAFILE = "MODE_DATAFILE_OUTPUT";

    /**
     * Key identifier - Output >> MODE >> UNCOMPRESSED Archive Size
     */
    private static final String KEY_LIMIT_SQL_ARCHIVE_SIZE = "LIMIT_SQL_ARCHIVE_SIZE";

    /**
     * Key identifier - Output >> MODE >> Zip-Compression enabled
     */
    private static final String KEY_MODE_ZIP_COMPRESSION_ENABLED = "MODE_ZIP_COMPRESSION_ENABLED";

    /**
     * Key identifier - Output >> MODE >> Binary output enabled
     */
    private static final String KEY_MODE_BINARY_OUTPUT_ENABLED = "MODE_BINARY_OUTPUT_ENABLED";

    /**
     * Subsection identifier - Output -> UNCOMPRESSED
     */
    private static final String SUBSECTION_SQL = "UNCOMPRESSED";

    /**
     * Key identifier - Output -> UNCOMPRESSED >> Host
     */
    private static final String KEY_HOST = "HOST";

    /**
     * Key identifier - Output -> UNCOMPRESSED >> Database
     */
    private static final String KEY_DATABASE = "DATABASE";

    /**
     * Key identifier - Output -> UNCOMPRESSED >> User
     */
    private static final String KEY_USER = "USER";

    /**
     * Key identifier - Output -> UNCOMPRESSED >> Password
     */
    private static final String KEY_PASSWORD = "PASSWORD";

    /**
     * Section identifier - Cache
     */
    private static final String SECTION_CACHE = "CACHE";

    /**
     * Key identifier - Cache >> Task Size Revisions
     */
    private static final String KEY_LIMIT_TASK_SIZE_REVISIONS = "LIMIT_TASK_SIZE_REVISIONS";

    /**
     * Key identifier - Cache >> Task Size Diff
     */
    private static final String KEY_LIMIT_TASK_SIZE_DIFFS = "LIMIT_TASK_SIZE_DIFFS";

    /**
     * Key identifier - Cache >> SQLProducer MAXALLOWEDPACKET
     */
    private static final String KEY_LIMIT_SQLSERVER_MAX_ALLOWED_PACKET = "LIMIT_SQLSERVER_MAX_ALLOWED_PACKET";

    /**
     * Section identifier - Logging
     */
    private static final String SECTION_LOGGING = "LOGGING";

    /**
     * Section identifier - Logging >> Root folder
     */
    private static final String KEY_ROOT_FOLDER = "ROOT_FOLDER";

    /**
     * Subsection identifier - Logging -> DiffTool
     */
    private static final String SUBSUBSECTION_DIFF_TOOL = "DIFF_TOOL";

    /**
     * Key identifier - Logging -> ... >> Level
     */
    private static final String KEY_LOG_LEVEL = "LEVEL";

    /**
     * Key identifier - Logging -> ... >> Path
     */
    private static final String KEY_LOG_PATH = "PATH";

    /**
     * Section identifier - Debug
     */
    private static final String SECTION_DEBUG = "DEBUG";

    /**
     * Key identifier - Debug -> Output >> Verification Diff
     */
    private static final String KEY_VERIFICATION_DIFF = "VERIFICATION_DIFF";

    /**
     * Key identifier - Debug -> Output >> Verification Encoding
     */
    private static final String KEY_VERIFICATION_ENCODING = "VERIFICATION_ENCODING";

    /**
     * Key identifier - Debug -> Output >> Statistical
     */
    private static final String KEY_STATISTICAL_OUTPUT = "STATISTICAL_OUTPUT";

    /**
     * Subsection identifier - Debug -> Output
     */
    private static final String SUBSECTION_DEBUG_OUTPUT = "DEBUG_OUTPUT";

    /**
     * Key identifier - Debug -> Output >> Enabled
     */
    private final String KEY_DEBUG_ENABLED = "ENABLED";

    /**
     * Key identifier - Debug -> Output >> Path
     */
    private static final String KEY_DEBUG_PATH = "PATH";

    /**
     * Section identifier - filter
     */
    private static final String SECTION_FILTER = "FILTER";

    /**
     * Subsection identifier - filter -> namespaces
     */
    private static final String SUBSECTION_FILTER_NAMESPACES = "NAMESPACES";

    /**
     * Key identifier - filter -> namespaces >> ns
     */
    private static final String NAMESPACE_TO_KEEP = "NS";

    /**
     *  Creates a new ConfigurationReader object.
     *
     * @param path The path to the configuration file to read from.
     * @throws IOException Thrown if an error occurs while reading the file.
     * @throws SAXException Thrown if an error occurs while building the document.
     * @throws ParserConfigurationException Thrown if an error occurs while parsing the document.
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
     * Reads the input of the configuration file and parses the into a {@link ConfigSettings} object.
     *
     * @return ConfigSettings The fully initialized configuration, guaranteed to be not {@code null}.
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
            switch (name) {
                case SECTION_MODE -> parseModeConfig(node, config);
                case SECTION_EXTERNALS -> parseExternalsConfig(node, config);
                case SECTION_INPUT -> parseInputConfig(node, config);
                case SECTION_OUTPUT -> parseOutputConfig(node, config);
                case SECTION_CACHE -> parseCacheConfig(node, config);
                case SECTION_LOGGING -> parseLoggingConfig(node, config);
                case SECTION_DEBUG -> parseDebugConfig(node, config);
                case SECTION_FILTER -> parseFilterConfig(node, config);
            }
        }

        return config;
    }

    /**
     * Parses the filter parameter section.
     *
     * @param node
     *            Reference to the current used XML node
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
     *            Reference to the current used XML node
     * @param config
     *            Reference to the ConfigSettings
     */
    private void parseNamespaceFilterConfig(final Node node, final ConfigSettings config)
    {
        String name;
        Node nnode;
        final NodeList list = node.getChildNodes();
        final int length = list.getLength();
        final Set<Integer> namespaces = new HashSet<>();

        for (int i = 0; i < length; i++) {
            nnode = list.item(i);

            name = nnode.getNodeName().toUpperCase();
            if (name.equals(NAMESPACE_TO_KEEP)) {
                int value = Integer.parseInt(nnode.getChildNodes().item(0).getNodeValue());
                namespaces.add(value);
            }
        }

        config.setConfigParameter(ConfigurationKeys.NAMESPACES_TO_KEEP, namespaces);

    }

    /**
     * Parses the mode parameter section.
     *
     * @param node
     *            Reference to the current used XML node
     * @param config
     *            Reference to the ConfigSettings
     */
    private void parseModeConfig(final Node node, final ConfigSettings config)
    {

        String name;
        Node nnode;
        NodeList list = node.getChildNodes();

        int length = list.getLength();
        for (int i = 0; i < length; i++) {
            nnode = list.item(i);

            name = nnode.getNodeName().toUpperCase();
            if (name.equals(KEY_VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING)) {
                int value = Integer.parseInt(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING, value);
            }
            else if (name.equals(KEY_COUNTER_FULL_REVISION)) {
                int value = Integer.parseInt(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.COUNTER_FULL_REVISION, value);
            }
        }
    }

    /**
     * Parses the externals parameter section.
     *
     * @param node
     *            Reference to the current used XML node
     * @param config
     *            Reference to the ConfigSettings
     */
    private void parseExternalsConfig(final Node node, final ConfigSettings config)
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
                config.setConfigParameter(ConfigurationKeys.PATH_PROGRAM_7ZIP, value);
            }
        }
    }

    /**
     * Parses the input parameter section.
     *
     * @param node
     *            Reference to the current used XML node
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
            switch (name) {
                case KEY_WIKIPEDIA_ENCODING -> {
                  value = nnode.getChildNodes().item(0).getNodeValue();
                  config.setConfigParameter(ConfigurationKeys.WIKIPEDIA_ENCODING, value);
                }
                case KEY_MODE_SURROGATES -> {
                  SurrogateModes oValue = SurrogateModes
                          .parse(nnode.getChildNodes().item(0).getNodeValue());
                  config.setConfigParameter(ConfigurationKeys.MODE_SURROGATES, oValue);
                }
                case SUBSECTION_ARCHIVE -> parseInputArchive(nnode, config);
            }
        }
    }

    /**
     * Parses the input archive subsection.
     *
     * @param node
     *            Reference to the current used XML node
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
                type = InputType.parse(nnode.getChildNodes().item(0).getNodeValue());
            }
            else if (name.equals(KEY_PATH)) {
                path = nnode.getChildNodes().item(0).getNodeValue();
                path = path.substring(1, path.length() - 1);
            }
            else if (name.equals(KEY_START)) {
                startPosition = Long.parseLong(nnode.getChildNodes().item(0).getNodeValue());
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
     *            Reference to the current used XML node
     * @param config
     *            Reference to the ConfigSettings
     */
    private void parseOutputConfig(final Node node, final ConfigSettings config)
    {

        String name;
        Node nnode;
        NodeList list = node.getChildNodes();

        int length = list.getLength();
        for (int i = 0; i < length; i++) {
            nnode = list.item(i);

            name = nnode.getNodeName().toUpperCase();
            if (name.equals(KEY_OUTPUT_MODE)) {
                OutputType oValue = OutputType.parse(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.MODE_OUTPUT, oValue);
            }
            else if (name.equals(KEY_PATH)) {
                String path = nnode.getChildNodes().item(0).getNodeValue();
                path = path.substring(1, path.length() - 1);
                config.setConfigParameter(ConfigurationKeys.PATH_OUTPUT_SQL_FILES, path);
            }
            else if (name.equals(KEY_OUTPUT_DATAFILE)) {
                boolean bValue = Boolean.parseBoolean(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.MODE_DATAFILE_OUTPUT, bValue);
            }
            else if (name.equals(KEY_LIMIT_SQL_FILE_SIZE)) {
                long lValue = Long.parseLong(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.LIMIT_SQL_FILE_SIZE, lValue);
            }
            else if (name.equals(KEY_LIMIT_SQL_ARCHIVE_SIZE)) {
                long lValue = Long.parseLong(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.LIMIT_SQL_ARCHIVE_SIZE, lValue);
            }
            else if (name.equals(KEY_MODE_ZIP_COMPRESSION_ENABLED)) {
                boolean bValue = Boolean.parseBoolean(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED, bValue);
            }
            else if (name.equals(KEY_MODE_BINARY_OUTPUT_ENABLED)) {
                boolean bValue = Boolean.parseBoolean(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.MODE_BINARY_OUTPUT_ENABLED, bValue);
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
     *            Reference to the current used XML node
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
     *            Reference to the current used XML node
     * @param config
     *            Reference to the ConfigSettings
     */
    private void parseCacheConfig(final Node node, final ConfigSettings config)
    {

        String name;
        Node nnode;
        NodeList list = node.getChildNodes();

        int length = list.getLength();
        for (int i = 0; i < length; i++) {
            nnode = list.item(i);

            name = nnode.getNodeName().toUpperCase();
            if (name.equals(KEY_LIMIT_TASK_SIZE_REVISIONS)) {
                long lValue = Long.parseLong(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.LIMIT_TASK_SIZE_REVISIONS, lValue);
            }
            else if (name.equals(KEY_LIMIT_TASK_SIZE_DIFFS)) {
                long lValue = Long.parseLong(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.LIMIT_TASK_SIZE_DIFFS, lValue);
            }
            else if (name.equals(KEY_LIMIT_SQLSERVER_MAX_ALLOWED_PACKET)) {
                long lValue = Long.parseLong(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.LIMIT_SQLSERVER_MAX_ALLOWED_PACKET, lValue);
            }
        }
    }

    /**
     * Parses the logging parameter section.
     *
     * @param node
     *            Reference to the current used XML node
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
                config.setConfigParameter(ConfigurationKeys.LOGGING_PATH_DIFFTOOL, value);
            }
            else if (name.equals(SUBSUBSECTION_DIFF_TOOL)) {
                parseLoggerConfig(nnode, config, null, ConfigurationKeys.LOGGING_LOGLEVEL_DIFFTOOL);
            }
        }
    }

    /**
     * Parses the information for a logger.
     *
     * @param node
     *            Reference to the current used XML node
     * @param config
     *            Reference to the ConfigSettings
     * @param logPath
     *            Key for the path of this logger.
     * @param logLevel
     *            Key for the level of this logger.
     */
    private void parseLoggerConfig(final Node node, final ConfigSettings config,
                                   final ConfigurationKeys logPath, final ConfigurationKeys logLevel)
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

                level = Level.valueOf(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(logLevel, level);
            }
        }
    }

    /**
     * Parses the debug parameter section.
     *
     * @param node
     *            Reference to the current used XML node
     * @param config
     *            Reference to the ConfigSettings
     */
    private void parseDebugConfig(final Node node, final ConfigSettings config)
    {

        String name;
        Node nnode;
        NodeList list = node.getChildNodes();

        int length = list.getLength();
        for (int i = 0; i < length; i++) {
            nnode = list.item(i);

            name = nnode.getNodeName().toUpperCase();
            if (name.equals(KEY_VERIFICATION_DIFF)) {

                boolean value = Boolean.parseBoolean(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.VERIFICATION_DIFF, value);

            }
            else if (name.equals(KEY_VERIFICATION_ENCODING)) {

                boolean value = Boolean.parseBoolean(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.VERIFICATION_ENCODING, value);

            }
            else if (name.equals(KEY_STATISTICAL_OUTPUT)) {

                boolean value = Boolean.parseBoolean(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.MODE_STATISTICAL_OUTPUT, value);

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
     *            Reference to the current used XML node
     * @param config
     *            Reference to the ConfigSettings
     */
    private void parseDebugOutputConfig(final Node node, final ConfigSettings config)
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

                config.setConfigParameter(ConfigurationKeys.LOGGING_PATH_DEBUG, value);

            }
            else if (name.equals(KEY_DEBUG_ENABLED)) {

                Boolean enabled = Boolean.parseBoolean(nnode.getChildNodes().item(0).getNodeValue());
                config.setConfigParameter(ConfigurationKeys.MODE_DEBUG_OUTPUT, enabled);
            }
        }
    }
}
