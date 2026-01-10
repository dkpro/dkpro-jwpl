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
package org.dkpro.jwpl.wikimachine.domain;

import java.sql.Timestamp;

/**
 * Holds the meta data for a dump version.
 */
public class MetaData
{

    private static final String SQL_NULL = "NULL";

    private String id;
    private String language;
    private String mainCategory;
    private String disambiguationCategory;
    private Timestamp timestamp;
    private Integer nrOfCategories = 0;
    private Integer nrOfPages = 0;
    private Integer nrOfRedirects = 0;
    private Integer nrOfDisambiguations = 0;

    /**
     * Instantiates an empty {@link MetaData} object.
     */
    public MetaData() {}

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the language
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * @return the mainCategory
     */
    public String getMainCategory()
    {
        return mainCategory;
    }

    /**
     * @param mainCategory the mainCategory to set
     */
    public void setMainCategory(String mainCategory)
    {
        this.mainCategory = mainCategory;
    }

    /**
     * @return the disambiguationCategory
     */
    public String getDisambiguationCategory()
    {
        return disambiguationCategory;
    }

    /**
     * @param disambiguationCategory the disambiguationCategory to set
     */
    public void setDisambiguationCategory(String disambiguationCategory)
    {
        this.disambiguationCategory = disambiguationCategory;
    }

    /**
     * @return the timestamp
     */
    public Timestamp getTimestamp()
    {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Timestamp timestamp)
    {
        this.timestamp = timestamp;
    }

    /**
     * @return the nrOfCategories
     */
    public int getNrOfCategories()
    {
        return nrOfCategories;
    }

    /**
     * @param nrOfCategories the nrOfCategories to set
     */
    public void setNrOfCategories(int nrOfCategories)
    {
        this.nrOfCategories = nrOfCategories;
    }

    /**
     * @return the nrOfPages
     */
    public int getNrOfPages()
    {
        return nrOfPages;
    }

    /**
     * @param nrOfPages the nrOfPages to set
     */
    public void setNrOfPages(int nrOfPages)
    {
        this.nrOfPages = nrOfPages;
    }

    /**
     * @return the nrOfRedirects
     */
    public int getNrOfRedirects()
    {
        return nrOfRedirects;
    }

    /**
     * @param nrOfRedirects  the nrOfRedirects to set
     */
    public void setNrOfRedirects(int nrOfRedirects)
    {
        this.nrOfRedirects = nrOfRedirects;
    }

    /**
     * @return the nrOfDisambiguations
     */
    public int getNrOfDisambiguations()
    {
        return nrOfDisambiguations;
    }

    /**
     * Increments the pages counter.
     */
    public void addPage()
    {
        nrOfPages++;
    }

    /**
     * Increments the disambiguation pages counter.
     */
    public void addDisamb()
    {
        nrOfDisambiguations++;
    }

    /**
     * Increments the redirects counter.
     */
    public void addRedirect()
    {
        nrOfRedirects++;
    }

    /**
     * Increments the categories counter.
     */
    public void addCategory()
    {
        nrOfCategories++;
    }

    /**
     * Inits a {@link MetaData} object via a {@link Configuration configuration} and its relevant
     * values of {@link Configuration#getLanguage() language},
     * {@link Configuration#getMainCategory() main category}, and
     *  {@link Configuration#getDisambiguationCategory() disambiguation category}
     *
     * @param config The {@link Configuration} to use for initializing a {@link MetaData} object.
     * @return A valid {@link MetaData} object filled with basic configuration parameters.
     */
    public static MetaData initWithConfig(Configuration config)
    {
        MetaData result = new MetaData();
        result.setId(SQL_NULL); // id is a auto_increment column
        result.setLanguage(config.getLanguage());
        result.setMainCategory(config.getMainCategory());
        result.setDisambiguationCategory(config.getDisambiguationCategory());
        return result;
    }
}
