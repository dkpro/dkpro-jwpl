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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.index.writer;

import java.io.IOException;
import java.sql.SQLException;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.index.indices.AbstractIndex;

/**
 * Interface for the IndexWriter
 *
 *
 *
 */
public interface IndexWriterInterface
{

	/**
	 * Writes the buffered finalzed queries to the output.
	 *
	 * @param index
	 *            Reference to an index
	 * @throws IOException
	 *             if an error occured while writing the output
	 * @throws SQLException
	 *             if an error occured while transmitting the output
	 */
	void write(final AbstractIndex index)
		throws IOException, SQLException;

	/**
	 * Closes the file or the database connection.
	 *
	 * @throws IOException
	 *             if an error occured while closing the file
	 * @throws SQLException
	 *             if an error occured while closing the database connection
	 */
	void close()
		throws IOException, SQLException;

	/**
	 * Wraps up the index generation process and writes all remaining statements
	 * e.g. concerning UNCOMPRESSED-Indexes on the created tables.
	 *
	 * @throws SQLException
	 *             if an error occured while accessing the database
	 * @throws IOException
	 *             if an error occured while accessing the sql file
	 */
	public void finish() throws IOException, SQLException;
}
