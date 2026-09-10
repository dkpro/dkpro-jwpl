/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.jwpl.api;

import java.util.HashSet;
import java.util.Set;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiPageNotFoundException;
import org.dkpro.jwpl.api.exception.WikiTitleParsingException;
import org.dkpro.jwpl.api.hibernate.PageDAO;
import org.dkpro.jwpl.api.sweble.PlainTextConverter;
import org.dkpro.jwpl.api.util.UnmodifiableArraySet;
import org.hibernate.LockOptions;
import org.hibernate.type.StandardBasicTypes;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

import de.fau.cs.osr.ptk.common.AstVisitor;

/**
 * Represents an article page as conceptually defined by Wikipedia.
 * Each page can link to other {@link Page pages} or be referenced to.
 * <p>
 * Moreover, a page can be contained in one or more {@link Category categories}.
 *
 * @see Category
 */
// Adapter class for hiding hibernate session management from the user.
public class Page
    implements WikiConstants
{

    /**
     * The title of the category that holds the hidden categories in English Wikipedia.
     */
    public static final String HIDDEN_CATEGORIES_TITLE = "Hidden_categories";

    private final Wikipedia wiki;

    private final PageDAO pageDAO;

    // The hibernatePage that is represented by this WikiAPI page.
    // The indirection is necessary to shield the user from Hibernate sessions.
    private org.dkpro.jwpl.api.hibernate.Page hibernatePage;

    // If we search for a redirect, the corresponding page is delivered transparently.
    // In that case, isRedirect is set to true, to indicate that.
    // Note: The page itself is _not_ a redirect, it is just a page.
    private boolean isRedirect = false;

    /**
     * Instantiates a {@link Page} object.
     *
     * @param wiki  A valid {@link Wikipedia} reference. Must not be {@code null}.
     * @param id    The hibernate id of the page.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    protected Page(Wikipedia wiki, long id) throws WikiApiException
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
     *             Thrown if errors occurred.
     */
    protected Page(Wikipedia wiki, int pageID) throws WikiApiException
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
     *             Thrown if errors occurred.
     */
    public Page(Wikipedia wiki, String pName) throws WikiApiException
    {
        this(wiki, pName, false);
    }

    /**
     * Creates a page object.
     *
     * @param wiki
     *            The wikipedia object.
     * @param pName
     *            The name of the page.
     * @param useExactTitle
     *            Whether to use the exact title or try to guess the correct wiki-style title.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public Page(Wikipedia wiki, String pName, boolean useExactTitle) throws WikiApiException
    {
        if (pName == null || pName.isEmpty()) {
            throw new WikiPageNotFoundException();
        }
        this.wiki = wiki;
        this.pageDAO = new PageDAO(wiki);
        Title pageTitle = new Title(pName);
        fetchByTitle(pageTitle, useExactTitle);
    }

    /**
     * Creates a Page object from an already retrieved hibernate Page
     *
     * @param wiki
     *            The wikipedia object.
     * @param id
     *            The hibernate id of the page.
     * @param hibernatePage
     *            The {@code api.hibernate.Page} that has already been retrieved
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    protected Page(Wikipedia wiki, long id, org.dkpro.jwpl.api.hibernate.Page hibernatePage)
        throws WikiApiException
    {
        this.wiki = wiki;
        this.pageDAO = new PageDAO(wiki);
        this.hibernatePage = hibernatePage;
    }

    /**
     * @throws WikiApiException
     *             Thrown if errors occurred.
     * @see Page
     */
    private void fetchByHibernateId(long id) throws WikiApiException
    {
        hibernatePage = wiki.__inTransaction(session -> pageDAO.findById(id));

        if (hibernatePage == null) {
            throw new WikiPageNotFoundException("No page with id " + id + " was found.");
        }
    }

    private void fetchByPageId(int pageID) throws WikiApiException
    {
        hibernatePage = wiki.__inTransaction(session -> session
                .createQuery("from Page where pageId = :id",
                        org.dkpro.jwpl.api.hibernate.Page.class)
                .setParameter("id", pageID, StandardBasicTypes.INTEGER).uniqueResult());

        if (hibernatePage == null) {
            throw new WikiPageNotFoundException("No page with page id " + pageID + " was found.");
        }
    }

    /**
     * CAUTION: Only returns 1 result, even if several results are possible.
     *
     * @param pTitle
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    private void fetchByTitle(Title pTitle, boolean useExactTitle) throws WikiApiException
    {
        final String searchString = useExactTitle ? pTitle.getPlainTitle()
                : pTitle.getWikiStyleTitle();

        String sql = "select pml.pageID from PageMapLine as pml where pml.name = :pagetitle LIMIT 1";
        Integer pageId = wiki.__inTransaction(
                session -> session.createNativeQuery(sql, Integer.class)
                        .setParameter("pagetitle", searchString, StandardBasicTypes.STRING)
                        .uniqueResult());

        if (pageId == null) {
            throw new WikiPageNotFoundException(
                    "No page with name " + searchString + " was found.");
        }
        fetchByPageId(pageId);
        if (!this.isRedirect && searchString != null
                && !searchString.equals(getTitle().getRawTitleText())) {
            if (this.isRedirect) {
                // in case we already tried to re-retrieve the discussion page unsuccessfully,
                // we have to give up here or we end up in an infinite loop.

                // reasons for this happening might be several entries in PageMapLine with the same
                // name but different upper/lower case variants
                // if the database does not allow case-sensitive queries, then the API will always
                // retrieve only the first result and if this is a redirect to a different writing
                // variant, we are stuck in a loop.
                // To fix this, either a case-sensitive collation should be used or the API should
                // be able to deal with set valued results and pick the correct one from the set.
                // For now, we gracefully return without retrieving the Talk page for this article
                // and throw an appropriate exception.
                throw new WikiPageNotFoundException("No discussion page with name " + searchString
                        + " could be retrieved. This is most likely due to multiple writing variants of the same page in the database");
            }
            else {
                this.isRedirect = true;
                /*
                 * WORKAROUND in our page is a redirect to a discussion page, we might not retrieve
                 * the target discussion page as expected but rather the article associated with the
                 * target discussion page we check this here and re-retrieve the correct page. this
                 * error should be avoided by keeping the namespace information in the database This
                 * fix has been provided by Shiri Dori-Hacohen and is discussed in the Google Group
                 * under https://groups.google.com/forum/#!topic/jwpl/2nlr55yp87I/discussion
                 */
                if (searchString.startsWith(DISCUSSION_PREFIX)
                        && !getTitle().getRawTitleText().startsWith(DISCUSSION_PREFIX)) {
                    try {
                        fetchByTitle(new Title(DISCUSSION_PREFIX + getTitle().getRawTitleText()),
                                useExactTitle);
                    }
                    catch (WikiPageNotFoundException e) {
                        throw new WikiPageNotFoundException("No page with name " + DISCUSSION_PREFIX
                                + getTitle().getRawTitleText() + " was found.", e);
                    }
                }
            }
        }
    }

    /**
     * @return Returns the id.
     */
    /*
     * Note well: Access is limited to package-private here intentionally, as the database ID is
     * considered framework-internal use.
     */
    long __getId()
    {
        return hibernatePage.getId();
    }

    /**
     * @return Returns a unique page id.
     */
    public int getPageId()
    {
        return hibernatePage.getPageId();
    }

    /**
     * @return A set of categories that this page belongs to.
     */
    public Set<Category> getCategories()
    {
        Set<Integer> tmp = wiki.__inTransaction(session -> {
            session.lock(hibernatePage, LockOptions.NONE);
            return new UnmodifiableArraySet<>(hibernatePage.getCategories());
        });

        Set<Category> categories = new HashSet<>();
        for (int pageID : tmp) {
            categories.add(wiki.getCategory(pageID));
        }

        return categories;
    }

    /**
     * This is a more efficient shortcut for writing {@link Page#getCategories()}.size, as that
     * would require to load all the categories first.
     *
     * @return The number of categories.
     */
    public int getNumberOfCategories()
    {
        int nrOfCategories = 0;

        long id = __getId();
        String sql = "select count(pages) from page_categories where id = :pageid";
        Long returnValue = wiki.__inTransaction(session -> session
                .createNativeQuery(sql, Long.class)
                .setParameter("pageid", id, StandardBasicTypes.LONG).uniqueResult());

        if (returnValue != null) {
            nrOfCategories = returnValue.intValue();
        }
        return nrOfCategories;
    }

    /**
     * Returns the categories of this page, leaving out the hidden categories of English Wikipedia,
     * i.e. the subcategories of {@value #HIDDEN_CATEGORIES_TITLE}. These are maintenance
     * categories, such as "All articles with dead external links", which Wikipedia does not show on
     * the page.
     * <p>
     * For other language editions, use {@link #getVisibleCategories(String)} with the title of that
     * edition's hidden-categories category.
     *
     * @return The categories of this page that are not hidden. If there is no
     *         {@value #HIDDEN_CATEGORIES_TITLE} category, all categories of this page.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public Set<Category> getVisibleCategories() throws WikiApiException
    {
        return getVisibleCategories(HIDDEN_CATEGORIES_TITLE);
    }

    /**
     * Returns the categories of this page, leaving out the hidden ones. A category is hidden if it
     * is a direct subcategory of the category with the given title.
     *
     * @param hiddenCategoriesTitle
     *            The title of the category that holds the hidden categories, e.g.
     *            {@value #HIDDEN_CATEGORIES_TITLE}. Must not be {@code null} or blank.
     * @return The categories of this page that are not hidden. If there is no category with the
     *         given title, all categories of this page.
     * @throws IllegalArgumentException
     *             Thrown if {@code hiddenCategoriesTitle} is {@code null} or blank.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public Set<Category> getVisibleCategories(String hiddenCategoriesTitle) throws WikiApiException
    {
        if (hiddenCategoriesTitle == null || hiddenCategoriesTitle.isBlank()) {
            throw new IllegalArgumentException(
                    "The title of the hidden categories category must not be null or blank.");
        }

        int hiddenCategoriesId;
        try {
            hiddenCategoriesId = wiki.getCategory(hiddenCategoriesTitle).getPageId();
        }
        catch (WikiPageNotFoundException e) {
            // Without a hidden categories category, none of the categories of this page is hidden.
            return getCategories();
        }

        Set<Category> visibleCategories = new HashSet<>();
        for (Category category : getCategories()) {
            if (!category.getParentIDs().contains(hiddenCategoriesId)) {
                visibleCategories.add(category);
            }
        }
        return visibleCategories;
    }

    /**
     * Returns the set of pages that have a link pointing to this page.
     * <p>
     * <b>Warning:</b> Do not use this for getting the number of inlinks with
     * {@link Page#getInlinks()}.size(). This is too slow. Use {@link Page#getNumberOfInlinks()}
     * instead.
     *
     * @return The set of pages that have a link pointing to this page.
     */
    public Set<Page> getInlinks()
    {
        // Have to copy links here since getPage later will close the session.
        Set<Integer> pageIDs = wiki.__inTransaction(session -> {
            session.lock(hibernatePage, LockOptions.NONE);
            return new UnmodifiableArraySet<>(hibernatePage.getInLinks());
        });

        Set<Page> pages = new HashSet<>();
        for (int pageID : pageIDs) {
            try {
                pages.add(wiki.getPage(pageID));
            }
            catch (WikiApiException e) {
                // Silently ignore if a page could not be found
                // There may be inlinks that do not come from an existing page.
            }
        }

        return pages;
    }

    /**
     * This is a more efficient shortcut for writing {@link Page#getInlinks()}.size(), as that would
     * require to load all the inlinks first.
     *
     * @return The number of inlinks.
     */
    public int getNumberOfInlinks()
    {
        int nrOfInlinks = 0;

        long id = __getId();
        String sql = "select count(pi.inLinks) from page_inlinks as pi where pi.id = :piid";
        Long returnValue = wiki.__inTransaction(session -> session
                .createNativeQuery(sql, Long.class)
                .setParameter("piid", id, StandardBasicTypes.LONG).uniqueResult());

        if (returnValue != null) {
            nrOfInlinks = returnValue.intValue();
        }
        return nrOfInlinks;
    }

    /**
     * The result set may also contain links from non-existing pages. It is in the responsibility of
     * the user to check whether the page exists.
     *
     * @return Returns the IDs of the inLinks of this page.
     */
    public Set<Integer> getInlinkIDs()
    {
        return wiki.__inTransaction(session -> {
            session.lock(hibernatePage, LockOptions.NONE);
            return new HashSet<>(hibernatePage.getInLinks());
        });
    }

    /**
     * Returns the titles of the pages that have a link pointing to this page.
     * <p>
     * This is a more efficient shortcut for collecting {@link Page#getTitle()} over
     * {@link Page#getInlinks()}, as that would load every linking page - article text included -
     * just to read its title. As with {@link Page#getInlinks()}, inlinks that do not come from an
     * existing page are not part of the result.
     *
     * @return The titles of the pages that have a link pointing to this page.
     */
    public Set<Title> getInlinkTitles()
    {
        return new HashSet<>(wiki.getTitles(getInlinkIDs()).values());
    }

    /**
     * Returns the set of pages that are linked from this page. Outlinks in a page might also point
     * to non-existing pages. They are not included in the result set. <b>Warning:</b> Do not use
     * this for getting the number of outlinks with {@link Page#getOutlinks()}.size(). This is too
     * slow. Use {@link Page#getNumberOfOutlinks()} instead.
     *
     * @return The set of pages that are linked from this page.
     */
    public Set<Page> getOutlinks()
    {
        // Have to copy links here since getPage later will close the session.
        Set<Integer> tmpSet = wiki.__inTransaction(session -> {
            session.lock(hibernatePage, LockOptions.NONE);
            return new UnmodifiableArraySet<>(hibernatePage.getOutLinks());
        });

        Set<Page> pages = new HashSet<>();
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
     * This is a more efficient shortcut for writing {@link Page#getOutlinks()}.size(), as that
     * would require to load all the outlinks first.
     *
     * @return The number of outlinks.
     */
    public int getNumberOfOutlinks()
    {
        int nrOfOutlinks = 0;

        long id = __getId();
        String sql = "select count(outLinks) from page_outlinks where id = :id";
        Long returnValue = wiki.__inTransaction(session -> session
                .createNativeQuery(sql, Long.class)
                .setParameter("id", id, StandardBasicTypes.LONG).uniqueResult());

        if (returnValue != null) {
            nrOfOutlinks = returnValue.intValue();
        }
        return nrOfOutlinks;
    }

    /**
     * The result set may also contain links from non-existing pages. It is in the responsibility of
     * the user to check whether the page exists.
     *
     * @return Returns the IDs of the outLinks of this page.
     */
    public Set<Integer> getOutlinkIDs()
    {

        return wiki.__inTransaction(session -> {
            session.lock(hibernatePage, LockOptions.NONE);
            return new HashSet<>(hibernatePage.getOutLinks());
        });
    }

    /**
     * Returns the titles of the pages that are linked from this page.
     * <p>
     * This is a more efficient shortcut for collecting {@link Page#getTitle()} over
     * {@link Page#getOutlinks()}, as that would load every linked page - article text included -
     * just to read its title. As with {@link Page#getOutlinks()}, outlinks pointing to
     * non-existing pages are not part of the result.
     *
     * @return The titles of the pages that are linked from this page.
     */
    public Set<Title> getOutlinkTitles()
    {
        return new HashSet<>(wiki.getTitles(getOutlinkIDs()).values());
    }

    /**
     * @return The title of the page.
     * @throws WikiTitleParsingException
     *             Thrown if errors occurred while parsing.
     */
    public Title getTitle() throws WikiTitleParsingException
    {
        String name = wiki.__inTransaction(session -> hibernatePage.getName());
        return new Title(name);
    }

    /**
     * @return The set of strings that are redirects to this page.
     */
    public Set<String> getRedirects()
    {
        return wiki.__inTransaction(session -> {
            session.lock(hibernatePage, LockOptions.NONE);
            return new HashSet<>(hibernatePage.getRedirects());
        });
    }

    /**
     * @return The text of the page with media wiki markup.
     */
    public String getText()
    {
        String text = wiki.__inTransaction(session -> hibernatePage.getText());

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
     * @return {@code True}, if the page is a disambiguation page, {@code false} otherwise.
     */
    public boolean isDisambiguation()
    {
        return wiki.__inTransaction(session -> hibernatePage.getIsDisambiguation());
    }

    /**
     * @return {@code True}, if the page was returned by querying a redirect string, {@code false}
     *         otherwise.
     */
    public boolean isRedirect()
    {
        return isRedirect;
    }

    /**
     * @return {@code True}, if the page is a discussion page, {@code false} otherwise.
     * @throws WikiTitleParsingException
     *             Thrown if errors occurred.
     */
    public boolean isDiscussion() throws WikiTitleParsingException
    {
        return getTitle().getRawTitleText().startsWith(DISCUSSION_PREFIX);
    }

    /**
     * <p>
     * Returns the Wikipedia article as plain text using the SwebleParser with a
     * SimpleWikiConfiguration and the PlainTextConverter. <br>
     * If you have different needs regarding the plain text, you can use getParsedPage(Visitor v)
     * and provide your own Sweble-Visitor. Examples are in the {@code org.dkpro.jwpl.api.sweble}
     * package or on <a href="https://www.sweble.org">http://www.sweble.org</a>
     * </p>
     *
     * <p>
     * Alternatively, use {@link Page#getText()} to return the Wikipedia article with all Wiki
     * markup. You can then use the old JWPL MediaWiki parser for creating a plain text version. The
     * JWPL parser is now located in a separate project {@code org.dkpro.jwpl.api.parser}. Please
     * refer to the JWPL Google Code project page for further reference.
     * </p>
     *
     * @return The plain text of a Wikipedia article
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public String getPlainText() throws WikiApiException
    {
        // Configure the PlainTextConverter for plain text parsing
        return (String) parsePage(
                new PlainTextConverter(this.wiki.getWikConfig(), false, Integer.MAX_VALUE));
    }

    /**
     * Parses the page with the Sweble parser using a SimpleWikiConfiguration and the provided
     * visitor. For further information about the visitor concept, look at the examples in the
     * {@code org.dkpro.jwpl.api.sweble} package, or on
     * <a href="https://www.sweble.org">https://www.sweble.org</a> or on the JWPL Google Code
     * project page.
     *
     * @return the parsed page. The actual return type depends on the provided visitor. You have to
     *         cast the return type according to the return type of the go() method of your visitor.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    private Object parsePage(AstVisitor v) throws WikiApiException
    {
        // Use the provided visitor to parse the page
        return v.go(getCompiledPage().getPage());
    }

    /**
     * Returns CompiledPage produced by the SWEBLE parser using the SimpleWikiConfiguration.
     *
     * @return the parsed page
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    private EngProcessedPage getCompiledPage() throws WikiApiException
    {
        EngProcessedPage cp;
        try {
            WtEngineImpl engine = new WtEngineImpl(this.wiki.getWikConfig());

            PageTitle pageTitle = PageTitle.make(this.wiki.getWikConfig(),
                    this.getTitle().toString());
            PageId pageId = new PageId(pageTitle, -1);

            // Compile the retrieved page
            cp = engine.postprocess(pageId, this.getText(), null);
        }
        catch (Exception e) {
            throw new WikiApiException(e);
        }
        return cp;
    }

    ///////////////////////////////////////////////////////////////////
    /*
     * The methods getInlinkAnchors() and getOutLinkAnchors() have not yet been migrated to the
     * SWEBLE parser. The original versions based on the JWPL MediaWikiParser can be found in
     * org.dkpro.jwpl.parser.LinkAnchorExtractor
     */
    ///////////////////////////////////////////////////////////////////

    /**
     * @return A string with infos about this page object.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    protected String getPageInfo() throws WikiApiException
    {
        StringBuilder sb = new StringBuilder(1000);

        sb.append("ID             : ").append(__getId()).append(LF);
        sb.append("PageID         : ").append(getPageId()).append(LF);
        sb.append("Name           : ").append(getTitle()).append(LF);
        sb.append("Disambiguation : ").append(isDisambiguation()).append(LF);
        sb.append("Redirects").append(LF);
        for (String redirect : getRedirects()) {
            sb.append("  ").append(redirect).append(LF);
        }
        sb.append("Categories").append(LF);
        for (Category category : getCategories()) {
            sb.append("  ").append(category.getTitle()).append(LF);
        }
        sb.append("In-Links").append(LF);
        for (Page inLink : getInlinks()) {
            sb.append("  ").append(inLink.getTitle()).append(LF);
        }
        sb.append("Out-Links").append(LF);
        for (Page outLink : getOutlinks()) {
            sb.append("  ").append(outLink.getTitle()).append(LF);
        }

        return sb.toString();
    }
}
