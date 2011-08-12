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
