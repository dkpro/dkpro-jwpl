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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.dkpro.jwpl.revisionmachine.common.exceptions.ConfigurationException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.DecodingException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.EncodingException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorFactory;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorKeys;
import org.dkpro.jwpl.revisionmachine.common.exceptions.LoggingException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.SQLConsumerException;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.WriterInterface;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.codec.DataFileEncoder;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.TaskTypes;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * This class writes the output to an archive.
 */
public class DataFileArchiveWriter
        implements WriterInterface {

  /**
   * File counter
   */
  private int counter;

  /**
   * Configuration parameter - maximum size of an output archive
   */
  private final long LIMIT_SQL_ARCHIVE_SIZE;

  /**
   * Configuration parameter - Flag, that indicates whether the statistical
   * output is enabled or not
   */
  private final boolean MODE_STATISTICAL_OUTPUT;

  /**
   * Reference to the output stream
   */
  private OutputStream output;

  /**
   * Name of the related sql consumer - used as prefix for the output
   * filenames
   */
  private String outputName;

  /**
   * Configuration parameter - output path
   */
  private final String PATH_OUTPUT_SQL_FILES;

  /**
   * Reference to the output archive
   */
  private File dataArchive;

  /**
   * Reference to the SQLEncoder
   */
  protected DataFileEncoder dataFileEncoder;

  /**
   * (Constructor) Creates a new SQLArchiveWriter object.
   *
   * @throws ConfigurationException if an error occurred while accessing the configuration
   */
  private DataFileArchiveWriter()
          throws ConfigurationException {

    // Load config parameters
    ConfigurationManager config = ConfigurationManager.getInstance();

    LIMIT_SQL_ARCHIVE_SIZE = (Long) config
            .getConfigParameter(ConfigurationKeys.LIMIT_SQL_ARCHIVE_SIZE);

    PATH_OUTPUT_SQL_FILES = (String) config
            .getConfigParameter(ConfigurationKeys.PATH_OUTPUT_SQL_FILES);

    MODE_STATISTICAL_OUTPUT = (Boolean) config
            .getConfigParameter(ConfigurationKeys.MODE_STATISTICAL_OUTPUT);

    // Create sql file
    counter = 0;
  }


  /**
   * (Constructor) Creates a new SQLArchiveWriter object.
   *
   * @param outputName Name of the sql consumer
   * @throws ConfigurationException if an error occurred while accessing the configuration
   * @throws LoggingException       if an error occurred while accessing the logger
   */
  public DataFileArchiveWriter(final String outputName)
          throws IOException, ConfigurationException, LoggingException {

    this();

    this.outputName = outputName;

    init();
    writeHeader();
  }

  /**
   * This method will close the connection to the output.
   *
   * @throws IOException if problems occurred while closing the file or process.
   */
  @Override
  public void close()
          throws IOException {
    this.output.close();
    this.output = null;
  }

  /**
   * Creates the sql encoder.
   *
   * @throws ConfigurationException if an error occurred while accessing the configuration
   * @throws LoggingException       if an error occurred while accessing the logger
   */
  protected void init()
          throws ConfigurationException, LoggingException {

    this.dataFileEncoder = new DataFileEncoder();
  }

  /**
   * This method will process the given DiffTask and send it to the specified
   * output.
   *
   * @param task DiffTask
   * @throws ConfigurationException if problems occurred while initializing the components
   * @throws IOException            if problems occurred while writing the output (to file or
   *                                archive)
   * @throws SQLConsumerException   if problems occurred while writing the output (to the sql
   *                                producer database)
   */
  @Override
  public void process(final Task<Diff> task)
          throws ConfigurationException, IOException, SQLConsumerException {

    // this.startTime = System.currentTimeMillis();
    try {
      List<String> data = dataFileEncoder.encodeTask(task);

      for (String d : data) {
        this.output.write((d + ";").getBytes());
        this.output.flush();
      }

      if (task.getTaskType() == TaskTypes.TASK_FULL
              || task.getTaskType() == TaskTypes.TASK_PARTIAL_LAST) {

        if (this.dataArchive.length() > LIMIT_SQL_ARCHIVE_SIZE) {
          writeHeader();
        }

        if (!MODE_STATISTICAL_OUTPUT) {
          System.out.println(task);
        }

      } else {
        System.out.println(task);
      }

    } catch (EncodingException | DecodingException e) {
      throw ErrorFactory.createSQLConsumerException(
              ErrorKeys.DIFFTOOL_SQLCONSUMER_FILEWRITER_EXCEPTION, e);
    }
  }

  /**
   * Creates a new output file and writes the header information.
   *
   * @throws ConfigurationException if an error occurred while accessing the configuration
   * @throws IOException            if an error occurred while writing a file
   */
  protected void writeHeader()
          throws ConfigurationException, IOException {

    if (this.output != null) {
      close();
    }

    this.counter++;

    String filePath = PATH_OUTPUT_SQL_FILES + this.outputName + "_" + counter;

    this.output = OutputFactory.getOutputStream(filePath);

    this.dataArchive = new File(filePath);

    this.output.flush();
  }
}
