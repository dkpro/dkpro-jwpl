/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.wikimachine.domain;

import java.io.IOException;

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql.CategorylinksParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql.PagelinksParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.PageParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.RevisionParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.TextParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.version.IDumpVersion;

public class DumpVersionProcessor {

	private static ILogger logger;

	private Integer step2Log = 100000;
	private Integer step2GC = step2Log * 10;
	private Integer step2Flush = step2GC;

	public DumpVersionProcessor(ILogger initialLogger) {
		logger = initialLogger;
	}

	private IDumpVersion[] versions;

	public void setDumpVersions(IDumpVersion[] versions) {
		this.versions = versions;
	}

	public void setStep2Log(Integer step2Log) {
		this.step2Log = step2Log;
	}

	public void setStep2GC(Integer step2GC) {
		this.step2GC = step2GC;
	}

	public void setStep2Flush(Integer step2Flush) {
		this.step2Flush = step2Flush;
	}

	public void processRevision(RevisionParser revisionParser)
			throws IOException {
		for (IDumpVersion version : versions) {
			version.initRevisionParsion();
		}
		int counter = 0;
		while (revisionParser.next()) {
			for (IDumpVersion version : versions) {
				version.processRevisionRow(revisionParser);
			}

			logAndClear(++counter, "Revision");
		}

		for (IDumpVersion version : versions) {
			version.exportAfterRevisionParsing();
			version.freeAfterRevisonParsing();
		}

		revisionParser.close();
	}

	public void processPage(PageParser pageParser) throws IOException {
		for (IDumpVersion version : versions) {
			version.initPageParsing();
		}

		int counter = 0;
		while (pageParser.next()) {
			for (IDumpVersion version : versions) {
				version.processPageRow(pageParser);
			}
			logAndClear(++counter, "Pages");
		}

		for (IDumpVersion version : versions) {
			version.exportAfterPageParsing();
			version.freeAfterPageParsing();
		}

		pageParser.close();
	}

	public void processCategorylinks(CategorylinksParser categorylinksParser)
			throws IOException {
		for (IDumpVersion version : versions) {
			version.initCategoryLinksParsing();
		}

		int counter = 0;
		while (categorylinksParser.next()) {
			for (IDumpVersion version : versions) {
				version.processCategoryLinksRow(categorylinksParser);
			}
			logAndClear(++counter, "Categorylinks");
		}

		for (IDumpVersion version : versions) {
			version.exportAfterCategoryLinksParsing();
			version.freeAfterCategoryLinksParsing();
		}

		categorylinksParser.close();
	}

	public void processPagelinks(PagelinksParser pagelinksParser)
			throws IOException {
		for (IDumpVersion version : versions) {
			version.initPageLinksParsing();
		}

		int counter = 0;
		while (pagelinksParser.next()) {
			for (IDumpVersion version : versions) {
				version.processPageLinksRow(pagelinksParser);
			}
			logAndClear(++counter, "Pagelinks");
		}

		for (IDumpVersion version : versions) {
			version.exportAfterPageLinksParsing();
			version.freeAfterPageLinksParsing();
		}

		pagelinksParser.close();
	}

	public void processText(TextParser textParser) throws IOException {
		for (IDumpVersion version : versions) {
			version.initTextParsing();
		}

		int counter = 0;
		while (textParser.next()) {
			for (IDumpVersion version : versions) {
				version.processTextRow(textParser);
			}
			if (step2Flush != 0 && counter % step2Flush == 0) {
				for (IDumpVersion version : versions) {
					version.flushByTextParsing();
				}
			}
			logAndClear(++counter, "Text");

		}

		for (IDumpVersion version : versions) {
			version.exportAfterTextParsing();
			version.freeAfterTextParsing();
		}

		textParser.close();
	}

	public void writeMetaData() throws IOException {
		for (IDumpVersion version : versions) {
			version.writeMetaData();
		}
	}

	private void logAndClear(int counter, String event) throws IOException {
		if (step2Log != 0 && counter % step2Log == 0) {
			String message = event + " " + counter;
			logger.log(message);
		}
	}

}
