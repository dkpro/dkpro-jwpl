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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util;

import java.util.HashMap;

/**
 * LetterNode This node represents a node of the keyword tree.
 *
 *
 *
 *
 * @param <V>
 *            contained value
 */
public class LetterNode<V>
{

	/** Alphabetic index of successor nodes */
	private HashMap<Character, LetterNode<V>> nodes;

	/** Flag, whether this node contains a valid key or not */
	private boolean isKeyword;

	/** Contained keyword */
	private String word;

	/** Contained value - related to the keyword */
	private V value;

	/**
	 * (Constructor) Creates a empty LetterNode.
	 */
	public LetterNode()
	{
		this.nodes = new HashMap<Character, LetterNode<V>>();
		this.isKeyword = false;
		this.word = "";
	}

	/**
	 * (Constructor) Creates a LetterNode with a keyword.
	 *
	 * @param word
	 *            keyword
	 */
	public LetterNode(final String word)
	{
		this.nodes = new HashMap<Character, LetterNode<V>>();
		this.isKeyword = false;
		this.word = word;
	}

	/**
	 * Adds a word and its related value.
	 *
	 * @param word
	 *            keyword
	 * @param value
	 *            related value
	 */
	public void add(final String word, final V value)
	{

		char c = word.charAt(0);

		LetterNode<V> node = get(c);
		if (node == null) {
			node = new LetterNode<V>(this.word + c);
		}
		this.nodes.put(c, node);

		if (word.length() == 1) {
			node.isKeyword = true;
			node.value = value;
			return;
		}

		node.add(word.substring(1), value);
	}

	/**
	 * Returns the keyword.
	 *
	 * @return keyword
	 */
	public String getWord()
	{
		return this.word;
	}

	/**
	 * Returns the related value.
	 *
	 * @return related value
	 */
	public V getValue()
	{
		return this.value;
	}

	/**
	 * Returns the specified successor node.
	 *
	 * @param c
	 *            character
	 * @return successor node or NULL if the specified node is not available
	 */
	public LetterNode<V> get(char c)
	{
		return this.nodes.get(c);
	}

	/**
	 * Checks whether the specified successor node is contained.
	 *
	 * @param c
	 *            character
	 * @return TRUE | FALSE
	 */
	public boolean contains(char c)
	{
		return this.nodes.containsKey(c);
	}

	/**
	 * Returns whether this node contains a keyword or not.
	 *
	 * @return TRUE | FALSE
	 */
	public boolean isKeyword()
	{
		return this.isKeyword;
	}
}
