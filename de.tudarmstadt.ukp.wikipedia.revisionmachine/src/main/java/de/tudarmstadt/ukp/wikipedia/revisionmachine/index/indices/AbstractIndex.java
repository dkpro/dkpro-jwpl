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
