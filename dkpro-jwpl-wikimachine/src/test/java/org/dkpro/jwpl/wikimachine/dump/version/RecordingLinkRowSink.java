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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@link LinkRowSink} that records what {@link LinkRowProcessor} asked it to write, so that the
 * classification of a {@code categorylinks} respectively a {@code pagelinks} row can be asserted
 * without a database or a dump version implementation.
 */
final class RecordingLinkRowSink
    implements LinkRowSink
{

    private final Map<String, Integer> categories = new HashMap<>();
    private final Map<String, Integer> pages = new HashMap<>();
    private final Set<Integer> articleIds = new HashSet<>();
    private final Set<Integer> categoryIds = new HashSet<>();

    private boolean skipPage = true;
    private String disambiguationCategory;

    final List<String> memberships = new ArrayList<>();
    final List<String> subcategories = new ArrayList<>();
    final List<String> pageLinks = new ArrayList<>();
    final List<Integer> disambiguations = new ArrayList<>();

    RecordingLinkRowSink withCategory(String title, int pageId)
    {
        categories.put(title, pageId);
        categoryIds.add(pageId);
        return this;
    }

    RecordingLinkRowSink withArticle(String title, int pageId)
    {
        pages.put(title, pageId);
        articleIds.add(pageId);
        return this;
    }

    RecordingLinkRowSink withDisambiguationCategory(String title)
    {
        disambiguationCategory = title;
        return this;
    }

    RecordingLinkRowSink withSkipPage(boolean value)
    {
        skipPage = value;
        return this;
    }

    @Override
    public Integer categoryIdByTitle(String title)
    {
        return categories.get(title);
    }

    @Override
    public Integer pageIdByTitle(String title)
    {
        return pages.get(title);
    }

    @Override
    public boolean isKnownArticleId(int pageId)
    {
        return articleIds.contains(pageId);
    }

    @Override
    public boolean isKnownCategoryId(int pageId)
    {
        return categoryIds.contains(pageId);
    }

    @Override
    public boolean isSkipPageEnabled()
    {
        return skipPage;
    }

    @Override
    public String getDisambiguationCategoryTitle()
    {
        return disambiguationCategory;
    }

    @Override
    public void recordDisambiguation(int pageId)
    {
        disambiguations.add(pageId);
    }

    @Override
    public void writeCategoryMembership(int categoryId, int pageId)
    {
        memberships.add(categoryId + "->" + pageId);
    }

    @Override
    public void writeSubcategory(int parentCategoryId, int childCategoryId)
    {
        subcategories.add(parentCategoryId + "->" + childCategoryId);
    }

    @Override
    public void writePageLink(int fromPageId, int toPageId)
    {
        pageLinks.add(fromPageId + "->" + toPageId);
    }
}
