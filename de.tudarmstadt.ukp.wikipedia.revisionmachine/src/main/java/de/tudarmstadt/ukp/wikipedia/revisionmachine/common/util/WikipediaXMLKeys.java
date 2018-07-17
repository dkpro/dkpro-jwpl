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

/**
 * This class contains all keys for wikipedia dump files.
 *
 *
 *
 */
public enum WikipediaXMLKeys
{

	/** Indicates the start of a page */
	KEY_START_PAGE("<page>"),

	/** Indicates the end of a page */
	KEY_END_PAGE("</page>"),

	/** Indicates the start of a title */
	KEY_START_TITLE("<title>"),

	/** Indicates the end of a title */
	KEY_END_TITLE("</title>"),

	/** Indicates the start of an id */
	KEY_START_ID("<id>"),

	/** Indicates the end of an id */
	KEY_END_ID("</id>"),

	/** Indicates the start of a revision */
	KEY_START_REVISION("<revision>"),

	/** Indicates the end of a revision */
	KEY_END_REVISION("</revision>"),

	/** Indicates the start of a comment */
	KEY_START_COMMENT("<comment>"),

	/** Indicates the end of a comment */
	KEY_END_COMMENT("</comment>"),

	/** Indicates the start of the contributor ip */
	KEY_START_IP("<ip>"),

	/** Indicates the end of the contributor ip */
	KEY_END_IP("</ip>"),

	/** Indicates the start of the the contributor username */
	KEY_START_USERNAME("<username>"),

	/** Indicates the end of the contributor username */
	KEY_END_USERNAME("</username>"),

	/** Indicates the start of a timestamp */
	KEY_START_TIMESTAMP("<timestamp>"),

	/** Indicates the end of a timestamp */
	KEY_END_TIMESTAMP("</timestamp>"),

	/** Indicates the start of the contributor info */
	KEY_START_CONTRIBUTOR("<contributor>"),

	/** Indicates the end of the contributor info */
	KEY_END_CONTRIBUTOR("</contributor>"),

	/** Indicates the start of the namespace block */
	KEY_START_NAMESPACES("<namespaces>"),

	/** Indicates the end of the namespace block */
	KEY_END_NAMESPACES("</namespaces>"),

	/** Indicates the start of a text segment */
	KEY_START_TEXT("<text xml:space=\"preserve\">"),

	/** Indicates the end of a text segment */
	KEY_END_TEXT("</text>"),

	/** Indicates that the revision is a minor revision */
	KEY_MINOR_FLAG("<minor />");



	/** Keyword related to the key */
	private final String keyword;

	/**
	 * Creates an enumerator with the speciefied keyword
	 *
	 * @param keyword
	 *            keyword
	 */
	private WikipediaXMLKeys(final String keyword)
	{
		this.keyword = keyword;
	}

	/**
	 * Returns the keyword
	 *
	 * @return keyword
	 */
	public String getKeyword()
	{
		return this.keyword;
	}
}
