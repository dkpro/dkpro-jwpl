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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.api;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.OutputTypes;

/**
 * This class contains the additional parameters for the {@link RevisionApi}.
 */
public class RevisionAPIConfiguration extends DatabaseConfiguration
{

	/** Number of maximum size of an result set */
	private int bufferSize;

	/** Character encoding */
	private String characterSet;

	/** Memory size for the storage of revisions for the chonological iteration */
	private long chronoStorageSpace;

	/**
	 * MAX_ALLOWED_PACKET - parameter of the MySQL Server This value indicates
	 * the maximum size of an sql query.
	 */
	private long maxAllowedPacket;

	/** Path for the IndexGenerator output */
	private String outputPath;

	/** Type of the IndexGenerator output */
	private OutputTypes outputType;

	/**
	 * <p>(Constructor) Creates the default configuration.</p>
	 * OutputType: UNCOMPRESSED (revisionIndex.sql)<br>
	 *
	 */
	public RevisionAPIConfiguration()
	{

		super();
		this.setHost("localhost");

		characterSet = "UTF-8";
		maxAllowedPacket = 1024 * 1023;
		bufferSize = 10000;

		chronoStorageSpace = 100 * 1024 * 1024;

		outputPath = "revisionIndex.sql";
		outputType = OutputTypes.SQL;
	}

	/**
	 * <p>Creates a (default) RevisionAPIConfiguration from an existing
	 * DatabaseConfiguration.</p>
	 *
	 * OutputType: DATABASE<br>
	 */
	public RevisionAPIConfiguration(DatabaseConfiguration existingWikiConfig)
	{

		super();

		characterSet = "UTF-8";
		maxAllowedPacket = 1024 * 1023;
		bufferSize = 10000;

		chronoStorageSpace = 100 * 1024 * 1024;

		outputType = OutputTypes.DATABASE;

		setHost(existingWikiConfig.getHost());
		setDatabase(existingWikiConfig.getDatabase());
		setDatabaseDriver(existingWikiConfig.getDatabaseDriver());
		setJdbcURL(existingWikiConfig.getJdbcURL());
		setUser(existingWikiConfig.getUser());
		setPassword(existingWikiConfig.getPassword());
		setLanguage(existingWikiConfig.getLanguage());

	}


	/**
	 * Returns the maximum size of a result set.
	 *
	 * @return maximum size of a result set
	 */
	public int getBufferSize()
	{
		return bufferSize;
	}

	/**
	 * Returns the character encoding.
	 *
	 * @return character encoding
	 */
	public String getCharacterSet()
	{
		return characterSet;
	}

	/**
	 * Returns the memory size used for the purpose of storing revisions.
	 *
	 * @return memory size
	 */
	public long getChronoStorageSpace()
	{
		return this.chronoStorageSpace;
	}

	/**
	 * Returns the value of MAX_ALLOWED_PACKET parameter.
	 *
	 * @return MAX_ALLOWED_PACKET
	 */
	public long getMaxAllowedPacket()
	{
		return maxAllowedPacket;
	}

	/**
	 * Returns the output path of the index generator.
	 *
	 * @return output path
	 */
	public String getOutputPath()
	{
		return outputPath;
	}

	/**
	 * Returns the output type of the index generator.
	 *
	 * @return output type
	 */
	public OutputTypes getOutputType()
	{
		return outputType;
	}

	/**
	 * Sets the maximum size of a result set.
	 *
	 * @param bufferSize
	 *            maximum size of a result set
	 */
	public void setBufferSize(final int bufferSize)
	{
		this.bufferSize = bufferSize;
	}

	/**
	 * Sets the character encoding.
	 *
	 * @param characterSet
	 *            character encoding
	 */
	public void setCharacterSet(final String characterSet)
	{
		this.characterSet = characterSet;
	}

	/**
	 * Set the memory size used for the purpose of storing revisions.
	 *
	 * @param chronoStorageSpace
	 *            memory size result
	 */
	public void setChronoStorageSpace(final long chronoStorageSpace)
	{
		this.chronoStorageSpace = chronoStorageSpace;
	}

	/**
	 * Sets the value of MAX_ALLOWED_PACKET parameter.
	 *
	 * @param maxAllowedPacket
	 *            MAX_ALLOWED_PACKET
	 */
	public void setMaxAllowedPacket(final long maxAllowedPacket)
	{
		this.maxAllowedPacket = maxAllowedPacket;
	}

	/**
	 * Sets the output path of the index generator.
	 *
	 * @param outputPath
	 *            output path
	 */
	public void setOutputPath(final String outputPath)
	{
		this.outputPath = outputPath;
	}

	/**
	 * Sets the output type of the index generator.
	 *
	 * @param outputType
	 *            output type
	 */
	public void setOutputType(final OutputTypes outputType)
	{
		this.outputType = outputType;
	}
}
