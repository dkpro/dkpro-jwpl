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
package org.dkpro.jwpl.wikimachine.dump.version;

import java.io.IOException;
import java.sql.Timestamp;

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.domain.Files;
import org.dkpro.jwpl.wikimachine.domain.MetaData;
import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;

/**
 * An interface to abstract from DumpVersion realization
 *
 *
 */
public interface IDumpVersion {

	void setLogger(ILogger logger);

	void setCategoryRedirectsSkip(boolean skipCategory);

	void setPageRedirectsSkip(boolean skipPage);

	// initialize
	void initialize(Timestamp timestamp);

	void setMetaData(MetaData commonMetaData);

	void setFiles(Files versionFiles);

	// parse revisions
	void initRevisionParsion();

	void processRevisionRow(RevisionParser revisionParser);

	void exportAfterRevisionParsing() throws IOException;

	void freeAfterRevisonParsing();

	// parse pages
	void initPageParsing() throws IOException;

	void processPageRow(PageParser pageParser) throws IOException;

	void exportAfterPageParsing() throws IOException;

	void freeAfterPageParsing();

	// parse category links
	void initCategoryLinksParsing() throws IOException;

	void processCategoryLinksRow(CategorylinksParser clParser)
			throws IOException;

	void exportAfterCategoryLinksParsing() throws IOException;

	void freeAfterCategoryLinksParsing();

	// parse page links
	void initPageLinksParsing() throws IOException;

	void processPageLinksRow(PagelinksParser plParser) throws IOException;

	void exportAfterPageLinksParsing() throws IOException;

	void freeAfterPageLinksParsing();

	// parse text
	void initTextParsing() throws IOException;

	void processTextRow(TextParser textParser) throws IOException;

	void flushByTextParsing() throws IOException;

	void exportAfterTextParsing() throws IOException;

	void freeAfterTextParsing();

	// write meta data
	void writeMetaData() throws IOException;

}
