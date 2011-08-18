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
package de.tudarmstadt.ukp.wikipedia.wikimachine.dump.version;

import java.io.IOException;
import java.sql.Timestamp;

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.Files;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.MetaData;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql.CategorylinksParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql.PagelinksParser;

import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.PageParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.RevisionParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.TextParser;

/**
 * An interface to abstract from DumpVersion realization
 * 
 * @author ivan.galkin
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
