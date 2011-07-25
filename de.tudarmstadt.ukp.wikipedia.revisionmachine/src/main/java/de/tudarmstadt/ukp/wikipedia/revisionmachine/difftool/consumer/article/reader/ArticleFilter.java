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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;

/**
 * Filter articles from unwanted namespaces.<br/>
 * The namespaces are read in from the <siteinfo> of the Wikipedia dump. The
 * corresponding prefixes of the language version are then used by the filter to
 * determine whether an article is part of an unwanted namespace or not. <br/>
 * 
 * If the ArticleFilter is not initialized or given an empty list of namespaces,
 * nothing is filtered at all.
 * 
 * 
 */
public class ArticleFilter
{
	private Map<Integer, String> namespaceMap;

	private Set<String> prefixesToAllow;

	private Set<String> prefixesToReject;

	private Collection<Integer> allowedNamespaces;

	private boolean excludeMainNamespace;

	private final int MAIN_NAMESPACE = 0;

	private static ConfigurationManager config;

	static {
		try {
			config = ConfigurationManager.getInstance();
		}
		catch (ConfigurationException e) {
			// TODO logger
			System.err.print(e);
		}
	}

	/**
	 * Creates an ArticleFilter that uses configuration file to filter prefixes
	 * 
	 * @throws ConfigurationException
	 */
	@SuppressWarnings("unchecked")
	public ArticleFilter()
		throws ConfigurationException
	{
		this((Set<Integer>) config
				.getConfigParameter(ConfigurationKeys.NAMESPACES_TO_KEEP));
	}

	/**
	 * Creates a new filter that filters all pages except the namespaces
	 * provided in the namespaceWhitelist
	 * 
	 * @param namespaceWhitelist
	 *            list of namespaces that should NOT be filtered
	 */
	public ArticleFilter(Collection<Integer> namespaceWhitelist)
	{
		this.allowedNamespaces = namespaceWhitelist;

		if (!this.allowedNamespaces.contains(MAIN_NAMESPACE)) {
			this.excludeMainNamespace = true;
		}

	}

	/**
	 * Initialized the Namespace-Prefix mapping for the current language version
	 * of Wikipedia.
	 * 
	 * @param namespaceMap
	 *            mapping of namespace ids to the corresponding article title
	 *            prefixes
	 */
	public void initializeNamespaces(Map<Integer, String> namespaceMap)
	{
		this.namespaceMap = namespaceMap;
		initializePrefixes();
	}

	/**
	 * Initialize allowed and restricted prefixes
	 */
	private void initializePrefixes()
	{
		if (namespaceMap == null) {
			// TODO use logger
			System.err
					.println("Cannot use whitespace filter without initializing the namespace-prefix map for the current Wikipedia language version. DISABLING FILTER.");
		}
		else {
			prefixesToAllow = new HashSet<String>();
			prefixesToReject = new HashSet<String>();

			for (Entry<Integer, String> namespace : namespaceMap.entrySet()) {
				if (allowedNamespaces.contains(namespace.getKey())) {
					prefixesToAllow.add(namespace.getValue() + ":");
				}
				else {
					prefixesToReject.add(namespace.getValue() + ":");
				}
			}
		}
	}

	/**
	 * Filter any pages by title prefixes
	 * 
	 * @param title
	 *            the page title
	 * @return true, if the page should be used. false, else
	 */
	public boolean checkArticle(String title)
	{
		// if filter isn't initialized, do not filter at all
		if (namespaceMap == null || namespaceMap.size() == 0
				|| allowedNamespaces == null || allowedNamespaces.size() == 0) {
			return true;
		}
		// else, do filter
		else {

			// perform filtering

			// reject restricted titles
			for (String str : prefixesToReject) {
				if (title.startsWith(str)) {
					return false;
				}
			}

			for (String str : prefixesToAllow) {
				// allows allowed prefixes
				if (title.startsWith(str)) {
					return true;
				}
				// special case for Main Namespace(Main Namespace has not any
				// prefixes)
				if (excludeMainNamespace) {
					return false;
				}

			}

			return true;
		}
	}

}
