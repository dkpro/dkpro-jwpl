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

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql.CategorylinksParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql.PagelinksParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.version.AbstractDumpVersion;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.PageParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.RevisionParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.TextParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.hashing.IStringHashCode;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.Redirects;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.TxtFileWriter;

public class SingleDumpVersionJDKGeneric<KeyType, HashAlgorithm extends IStringHashCode>
		extends AbstractDumpVersion {

	private static final String SQL_NULL = "NULL";
	//TODO 	This constant is used to flag page titles of discussion pages.
	//		Is also defined in wikipedia.api:WikiConstants.DISCUSSION_PREFIX
	//		It just doesn't make sense to add a dependency just for the constant
	private static final String DISCUSSION_PREFIX = "Discussion:";

	private Map<Integer, String> pPageIdNameMap;
	private TIntHashSet cPageIdNameMap;
	private Map<KeyType, Integer> pNamePageIdMap;
	private Map<KeyType, Integer> cNamePageIdMap;
	private Map<Integer, String> rPageIdNameMap;
	private TIntHashSet disambiguations;
	private TIntIntHashMap textIdPageIdMap;

	IStringHashCode hashAlgorithm;

	@SuppressWarnings("unchecked")
	public SingleDumpVersionJDKGeneric(Class<HashAlgorithm> hashAlgorithmClass)
			throws InstantiationException, IllegalAccessException {

		hashAlgorithm = hashAlgorithmClass.newInstance();
		@SuppressWarnings("unused")
		KeyType hashAlgorithmResult = (KeyType) hashAlgorithm.hashCode("test");
	}

	@Override
	public void freeAfterCategoryLinksParsing() {
		cPageIdNameMap.clear();
		cNamePageIdMap.clear();
		System.gc();
	}

	@Override
	public void freeAfterPageLinksParsing() {
		// nothing to free

	}

	@Override
	public void freeAfterPageParsing() {
		metaData.setNrOfCategories(cPageIdNameMap.size());
		metaData.setNrOfPages(pPageIdNameMap.keySet().size()
				+ rPageIdNameMap.keySet().size());
		System.out.println("nrOfCategories: " + metaData.getNrOfCategories());
		System.out.println("nrOfPage: " + metaData.getNrOfPages());
		System.out
				.println("nrOfRedirects before testing the validity of the destination:"
						+ rPageIdNameMap.size());

	}

	@Override
	public void freeAfterRevisonParsing() {
		// nothing to free

	}

	@Override
	public void freeAfterTextParsing() {
		pPageIdNameMap.clear();
		cPageIdNameMap.clear();
		pNamePageIdMap.clear();
		cNamePageIdMap.clear();
		rPageIdNameMap.clear();
		disambiguations.clear();
		textIdPageIdMap.clear();
	}

	@Override
	public void initialize(Timestamp timestamp) {
		pPageIdNameMap = new HashMap<Integer, String>();
		cPageIdNameMap = new TIntHashSet();
		pNamePageIdMap = new HashMap<KeyType, Integer>();
		cNamePageIdMap = new HashMap<KeyType, Integer>();
		rPageIdNameMap = new HashMap<Integer, String>();
		disambiguations = new TIntHashSet();
		textIdPageIdMap = new TIntIntHashMap();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processCategoryLinksRow(CategorylinksParser clParser)
			throws IOException {
		String cl_to = clParser.getClTo();

		if (cl_to != null) {
			KeyType clToHash = (KeyType) hashAlgorithm.hashCode(cl_to);

			Integer cl_toValue = cNamePageIdMap.get(clToHash);

			if (cl_toValue != null) {
				int cl_from = clParser.getClFrom();

				if (pPageIdNameMap.containsKey(cl_from)) {
					categoryPages.addRow(cl_toValue, cl_from);
					pageCategories.addRow(cl_from, cl_toValue);

					if (cl_to.equals(metaData.getDisambiguationCategory())) {
						disambiguations.add(cl_from);
						metaData.addDisamb();
					}
				} else if (cPageIdNameMap.contains(cl_from)) {
					categoryOutlinks.addRow(cl_toValue, cl_from);
					categoryInlinks.addRow(cl_from, cl_toValue);
				}

			}
		}
		else {
		    throw new IOException("Parsin error." + CategorylinksParser.class.getName() +
		                          " returned null value in " + this.getClass().getName());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processPageLinksRow(PagelinksParser plParser)
			throws IOException {
		int pl_from = plParser.getPlFrom();
		String pl_to = plParser.getPlTo();
		if (pl_to != null) {
			KeyType plToHash = (KeyType) hashAlgorithm.hashCode(pl_to);
			Integer pl_toValue = pNamePageIdMap.get(plToHash);
			// skip redirects if skipPage is enabled
			if ((!skipPage || pPageIdNameMap.containsKey(pl_from))
					&& pl_toValue != null) {
				pageOutlinks.addRow(pl_from, pl_toValue);
				pageInlinks.addRow(pl_toValue, pl_from);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processPageRow(PageParser pageParser) throws IOException {
		int page_namespace = pageParser.getPageNamespace();
		int page_id = pageParser.getPageId();
		String page_title = pageParser.getPageTitle();
		if (page_title != null) {
			switch (page_namespace) {
				case NS_CATEGORY: {
					// skip redirect categories if skipCategory is enabled
					if (!(skipCategory && pageParser.getPageIsRedirect())) {
						cPageIdNameMap.add(page_id);
						cNamePageIdMap.put(
								(KeyType) hashAlgorithm.hashCode(page_title),
								page_id);
						txtFW.addRow(page_id, page_id, page_title);
					}
					break;
				}

				case NS_TALK: {
					page_title = DISCUSSION_PREFIX + page_title;
					//the NS_MAIN block will also be executed
					//for NS_TALK pages ...
				}

				case NS_MAIN: {
					if (pageParser.getPageIsRedirect()) {
						rPageIdNameMap.put(page_id, page_title);
					}
					else {
						pPageIdNameMap.put(page_id, page_title);
						pNamePageIdMap.put(
								(KeyType) hashAlgorithm.hashCode(page_title),
								page_id);
					}
					break;
				}
			}
		}

	}

	@Override
	public void processRevisionRow(RevisionParser revisionParser) {
		textIdPageIdMap.put(revisionParser.getRevTextId(), revisionParser
				.getRevPage());

	}

	@SuppressWarnings("unchecked")
	@Override
	public void processTextRow(TextParser textParser) throws IOException {
		int text_id = textParser.getOldId();
		if (textIdPageIdMap.containsKey(text_id)) {

			int page_id = textIdPageIdMap.get(text_id);
			String page_idValueP = pPageIdNameMap.get(page_id);
			if (page_idValueP != null) {// pages
				page.addRow(page_id, page_id, page_idValueP, textParser
						.getOldText(), formatBoolean(disambiguations
						.contains(page_id)));
				pageMapLine.addRow(page_id, page_idValueP, page_id, SQL_NULL,
						SQL_NULL);

			} else {
				String page_idValueR = rPageIdNameMap.get(page_id);
				if (page_idValueR != null) {// Redirects
					String destination = Redirects
							.getRedirectDestination(textParser.getOldText());
					if (destination != null) {
						KeyType destinationHash = (KeyType) hashAlgorithm
								.hashCode(destination);
						Integer destinationValue = pNamePageIdMap
								.get(destinationHash);
						if (destinationValue != null) {

							pageRedirects.addRow(destinationValue,
									page_idValueR);
							pageMapLine.addRow(page_id, page_idValueR,
									destinationValue, SQL_NULL, SQL_NULL);
							metaData.addRedirect();
						}
					}
				}
			}
		}

	}

	@Override
	public void writeMetaData() throws IOException {
		TxtFileWriter outputFile = new TxtFileWriter(versionFiles
				.getOutputMetadata());
		// ID,LANGUAGE,DISAMBIGUATION_CATEGORY,MAIN_CATEGORY,nrOfPages,nrOfRedirects,nrOfDisambiguationPages,nrOfCategories
		outputFile
				.addRow(metaData.getId(), metaData.getLanguage(), metaData
						.getDisambiguationCategory(), metaData
						.getMainCategory(), metaData.getNrOfPages(), metaData
						.getNrOfRedirects(), metaData.getNrOfDisambiguations(),
						metaData.getNrOfCategories());
		outputFile.flush();
		outputFile.close();
	}

}
