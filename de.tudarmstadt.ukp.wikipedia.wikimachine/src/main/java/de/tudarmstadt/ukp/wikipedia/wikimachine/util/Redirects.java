/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.wikimachine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Get the destination of a redirect using its text.
 *
 * @author Torsten Zesch
 */
public class Redirects {

	private static final Logger log4j = Logger.getLogger(Redirects.class);

	private Redirects() {
	}

	/**
	 * redirects patterns for some popular languages <br>
	 * TODO extend this list, source file codepage changes are necessary
	 *
	 * @see [Bug 86]
	 * @see http://en.wikipedia.org/wiki/Wikipedia:Redirect
	 * @author ivan.galkin
	 *
	 */
	private static final List<String> redirectPatterns = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("#REDIRECT"); // en
			add("#WEITERLEITUNG"); // de

			// add("#AANSTUUR"); // af
			// add("#SUUNA"); // et
			// add("#REDIRECCIÓN"); // es, gl
			// add("#PREUSMJERI"); // hr
			// add("#ALIH "); // id, jv
			// add("#RINVIA"); // it
			// add("#OHJAUS"); // fi
			// add("#OMDIRIGERING "); // sv

		}
	};

	/**
	 * Check if given text starts with #REDIRECT, with case ignoring
	 *
	 * @param text
	 *            given text
	 * @return true if text starts with #REDIRECT, false otherwise
	 * @author ivan.galkin
	 */
	public static boolean isRedirect(String text) {
		boolean result = false;
		String pattern;
		for (int i = 0; i < redirectPatterns.size() && !result; i++) {
			pattern = redirectPatterns.get(i);
			result = text.regionMatches(true, 0, pattern, 0, pattern.length());
		}
		return result;

	}

	/**
	 *
	 * Return the redirect destination of according to wikimedia syntax.
	 *
	 * FIXME the whole function body was temporary wrapped with try/catch to
	 * find a error in this code
	 *
	 * @param pageText
	 * @return redirect destination
	 */
	public static String getRedirectDestination(String pageText) {
		String redirectString = null;
		try {
			String regex = "\\[\\[\\s*(.+?)\\s*]]";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(pageText);

			// group 0 is the whole match
			if (matcher.find()) {
				redirectString = matcher.group(1);
			}
			if (redirectString == null) {
				return null;
			}

			// remove anchor (case: "#Redirect [[Article #Anchor]])
			String[] anchorSplitValues = redirectString.split("#");
			redirectString = anchorSplitValues[0];
			// remove whitespace (case: "Article " - when splitting the example
			// above)
			redirectString = redirectString.trim();

			// remove direct|redirect alternatives
			String[] directSplitValues = redirectString.split("\\|");
			redirectString = directSplitValues[0];
			// remove whitespace (case: "Article " - when splitting the example
			// above)
			redirectString = redirectString.trim();

			// remove whitespace (case: "Article " - when splitting the example
			// above)
			redirectString = redirectString.trim();

			// remove namespace string (case:
			// "#REDIRECT [[Portal:Recht/Liste der Rechtsthemen]]")
			// but there are names with colons in it => consider only cases
			// where
			// there are no spaces around the colon
			String regexNamespace = ":([^\\s].+)";
			Pattern patternNamespace = Pattern.compile(regexNamespace);
			Matcher matcherNamespace = patternNamespace.matcher(redirectString);

			// group 0 is the whole match
			if (matcherNamespace.find()) {
				redirectString = matcherNamespace.group(1);
			}

			// replace spaces with underscores (spaces are represented as
			// underscores in page titles)
			// e.g. "Englische Grammatik"
			redirectString = redirectString.replace(" ", "_");

			// page titles always start with a capital letter
			if (redirectString.length() > 0) {
				redirectString = redirectString.substring(0, 1).toUpperCase()
						+ redirectString.substring(1, redirectString.length());
			}
		} catch (Exception e) {
			redirectString = null;
			log4j.debug("Error in Redirects ignored");
		}

		return redirectString;
	}
}
