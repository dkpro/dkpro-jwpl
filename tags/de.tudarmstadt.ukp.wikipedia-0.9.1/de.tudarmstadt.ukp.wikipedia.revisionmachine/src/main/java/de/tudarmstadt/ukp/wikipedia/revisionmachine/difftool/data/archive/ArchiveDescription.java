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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive;

/**
 * This class represents a description of an input file.
 * 
 * TODO: The start position is currently unused.
 * 
 * 
 * 
 */
public class ArchiveDescription
{

	/** Path to the archive */
	private String path;

	/** Start position */
	private long startPosition;

	/** InputType */
	private InputType type;

	/**
	 * (Constructor) Creates a new ArchiveDescription
	 * 
	 * @param type
	 *            InputType
	 * @param path
	 *            Path
	 */
	public ArchiveDescription(final InputType type, final String path)
	{
		this.type = type;
		this.path = path;
	}

	/**
	 * Returns the path.
	 * 
	 * @return path
	 */
	public String getPath()
	{
		return this.path;
	}

	/**
	 * Returns the start position.
	 * 
	 * @return start position
	 */
	public long getStartPosition()
	{
		return startPosition;
	}

	/**
	 * Returns the InputType.
	 * 
	 * @return InputType
	 */
	public InputType getType()
	{
		return this.type;
	}

	/**
	 * Sets the start position.
	 * 
	 * @param startPosition
	 *            start position
	 */
	public void setStartPosition(final long startPosition)
	{
		this.startPosition = startPosition;
	}

	/**
	 * Returns the string representation of this object.
	 * 
	 * @return [InputType, path]
	 */
	public String toString()
	{
		return "[" + this.getType() + ", " + this.getPath() + "]";
	}
}
