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
package org.dkpro.jwpl.revisionmachine.index.indices;

import java.util.List;

/**
 * Index for article information.
 *
 *
 *
 */
public class ArticleIndex
	extends AbstractIndex
{
	/**
	 * (Constructor) Creates a new ArticleIndex object.
	 */
	public ArticleIndex()
	{

		super();
	}

	/**
	 * (Constructor) Creates a new ArticleIndex object.
	 *
	 * @param MAX_ALLOWED_PACKET
	 *            MAX_ALLOWED_PACKET
	 */
	public ArticleIndex(final long MAX_ALLOWED_PACKET)
	{

		super("INSERT INTO index_articleID_rc_ts VALUES ", MAX_ALLOWED_PACKET);
	}

	/**
	 * Adds the information for an new entry in the article index.
	 *
	 * @param currentArticleID
	 *            ID of the currently used article
	 * @param startTime
	 *            First date of appearance
	 * @param endTime
	 *            Last date of appearance
	 * @param infoList
	 *            List of revision blocks
	 */
	public void add(final int currentArticleID, final long startTime,
			final long endTime, final List<ArticleIndexData> infoList)
	{

		// index_articleID_rc_ts
		if (!infoList.isEmpty()) {

			StringBuilder fullRevBuffer = new StringBuilder();
			StringBuilder revCountBuffer = new StringBuilder();

			boolean first = true;
			ArticleIndexData info;
			while (!infoList.isEmpty()) {

				info = infoList.remove(0);

				if (!first) {
					fullRevBuffer.append(" ");
					revCountBuffer.append(" ");
				}

				fullRevBuffer.append(info.getFullRevisionPrimaryKey());

				revCountBuffer.append(info.getStartRevisionCount());
				revCountBuffer.append(" ");
				revCountBuffer.append(info.getEndRevisionCount());

				first = false;
			}

			boolean sql = !insertStatement.isEmpty();
			if (buffer.length() + fullRevBuffer.length()
					+ revCountBuffer.length() + 20 >= MAX_ALLOWED_PACKET) {
				storeBuffer();
			}


			if(sql) {
				if (buffer.length() > insertStatement.length()) {
					buffer.append(",");
				}
				buffer.append("(");
			}
			buffer.append(currentArticleID);
			buffer.append(",");
			buffer.append(sql?"\'":"\"");
			buffer.append(fullRevBuffer);
			buffer.append(sql?"\'":"\"");
			buffer.append(",");
			buffer.append(sql?"\'":"\"");
			buffer.append(revCountBuffer);
			buffer.append(sql?"\'":"\"");
			buffer.append(",");
			buffer.append(startTime);
			buffer.append(",");
			buffer.append(endTime);
			if(sql) {
				buffer.append(")");
			}else{
				buffer.append("\n");
			}
		}
	}
}
