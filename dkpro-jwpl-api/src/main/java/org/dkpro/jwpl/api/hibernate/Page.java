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
package org.dkpro.jwpl.api.hibernate;

import java.util.HashSet;
import java.util.Set;

/**
 * An object-relational entity which maps a {@link org.dkpro.jwpl.api.Page}
 * to data attributes in a database. Those are persisted and retrieved by
 * an OR mapper, such as Hibernate.
 * <p>
 * It is accessed via an equally named class in the {@code api} package
 * to hide session management from the user.
 */
public class Page
{

    private long id;
    private int pageId;
    private String name;
    private String text;
    private boolean isDisambiguation;
    private Set<Integer> inLinks = new HashSet<>();
    private Set<Integer> outLinks = new HashSet<>();
    private Set<Integer> categories = new HashSet<>();
    private Set<String> redirects = new HashSet<>();

    /**
     * A no argument constructor as required by Hibernate.
     */
    public Page()
    {
    }

    /**
     * @return Retrieves the primary key identifying this persistent object.
     */
    public long getId()
    {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(long id)
    {
        this.id = id;
    }

    /**
     * @return Retrieves the page identifier as used in Wikipedia.
     */
    public int getPageId()
    {
        return pageId;
    }

    /**
     * @param pageId The page identifier as used in Wikipedia.
     */
    public void setPageId(int pageId)
    {
        this.pageId = pageId;
    }

    /**
     * @return Retrieves a set of {@link Category categoryIDs} a {@link Page} is found in.
     */
    public Set<Integer> getCategories()
    {
        return categories;
    }

    /**
     * @param categories A set of {@link Category categoryIDs} a {@link Page} is found in.
     */
    public void setCategories(Set<Integer> categories)
    {
        this.categories = categories;
    }

    /**
     * @return Retrieves a set of {@link Page pageIds} a {@link Page} is referenced from.
     */
    public Set<Integer> getInLinks()
    {
        return inLinks;
    }

    /**
     * @param inLinks set of {@link Page pageIds} this {@link Page} is referenced from.
     */
    public void setInLinks(Set<Integer> inLinks)
    {
        this.inLinks = inLinks;
    }

    /**
     * @return Retrieves the page title as used in Wikipedia.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name The page title as used in Wikipedia.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return Retrieves a set of {@link Page pageIds} a {@link Page} references to.
     */
    public Set<Integer> getOutLinks()
    {
        return outLinks;
    }

    /**
     * @return Retrieves the cardinality of outgoing page references.
     */
    public int getOutDegree()
    {
        return outLinks.size();
    }

    /**
     * @param outLinks A set of {@link Page pageIds} a {@link Page} references to.
     */
    public void setOutLinks(Set<Integer> outLinks)
    {
        this.outLinks = outLinks;
    }

    /**
     * @return Retrieves a set of {@link String redirects} which exist for a {@link Page}.
     */
    public Set<String> getRedirects()
    {
        return redirects;
    }

    /**
     * @param redirects  A set of {@link String redirects} which exist for a {@link Page}.
     */
    public void setRedirects(Set<String> redirects)
    {
        this.redirects = redirects;
    }

    /**
     * @return Retrieves a page's content (text) as used in Wikipedia.
     */
    public String getText()
    {
        return text;
    }

    /**
     * @param text The page's content (text) as used in Wikipedia.
     */
    public void setText(String text)
    {
        this.text = text;
    }

    /**
     * @return {@code True} if a page is a disambiguation page, {@code false} otherwise.
     */
    public boolean getIsDisambiguation()
    {
        return isDisambiguation;
    }

    /**
     * @param isDisambiguation  {@code True} if a page is a disambiguation page, {@code false} otherwise.
     */
    public void setIsDisambiguation(Boolean isDisambiguation)
    {
        if (isDisambiguation == null) {
            isDisambiguation = false;
        }
        this.isDisambiguation = isDisambiguation;
    }
}
