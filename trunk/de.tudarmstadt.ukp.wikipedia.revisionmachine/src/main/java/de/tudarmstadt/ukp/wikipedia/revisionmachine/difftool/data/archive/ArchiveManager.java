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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive;

import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;

/**
 * DiffManager Manages the data contained by the ArchiveProducer
 * 
 * @author Simon Kulessa
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
