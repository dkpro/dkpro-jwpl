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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions;

/**
 * This class contains an enumeration of the possible error sources.
 * 
 * 
 * 
 */
public enum ErrorKeys
{

	/** The configuration manager has not been created */
	CONFIGURATION_CONFIGURATIONMANAGER_NOT_INITIALIZED,

	/** An unknown configuration parameter was requested */
	CONFIGURATION_CONFIGURATIONMANAGER_UNKNOWN_CONFIG_PARAMETER,

	/** An undefined parameter was requested */
	CONFIGURATION_PARAMETER_UNDEFINED,

	/** An IOException occurred while parsing the xml input */
	DELTA_CONSUMERS_TASK_READER_WIKIPEDIAXMLREADER_IOEXCEPTION,

	/** An keyword was found were it was not supposed to be */
	DELTA_CONSUMERS_TASK_READER_WIKIPEDIAXMLREADER_UNEXPECTED_KEYWORD,

	/**
	 * The end of the file was reached, but the parsing process was not finished
	 */
	DELTA_CONSUMERS_TASK_READER_WIKIPEDIAXMLREADER_UNEXPECTED_END_OF_FILE,

	DELTA_CONSUMERS_TASK_READER_INPUTFACTORY_ILLEGAL_INPUTMODE_VALUE,

	DELTA_CONSUMERS_SQL_CODEC_BITREADER_READ_OPERATION_OUT_OF_RANGE, DELTA_CONSUMERS_SQL_CODEC_BITREADER_READ_OPERATION_AFTER_END_OF_STREAM,

	DELTA_CONSUMERS_SQL_CODEC_BITWRITER_WRITE_OPERATOR_OUT_OF_RANGE, DELTA_CONSUMERS_SQL_CODEC_BITWRITER_INVALID_WRITE_OPERATION,

	DELTA_CONSUMERS_SQL_WRITER_OUTPUTFACTORY_ILLEGAL_OUTPUTMODE_VALUE,

	DIFFTOOL_DIFFCONSUMER_DIFF_VERIFICATION_FAILED,

	DIFFTOOL_SQLCONSUMER_ENCODING_VERIFICATION_FAILED, DIFFTOOL_SQLCONSUMER_DATABASEWRITER_EXCEPTION, DIFFTOOL_SQLCONSUMER_FILEWRITER_EXCEPTION,

	DIFFTOOL_ENCODING_INVALID_VALUE, DIFFTOOL_ENCODING_VALUE_OUT_OF_RANGE,

	DIFFTOOL_DECODING_INVALID_VALUE, DIFFTOOL_DECODING_VALUE_OUT_OF_RANGE, DIFFTOOL_DECODING_UNEXPECTED_END_OF_STREAM,

	LOGGING_LOGGER_INITIALIZISATION_FAILED, LOGGING_LOGGINGFACTORY_NO_SUCH_LOGGER, LOGGING_LOGGINGFACTORY_LOGGER_ALREADY_EXIST,

	ABSTRACT_CONSUMER_TIMEOUT
}
