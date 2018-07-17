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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.codec;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to stored the sql statements.
 *
 *
 *
 */
public class SQLEncoding
{

	/** UNCOMPRESSED Query */
	private StringBuilder query;

	/** List of binary data */
	private List<byte[]> list;

	/** Size of binary data */
	private int binaryDataSize;

	/**
	 * (Constructor) Creates a new SQLEncoding object.
	 */
	public SQLEncoding()
	{
		this.query = new StringBuilder();
		this.list = new ArrayList<byte[]>();
		this.binaryDataSize = 0;
	}

	/**
	 * Appends textual content to the query.
	 *
	 * @param seq
	 *            textual content
	 */
	public void append(final CharSequence seq)
	{
		this.query.append(seq);
	}

	/**
	 * Appends binary data to storage.
	 *
	 * @param bData
	 *            binary data
	 */
	public void addBinaryData(final byte[] bData)
	{
		this.binaryDataSize += bData.length;
		this.list.add(bData);
	}

	/**
	 * Returns the size of the query.
	 *
	 * @return size of the query
	 */
	public int byteSize()
	{
		return this.binaryDataSize + this.query.length();
	}

	/**
	 * Returns the number of contained binary data parts.
	 *
	 * @return number of binary data parts
	 */
	public int size()
	{
		return this.list.size();
	}

	/**
	 * Returns the specified binary data.
	 *
	 * @param index
	 *            index of the binary data
	 * @return binary data
	 */
	public byte[] getBinaryData(final int index)
	{
		return list.get(index);
	}

	/**
	 * Returns the query.
	 *
	 * @return query
	 */
	public String getQuery()
	{
		return query.toString();
	}

	/**
	 * Returns the string representation of this object.
	 *
	 * @return string representation
	 */
	public String toString()
	{

		try {
			StringBuilder buffer = new StringBuilder();

			buffer.append(query + "\r\n\r\n");

			for (int i = 0; i < list.size(); i++) {
				buffer.append(i + "\t" + list.get(i).length + "\r\n");
			}

			return buffer.toString();

		}
		catch (Exception e) {

		}

		return "<" + list.size() + ">\r\n" + query.toString();
	}
}
