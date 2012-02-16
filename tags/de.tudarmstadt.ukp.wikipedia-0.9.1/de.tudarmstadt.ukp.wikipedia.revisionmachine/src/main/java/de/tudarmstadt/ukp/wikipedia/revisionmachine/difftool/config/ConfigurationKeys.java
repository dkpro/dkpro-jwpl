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

/**
 * Contains all applicable keys for the configuration file.
 *
 *
 *
 */
public enum ConfigurationKeys
{

	/*
	 * +DIVERSES+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */


	/**
	 * Type: SurrogateModes Used by: DiffCalculator, RevisionApi
	 *
	 * Description: Surrogate Mode
	 */
	MODE_SURROGATES,

	/**
	 * Type: Integer Used by: SQLEncoder
	 *
	 * Description: MaxAllowedPacket variable of the MySQL Server
	 */
	LIMIT_SQLSERVER_MAX_ALLOWED_PACKET,

	/**
	 * Type: OutputMode Used by: SQLConsumer
	 *
	 * Description: Output Mode
	 */
	MODE_OUTPUT,

	/**
	 * Type: boolean Used by: RevisionApi
	 *
	 * Description: Enables the zip compression
	 */
	MODE_ZIP_COMPRESSION_ENABLED,

	/**
	 * Type: boolean Used by: RevisionApi
	 *
	 * Description: Enables the binary output
	 */
	MODE_BINARY_OUTPUT_ENABLED,

	/**
	 * Type: boolean Used by: All Consumers and the processing components
	 *
	 * Description: Enables the statistical output
	 */
	MODE_STATISTICAL_OUTPUT,

	/**
	 * Type: boolean
	 *
	 * Description: Write datafiles instead of SQL dumps
	 */
	MODE_DATAFILE_OUTPUT,

	/**
	 * Type: boolean Used by: All Consumers and the processing components
	 *
	 * Description: Enables the debug output
	 */
	MODE_DEBUG_OUTPUT,

	/**
	 * Type: String Used by: everybody
	 *
	 * Description: Charset name of the input data
	 *
	 * Recommendation / Default: "UTF-8"
	 */
	WIKIPEDIA_ENCODING,

	/**
	 * Type: Integer Range: > 1 Used by: DiffConsumers - Diff Generation
	 *
	 * Description: This number indicates which revisions should be full
	 * revisions.
	 *
	 * A full revision is generated if the result of the revisionCounter of the
	 * revision modulo COUNTER_FULL_REVISION is 0.
	 *
	 * Recommendation / Default: Currenty a value of 1000 is used.
	 *
	 * Example: COUNTER_FULL_REVISION = 100
	 *
	 * FullRevisions are all revisions with a revisionCounter % 100 == 0 0, 100,
	 * 200, 300, 400, ...
	 */
	COUNTER_FULL_REVISION,

	/**
	 * Type: Integer Range: > 1 Used by: DiffConsumers - Common Longest
	 * Substring Search
	 *
	 * Description: This number indicates when a matching sequence between two
	 * revisions is considered as sequence.
	 *
	 * Recommendation / Default: Currently 12 Value should be greater than the
	 * encoded size of an operation.
	 */
	VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING,


	/*
	 * +OUTPUT+VERIFICATION++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Type: String Used by: SQLConsumer - SQLFileWriter
	 *
	 * Description: Output-Directory for the sql files
	 *
	 * Recommendation / Default: No default value - has to be configured!
	 *
	 * More consumers should lead to a speed-up
	 */
	PATH_OUTPUT_SQL_FILES,

	/**
	 * Type: Long Used by: SQLConsumer - SQLFileWriter
	 *
	 * Description: Maximum size of an sql file (in bytes)
	 *
	 * Recommendation / Default: Currently 100 MB
	 */
	LIMIT_SQL_FILE_SIZE,

	/**
	 * Type: Long Used by: SQLConsumer - SevenZipSQLWriter
	 *
	 * Description: Maximum size of an sql archive file (in bytes)
	 *
	 * Recommendation / Default: Currently not implemented
	 */
	LIMIT_SQL_ARCHIVE_SIZE,

	/**
	 * Type: Boolean Used by: DiffConsumer - DiffCalculator
	 *
	 * Description: Enabels the verification of the diff generation
	 *
	 * Recommendation / Default: Should only be used for debug purposes
	 */
	VERIFICATION_DIFF,

	/**
	 * Type: Boolean Used by: SQLConsumer - SQLFileWriter
	 *
	 * Description: Enables the verification of the encoded revision data
	 *
	 * Recommendation / Default: Should only be used for debug purposes
	 */
	VERIFICATION_ENCODING,


	/*
	 * +RESOURCE+LIMITATIONS+++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Type: Long Used by: ArticleConsumers
	 *
	 * Description: This value indicates the maximum size of an article task (in
	 * bytes). If the limit is reached the task will be splitted into parts.
	 *
	 * Recommendation / Default: Currently 10 MB
	 *
	 * USE WITH CAUTION! Large value could lead to a memory overflow
	 */
	LIMIT_TASK_SIZE_REVISIONS,

	/**
	 * Type: Long Used by: DiffConsumers
	 *
	 * Description: This value indicates the maximum size of a diff task (in
	 * bytes). If the limit is reached the task will be splitted into parts.
	 *
	 * Recommendation / Default: Currently 10 MB
	 *
	 * USE WITH CAUTION! Large value could lead to a memory overflow
	 */
	LIMIT_TASK_SIZE_DIFFS,


	/*
	 * +EXTERNAL+PROGRAMS++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Type: String Used by: ArticleConsumers - ArticleReader - InputFactory
	 *
	 * Description: If you want to use 7Zip to decompress your 7z or bz2
	 * archives set the corresponding path in the config file.
	 *
	 * Recommendation / Default: not set, faster than bzip2
	 */
	PATH_PROGRAM_7ZIP,


	/*
	 * +UNCOMPRESSED+SERVER+SETTINGS++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	SQL_HOST,

	/**
	 * Type: String Used by: SQLConsumers - SQLDatabaseWriter
	 *
	 * Description: Name of the sql database
	 *
	 * Recommendation / Default: currently not used
	 */
	SQL_DATABASE,

	/**
	 * Type: String Used by: SQLConsumers - SQLDatabaseWriter
	 *
	 * Description: Username of your sql producer
	 *
	 * Recommendation / Default: currently not used
	 */
	SQL_USERNAME,

	/**
	 * Type: String Used by: SQLConsumers - SQLDatabaseWriter
	 *
	 * Description: Password for the corresponding username
	 *
	 * Recommendation / Default: currently not used
	 */
	SQL_PASSWORD,

	/*
	 * +LOGGING++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Type: String Used by: All Loggers
	 *
	 * Description: Root-Directory for all logger
	 *
	 * Recommendation / Default: "logs/"
	 */
	LOGGING_PATH_DIFFTOOL,

	/**
	 * Type: String Used by: DiffConsumer, SQLConsumer
	 *
	 * Description: Output directory for articles with failed verifications
	 *
	 * Recommendation / Default: "logs/" + "debug/"
	 */
	LOGGING_PATH_DEBUG,

	/**
	 * Type: java.util.logging.Level Used by: DiffTool Logger
	 *
	 * Description: Log level for the diff tool logger
	 *
	 * Recommendation / Default: Log.INFO
	 *
	 * Note that the corresponding output directory for the logger has to exist
	 * when the LogLevel is not Level.OFF
	 */
	LOGGING_LOGLEVEL_DIFFTOOL,

	/**
	 * Type: java.util.Set Used by: ArticleFilter
	 *
	 * Description: The Set of namespaces to keep in output
	 *
	 */
	NAMESPACES_TO_KEEP
}
