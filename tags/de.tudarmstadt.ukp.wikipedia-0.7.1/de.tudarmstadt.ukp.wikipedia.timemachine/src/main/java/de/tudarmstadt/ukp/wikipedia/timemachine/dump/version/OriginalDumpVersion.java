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
package de.tudarmstadt.ukp.wikipedia.timemachine.dump.version;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.ukp.wikipedia.timemachine.domain.Revision;
import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.Files;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.MetaData;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql.CategorylinksParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql.PagelinksParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.version.IDumpVersion;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.PageParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.RevisionParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.TextParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.Redirects;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.TimestampUtil;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.TxtFileWriter;

/**
 * This class holds the data for a specific dump version.
 * 
 * @author Anouar
 * 
 */

public class OriginalDumpVersion implements IDumpVersion {

	private Timestamp timestamp;
	private MetaData metaData;

	// XXX ivan.galkin
	@SuppressWarnings("unused")
	private String outputPath;
	// XXX ivan.galkin
	// private Map<Integer, Revision> pageIdRevMap; // maps page id's to
	// Revision
	// objects
	private Set<Integer> disambiguations; // caches the page id's of
	// disambiguation pages.
	private Map<Integer, Integer> textIdPageIdMap;// maps text id's to the page
	// id's.
	private Map<Integer, String> pPageIdNameMap;// maps page id's of pages to
	// their names
	private Map<Integer, String> cPageIdNameMap;// maps page id's of categories
	// to their names
	private Map<String, Integer> pNamePageIdMap;// maps names of pages to their
	// page id's.
	private Map<String, Integer> cNamePageIdMap;// maps names of categories to
	// their page id's.
	private Map<Integer, String> rPageIdNameMap;// maps page id's of redirects

	// to their names.

	// XXX ivan.galkin
	private Files versionFiles;
	private Map<Integer, Long> pageIdRevMap;
	private boolean skipCategory = true;
	private boolean skipPage = true;

	/**
	 * Creates a new DumpVersion that corresponds to the specified time stamp.
	 * 
	 * @param timestamp
	 */
	public OriginalDumpVersion(Timestamp timestamp) {
		// XXX ivan.galkin
		// this.timestamp = timestamp;
		// pageIdRevMap = new HashMap<Integer, Revision>();
		pageIdRevMap = new HashMap<Integer, Long>();
		disambiguations = new HashSet<Integer>();
		textIdPageIdMap = new HashMap<Integer, Integer>();
		pPageIdNameMap = new HashMap<Integer, String>();
		cPageIdNameMap = new HashMap<Integer, String>();
		pNamePageIdMap = new HashMap<String, Integer>();
		cNamePageIdMap = new HashMap<String, Integer>();
		rPageIdNameMap = new HashMap<Integer, String>();

	}

	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}

	public void setOutputPath(String outputPath) throws IOException {
		this.outputPath = outputPath;
		File directory = new File(outputPath);
		directory.mkdir();
	}

	public void processRevisionRow(RevisionParser revisionParser) {
		int rev_page;
		Timestamp rev_timestamp;
		Timestamp old_timestamp;
		int old_text_id;
		// get the rev_page (corresponds to page_id in the table page)
		rev_page = revisionParser.getRevPage();
		// get the timestamp of the revision

		// XXX ivan.galkin
		rev_timestamp = new Timestamp(Revision.extractTime(revisionParser
				.getRevTimestamp()));

		if (rev_timestamp.before(timestamp)) {

			if (pageIdRevMap.containsKey(rev_page)) {
				// XXX ivan.galkin go back to the time stamp classes
				old_timestamp = new Timestamp(Revision.extractTime(Revision
						.getTimestamp(pageIdRevMap.get(rev_page))));
				old_text_id = Revision.getTextId(pageIdRevMap.get(rev_page));
				// is it a better time stamp ?
				if (rev_timestamp.after(old_timestamp)) {
					pageIdRevMap.remove(rev_page);
					pageIdRevMap.put(rev_page, Revision.createRevision(
							revisionParser.getRevTextId(), Revision
									.compressTime(rev_timestamp.getTime())));
					textIdPageIdMap.remove(old_text_id);
					textIdPageIdMap
							.put(revisionParser.getRevTextId(), rev_page);
				}
			} else {
				// this is the first recoreded time stamp for that page id
				pageIdRevMap.put(rev_page, Revision.createRevision(
						revisionParser.getRevTextId(), Revision
								.compressTime(rev_timestamp.getTime())));
				textIdPageIdMap.put(revisionParser.getRevTextId(), rev_page);
			}

		}
	}

	TxtFileWriter txtFW = null;

	public void initPageParsing() throws IOException {
		// XXX ivan.galkin
		// txtFW = new TxtFileWriter(outputPath + "/Category.txt");
		txtFW = new TxtFileWriter(versionFiles.getOutputCategory());
	}

	public void processPageRow(PageParser pageParser) throws IOException {

		int page_id;
		int page_namespace;
		String page_title;
		page_namespace = pageParser.getPageNamespace();
		// handle categories
		if (page_namespace == 14) {
			if (skipCategory && pageParser.getPageIsRedirect())
				// skip categories that are redirects
				return;
			// retrieve page id and page title
			page_id = pageParser.getPageId();
			// ignore categories, which have no revisions before the timestamp
			if (!pageIdRevMap.containsKey(page_id))
				return;

			page_title = pageParser.getPageTitle();

			// cache the retrieved values
			recordCategory(page_id, page_title);
			// write a new row in the table Category.
			// Note that we also consider the page_id as id
			txtFW.addRow(page_id, page_id, page_title);
			metaData.addCategory();
			return;
		}
		// handle pages
		if (page_namespace == 0) {
			// retrieve page id and title
			page_id = pageParser.getPageId();
			page_title = pageParser.getPageTitle();
			// ignore pages, which habe no revisions prior to the timestamp
			if (!pageIdRevMap.containsKey(page_id))
				return;
			// distinguish redirects
			if (pageParser.getPageIsRedirect()) {
				recordRedirect(page_id, page_title);
			} else {
				recordPage(page_id, page_title);
			}
		}

	}

	public void exportAfterPageParsing() throws IOException {
		txtFW.export();
	}

	TxtFileWriter pageCategories = null;
	TxtFileWriter categoryPages = null;
	TxtFileWriter categoryInlinks = null;
	TxtFileWriter categoryOutlinks = null;

	public void initCategoryLinksParsing() throws IOException {
		// XXX ivan.galkin
		// pageCategories = new TxtFileWriter(outputPath + File.separator
		// + "page_categories.txt");
		// categoryPages = new TxtFileWriter(outputPath + File.separator
		// + "category_pages.txt");
		// categoryInlinks = new TxtFileWriter(outputPath + File.separator
		// + "category_inlinks.txt");
		// categoryOutlinks = new TxtFileWriter(outputPath + File.separator
		// + "category_outlinks.txt");

		pageCategories = new TxtFileWriter(versionFiles
				.getOutputPageCategories());
		categoryPages = new TxtFileWriter(versionFiles.getOutputCategoryPages());
		categoryInlinks = new TxtFileWriter(versionFiles
				.getOutputCategoryInlinks());
		categoryOutlinks = new TxtFileWriter(versionFiles
				.getOutputCategoryOutlinks());

	}

	public void processCategoryLinksRow(CategorylinksParser clParser)
			throws IOException {
		int cl_from;
		String cl_to;

		cl_from = clParser.getClFrom();
		cl_to = clParser.getClTo();
		if (!existsCategory(cl_to)) {// discard links with non registred targets
			return;
		}
		// if the link source is a page then write the link in category_pages
		// and
		// page_categories
		if (existsPage(cl_from)) {

			categoryPages.addRow(getCategoryPageId(cl_to), cl_from);
			pageCategories.addRow(cl_from, getCategoryPageId(cl_to));
			if (cl_to.equals(metaData.getDisambiguationCategory())) {
				disambiguations.add(cl_from);
				metaData.addDisamb();
			}
		} else {
			// if the link source is a category than write the link in
			// category_inlinks and category_outlinks
			if (existsCategoryPageId(cl_from)) {
				categoryOutlinks.addRow(getCategoryPageId(cl_to), cl_from);
				categoryInlinks.addRow(cl_from, getCategoryPageId(cl_to));
			}
		}
	}

	public void exportAfterCategoryLinksParsing() throws IOException {
		// Export the written tables
		pageCategories.export();
		categoryPages.export();
		categoryInlinks.export();
		categoryOutlinks.export();
	}

	TxtFileWriter pageInlinks = null;
	TxtFileWriter pageOutlinks = null;

	public void initPageLinksParsing() throws IOException {
		// XXX ivan.galkin
		// pageInlinks = new TxtFileWriter(outputPath + File.separator
		// + "page_inlinks.txt");
		// pageOutlinks = new TxtFileWriter(outputPath + File.separator
		// + "page_outlinks.txt");
		pageInlinks = new TxtFileWriter(versionFiles.getOutputPageInlinks());
		pageOutlinks = new TxtFileWriter(versionFiles.getOutputPageOutlinks());
	}

	public void processPageLinksRow(PagelinksParser plParser)
			throws IOException {
		int pl_from;
		String pl_to;
		pl_from = plParser.getPlFrom();
		pl_to = plParser.getPlTo();
		// skip redirects or page with other namespace than 0
		if (skipPage && !existsPagePageId(pl_from) || !existsPageName(pl_to)) {
			return;
		}
		pageOutlinks.addRow(pl_from, getPagePageId(pl_to));
		pageInlinks.addRow(getPagePageId(pl_to), pl_from);
	}

	public void exportAfterPageLinksProcessing() throws IOException {
		// export the written tables
		pageInlinks.export();
		pageOutlinks.export();
	}

	TxtFileWriter page = null;
	TxtFileWriter pageMapLine = null;
	TxtFileWriter pageRedirects = null;

	public void initTextParsing() throws IOException {
		// XXX ivan.galkin
		// page = new TxtFileWriter(outputPath + File.separator + "Page.txt");
		// pageMapLine = new TxtFileWriter(outputPath + File.separator
		// + "PageMapLine.txt");
		// pageRedirects = new TxtFileWriter(outputPath + File.separator
		// + "page_redirects.txt");
		page = new TxtFileWriter(versionFiles.getOutputPage());
		pageMapLine = new TxtFileWriter(versionFiles.getOutputPageMapLine());
		pageRedirects = new TxtFileWriter(versionFiles.getOutputPageRedirects());
	}

	public void processTextRow(TextParser textParser) throws IOException {
		String destination;
		int text_id;
		int page_id;
		text_id = textParser.getOldId();
		if (!textIdPageIdMap.containsKey(text_id))
			return;
		page_id = textIdPageIdMap.get(text_id);
		if (existsPagePageId(page_id)) {// pages
			page.addRow(page_id, page_id, getPageName(page_id), textParser
					.getOldText(), formatBoolean(disambiguations
					.contains(page_id)));
			pageMapLine.addRow(page_id, getPageName(page_id), page_id, "NULL",
					"NULL");
			metaData.addPage();
			return;
		}
		if (existsRedirect(page_id)) {// Redirects
			destination = Redirects.getRedirectDestination(textParser
					.getOldText());
			if (!existsPageName(destination))
				return;
			pageRedirects.addRow(getPagePageId(destination),
					getRedirectName(page_id));
			pageMapLine.addRow(page_id, getRedirectName(page_id),
					getPagePageId(destination), "NULL", "NULL");
			metaData.addRedirect();
		}
	}

	public void exportAfterTextParsing() throws IOException {
		// export the written tables
		page.export();
		pageRedirects.export();
		pageMapLine.export();
	}

	public void writeMetaData() throws IOException {
		// XXX ivan.galkin
		// TxtFileWriter metaData_ = new TxtFileWriter(outputPath +
		// File.separator
		// + "MetaData.txt");
		TxtFileWriter metaData_ = new TxtFileWriter(versionFiles
				.getOutputMetadata());
		// ID,LANGUAGE,DISAMBIGUATION_CATEGORY,MAIN_CATEGORY,nrOfPages,nrOfRedirects,nrOfDisambiguationPages,nrOfCategories,timestamp
		metaData_.addRow(metaData.getId(), metaData.getLanguage(), metaData
				.getDisambiguationCategory(), metaData.getMainCategory(),
				metaData.getNrOfPages(), metaData.getNrOfRedirects(), metaData
						.getNrOfDisambiguations(),
				metaData.getNrOfCategories(), TimestampUtil
						.toMediaWikiString(metaData.getTimestamp()));
		System.out.println("-------------------------------");
		System.out.println("Timestamp          : " + timestamp.toString());
		System.out.println("nrOfCategories     : "
				+ metaData.getNrOfCategories());
		System.out.println("nrOfPages          : " + metaData.getNrOfPages());
		System.out.println("nrOfRedirects      : "
				+ metaData.getNrOfRedirects());
		System.out.println("nrOfDisambiguations: "
				+ metaData.getNrOfDisambiguations());
		metaData_.export();
	}

	/**
	 * Returns the String value of the bit 1 if the given boolean is true<br>
	 * and an empty String otherwise. This the way bit values are written<br>
	 * in .txt dump files.
	 * 
	 * @param b
	 * @return
	 */
	private String formatBoolean(boolean b) {
		return b ? new String(new byte[] { 1 }) : "";
	}

	public void recordCategory(int page_id, String page_title) {
		cPageIdNameMap.put(page_id, page_title);
		cNamePageIdMap.put(page_title, page_id);
	}

	public void recordPage(int page_id, String page_title) {
		pPageIdNameMap.put(page_id, page_title);
		pNamePageIdMap.put(page_title, page_id);
	}

	public void recordRedirect(int page_id, String page_title) {
		rPageIdNameMap.put(page_id, page_title);
	}

	public boolean existsCategory(String name) {
		return cNamePageIdMap.containsKey(name);
	}

	public boolean existsPageName(String name) {
		return pNamePageIdMap.containsKey(name);
	}

	public boolean existsPage(int page_id) {
		return pPageIdNameMap.containsKey(page_id);
	}

	public boolean existsCategoryPageId(int page_id) {
		return cPageIdNameMap.containsKey(page_id);
	}

	public boolean existsPagePageId(int page_id) {
		return pPageIdNameMap.containsKey(page_id);
	}

	public int getPagePageId(String name) {
		return pNamePageIdMap.get(name);
	}

	public int getCategoryPageId(String name) {
		return cNamePageIdMap.get(name);
	}

	public String getPageName(int page_id) {
		return pPageIdNameMap.get(page_id);
	}

	public boolean existsRedirect(int page_id) {
		return rPageIdNameMap.containsKey(page_id);
	}

	public String getRedirectName(int page_id) {
		return rPageIdNameMap.get(page_id);
	}

	/*
	 * implemented methods from IDumpVersion interface
	 */

	@Override
	public void initialize(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public void setFiles(Files versionFiles) {
		this.versionFiles = versionFiles;
	}

	/*
	 * not implemented methods
	 */

	@Override
	public void exportAfterPageLinksParsing() throws IOException {
	}

	@Override
	public void exportAfterRevisionParsing() throws IOException {
	}

	@Override
	public void flushByTextParsing() throws IOException {
	}

	@Override
	public void freeAfterCategoryLinksParsing() {
	}

	@Override
	public void freeAfterPageLinksParsing() {
	}

	@Override
	public void freeAfterPageParsing() {
	}

	@Override
	public void freeAfterRevisonParsing() {
	}

	@Override
	public void freeAfterTextParsing() {
	}

	@Override
	public void initRevisionParsion() {
	}

	@Override
	public void setLogger(ILogger logger) {
	}

	@Override
	public void setCategoryRedirectsSkip(boolean skipCategory) {
		this.skipCategory = skipCategory;
	}

	@Override
	public void setPageRedirectsSkip(boolean skipPage) {
		this.skipPage = skipPage;
	}
}
