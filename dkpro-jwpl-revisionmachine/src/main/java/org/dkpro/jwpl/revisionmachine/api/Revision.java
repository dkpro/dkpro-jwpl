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
package org.dkpro.jwpl.revisionmachine.api;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;

import org.apache.commons.lang.StringEscapeUtils;

import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.ISizeable;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;

/**
 *
 * This class contains all revision data.
 *
 * The revision text is loaded upon first access (lazy loading).
 * When serializing a Revision, the revisionText will be loaded first.
 *
 */
public class Revision
	implements ISizeable, Comparable<Revision>, RevisionDataInterface, Serializable
{

	private static final long serialVersionUID = 7955292965697731279L;

	/** ID of the article */
	private int articleID;

	/** Full Revision ID */
	private int fullRevisionID;

	/** Primary Key */
	private int primaryKey;

	/** Revision counter */
	private final int revisionCounter;

	/** ID of the revision */
	private int revisionId;

	/** Content */
	private String revisionText;

	/** Timestamp */
	private Timestamp timeStamp;

	/** Username of the contributor who created this revision */
	private String contributorName;

	/** Username of the contributor who created this revision */
	private Integer contributorId;

	/** The user comment for this revision */
	private String comment;

	/** Determine whether revision is a minor revision */
	private boolean isMinor = false;

	/**
	 * Determine whether the contributor was registered. True: contributorName=
	 * username False: contributorName= IP
	 */
	private boolean contributorIsRegistered;

	/** Reference to RevisionApi */
	private transient RevisionApi revisionApi;

	// TODO add fields for the revision flags

	/**
	 * A collection of DiffParts that make up this revision. This can be used to
	 * get Information about the actions that have been performed to create this
	 * revision
	 */
	private Collection<DiffPart> parts;

	/**
	 * (Constructor) Creates a new Revision object.
	 *
	 * @param revisionCounter
	 *            revision counter
	 */
	public Revision(final int revisionCounter)
	{
		this.revisionCounter = revisionCounter;
	}

	/**
	 * (Constructor) Creates a new Revision object.
	 *
	 * @param revisionCounter
	 *            revision counter
	 * @param revisionApi
	 *            revision API
	 */
	public Revision(final int revisionCounter, RevisionApi revisionApi)
	{
		this.revisionCounter = revisionCounter;
		this.revisionApi = revisionApi;
	}

	/**
	 * Returns the estimated number of bytes used to encode the contained
	 * information.
	 *
	 * @return estimated size in bytes
	 */
	@Override
	public long byteSize()
	{
		if (this.revisionText == null) {
			return 0;
		}
		return this.revisionText.length();
	}

	/**
	 * Returns the ID of the article.
	 *
	 * @return article ID
	 */
	@Override
	public int getArticleID()
	{
		return articleID;
	}

	/**
	 * Returns the full revision ID.
	 *
	 * @return full revision ID
	 */
	public int getFullRevisionID()
	{
		return this.fullRevisionID;
	}

	/**
	 * Returns the primary key.
	 *
	 * @return primary key
	 */
	public int getPrimaryKey()
	{
		return primaryKey;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Revision r)
	{
		long value = this.timeStamp.getTime() - r.getTimeStamp().getTime();

		if (value == 0) {
			return this.getRevisionID() - r.getRevisionID();
		}
		else if (value > 0) {
			return 1;
		}
		else {
			return -1;
		}
	}

	/**
	 * Sets the revision api
	 *
	 * @param revisionApi
	 *            api to set
	 *
	 */
	public void setRevisionApi(RevisionApi revisionApi)
	{
		this.revisionApi = revisionApi;
	}

	/**
	 * Returns the revision counter.
	 *
	 * @return revision counter
	 */
	@Override
	public int getRevisionCounter()
	{
		return revisionCounter;
	}

	/**
	 * Returns the ID of the revision.
	 *
	 * @return revision ID
	 */
	@Override
	public int getRevisionID()
	{
		return revisionId;
	}

	/**
	 * Returns the textual content of this revision.
	 *
	 * @return content
	 */
	public String getRevisionText()
	{
		if (this.revisionText == null) {
			revisionApi.setRevisionTextAndParts(this);
		}
		return StringEscapeUtils.unescapeHtml(this.revisionText);
	}

	/**
	 * Returns the timestamp.
	 *
	 * @return timestamp
	 */
	@Override
	public Timestamp getTimeStamp()
	{
		return timeStamp;
	}

	/**
	 * Returns a collection of DiffPart objects that make up this revision
	 *
	 * @return a collection of DiffPart object that make up this revision
	 */
	public Collection<DiffPart> getParts()
	{
		if (this.parts == null) {
			revisionApi.setRevisionTextAndParts(this);
		}
		return this.parts;
	}

	/**
	 * Sets the ID of the article.
	 *
	 * @param articleID
	 *            article ID
	 */
	public void setArticleID(final int articleID)
	{
		this.articleID = articleID;
	}

	/**
	 * Set the ID of the full revision.
	 *
	 * @param fullRevisionID
	 *            full revision ID
	 */
	public void setFullRevisionID(final int fullRevisionID)
	{
		this.fullRevisionID = fullRevisionID;
	}

	/**
	 * Sets the primary key.
	 *
	 * @param primaryKey
	 *            primary key
	 */
	public void setPrimaryKey(final int primaryKey)
	{
		this.primaryKey = primaryKey;
	}

	/**
	 * Sets the ID of the revision.
	 *
	 * @param revisionId
	 *            revision ID
	 */
	public void setRevisionID(final int revisionId)
	{
		this.revisionId = revisionId;
	}

	/**
	 * Sets the revision text.
	 *
	 * @param revisionText
	 *            content
	 */
	public void setRevisionText(final String revisionText)
	{
		this.revisionText = revisionText;
	}

	/**
	 * Sets the timestamp information.
	 *
	 * The input is expected to be the wikipedia version of the timestamp as
	 * String (YYYY-MM-DDThh-mm-ssZ). T and Z will be replaced with spaces.
	 *
	 * @param timeStamp
	 *            timestamp (wikipedia version)
	 */
	public void setTimeStamp(final String timeStamp)
	{

		String time = timeStamp.replace('T', ' ');
		time = time.replace('Z', ' ');

		this.timeStamp = Timestamp.valueOf(time);
	}

	/**
	 * Sets the timestamp information.
	 *
	 * @param timeStamp
	 *            timestamp
	 */
	public void setTimeStamp(final Timestamp timeStamp)
	{

		this.timeStamp = timeStamp;
	}

	/**
	 * Sets the collection of DiffPart objects that make up this revision
	 *
	 * @param parts
	 *            a collection of DiffPart object that make up this revision
	 */
	public void setParts(Collection<DiffPart> parts)
	{
		this.parts = parts;
	}

	/**
	 * Returns the string representation of this object.
	 *
	 * @return (ArticleID, RevisionCounter, Timestamp, RevisionID, TextLength)
	 */
	@Override
	public String toString()
	{

		StringBuilder sRep = new StringBuilder();
		sRep.append('(');
		sRep.append(articleID);
		sRep.append(", ");
		sRep.append(revisionCounter);
		sRep.append(", ");
		sRep.append(timeStamp);
		sRep.append(", ");
		sRep.append(revisionId);

		if (revisionText != null) {
			sRep.append(", ");
			sRep.append(revisionText.length());
		}
		sRep.append(')');

		return sRep.toString();
	}

	/**
	 * Sets the user comment for this revision
	 *
	 * @param comment
	 *            the user comment for this revision
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * Returns the user comment for this revision
	 *
	 *
	 * @return the user comment for this revision
	 */
	@Override
	public String getComment()
	{
		return comment;
	}

	public void setMinor(boolean isMinor)
	{
		this.isMinor = isMinor;
	}

	@Override
	public boolean isMinor()
	{
		return isMinor;
	}

	public void setContributorName(String contributorName)
	{
		this.contributorName = contributorName;
	}

	@Override
	public String getContributorName()
	{
		return contributorName;
	}

	public void setContributorIsRegistered(boolean contributorIsRegistered)
	{
		this.contributorIsRegistered = contributorIsRegistered;
	}

	@Override
	public boolean contributorIsRegistered()
	{
		return contributorIsRegistered;
	}

	public void setContributorId(Integer contributorId)
	{
		this.contributorId = contributorId;
	}

	@Override
	public Integer getContributorId()
	{
		return contributorId;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		//load DiffParts before serializing
		getParts();
		//load revision text before serializing
		getRevisionText();
		//now we can serialize the object with the default write method
		out.defaultWriteObject();
	}

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     *
     * Revisions are equal if their ids are equal
     */
    @Override
	public boolean equals(Object anObject) {

    	if(!(anObject instanceof Revision)){
    		return false;
    	}else{
    		Revision otherRev = (Revision)anObject;
			if (this.getRevisionID()==otherRev.getRevisionID()) {
    			return true;
    		}else{
    			return false;
    		}
    	}
    }

}
