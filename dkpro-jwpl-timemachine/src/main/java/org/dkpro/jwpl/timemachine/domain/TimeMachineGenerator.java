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
package org.dkpro.jwpl.timemachine.domain;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

import org.dkpro.jwpl.wikimachine.domain.AbstractSnapshotGenerator;
import org.dkpro.jwpl.wikimachine.domain.Files;
import org.dkpro.jwpl.wikimachine.domain.MetaData;
import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableEnum;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableInputStream;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;
import org.dkpro.jwpl.wikimachine.factory.IEnvironmentFactory;
import org.dkpro.jwpl.wikimachine.util.TimestampUtil;

/**
 * Generate dumps as .txt files for the JWPL database from given MediaWiki dump
 * files.<br>
 * By specifying a 'from' and a 'to' time stamps and the number of days to take
 * as interval<br>
 * this class produces multiple dump versions.
 *
 *
 */
public class TimeMachineGenerator extends AbstractSnapshotGenerator {

	private IDumpVersion[] versions = null;
	private TimeMachineFiles initialFiles = null;

	public TimeMachineGenerator(IEnvironmentFactory environmentFactory) {
		super(environmentFactory);
	}

	@Override
	public void setFiles(Files files) {
		initialFiles = (TimeMachineFiles) files;
	}

	private Integer calculateSnapshotsCount(Timestamp from, Timestamp to,
			Integer dayInterval) {
		Integer result = 0;

		for (Timestamp i = from; i.before(to); i = TimestampUtil
				.getNextTimestamp(i, dayInterval)) {
			result++;
		}

		return result;
	}

	@Override
	public void start() throws Exception {

		Timestamp fromTimestamp = configuration.getFromTimestamp();
		Timestamp toTimestamp = configuration.getToTimestamp();
		Integer each = configuration.getEach();

		Integer snapshotsCount = fromTimestamp.equals(toTimestamp) ? 1
				: calculateSnapshotsCount(fromTimestamp, toTimestamp, each);

		if (snapshotsCount > 0) {

			versions = new IDumpVersion[snapshotsCount];
			logger.log("Dumps to be generated:");

			for (int i = 0; i < snapshotsCount; i++) {

				Timestamp currentTimestamp = TimestampUtil.getNextTimestamp(
						fromTimestamp, i * each);
				logger.log(currentTimestamp);

				MetaData commonMetaData = MetaData
						.initWithConfig(configuration);
				commonMetaData.setTimestamp(currentTimestamp);

				IDumpVersion version = environmentFactory.getDumpVersion();

				version.initialize(currentTimestamp);
				version.setMetaData(commonMetaData);
				TimeMachineFiles currentFiles = new TimeMachineFiles(
						initialFiles);
				currentFiles.setTimestamp(currentTimestamp);
				version.setFiles(currentFiles);
				versions[i] = version;
			}

			processInputDumps();

		} else {
			logger.log("No timestamps.");
		}
	}

	private void processInputDumps() throws IOException {

		dumpVersionProcessor.setDumpVersions(versions);

		logger.log("Processing the revision table");
		dumpVersionProcessor.processRevision(createRevisionParser());

		logger.log("Processing the page table");
		dumpVersionProcessor.processPage(createPageParser());

		logger.log("Processing the categorylinks table");
		dumpVersionProcessor.processCategorylinks(createCategorylinksParser());

		logger.log("Processing the pagelinks table");
		dumpVersionProcessor.processPagelinks(createPagelinksParser());

		logger.log("Processing the text table");
		dumpVersionProcessor.processText(createTextParser());

		logger.log("Writing meta data");
		dumpVersionProcessor.writeMetaData();
	}

	private RevisionParser createRevisionParser() throws IOException {

		String metahistory = initialFiles.getMetaHistoryFile();

		DumpTableInputStream revisionTableInputStream = environmentFactory
				.getDumpTableInputStream();
		revisionTableInputStream.initialize(decompressor
				.getInputStream(metahistory), DumpTableEnum.REVISION);

		RevisionParser revisionParser = environmentFactory.getRevisionParser();
		revisionParser.setInputStream(revisionTableInputStream);

		return revisionParser;

	}

	private PageParser createPageParser() throws IOException {

		String metahistory = initialFiles.getMetaHistoryFile();

		DumpTableInputStream pageTableInputStream = environmentFactory
				.getDumpTableInputStream();
		pageTableInputStream.initialize(decompressor
				.getInputStream(metahistory), DumpTableEnum.PAGE);

		PageParser pageParser = environmentFactory.getPageParser();
		pageParser.setInputStream(pageTableInputStream);

		return pageParser;

	}

	private CategorylinksParser createCategorylinksParser() throws IOException {

		String categorylinks = initialFiles.getCategoryLinksFile();
		InputStream categorylinksStream = decompressor
				.getInputStream(categorylinks);

		return new CategorylinksParser(categorylinksStream);

	}

	private PagelinksParser createPagelinksParser() throws IOException {

		String pagelinks = initialFiles.getPageLinksFile();

		InputStream pagelinksStream = decompressor.getInputStream(pagelinks);
		return new PagelinksParser(pagelinksStream);

	}

	private TextParser createTextParser() throws IOException {

		String metahistory = initialFiles.getMetaHistoryFile();

		DumpTableInputStream textTableIntputStream = environmentFactory
				.getDumpTableInputStream();
		textTableIntputStream.initialize(decompressor
				.getInputStream(metahistory), DumpTableEnum.TEXT);

		TextParser textParser = environmentFactory.getTextParser();
		textParser.setInputStream(textTableIntputStream);

		return textParser;

	}

}
