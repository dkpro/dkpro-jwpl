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
package org.dkpro.jwpl.revisionmachine.difftool;

import java.io.IOException;
import java.sql.SQLException;

import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ArticleReaderException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ConfigurationException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.DiffException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorFactory;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorKeys;
import org.dkpro.jwpl.revisionmachine.common.exceptions.LoggingException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.SQLConsumerException;
import org.dkpro.jwpl.revisionmachine.common.logging.Logger;
import org.dkpro.jwpl.revisionmachine.common.logging.LoggerType;
import org.dkpro.jwpl.revisionmachine.common.logging.LoggingFactory;
import org.dkpro.jwpl.revisionmachine.common.logging.messages.DiffToolLogMessages;
import org.dkpro.jwpl.revisionmachine.common.logging.messages.consumer.ArticleConsumerLogMessages;
import org.dkpro.jwpl.revisionmachine.common.logging.messages.consumer.DiffConsumerLogMessages;
import org.dkpro.jwpl.revisionmachine.common.logging.messages.consumer.SQLConsumerLogMessages;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.article.ArticleReaderInterface;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.article.reader.ArticleFilter;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.article.reader.InputFactory;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.DiffCalculatorInterface;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.TaskTransmitterInterface;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.calculation.DiffCalculator;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.calculation.TimedDiffCalculator;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.WriterInterface;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer.DataFileArchiveWriter;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer.DataFileWriter;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer.SQLArchiveWriter;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer.SQLDatabaseWriter;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer.SQLFileWriter;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer.TimedSQLArchiveWriter;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer.TimedSQLDatabaseWriter;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer.TimedSQLFileWriter;
import org.dkpro.jwpl.revisionmachine.difftool.data.OutputType;
import org.dkpro.jwpl.revisionmachine.difftool.data.archive.ArchiveDescription;
import org.dkpro.jwpl.revisionmachine.difftool.data.archive.ArchiveManager;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * This class represents the main method for the DiffTool application
 */
public class DiffToolThread
        extends Thread {

  /**
   * Reference to the DiffTool Logger
   */
  private static Logger logger;

  /**
   * Reference to the Configuration
   */
  private final ConfigurationManager cconfig;

  /**
   * Configuration Parameter - Statistical output flag
   */
  private boolean MODE_STATISTICAL_OUTPUT;

  /**
   * (Constructor) Creates a DiffToolThread object.
   *
   * @param config Reference to the configuration
   * @throws LoggingException if an error occurs while logging
   */
  public DiffToolThread(final ConfigSettings config)
          throws LoggingException {

    this.cconfig = new ConfigurationManager(config);

    try {
      MODE_STATISTICAL_OUTPUT = (Boolean) cconfig
              .getConfigParameter(ConfigurationKeys.MODE_STATISTICAL_OUTPUT);
    } catch (ConfigurationException e) {
      MODE_STATISTICAL_OUTPUT = false;
    }

    logger = LoggingFactory.createLogger(LoggerType.DIFF_TOOL, "DiffTool");
  }

  /**
   * This class is used to receive tasks from the diff modules and transmits
   * them to the sql modules.
   */
  private class TaskTransmitter
          implements TaskTransmitterInterface {

    /**
     * Reference to the (dump) output writer
     */
    private final WriterInterface dumpWriter;

    /**
     * Configuration Parameter - Output mode
     */
    private final OutputType MODE_OUTPUT;

    /**
     * Configuration Parameter - Statistical output flag
     */
    private final boolean MODE_STATISTICAL_OUTPUT;

    /**
     * Configuration Parameter - Datafile output flasg
     */
    private final boolean MODE_DATAFILE_OUTPUT;

    /**
     * (Constructor) Creates a TaskTransmitter object.
     *
     * @throws ConfigurationException if an error occurs while accessing the configuration
     * @throws IOException            if an error occurs while writing the output
     * @throws LoggingException       if an error occurs while logging
     */
    public TaskTransmitter()
            throws ConfigurationException, IOException, LoggingException {

      ConfigurationManager config = ConfigurationManager.getInstance();

      MODE_OUTPUT = (OutputType) config
              .getConfigParameter(ConfigurationKeys.MODE_OUTPUT);

      MODE_STATISTICAL_OUTPUT = (Boolean) cconfig
              .getConfigParameter(ConfigurationKeys.MODE_STATISTICAL_OUTPUT);

      MODE_DATAFILE_OUTPUT = (Boolean) cconfig
              .getConfigParameter(ConfigurationKeys.MODE_DATAFILE_OUTPUT);

      switch (MODE_OUTPUT) {

        case UNCOMPRESSED:
          if (MODE_DATAFILE_OUTPUT) {
            this.dumpWriter = new DataFileWriter("output");
          } else {
            if (MODE_STATISTICAL_OUTPUT) {
              this.dumpWriter = new TimedSQLFileWriter("output", logger);
            } else {
              this.dumpWriter = new SQLFileWriter("output", logger);
            }
          }
          break;

        case SEVENZIP:
        case BZIP2:
        case ALTERNATE:
          if (MODE_DATAFILE_OUTPUT) {
            this.dumpWriter = new DataFileArchiveWriter("output");
          } else {
            if (MODE_STATISTICAL_OUTPUT) {
              this.dumpWriter = new TimedSQLArchiveWriter("output", logger);
            } else {
              this.dumpWriter = new SQLArchiveWriter("output", logger);
            }
          }
          break;

        case DATABASE:
          if (MODE_DATAFILE_OUTPUT) {
            throw ErrorFactory
                    .createConfigurationException(ErrorKeys.DELTA_CONSUMERS_SQL_WRITER_OUTPUTFACTORY_ILLEGAL_OUTPUTMODE_VALUE);
          } else {
            if (MODE_STATISTICAL_OUTPUT) {
              this.dumpWriter = new TimedSQLDatabaseWriter(logger);
            } else {
              this.dumpWriter = new SQLDatabaseWriter(logger);
            }
          }
          break;

        default:
          throw ErrorFactory
                  .createConfigurationException(ErrorKeys.DELTA_CONSUMERS_SQL_WRITER_OUTPUTFACTORY_ILLEGAL_OUTPUTMODE_VALUE);
      }
    }

    /**
     * Receives a DiffTask Transmission.
     */
    @Override
    public void transmitDiff(final Task<Diff> result) {
      writeOutput(result);
    }

    /**
     * Receives a partial DiffTask Transmission.
     */
    @Override
    public void transmitPartialDiff(final Task<Diff> result) {
      writeOutput(result);
    }

    @Override
    public void close() throws IOException, SQLException {
      dumpWriter.close();
    }

    /**
     * Forwards the DiffTask to the encoding modules.
     *
     * @param result Reference to a DiffTask
     */
    private void writeOutput(final Task<Diff> result) {

      try {
        long time, start = System.currentTimeMillis();
        dumpWriter.process(result);

        time = System.currentTimeMillis() - start;

        SQLConsumerLogMessages.logDiffProcessed(logger, result, time);

        // Output Encoding Error
      } catch (SQLConsumerException e) {

        SQLConsumerLogMessages.logSQLConsumerException(logger, e);
        e.printStackTrace();

        // Critical Exceptions
      } catch (ConfigurationException | IOException e) {
        throw new RuntimeException(e);
      }
    }


  }

  /**
   * Runs the diff creation process
   */
  @Override
  public void run() {

    try {
      ArchiveManager archives = new ArchiveManager();
      ArticleReaderInterface articleReader;
      ArchiveDescription description = null;
      Task<Revision> task;
      DiffCalculatorInterface diffCalc;

      if (MODE_STATISTICAL_OUTPUT) {
        diffCalc = new TimedDiffCalculator(new TaskTransmitter());
      } else {
        diffCalc = new DiffCalculator(new TaskTransmitter());
      }

      long start, time;

      while (archives.hasArchive()) {


        // Retrieve Archive
        try {
          description = archives.getArchive();

          // initialize filter
          ArticleFilter nameFilter = new ArticleFilter();

          articleReader = InputFactory.getTaskReader(description,
                  nameFilter);
          ArticleConsumerLogMessages.logArchiveRetrieved(logger,
                  description);

          // Exception while accessing the archive
        } catch (ArticleReaderException e) {

          articleReader = null;
          ArticleConsumerLogMessages.logExceptionRetrieveArchive(
                  logger, description, e);
        }

        // Process Archive
        while (articleReader != null) {
          try {
            if (articleReader.hasNext()) {

              start = System.currentTimeMillis();
              //read the next article (may be null if filtered)
              task = articleReader.next();
              time = System.currentTimeMillis() - start;

              // task will be null if the name filter removed that
              // article
              if (task == null) {
                continue;
              }

              ArticleConsumerLogMessages
                      .logArticleRead(logger, task, time,
                              articleReader.getBytePosition());

              start = System.currentTimeMillis();
              //calculate the diff for this article version
              diffCalc.process(task);
              time = System.currentTimeMillis() - start;

              DiffConsumerLogMessages.logArticleProcessed(logger,
                      task, time);

            } else {
              ArticleConsumerLogMessages.logNoMoreArticles(
                      logger, description);
              articleReader = null;
            }

            // Reset current article
          } catch (ArticleReaderException e) {

            ArticleConsumerLogMessages.logTaskReaderException(
                    logger, e);
            articleReader.resetTaskCompleted();

          } catch (DiffException e) {

            DiffConsumerLogMessages.logDiffException(logger, e);
            articleReader.resetTaskCompleted();
            diffCalc.reset();
          }
        }
      }
      diffCalc.closeTransmitter();

      ArticleConsumerLogMessages.logNoMoreArchives(logger);

      // Critical Exceptions
    } catch (Exception e) {
      DiffToolLogMessages.logException(logger, e);
      throw new RuntimeException(e);
    }
  }
}
