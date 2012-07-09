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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.api;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.OutputTypes;

/**
 * This class contains the additional parameters for the RevisionApi.
 *
 *
 *
 */
public class RevisionAPIConfiguration
	extends DatabaseConfiguration
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
	 * OutputType: UNCOMPRESSED (revisionIndex.sql)<br/>
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
	 * OutputType: DATABASE<br/>
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
	 * Sets the character endocing.
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
	 *            memory sizeresult
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
