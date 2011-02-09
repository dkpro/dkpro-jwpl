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
/**
 * @(#)Configuration.java
 */
package de.tudarmstadt.ukp.wikipedia.wikimachine.domain;

import java.sql.Timestamp;

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;

/**
 * The <code>Configuration</code> class wraps all the parameters for the
 * execution <br>
 * of the DBMapping tool.
 *
 * @author Anouar
 *
 */
public class Configuration {

	private static final Timestamp TIMESTAMP_UNDEFINED = new Timestamp(
			Long.MIN_VALUE);

	private Timestamp fromTimestamp = TIMESTAMP_UNDEFINED;
	private Timestamp toTimestamp = TIMESTAMP_UNDEFINED;
	private int each;
	private String language;
	private String mainCategory;
	private String disambiguationCategory;

	protected ILogger logger;

	public Configuration(ILogger logger) {
		this.logger = logger;
	}

	public Timestamp getFromTimestamp() {
		return fromTimestamp;
	}

	public void setFromTimestamp(Timestamp fromTimestamp) {
		this.fromTimestamp = fromTimestamp;
	}

	public Timestamp getToTimestamp() {
		return toTimestamp;
	}

	public void setToTimestamp(Timestamp toTimestamp) {
		this.toTimestamp = toTimestamp;
	}

	public boolean checkTimestamp() {
		boolean result = !toTimestamp.equals(TIMESTAMP_UNDEFINED)
				&& !fromTimestamp.equals(TIMESTAMP_UNDEFINED)
				&& (this.toTimestamp.after(this.fromTimestamp)||this.toTimestamp.equals(this.fromTimestamp));
		if (!result) {

			logger.log("fromTimestamp is after toTimestamp");
		}
		return result;
	}

	public int getEach() {
		return each;
	}

	public void setEach(int each) {
		this.each = each;
	}

	public boolean checkEach() {
		boolean result = each > 0;
		if (!result) {
			logger.log("'each' must be positive");
		}
		return result;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getMainCategory() {
		return mainCategory;
	}

	public void setMainCategory(String mainCategory) {
		this.mainCategory = mainCategory;
	}

	public String getDisambiguationCategory() {
		return disambiguationCategory;
	}

	public void setDisambiguationCategory(String disambiguationCategory) {
		this.disambiguationCategory = disambiguationCategory;
	}

	public boolean checkTimeConfig() {
		return checkEach() && checkTimestamp();
	}
}
