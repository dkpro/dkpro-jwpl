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
import java.util.List;

/**
 * This class represents an abstact index.
 *
 *
 *
 */
public abstract class AbstractIndex
{

	/** Current query buffer */
	protected StringBuilder buffer;

	/** List of contained queries. */
	private List<StringBuilder> bufferList;

	/** Insert Statement to use */
	protected final String insertStatement;

	/** MAX_ALLOWED_PACKET */
	protected long MAX_ALLOWED_PACKET;

	/**
	 * (Constructor) Creates an index object.
	 *
	 * @param insertStatement
	 *            Insert Statement
	 * @param MAX_ALLOWED_PACKET
	 *            MAX_ALLOWED_PACKET
	 */
	public AbstractIndex()
	{

		this.bufferList = new ArrayList<StringBuilder>();
		this.buffer = null;

		//does not really matter here- should be big to speed up data file creation
		this.MAX_ALLOWED_PACKET = 16760832;

		this.insertStatement = "";

		storeBuffer();
	}

	/**
	 * (Constructor) Creates an index object.
	 *
	 * @param insertStatement
	 *            Insert Statement
	 * @param MAX_ALLOWED_PACKET
	 *            MAX_ALLOWED_PACKET
	 */
	public AbstractIndex(final String insertStatement,
			final long MAX_ALLOWED_PACKET)
	{

		this.bufferList = new ArrayList<StringBuilder>();
		this.buffer = null;

		this.MAX_ALLOWED_PACKET = MAX_ALLOWED_PACKET;

		this.insertStatement = insertStatement;

		storeBuffer();
	}

	/**
	 * Returns the size of the currently used buffer.
	 *
	 * @return size of current query
	 */
	public int byteSize()
	{
		return this.buffer.length();
	}

	/**
	 * Finalizes the query in the currently used buffer and creates a new one.
	 * The finalized query will be added to the list of queries.
	 */
	public void finalizeIndex()
	{
		storeBuffer();
	}

	/**
	 * Removes a query from the list of queries.
	 *
	 * @return Buffer containing a finalized query
	 */
	public StringBuilder remove()
	{
		return this.bufferList.remove(0);
	}

	/**
	 * Returns the current number of buffered queries.
	 *
	 * @return size of the list of queries
	 */
	public int size()
	{
		return bufferList.size();
	}

	/**
	 * Finalizes the query in the currently used buffer and creates a new one.
	 * The finalized query will be added to the list of queries.
	 */
	protected void storeBuffer()
	{

		if (buffer != null && buffer.length() > insertStatement.length()) {
			if(!insertStatement.isEmpty()) {
				//only do this in SQL/DATABASE MODE
				this.buffer.append(";");
			}
			bufferList.add(buffer);
		}

		this.buffer = new StringBuilder();
		this.buffer.append(insertStatement);
	}
}
