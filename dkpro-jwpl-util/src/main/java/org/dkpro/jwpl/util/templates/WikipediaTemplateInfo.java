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
package org.dkpro.jwpl.util.templates;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
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

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.Page;
import org.dkpro.jwpl.api.Wikipedia;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiPageNotFoundException;
import org.dkpro.jwpl.api.util.StringUtils;
import org.dkpro.jwpl.parser.ParsedPage;
import org.dkpro.jwpl.parser.Template;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.dkpro.jwpl.parser.mediawiki.ShowTemplateNamesAndParameters;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionApi;
import org.dkpro.jwpl.util.templates.RevisionPair.RevisionPairType;
import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gives access to the additional information created by the TemplateInfoGenerator.
 */
public class WikipediaTemplateInfo
{

    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private final Wikipedia wiki;
    private RevisionApi revApi = null;
    private MediaWikiParser parser = null;

    private Connection connection;

    public WikipediaTemplateInfo(Wikipedia pWiki) throws SQLException, WikiApiException
    {
        this.wiki = pWiki;
        this.connection = getConnection(wiki);

        if (!tableExists(GeneratorConstants.TABLE_TPLID_TPLNAME)) {
            System.err.println(
                    "No Template Database could be found. You can only use methods that work without a template index");
        }
    }

    /**
     * Returns the number of all pages that contain a template the name of which starts with any of
     * the given Strings.
     *
     * @param templateFragments
     *            a list Strings containing the beginnings of the desired templates
     * @param whitelist
     *            whether to return pages containing these templates (true) or return pages NOT
     *            containing these templates (false)
     * @return the number of pages that contain any template starting with templateFragment
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the template
     *             templates are corrupted)
     */
    private Integer countFragmentFilteredPages(List<String> templateFragments, boolean whitelist)
        throws WikiApiException
    {
        return countIndexedPages(templateFragments, true, whitelist);
    }

    /**
     * Returns the number of all pages that contain a template the name of which starts with any of
     * the given Strings.
     *
     * <p>
     * Each page is counted once, however many of its templates match.
     *
     * @param templateFragments
     *            a list Strings containing the beginnings of the desired templates
     * @return the number of pages that contain any template starting with templateFragment
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the template
     *             templates are corrupted)
     */
    public Integer countPagesContainingTemplateFragments(List<String> templateFragments)
        throws WikiApiException
    {
        return countFragmentFilteredPages(templateFragments, true);
    }

    /**
     * Returns the number of all pages that do not contain a template the name of which starts with
     * any of the given Strings.
     *
     * <p>
     * The page template index is evaluated per page: only pages that contain at least one
     * template are considered, and a page is not counted if any of its templates matches.
     *
     * @param templateFragments
     *            a list Strings containing the beginnings of the desired templates
     * @return the number of pages that do not contain any template starting with templateFragment
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the template
     *             templates are corrupted)
     */
    public Integer countPagesNotContainingTemplateFragments(List<String> templateFragments)
        throws WikiApiException
    {
        return countFragmentFilteredPages(templateFragments, false);
    }

    /**
     * Returns the number of all pages that contain a template the name of which equals the given
     * String.
     *
     * @param templateNames
     *            a list of String containing the beginnings of the templates that have to be
     *            matched
     * @param whitelist
     *            whether to return pages containing these templates (true) or return pages NOT
     *            containing these templates (false)
     * @return the number of pages that contain a template starting with any templateFragment
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    private Integer countFilteredPages(List<String> templateNames, boolean whitelist)
        throws WikiApiException
    {
        return countIndexedPages(templateNames, false, whitelist);
    }

    /**
     * Returns the number of all pages that contain a template the name of which equals the given
     * String.
     *
     * <p>
     * Each page is counted once, however many of its templates match.
     *
     * @param templateNames
     *            a list of String containing the beginnings of the templates that have to be
     *            matched
     * @return the number of pages that contain a template starting with any templateFragment
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public Integer countPagesContainingTemplateNames(List<String> templateNames)
        throws WikiApiException
    {
        return countFilteredPages(templateNames, true);
    }

    /**
     * Returns the number of all pages that do not contain a template the name of which equals the
     * given String.
     *
     * <p>
     * The page template index is evaluated per page: only pages that contain at least one
     * template are considered, and a page is not counted if any of its templates matches.
     *
     * @param templateNames
     *            a list of String containing the beginnings of the templates that have to be
     *            matched
     * @return the number of pages that do not contain a template starting with any templateFragment
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public Integer countPagesNotContainingTemplateNames(List<String> templateNames)
        throws WikiApiException
    {
        return countFilteredPages(templateNames, false);
    }

    /**
     * Return an iterable containing all pages that contain a template the name of which starts with
     * any of the given Strings.
     *
     * @param templateFragments
     *            the beginning of the templates that have to be matched
     * @param whitelist
     *            whether to return pages containing these templates (true) or return pages NOT
     *            containing these templates (false)
     * @return An iterable with the page objects that contain templates beginning with any String in
     *         templateFragments
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    private Iterable<Page> getFragmentFilteredPages(List<String> templateFragments,
            boolean whitelist)
        throws WikiApiException
    {
        return loadPages(selectIndexedPageIds(templateFragments, true, whitelist));
    }

    /**
     * Returns the id of the template with the given name.
     * <p>
     * As in previous versions, the name is expected in the SQL escaped form produced by
     * {@link StringUtils#sqlEscape(String)}. Leading and trailing whitespace is removed and blanks
     * are replaced by underscores, then the name is unescaped and bound as a statement parameter.
     * If several templates share the name, the smallest id is returned.
     *
     * @param templateName
     *            the SQL escaped name of the template
     * @return the id of the template or {@code -1} if no template with that name exists
     * @throws WikiApiException
     *             If there was any error retrieving the id from the database
     */
    public int checkTemplateId(String templateName) throws WikiApiException
    {
        try {
            String sqlString = "SELECT tpl.templateId FROM " + GeneratorConstants.TABLE_TPLID_TPLNAME
                    + " AS tpl WHERE tpl.templateName = ? ORDER BY tpl.templateId LIMIT 1";

            try (PreparedStatement statement = connection.prepareStatement(sqlString)) {
                statement.setString(1, sqlUnescape(templateName.trim().replace(' ', '_')));
                ResultSet result = execute(statement);

                if (result == null) {
                    return -1;
                }

                if (result.next()) {
                    return result.getInt(1);
                }
            }

            return -1;
        }
        catch (Exception e) {
            throw new WikiApiException(e);
        }
    }

    /**
     * Reverts {@link StringUtils#sqlEscape(String)}, following the rules MySQL applies to escape
     * sequences in string literals: {@code \0}, {@code \b}, {@code \n}, {@code \r},
     * {@code \t} and {@code \Z} denote control characters, a backslash before any other
     * character denotes that character, and a doubled single quote denotes a single quote. This
     * keeps names that callers escaped for the formerly concatenated query working with a bound
     * parameter.
     *
     * @param escaped
     *            an SQL escaped string, must not be {@code null}
     * @return the unescaped string
     */
    static String sqlUnescape(String escaped)
    {
        StringBuilder unescaped = new StringBuilder(escaped.length());
        for (int i = 0; i < escaped.length(); i++) {
            char c = escaped.charAt(i);
            if (c == '\\' && i + 1 < escaped.length()) {
                char next = escaped.charAt(++i);
                switch (next) {
                case '0':
                    unescaped.append('\u0000');
                    break;
                case 'b':
                    unescaped.append('\b');
                    break;
                case 'n':
                    unescaped.append('\n');
                    break;
                case 'r':
                    unescaped.append('\r');
                    break;
                case 't':
                    unescaped.append('\t');
                    break;
                case 'Z':
                    unescaped.append('\u001a');
                    break;
                default:
                    unescaped.append(next);
                    break;
                }
            }
            else if (c == '\'' && i + 1 < escaped.length() && escaped.charAt(i + 1) == '\'') {
                unescaped.append('\'');
                i++;
            }
            else {
                unescaped.append(c);
            }
        }
        return unescaped.toString();
    }

    /**
     * Loads the ids of all templates with a single query. This allows resolving many template
     * names, as collected by the template info generator, without one query per name.
     *
     * @return the ids of all templates, to be looked up by their SQL escaped names
     * @throws WikiApiException
     *             If there was any error retrieving the ids from the database
     */
    public TemplateIds loadTemplateIds() throws WikiApiException
    {
        try {
            return loadTemplateIds(connection);
        }
        catch (SQLException e) {
            throw new WikiApiException(e);
        }
    }

    static TemplateIds loadTemplateIds(Connection connection) throws SQLException
    {
        Map<String, Integer> ids = new HashMap<>();
        String sqlString = "SELECT templateId, templateName FROM "
                + GeneratorConstants.TABLE_TPLID_TPLNAME;
        try (PreparedStatement statement = connection.prepareStatement(sqlString,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            try {
                // Lets MySQL Connector/J (and the MariaDB driver) stream the rows instead of
                // buffering the whole table on the client
                statement.setFetchSize(Integer.MIN_VALUE);
            }
            catch (SQLException e) {
                logger.debug("Row streaming is not supported by the JDBC driver.", e);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int id = result.getInt(1);
                    String key = normalizeTemplateName(StringUtils.sqlEscape(result.getString(2)));
                    ids.merge(key, id, Math::min);
                }
            }
        }
        return new TemplateIds(ids);
    }

    /**
     * Normalizes an SQL escaped template name for looking it up in {@link TemplateIds}. It applies
     * the normalization of {@link #checkTemplateId(String)} (trimming, blanks replaced by
     * underscores) and mimics the default case-insensitive MySQL collation by lower casing the
     * name.
     *
     * @param escapedTemplateName
     *            a template name escaped via {@link StringUtils#sqlEscape(String)}
     * @return the normalized template name
     */
    private static String normalizeTemplateName(String escapedTemplateName)
    {
        return escapedTemplateName.trim().replace(' ', '_').toLowerCase();
    }

    /**
     * The ids of all templates as loaded by {@link WikipediaTemplateInfo#loadTemplateIds()}.
     * Template names are normalized for the lookup the same way as by
     * {@link WikipediaTemplateInfo#checkTemplateId(String)}, but compared case-insensitively like
     * the default MySQL collation does. If several templates share a normalized name, the smallest
     * id is kept.
     */
    public static final class TemplateIds
    {

        private final Map<String, Integer> idsByNormalizedName;

        private TemplateIds(Map<String, Integer> idsByNormalizedName)
        {
            this.idsByNormalizedName = idsByNormalizedName;
        }

        /**
         * Returns the id of the template with the given name.
         *
         * @param escapedTemplateName
         *            the template name escaped via {@link StringUtils#sqlEscape(String)}
         * @return the id of the template or {@code -1} if no template with that name exists
         */
        public int getTemplateId(String escapedTemplateName)
        {
            return idsByNormalizedName.getOrDefault(normalizeTemplateName(escapedTemplateName), -1);
        }

        /**
         * @return the number of distinct normalized template names
         */
        int size()
        {
            return idsByNormalizedName.size();
        }
    }

    /**
     * Return an iterable containing all pages that contain a template the name of which starts with
     * any of the given Strings.
     *
     * <p>
     * Each page is returned once, ordered by page id.
     * <p>
     * All matching pages are loaded, including their text, before this method returns. Use
     * {@link #getPageIdsContainingTemplateFragments(List)} if the page ids are sufficient.
     *
     * @param templateFragments
     *            the beginning of the templates that have to be matched
     * @return An iterable with the page objects that contain templates beginning with any String in
     *         templateFragments
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public Iterable<Page> getPagesContainingTemplateFragments(List<String> templateFragments)
        throws WikiApiException
    {
        return getFragmentFilteredPages(templateFragments, true);
    }

    /**
     * Return an iterable containing all pages that do not contain a template the name of which
     * starts with any of the given Strings.
     *
     * <p>
     * Each page is returned once, ordered by page id.
     * <p>
     * The page template index is evaluated per page: only pages that contain at least one
     * template are considered, and a page is not returned if any of its templates matches.
     * <p>
     * All matching pages are loaded, including their text, before this method returns. Use
     * {@link #getPageIdsNotContainingTemplateFragments(List)} if the page ids are sufficient.
     *
     * @param templateFragments
     *            the beginning of the templates that have to be matched
     * @return An iterable with the page objects that do not contain templates beginning with any
     *         String in templateFragments
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public Iterable<Page> getPagesNotContainingTemplateFragments(List<String> templateFragments)
        throws WikiApiException
    {
        return getFragmentFilteredPages(templateFragments, false);
    }

    /**
     * Return an iterable containing all pages that contain a template the name of which starts with
     * any of the given Strings.
     *
     * @param templateNames
     *            the names of the template that we want to match
     * @param whitelist
     *            whether to return pages containing these templates (true) or return pages NOT
     *            containing these templates (false)
     * @return An iterable with the page objects that contain any of the specified templates
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    private Iterable<Page> getFilteredPages(List<String> templateNames, boolean whitelist)
        throws WikiApiException
    {
        return loadPages(selectIndexedPageIds(templateNames, false, whitelist));
    }

    /**
     * Return an iterable containing all pages that contain a template the name of which equals any
     * of the given Strings.
     *
     * <p>
     * Each page is returned once, ordered by page id.
     * <p>
     * All matching pages are loaded, including their text, before this method returns. Use
     * {@link #getPageIdsContainingTemplateNames(List)} if the page ids are sufficient.
     *
     * @param templateNames
     *            the names of the template that we want to match
     * @return An iterable with the page objects that contain any of the specified templates
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public Iterable<Page> getPagesContainingTemplateNames(List<String> templateNames)
        throws WikiApiException
    {
        return getFilteredPages(templateNames, true);
    }

    /**
     * Return an iterable containing all pages that do NOT contain a template the name of which
     * equals of the given Strings.
     *
     * <p>
     * Each page is returned once, ordered by page id.
     * <p>
     * The page template index is evaluated per page: only pages that contain at least one
     * template are considered, and a page is not returned if any of its templates matches.
     * <p>
     * All matching pages are loaded, including their text, before this method returns. Use
     * {@link #getPageIdsNotContainingTemplateNames(List)} if the page ids are sufficient.
     *
     * @param templateNames
     *            the names of the template that we want to match
     * @return An iterable with the page objects that do NOT contain any of the specified
     *         templates
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public Iterable<Page> getPagesNotContainingTemplateNames(List<String> templateNames)
        throws WikiApiException
    {
        return getFilteredPages(templateNames, false);
    }

    /**
     * This method first creates a list of pages containing templates that equal any of the provided
     * Strings. It then returns a list of revision ids of the revisions in which the respective
     * templates first appeared.
     *
     * @param templateName
     *            the template names that have to be matched
     * @return A list with the revision ids of the first appearance of the template
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public List<Integer> getRevisionsWithFirstTemplateAppearance(String templateName)
        throws WikiApiException
    {
        /*
         * Note: This method does not use any revision-template-index. Each revision has to be
         * parsed until the first revision is found that does not contain a certain template. TODO
         * also create version using revision-template index
         */
        System.err
                .println("Note: This function call demands parsing several revision for each page. "
                        + "A method using the revision-template index is currently under construction.");

        templateName = templateName.trim().replaceAll(" ", "_");

        List<Integer> revisionIds = new LinkedList<>();
        List<Integer> pageIds = getPageIdsContainingTemplateNames(List.of(templateName));
        if (pageIds.isEmpty()) {
            return revisionIds;
        }
        if (revApi == null) {
            revApi = new RevisionApi(wiki.getDatabaseConfiguration());
        }
        if (parser == null) {
            // TODO switch to SWEBLE
            MediaWikiParserFactory pf = new MediaWikiParserFactory(
                    wiki.getDatabaseConfiguration().getLanguage());
            pf.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
            parser = pf.createParser();
        }

        for (int id : pageIds) {
            // get timestamps of all revisions
            List<Timestamp> tsList = revApi.getRevisionTimestamps(id);

            // sort in reverse order - newest first
            tsList.sort(Comparator.reverseOrder());

            Revision prevRev = null;
            tsloop: for (Timestamp ts : tsList) {

                Revision rev = revApi.getRevision(id, ts);

                // initialize previous revision
                if (prevRev == null) {
                    prevRev = rev;
                }

                // Parse templates and check if the revision contains the template
                ParsedPage pp = parser.parse(rev.getRevisionText());
                boolean containsTpl = false;
                tplLoop: for (Template tpl : pp.getTemplates()) {
                    if (tpl.getName().equalsIgnoreCase(templateName)) {
                        containsTpl = true;
                        break tplLoop;
                    }
                }

                // if the revision does not contain the template, we have found
                // what we were looking for. add id of previous revision
                if (!containsTpl) {
                    revisionIds.add(prevRev.getRevisionID());
                    break tsloop;
                }
                prevRev = rev;
            }
        }

        return revisionIds;
    }

    //////////

    /**
     * Returns a list containing the ids of all pages that contain a template the name of which
     * starts with any of the given Strings.
     *
     * @param templateFragments
     *            the beginning of the templates that have to be matched
     * @param whitelist
     *            whether to return pages containing these templates (true) or return pages NOT
     *            containing these templates (false)
     * @return A list with the ids of the pages that contain templates beginning with any String in
     *         templateFragments
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    private List<Integer> getFragmentFilteredPageIds(List<String> templateFragments,
            boolean whitelist)
        throws WikiApiException
    {
        return selectIndexedPageIds(templateFragments, true, whitelist);
    }

    /**
     * Returns a list containing the ids of all pages that contain a template the name of which
     * starts with any of the given Strings.
     *
     * <p>
     * Each page is returned once, ordered by page id.
     *
     * @param templateFragments
     *            the beginning of the templates that have to be matched
     * @return A list with the ids of the pages that contain templates beginning with any String in
     *         templateFragments
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the template
     *             templates are corrupted)
     */
    public List<Integer> getPageIdsContainingTemplateFragments(List<String> templateFragments)
        throws WikiApiException
    {
        return getFragmentFilteredPageIds(templateFragments, true);
    }

    /**
     * Returns a list containing the ids of all pages that do not contain a template the name of
     * which starts with any of the given Strings.
     *
     * <p>
     * Each page is returned once, ordered by page id.
     * <p>
     * The page template index is evaluated per page: only pages that contain at least one
     * template are considered, and a page is not returned if any of its templates matches.
     *
     * @param templateFragments
     *            the beginning of the templates that have to be matched
     * @return A list with the ids of the pages that do not contain templates beginning with any
     *         String in templateFragments
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the template
     *             templates are corrupted)
     */
    public List<Integer> getPageIdsNotContainingTemplateFragments(List<String> templateFragments)
        throws WikiApiException
    {
        return getFragmentFilteredPageIds(templateFragments, false);
    }

    ///////////////////

    /**
     * Returns a list containing the ids of all revisions that contain a template the name of which
     * starts with any of the given Strings.
     *
     * @param templateFragments
     *            the beginning of the templates that have to be matched
     * @param whitelist
     *            whether to return pages containing these templates (true) or return pages NOT
     *            containing these templates (false)
     * @return A list with the ids of the revisions that contain templates beginning with any
     *         String in templateFragments
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    private List<Integer> getFragmentFilteredRevisionIds(List<String> templateFragments,
            boolean whitelist)
        throws WikiApiException
    {

        List<Integer> matchedPages = new LinkedList<>();
        try {
            String sqlString = "SELECT r.revisionId FROM " + GeneratorConstants.TABLE_TPLID_TPLNAME
                    + " AS tpl, " + GeneratorConstants.TABLE_TPLID_REVISIONID
                    + " AS r WHERE tpl.templateId = r.templateId "
                    + (whitelist ? "AND " : "AND NOT ")
                    + buildTemplateNameCondition(templateFragments.size(), true);

            try (PreparedStatement statement = connection.prepareStatement(sqlString)) {

                bindTemplateNames(statement, templateFragments, true);

                ResultSet result = execute(statement);

                if (result == null) {
                    throw new WikiPageNotFoundException("Nothing was found");
                }

                while (result.next()) {
                    matchedPages.add(result.getInt(1));
                }
            }

            return matchedPages;
        }
        catch (Exception e) {
            throw new WikiApiException(e);
        }
    }

    /**
     * Returns a list containing the ids of all revisions that contain a template the name of which
     * starts with any of the given Strings.
     *
     * @param templateFragments
     *            the beginning of the templates that have to be matched
     * @return A list with the ids of the revisions that contain templates beginning with any
     *         String in templateFragments
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the template
     *             templates are corrupted)
     */
    public List<Integer> getRevisionIdsContainingTemplateFragments(List<String> templateFragments)
        throws WikiApiException
    {
        return getFragmentFilteredRevisionIds(templateFragments, true);
    }

    /**
     * Returns a list containing the ids of all revisions that contain a template the name of which
     * starts with any of the given Strings.
     *
     * @param templateFragments
     *            the beginning of the templates that have to be matched
     * @return A list with the ids of the revisions that do not contain templates beginning with
     *         any String in templateFragments
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the template
     *             templates are corrupted)
     */
    public List<Integer> getRevisionIdsNotContainingTemplateFragments(
            List<String> templateFragments)
        throws WikiApiException
    {
        return getFragmentFilteredRevisionIds(templateFragments, false);
    }

    ///////////////////

    /**
     * Returns the ids of all pages that ever contained any of the given template names in the
     * history of their existence.
     * <p>
     * Revisions listed in the template index that cannot be found in the revision tables are
     * ignored.
     *
     * @param templateNames
     *            template names to look for
     * @return list of page ids of the pages that once contained any of the given template names
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the template
     *             templates are corrupted)
     */
    public List<Integer> getIdsOfPagesThatEverContainedTemplateNames(List<String> templateNames)
        throws WikiApiException
    {
        return getIdsOfPagesWithRevisionsMatching(templateNames, false);
    }

    /**
     * Returns the ids of all pages that ever contained any template that started with any of the
     * given template fragments.
     * <p>
     * Revisions listed in the template index that cannot be found in the revision tables are
     * ignored.
     *
     * @param templateFragments
     *            template-fragments to look for
     * @return list of page ids of the pages that once contained any template that started with any
     *         of the given template fragments
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the template
     *             templates are corrupted)
     */
    public List<Integer> getIdsOfPagesThatEverContainedTemplateFragments(
            List<String> templateFragments)
        throws WikiApiException
    {
        return getIdsOfPagesWithRevisionsMatching(templateFragments, true);
    }

    /**
     * Returns the ids of all pages that have at least one revision containing a template whose
     * name equals (or, if {@code prefix} is set, starts with) any of the given Strings. The
     * revision ids are resolved to page ids with a single join in the database.
     * <p>
     * Revisions listed in the template index that cannot be found in the revision tables are
     * ignored.
     *
     * @param templateNames
     *            template names or template name fragments to look for
     * @param prefix
     *            whether the given Strings are matched as name prefixes (true) or as full names
     *            (false)
     * @return list of the distinct ids of the matching pages
     * @throws WikiApiException
     *             If there was any error retrieving the page ids
     */
    private List<Integer> getIdsOfPagesWithRevisionsMatching(List<String> templateNames,
            boolean prefix)
        throws WikiApiException
    {
        List<Integer> pageIds = new LinkedList<>();
        if (templateNames == null || templateNames.isEmpty()) {
            return pageIds;
        }

        try (PreparedStatement statement = connection
                .prepareStatement(buildPageIdQuery(templateNames.size(), prefix))) {
            bindTemplateNames(statement, templateNames, prefix);

            try (ResultSet result = execute(statement)) {
                while (result.next()) {
                    pageIds.add(result.getInt(1));
                }
            }
        }
        catch (SQLException e) {
            throw new WikiApiException(e);
        }
        return pageIds;
    }

    /**
     * Builds the query that selects the distinct ids of all pages which have at least one revision
     * containing any of the given templates. It joins the template index with the RevisionMachine
     * tables {@code index_revisionID} and {@code revisions}.
     *
     * @param nameCount
     *            the number of template names (or fragments) the query has to match
     * @param prefix
     *            whether the template names are matched as prefixes (true) or as full names
     *            (false)
     * @return the SQL query with one parameter per template name
     */
    static String buildPageIdQuery(int nameCount, boolean prefix)
    {
        return "SELECT DISTINCT rev.ArticleID FROM " + GeneratorConstants.TABLE_TPLID_TPLNAME
                + " AS tpl JOIN " + GeneratorConstants.TABLE_TPLID_REVISIONID
                + " AS tr ON tr.templateId = tpl.templateId"
                + " JOIN index_revisionID AS idx ON idx.RevisionID = tr.revisionId"
                + " JOIN revisions AS rev ON rev.PrimaryKey = idx.RevisionPK WHERE "
                + buildTemplateNameCondition(nameCount, prefix);
    }

    /**
     * Builds a parenthesized condition that matches {@code tpl.templateName} against the given
     * number of parameters, joined by {@code OR}.
     *
     * @param nameCount
     *            the number of template names (or fragments) to match
     * @param prefix
     *            whether to match with {@code LIKE} (true) or with {@code =} (false)
     * @return the condition
     */
    static String buildTemplateNameCondition(int nameCount, boolean prefix)
    {
        String single = prefix ? "tpl.templateName LIKE ?" : "tpl.templateName = ?";
        StringBuilder condition = new StringBuilder("(");
        for (int i = 0; i < nameCount; i++) {
            if (i > 0) {
                condition.append(" OR ");
            }
            condition.append(single);
        }
        return condition.append(")").toString();
    }

    /**
     * Binds the given template names (or fragments) to the parameters of a statement built with
     * {@link #buildTemplateNameCondition(int, boolean)}. The names are normalized the way they are
     * stored in the template index (lower case, trimmed, spaces replaced by underscores).
     *
     * @param statement
     *            the statement to bind the names to, starting at parameter index 1
     * @param templateNames
     *            the template names (or fragments)
     * @param prefix
     *            whether the names are bound as prefix patterns for {@code LIKE}
     * @throws SQLException
     *             If a parameter could not be set
     */
    static void bindTemplateNames(PreparedStatement statement, List<String> templateNames,
            boolean prefix)
        throws SQLException
    {
        int curIdx = 1;
        for (String name : templateNames) {
            name = name.toLowerCase().trim().replaceAll(" ", "_");
            statement.setString(curIdx++, prefix ? name + "%" : name);
        }
    }

    /**
     * Selects the ids of the pages that the page template index ({@code templateId_pageId})
     * associates with any of the given templates (whitelist), or of the pages in the index that
     * are associated with none of them (blacklist). Each page id is returned once, in ascending
     * order.
     *
     * @param templateNames
     *            template names or template name fragments to look for
     * @param prefix
     *            whether the given Strings are matched as name prefixes (true) or as full names
     *            (false)
     * @param whitelist
     *            whether to select pages containing these templates (true) or pages NOT
     *            containing these templates (false)
     * @return the distinct ids of the matching pages in ascending order
     * @throws WikiApiException
     *             If there was any error retrieving the page ids
     */
    private List<Integer> selectIndexedPageIds(List<String> templateNames, boolean prefix,
            boolean whitelist)
        throws WikiApiException
    {
        List<Integer> pageIds = new ArrayList<>();
        List<String> names = templateNames == null ? List.of() : templateNames;
        if (whitelist && names.isEmpty()) {
            return pageIds;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                buildIndexedPageIdQuery(names.size(), prefix, whitelist))) {
            bindTemplateNames(statement, names, prefix);

            try (ResultSet result = execute(statement)) {
                while (result.next()) {
                    pageIds.add(result.getInt(1));
                }
            }
        }
        catch (SQLException e) {
            throw new WikiApiException(e);
        }
        return pageIds;
    }

    /**
     * Counts the pages {@link #selectIndexedPageIds(List, boolean, boolean)} would select.
     *
     * @param templateNames
     *            template names or template name fragments to look for
     * @param prefix
     *            whether the given Strings are matched as name prefixes (true) or as full names
     *            (false)
     * @param whitelist
     *            whether to count pages containing these templates (true) or pages NOT containing
     *            these templates (false)
     * @return the number of distinct matching pages
     * @throws WikiApiException
     *             If there was any error counting the pages
     */
    private Integer countIndexedPages(List<String> templateNames, boolean prefix,
            boolean whitelist)
        throws WikiApiException
    {
        List<String> names = templateNames == null ? List.of() : templateNames;
        if (whitelist && names.isEmpty()) {
            return 0;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                buildIndexedPageCountQuery(names.size(), prefix, whitelist))) {
            bindTemplateNames(statement, names, prefix);

            try (ResultSet result = execute(statement)) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
        catch (SQLException e) {
            throw new WikiApiException(e);
        }
    }

    /**
     * Loads the pages with the given ids, in the given order, with one query per batch of ids.
     *
     * @param pageIds
     *            the ids of the pages to load
     * @return the loaded pages
     * @throws WikiApiException
     *             If a page could not be loaded, in particular a {@link WikiPageNotFoundException}
     *             if the template index refers to a page that does not exist
     */
    private List<Page> loadPages(List<Integer> pageIds) throws WikiApiException
    {
        List<Page> pages = wiki.getPages(pageIds);
        if (pages.size() != pageIds.size()) {
            throw new WikiPageNotFoundException((pageIds.size() - pages.size())
                    + " of the pages referenced by the template index were not found.");
        }
        return pages;
    }

    /**
     * Builds the query that selects the distinct ids of the pages in the page template index
     * ({@code templateId_pageId}) that contain any of the given templates (whitelist) or none of
     * them (blacklist), ordered by page id. The blacklist is evaluated per page, not per template
     * of a page: a page is selected only if none of its templates matches. Pages that do not
     * appear in the index, i.e. pages without any template, are never selected.
     *
     * @param nameCount
     *            the number of template names (or fragments) the query has to match; must be
     *            positive for a whitelist, a blacklist without names selects every indexed page
     * @param prefix
     *            whether the template names are matched as prefixes (true) or as full names
     *            (false)
     * @param whitelist
     *            whether to select pages containing the templates (true) or pages not containing
     *            them (false)
     * @return the SQL query with one parameter per template name
     */
    static String buildIndexedPageIdQuery(int nameCount, boolean prefix, boolean whitelist)
    {
        return "SELECT DISTINCT p.pageId" + buildIndexedPageFilter(nameCount, prefix, whitelist)
                + " ORDER BY p.pageId";
    }

    /**
     * Builds the query that counts the pages selected by
     * {@link #buildIndexedPageIdQuery(int, boolean, boolean)}.
     *
     * @param nameCount
     *            the number of template names (or fragments) the query has to match
     * @param prefix
     *            whether the template names are matched as prefixes (true) or as full names
     *            (false)
     * @param whitelist
     *            whether to count pages containing the templates (true) or pages not containing
     *            them (false)
     * @return the SQL query with one parameter per template name
     */
    static String buildIndexedPageCountQuery(int nameCount, boolean prefix, boolean whitelist)
    {
        return "SELECT COUNT(DISTINCT p.pageId)"
                + buildIndexedPageFilter(nameCount, prefix, whitelist);
    }

    private static String buildIndexedPageFilter(int nameCount, boolean prefix,
            boolean whitelist)
    {
        if (whitelist) {
            if (nameCount < 1) {
                throw new IllegalArgumentException("A whitelist needs at least one template name");
            }
            return " FROM " + GeneratorConstants.TABLE_TPLID_TPLNAME + " AS tpl JOIN "
                    + GeneratorConstants.TABLE_TPLID_PAGEID
                    + " AS p ON p.templateId = tpl.templateId WHERE "
                    + buildTemplateNameCondition(nameCount, prefix);
        }
        String from = " FROM " + GeneratorConstants.TABLE_TPLID_PAGEID + " AS p";
        if (nameCount < 1) {
            return from;
        }
        return from + " WHERE NOT EXISTS (SELECT 1 FROM " + GeneratorConstants.TABLE_TPLID_PAGEID
                + " AS p2 JOIN " + GeneratorConstants.TABLE_TPLID_TPLNAME
                + " AS tpl ON tpl.templateId = p2.templateId WHERE p2.pageId = p.pageId AND "
                + buildTemplateNameCondition(nameCount, prefix) + ")";
    }

    ///////////////////

    /**
     * Returns a list containing the ids of all pages that contain a template the name of which
     * equals any of the given Strings.
     *
     * @param templateNames
     *            the names of the template that we want to match
     * @param whitelist
     *            whether to return pages containing these templates (true) or return pages NOT
     *            containing these templates (false)
     * @return A list with the ids of all pages that contain any of the specified templates
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    private List<Integer> getFilteredPageIds(List<String> templateNames, boolean whitelist)
        throws WikiApiException
    {
        return selectIndexedPageIds(templateNames, false, whitelist);
    }

    /**
     * Returns a list containing the ids of all pages that contain a template the name of which
     * equals any of the given Strings.
     *
     * <p>
     * Each page is returned once, ordered by page id.
     *
     * @param templateNames
     *            the names of the template that we want to match
     * @return A list with the ids of all pages that contain any of the specified templates
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public List<Integer> getPageIdsContainingTemplateNames(List<String> templateNames)
        throws WikiApiException
    {
        return getFilteredPageIds(templateNames, true);
    }

    /**
     * Returns a list containing the ids of all pages that do not contain a template the name of
     * which equals any of the given Strings.
     *
     * <p>
     * Each page is returned once, ordered by page id.
     * <p>
     * The page template index is evaluated per page: only pages that contain at least one
     * template are considered, and a page is not returned if any of its templates matches.
     *
     * @param templateNames
     *            the names of the template that we want to match
     * @return A list with the ids of all pages that do not contain any of the specified
     *         templates
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public List<Integer> getPageIdsNotContainingTemplateNames(List<String> templateNames)
        throws WikiApiException
    {
        return getFilteredPageIds(templateNames, false);
    }

    /**
     * Returns a list containing the ids of all revisions that contain a template the name of which
     * equals any of the given Strings.
     *
     * @param templateNames
     *            the names of the template that we want to match
     * @param whitelist
     *            whether to return pages containing these templates (true) or return pages NOT
     *            containing these templates (false)
     * @return A list with the ids of all revisions that contain any of the specified templates
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    private List<Integer> getFilteredRevisionIds(List<String> templateNames, boolean whitelist)
        throws WikiApiException
    {
        List<Integer> matchedPages = new LinkedList<>();
        try {
            String sqlString = "SELECT r.revisionId FROM " + GeneratorConstants.TABLE_TPLID_TPLNAME
                    + " AS tpl, " + GeneratorConstants.TABLE_TPLID_REVISIONID
                    + " AS r WHERE tpl.templateId = r.templateId "
                    + (whitelist ? "AND " : "AND NOT ")
                    + buildTemplateNameCondition(templateNames.size(), false);

            try (PreparedStatement statement = connection.prepareStatement(sqlString)) {

                bindTemplateNames(statement, templateNames, false);

                ResultSet result = execute(statement);

                if (result == null) {
                    throw new WikiPageNotFoundException("Nothing was found");
                }

                while (result.next()) {
                    matchedPages.add(result.getInt(1));
                }
            }

            return matchedPages;
        }
        catch (Exception e) {
            throw new WikiApiException(e);
        }
    }

    /**
     * Returns a list containing the ids of all revisions that contain a template the name of which
     * equals any of the given Strings.
     *
     * @param templateNames
     *            the names of the template that we want to match
     * @return A list with the ids of all revisions that contain any of the specified templates
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public List<Integer> getRevisionIdsContainingTemplateNames(List<String> templateNames)
        throws WikiApiException
    {
        return getFilteredRevisionIds(templateNames, true);
    }

    /**
     * Returns a list containing the ids of all revisions that do not contain a template the name of
     * which equals any of the given Strings.
     *
     * @param templateNames
     *            the names of the template that we want to match
     * @return A list with the ids of all revisions that do not contain any of the specified
     *         templates
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public List<Integer> getRevisionIdsNotContainingTemplateNames(List<String> templateNames)
        throws WikiApiException
    {
        return getFilteredRevisionIds(templateNames, false);
    }

    /**
     * Returns the names of all templates contained in the specified page.
     *
     * @param page
     *            the page object for which the templates should be retrieved
     * @return A List with the names of the templates contained in the specified page
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public List<String> getTemplateNamesFromPage(Page page) throws WikiApiException
    {
        return getTemplateNamesFromPage(page.getPageId());
    }

    /**
     * Returns the names of all templates contained in the specified page.
     *
     * @param pageTitle
     *            the title of the page for which the templates should be retrieved
     * @return A List with the names of the templates contained in the specified page
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public List<String> getTemplateNamesFromPage(String pageTitle) throws WikiApiException
    {
        Page p;
        try {
            p = wiki.getPage(pageTitle);
        }
        catch (WikiApiException e) {
            // The page does not exist (or cannot be retrieved) - by contract, this method then
            // yields an empty result instead of failing. As the exception is not propagated,
            // it is logged here as it would be lost otherwise.
            logger.debug("Could not retrieve page [{}]. Returning an empty template name list.",
                    pageTitle, e);
            return new ArrayList<>();
        }
        return getTemplateNamesFromPage(p);
    }

    /**
     * Returns the names of all templates contained in the specified page.
     *
     * @param pageId
     *            the id of the Wiki page
     * @return A List with the names of the templates contained in the specified page
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public List<String> getTemplateNamesFromPage(int pageId) throws WikiApiException
    {
        if (pageId < 1) {
            throw new WikiApiException("Page ID must be > 0");
        }
        List<String> templateNames = new LinkedList<>();
        try {
            final String sql = "SELECT tpl.templateName FROM " + GeneratorConstants.TABLE_TPLID_TPLNAME
                            + " AS tpl, " + GeneratorConstants.TABLE_TPLID_PAGEID
                            + " AS p WHERE tpl.templateId = p.templateId AND p.pageId = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, pageId);

                ResultSet result = execute(statement);

                if (result == null) {
                    return templateNames;
                }

                while (result.next()) {
                    templateNames.add(result.getString(1).toLowerCase());
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
     * @return A List with the names of the templates contained in the specified revision
     * @throws WikiApiException
     *             If there was any error retrieving the page object (most likely if the templates
     *             are corrupted)
     */
    public List<String> getTemplateNamesFromRevision(int revid) throws WikiApiException
    {
        if (revid < 1) {
            throw new WikiApiException("Revision ID must be > 0");
        }
        List<String> templateNames = new LinkedList<>();
        try {
            final String sql = "SELECT tpl.templateName FROM " + GeneratorConstants.TABLE_TPLID_TPLNAME
                    + " AS tpl, " + GeneratorConstants.TABLE_TPLID_REVISIONID
                    + " AS p WHERE tpl.templateId = p.templateId AND p.revisionId = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, revid);

                ResultSet result = execute(statement);

                if (result == null) {
                    return templateNames;
                }

                while (result.next()) {
                    templateNames.add(result.getString(1).toLowerCase());
                }
            }

            return templateNames;
        }
        catch (Exception e) {
            throw new WikiApiException(e);
        }
    }

    /**
     * Determines whether a given revision contains a given template name.
     *
     * @param revId The revision identifier to use.
     * @param templateName A template name to check for.
     * @return {@code true} if the revision contains {@code templateName}, {@code false} otherwise.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public boolean revisionContainsTemplateName(int revId, String templateName)
        throws WikiApiException
    {
        return revisionContainsTemplateNames(revId, List.of(templateName));
    }

    /**
     * Determines whether a given revision contains a given template name.
     *
     * @param revId The revision identifier to use.
     * @param templateNames A list of template names.
     * @return {@code true} if the revision contains one element in {@code templateNames},
     *         {@code false} otherwise.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public boolean revisionContainsTemplateNames(int revId, List<String> templateNames)
        throws WikiApiException
    {
        List<String> tplList = getTemplateNamesFromRevision(revId);
        for (String tpl : tplList) {
            for (String templateName : templateNames) {
                if (tpl.equalsIgnoreCase(templateName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether a given revision contains a template starting with the given fragment.
     *
     * @param revId The revision identifier to use.
     * @param templateFragment A (partial) template name to check for.
     * @return {@code true} if the revision contains {@code templateFragment}, {@code false}
     *         otherwise.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public boolean revisionContainsTemplateFragment(int revId, String templateFragment)
        throws WikiApiException
    {
        List<String> tplList = getTemplateNamesFromRevision(revId);
        for (String tpl : tplList) {
            if (tpl.toLowerCase().startsWith(templateFragment.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Does the same as {@link #revisionContainsTemplateFragment(int, String)} without using a
     * template index
     *
     * @param revId The revision identifier to use.
     * @param templateName A template name to check for.
     * @return {@code true} if the revision contains {@code templateName}, {@code false} otherwise.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public boolean revisionContainsTemplateNameWithoutIndex(int revId, String templateName)
        throws WikiApiException
    {
        if (revApi == null) {
            revApi = new RevisionApi(wiki.getDatabaseConfiguration());
        }
        if (parser == null) {
            // TODO switch to SWEBLE
            MediaWikiParserFactory pf = new MediaWikiParserFactory(wiki.getDatabaseConfiguration().getLanguage());
            pf.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
            parser = pf.createParser();
        }

        List<Template> tplList = parser.parse(revApi.getRevision(revId).getRevisionText())
                .getTemplates();
        for (Template tpl : tplList) {
            if (tpl.getName().equalsIgnoreCase(templateName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Does the same as {@link #revisionContainsTemplateNameWithoutIndex(int, String)} without
     * using a template index
     *
     * @param revId The revision identifier to use.
     * @param templateFragment A (partial) template name to check for.
     * @return {@code true} if the revision contains {@code templateFragment}, {@code false}
     *         otherwise.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public boolean revisionContainsTemplateFragmentWithoutIndex(int revId, String templateFragment)
        throws WikiApiException
    {
        if (revApi == null) {
            revApi = new RevisionApi(wiki.getDatabaseConfiguration());
        }
        if (parser == null) {
            // TODO switch to SWEBLE
            MediaWikiParserFactory pf = new MediaWikiParserFactory(
                    wiki.getDatabaseConfiguration().getLanguage());
            pf.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
            parser = pf.createParser();
        }

        List<Template> tplList = parser.parse(revApi.getRevision(revId).getRevisionText())
                .getTemplates();
        for (Template tpl : tplList) {
            if (tpl.getName().toLowerCase().startsWith(templateFragment.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns all adjacent revision pairs of namespace 0 pages (articles) in which a
     * given template has been removed or added (depending on the RevisionPairType) in the second
     * pair part.
     *
     * @param template
     *            a template to look for
     * @param type
     *            the type of template change (add or remove) that should be extracted
     * @return list of revision pairs containing the desired template changes.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public List<RevisionPair> getArticleRevisionPairs(String template,
            RevisionPair.RevisionPairType type)
        throws WikiApiException
    {
        if (revApi == null) {
            revApi = new RevisionApi(wiki.getDatabaseConfiguration());
        }
        // get revisions via index (this COULD take a while)
        List<Integer> revIds = getRevisionIdsContainingTemplateNames(List.of(template));
        System.out.println(revIds.size() + " revisions with given template found"); // TODO
                                                                                    // DEBUGCODE
        // membership tests only; iteration still runs over revIds to keep the result order
        Set<Integer> revIdSet = new HashSet<>(revIds);
        // discussion status per article id, so that each article is looked up only once
        Map<Integer, Boolean> discussionByArticle = new HashMap<>();
        List<RevisionPair> resultList = new LinkedList<>();

        // check all revisions. this WILL take a while
        int curRevNum = 0;
        for (int revId : revIds) {
            //////////////////
            // TODO DEBUGCODE
            curRevNum++;
            if (curRevNum % 100 == 0) {
                System.out.println("Processing revision " + curRevNum);
            }
            /////////////////
            try {
                // TODO check article/discussion status without creating Revision object
                Revision current = revApi.getRevision(revId);
                if (!isDiscussion(current.getArticleID(), discussionByArticle)) {
                    int currentCounter = current.getRevisionCounter();

                    if (type == RevisionPairType.deleteTemplate) {
                        // We want revs in which a template has just been removed.
                        // Check succeeding revision. If template is not present there anymore,
                        // it has been deleted. If so, add pair to list.
                        try {
                            // TODO retrieve succeeding revId without creating Revision object
                            Revision succeeding = revApi.getRevision(current.getArticleID(),
                                    currentCounter + 1);
                            // check status of succeeding rev in tplIndex
                            if (!revIdSet.contains(succeeding.getRevisionID())) {
                                resultList
                                        .add(new RevisionPair(current, succeeding, template, type));
                            }
                        }
                        catch (WikiPageNotFoundException e) {
                            // current was probably the last revision already
                            logger.debug("Succeeding revision not found.", e);
                        }
                    }
                    if (type == RevisionPairType.addTemplate) {
                        // We want revs in which a template has just been added
                        // Check preceding revision. If template is not present there,
                        // it has been added in this revision. If so, add pair to list.
                        try {
                            // TODO retrieve preceding revId without creating Revision object
                            Revision preceding = revApi.getRevision(current.getArticleID(),
                                    currentCounter - 1);
                            // check status of preceding rev in tplIndex
                            if (!revIdSet.contains(preceding.getRevisionID())) {
                                resultList
                                        .add(new RevisionPair(preceding, current, template, type));
                            }
                        }
                        catch (WikiPageNotFoundException e) {
                            // current was probably the first revision already
                            logger.debug("Preceding revision not found.", e);
                        }
                    }
                }
            }
            catch (WikiPageNotFoundException e) {
                // The revision from the template db is missing in the revision db.
                logger.warn("Current revision ({}) not found.", revId, e);
            }
        }

        return resultList;
    }

    /**
     * Checks whether the page with the given id is a discussion page via
     * {@link Page#isDiscussion()}. The outcome is stored in the given cache, so each page is loaded
     * at most once per cache.
     *
     * @param pageId
     *            the id of the page to check
     * @param cache
     *            discussion status of the pages checked so far, keyed by page id
     * @return {@code true} if the page is a discussion page
     * @throws WikiApiException
     *             If the page does not exist or its title cannot be read
     */
    private boolean isDiscussion(int pageId, Map<Integer, Boolean> cache) throws WikiApiException
    {
        Boolean discussion = cache.get(pageId);
        if (discussion == null) {
            discussion = wiki.getPage(pageId).isDiscussion();
            cache.put(pageId, discussion);
        }
        return discussion;
    }

    /**
     * For a given page (pageId), this method returns all adjacent revision pairs in which a given
     * template has been removed or added (depending on the RevisionPairType) in the second pair
     * part.
     *
     * @param pageId
     *            id of the page whose revision history should be inspected
     * @param template
     *            the template to look for
     * @param type
     *            the type of template change (add or remove) that should be extracted
     * @return list of revision pairs containing the desired template changes.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public List<RevisionPair> getRevisionPairs(int pageId, String template,
            RevisionPair.RevisionPairType type)
        throws WikiApiException
    {
        if (revApi == null) {
            revApi = new RevisionApi(wiki.getDatabaseConfiguration());
        }

        List<RevisionPair> resultList = new LinkedList<>();
        Map<Timestamp, Boolean> tplIndexMap = new HashMap<>();

        List<Timestamp> revTsList = revApi.getRevisionTimestamps(pageId);
        for (Timestamp ts : revTsList) {
            tplIndexMap.put(ts, revisionContainsTemplateName(
                    revApi.getRevision(pageId, ts).getRevisionID(), template));
        }

        SortedSet<Entry<Timestamp, Boolean>> entries = new TreeSet<>(Entry.comparingByKey());
        entries.addAll(tplIndexMap.entrySet());

        Entry<Timestamp, Boolean> prev = null;
        Entry<Timestamp, Boolean> current;
        for (Entry<Timestamp, Boolean> e : entries) {
            current = e;
            // check pair
            if (prev != null && prev.getValue() != current.getValue()) {
                // case: template has been deleted since last revision
                if (prev.getValue() && !current.getValue()
                        && type == RevisionPairType.deleteTemplate) {
                    resultList.add(new RevisionPair(revApi.getRevision(pageId, prev.getKey()),
                            revApi.getRevision(pageId, current.getKey()), template,
                            RevisionPairType.deleteTemplate));
                }
                // case: template has been added since last revision
                if (!prev.getValue() && current.getValue()
                        && type == RevisionPairType.addTemplate) {
                    resultList.add(new RevisionPair(revApi.getRevision(pageId, prev.getKey()),
                            revApi.getRevision(pageId, current.getKey()), template,
                            RevisionPairType.addTemplate));
                }
            }
            prev = current;
        }
        return resultList;
    }

    /**
     * Does the same as {@link #getRevisionPairs(int, String, RevisionPairType)}, but does not use a
     * template index
     *
     * @param pageId
     *            id of the page whose revision history should be inspected
     * @param template
     *            the template to look for
     * @param type
     *            the type of template change (add or remove) that should be extracted
     * @return list of revision pairs containing the desired template changes
     * @throws WikiApiException Thrown if errors occurred.
     */
    public List<RevisionPair> getRevisionPairsWithoutIndex(int pageId, String template,
            RevisionPair.RevisionPairType type)
        throws WikiApiException
    {
        System.err.println(
                "This methods has to parse each revision of the given page. " +
                        "If you have a revision-template index, please use getRevisionPairs().");
        if (revApi == null) {
            revApi = new RevisionApi(wiki.getDatabaseConfiguration());
        }

        List<RevisionPair> resultList = new LinkedList<>();
        Map<Timestamp, Boolean> tplIndexMap = new HashMap<>();

        List<Timestamp> revTsList = revApi.getRevisionTimestamps(pageId);
        for (Timestamp ts : revTsList) {
            tplIndexMap.put(ts, revisionContainsTemplateNameWithoutIndex(
                    revApi.getRevision(pageId, ts).getRevisionID(), template));
        }

        SortedSet<Entry<Timestamp, Boolean>> entries = new TreeSet<>(Entry.comparingByKey());
        entries.addAll(tplIndexMap.entrySet());

        Entry<Timestamp, Boolean> prev = null;
        Entry<Timestamp, Boolean> current;
        for (Entry<Timestamp, Boolean> e : entries) {
            current = e;
            // check pair
            if (prev != null && prev.getValue() != current.getValue()) {
                // case: template has been deleted since last revision
                if (prev.getValue() && !current.getValue()
                        && type == RevisionPairType.deleteTemplate) {
                    resultList.add(new RevisionPair(revApi.getRevision(pageId, prev.getKey()),
                            revApi.getRevision(pageId, current.getKey()), template,
                            RevisionPairType.deleteTemplate));
                }
                // case: template has been added since last revision
                if (!prev.getValue() && current.getValue()
                        && type == RevisionPairType.addTemplate) {
                    resultList.add(new RevisionPair(revApi.getRevision(pageId, prev.getKey()),
                            revApi.getRevision(pageId, current.getKey()), template,
                            RevisionPairType.addTemplate));
                }
            }
            prev = current;
        }
        return resultList;
    }

    /**
     * Checks if a specific table exists
     *
     * @param table the table's name to check.
     * @return {@code true} if table exists, {@code false} otherwise.
     * @throws SQLException
     *             if an error occurs connecting to or querying the db.
     */
    public boolean tableExists(String table) throws SQLException
    {
        if (table == null || table.isBlank()) {
            return false;
        }
        try (PreparedStatement statement = this.connection.prepareStatement("SHOW TABLES;");
                ResultSet result = execute(statement)) {

            if (result == null) {
                return false;
            }
            boolean found = false;
            while (result.next()) {
                if (table.equalsIgnoreCase(result.getString(1))) {
                    found = true;
                }
            }
            return found;

        }

    }

    private Connection getConnection(Wikipedia wiki) throws WikiApiException
    {
        DatabaseConfiguration config = wiki.getDatabaseConfiguration();

        Connection c;
        try {
            String driverDB = "com.mysql.jdbc.Driver";
            Class.forName(driverDB);

            c = DriverManager.getConnection(
                            "jdbc:mysql://" + config.getHost() + "/" + config.getDatabase()
                                    + "?autoReconnect=true",
                            config.getUser(), config.getPassword());

            if (!c.isValid(5)) {
                throw new WikiApiException("Connection could not be established.");
            }
        }
        catch (SQLException | ClassNotFoundException e) {
            throw new WikiApiException(e);
        }

        return c;
    }

    public void close() throws SQLException
    {
        if (this.connection != null) {
            this.connection.close();
        }
    }

    public void reconnect() throws SQLException
    {
        close();
        try {
            this.connection = getConnection(wiki);
        }
        catch (WikiApiException e) {
            close();
            logger.error("Could not reconnect. Closing connection...", e);
        }
    }

    private ResultSet execute(PreparedStatement state) throws SQLException
    {
        ResultSet res;
        try {
            res = state.executeQuery();
        }
        catch (Exception e) {
            // The query failed - most likely due to a stale connection. Reconnect once and retry.
            // The original failure is not propagated as the retry either succeeds or fails with
            // its own exception, hence it is logged here to not lose that information.
            logger.debug("Query execution failed. Reconnecting and retrying ...", e);
            reconnect();
            res = state.executeQuery();
        }
        return res;
    }

}
