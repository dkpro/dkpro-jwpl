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
package de.tudarmstadt.ukp.wikipedia.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;

/**
 * This class gives access to the additional information created by
 * the TemplateInfoGenerator.
 *
 * TODO There is quite some redundancy in the code. The parts responsible for
 * creating the queries might be pulled out of the methods an be reused several
 * times.
 *
 * @author Oliver Ferschke
 */
public class WikipediaTemplateInfo {

    private final Wikipedia wiki;
    private Connection connection=null;

    /**
     */
    public WikipediaTemplateInfo(Wikipedia pWiki) throws WikiApiException {

    	this.wiki = pWiki;
        this.connection=getConnection(wiki);

        try{
    		if (!tableExists(WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME)) {
    			throw new WikiApiException(
    					"Missing tables. Please use the WikipediaTemplateInfoGenerator to generate the template data.");
    		}
        }catch(SQLException e){
			throw new WikiApiException(
					"Could not access database.");
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
    private Integer countPagesContainingOrNotContainingTemplateFragments(List<String> templateFragments, boolean whitelist)
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
					statement.setString(curIdx++, fragment + "%");
				}

				result = statement.executeQuery();

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
		return countPagesContainingOrNotContainingTemplateFragments(templateFragments, true);
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
		return countPagesContainingOrNotContainingTemplateFragments(templateFragments, false);
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
	 *             likely if the template templates are corrupted)
	 */
	private Integer countPagesContainingOrNotContainingTemplateNames(List<String> templateNames, boolean whitelist)
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
				name=name.toLowerCase();
				statement.setString(curIdx++, name);
			}

			result = statement.executeQuery();

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
	 *             likely if the template templates are corrupted)
	 */
	public Integer countPagesContainingTemplateNames(List<String> templateNames) throws WikiApiException{
		return countPagesContainingOrNotContainingTemplateNames(templateNames, true);
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
	 *             likely if the template templates are corrupted)
	 */
	public Integer countPagesNotContainingTemplateNames(List<String> templateNames) throws WikiApiException{
		return countPagesContainingOrNotContainingTemplateNames(templateNames, false);
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
	 *             likely if the template templates are corrupted)
	 */
    private Iterable<Page> getPagesContainingOrNotContainingTemplateFragments(List<String> templateFragments, boolean whitelist) throws WikiApiException{

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
					fragment=fragment.toLowerCase();
					statement.setString(curIdx++, fragment + "%");
				}

				result = statement.executeQuery();

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
				
				
				sqlString.append("SELECT tpl.templateId FROM "+WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+" AS tpl WHERE tpl.templateName='"+templateName+"'");

				statement = connection.prepareStatement(sqlString.toString());
			
				result = statement.executeQuery();

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
	 *             likely if the template templates are corrupted)
	 */
    public Iterable<Page> getPagesContainingTemplateFragments(List<String> templateFragments) throws WikiApiException{
    	return getPagesContainingOrNotContainingTemplateFragments(templateFragments, true);
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
	 *             likely if the template templates are corrupted)
	 */
    public Iterable<Page> getPagesNotContainingTemplateFragments(List<String> templateFragments) throws WikiApiException{
    	return getPagesContainingOrNotContainingTemplateFragments(templateFragments, false);
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
	 *             likely if the template templates are corrupted)
	 */
    private Iterable<Page> getPagesContainingOrNotContainingTemplateNames(List<String> templateNames, boolean whitelist) throws WikiApiException{
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
					name=name.toLowerCase();
					statement.setString(curIdx++, name);
				}

				result = statement.executeQuery();

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
	 *             likely if the template templates are corrupted)
	 */
    public Iterable<Page> getPagesContainingTemplateNames(List<String> templateNames) throws WikiApiException{
    	return getPagesContainingOrNotContainingTemplateNames(templateNames, true);
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
	 *             likely if the template templates are corrupted)
	 */
    public Iterable<Page> getPagesNotContainingTemplateNames(List<String> templateNames) throws WikiApiException{
    	return getPagesContainingOrNotContainingTemplateNames(templateNames, false);
    }


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
	 *             likely if the template templates are corrupted)
	 */
    private List<Integer> getPageIdsContainingOrNotContainingTemplateFragments(List<String> templateFragments, boolean whitelist) throws WikiApiException{

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
					fragment=fragment.toLowerCase();
					statement.setString(curIdx++, fragment + "%");
				}

				result = statement.executeQuery();

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
    	return getPageIdsContainingOrNotContainingTemplateFragments(templateFragments,true);
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
    	return getPageIdsContainingOrNotContainingTemplateFragments(templateFragments,false);
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
	 *             likely if the template templates are corrupted)
	 */
    private List<Integer> getPageIdsContainingOrNotContainingTemplateNames(List<String> templateNames, boolean whitelist) throws WikiApiException{
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
					name=name.toLowerCase();
					statement.setString(curIdx++, name);
				}

				result = statement.executeQuery();

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
	 *             likely if the template templates are corrupted)
	 */
    public List<Integer> getPageIdsContainingTemplateNames(List<String> templateNames) throws WikiApiException{
    	return getPageIdsContainingOrNotContainingTemplateNames(templateNames, true);
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
	 *             likely if the template templates are corrupted)
	 */
    public List<Integer> getPageIdsNotContainingTemplateNames(List<String> templateNames) throws WikiApiException{
    	return getPageIdsContainingOrNotContainingTemplateNames(templateNames, false);
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
	 *             likely if the template templates are corrupted)
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
	 *             likely if the template templates are corrupted)
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
	 *             likely if the template templates are corrupted)
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

				result = statement.executeQuery();

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
			result = statement.executeQuery();

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
					+ "/" + config.getDatabase(), config.getUser(),
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


}
