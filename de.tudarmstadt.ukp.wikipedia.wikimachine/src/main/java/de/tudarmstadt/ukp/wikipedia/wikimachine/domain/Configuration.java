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
package de.tudarmstadt.ukp.wikipedia.wikimachine.domain;

import java.sql.Timestamp;

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;

/**
 * The <code>Configuration</code> class wraps all the parameters for the
 * execution <br>
 * of the DBMapping tool.
 *
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
