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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.index.indices;

/**
 * Index for revision information.
 *
 *
 *
 */
public class RevisionIndex
	extends AbstractIndex
{

	/**
	 * (Constructor) Creates a new RevisionIndex object.
	 *
	 */
	public RevisionIndex()
	{

		super();
	}

	/**
	 * (Constructor) Creates a new RevisionIndex object.
	 *
	 * @param MAX_ALLOWED_PACKET
	 *            MAX_ALLOWED_PACKET
	 */
	public RevisionIndex(final long MAX_ALLOWED_PACKET)
	{

		super("INSERT INTO index_revisionID VALUES ", MAX_ALLOWED_PACKET);
	}

	/**
	 * Adds the information for an new entry in the revision index.
	 *
	 * @param revisionID
	 *            ID of the revision
	 * @param revisionPrimaryKey
	 *            PK of the revison
	 * @param fullRevisionPrimaryKey
	 *            PK of the related full revison
	 */
	public void add(final int revisionID, final long revisionPrimaryKey,
			final long fullRevisionPrimaryKey)
	{

		boolean sql = !insertStatement.isEmpty();
		if (sql&&buffer.length() != insertStatement.length()) {
			this.buffer.append(",");
		}

		this.buffer.append((sql?"(":"") + revisionID + "," + revisionPrimaryKey + ","
				+ fullRevisionPrimaryKey + (sql?")":""));

		if(!sql){
			buffer.append("\n");
		}

		if (buffer.length() + 100 >= MAX_ALLOWED_PACKET) {
			storeBuffer();
		}
	}
}
