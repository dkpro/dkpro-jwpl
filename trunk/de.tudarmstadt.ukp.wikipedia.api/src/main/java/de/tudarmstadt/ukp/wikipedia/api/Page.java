/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 *     Oliver Ferschke
 *     Samy Ateia
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.LockOptions;
import org.hibernate.Session;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.api.hibernate.PageDAO;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.util.UnmodifiableArraySet;

/**
 * Represents a Wikipedia article page.
 *
 * @author zesch
 *
 */
// Adapter class for hidding hibernate session management from the user.
public class Page
	implements WikiConstants
{
	private final Wikipedia wiki;

	private final PageDAO pageDAO;

	// The hibernatePage that is represented by this WikiAPI page.
	// The indirection is necessary to shield the user from Hibernate sessions.
	private de.tudarmstadt.ukp.wikipedia.api.hibernate.Page hibernatePage;

//	// The String that was used to search that page
//	// If it differs from the page's name, we searched for a redirect.
//	private String searchString;

	// If we search for a redirect, the corresponding page is delivered transparently.
	// In that case, isRedirect is set to true, to indicate that.
	// Note: The page itself is _not_ a redirect, it is just a page.
	private boolean isRedirect = false;

	/**
	 * Creates a page object.
	 *
	 * @param wiki
	 *            The wikipedia object.
	 * @param id
	 *            The hibernate id of the page.
	 * @throws WikiApiException
	 */
	protected Page(Wikipedia wiki, long id)
		throws WikiApiException
	{
		this.wiki = wiki;
		this.pageDAO = new PageDAO(wiki);
		fetchByHibernateId(id);
	}

	/**
	 * Creates a page object.
	 *
	 * @param wiki
	 *            The wikipedia object.
	 * @param pageID
	 *            The pageID of the page.
	 * @throws WikiApiException
	 */
	protected Page(Wikipedia wiki, int pageID)
		throws WikiApiException
	{
		this.wiki = wiki;
		this.pageDAO = new PageDAO(wiki);
		fetchByPageId(pageID);
	}

	/**
	 * Creates a page object.
	 *
	 * @param wiki
	 *            The wikipedia object.
	 * @param pName
	 *            The name of the page.
	 * @throws WikiApiException
	 */
	public Page(Wikipedia wiki, String pName)
		throws WikiApiException
	{
		if (pName == null || pName.length() == 0) {
			throw new WikiPageNotFoundException();
		}
		this.wiki = wiki;
		this.pageDAO = new PageDAO(wiki);
		Title pageTitle = new Title(pName);
		fetchByTitle(pageTitle);
	}

	/**
	 * Creates a Page object from an already retrieved hibernate Page
	 *
	 * @param wiki
	 *            The wikipedia object.
	 * @param id
	 *            The hibernate id of the page.
	 * @param hibernatePage
	 * 			  The {@code api.hibernatePage} that has already been retrieved
	 * @throws WikiApiException
	 */
	protected Page(Wikipedia wiki, long id,
			de.tudarmstadt.ukp.wikipedia.api.hibernate.Page hibernatePage)
		throws WikiApiException
	{
		this.wiki = wiki;
		this.pageDAO = new PageDAO(wiki);
		this.hibernatePage = hibernatePage;
	}

	/**
	 * @throws WikiApiException
	 * @see de.tudarmstadt.ukp.wikipedia.api.Page#Page(long)
	 */
	private void fetchByHibernateId(long id)
		throws WikiApiException
	{
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        hibernatePage = pageDAO.findById(id);
        session.getTransaction().commit();

        if (hibernatePage == null) {
            throw new WikiPageNotFoundException("No page with id " + id + " was found.");
        }
	}

	private void fetchByPageId(int pageID)
		throws WikiApiException
	{
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
		hibernatePage = (de.tudarmstadt.ukp.wikipedia.api.hibernate.Page) session
				.createQuery("from Page where pageId = ?").setInteger(0, pageID).uniqueResult();
        session.getTransaction().commit();

        if (hibernatePage == null) {
            throw new WikiPageNotFoundException("No page with page id " + pageID + " was found.");
        }
	}

	/**
	 * CAUTION: Only returns 1 result, even if several results are possible.
	 *
	 * @param pTitle
	 * @throws WikiApiException
	 */
	private void fetchByTitle(Title pTitle)
		throws WikiApiException
	{
		String searchString = pTitle.getWikiStyleTitle();
		Session session;
		session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		Integer pageId = (Integer) session
				.createSQLQuery(
						"select pml.pageID from PageMapLine as pml where pml.name = ? LIMIT 1")
				.setString(0, searchString).uniqueResult();

        session.getTransaction().commit();

        if (pageId == null) {
			throw new WikiPageNotFoundException("No page with name " + searchString + " was found.");
		}
		fetchByPageId(pageId);

//		hibernatePage = (de.tudarmstadt.ukp.wikipedia.api.hibernate.Page) session.createSQLQuery(
//				"SELECT p.* " +
//				"FROM Page AS p " +
//				"JOIN PageMapLine AS pml ON p.pageId = pml.pageID " +
//				"WHERE pml.name COLLATE utf8_bin = ? LIMIT 1")
//				.addEntity(de.tudarmstadt.ukp.wikipedia.api.hibernate.Page.class)
//				.setString(0, searchString)
//				.uniqueResult();
//
//		session.getTransaction().commit();
//
//
//		// if there is no page with this name, the hibernatePage is null
//		if (hibernatePage == null) {
//			throw new WikiPageNotFoundException("No page with name " + searchString + " was found.");
//		}

        // If this page was created using a redirect searchString, then set the isRedirect flag.
        // A redirect searchString differs from the page's name.
        if (searchString != null) {
            if (!searchString.equals(getTitle().getRawTitleText())) {
                this.isRedirect = true;
            }
        }
	}

	/**
	 * @return Returns the id.
	 */
	protected long __getId()
	{
//		Session session = this.wiki.__getHibernateSession();
//		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		long id = hibernatePage.getId();
//		session.getTransaction().commit();
		return id;
	}

	/**
	 * @return Returns a unique page id.
	 */
	public int getPageId()
	{
//		Session session = this.wiki.__getHibernateSession();
//		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		int id = hibernatePage.getPageId();
//		session.getTransaction().commit();
		return id;
	}

	/**
	 * Returns a set of categories that this page belongs to.
	 *
	 * @return The a set of categories that this page belongs to.
	 */
	public Set<Category> getCategories()
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		session.buildLockRequest(LockOptions.NONE).lock(hibernatePage);
		Set<Integer> tmp = new UnmodifiableArraySet<Integer>(hibernatePage.getCategories());
		session.getTransaction().commit();

		Set<Category> categories = new HashSet<Category>();
		for (int pageID : tmp) {
			categories.add(wiki.getCategory(pageID));
		}

		return categories;
	}

	/**
	 * This is a more efficient shortcut for writing "getCategories().size()", as that would require
	 * to load all the categories first.
	 *
	 * @return The number of categories.
	 */
	public int getNumberOfCategories()
	{
		BigInteger nrOfCategories = new BigInteger("0");

		long id = __getId();
		Session session = wiki.__getHibernateSession();
		 session.beginTransaction();
		Object returnValue = session
				.createSQLQuery("select count(pages) from page_categories where id = ?")
				.setLong(0, id).uniqueResult();
		 session.getTransaction().commit();

		if (returnValue != null) {
			nrOfCategories = (BigInteger) returnValue;
		}
		return nrOfCategories.intValue();
	}

	/**
	 * Returns the set of pages that have a link pointing to this page. <b>Warning:</b> Do not use
	 * this for getting the number of inlinks with getInlinks().size(). This is too slow. Use
	 * getNumberOfInlinks() instead.
	 *
	 * @return The set of pages that have a link pointing to this page.
	 * @throws WikiApiException
	 */
	public Set<Page> getInlinks()
	{
		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		session.buildLockRequest(LockOptions.NONE).lock(hibernatePage);
		// Have to copy links here since getPage later will close the session.
		Set<Integer> pageIDs = new UnmodifiableArraySet<Integer>(hibernatePage.getInLinks());
		session.getTransaction().commit();

		Set<Page> pages = new HashSet<Page>();
		for (int pageID : pageIDs) {
			try {
				pages.add(wiki.getPage(pageID));
			}
			catch (WikiApiException e) {
				// Silently ignore if a page could not be found
				// There may be inlinks that do not come from an existing page.
				continue;
			}
		}

		return pages;
	}

	/**
	 * This is a more efficient shortcut for writing "getInlinks().size()", as that would require to
	 * load all the inlinks first.
	 *
	 * @return The number of inlinks.
	 */
	public int getNumberOfInlinks()
	{
		BigInteger nrOfInlinks = new BigInteger("0");

		long id = __getId();
		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
		Object returnValue = session
				.createSQLQuery("select count(pi.inLinks) from page_inlinks as pi where pi.id = ?")
				.setLong(0, id).uniqueResult();
		session.getTransaction().commit();

		if (returnValue != null) {
			nrOfInlinks = (BigInteger) returnValue;
		}
		return nrOfInlinks.intValue();
	}

	/**
	 * The result set may also contain links from non-existing pages. It is in the responsibility of
	 * the user to check whether the page exists.
	 *
	 * @return Returns the IDs of the inLinks of this page.
	 */
	public Set<Integer> getInlinkIDs()
	{
		Set<Integer> tmpSet = new HashSet<Integer>();

		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
		session.buildLockRequest(LockOptions.NONE).lock(hibernatePage);

		tmpSet.addAll(hibernatePage.getInLinks());

		session.getTransaction().commit();

		return tmpSet;
	}

	/**
	 * Returns the set of pages that are linked from this page. Outlinks in a page might also point
	 * to non-existing pages. They are not included in the result set. <b>Warning:</b> Do not use
	 * this for getting the number of outlinks with getOutlinks().size(). This is too slow. Use
	 * getNumberOfOutlinks() instead.
	 *
	 * @return The set of pages that are linked from this page.
	 * @throws WikiApiException
	 */
	public Set<Page> getOutlinks()
	{
		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		session.buildLockRequest(LockOptions.NONE).lock(hibernatePage);
		// Have to copy links here since getPage later will close the session.
		Set<Integer> tmpSet = new UnmodifiableArraySet<Integer>(hibernatePage.getOutLinks());
		session.getTransaction().commit();

		Set<Page> pages = new HashSet<Page>();
		for (int pageID : tmpSet) {
			try {
				pages.add(wiki.getPage(pageID));
			}
			catch (WikiApiException e) {
				// Silently ignore if a page could not be found.
				// There may be outlinks pointing to non-existing pages.
			}
		}
		return pages;
	}

	/**
	 * This is a more efficient shortcut for writing "getOutlinks().size()", as that would require
	 * to load all the outlinks first.
	 *
	 * @return The number of outlinks.
	 */
	public int getNumberOfOutlinks()
	{
		BigInteger nrOfOutlinks = new BigInteger("0");

		long id = __getId();
		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
		Object returnValue = session
				.createSQLQuery("select count(outLinks) from page_outlinks where id = ?")
				.setLong(0, id).uniqueResult();
		session.getTransaction().commit();

		if (returnValue != null) {
			nrOfOutlinks = (BigInteger) returnValue;
		}
		return nrOfOutlinks.intValue();
	}

	/**
	 * The result set may also contain links from non-existing pages. It is in the responsibility of
	 * the user to check whether the page exists.
	 *
	 * @return Returns the IDs of the outLinks of this page.
	 */
	public Set<Integer> getOutlinkIDs()
	{
		Set<Integer> tmpSet = new HashSet<Integer>();

		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
		session.buildLockRequest(LockOptions.NONE).lock(hibernatePage);

		tmpSet.addAll(hibernatePage.getOutLinks());

		session.getTransaction().commit();
		return tmpSet;
	}

	/**
	 * Returns the title of the page.
	 *
	 * @return The title of the page.
	 * @throws WikiTitleParsingException
	 */
	public Title getTitle()
		throws WikiTitleParsingException
	{
		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		String name = hibernatePage.getName();
		session.getTransaction().commit();
		Title title = new Title(name);
		return title;
	}

	/**
	 * Returns the set of strings that are redirects to this page.
	 *
	 * @return The set of redirect strings.
	 */
	public Set<String> getRedirects()
	{
		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		session.buildLockRequest(LockOptions.NONE).lock(hibernatePage);
		Set<String> tmpSet = new HashSet<String>(hibernatePage.getRedirects());
		session.getTransaction().commit();
		return tmpSet;
	}

	/**
	 * Returns the text of the page with media wiki markup.
	 *
	 * @return The text of the page with media wiki markup.
	 */
	public String getText()
	{
		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		String text = hibernatePage.getText();
		session.getTransaction().commit();

		// Normalize strings read from the DB to use "\n" for all line breaks.
		StringBuilder sb = new StringBuilder(text);
		int t = 0;
		boolean seenLineBreak = false;
		char breakQue = ' ';
		for (int s = 0; s < sb.length(); s++) {
			char c = sb.charAt(s);
			boolean isLineBreak = c == '\n' || c == '\r';
			if (isLineBreak) {
				if (seenLineBreak && !(c == breakQue)) {
					// This is a Windows or Mac line ending. Ignoring the second char
					seenLineBreak = false;
					continue;
				}
				else {
					// Linebreak character that we cannot ignore
					seenLineBreak = true;
					breakQue = c;
				}
			}
			else {
				// Reset linebreak state
				seenLineBreak = false;
			}

			// Character needs to be copied
			sb.setCharAt(t, isLineBreak ? '\n' : c);
			t++;
		}
		sb.setLength(t);

		return sb.toString();
	}

	/**
	 * Returns true, if the page is a disambiguation page, false otherwise.
	 *
	 * @return True, if the page is a disambiguation page, false otherwise.
	 */
	public boolean isDisambiguation()
	{
		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		boolean isDisambiguation = hibernatePage.getIsDisambiguation();
		session.getTransaction().commit();
		return isDisambiguation;
	}

	/**
	 * Returns true, if the page was returned by querying a redirect string, false otherwise.
	 *
	 * @return True, if the page was returned by querying a redirect string, false otherwise.
	 */
	public boolean isRedirect()
	{
		return isRedirect;
	}

    /**
     * @return True, if the page is a discussion page.
     * @throws WikiTitleParsingException
     */
    public boolean isDiscussion() throws WikiTitleParsingException
    {
        return getTitle().getRawTitleText().startsWith(DISCUSSION_PREFIX);
    }

    /**
	 * Returns the Wikipedia article as plain text. Page.getText() returns the Wikipedia article
	 * with all Wiki markup.
	 *
	 * @return The plain text of a Wikipedia article without all markup.
	 * @throws WikiApiException
	 */
	public String getPlainText()
		throws WikiApiException
	{
		ParsedPage pp = getParsedPage();
		if (pp == null) {
			return "";
		}
		return getTitle().getPlainTitle() + " " + pp.getText();
	}

	/**
	 * Returns a ParsedPage object as would have been returned by the JWPL Parser.
	 *
	 * @return A ParsedPage object as would have been returned by the JWPL Parser.
	 */
	public ParsedPage getParsedPage()
	{
		return wiki.getParser().parse(getText());
	}

	/**
	 * Note that this method only returns the anchors that are not equal to the page's title.
	 * Anchors might contain references to sections in an article in the form of "Page#Section".
	 * If you need the plain title, e.g. for checking whether the page exists in Wikipedia, the Title object can be used.
	 *
	 * @return A set of strings used as anchor texts in links pointing to that page.
	 * @throws WikiTitleParsingException
	 */
	public Set<String> getInlinkAnchors()
		throws WikiTitleParsingException
	{
		Set<String> inAnchors = new HashSet<String>();
		for (Page p : getInlinks()) {
			ParsedPage pp = p.getParsedPage();
			if (pp == null) {
				return inAnchors;
			}
			for (Link l : pp.getLinks()) {
				String pageTitle = p.getTitle().getPlainTitle();
				String anchorText = l.getText();
				if (l.getTarget().equals(pageTitle) && !anchorText.equals(pageTitle)) {
					inAnchors.add(anchorText);
				}
			}
		}
		return inAnchors;
	}

	/**
	 * Note that this method only returns the anchors that are not equal to the title of the page
	 * they are pointing to.
	 * Anchors might contain references to sections in an article in the form of "Page#Section".
	 * If you need the plain title, e.g. for checking whether the page exists in Wikipedia, the Title object can be used.
	 *
	 * @return A mapping from the page titles of links in that page to the anchor texts used in the
	 *         links.
	 * @throws WikiTitleParsingException
	 */
	public Map<String, Set<String>> getOutlinkAnchors()
		throws WikiTitleParsingException
	{
		Map<String, Set<String>> outAnchors = new HashMap<String, Set<String>>();
		ParsedPage pp = getParsedPage();
		if (pp == null) {
			return outAnchors;
		}
		for (Link l : pp.getLinks()) {
			if (l.getTarget().length() == 0) {
				continue;
			}

			String targetTitle = new Title(l.getTarget()).getPlainTitle();
			if (!l.getType().equals(Link.type.EXTERNAL) && !l.getType().equals(Link.type.IMAGE)
					&& !l.getType().equals(Link.type.AUDIO) && !l.getType().equals(Link.type.VIDEO)
					&& !targetTitle.contains(":")) // Wikipedia titles only contain colons if they
													// are categories or other meta data
			{
				String anchorText = l.getText();
				if (!anchorText.equals(targetTitle)) {
					Set<String> anchors;
					if (outAnchors.containsKey(targetTitle)) {
						anchors = outAnchors.get(targetTitle);
					}
					else {
						anchors = new HashSet<String>();
					}
					anchors.add(anchorText);
					outAnchors.put(targetTitle, anchors);
				}
			}
		}
		return outAnchors;
	}


	/**
	 * Returns a string with infos about this page object.
	 *
	 * @return A string with infos about this page object.
	 * @throws WikiApiException
	 */
	protected String getPageInfo()
		throws WikiApiException
	{
		StringBuffer sb = new StringBuffer(1000);

		sb.append("ID             : " + __getId() + LF);
		sb.append("PageID         : " + getPageId() + LF);
		sb.append("Name           : " + getTitle() + LF);
		sb.append("Disambiguation : " + isDisambiguation() + LF);
		sb.append("Redirects" + LF);
		for (String redirect : getRedirects()) {
			sb.append("  " + redirect + LF);
		}
		sb.append("Categories" + LF);
		for (Category category : getCategories()) {
			sb.append("  " + category.getTitle() + LF);
		}
		sb.append("In-Links" + LF);
		for (Page inLink : getInlinks()) {
			sb.append("  " + inLink.getTitle() + LF);
		}
		sb.append("Out-Links" + LF);
		for (Page outLink : getOutlinks()) {
			sb.append("  " + outLink.getTitle() + LF);
		}

		return sb.toString();
	}
}
