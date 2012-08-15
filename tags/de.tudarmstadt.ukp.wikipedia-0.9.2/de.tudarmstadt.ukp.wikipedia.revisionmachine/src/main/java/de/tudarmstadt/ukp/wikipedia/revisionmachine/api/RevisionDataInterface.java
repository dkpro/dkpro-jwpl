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

import java.sql.Timestamp;

/**
 * This interface contains method to access the additional data of a revision.
 *
 *
 *
 */
public interface RevisionDataInterface
{

	/**
	 * Returns the ID of the article.
	 *
	 * @return ID of the article
	 */
	public int getArticleID();

	/**
	 * Returns the ID of the revision.
	 *
	 * @return ID of the revision
	 */
	public int getRevisionID();

	/**
	 * Returns the timestamp
	 *
	 * @return timestamp
	 */
	public Timestamp getTimeStamp();

	/**
	 * Returns the revision counter
	 *
	 * @return revision counter
	 */
	public int getRevisionCounter();

	/**
	 * Returns the user comment for this revision
	 *
	 *
	 * @return the user comment for this revision
	 */
	public String getComment();

	/**
	 * Returns true if revision is a minor revision.
	 *
	 * @return true if revision is a minor revision, false else
	 */
	public boolean isMinor();

	/**
	 * Returns the contributorID of the revision contributor
	 * Unregistered users do not have an id, so the return value might be null.
	 *
	 * @return the contributorID of the revision contributor or null, if user does not have an id (= is not registered)
	 */
	public Integer getContributorId();

	/**
	 * Returns the contributorName of the revision contributor
	 *
	 * @return the contributorName of the revision contributor
	 */
	public String getContributorName();

	/**
	 * Returns true, if the contributor is a registered user
	 *
	 * @return true, if the contributor is a registered user, false else
	 */
	public boolean contributorIsRegistered();

}
