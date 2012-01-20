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
