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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.article.reader;

/**
 * Interface for the EnglishArticleNameChecker
 *
 * This interface can be used to filter articles by name.
 *
 *
 *
 */
public abstract class AbstractNameChecker
{
	/**
	 * Defines the allowed page name prefixed for the current language
	 */
	//TODO the prefixes should be made configurable
	private String[] ALLOWED_PREFIXES;

	/**
	 * Filter any pages with prefixes that are not whitelisted
	 *
	 * @param title
	 *            the page title
	 * @return true, if the page should be used. false, else
	 */
	public boolean checkArticle(String title)
	{
		// Allow pages without prefix
		//FIXME: This is a bug! We should use namespace mappings from the dump file. They should be read and forwarded by the WikipediaXMLReader
		if (!title.contains(":")) {
			return true;
		}

		// ALlow pages with whitespaces prefix
		for (String str : ALLOWED_PREFIXES) {
			if (title.startsWith(str)) {
				return true;
			}
		}

		return false;
	}

}
