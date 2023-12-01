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

/**
 * An object-relational entity which maps a {@link org.dkpro.jwpl.api.MetaData}
 * to data attributes in a database. Those are persisted and retrieved by
 * an OR mapper, such as Hibernate.
 * <p>
 * It is accessed via an equally named class in the {@code api} package
 * to hide session management from the user.
 */
public class MetaData
{

    private long id;

    private String language;
    private String disambiguationCategory;
    private String mainCategory;
    private String version;

    private long nrofPages;
    private long nrofRedirects;
    private long nrofDisambiguationPages;
    private long nrofCategories;

    /**
     * A no argument constructor as required by Hibernate.
     */
    public MetaData()
    {
    }

    /**
     * @return Retrieves the category title used for disambiguation.
     */
    public String getDisambiguationCategory()
    {
        return disambiguationCategory;
    }

    /**
     * @param disambiguationCategory The category title used for disambiguation.
     */
    public void setDisambiguationCategory(String disambiguationCategory)
    {
        this.disambiguationCategory = disambiguationCategory;
    }

    /**
     * @return Retrieves the title of main (or root) category associated with this Wikipedia.
     */
    public String getMainCategory()
    {
        return mainCategory;
    }

    /**
     * @param mainCategory The title of the main (or root) category associated with this Wikipedia.
     */
    public void setMainCategory(String mainCategory)
    {
        this.mainCategory = mainCategory;
    }

    /**
     * @return Retrieves the primary key identifying a {@link MetaData} instance.
     */
    public long getId()
    {
        return id;
    }

    /**
     * @param id The primary key identifying a {@link MetaData} instance.
     */
    public void setId(long id)
    {
        this.id = id;
    }

    /**
     * @return Retrieves the main language associated by the {@link MetaData} of a Wikipedia instance.
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * @param language The main language associated by the {@link MetaData} of a Wikipedia instance.
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * @return Retrieves the total number of available categories for a Wikipedia instance.
     */
    public long getNrofCategories()
    {
        return nrofCategories;
    }

    /**
     * @param nrofCategories The total number of available categories for a Wikipedia instance.
     */
    public void setNrofCategories(long nrofCategories)
    {
        this.nrofCategories = nrofCategories;
    }

    /**
     * @return Retrieves the total number of disambiguation pages for a Wikipedia instance.
     */
    public long getNrofDisambiguationPages()
    {
        return nrofDisambiguationPages;
    }

    /**
     * @param nrofDisambiguationPages The total number of disambiguation pages for a Wikipedia instance.
     */
    public void setNrofDisambiguationPages(long nrofDisambiguationPages)
    {
        this.nrofDisambiguationPages = nrofDisambiguationPages;
    }

    /**
     * @return Retrieves the total number of pages for a Wikipedia instance.
     */
    public long getNrofPages()
    {
        return nrofPages;
    }

    /**
     * @param nrofPages The total number of pages for a Wikipedia instance.
     */
    public void setNrofPages(long nrofPages)
    {
        this.nrofPages = nrofPages;
    }

    /**
     * @return Retrieves the total number of redirects for a Wikipedia instance.
     */
    public long getNrofRedirects()
    {
        return nrofRedirects;
    }
    
    /**
     * @param nrofRedirects The total number of redirects for a Wikipedia instance.
     */
    public void setNrofRedirects(long nrofRedirects)
    {
        this.nrofRedirects = nrofRedirects;
    }

    /**
     * @return Retrieves the version of a {@link MetaData} instance.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * @param version The version of a {@link MetaData} instance.
     */
    public void setVersion(String version)
    {
        this.version = version;
    }
}
