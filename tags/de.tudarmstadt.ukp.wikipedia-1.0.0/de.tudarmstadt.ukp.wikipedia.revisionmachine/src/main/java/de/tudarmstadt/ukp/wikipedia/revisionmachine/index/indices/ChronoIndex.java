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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Index for the correct chonological order of revisions.
 *
 *
 *
 */
public class ChronoIndex
	extends AbstractIndex
{

	/** ID of the last procesed article */
	private int articleID;

	/** List of ChonoInfo's */
	private List<ChronoIndexData> list;


	/**
	 * (Constructor) Creates a new ChronoIndex object.
	 *
	 * @param MAX_ALLOWED_PACKET
	 *            MAX_ALLOWED_PACKET
	 */
	public ChronoIndex()
	{

		super();

		this.list = null;
	}

	/**
	 * (Constructor) Creates a new ChronoIndex object.
	 *
	 * @param MAX_ALLOWED_PACKET
	 *            MAX_ALLOWED_PACKET
	 */
	public ChronoIndex(final long MAX_ALLOWED_PACKET)
	{

		super("INSERT INTO index_chronological VALUES ", MAX_ALLOWED_PACKET);

		this.list = null;
	}

	/**
	 * Adds the information for an new entry in the chrono index.
	 *
	 * @param articleID
	 *            ID of the article
	 * @param revisionCounter
	 *            Revision counter
	 * @param timestamp
	 *            Timestamp
	 */
	public void add(final int articleID, final int revisionCounter,
			final long timestamp)
	{

		if (this.articleID != articleID) {

			if (list != null) {
				addToBuffer();
			}

			this.articleID = articleID;
			this.list = new ArrayList<ChronoIndexData>();
		}

		this.list.add(new ChronoIndexData(timestamp, revisionCounter));
	}

	/**
	 * Creates the mapping and the reverse mapping. The generated information
	 * will be added to the query buffer. This list will be cleared afterwards.
	 */
	private void addToBuffer()
	{

		if (list != null && !list.isEmpty()) {

			ChronoIndexData info;

			// Real index in revision history mapped to RevisionCounter
			// Sorted by real index (time) in ascending order
			Collections.sort(list);

			StringBuilder reverseMapping = new StringBuilder();

			int size = list.size();
			for (int i = 1; i <= size; i++) {

				info = list.get(i - 1);
				if (info.getRevisionCounter() != i) {

					if (reverseMapping.length() > 0) {
						reverseMapping.append(" ");
					}

					reverseMapping.append(i);
					reverseMapping.append(" ");
					reverseMapping.append(info.getRevisionCounter());
				}

				info.setIndex(i);
				info.setSortFlag(false);
			}

			// RevisionCounter mapped to real index in revision history
			// Sorted by revisionCounters in ascending order
			Collections.sort(list);
			StringBuilder mapping = new StringBuilder();

			while (!list.isEmpty()) {

				info = list.remove(0);
				if (info.getRevisionCounter() != info.getIndex()) {

					if (mapping.length() > 0) {
						mapping.append(" ");
					}

					mapping.append(info.getRevisionCounter());
					mapping.append(" ");
					mapping.append(info.getIndex());
				}
			}

			if (mapping.length() > 0) {

				boolean sql = !insertStatement.isEmpty();
				String val = (sql?"(":"") + articleID + (sql?",'":",\"") + mapping.toString()
						+ (sql?"','":"\",\"") + reverseMapping.toString() +(sql?"')":"\"");

				if (buffer.length() + val.length() >= MAX_ALLOWED_PACKET) {
					storeBuffer();
				}

				if (sql&&buffer.length() > insertStatement.length()) {
					buffer.append(",");
				}

				buffer.append(val);

				if(!sql){
					buffer.append("\n");
				}
			}
		}
	}

	/**
	 * Finalizes the query in the currently used buffer and creates a new one.
	 * The finalized query will be added to the list of queries.
	 */
	@Override
	public void finalizeIndex()
	{
		addToBuffer();
		storeBuffer();
	}
}
