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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.api.chrono;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionAPIConfiguration;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionDecoder;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * ChronoIterator Iterates articles in chronological order.
 * 
 * 
 * 1
 */
public class ChronoIterator
{

	/** Reference to the configuration */
	private final RevisionAPIConfiguration config;

	/** Reference to the database connection */
	private final Connection connection;

	/** Reference to the ChronoStorage */
	private final ChronoStorage chronoStorage;

	/** currently used article pk */
	private final int articlePK;

	/** revision index */
	private int revisionIndex;

	/** maximum revision */
	private final int maxRevision;

	/** ChronoFullRevision Storage */
	private final Map<Integer, ChronoFullRevision> fullRevStorage;

	/** Mapping chronological position to revision counter */
	private final Map<Integer, Integer> mappingStorage;

	/**
	 * (Constructor) Create a ChronoIterator object
	 * 
	 * @param config
	 *            reference to the configuration
	 * @param connection
	 *            reference to the database connection
	 * @param mapping
	 *            mapping (chrono counter to revision counter)
	 * @param fullRevisionPKs
	 *            space separated list of full revision pks
	 * @param revisionCounters
	 *            space separated list of revision counter intervals
	 */
	public ChronoIterator(final RevisionAPIConfiguration config,
			final Connection connection, final String mapping,
			final String fullRevisionPKs, final String revisionCounters)
	{

		this.config = config;
		this.connection = connection;

		int index = fullRevisionPKs.indexOf(' ');
		if (index == -1) {
			index = fullRevisionPKs.length();
		}

		articlePK = Integer.parseInt(fullRevisionPKs.substring(0, index));

		index = revisionCounters.lastIndexOf(' ');
		if (index == -1) {
			throw new RuntimeException("Invalid revisioncounter content");
		}

		this.revisionIndex = 0;
		this.maxRevision = Integer.parseInt(revisionCounters.substring(
				index + 1, revisionCounters.length()));

		Map<Integer, Integer> reverseMappingStorage = new HashMap<Integer, Integer>();

		this.mappingStorage = new HashMap<Integer, Integer>();
		this.fullRevStorage = new HashMap<Integer, ChronoFullRevision>();

		ChronoFullRevision previous = null, current = null, firstCFR = null;

		int length = 0;
		int revC = -1, mapC = -1;

		int max = mapping.length();
		length = 0;

		// Creates the mapping information for each revision
		while (length < max) {

			// Read revisionCounter
			index = mapping.indexOf(' ', length);
			revC = Integer.parseInt(mapping.substring(length, index));
			length = index + 1;

			// Read mappedCounter
			index = mapping.indexOf(' ', length);
			if (index == -1) {
				index = mapping.length();
			}
			mapC = Integer.parseInt(mapping.substring(length, index));
			length = index + 1;

			reverseMappingStorage.put(revC, mapC);
			mappingStorage.put(mapC, revC);
		}

		length = 0;
		max = revisionCounters.length();
		int fullRevPK = 0, lengthFR = 0;

		// Creates the full revision blocks for each full revision
		while (length < max) {

			// Read fullRevisionPK (as string)
			index = fullRevisionPKs.indexOf(' ', lengthFR);
			if (index == -1) {
				index = fullRevisionPKs.length();
			}

			fullRevPK = Integer.parseInt(fullRevisionPKs.substring(lengthFR,
					index));
			lengthFR = index + 1;

			// Read start RC
			index = revisionCounters.indexOf(' ', length);
			revC = Integer.parseInt(revisionCounters.substring(length, index));
			length = index + 1;

			// Read end RC
			index = revisionCounters.indexOf(' ', length);
			if (index == -1) {
				index = revisionCounters.length();
			}
			mapC = Integer.parseInt(revisionCounters.substring(length, index));
			length = index + 1;

			// Constructs a double linked list containing the full revision
			current = new ChronoFullRevision(fullRevPK, revC, mapC);
			if (firstCFR == null) {
				firstCFR = current;
			}
			else {
				current.setPrev(previous);
				previous.setNext(current);
			}

			// Add index information for each revision contained in such
			// a block
			for (int i = revC; i <= mapC; i++) {
				fullRevStorage.put(i, current);
			}

			previous = current;
		}

		// Create ChronoStorage object
		this.chronoStorage = new ChronoStorage(config, reverseMappingStorage,
				firstCFR, fullRevStorage);
	}

	/**
	 * Returns if all revision have retrieved.
	 * 
	 * @return
	 */
	public boolean hasNext()
	{
		return ++revisionIndex <= maxRevision;
	}

	/**
	 * Returns the next revision.
	 * 
	 * @return next revision
	 */
	public Revision next()
		throws Exception
	{

		// Checks whether the next revision has already been reconstructed.
		Revision revision;
		if (chronoStorage.isTop(revisionIndex)) {

			// If this is the case the revision will removed from the storage
			return chronoStorage.remove();
		}

		// Otherwise the chronological order counter will be mapped to the
		// revsision counter
		int revCount = revisionIndex;
		if (mappingStorage.containsKey(revisionIndex)) {
			revCount = mappingStorage.get(revisionIndex);
		}

		// Retrieve the related full revision block
		ChronoFullRevision cfr = fullRevStorage.get(revCount);

		int queryPK, limit, previousRevisionCounter;
		String previousRevision;

		// Determine the nearest revision that could be used to construct
		// the specified revision
		revision = cfr.getNearest(revCount);
		if (revision == null) {

			// Create query bounds (all revisions from the full revision till
			// now)
			queryPK = articlePK + cfr.getStartRC() - 1;
			limit = revCount - cfr.getStartRC() + 1;

			previousRevision = null;
			previousRevisionCounter = -1;

		}
		else {

			// Create query bounds (only new revisions, last known + 1 till now)
			queryPK = revision.getPrimaryKey() + 1;
			limit = revCount - revision.getRevisionCounter();

			previousRevision = revision.getRevisionText();
			previousRevisionCounter = revision.getRevisionCounter();

		}

		Statement statement = null;
		ResultSet result = null;
		revision = null;

		try {
			statement = this.connection.createStatement();

			// Retrieve encoded revisions
			result = statement
					.executeQuery("SELECT Revision, PrimaryKey, RevisionCounter, RevisionID, ArticleID, Timestamp "
							+ "FROM revisions "
							+ "WHERE PrimaryKey >= "
							+ queryPK + " LIMIT " + limit);

			String currentRevision = null;

			Diff diff;
			RevisionDecoder decoder;

			boolean binaryData = result.getMetaData().getColumnType(1) == Types.LONGVARBINARY;

			while (result.next()) {

				decoder = new RevisionDecoder(config.getCharacterSet());

				// binary or base64 encoded
				if (binaryData) {
					decoder.setInput(result.getBinaryStream(1), true);
				}
				else {
					decoder.setInput(result.getString(1));
				}

				// Decode and rebuild
				diff = decoder.decode();
				if (previousRevisionCounter != -1) {

					if (previousRevisionCounter + 1 != result.getInt(3)) {

						System.err.println("Reconstruction data invalid - "
								+ "\r\n\t" + "Expected "
								+ (previousRevisionCounter + 1)
								+ " instead of " + result.getInt(3));

						return null;
					}

				}
				else {

					if (cfr.getStartRC() != result.getInt(3)) {

						System.err.println("Reconstruction data invalid - "
								+ "\r\n\t" + "Expected " + (cfr.getStartRC())
								+ " instead of " + result.getInt(3));

						return null;
					}

				}

				try {
					currentRevision = diff.buildRevision(previousRevision);

					revision = new Revision(result.getInt(3));
					revision.setRevisionText(currentRevision);
					revision.setPrimaryKey(result.getInt(2));
					revision.setRevisionID(result.getInt(4));
					revision.setArticleID(result.getInt(5));
					revision.setTimeStamp(new Timestamp(result.getLong(6)));

					previousRevision = currentRevision;
					previousRevisionCounter = revision.getRevisionCounter();

				}
				catch (Exception e) {

					System.err.println("Reconstruction failed while retrieving"
							+ " data to reconstruct <" + revisionIndex + ">"
							+ "\r\n\t" + "[ArticleId " + result.getInt(5)
							+ ", RevisionId " + result.getInt(4)
							+ ", RevisionCounter " + result.getInt(3) + "]");

					previousRevision = null;
					revision = null;

					return null;
				}

				// Add the reconstructed revision to the storage
				if (revision != null) {
					chronoStorage.add(revision);
				}
			}

			// Ensure that the correct revision is on top of the storage
			if (chronoStorage.isTop(revisionIndex)) {

				chronoStorage.remove();
				return revision;

			}
			else {
				return null;
			}

		}
		finally {
			if (statement != null) {
				statement.close();
			}
			if (result != null) {
				result.close();
			}
		}
	}

	/**
	 * Returns the storage size description.
	 * 
	 * @return storage size description
	 */
	public String getStorageSize()
	{
		return chronoStorage.getStorageSize();
	}
}
