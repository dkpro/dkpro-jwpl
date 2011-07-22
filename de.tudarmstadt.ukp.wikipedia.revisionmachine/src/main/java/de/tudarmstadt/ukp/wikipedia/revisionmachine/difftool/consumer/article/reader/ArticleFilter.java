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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Filter articles from unwanted namespaces.<br/>
 * The namespaces are read in from the <siteinfo> of the Wikipedia dump.
 * The corresponding prefixes of the language version are then used by the
 * filter to determine whether an article is part of an unwanted namespace or
 * not. <br/>
 *
 * If the ArticleFilter is not initialized or given an empty list of namespaces,
 * nothing is filtered at all.
 *
 *
 */
public class ArticleFilter
{
	Map<Integer, String> namespaceMap=null;
	Collection<String> allowedPrefixes=null;
	Collection<Integer> allowedNamespaces=null;

	/**
	 * Creates an uninitialized ArticleFilter that won't filter anything.
	 *
	 * @param namespaceWhitelist list of namespaces that should NOT be filtered
	 */
	public ArticleFilter(){
	}

	/**
	 * Creates a new filter that filters all pages except the namespaces
	 * provided in the namespaceWhitelist
	 *
	 * @param namespaceWhitelist list of namespaces that should NOT be filtered
	 */
	public ArticleFilter(Collection<Integer> namespaceWhitelist){
		this.allowedNamespaces=namespaceWhitelist;
	}


	/**
	 * Initialized the Namespace-Prefix mapping for the current language version
	 * of Wikipedia.
	 *
	 * @param namespaceMap
	 *            mapping of namespace ids to the corresponding article title
	 *            prefixes
	 */
	public void initializeNamespaces(Map<Integer, String> namespaceMap){
		this.namespaceMap=namespaceMap;
	}

	public void initializeAllowedPrefixes(){
		if(namespaceMap==null){
			//TODO user logger
			System.err.println("Cannot use whitespace filter without initializing the namespace-prefix map for the current Wikipedia language version. DISABLING FILTER.");
		}else{
			allowedPrefixes=new LinkedList<String>();
			for(Integer allowedNS:allowedNamespaces){
				allowedPrefixes.add(namespaceMap.get(allowedNS));
			}
		}
	}


	/**
	 * Filter any pages with prefixes that are not whitelisted
	 *
	 * @param title
	 *            the page title
	 * @return true, if the page should be used. false, else
	 */
	public boolean checkArticle(String title)
	{
		//if filter isn't initialized, do not filter at all
		if(namespaceMap==null||allowedNamespaces==null||allowedNamespaces.size()==0){
			return true;
		}
		//else, do filter
		else{
			if(allowedPrefixes==null){
				//if called for the first time, init prefixes
				initializeAllowedPrefixes();
			}
			//perform filtering
			for (String str : allowedPrefixes) {
				if (title.startsWith(str)) {
					return true;
				}
			}
			return false;
		}
	}

}
