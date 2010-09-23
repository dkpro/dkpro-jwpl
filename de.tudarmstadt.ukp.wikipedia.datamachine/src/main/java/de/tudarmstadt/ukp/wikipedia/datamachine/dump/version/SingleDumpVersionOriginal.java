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
package de.tudarmstadt.ukp.wikipedia.datamachine.dump.version;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.TxtFileWriter;

/**
 * Transforms a database from mediawiki format to JWPL format.<br>
 * The transformation produces .txt files for the different tables<br>
 * in the JWPL database.<br>
 * <br>
 * 
 * Adopted to IDumpVersion by Galkin
 * 
 * @author Anouar
 * 
 * 
 */
public class SingleDumpVersionOriginal implements IDumpVersion {

	// metadata
	private String language;
	private String mainCategory;
	private String disambiguationsCategory;

	// statistics
	private int nrOfDisambiguations = 0;
	private int nrOfPages = 0;
	private int nrOfCategories = 0;
	private int nrOfRedirects = 0;

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
	private Set<Integer> disambiguations; // caches the page id's of
	// disambiguation pages.
	private Map<Integer, Integer> textIdPageIdMap;// maps text id's to the page

	// id's.

	// galkin: moved from local variables to fields
	private TxtFileWriter txtFW;
	private TxtFileWriter pageCategories;
	private TxtFileWriter categoryPages;
	private TxtFileWriter categoryInlinks;
	private TxtFileWriter categoryOutlinks;
	private TxtFileWriter pageInlinks;
	private TxtFileWriter pageOutlinks;
	private TxtFileWriter page;
	private TxtFileWriter pageMapLine;
	private TxtFileWriter pageRedirects;
	private String outputDir;

	// galkin: added

	private ILogger logger;
	private boolean skipPage = true;
	private boolean skipCategory = true;

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

	@Override
	public void exportAfterCategoryLinksParsing() throws IOException {
		pageCategories.export();
		categoryPages.export();
		categoryInlinks.export();
		categoryOutlinks.export();
	}

	@Override
	public void exportAfterPageLinksParsing() throws IOException {
		pageInlinks.export();
		pageOutlinks.export();
	}

	@Override
	public void exportAfterPageParsing() throws IOException {
		txtFW.export();

		nrOfCategories = cPageIdNameMap.keySet().size();
		nrOfPages = pPageIdNameMap.keySet().size()
				+ rPageIdNameMap.keySet().size();
	}

	@Override
	public void exportAfterRevisionParsing() throws IOException {
	}

	@Override
	public void exportAfterTextParsing() throws IOException {
		page.export();
		pageRedirects.export();
		pageMapLine.export();
	}

	@Override
	public void flushByTextParsing() throws IOException {
		page.flush();
		pageRedirects.flush();
		pageMapLine.flush();
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
		page.export();
		pageRedirects.export();
		pageMapLine.export();
	}

	@Override
	public void initCategoryLinksParsing() throws IOException {
		pageCategories = new TxtFileWriter(outputDir + File.separator
				+ "page_categories.txt");
		categoryPages = new TxtFileWriter(outputDir + File.separator
				+ "category_pages.txt");
		categoryInlinks = new TxtFileWriter(outputDir + File.separator
				+ "category_inlinks.txt");
		categoryOutlinks = new TxtFileWriter(outputDir + File.separator
				+ "category_outlinks.txt");

	}

	@Override
	public void initPageLinksParsing() throws IOException {

		pageInlinks = new TxtFileWriter(outputDir + File.separator
				+ "page_inlinks.txt");
		pageOutlinks = new TxtFileWriter(outputDir + File.separator
				+ "page_outlinks.txt");

	}

	@Override
	public void initPageParsing() throws IOException {
		txtFW = new TxtFileWriter(outputDir + File.separator + "Category.txt");

	}

	@Override
	public void initRevisionParsion() {

	}

	@Override
	public void initTextParsing() throws IOException {
		page = new TxtFileWriter(outputDir + File.separator + "Page.txt");
		pageMapLine = new TxtFileWriter(outputDir + File.separator
				+ "PageMapLine.txt");
		pageRedirects = new TxtFileWriter(outputDir + File.separator
				+ "page_redirects.txt");

	}

	@Override
	public void initialize(Timestamp timestamp) {
		this.pPageIdNameMap = new HashMap<Integer, String>();
		this.cPageIdNameMap = new HashMap<Integer, String>();
		this.pNamePageIdMap = new HashMap<String, Integer>();
		this.cNamePageIdMap = new HashMap<String, Integer>();
		this.rPageIdNameMap = new HashMap<Integer, String>();
		this.disambiguations = new HashSet<Integer>();
		this.textIdPageIdMap = new HashMap<Integer, Integer>();

	}

	@Override
	public void processCategoryLinksRow(CategorylinksParser clParser)
			throws IOException {

		int cl_from;
		String cl_to;

		cl_from = clParser.getClFrom();
		cl_to = clParser.getClTo();
		if (!cNamePageIdMap.containsKey(cl_to)) {// discard links with non
			// registred targets
			return;
		}
		// if the link source is a page then write the link in
		// category_pages and
		// page_categories
		if (pPageIdNameMap.containsKey(cl_from)) {
			categoryPages.addRow(cNamePageIdMap.get(cl_to), cl_from);
			pageCategories.addRow(cl_from, cNamePageIdMap.get(cl_to));
			if (cl_to.equals(disambiguationsCategory)) {
				disambiguations.add(cl_from);
				nrOfDisambiguations++;
			}
		} else {
			// if the link source is a category than write the link in
			// category_inlinks and category_outlinks
			if (cPageIdNameMap.containsKey(cl_from)) {
				categoryOutlinks.addRow(cNamePageIdMap.get(cl_to), cl_from);
				categoryInlinks.addRow(cl_from, cNamePageIdMap.get(cl_to));
			}
		}

	}

	@Override
	public void processPageLinksRow(PagelinksParser plParser)
			throws IOException {

		int pl_from;
		String pl_to;

		pl_from = plParser.getPlFrom();
		pl_to = plParser.getPlTo();
		// skip redirects or page with other namespace than 0

		if (skipPage && !pPageIdNameMap.containsKey(pl_from)
				|| !pNamePageIdMap.containsKey(pl_to)) {
			return;
		}

		pageOutlinks.addRow(pl_from, pNamePageIdMap.get(pl_to));
		pageInlinks.addRow(pNamePageIdMap.get(pl_to), pl_from);
	}

	@Override
	public void processPageRow(PageParser pageParser) throws IOException {

		int page_id;
		int page_namespace;
		String page_title;

		page_namespace = pageParser.getPageNamespace();
		// handle categories
		if (page_namespace == 14) {
			if (skipCategory) {
				if (pageParser.getPageIsRedirect())
					// skip categories that are redirects
					return;
			}
			// retrieve page id and page title
			page_id = pageParser.getPageId();
			page_title = pageParser.getPageTitle();
			if (page_title.equals(disambiguationsCategory)) {
				logger.log("Disambiguations Category found: " + page_title);
			}
			if (page_title.equals(mainCategory)) {
				logger.log("Main Category found: " + page_title);
			}
			// cache the retrieved values
			cPageIdNameMap.put(page_id, page_title);
			cNamePageIdMap.put(page_title, page_id);
			// write a new row in the table Category.
			// Note that we also consider the page_id as id
			txtFW.addRow(page_id, page_id, page_title);
			return;
		}
		// handle pages
		if (page_namespace == 0) {
			// retrieve page id and title
			page_id = pageParser.getPageId();
			page_title = pageParser.getPageTitle();
			// distinguish redirects
			if (pageParser.getPageIsRedirect()) {
				rPageIdNameMap.put(page_id, page_title);
			} else {
				pPageIdNameMap.put(page_id, page_title);
				pNamePageIdMap.put(page_title, page_id);
			}
		}

	}

	@Override
	public void processRevisionRow(RevisionParser revisionParser) {
		textIdPageIdMap.put(revisionParser.getRevTextId(), revisionParser
				.getRevPage());
	}

	@Override
	public void processTextRow(TextParser textParser) throws IOException {

		String destination;
		int text_id;
		int page_id;

		text_id = textParser.getOldId();
		if (!textIdPageIdMap.containsKey(text_id))
			return;
		page_id = textIdPageIdMap.get(text_id);
		if (pPageIdNameMap.containsKey(page_id)) {// pages
			page.addRow(page_id, page_id, pPageIdNameMap.get(page_id),
					textParser.getOldText(), formatBoolean(disambiguations
							.contains(page_id)));
			pageMapLine.addRow(page_id, pPageIdNameMap.get(page_id), page_id,
					"NULL", "NULL");
			return;
		}
		if (rPageIdNameMap.containsKey(page_id)) {// Redirects
			destination = Redirects.getRedirectDestination(textParser
					.getOldText());
			if (!pNamePageIdMap.containsKey(destination))
				return;
			pageRedirects.addRow(pNamePageIdMap.get(destination),
					rPageIdNameMap.get(page_id));
			pageMapLine.addRow(page_id, rPageIdNameMap.get(page_id),
					pNamePageIdMap.get(destination), "NULL", "NULL");
			nrOfRedirects++;
		}

	}

	@Override
	public void setFiles(Files versionFiles) {
		// galkin: only output directory will be used, other file names will be
		// taken from original source code
		outputDir = versionFiles.getOutputDirectory().getAbsolutePath();
	}

	@Override
	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	@Override
	public void setMetaData(MetaData commonMetaData) {
		this.language = commonMetaData.getLanguage();
		this.mainCategory = commonMetaData.getMainCategory();
		this.disambiguationsCategory = commonMetaData
				.getDisambiguationCategory();
	}

	@Override
	public void writeMetaData() throws IOException {
		TxtFileWriter metaData = new TxtFileWriter(outputDir + File.separator
				+ "MetaData.txt");
		// ID,LANGUAGE,DISAMBIGUATION_CATEGORY,MAIN_CATEGORY,nrOfPages,nrOfRedirects,nrOfDisambiguationPages,nrOfCategories
		metaData.addRow("null", language, disambiguationsCategory,
				mainCategory, nrOfPages, nrOfRedirects, nrOfDisambiguations,
				nrOfCategories);
		metaData.export();

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
