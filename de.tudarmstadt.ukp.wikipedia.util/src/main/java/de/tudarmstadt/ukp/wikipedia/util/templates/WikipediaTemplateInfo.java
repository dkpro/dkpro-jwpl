/*******************************************************************************
 * Copyright (c) 2011 Ubiquitous Knowledge Processing (UKP) Lab
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Oliver Ferschke
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.util.templates;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Template;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.ShowTemplateNamesAndParameters;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import de.tudarmstadt.ukp.wikipedia.util.templates.RevisionPair.RevisionPairType;

/**
 * This class gives access to the additional information created by
 * the TemplateInfoGenerator.
 *
 * @author Oliver Ferschke
 */
public class WikipediaTemplateInfo {

    private final Wikipedia wiki;
    private RevisionApi revApi=null;
    private MediaWikiParser parser=null;

    private Connection connection=null;

    /**
     */
    public WikipediaTemplateInfo(Wikipedia pWiki) throws SQLException, WikiApiException{

    	this.wiki = pWiki;
        this.connection=getConnection(wiki);

    	if (!tableExists(WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME)) {
    		System.err.println("No Template Database could be found. You can only use methods that work without a template index");
    	}
    }

    /**
     * Returns the number of all pages that contain a template the name
     * of which starts with any of the the given Strings.
     *
     * @param templateFragments a list Strings containing the beginnings of the desired templates
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
     * @return the number of pages that contain any template starting with templateFragment
     * @throws WikiApiException If there was any error retrieving the page object (most likely if the template templates are corrupted)
     */
    private Integer countFragmentFilteredPages(List<String> templateFragments, boolean whitelist)
		throws WikiApiException
	{
		try {
			int count = 0;
			PreparedStatement statement = null;
			ResultSet result = null;

			try {
				StringBuffer sqlString = new StringBuffer();
				StringBuffer subconditions = new StringBuffer();
				sqlString.append("SELECT distinct(count(*)) FROM "+ WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+ " as tpl, "
								+ WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID+ " AS p WHERE tpl.templateId = p.templateId "+(whitelist?"AND":"AND NOT")+" (");
				for(@SuppressWarnings("unused") String fragment:templateFragments){
					if(subconditions.length()!=0){
						subconditions.append("OR ");
					}
					subconditions.append("tpl.templateName LIKE ?");
				}
				sqlString.append(subconditions);
				sqlString.append(")");

				statement = connection.prepareStatement(sqlString.toString());

				int curIdx=1;
				for(String fragment:templateFragments){
					fragment=fragment.toLowerCase();
					fragment=fragment.trim();
					fragment=fragment.replaceAll(" ", "_");
					statement.setString(curIdx++, fragment + "%");
				}

				result = execute(statement);

				if (result == null) {
					return 0;
				}

				if (result.next()) {
					count = result.getInt(1);
				}
			}
			finally {
				if (statement != null) {
					statement.close();
				}
				if (result != null) {
					result.close();
				}
			}

			return count;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

    /**
     * Returns the number of all pages that contain a template the name
     * of which starts with any of the the given Strings.
     *
     * @param templateFragments a list Strings containing the beginnings of the desired templates
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
     * @return the number of pages that contain any template starting with templateFragment
     * @throws WikiApiException If there was any error retrieving the page object (most likely if the template templates are corrupted)
     */
    public Integer countPagesContainingTemplateFragments(List<String> templateFragments) throws WikiApiException{
		return countFragmentFilteredPages(templateFragments, true);
	}

    /**
     * Returns the number of all pages that contain a template the name
     * of which starts with any of the the given Strings.
     *
     * @param templateFragments a list Strings containing the beginnings of the desired templates
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
     * @return the number of pages that contain any template starting with templateFragment
     * @throws WikiApiException If there was any error retrieving the page object (most likely if the template templates are corrupted)
     */
    public Integer countPagesNotContainingTemplateFragments(List<String> templateFragments) throws WikiApiException{
		return countFragmentFilteredPages(templateFragments, false);
	}


	/**
	 * Returns the number of all pages that contain a template the name of which
	 * equals the given String.
	 *
	 * @param templateNames
	 *            a list of String containing the beginnings of the templates that have to be matched
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
	 * @return the number of pages that contain a template starting with
	 *         any templateFragment
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
	private Integer countFilteredPages(List<String> templateNames, boolean whitelist)
		throws WikiApiException{

	try {
		int count = 0;
		PreparedStatement statement = null;
		ResultSet result = null;

		try {
			StringBuffer sqlString = new StringBuffer();
			StringBuffer subconditions = new StringBuffer();
			sqlString
					.append("SELECT distinct(count(*)) FROM "+ WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+ " as tpl, "
							+ WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID+ " AS p WHERE tpl.templateId = p.templateId "+(whitelist?"AND":"AND NOT")+" (");

			for(@SuppressWarnings("unused") String name:templateNames){
				if(subconditions.length()!=0){
					subconditions.append("OR ");
				}
				subconditions.append("tpl.templateName = ?");
			}
			sqlString.append(subconditions);
			sqlString.append(")");

			statement = connection.prepareStatement(sqlString.toString());

			int curIdx=1;
			for(String name:templateNames){
				name=name.toLowerCase().trim();
				name=name.replaceAll(" ", "_");
				statement.setString(curIdx++, name);
			}

			result = execute(statement);

			if (result == null) {
				return 0;
			}

			if (result.next()) {
				count = result.getInt(1);
			}
		}
		finally {
			if (statement != null) {
				statement.close();
			}
			if (result != null) {
				result.close();
			}
		}

		return count;
	}
	catch (Exception e) {
		throw new WikiApiException(e);
	}
}

	/**
	 * Returns the number of all pages that contain a template the name of which
	 * equals the given String.
	 *
	 * @param templateNames
	 *            a list of String containing the beginnings of the templates that have to be matched
	 * @return the number of pages that contain a template starting with
	 *         any templateFragment
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
	public Integer countPagesContainingTemplateNames(List<String> templateNames) throws WikiApiException{
		return countFilteredPages(templateNames, true);
	}

	/**
	 * Returns the number of all pages that do not contain a template the name of which
	 * equals the given String.
	 *
	 * @param templateNames
	 *            a list of String containing the beginnings of the templates that have to be matched
	 * @return the number of pages that do not contain a template starting with
	 *         any templateFragment
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
	public Integer countPagesNotContainingTemplateNames(List<String> templateNames) throws WikiApiException{
		return countFilteredPages(templateNames, false);
	}

	/**
	 * Return an iterable containing all pages that contain a template the name
	 * of which starts with any of the given Strings.
	 *
	 * @param templateFragments
	 *            the beginning of the templates that have to be matched
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
	 * @return An iterable with the page objects that contain templates
	 *         beginning with any String in templateFragments
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    private Iterable<Page> getFragmentFilteredPages(List<String> templateFragments, boolean whitelist) throws WikiApiException{

		try {
	    	PreparedStatement statement = null;
			ResultSet result = null;
	        List<Page> matchedPages = new LinkedList<Page>();

			try {
				StringBuffer sqlString = new StringBuffer();
				StringBuffer subconditions = new StringBuffer();
				sqlString.append("SELECT p.pageId FROM "+
						WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+ " AS tpl, "
						+ WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID
						+ " AS p WHERE tpl.templateId = p.templateId "+(whitelist?"AND":"AND NOT")+" (");

				for(@SuppressWarnings("unused") String fragment:templateFragments){
					if(subconditions.length()!=0){
						subconditions.append("OR ");
					}
					subconditions.append("tpl.templateName LIKE ?");
				}
				sqlString.append(subconditions);
				sqlString.append(")");

				statement = connection.prepareStatement(sqlString.toString());

				int curIdx=1;
				for(String fragment:templateFragments){
					fragment=fragment.toLowerCase().trim();
					fragment=fragment.replaceAll(" ", "_");
					statement.setString(curIdx++, fragment + "%");
				}

				result = execute(statement);

				if (result == null) {
					throw new WikiPageNotFoundException("Nothing was found");
				}

				while (result.next()) {
					int pageID = result.getInt(1);
		            matchedPages.add(wiki.getPage(pageID));
				}
			}
			finally {
				if (statement != null) {
					statement.close();
				}
				if (result != null) {
					result.close();
				}
			}

			return matchedPages;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}


    public int checkTemplateId(String templateName) throws WikiApiException{
    	try {
	    	PreparedStatement statement = null;
			ResultSet result = null;

			try {
				StringBuffer sqlString = new StringBuffer();


				sqlString.append("SELECT tpl.templateId FROM "+WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+" AS tpl WHERE tpl.templateName='"+templateName.trim().replaceAll(" ","_")+"'");

				statement = connection.prepareStatement(sqlString.toString());

				result = execute(statement);

				if (result == null) {
					return -1;
				}

				while (result.next()) {
					int templateID = result.getInt(1);
		            return templateID;
				}
			}
			finally {
				if (statement != null) {
					statement.close();
				}
				if (result != null) {
					result.close();
				}

			}

			return -1;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
    }


	/**
	 * Return an iterable containing all pages that contain a template the name
	 * of which starts with any of the given Strings.
	 *
	 * @param templateFragments
	 *            the beginning of the templates that have to be matched
	 * @return An iterable with the page objects that contain templates
	 *         beginning with any String in templateFragments
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public Iterable<Page> getPagesContainingTemplateFragments(List<String> templateFragments) throws WikiApiException{
    	return getFragmentFilteredPages(templateFragments, true);
    }

	/**
	 * Return an iterable containing all pages that contain a template the name
	 * of which starts with any of the given Strings.
	 *
	 * @param templateFragments
	 *            the beginning of the templates that have to be matched
	 * @return An iterable with the page objects that contain templates
	 *         beginning with any String in templateFragments
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public Iterable<Page> getPagesNotContainingTemplateFragments(List<String> templateFragments) throws WikiApiException{
    	return getFragmentFilteredPages(templateFragments, false);
    }


	/**
	 * Return an iterable containing all pages that contain a template the name
	 * of which starts with any of the given Strings.
	 *
	 * @param templateNames
	 *            the names of the template that we want to match
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
	 * @return An iterable with the page objects that contain any of the the
	 *         specified templates
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    private Iterable<Page> getFilteredPages(List<String> templateNames, boolean whitelist) throws WikiApiException{
		try {
	    	PreparedStatement statement = null;
			ResultSet result = null;
	        List<Page> matchedPages = new LinkedList<Page>();

			try {
				StringBuffer sqlString = new StringBuffer();
				StringBuffer subconditions = new StringBuffer();
				sqlString.append("SELECT p.pageId FROM "+ WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+ " AS tpl, "
								+ WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID+ " AS p WHERE tpl.templateId = p.templateId "+(whitelist?"AND":"AND NOT")+" (");

				for(@SuppressWarnings("unused") String name:templateNames){
					if(subconditions.length()!=0){
						subconditions.append("OR ");
					}
					subconditions.append("tpl.templateName = ?");
				}
				sqlString.append(subconditions);
				sqlString.append(")");

				statement = connection.prepareStatement(sqlString.toString());

				int curIdx=1;
				for(String name:templateNames){
					name=name.toLowerCase().trim();
					name=name.replaceAll(" ","_");
					statement.setString(curIdx++, name);
				}

				result = execute(statement);

				if (result == null) {
					throw new WikiPageNotFoundException("Nothing was found");
				}

				while (result.next()) {
					int pageID = result.getInt(1);
		            matchedPages.add(wiki.getPage(pageID));
				}
			}
			finally {
				if (statement != null) {
					statement.close();
				}
				if (result != null) {
					result.close();
				}
			}

			return matchedPages;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

	/**
	 * Return an iterable containing all pages that contain a template the name
	 * of which equals any of the given Strings.
	 *
	 * @param templateNames
	 *            the names of the template that we want to match
	 * @return An iterable with the page objects that contain any of the the
	 *         specified templates
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public Iterable<Page> getPagesContainingTemplateNames(List<String> templateNames) throws WikiApiException{
    	return getFilteredPages(templateNames, true);
    }

	/**
	 * Return an iterable containing all pages that do NOT contain a template
	 * the name of which equals of the given Strings.
	 *
	 * @param templateNames
	 *            the names of the template that we want to match
	 * @return An iterable with the page objects that do NOT contain any of the
	 * 			the specified templates
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public Iterable<Page> getPagesNotContainingTemplateNames(List<String> templateNames) throws WikiApiException{
    	return getFilteredPages(templateNames, false);
    }

    
    /**
	 * This method first creates a list of pages containing templates that equal
	 * any of the provided Strings {@see getFilteredPageIds()}.
	 * It then returns a list of revision ids of the revisions in which the
	 * respective templates first appeared.
	 *
	 * @param templateNames
	 *            the template names that have to be matched
	 * @return An list with the revision ids of the first appearance of the template
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public List<Integer> getRevisionsWithFirstTemplateAppearance(String templateName) throws WikiApiException{
    	/*
    	 * Note: This method does not use any revision-template-index. Each revision has to be parsed until the first revision is found that does not contain a certain template.
    	 * TODO also create version using revision-template index 
    	 */
    	System.err.println("Note: This function call demands parsing several revision for each page. A method using the revision-template index is currently under construction.");

    	templateName=templateName.trim().replaceAll(" ", "_");

    	List<Integer> revisionIds = new LinkedList<Integer>();
    	List<Integer> pageIds = getPageIdsContainingTemplateNames(Arrays.asList(new String[]{templateName}));
    	if(pageIds.size()==0){
    		return revisionIds;
    	}
    	if(revApi==null){
    		revApi = new RevisionApi(wiki.getDatabaseConfiguration());
    	}
    	if(parser==null){
    		//TODO switch to SWEBLE
    		MediaWikiParserFactory pf = new MediaWikiParserFactory(
    				wiki.getDatabaseConfiguration().getLanguage());
    		pf.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
    		parser = pf.createParser();
    	}

    	for(int id:pageIds){
    		//get timestamps of all revisions
    		List<Timestamp> tsList = revApi.getRevisionTimestamps(id);

			// sort in reverse order - newest first
			Collections.sort(tsList, new Comparator<Timestamp>()
			{
				public int compare(Timestamp ts1, Timestamp ts2)
				{
					return ts2.compareTo(ts1);
				}
			});

			Revision prevRev=null;
			tsloop:for(Timestamp ts:tsList){

				Revision rev = revApi.getRevision(id, ts);

				//initialize previous revision
				if(prevRev==null){
					prevRev=rev;
				}

				//Parse templates and check if the revision contains the template
				ParsedPage pp = parser.parse(rev.getRevisionText());
				boolean containsTpl = false;
				tplLoop:for(Template tpl:pp.getTemplates()){
					if(tpl.getName().equalsIgnoreCase(templateName)){
						containsTpl=true;
						break tplLoop;
					}
				}

				//if the revision does not contain the template, we have found
				//what we were looking for. add id of previous revision
				if(!containsTpl){
					revisionIds.add(prevRev.getRevisionID());
					break tsloop;
				}
				prevRev=rev;
			}
    	}

    	return revisionIds;
    }

    
    //////////
    

    /**
	 * Returns a list containing the ids of all pages that contain a
	 * template the name of which starts with any of the given Strings.
	 *
	 * @param templateFragments
	 *            the beginning of the templates that have to be matched
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
	 * @return An list with the ids of the pages that contain templates
	 *         beginning with any String in templateFragments
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    private List<Integer> getFragmentFilteredPageIds(List<String> templateFragments, boolean whitelist) throws WikiApiException{

		try {
	    	PreparedStatement statement = null;
			ResultSet result = null;
	        List<Integer> matchedPages = new LinkedList<Integer>();

			try {
				StringBuffer sqlString = new StringBuffer();
				StringBuffer subconditions = new StringBuffer();
				sqlString
						.append("SELECT p.pageId FROM "+ WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+ " AS tpl, "
								+ WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID+ " AS p WHERE tpl.templateId = p.templateId "+(whitelist?"AND":"AND NOT")+" (");
				for(@SuppressWarnings("unused") String fragment:templateFragments){
					if(subconditions.length()!=0){
						subconditions.append("OR ");
					}
					subconditions.append("tpl.templateName LIKE ?");
				}
				sqlString.append(subconditions);
				sqlString.append(")");

				statement = connection.prepareStatement(sqlString.toString());

				int curIdx=1;
				for(String fragment:templateFragments){
					fragment=fragment.toLowerCase().trim();
					fragment=fragment.replaceAll(" ","_");
					statement.setString(curIdx++, fragment + "%");
				}

				result = execute(statement);

				if (result == null) {
					throw new WikiPageNotFoundException("Nothing was found");
				}

				while (result.next()) {
		            matchedPages.add(result.getInt(1));
				}
			}
			finally {
				if (statement != null) {
					statement.close();
				}
				if (result != null) {
					result.close();
				}
			}

			return matchedPages;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}



	/**
	 * Returns a list containing the ids of all pages that contain a
	 * template the name of which starts with any of the given Strings.
	 *
	 * @param templateFragments
	 *            the beginning of the templates that have to be matched
	 * @return An list with the ids of the pages that contain templates
	 *         beginning with any String in templateFragments
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the template templates are corrupted)
	 */
    public List<Integer> getPageIdsContainingTemplateFragments(List<String> templateFragments) throws WikiApiException{
    	return getFragmentFilteredPageIds(templateFragments,true);
    }

    /**
	 * Returns a list containing the ids of all pages that contain a
	 * template the name of which starts with any of the given Strings.
	 *
	 * @param templateFragments
	 *            the beginning of the templates that have to be matched
	 * @return An list with the ids of the pages that do not contain templates
	 *         beginning with any String in templateFragments
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the template templates are corrupted)
	 */
    public List<Integer> getPageIdsNotContainingTemplateFragments(List<String> templateFragments) throws WikiApiException{
    	return getFragmentFilteredPageIds(templateFragments,false);
    }

    ///////////////////
    
    /**
	 * Returns a list containing the ids of all revisions that contain a
	 * template the name of which starts with any of the given Strings.
	 *
	 * @param templateFragments
	 *            the beginning of the templates that have to be matched
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
	 * @return An list with the ids of the revisions that contain templates
	 *         beginning with any String in templateFragments
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    private List<Integer> getFragmentFilteredRevisionIds(List<String> templateFragments, boolean whitelist) throws WikiApiException{

		try {
	    	PreparedStatement statement = null;
			ResultSet result = null;
	        List<Integer> matchedPages = new LinkedList<Integer>();

			try {
				StringBuffer sqlString = new StringBuffer();
				StringBuffer subconditions = new StringBuffer();
				sqlString
						.append("SELECT r.revisionId FROM "+ WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+ " AS tpl, "
								+ WikipediaTemplateInfoGenerator.TABLE_TPLID_REVISIONID+ " AS r WHERE tpl.templateId = r.templateId "+(whitelist?"AND":"AND NOT")+" (");
				for(@SuppressWarnings("unused") String fragment:templateFragments){
					if(subconditions.length()!=0){
						subconditions.append("OR ");
					}
					subconditions.append("tpl.templateName LIKE ?");
				}
				sqlString.append(subconditions);
				sqlString.append(")");

				statement = connection.prepareStatement(sqlString.toString());

				int curIdx=1;
				for(String fragment:templateFragments){
					fragment=fragment.toLowerCase().trim();
					fragment=fragment.replaceAll(" ","_");
					statement.setString(curIdx++, fragment + "%");
				}

				result = execute(statement);

				if (result == null) {
					throw new WikiPageNotFoundException("Nothing was found");
				}

				while (result.next()) {
		            matchedPages.add(result.getInt(1));
				}
			}
			finally {
				if (statement != null) {
					statement.close();
				}
				if (result != null) {
					result.close();
				}
			}

			return matchedPages;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}



	/**
	 * Returns a list containing the ids of all revisions that contain a
	 * template the name of which starts with any of the given Strings.
	 *
	 * @param templateFragments
	 *            the beginning of the templates that have to be matched
	 * @return An list with the ids of the revisions that contain templates
	 *         beginning with any String in templateFragments
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the template templates are corrupted)
	 */
    public List<Integer> getRevisionIdsContainingTemplateFragments(List<String> templateFragments) throws WikiApiException{
    	return getFragmentFilteredRevisionIds(templateFragments,true);
    }

    /**
	 * Returns a list containing the ids of all revisions that contain a
	 * template the name of which starts with any of the given Strings.
	 *
	 * @param templateFragments
	 *            the beginning of the templates that have to be matched
	 * @return An list with the ids of the revisions that do not contain templates
	 *         beginning with any String in templateFragments
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the template templates are corrupted)
	 */
    public List<Integer> getRevisionIdsNotContainingTemplateFragments(List<String> templateFragments) throws WikiApiException{
    	return getFragmentFilteredRevisionIds(templateFragments,false);
    }

    
    ///////////////////

    
    /**
     * Returns the ids of all pages that ever contained any of the given template names in the history of their existence.
     * 
     * @param templateNames template names to look for
     * @return list of page ids of the pages that once contained any of the given template names
     * @throws WikiApiException If there was any error retrieving the page object (most
	 *             likely if the template templates are corrupted)
     */
    public List<Integer> getIdsOfPagesThatEverContainedTemplateNames(List<String> templateNames) throws WikiApiException{
    	if(revApi==null){
    		revApi = new RevisionApi(wiki.getDatabaseConfiguration());
    	}
    	Set<Integer> pageIdSet = new HashSet<Integer>();

    	//TODO instead of getting rev ids and then getting page ids, do one query and make the join in the db directly
    	List<Integer> revsWithTemplate = getRevisionIdsContainingTemplateNames(templateNames);
    	for(int revId:revsWithTemplate){
    		pageIdSet.add(revApi.getPageIdForRevisionId(revId));
    	}
    	
    	List<Integer> pageIds = new LinkedList<Integer>();
    	pageIds.addAll(pageIdSet);
    	
    	return pageIds;  	
    }

    /**
     * Returns the ids of all pages that ever contained any template that started with any of the given template fragments 
     * 
     * @param template template-fragments to look for 
     * @return list of page ids of the pages that once contained any template that started with any of the given template fragments
     * @throws WikiApiException If there was any error retrieving the page object (most
	 *             likely if the template templates are corrupted)
     */
    public List<Integer> getIdsOfPagesThatEverContainedTemplateFragments(List<String> templateFraments) throws WikiApiException{
    	if(revApi==null){
    		revApi = new RevisionApi(wiki.getDatabaseConfiguration());
    	}
    	Set<Integer> pageIdSet = new HashSet<Integer>();
    	
    	//TODO instead of getting rev ids and then getting page ids, do one query and make the join in the db directly
    	List<Integer> revsWithTemplate = getRevisionIdsContainingTemplateFragments(templateFraments);
    	for(int revId:revsWithTemplate){
    		pageIdSet.add(revApi.getPageIdForRevisionId(revId));
    	}
    	
    	List<Integer> pageIds = new LinkedList<Integer>();
    	pageIds.addAll(pageIdSet);
    	
    	return pageIds;  	
    }

    ///////////////////
    
    
	/**
	 * Returns a list containing the ids of all pages that contain a template
	 * the name of which equals any of the given Strings.
	 *
	 * @param templateNames
	 *            the names of the template that we want to match
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
	 * @return A list with the ids of all pages that contain any of the the
	 *         specified templates
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    private List<Integer> getFilteredPageIds(List<String> templateNames, boolean whitelist) throws WikiApiException{
		try {
	    	PreparedStatement statement = null;
			ResultSet result = null;
	        List<Integer> matchedPages = new LinkedList<Integer>();

			try {
				StringBuffer sqlString = new StringBuffer();
				StringBuffer subconditions = new StringBuffer();
				sqlString.append("SELECT p.pageId FROM "+ WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+ " AS tpl, "
								+ WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID+ " AS p WHERE tpl.templateId = p.templateId "+(whitelist?"AND":"AND NOT")+" (");

				for(@SuppressWarnings("unused") String name:templateNames){
					if(subconditions.length()!=0){
						subconditions.append("OR ");
					}
					subconditions.append("tpl.templateName = ?");
				}
				sqlString.append(subconditions);
				sqlString.append(")");

				statement = connection.prepareStatement(sqlString.toString());

				int curIdx=1;
				for(String name:templateNames){
					name=name.toLowerCase().trim();
					name=name.replaceAll(" ", "_");
					statement.setString(curIdx++, name);
				}

				result = execute(statement);

				if (result == null) {
					throw new WikiPageNotFoundException("Nothing was found");
				}

				while (result.next()) {
		            matchedPages.add(result.getInt(1));
				}
			}
			finally {
				if (statement != null) {
					statement.close();
				}
				if (result != null) {
					result.close();
				}
			}

			return matchedPages;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}


    
	/**
	 * Returns a list containing the ids of all pages that contain a template
	 * the name of which equals any of the given Strings.
	 *
	 * @param templateNames
	 *            the names of the template that we want to match
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
	 * @return A list with the ids of all pages that contain any of the the
	 *         specified templates
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public List<Integer> getPageIdsContainingTemplateNames(List<String> templateNames) throws WikiApiException{
    	return getFilteredPageIds(templateNames, true);
    }
	/**
	 * Returns a list containing the ids of all pages that do not contain a template
	 * the name of which equals any of the given Strings.
	 *
	 * @param templateNames
	 *            the names of the template that we want to match
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
	 * @return A list with the ids of all pages that do not contain any of the the
	 *         specified templates
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public List<Integer> getPageIdsNotContainingTemplateNames(List<String> templateNames) throws WikiApiException{
    	return getFilteredPageIds(templateNames, false);
    }

    
    
    
	/**
	 * Returns a list containing the ids of all revisions that contain a template
	 * the name of which equals any of the given Strings.
	 *
	 * @param templateNames
	 *            the names of the template that we want to match
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
	 * @return A list with the ids of all revisions that contain any of the the
	 *         specified templates
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    private List<Integer> getFilteredRevisionIds(List<String> templateNames, boolean whitelist) throws WikiApiException{
		try {
	    	PreparedStatement statement = null;
			ResultSet result = null;
	        List<Integer> matchedPages = new LinkedList<Integer>();

			try {
				StringBuffer sqlString = new StringBuffer();
				StringBuffer subconditions = new StringBuffer();
				sqlString.append("SELECT r.revisionId FROM "+ WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+ " AS tpl, "
								+ WikipediaTemplateInfoGenerator.TABLE_TPLID_REVISIONID+ " AS r WHERE tpl.templateId = r.templateId "+(whitelist?"AND":"AND NOT")+" (");

				for(@SuppressWarnings("unused") String name:templateNames){
					if(subconditions.length()!=0){
						subconditions.append("OR ");
					}
					subconditions.append("tpl.templateName = ?");
				}
				sqlString.append(subconditions);
				sqlString.append(")");

				statement = connection.prepareStatement(sqlString.toString());

				int curIdx=1;
				for(String name:templateNames){
					name=name.toLowerCase().trim();
					name=name.replaceAll(" ", "_");
					statement.setString(curIdx++, name);
				}

				result = execute(statement);

				if (result == null) {
					throw new WikiPageNotFoundException("Nothing was found");
				}

				while (result.next()) {
		            matchedPages.add(result.getInt(1));
				}
			}
			finally {
				if (statement != null) {
					statement.close();
				}
				if (result != null) {
					result.close();
				}
			}

			return matchedPages;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}
    
    
	/**
	 * Returns a list containing the ids of all revisions that contain a template
	 * the name of which equals any of the given Strings.
	 *
	 * @param templateNames
	 *            the names of the template that we want to match
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
	 * @return A list with the ids of all revisions that contain any of the the
	 *         specified templates
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public List<Integer> getRevisionIdsContainingTemplateNames(List<String> templateNames) throws WikiApiException{
    	return getFilteredRevisionIds(templateNames, true);
    }

    /**
	 * Returns a list containing the ids of all revisions that do not contain a template
	 * the name of which equals any of the given Strings.
	 *
	 * @param templateNames
	 *            the names of the template that we want to match
	 * @param whitelist
	 *            whether to return pages containing these templates (true) or return pages NOT containing these templates (false)
	 * @return A list with the ids of all revisions that do not contain any of the the
	 *         specified templates
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public List<Integer> getRevisionIdsNotContainingTemplateNames(List<String> templateNames) throws WikiApiException{
    	return getFilteredRevisionIds(templateNames, false);
    }
    

    
    
	/**
	 * Returns the names of all templates contained in the specified page.
	 *
	 * @param pageId
	 *            the page object for which the templates should be retrieved
	 * @return A List with the names of the templates contained in the specified
	 *         page
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public List<String> getTemplateNamesFromPage(Page p) throws WikiApiException{
    	return getTemplateNamesFromPage(p.getPageId());
    }

	/**
	 * Returns the names of all templates contained in the specified page.
	 *
	 * @param pageId
	 *            the title of the page for which the templates should be
	 *            retrieved
	 * @return A List with the names of the templates contained in the specified
	 *         page
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public List<String> getTemplateNamesFromPage(String pageTitle) throws WikiApiException{
    	Page p=null;
    	try{
    		p = wiki.getPage(pageTitle);
    	}catch (WikiApiException e) {
    		return new ArrayList<String>();
		}
    	return getTemplateNamesFromPage(p);
    }


	/**
	 * Returns the names of all templates contained in the specified page.
	 *
	 * @param pageId
	 *            the id of the Wiki page
	 * @return A List with the names of the templates contained in the specified
	 *         page
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public List<String> getTemplateNamesFromPage(int pageId) throws WikiApiException{
    	if(pageId<1){
    		throw new WikiApiException("Page ID must be > 0");
    	}
		try {
	    	PreparedStatement statement = null;
			ResultSet result = null;
	        List<String> templateNames = new LinkedList<String>();

			try {
				statement = connection.prepareStatement("SELECT tpl.templateName FROM "+ WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+ " AS tpl, "
						+ WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID+ " AS p WHERE tpl.templateId = p.templateId AND p.pageId = ?");
				statement.setInt(1, pageId);

				result = execute(statement);

				if (result == null) {
					return templateNames;
				}

				while (result.next()) {
					templateNames.add(result.getString(1).toLowerCase());
				}
			}
			finally {
				if (statement != null) {
					statement.close();
				}
				if (result != null) {
					result.close();
				}
			}

			return templateNames;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

    
	/**
	 * Returns the names of all templates contained in the specified revision.
	 *
	 * @param revid
	 *            the revision id
	 * @return A List with the names of the templates contained in the specified
	 *         revision
	 * @throws WikiApiException
	 *             If there was any error retrieving the page object (most
	 *             likely if the templates are corrupted)
	 */
    public List<String> getTemplateNamesFromRevision(int revid) throws WikiApiException{
    	if(revid<1){
    		throw new WikiApiException("Revision ID must be > 0");
    	}
		try {
	    	PreparedStatement statement = null;
			ResultSet result = null;
	        List<String> templateNames = new LinkedList<String>();

			try {
				statement = connection.prepareStatement("SELECT tpl.templateName FROM "+ WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+ " AS tpl, "
						+ WikipediaTemplateInfoGenerator.TABLE_TPLID_REVISIONID+ " AS p WHERE tpl.templateId = p.templateId AND p.revisionId = ?");
				statement.setInt(1, revid);

				result = execute(statement);

				if (result == null) {
					return templateNames;
				}

				while (result.next()) {
					templateNames.add(result.getString(1).toLowerCase());
				}
			}
			finally {
				if (statement != null) {
					statement.close();
				}
				if (result != null) {
					result.close();
				}
			}

			return templateNames;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

    
    /**
     * Determines whether a given revision contains a given template name
     * 
     * @param revId
     * @param templateName
     * @return
     * @throws WikiApiException
     */
    public boolean revisionContainsTemplateName(int revId, String templateName) throws WikiApiException{
    	List<String> tplList = getTemplateNamesFromRevision(revId);
    	for(String tpl:tplList){
    		if(tpl.equalsIgnoreCase(templateName)){
    			return true;
    		}
    	}
    	return false;  	
    }

    /**
     * Determines whether a given revision contains a template starting witht the given fragment
     * 
     * @param revId
     * @param templateFragment
     * @return
     * @throws WikiApiException
     */
    public boolean revisionContainsTemplateFragment(int revId, String templateFragment) throws WikiApiException{
    	List<String> tplList = getTemplateNamesFromRevision(revId);
    	for(String tpl:tplList){
    		if(tpl.toLowerCase().startsWith(templateFragment.toLowerCase())){
    			return true;
    		}
    	}
    	return false;  	
    }

    /**
     * Does the same as revisionContainsTemplateName() without using a template index
     * 
     * @param revId
     * @param templateName
     * @return
     * @throws WikiApiException
     */
    public boolean revisionContainsTemplateNameWithoutIndex(int revId, String templateName) throws WikiApiException{    	
    	if(revApi==null){
    		revApi = new RevisionApi(wiki.getDatabaseConfiguration());
    	}
    	if(parser==null){
    		//TODO switch to SWEBLE
    		MediaWikiParserFactory pf = new MediaWikiParserFactory(
    				wiki.getDatabaseConfiguration().getLanguage());
    		pf.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
    		parser = pf.createParser();
    	}
    	    	
    	List<Template> tplList = parser.parse(revApi.getRevision(revId).getRevisionText()).getTemplates();
    	for(Template tpl:tplList){
    		if(tpl.getName().equalsIgnoreCase(templateName)){
    			return true;
    		}
    	}
    	return false;  	
    }

    /**
     * Does the same as revisionContainsTemplateFragment() without using a template index
     * 
     * @param revId
     * @param templateFragment
     * @return
     * @throws WikiApiException
     */
    public boolean revisionContainsTemplateFragmentWithoutIndex(int revId, String templateFragment) throws WikiApiException{
    	if(revApi==null){
    		revApi = new RevisionApi(wiki.getDatabaseConfiguration());
    	}
    	if(parser==null){
    		//TODO switch to SWEBLE
    		MediaWikiParserFactory pf = new MediaWikiParserFactory(
    				wiki.getDatabaseConfiguration().getLanguage());
    		pf.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
    		parser = pf.createParser();
    	}

    	List<Template> tplList = parser.parse(revApi.getRevision(revId).getRevisionText()).getTemplates();
    	for(Template tpl:tplList){
    		if(tpl.getName().toLowerCase().startsWith(templateFragment.toLowerCase())){
    			return true;
    		}
    	}
    	return false;  	
    }
    
    
    
    /**
     * For a given page (pageId), this method returns all adjacent revision pairs in which a given template 
     * has been removed or added (depending on the RevisionPairType) in the second pair part.
     * 
     * @param pageId id of the page whose revision history should be inspected
     * @param template the template to look for
     * @param type the type of template change (add or remove) that should be extracted
     * @return list of revision pairs containing the desired template changes
     */
    public List<RevisionPair> getRevisionPairs(int pageId, String template, RevisionPair.RevisionPairType type) throws WikiApiException{
    	if(revApi==null){
    		revApi = new RevisionApi(wiki.getDatabaseConfiguration());
    	}

    	List<RevisionPair> resultList = new LinkedList<RevisionPair>();    	    	
    	Map<Timestamp, Boolean> tplIndexMap = new HashMap<Timestamp, Boolean>();

    	List<Timestamp> revTsList = revApi.getRevisionTimestamps(pageId);
    	for(Timestamp ts:revTsList){
    		tplIndexMap.put(ts, revisionContainsTemplateName(revApi.getRevision(pageId, ts).getRevisionID(), template));
    	}
    	
		SortedSet<Entry<Timestamp, Boolean>> entries = new TreeSet<Entry<Timestamp, Boolean>>(
				new Comparator<Entry<Timestamp, Boolean>>() {
					public int compare(Entry<Timestamp, Boolean> e1, Entry<Timestamp, Boolean> e2) {
						return e1.getKey().compareTo(e2.getKey());
					}
				});
		entries.addAll(tplIndexMap.entrySet());    	
    	
    	Entry<Timestamp,Boolean> prev=null;
    	Entry<Timestamp,Boolean> current=null;
    	for(Entry<Timestamp,Boolean> e:entries){
    		current=e;
    		//check pair
    		if(prev!=null&&prev.getValue()!=current.getValue()){
    			//case: template has been deleted since last revision
    			if(prev.getValue()&&!current.getValue()&&type==RevisionPairType.deleteTemplate){
    				resultList.add(new RevisionPair(revApi.getRevision(pageId, prev.getKey()),revApi.getRevision(pageId, current.getKey()),template,RevisionPairType.deleteTemplate));
    			}
    			//case: template has been added since last revision
    			if(!prev.getValue()&&current.getValue()&&type==RevisionPairType.addTemplate){
    				resultList.add(new RevisionPair(revApi.getRevision(pageId, prev.getKey()),revApi.getRevision(pageId, current.getKey()),template,RevisionPairType.addTemplate));    				
    			}
    		}
    		prev=current;
    	}    	    	
    	return resultList;
    }

    /**
     * Does the same as getRevisionPairs(), but does not use a template index
     * 
     * @param pageId id of the page whose revision history should be inspected
     * @param template the template to look for
     * @param type the type of template change (add or remove) that should be extracted
     * @return list of revision pairs containing the desired template changes
     */
    public List<RevisionPair> getRevisionPairsWithoutIndex(int pageId, String template, RevisionPair.RevisionPairType type) throws WikiApiException{
    	System.err.println("This methods has to parse each revision of the given page. If you have a revision-template index, please use getRevisionPairs().");
    	if(revApi==null){
    		revApi = new RevisionApi(wiki.getDatabaseConfiguration());
    	}

    	List<RevisionPair> resultList = new LinkedList<RevisionPair>();    	    	
    	Map<Timestamp, Boolean> tplIndexMap = new HashMap<Timestamp, Boolean>();

    	List<Timestamp> revTsList = revApi.getRevisionTimestamps(pageId);
    	for(Timestamp ts:revTsList){
    		tplIndexMap.put(ts, revisionContainsTemplateNameWithoutIndex(revApi.getRevision(pageId, ts).getRevisionID(), template));
    	}
    	
		SortedSet<Entry<Timestamp, Boolean>> entries = new TreeSet<Entry<Timestamp, Boolean>>(
				new Comparator<Entry<Timestamp, Boolean>>() {
					public int compare(Entry<Timestamp, Boolean> e1, Entry<Timestamp, Boolean> e2) {
						return e1.getKey().compareTo(e2.getKey());
					}
				});
		entries.addAll(tplIndexMap.entrySet());    	
    	
    	Entry<Timestamp,Boolean> prev=null;
    	Entry<Timestamp,Boolean> current=null;
    	for(Entry<Timestamp,Boolean> e:entries){
    		current=e;
    		//check pair
    		if(prev!=null&&prev.getValue()!=current.getValue()){
    			//case: template has been deleted since last revision
    			if(prev.getValue()&&!current.getValue()&&type==RevisionPairType.deleteTemplate){
    				resultList.add(new RevisionPair(revApi.getRevision(pageId, prev.getKey()),revApi.getRevision(pageId, current.getKey()),template,RevisionPairType.deleteTemplate));
    			}
    			//case: template has been added since last revision
    			if(!prev.getValue()&&current.getValue()&&type==RevisionPairType.addTemplate){
    				resultList.add(new RevisionPair(revApi.getRevision(pageId, prev.getKey()),revApi.getRevision(pageId, current.getKey()),template,RevisionPairType.addTemplate));    				
    			}
    		}
    		prev=current;
    	}    	    	
    	return resultList;
    }

    
	/**
	 * Checks if a specific table exists
	 *
	 * @param table
	 *            the table to check

	 * @return true, if table exists, false else
	 * @throws SQLException
	 *             if an error occurs connecting to or querying the db
	 * @author Oliver Ferschke
	 */
	public boolean tableExists(String table)
		throws SQLException
	{

		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = this.connection.prepareStatement("SHOW TABLES;");
			result = execute(statement);

			if (result == null) {
				return false;
			}
			boolean found = false;
			while(result.next()){
					if(table.equalsIgnoreCase(result.getString(1))){
						found = true;
					}
			}
			return found;

		}
		finally {
			if (statement != null) {
				statement.close();
			}
			if (result != null) {
				result.close();
			}
		}

	}

	private Connection getConnection(Wikipedia wiki)
		throws WikiApiException
	{
		DatabaseConfiguration config = wiki.getDatabaseConfiguration();

		Connection c;
		try {

			String driverDB = "com.mysql.jdbc.Driver";
			Class.forName(driverDB);

			c = DriverManager.getConnection("jdbc:mysql://" + config.getHost()
					+ "/" + config.getDatabase()+"?autoReconnect=true", config.getUser(),
					config.getPassword());

			if (!c.isValid(5)) {
				throw new WikiApiException(
						"Connection could not be established.");
			}
		}
		catch (SQLException e) {
			throw new WikiApiException(e);
		}
		catch (ClassNotFoundException e) {
			throw new WikiApiException(e);
		}

		return c;
	}

	public void close() throws SQLException{
		if(this.connection!=null){
			this.connection.close();
		}
	}

	public void reconnect() throws SQLException{
		close();
		try{
			this.connection=getConnection(wiki);
		}catch(WikiApiException e){
			close();
			System.err.println("Could not reconnect. Closing connection...");
		}
	}

	private ResultSet execute(PreparedStatement state) throws SQLException{
		ResultSet res=null;
		try {
			res = state.executeQuery();
		}
		catch (Exception e) {
			reconnect();
			res = state.executeQuery();
		}
		return res;
	}

}
