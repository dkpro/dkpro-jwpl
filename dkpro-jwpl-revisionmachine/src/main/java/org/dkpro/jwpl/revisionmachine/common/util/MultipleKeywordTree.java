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
package org.dkpro.jwpl.revisionmachine.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a keyword tree and is used to process or to search a character sequence.
 * <p>
 * This keyword tree can be used for overlapping keywords.
 *
 * @param <V>
 *            related value
 */
public class MultipleKeywordTree<V>
{

    /**
     * Reference to the root
     */
    private final LetterNode<V> root;

    /**
     * List of current nodes
     */
    private List<LetterNode<V>> currentList;

    /**
     * List of successor nodes
     */
    private final List<LetterNode<V>> hits;

    /**
     * Creates an empty MultipleKeywordTree object.
     */
    public MultipleKeywordTree()
    {
        root = new LetterNode<>();
        this.currentList = new ArrayList<>();
        this.hits = new ArrayList<>();
        this.currentList.add(root);
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
     * Checks whether the character is related to one of the current nodes (the root node is always
     * a current node).
     * <p>
     * After the comparison the list of current nodes will be replaced.
     *
     * @param c
     *            character
     * @return TRUE if successor nodes could be identified {@code false} otherwise
     */
    public boolean check(final char c)
    {

        List<LetterNode<V>> newList = new ArrayList<>();
        newList.add(root);

        LetterNode<V> current;
        hits.clear();

        int size = this.currentList.size();
        for (int i = 0; i < size; i++) {
            current = this.currentList.get(i);

            current = current.get(c);
            if (current != null) {
                newList.add(current);

                if (current.isKeyword()) {
                    hits.add(current);
                }
            }
        }

        this.currentList = newList;
        return !hits.isEmpty();
    }

    /**
     * Resets the list of current node to only contain the root node.
     */
    public void reset()
    {
        this.currentList.clear();
        this.currentList.add(root);
    }

    /**
     * Returns the list of successor nodes.
     *
     * @return list of successor nodes
     */
    public List<LetterNode<V>> getHits()
    {
        return this.hits;
    }
}
