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
	 * <p>
	 * Description: Surrogate Mode
	 */
	MODE_SURROGATES,

	/**
	 * Type: Integer Used by: SQLEncoder
	 * <p>
	 * Description: MaxAllowedPacket variable of the MySQL Server
	 */
	LIMIT_SQLSERVER_MAX_ALLOWED_PACKET,

	/**
	 * Type: OutputMode Used by: SQLConsumer
	 * <p>
	 * Description: Output Mode
	 */
	MODE_OUTPUT,

	/**
	 * Type: boolean Used by: RevisionApi
	 * <p>
	 * Description: Enables the zip compression
	 */
	MODE_ZIP_COMPRESSION_ENABLED,

	/**
	 * Type: boolean Used by: RevisionApi
	 * <p>
	 * Description: Enables the binary output
	 */
	MODE_BINARY_OUTPUT_ENABLED,

	/**
	 * Type: boolean Used by: All Consumers and the processing components
	 * <p>
	 * Description: Enables the statistical output
	 */
	MODE_STATISTICAL_OUTPUT,

	/**
	 * Type: boolean
	 * <p>
	 * Description: Write datafiles instead of SQL dumps
	 */
	MODE_DATAFILE_OUTPUT,

	/**
	 * Type: boolean Used by: All Consumers and the processing components
	 * <p>
	 * Description: Enables the debug output
	 */
	MODE_DEBUG_OUTPUT,

	/**
	 * Type: String Used by: everybody
	 * <p>
	 * Description: Charset name of the input data
	 * <p>
	 * Recommendation / Default: "UTF-8"
	 */
	WIKIPEDIA_ENCODING,

	/**
	 * Type: Integer Range: &gt; 1 Used by: DiffConsumers - Diff Generation
	 * <p>
	 * Description: This number indicates which revisions should be full
	 * revisions.
	 * <p>
	 * A full revision is generated if the result of the revisionCounter of the
	 * revision modulo COUNTER_FULL_REVISION is 0.
	 * <p>
	 * Recommendation / Default: Currenty a value of 1000 is used.
	 * <p>
	 * Example: COUNTER_FULL_REVISION = 100
	 * <p>
	 * FullRevisions are all revisions with a revisionCounter % 100 == 0 0, 100,
	 * 200, 300, 400, ...
	 */
	COUNTER_FULL_REVISION,

	/**
	 * Type: Integer Range: &gt; 1 Used by: DiffConsumers - Common Longest
	 * Substring Search
	 * <p>
	 * Description: This number indicates when a matching sequence between two
	 * revisions is considered as sequence.
	 * <p>
	 * Recommendation / Default: Currently 12 Value should be greater than the
	 * encoded size of an operation.
	 */
	VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING,


	/*
	 * +OUTPUT+VERIFICATION++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Type: String Used by: SQLConsumer - SQLFileWriter
	 * <p>
	 * Description: Output-Directory for the sql files
	 * <p>
	 * Recommendation / Default: No default value - has to be configured!
	 * <p>
	 * More consumers should lead to a speed-up
	 */
	PATH_OUTPUT_SQL_FILES,

	/**
	 * Type: Long Used by: SQLConsumer - SQLFileWriter
	 * <p>
	 * Description: Maximum size of an sql file (in bytes)
	 * <p>
	 * Recommendation / Default: Currently 100 MB
	 */
	LIMIT_SQL_FILE_SIZE,

	/**
	 * Type: Long Used by: SQLConsumer - SevenZipSQLWriter
	 * <p>
	 * Description: Maximum size of an sql archive file (in bytes)
	 * <p>
	 * Recommendation / Default: Currently not implemented
	 */
	LIMIT_SQL_ARCHIVE_SIZE,

	/**
	 * Type: Boolean Used by: DiffConsumer - DiffCalculator
	 * <p>
	 * Description: Enabels the verification of the diff generation
	 * <p>
	 * Recommendation / Default: Should only be used for debug purposes
	 */
	VERIFICATION_DIFF,

	/**
	 * Type: Boolean Used by: SQLConsumer - SQLFileWriter
	 * <p>
	 * Description: Enables the verification of the encoded revision data
	 * <p>
	 * Recommendation / Default: Should only be used for debug purposes
	 */
	VERIFICATION_ENCODING,


	/*
	 * +RESOURCE+LIMITATIONS+++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Type: Long Used by: ArticleConsumers
	 * <p>
	 * Description: This value indicates the maximum size of an article task (in
	 * bytes). If the limit is reached the task will be splitted into parts.
	 * <p>
	 * Recommendation / Default: Currently 10 MB
	 * <p>
	 * USE WITH CAUTION! Large value could lead to a memory overflow
	 */
	LIMIT_TASK_SIZE_REVISIONS,

	/**
	 * Type: Long Used by: DiffConsumers
	 * <p>
	 * Description: This value indicates the maximum size of a diff task (in
	 * bytes). If the limit is reached the task will be splitted into parts.
	 * <p>
	 * Recommendation / Default: Currently 10 MB
	 * <p>
	 * USE WITH CAUTION! Large value could lead to a memory overflow
	 */
	LIMIT_TASK_SIZE_DIFFS,


	/*
	 * +EXTERNAL+PROGRAMS++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Type: String Used by: ArticleConsumers - ArticleReader - InputFactory
	 * <p>
	 * Description: If you want to use 7Zip to decompress your 7z or bz2
	 * archives set the corresponding path in the config file.
	 * <p>
	 * Recommendation / Default: not set, faster than bzip2
	 */
	PATH_PROGRAM_7ZIP,


	/*
	 * +UNCOMPRESSED+SERVER+SETTINGS++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	SQL_HOST,

	/**
	 * Type: String Used by: SQLConsumers - SQLDatabaseWriter
	 * <p>
	 * Description: Name of the sql database
	 * <p>
	 * Recommendation / Default: currently not used
	 */
	SQL_DATABASE,

	/**
	 * Type: String Used by: SQLConsumers - SQLDatabaseWriter
	 * <p>
	 * Description: Username of your sql producer
	 * <p>
	 * Recommendation / Default: currently not used
	 */
	SQL_USERNAME,

	/**
	 * Type: String Used by: SQLConsumers - SQLDatabaseWriter
	 * <p>
	 * Description: Password for the corresponding username
	 * <p>
	 * Recommendation / Default: currently not used
	 */
	SQL_PASSWORD,

	/*
	 * +LOGGING++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Type: String Used by: All Loggers
	 * <p>
	 * Description: Root-Directory for all logger
	 * <p>
	 * Recommendation / Default: "logs/"
	 */
	LOGGING_PATH_DIFFTOOL,

	/**
	 * Type: String Used by: DiffConsumer, SQLConsumer
	 * <p>
	 * Description: Output directory for articles with failed verifications
	 * <p>
	 * Recommendation / Default: "logs/" + "debug/"
	 */
	LOGGING_PATH_DEBUG,

	/**
	 * Type: {@link org.slf4j.event.Level} Used by: DiffTool Logger
	 * <p>
	 * Description: Log level for the diff tool logger
	 * <p>
	 * Recommendation / Default: Log.INFO
	 * <p>
	 * Note that the corresponding output directory for the logger has to exist
	 * when the LogLevel is not Level.OFF
	 */
	LOGGING_LOGLEVEL_DIFFTOOL,

	/**
	 * Type: java.util.Set Used by: ArticleFilter
	 * <p>
	 * Description: The Set of namespaces to keep in output
	 *
	 */
	NAMESPACES_TO_KEEP
}
