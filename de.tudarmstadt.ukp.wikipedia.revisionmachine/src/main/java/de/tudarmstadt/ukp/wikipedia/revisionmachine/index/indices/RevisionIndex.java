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
