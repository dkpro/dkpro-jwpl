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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.archives;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.ArchiveDescription;

/**
 * Interface of the ArcbiveProducer
 * 
 * 
 * 
 */
public interface ArchiveProducerInterface
{

	/**
	 * Returns whether an archive is available or not.
	 * 
	 * @return TRUE | FALSE
	 */
	boolean hasArchive();

	/**
	 * Returns an archive.
	 * 
	 * @return ArchiveDescription
	 */
	ArchiveDescription getArchive();
}
