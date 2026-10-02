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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.dkpro.jwpl.revisionmachine.difftool.data.archive.ArchiveScheduler;
import org.dkpro.jwpl.revisionmachine.difftool.data.archive.ArchiveScheduler.ArchiveJob;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.slf4j.event.Level;

/**
 * This class represents the main method for the DiffTool application
 */
public class DiffToolThread
    extends Thread
{

    /**
     * Name prefix of the output files
     */
    private static final String OUTPUT_NAME = "output";

    /**
     * Reference to the DiffTool Logger, shared by all workers (its methods are synchronized)
     */
    private final Logger logger;

    /**
     * Reference to the Configuration
     */
    private final ConfigurationManager cconfig;

    /**
     * Configuration Parameter - Statistical output flag
     */
    private boolean MODE_STATISTICAL_OUTPUT;

    /**
     * Configuration Parameter - Number of archives processed in parallel
     */
    private final int LIMIT_ARCHIVE_THREADS;

    /**
     * Whether an archive, article or diff was skipped because of an error; set by all workers
     */
    private volatile boolean failed;

    /**
     * (Constructor) Creates a DiffToolThread object.
     *
     * @param config
     *            Reference to the configuration
     * @throws LoggingException
     *             if an error occurs while logging
     */
    public DiffToolThread(final ConfigSettings config) throws LoggingException
    {

        this.cconfig = new ConfigurationManager(config);

        try {
            MODE_STATISTICAL_OUTPUT = (Boolean) cconfig
                    .getConfigParameter(ConfigurationKeys.MODE_STATISTICAL_OUTPUT);
        }
        catch (ConfigurationException e) {
            // The parameter is optional: if it is not configured, the statistical output stays
            // disabled. The exception carries no information beyond that, hence it is not chained.
            MODE_STATISTICAL_OUTPUT = false;
        }

        int threads;
        try {
            threads = (Integer) cconfig.getConfigParameter(ConfigurationKeys.LIMIT_ARCHIVE_THREADS);
        }
        catch (ConfigurationException e) {
            // Optional parameter: without it, the archives are processed one after another.
            threads = 1;
        }
        LIMIT_ARCHIVE_THREADS = threads;

        logger = LoggingFactory.createLogger(LoggerType.DIFF_TOOL, "DiffTool");
    }

    /**
     * This class is used to receive tasks from the diff modules and transmits them to the SQL
     * modules.
     */
    private class TaskTransmitter
        implements TaskTransmitterInterface
    {

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
         * @param outputName
         *            prefix of the output files (not used by the DATABASE output mode)
         * @throws ConfigurationException
         *             if an error occurs while accessing the configuration
         * @throws IOException
         *             if an error occurs while writing the output
         * @throws LoggingException
         *             if an error occurs while logging
         */
        public TaskTransmitter(final String outputName)
            throws ConfigurationException, IOException, LoggingException
        {

            ConfigurationManager config = ConfigurationManager.getInstance();

            MODE_OUTPUT = (OutputType) config.getConfigParameter(ConfigurationKeys.MODE_OUTPUT);
            MODE_STATISTICAL_OUTPUT = (Boolean) cconfig
                    .getConfigParameter(ConfigurationKeys.MODE_STATISTICAL_OUTPUT);
            MODE_DATAFILE_OUTPUT = (Boolean) cconfig
                    .getConfigParameter(ConfigurationKeys.MODE_DATAFILE_OUTPUT);

            switch (MODE_OUTPUT) {

            case UNCOMPRESSED:
                if (MODE_DATAFILE_OUTPUT) {
                    this.dumpWriter = new DataFileWriter(outputName);
                }
                else {
                    if (MODE_STATISTICAL_OUTPUT) {
                        this.dumpWriter = new TimedSQLFileWriter(outputName, logger);
                    }
                    else {
                        this.dumpWriter = new SQLFileWriter(outputName, logger);
                    }
                }
                break;

            case SEVENZIP:
            case BZIP2:
            case ALTERNATE:
                if (MODE_DATAFILE_OUTPUT) {
                    this.dumpWriter = new DataFileArchiveWriter(outputName);
                }
                else {
                    if (MODE_STATISTICAL_OUTPUT) {
                        this.dumpWriter = new TimedSQLArchiveWriter(outputName, logger);
                    }
                    else {
                        this.dumpWriter = new SQLArchiveWriter(outputName, logger);
                    }
                }
                break;

            case DATABASE:
                if (MODE_DATAFILE_OUTPUT) {
                    throw ErrorFactory.createConfigurationException(
                            ErrorKeys.DELTA_CONSUMERS_SQL_WRITER_OUTPUTFACTORY_ILLEGAL_OUTPUTMODE_VALUE);
                }
                else {
                    if (MODE_STATISTICAL_OUTPUT) {
                        this.dumpWriter = new TimedSQLDatabaseWriter(logger);
                    }
                    else {
                        this.dumpWriter = new SQLDatabaseWriter(logger);
                    }
                }
                break;

            default:
                throw ErrorFactory.createConfigurationException(
                        ErrorKeys.DELTA_CONSUMERS_SQL_WRITER_OUTPUTFACTORY_ILLEGAL_OUTPUTMODE_VALUE);
            }
        }

        /**
         * Receives a DiffTask Transmission.
         */
        @Override
        public void transmitDiff(final Task<Diff> result)
        {
            writeOutput(result);
        }

        /**
         * Receives a partial DiffTask Transmission.
         */
        @Override
        public void transmitPartialDiff(final Task<Diff> result)
        {
            writeOutput(result);
        }

        @Override
        public void close() throws IOException, SQLException
        {
            dumpWriter.close();
        }

        /**
         * Forwards the DiffTask to the encoding modules.
         *
         * @param result
         *            Reference to a DiffTask
         */
        private void writeOutput(final Task<Diff> result)
        {

            try {
                long time, start = System.currentTimeMillis();
                dumpWriter.process(result);

                time = System.currentTimeMillis() - start;

                SQLConsumerLogMessages.logDiffProcessed(logger, result, time);

                // Output Encoding Error
            }
            catch (SQLConsumerException e) {

                SQLConsumerLogMessages.logSQLConsumerException(logger, e);
                failed = true;

                // Critical Exceptions
            }
            catch (ConfigurationException | IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Runs the diff creation process.
     * <p>
     * An archive, article or diff that cannot be read or processed is logged and skipped, and the
     * remaining input is still processed.
     *
     * @throws IllegalStateException
     *             if any input was skipped because of an error
     * @throws RuntimeException
     *             if a critical error aborted the process, or, with several archive threads, if
     *             any archive could not be processed; the other archives are processed first
     */
    @Override
    public void run()
    {
        try {
            if (LIMIT_ARCHIVE_THREADS > 1 && !isDatabaseOutput()) {
                runParallel();
            }
            else {
                if (LIMIT_ARCHIVE_THREADS > 1) {
                    logger.logMessage(Level.WARN, "The DATABASE output mode uses a single "
                            + "connection: ignoring LIMIT_ARCHIVE_THREADS = "
                            + LIMIT_ARCHIVE_THREADS + " and processing the archives sequentially");
                }
                runSequential();
            }
        }
        finally {
            logger.flush();
        }

        if (failed) {
            throw new IllegalStateException(
                    "Input was skipped because of errors, see the DiffTool error log.");
        }
    }

    /**
     * Processes all archives one after another and writes them to the same output.
     */
    private void runSequential()
    {

        DiffCalculatorInterface diffCalc = null;
        try {
            ArchiveManager archives = new ArchiveManager();
            ArchiveDescription description;

            diffCalc = createDiffCalculator(OUTPUT_NAME);

            while ((description = archives.getArchive()) != null) {
                processArchive(description, diffCalc);
            }
            diffCalc.closeTransmitter();

            ArticleConsumerLogMessages.logNoMoreArchives(logger);

            // Critical Exceptions
        }
        catch (Exception e) {
            DiffToolLogMessages.logException(logger, e);
            if (diffCalc != null) {
                // write out the buffered output produced before the failure
                try {
                    diffCalc.closeTransmitter();
                }
                catch (Exception ce) {
                    e.addSuppressed(ce);
                }
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Processes the archives on {@link #LIMIT_ARCHIVE_THREADS} worker threads. Every archive gets
     * its own diff calculator and writer, and its own output files named after the archive, so
     * the workers share no mutable state and the output does not depend on the scheduling.
     */
    private void runParallel()
    {
        Map<ArchiveJob, Throwable> failures;
        int archiveCount;
        try {
            List<ArchiveDescription> archives = new ArrayList<>();
            ArchiveManager manager = new ArchiveManager();
            ArchiveDescription description;
            while ((description = manager.getArchive()) != null) {
                archives.add(description);
            }

            List<ArchiveJob> jobs = ArchiveScheduler.plan(archives, OUTPUT_NAME + "_");
            archiveCount = jobs.size();
            logger.logMessage(Level.INFO, "Processing " + archiveCount + " archives on "
                    + Math.min(LIMIT_ARCHIVE_THREADS, Math.max(1, archiveCount)) + " threads");

            failures = ArchiveScheduler.run(jobs, LIMIT_ARCHIVE_THREADS, this::processJob);

            ArticleConsumerLogMessages.logNoMoreArchives(logger);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            DiffToolLogMessages.logException(logger, e);
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            DiffToolLogMessages.logException(logger, e);
            throw new RuntimeException(e);
        }

        // the failures have already been logged by the workers
        if (!failures.isEmpty()) {
            RuntimeException e = new RuntimeException(failures.size() + " of " + archiveCount
                    + " archives could not be processed: " + failures.keySet().stream()
                            .map(job -> job.archive().getPath()).toList());
            failures.values().forEach(e::addSuppressed);
            throw e;
        }
    }

    /**
     * Processes one archive with its own diff calculator and output writer.
     *
     * @param job
     *            the archive and its output name
     * @throws Exception
     *             if a critical error occurred; the output written so far is closed
     */
    private void processJob(final ArchiveJob job) throws Exception
    {
        DiffCalculatorInterface diffCalc = createDiffCalculator(job.outputName());
        try {
            processArchive(job.archive(), diffCalc);
        }
        catch (Exception e) {
            DiffToolLogMessages.logException(logger, e);
            try {
                diffCalc.closeTransmitter();
            }
            catch (Exception ce) {
                e.addSuppressed(ce);
            }
            throw e;
        }
        diffCalc.closeTransmitter();
    }

    /**
     * @return whether the output is written directly to a database
     */
    private boolean isDatabaseOutput()
    {
        try {
            return cconfig.getConfigParameter(ConfigurationKeys.MODE_OUTPUT) == OutputType.DATABASE;
        }
        catch (ConfigurationException e) {
            // reported by the TaskTransmitter when the output is created
            return false;
        }
    }

    /**
     * Creates a diff calculator that writes to a new output.
     *
     * @param outputName
     *            prefix of the output files
     * @return the diff calculator
     */
    private DiffCalculatorInterface createDiffCalculator(final String outputName)
        throws ConfigurationException, IOException, LoggingException
    {
        if (MODE_STATISTICAL_OUTPUT) {
            return new TimedDiffCalculator(new TaskTransmitter(outputName));
        }
        return new DiffCalculator(new TaskTransmitter(outputName));
    }

    /**
     * Reads all articles of one archive and passes them to the diff calculator.
     *
     * @param description
     *            the archive
     * @param diffCalc
     *            the diff calculator
     */
    private void processArchive(final ArchiveDescription description,
            final DiffCalculatorInterface diffCalc)
        throws Exception
    {
        ArticleReaderInterface articleReader;
        Task<Revision> task;
        long start, time;

        // Retrieve Archive
        try {
            // initialize filter
            ArticleFilter nameFilter = new ArticleFilter();

            articleReader = InputFactory.getTaskReader(description, nameFilter);
            ArticleConsumerLogMessages.logArchiveRetrieved(logger, description);

            // Exception while accessing the archive
        }
        catch (ArticleReaderException e) {

            articleReader = null;
            ArticleConsumerLogMessages.logExceptionRetrieveArchive(logger, description, e);
            failed = true;
        }

        // Process Archive
        while (articleReader != null) {
            try {
                if (articleReader.hasNext()) {

                    start = System.currentTimeMillis();
                    // read the next article (may be null if filtered)
                    task = articleReader.next();
                    time = System.currentTimeMillis() - start;

                    // task will be null if the name filter removed that
                    // article
                    if (task == null) {
                        continue;
                    }

                    ArticleConsumerLogMessages.logArticleRead(logger, task, time,
                            articleReader.getBytePosition());

                    start = System.currentTimeMillis();
                    // calculate the diff for this article version
                    diffCalc.process(task);
                    time = System.currentTimeMillis() - start;

                    DiffConsumerLogMessages.logArticleProcessed(logger, task, time);

                }
                else {
                    ArticleConsumerLogMessages.logNoMoreArticles(logger, description);
                    articleReader = null;
                }

                // Reset current article
            }
            catch (ArticleReaderException e) {

                ArticleConsumerLogMessages.logTaskReaderException(logger, e);
                articleReader.resetTaskCompleted();
                failed = true;

            }
            catch (DiffException e) {

                DiffConsumerLogMessages.logDiffException(logger, e);
                articleReader.resetTaskCompleted();
                diffCalc.reset();
                failed = true;
            }
        }
    }
}
