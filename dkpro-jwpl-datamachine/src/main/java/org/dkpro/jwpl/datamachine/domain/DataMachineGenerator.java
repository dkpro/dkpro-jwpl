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
package org.dkpro.jwpl.datamachine.domain;

import java.io.IOException;

import org.dkpro.jwpl.datamachine.dump.xml.XML2Binary;
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

/**
 * Transforms a database from mediawiki format to JWPL format.<br>
 * The transformation produces .txt files for the different tables in the JWPL
 * database.
 *
 *
 */
public class DataMachineGenerator extends AbstractSnapshotGenerator {

	DataMachineFiles files = null;
	IDumpVersion version = null;

	public DataMachineGenerator(IEnvironmentFactory environmentFactory) {
		super(environmentFactory);
	}

	@Override
	public void setFiles(Files files) {
		this.files = (DataMachineFiles) files;
	}

	@Override
	public void start() throws Exception {
		version = environmentFactory.getDumpVersion();
		MetaData metaData = MetaData.initWithConfig(configuration);
		version.initialize(null);
		version.setMetaData(metaData);
		version.setFiles(files);
		processInputDump();
	}

	private void processInputDump() throws IOException {

		logger.log("parse input dumps...");
		new XML2Binary(decompressor.getInputStream(getPagesArticlesFile()),
				files);
		
		
		dumpVersionProcessor.setDumpVersions(new IDumpVersion[] { version });

		logger.log("processing table page...");
		dumpVersionProcessor.processPage(createPageParser());

		logger.log("processing table categorylinks...");
		dumpVersionProcessor.processCategorylinks(createCategorylinksParser());

		logger.log("processing table pagelinks...");
		dumpVersionProcessor.processPagelinks(createPagelinksParser());

		logger.log("processing table revision...");
		dumpVersionProcessor.processRevision(createRevisionParser());

		logger.log("processing table text...");
		dumpVersionProcessor.processText(createTextParser());

		logger.log("writing metadata...");
		dumpVersionProcessor.writeMetaData();

		logger.log("finished");
	}

	/**
	 * Parse either "pages-articles.xml" or "pages-meta-current.xml". If both
	 * files exist in the input directory "pages-meta-current.xml" will be
	 * favored.
	 *
	 * @return the input articles dump
	 */
	private String getPagesArticlesFile() {
		String pagesArticlesFile = null;
		String parseMessage = null;

		//Use of minimal dump only with articles
		if (files.getInputPagesArticles() != null) {
			pagesArticlesFile = files.getInputPagesArticles();
			parseMessage = "Discussions are unavailable";
		}

		//Use of dump with discussions
		if (files.getInputPagesMetaCurrent() != null) {
			pagesArticlesFile = files.getInputPagesMetaCurrent();
			parseMessage = "Discussions are available";
		}

		logger.log(parseMessage);
		return pagesArticlesFile;
	}

	private PageParser createPageParser() throws IOException {
		String pageFile = files.getGeneratedPage();

		DumpTableInputStream pageTableInputStream = environmentFactory
				.getDumpTableInputStream();
		pageTableInputStream.initialize(decompressor.getInputStream(pageFile),
				DumpTableEnum.PAGE);

		PageParser pageParser = environmentFactory.getPageParser();
		pageParser.setInputStream(pageTableInputStream);
		return pageParser;
	}

	private CategorylinksParser createCategorylinksParser() throws IOException {
		String categorylinksFile = files.getInputCategoryLinks();
		return new CategorylinksParser(decompressor
				.getInputStream(categorylinksFile));
	}

	private PagelinksParser createPagelinksParser() throws IOException {
		String pagelinksFile = files.getInputPageLinks();
		return new PagelinksParser(decompressor.getInputStream(pagelinksFile));
	}

	private RevisionParser createRevisionParser() throws IOException {
		String revisionFile = files.getGeneratedRevision();

		DumpTableInputStream revisionTableInputStream = environmentFactory
				.getDumpTableInputStream();
		revisionTableInputStream.initialize(decompressor
				.getInputStream(revisionFile), DumpTableEnum.REVISION);

		RevisionParser revisionParser = environmentFactory.getRevisionParser();
		revisionParser.setInputStream(revisionTableInputStream);
		return revisionParser;
	}

	private TextParser createTextParser() throws IOException {
		String textFile = files.getGeneratedText();

		DumpTableInputStream textTableInputStream = environmentFactory
				.getDumpTableInputStream();
		textTableInputStream.initialize(decompressor.getInputStream(textFile),
				DumpTableEnum.TEXT);

		TextParser textParser = environmentFactory.getTextParser();
		textParser.setInputStream(textTableInputStream);
		return textParser;
	}

}
