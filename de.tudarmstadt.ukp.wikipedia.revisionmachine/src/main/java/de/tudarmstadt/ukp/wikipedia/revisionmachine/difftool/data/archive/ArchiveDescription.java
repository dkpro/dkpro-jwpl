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
