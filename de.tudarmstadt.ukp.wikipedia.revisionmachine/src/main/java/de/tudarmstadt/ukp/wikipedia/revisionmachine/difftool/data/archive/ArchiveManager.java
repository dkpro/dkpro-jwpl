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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive;

import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;

/**
 * DiffManager Manages the data contained by the ArchiveProducer
 *
 * @version 0.5.0
 */
public class ArchiveManager
{

	/** List of available archives */
	private List<ArchiveDescription> archives;

	/**
	 * (Constructor) Creates the ArchiveManager.
	 *
	 * @throws ConfigurationException
	 *             if an error occurs while accessing the configuration
	 */
	public ArchiveManager()
		throws ConfigurationException
	{

		ConfigurationManager config = ConfigurationManager.getInstance();
		this.archives = config.getArchiveList();
	}

	/**
	 * Returns whether an archive is available or not.
	 *
	 * @return TRUE | FALSE
	 */
	public boolean hasArchive()
	{
		return !this.archives.isEmpty();
	}

	/**
	 * Returns an archive.
	 *
	 * @return ArchiveDescription or NULL if no archive is available
	 */
	public synchronized ArchiveDescription getArchive()
	{

		if (!this.archives.isEmpty()) {

			return this.archives.remove(0);
		}

		return null;
	}

	/**
	 * Returns the number of remaining archives.
	 *
	 * @return number of available archives
	 */
	public int size()
	{
		return this.archives.size();
	}

	/**
	 * Returns the string representation of the ArchiveManager's content.
	 *
	 * @return [ number of archives ]
	 */
	public String toString()
	{
		return "ArchiveManager:\t[" + this.size() + "]";
	}
}
