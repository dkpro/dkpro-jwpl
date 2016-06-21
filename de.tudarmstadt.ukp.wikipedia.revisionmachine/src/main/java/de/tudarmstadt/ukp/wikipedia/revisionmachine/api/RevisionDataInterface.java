/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
