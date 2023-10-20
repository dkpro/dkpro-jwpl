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
package org.dkpro.jwpl.revisionmachine.common.logging;

import java.io.FileWriter;
import java.io.IOException;

import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorFactory;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorKeys;
import org.dkpro.jwpl.revisionmachine.common.exceptions.LoggingException;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.slf4j.event.Level;

/**
 * DiffTool Logger class
 *
 */
public class Logger
{

	/** Name of the logger */
	private final String consumerName;

	/** Reference to level of the logging */
	private final Level logLevel;

	/** Type of the logger */
	private final LoggerType type;

	/** Reference to the output writer */
	private final FileWriter writer;

	/**
	 * Creates a new logger.
	 *
	 * @param type
	 *            type
	 * @param consumerName
	 *            name
	 * @throws LoggingException
	 *             if an error occurred
	 */
	public Logger(final LoggerType type, final String consumerName)
		throws LoggingException
	{

		try {
			this.type = type;
			this.consumerName = consumerName;

			ConfigurationManager config = ConfigurationManager.getInstance();
			String path = (String) config
					.getConfigParameter(ConfigurationKeys.LOGGING_PATH_DIFFTOOL);

			switch (type) {
			case ARTICLE_OUTPUT:
				logLevel = Level.INFO;
				break;
			case DIFF_TOOL_ERROR:
				logLevel = Level.ERROR;
				break;
			case DIFF_TOOL:
				logLevel = (Level) config
						.getConfigParameter(ConfigurationKeys.LOGGING_LOGLEVEL_DIFFTOOL);
				break;
			default:
				throw ErrorFactory
						.createLoggingException(ErrorKeys.LOGGING_LOGGER_INITIALIZISATION_FAILED);
			}

			this.writer = new FileWriter(path + consumerName + ".log");

		}
		catch (Exception e) {
			throw ErrorFactory.createLoggingException(
					ErrorKeys.LOGGING_LOGGER_INITIALIZISATION_FAILED, e);
		}
	}

	/**
	 * Closes the output writer.
	 */
	public synchronized void close()
	{
		try {
			writer.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Flushes the buffered output of the writer to the file.
	 */
	public synchronized void flush()
	{
		try {
			writer.flush();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Returns the log level.
	 *
	 * @return log level
	 */
	public Level getLogLevel()
	{
		return this.logLevel;
	}

	/**
	 * Writes the given text to the output file.
	 *
	 * @param text
	 *            log message
	 */
	private synchronized void log(final String text)
	{

		try {
			this.writer.write(text);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * The occurred error with the related log level and message has to be given
	 * to this method.
	 * <p>
	 * This method will verify if the message should be logged or not.
	 *
	 * @param level
	 *            log level
	 * @param message
	 *            message
	 * @param e
	 *            Error
	 */
	public void logError(final Level level, final String message, final Error e)
	{
		try {
			Logger errors = LoggingFactory
					.getLogger(LoggingFactory.NAME_ERROR_LOGGER);

			errors.logThrowable(level, message, e);

		}
		catch (LoggingException ex) {
			ex.printStackTrace();
		}

		if (logLevel.toInt() > level.toInt()) {
			return;
		}

		logThrowable(level, message, e);
	}

	/**
	 * The occurred exception with the related log level and message has to be
	 * given to this method.
	 * <p>
	 * This method will verify if the message should be logged or not.
	 *
	 * @param level
	 *            log level
	 * @param message
	 *            message
	 * @param e
	 *            Exception
	 */
	public void logException(final Level level, final String message,
			final Exception e)
	{

		try {
			Logger errors = LoggingFactory
					.getLogger(LoggingFactory.NAME_ERROR_LOGGER);

			errors.logThrowable(level, message, e);

		}
		catch (LoggingException ex) {
			ex.printStackTrace();
		}

		if (logLevel.toInt() > level.toInt()) {
			return;
		}

		logThrowable(level, message, e);
	}

	/**
	 * This method will be called with a message and the related log level. It
	 * be verified if the message should be logged or not.
	 * <p>
	 * The format of the logged message is: \t consumerName [ Type of Logger ]
	 * \t message \r\n
	 *
	 * @param level
	 *            level
	 * @param message
	 *            message
	 */
	public synchronized void logMessage(final Level level, final String message)
	{

		if (logLevel.toInt() > level.toInt()) {
			return;
		}

		try {
			this.writer.write(System.currentTimeMillis() + "\t" + consumerName
					+ " [" + type.toString() + "] " + "\t" + message + "\r\n");
			this.writer.flush();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * The occurred error or exception with the related log level and message
	 * will be logged by this method.
	 *
	 * @param level
	 *            log level
	 * @param message
	 *            message
	 * @param t
	 *            Throwable
	 */
	private synchronized void logThrowable(final Level level,
			final String message, final Throwable t)
	{

		if (t != null) {
			log("\r\n[" + System.currentTimeMillis() + "]\t" + message);
			log("\r\n" + t);
			log("\r\n");

			for (StackTraceElement st : t.getStackTrace()) {
				log("\t" + st.toString() + "\r\n");
			}

			Throwable c = t.getCause();
			if (c != null) {

				log("Caused by:\t" + c + "\r\n");

				for (StackTraceElement st : c.getStackTrace()) {
					log("\t" + st.toString() + "\r\n");
				}
			}

			log("\r\n");
			this.flush();
		}
	}
}
