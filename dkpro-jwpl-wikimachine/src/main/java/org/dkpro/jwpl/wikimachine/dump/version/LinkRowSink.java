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
package org.dkpro.jwpl.wikimachine.dump.version;

import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;

/**
 * The lookup and write hooks a {@link IDumpVersion} has to provide so that {@link LinkRowProcessor}
 * can turn a {@link CategorylinksParser} or {@link PagelinksParser} row into the corresponding
 * output rows.
 * <p>
 * The concrete {@link IDumpVersion} implementations differ only in the flavour of the maps they
 * keep - plain JDK maps, hashed keys or fastutil primitive maps. Isolating those differences
 * behind this interface keeps a single copy of the link row algorithm, which is what makes the
 * behaviour of every variant consistent.
 */
public interface LinkRowSink
{

    /**
     * @param title The SQL escaped title of a category.
     * @return The page id registered for that category, or {@code null} if it is unknown.
     */
    Integer categoryIdByTitle(String title);

    /**
     * @param title The SQL escaped title of an article.
     * @return The page id registered for that article, or {@code null} if it is unknown.
     */
    Integer pageIdByTitle(String title);

    /**
     * @param pageId A page id.
     * @return {@code true} if {@code pageId} denotes a registered article.
     */
    boolean isKnownArticleId(int pageId);

    /**
     * @param pageId A page id.
     * @return {@code true} if {@code pageId} denotes a registered category.
     */
    boolean isKnownCategoryId(int pageId);

    /**
     * @return {@code true} if page links whose source is not a registered article are to be
     *         skipped.
     */
    boolean isSkipPageEnabled();

    /**
     * @return The title of the disambiguation category, may be {@code null}.
     */
    String getDisambiguationCategoryTitle();

    /**
     * Records that the given page is a disambiguation page.
     *
     * @param pageId The page id of the disambiguation page.
     */
    void recordDisambiguation(int pageId);

    /**
     * Writes the membership of a page in a category, that is, one row into {@code category_pages}
     * and one row into {@code page_categories}.
     *
     * @param categoryId The page id of the category.
     * @param pageId     The page id of the member page.
     */
    void writeCategoryMembership(int categoryId, int pageId);

    /**
     * Writes the membership of a category in another category, that is, one row into
     * {@code category_outlinks} and one row into {@code category_inlinks}.
     *
     * @param parentCategoryId The page id of the parent category.
     * @param childCategoryId  The page id of the child category.
     */
    void writeSubcategory(int parentCategoryId, int childCategoryId);

    /**
     * Writes a page link, that is, one row into {@code page_outlinks} and one row into
     * {@code page_inlinks}.
     *
     * @param fromPageId The page id of the linking page.
     * @param toPageId   The page id of the linked page.
     */
    void writePageLink(int fromPageId, int toPageId);
}
