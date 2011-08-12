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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content;

/**
 * The DiffPart class represents the operation used to create a new revision
 * from an older revision.
 * 
 * 
 * 
 */
public class DiffPart
{

	/** Start position of the text block */
	private int start;

	/** Lengthof the text block */
	private int length;

	/** DiffAction value */
	private DiffAction action;

	/** Textual information */
	private String text;

	/**
	 * (Constructor) Creates a new DiffPart object.
	 * 
	 * @param action
	 *            DiffAction
	 */
	public DiffPart(final DiffAction action)
	{

		this.action = action;
	}

	/**
	 * Returns the length of the text block.
	 * 
	 * @return length of the text block
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * Sets the length of the text block.
	 * 
	 * @param length
	 *            length of the text block
	 */
	public void setLength(final int length)
	{
		this.length = length;
	}

	/**
	 * Returns the start position of the text block.
	 * 
	 * @return start position
	 */
	public int getStart()
	{
		return start;
	}

	/**
	 * Returns the end position of the text block.
	 * 
	 * @return end position
	 */
	public int getEnd()
	{
		return start + length;
	}

	/**
	 * Sets the start position of the text block.
	 * 
	 * @param start
	 *            start position
	 */
	public void setStart(final int start)
	{
		this.start = start;
	}

	/**
	 * Sets the textual information.
	 * 
	 * @param text
	 *            content
	 */
	public void setText(final String text)
	{
		this.text = text;
	}

	/**
	 * Returns the DiffAction value.
	 * 
	 * @return DiffAction
	 */
	public DiffAction getAction()
	{
		return this.action;
	}

	/**
	 * Returns the textual information.
	 * 
	 * @return content
	 */
	public String getText()
	{
		return this.text;
	}

	/**
	 * Returns a representation of the DiffAction content.
	 * 
	 * @return [ DiffAction, start position, length, content ]
	 */
	public String toString()
	{
		return "[" + action + " " + start + " " + length + " " + text + "]\n";
	}

	/**
	 * Returns the estimated number of bytes used to encode the contained
	 * information.
	 * 
	 * @return estimated size in bytes
	 */
	public int byteSize()
	{
		if (text == null) {
			return 9;
		}
		return 9 + text.length();
	}
}
