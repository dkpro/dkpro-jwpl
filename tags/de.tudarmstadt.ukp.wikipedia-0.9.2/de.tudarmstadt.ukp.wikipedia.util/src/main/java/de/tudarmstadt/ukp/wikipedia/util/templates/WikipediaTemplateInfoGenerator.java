/*******************************************************************************
 * Copyright (c) 2011 Ubiquitous Knowledge Processing (UKP) Lab
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Oliver Ferschke
 *     Artem Vovk
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.util.templates;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.PageIterator;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Template;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.ShowTemplateNamesAndParameters;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionIterator;
import de.tudarmstadt.ukp.wikipedia.util.StringUtils;

/**
 * This class determines which page in a JWPL database contains which templates.
 * It produces an SQL file that will add this data to the extisting database. It
 * can then be accessed by the WikipediaTemplateInfo class.
 *
 * @author Oliver Ferschke
 *
 */
public class WikipediaTemplateInfoGenerator
{
	private MediaWikiParser parser = null;
	private final Log logger = LogFactory.getLog(getClass());
	private Wikipedia wiki;
	private final DatabaseConfiguration dbConf;

	private final int pageBuffer;

	private final Map<String, Set<Integer>> TPLNAME_TO_REVISIONIDS = new HashMap<String, Set<Integer>>();
	private final Map<String, Set<Integer>> TPLNAME_TO_PAGEIDS = new HashMap<String, Set<Integer>>();
	private final Map<String, Integer> tplNameToTplId = new HashMap<String, Integer>();

	private final String charset;
	// private final long maxAllowedPacket;
	private final String outputPath;

	protected final static String TABLE_TPLID_REVISIONID = "templateId_revisionId";
	protected final static String TABLE_TPLID_PAGEID = "templateId_pageId";
	protected final static String TABLE_TPLID_TPLNAME = "templates";

	private final int VERBOSITY = 500;

	private TemplateFilter pageFilter;

	private TemplateFilter revisionFilter;

	private boolean revisionTableExists;

	private boolean pageTableExists;

	private GeneratorMode mode;

	public WikipediaTemplateInfoGenerator(DatabaseConfiguration dbc, int pageBuffer,
			String charset, String outputPath, long maxAllowedPacket,
			TemplateFilter pageFilter, TemplateFilter revisionFilter,
			GeneratorMode mode)
		throws WikiApiException
	{
		this.dbConf = dbc;

		this.pageBuffer=pageBuffer;

		MediaWikiParserFactory pf = new MediaWikiParserFactory(
				dbc.getLanguage());
		pf.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
		parser = pf.createParser();

		// this.maxAllowedPacket = maxAllowedPacket;
		this.charset = charset;
		this.outputPath = outputPath;

		// Filters
		this.pageFilter = pageFilter;
		this.revisionFilter = revisionFilter;
		//

		this.mode = mode;

	}

	/**
	 * Fill map(mapToFill) with template data
	 *
	 * @param textForTemplateExtraction
	 *            text for template extraction
	 * @param filterToApply
	 *            filter to apply for templates
	 * @param id
	 *            id of a page/revision
	 * @param mapToFill
	 *            map to fill with data
	 */
	private void fillMapWithTemplateData(String textForTemplateExtraction,
			TemplateFilter filterToApply, int id,
			Map<String, Set<Integer>> mapToFill)
	{
		Set<String> names = getTemplateNames(textForTemplateExtraction);
		// Update the map with template values for current page
		for (String name : names) {

			// filter templates - only use templates from a provided
			// whitelist
			if (filterToApply.acceptTemplate(name)) {
				// Create records for TEMPLATE->PAGES/REVISION map
				if (mapToFill.containsKey(name)) {
					// add the page id to the set for the current template
					Set<Integer> pIdList = mapToFill.remove(name);
					pIdList.add(id);
					mapToFill.put(name, pIdList);
				}
				else {
					// add new list with page id of current page
					Set<Integer> newIdList = new HashSet<Integer>();
					newIdList.add(id);
					mapToFill.put(name, newIdList);
				}
			}
		}
	}

	/**
	 * Extracts templates from pages and revisions
	 */
	private void extractTemplates() throws WikiApiException
	{
		PageIterator pageIter = new PageIterator(getWiki(), true, pageBuffer);
		RevisionApi revApi = new RevisionApi(dbConf);

		int pageCounter = 0;
		long revisionCounter =0L;

		while (pageIter.hasNext()) {
			pageCounter++;

			if (pageCounter % VERBOSITY == 0) {
				logger.info(pageCounter + " pages processed ...");
			}

			Page curPage = pageIter.next();
			int curPageId = curPage.getPageId();


			//PROCESS PAGES
			if (mode.active_for_pages) {
				fillMapWithTemplateData(curPage.getText(), pageFilter, curPageId,
						TPLNAME_TO_PAGEIDS);
			}

			//PROCESS REVISIONS
			if (mode.active_for_revisions) {
				List<Timestamp> tsList = revApi.getRevisionTimestamps(curPageId);
				for(Timestamp ts:tsList){

					revisionCounter++;
					if (revisionCounter % (VERBOSITY*10) == 0) {
						logger.info(revisionCounter + " revisions processed ...");
					}

					Revision curRevision = revApi.getRevision(curPageId, ts);
					int curRevisionId = curRevision.getRevisionID();

					fillMapWithTemplateData(curRevision.getRevisionText(),
							revisionFilter, curRevisionId, TPLNAME_TO_REVISIONIDS);
				}
			}
		}
	}


	/**
	 * Extracts templates from pages only
	 */
	private void processPages()
	{
		PageIterator pageIter = new PageIterator(getWiki(), true, pageBuffer);

		int pageCounter = 0;

		while (pageIter.hasNext()) {
			pageCounter++;

			if (pageCounter % VERBOSITY == 0) {
				logger.info(pageCounter + " pages processed ...");
			}

			Page curPage = pageIter.next();
			int curPageId = curPage.getPageId();

			fillMapWithTemplateData(curPage.getText(), pageFilter, curPageId,
					TPLNAME_TO_PAGEIDS);
		}
	}


	/**
	 * Processes only revision templates using the Revision Iterator
	 */
	private void processRevisions()
	{
		logger.info("Processing revisions, extracting template information ...");
		RevisionIterator revisionIter=null;
		try{
			revisionIter = new RevisionIterator(dbConf);

			int revCounter = 0;
			while (revisionIter.hasNext()) {
				revCounter++;

				if (revCounter % VERBOSITY == 0) {
					logger.info(revCounter + " revisions processed ...");
				}

				Revision curRevision = revisionIter.next();
				int curRevisionId = curRevision.getRevisionID();

				fillMapWithTemplateData(curRevision.getRevisionText(),
						revisionFilter, curRevisionId, TPLNAME_TO_REVISIONIDS);
			}
		}catch(WikiApiException e){
			System.err.println("Error initializing Revision Iterator");
			e.printStackTrace();
		}finally{
			if(revisionIter!=null){
				try{
					revisionIter.close();
				}catch(SQLException e){
					System.err.println("Error closing RevisionIterator");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Start generator
	 */
	public void process() throws Exception
	{
		WikipediaTemplateInfo info = new WikipediaTemplateInfo(getWiki());
		pageTableExists = info.tableExists(TABLE_TPLID_PAGEID);
		revisionTableExists = info.tableExists(TABLE_TPLID_REVISIONID);
		
		if(!pageTableExists&&!revisionTableExists&&mode.active_for_pages&&mode.active_for_revisions){
			//TODO see fix-me comment in WikipediaTemplateInfoDumpWriter
			throw new IllegalStateException("Currently, you cannot create revision-tpl index and page-tpl index at the same time. The code is there, but it currently assigns separate tpl-name-ids for page-tpls and revisions-tpls. Please create a revision-tpl index, import the data into the db, create the page-tpl index and import this data."); 
		}

		if(mode.useRevisionIterator){
			if (mode.active_for_revisions) {
				processRevisions();
			}
			if (mode.active_for_pages) {
				processPages();
			}
		}else{
			try{
				extractTemplates();
			}catch(WikiApiException e){
				System.err.println("Error extracting templates.");
				e.printStackTrace();
			}
		}

		////////////////////
		
		logger.info("Generating template indices ...");
		boolean tableWithTemplatesExists = false;

		tableWithTemplatesExists = true;
	
		if (mode.active_for_pages && pageTableExists) {
			generateTemplateIndices(info, TPLNAME_TO_PAGEIDS.keySet());
		}

		if (mode.active_for_revisions && revisionTableExists) {
			generateTemplateIndices(info, TPLNAME_TO_REVISIONIDS.keySet());
		}

		////////////////////

		
		logger.info("Writing SQL dump ...");

		WikipediaTemplateInfoDumpWriter writer = new WikipediaTemplateInfoDumpWriter(
				this.outputPath, this.charset, this.tplNameToTplId,
				tableWithTemplatesExists);
		mode.templateNameToPageId = TPLNAME_TO_PAGEIDS;
		mode.templateNameToRevId = TPLNAME_TO_REVISIONIDS;
		writer.writeSQL(revisionTableExists, pageTableExists, mode);

		////////////////////
	}

	/**
	 * Loads existing ids into the map. If no id exists, a template will
	 * get a new one in the dump writer
	 *
	 * @param info
	 * @param templateNames
	 *            template names to use
	 */
	private void generateTemplateIndices(WikipediaTemplateInfo info,
			Set<String> templateNames)
	{
		try {
			for (String name : templateNames) {
				int id = info.checkTemplateId(name);
				if (id != -1) {
					tplNameToTplId.put(name, id);
				}
			}
		}
		catch (WikiApiException e) {

		}
	}

	/**
	 *
	 * Returns the set of names of all templates that are contained in the given
	 * article (without duplicates).<br/>
	 * The names are SQL escaped using StringUtils.sqlEscape
	 *
	 * @param pageText
	 *            the page to get the templates from
	 * @return a set of template names (without duplicates)
	 */
	private Set<String> getTemplateNames(String pageText)
	{
		Set<String> names = new HashSet<String>();
		if (!pageText.isEmpty()) {
			try {
				ParsedPage pp = parser.parse(pageText);
				List<Template> templates = pp.getTemplates();
				for (Template t : templates) {
					names.add(StringUtils.sqlEscape(t.getName().toLowerCase()));
				}
			}
			catch (Exception e) {
				// Most likely parsing problems
				e.printStackTrace();
				logger.warn("Problems parsing page");
			}
		}
		return names;
	}

	private Wikipedia getWiki()
	{
		if (this.wiki == null) {
			Wikipedia nWiki = null;
			try {
				nWiki = new Wikipedia(dbConf);
			}
			catch (WikiInitializationException e) {
				System.err.println("Error initializing Wiki connection");
			}
			return nWiki;
		}
		else {
			return this.wiki;
		}
	}

}
