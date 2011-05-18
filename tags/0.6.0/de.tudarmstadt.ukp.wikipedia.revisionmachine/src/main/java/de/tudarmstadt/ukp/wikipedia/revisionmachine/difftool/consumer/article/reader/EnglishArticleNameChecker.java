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
 * Filter for English Wikipedia
 * Filters unwanted pages according to given prefixes.
 */
public class EnglishArticleNameChecker
	extends AbstractNameChecker
{

	//Whitelist of page name prefixes for English
	private final String[] ALLOWED_PREFIXES = {"Talk:"};

	@Override
	public boolean checkArticle(String title)
	{
		//Allow pages without prefix
		if (!title.contains(":")){
			return true;
		}

		//ALlow pages with whitespaces prefix
		for(String str:ALLOWED_PREFIXES){
			if(title.startsWith(str)) {
				return true;
			}
		}

		return false;
	}

}
