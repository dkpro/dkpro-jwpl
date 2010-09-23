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

import de.tudarmstadt.ukp.wikipedia.wikimachine.util.TxtFileWriter;
import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.Files;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.MetaData;

public abstract class AbstractDumpVersion implements IDumpVersion {
	protected static final int NS_MAIN = 0;
	protected static final int NS_TALK = 1;
	protected static final int NS_CATEGORY = 14;

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
