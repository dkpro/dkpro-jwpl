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
 * An object-relational entity which maps a {@link org.dkpro.jwpl.api.Category}
 * to data attributes in a database. Those are persisted and retrieved by
 * an OR mapper, such as Hibernate.
 * <p>
 * It is accessed via an equally named class in the {@code api} package
 * to hide session management from the user.
 */
public class Category
{
    private long id;
    private int pageId;
    private String name;
    private Set<Integer> inLinks = new HashSet<>();
    private Set<Integer> outLinks = new HashSet<>();
    private Set<Integer> pages = new HashSet<>();

    /**
     * A no argument constructor as required by Hibernate.
     */
    public Category()
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
     * @return Retrieves a set of {@link Page pageIds} a {@link Category} is referenced from.
     */
    public Set<Integer> getInLinks()
    {
        return inLinks;
    }

    /**
     * @param inLinks A set of {@link Page pageIds} this {@link Category} is referenced from.
     */
    public void setInLinks(Set<Integer> inLinks)
    {
        this.inLinks = inLinks;
    }

    /**
     * @return Retrieves the category title as used in Wikipedia.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name The category title as used in Wikipedia.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return Retrieves a set of {@link Page pageIds} a {@link Category} references to.
     */
    public Set<Integer> getOutLinks()
    {
        return outLinks;
    }

    /**
     * @param outLinks A set of {@link Page pageIds} a {@link Category} references to.
     */
    public void setOutLinks(Set<Integer> outLinks)
    {
        this.outLinks = outLinks;
    }

    /**
     * @return Retrieves a set of {@link Page pages} a {@link Category} groups together.
     */
    public Set<Integer> getPages()
    {
        return pages;
    }

    /**
     * @param pages The set of {@link Page pages} a {@link Category} groups together.
     */
    public void setPages(Set<Integer> pages)
    {
        this.pages = pages;
    }
}
