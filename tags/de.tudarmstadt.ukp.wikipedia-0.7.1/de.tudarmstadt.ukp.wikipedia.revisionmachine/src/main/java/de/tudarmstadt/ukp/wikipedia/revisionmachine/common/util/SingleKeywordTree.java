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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util;

/**
 * This class represents a keyword tree and is used to process or to search a
 * character sequence.
 * 
 * This keyword tree can only be used for non overlapping keywords.
 * 
 * 
 * 
 * 
 * @param <V>
 *            related value
 */
public class SingleKeywordTree<V>
{

	/** Reference to the root */
	private LetterNode<V> root;

	/** Reference to the current node */
	private LetterNode<V> current;

	/**
	 * (Constructor) Creates an empty SingleKeywordTree object.
	 */
	public SingleKeywordTree()
	{
		root = new LetterNode<V>();
		reset();
	}

	/**
	 * Adds a keyword and its related value.
	 * 
	 * @param s
	 *            keyword
	 * @param value
	 *            related value
	 */
	public void addKeyword(final String s, final V value)
	{
		root.add(s, value);
	}

	/**
	 * Checks whether the character is related to the currently used node. If
	 * the comparison fails the keyword tree will be reseted to its root node,
	 * otherwise the related node will replace the current node.
	 * 
	 * @param c
	 *            character
	 * @return TRUE if the current node contains a keyword FALSE otherwise
	 */
	public boolean check(final char c)
	{
		current = current.get(c);
		if (current == null) {
			reset();
		}
		return current.isKeyword();
	}

	/**
	 * Resets the current node with the root node.
	 */
	public void reset()
	{
		this.current = root;
	}

	/**
	 * Returns the keyword of the current node.
	 * 
	 * @return keyword
	 */
	public String getWord()
	{
		return this.current.getWord();
	}

	/**
	 * Returns the related value of the current node.
	 * 
	 * @return related value
	 */
	public V getValue()
	{
		return this.current.getValue();
	}
}
