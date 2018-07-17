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
package de.tudarmstadt.ukp.wikipedia.util.templates.generator.simple;

import java.util.HashSet;

/**
 * This class is used for filtering templates by they names using different
 * white/black lists
 *
 *
 */
public class TemplateFilter
{
	private HashSet<String> whiteList = new HashSet<String>();
	private HashSet<String> whitePrefixList = new HashSet<String>();
	private HashSet<String> blackList = new HashSet<String>();
	private HashSet<String> blackPrefixList = new HashSet<String>();

	/**
	 * Init template filter with different lists
	 *
	 * @param whiteList
	 *            list with allowed template names
	 * @param whitePrefixList
	 *            list with allowed template prefixes
	 * @param blackList
	 *            list with prohibited template names
	 * @param blackPrefixList
	 *            list with prohibited template prefixes
	 */
	public TemplateFilter(HashSet<String> whiteList,
			HashSet<String> whitePrefixList, HashSet<String> blackList,
			HashSet<String> blackPrefixList)
	{
		this.whiteList = whiteList;
		this.whitePrefixList = whitePrefixList;
		this.blackList = blackList;
		this.blackPrefixList = blackPrefixList;

	}

	/**
	 * Checks if the input string is in white list
	 *
	 * @param tpl
	 *            string to check
	 * @return
	 */
	private boolean isInWhiteList(String tpl)
	{
		if ((!whiteList.isEmpty() && whiteList.contains(tpl))
				|| (whiteList.isEmpty())) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the input string is in black list
	 *
	 * @param tpl
	 *            string to check
	 * @return
	 */
	private boolean isInBlackList(String tpl)
	{
		if (blackList.contains(tpl)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the input string contains prefixes from white list
	 *
	 * @param tpl
	 *            string to check
	 * @return
	 */
	private boolean containsAllowedPrefix(String tpl)
	{
		if (whitePrefixList.isEmpty())
			return true;

		for (String i : whitePrefixList) {
			if (tpl.startsWith(i))
				return true;
		}
		return false;
	}

	/**
	 * Checks if the input string contains prefixes from black list
	 *
	 * @param tpl
	 *            string to check
	 * @return
	 */
	private boolean containsRestrictedPrefix(String tpl)
	{
		for (String i : blackPrefixList) {
			if (tpl.startsWith(i))
				return true;
		}
		return false;
	}

	/**
	 * Checks whether to include the template with the given name in the
	 * database or not.
	 *
	 * @param tpl
	 *            the template name
	 * @return true, if the template should be included in the db
	 */
	public boolean acceptTemplate(String tpl)
	{

		if (isInWhiteList(tpl) && !isInBlackList(tpl)) {
			if (containsAllowedPrefix(tpl) && !containsRestrictedPrefix(tpl)) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}

	}

}
