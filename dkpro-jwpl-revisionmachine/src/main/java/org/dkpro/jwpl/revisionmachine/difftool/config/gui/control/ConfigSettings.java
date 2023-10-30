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
package org.dkpro.jwpl.revisionmachine.difftool.config.gui.control;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationReader;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.data.ConfigEnum;
import org.dkpro.jwpl.revisionmachine.difftool.data.OutputType;
import org.dkpro.jwpl.revisionmachine.difftool.data.SurrogateModes;
import org.dkpro.jwpl.revisionmachine.difftool.data.archive.ArchiveDescription;
import org.slf4j.event.Level;

/**
 * This class contain all configuration parameters.
 */
public class ConfigSettings {

  /**
   * Returns the type of the configuration
   */
  private ConfigEnum type;

  /**
   * List of input archives
   */
  private List<ArchiveDescription> archives;

  /**
   * Map that contains the configuration parameters and values
   */
  private Map<ConfigurationKeys, Object> parameterMap;

  /**
   * Creates an empty {@link ConfigSettings} object of unspecified
   * type.
   */
  public ConfigSettings() {
    this.parameterMap = new HashMap<>();
    this.archives = new ArrayList<>();
  }

  /**
   * Creates an empty {@link ConfigSettings} object of given type.
   *
   * @param type Configuration Type
   */
  public ConfigSettings(final ConfigEnum type) {
    this.type = type;
    this.parameterMap = new HashMap<>();
    this.archives = new ArrayList<>();
  }

  /**
   * Adds an input archive description object to the input archive list.
   *
   * @param archive ArchiveDescription
   */
  public void add(final ArchiveDescription archive) {
    this.archives.add(archive);
  }

  /**
   * Returns the input archive at the specified position.
   *
   * @param index position
   * @return input archive description
   */
  public ArchiveDescription getArchiveDescription(int index) {
    return this.archives.get(index);
  }

  /**
   * Returns the list of input archives.
   *
   * @return list of the input archive descriptions
   */
  public List<ArchiveDescription> getArchiveList() {
    return this.archives;
  }

  /**
   * Returns the number of input archives.
   *
   * @return size of the input archive list
   */
  public int archiveSize() {
    return this.archives.size();
  }

  /**
   * Returns an iterator over the input archive list.
   *
   * @return Iterator
   */
  public Iterator<ArchiveDescription> archiveIterator() {
    return this.archives.iterator();
  }

  /**
   * Assigns the given value to the the given key.
   *
   * @param key   configuration key
   * @param value value
   */
  public void setConfigParameter(final ConfigurationKeys key, Object value) {
    // before setting parameter, check if paths have trailing File.separator
    if (key == ConfigurationKeys.LOGGING_PATH_DEBUG
            || key == ConfigurationKeys.LOGGING_PATH_DIFFTOOL
            || key == ConfigurationKeys.PATH_OUTPUT_SQL_FILES) {

      String v = (String) value;
      // if we do not have a trailing file separator and the current
      // path is compatible to the system that is running the config tool,
      // then add a trailing separator
      if (!v.endsWith(File.separator) && v.contains(File.separator)) {
        value = v + File.separator;
      }
    }

    this.parameterMap.put(key, value);
  }

  /**
   * Returns the value related to the configuration key or null if the key is
   * not contained.
   *
   * @param configParameter configuration key
   * @return value or null
   */
  public Object getConfigParameter(final ConfigurationKeys configParameter) {
    if (this.parameterMap.containsKey(configParameter)) {
      return this.parameterMap.get(configParameter);
    }

    return null;
  }

  /**
   * Applies the default single thread configuration of the DiffTool to this
   * settings.
   */
  public void defaultConfiguration() {
    clear();

    setConfigParameter(ConfigurationKeys.VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING, 12);

    setConfigParameter(ConfigurationKeys.COUNTER_FULL_REVISION, 1000);

    setConfigParameter(ConfigurationKeys.LIMIT_TASK_SIZE_REVISIONS, 5000000L);

    setConfigParameter(ConfigurationKeys.LIMIT_TASK_SIZE_DIFFS, 1000000L);

    setConfigParameter(ConfigurationKeys.LIMIT_SQLSERVER_MAX_ALLOWED_PACKET, 1000000L);

    setConfigParameter(ConfigurationKeys.MODE_SURROGATES, SurrogateModes.DISCARD_REVISION);

    setConfigParameter(ConfigurationKeys.WIKIPEDIA_ENCODING, StandardCharsets.UTF_8.toString());

    setConfigParameter(ConfigurationKeys.MODE_OUTPUT, OutputType.BZIP2);

    setConfigParameter(ConfigurationKeys.MODE_DATAFILE_OUTPUT, false);

    setConfigParameter(ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED, true);

    setConfigParameter(ConfigurationKeys.LIMIT_SQL_FILE_SIZE, 1000000000L);

    setConfigParameter(ConfigurationKeys.LOGGING_PATH_DIFFTOOL, "logs");

    setConfigParameter(ConfigurationKeys.LOGGING_LOGLEVEL_DIFFTOOL, Level.INFO);

    setConfigParameter(ConfigurationKeys.VERIFICATION_DIFF, false);

    setConfigParameter(ConfigurationKeys.VERIFICATION_ENCODING, false);

    setConfigParameter(ConfigurationKeys.MODE_DEBUG_OUTPUT, false);

    setConfigParameter(ConfigurationKeys.MODE_STATISTICAL_OUTPUT, false);

    Set<Integer> defaultNamespaces = new HashSet<>();
    defaultNamespaces.add(0);
    defaultNamespaces.add(1);
    setConfigParameter(ConfigurationKeys.NAMESPACES_TO_KEEP, defaultNamespaces);

    this.type = ConfigEnum.DEFAULT;
  }


  /**
   * Deletes all contained input archives and configuration parameter.
   */
  public void clear() {
    this.parameterMap.clear();
    this.archives.clear();
  }

  /**
   * Returns the configuration type.
   *
   * @return configuration type
   */
  public ConfigEnum getConfigType() {
    return this.type;
  }

  /**
   * Loads the configuration settings from a file.
   *
   * @param path path to the configuration file
   */
  public void loadConfig(final String path) {
    try {

      ConfigurationReader reader = new ConfigurationReader(path);
      ConfigSettings settings = reader.read();

      clear();

      this.type = settings.type;
      this.parameterMap = settings.parameterMap;
      this.archives = settings.archives;

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
