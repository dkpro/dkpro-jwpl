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

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.Files;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.MetaData;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.TxtFileWriter;

public abstract class AbstractDumpVersion implements IDumpVersion {

	/*
	 * Wikipedia namespace codes according to
	 * http://en.wikipedia.org/wiki/Wikipedia:MediaWiki_namespace
	 */
	protected static final int NS_MAIN = 0;
	protected static final int NS_TALK = 1;
	protected static final int NS_USER = 2;
	protected static final int NS_USER_TALK = 3;
	protected static final int NS_WIKIPEDIA = 4;
	protected static final int NS_WIKIPEDIA_TALK = 5;
	protected static final int NS_FILE = 6;
	protected static final int NS_FILE_TALK = 7;
	protected static final int NS_MEDIAWIKI = 8;
	protected static final int NS_MEDIAWIKI_TALK = 9;
	protected static final int NS_TEMPLATE = 10;
	protected static final int NS_TEMPLATE_TALK = 11;
	protected static final int NS_HELP = 12;
	protected static final int NS_HELP_TALK = 13;
	protected static final int NS_CATEGORY = 14;
	protected static final int NS_CATEGORY_TALK = 15;
	protected static final int NS_THREAD = 90;
	protected static final int NS_THREAD_TALK = 91;
	protected static final int NS_SUMMARY = 92;
	protected static final int NS_SUMMARY_TALK = 93;
	protected static final int NS_PORTAL = 100;
	protected static final int NS_PORTAL_TALK = 101;
	protected static final int NS_BOOK = 108;
	protected static final int NS_BOOK_TALK = 109;

	protected int timestamp;
	protected MetaData metaData;

	protected TxtFileWriter txtFW;
	protected TxtFileWriter pageCategories;
	protected TxtFileWriter categoryPages;
	protected TxtFileWriter categoryInlinks;
	protected TxtFileWriter categoryOutlinks;
	protected TxtFileWriter pageInlinks;
	protected TxtFileWriter pageOutlinks;
	protected TxtFileWriter page = null;
	protected TxtFileWriter pageMapLine = null;
	protected TxtFileWriter pageRedirects = null;

	protected Files versionFiles;
	protected ILogger logger = null;

	protected boolean skipCategory = true;
	protected boolean skipPage = true;

	@Override
	public void setCategoryRedirectsSkip(boolean skipCategory) {
		this.skipCategory = skipCategory;
	}

	@Override
	public void setPageRedirectsSkip(boolean skipPage) {
		this.skipPage = skipPage;
	}

	@Override
	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	@Override
	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}

	@Override
	public void setFiles(Files versionFiles) {
		this.versionFiles = versionFiles;
	}

	@Override
	public void initPageParsing() throws IOException {
		txtFW = new TxtFileWriter(versionFiles.getOutputCategory());
	}

	@Override
	public void exportAfterPageParsing() throws IOException {
		txtFW.flush();
		txtFW.close();
	}

	@Override
	public void initCategoryLinksParsing() throws IOException {
		pageCategories = new TxtFileWriter(versionFiles
				.getOutputPageCategories());
		categoryPages = new TxtFileWriter(versionFiles.getOutputCategoryPages());
		categoryInlinks = new TxtFileWriter(versionFiles
				.getOutputCategoryInlinks());
		categoryOutlinks = new TxtFileWriter(versionFiles
				.getOutputCategoryOutlinks());

	}

	@Override
	public void exportAfterCategoryLinksParsing() throws IOException {
		pageCategories.flush();
		pageCategories.close();

		categoryPages.flush();
		categoryPages.close();

		categoryInlinks.flush();
		categoryInlinks.close();

		categoryOutlinks.flush();
		categoryOutlinks.close();
	}

	@Override
	public void initPageLinksParsing() throws IOException {
		pageInlinks = new TxtFileWriter(versionFiles.getOutputPageInlinks());
		pageOutlinks = new TxtFileWriter(versionFiles.getOutputPageOutlinks());
	}

	@Override
	public void exportAfterPageLinksParsing() throws IOException {
		// export the written tables
		pageInlinks.flush();
		pageInlinks.close();

		pageOutlinks.flush();
		pageOutlinks.close();
	}

	@Override
	public void initTextParsing() throws IOException {
		page = new TxtFileWriter(versionFiles.getOutputPage());
		pageMapLine = new TxtFileWriter(versionFiles.getOutputPageMapLine());
		pageRedirects = new TxtFileWriter(versionFiles.getOutputPageRedirects());

	}

	@Override
	public void exportAfterTextParsing() throws IOException {
		page.flush();
		page.close();

		pageRedirects.flush();
		pageRedirects.close();

		pageMapLine.flush();
		pageMapLine.close();

	}

	@Override
	public void flushByTextParsing() throws IOException {
		page.flush();
		pageMapLine.flush();
		pageRedirects.flush();
	}

	@Override
	public void exportAfterRevisionParsing() {

	}

	@Override
	public void initRevisionParsion() {

	}

	/**
	 * Returns the String value of the bit 1 if the given boolean is true<br>
	 * and an empty String otherwise. This the way bit values are written<br>
	 * in .txt dump files.
	 *
	 * @param b
	 * @return
	 */
	protected String formatBoolean(boolean b) {
		return b ? new String(new byte[] { 1 }) : "";
	}
}
