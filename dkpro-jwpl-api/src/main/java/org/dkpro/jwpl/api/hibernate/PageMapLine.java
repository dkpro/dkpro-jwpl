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
 * An object-relational entity to reflect data attributes in a database.
 * Eventually, those are retrieved by an OR mapper, such as Hibernate.
 */
public class PageMapLine
{
    private long id;
    private String name;
    private int pageID;
    private String stem;
    private String lemma;

    /**
     * A no argument constructor as required by Hibernate.
     */
    public PageMapLine()
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
     * @return Retrieves the name as used in Wikipedia.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name The name as used in Wikipedia.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return Retrieves the page identifier as used in Wikipedia.
     */
    public int getPageID()
    {
        return pageID;
    }

    /**
     * @param pageID The page identifier as used in Wikipedia.
     */
    public void setPageID(int pageID)
    {
        this.pageID = pageID;
    }

    /**
     * @return Retrieves the lemma.
     */
    public String getLemma()
    {
        return lemma;
    }

    /**
     * @param lemma The lemma to set.
     */
    public void setLemma(String lemma)
    {
        this.lemma = lemma;
    }

    /**
     * @return Retrieves the stem.
     */
    public String getStem()
    {
        return stem;
    }

    /**
     * @param stem The stem to set.
     */
    public void setStem(String stem)
    {
        this.stem = stem;
    }
}
