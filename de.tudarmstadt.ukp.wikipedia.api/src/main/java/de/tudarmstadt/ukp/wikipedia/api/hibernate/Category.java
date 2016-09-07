/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api.hibernate;

import java.util.*;

public class Category {
    private long id;
    private int pageId;
    private String name;
    private Set<Integer> inLinks  = new HashSet<Integer>();
    private Set<Integer> outLinks = new HashSet<Integer>();
    private Set<Integer> pages    = new HashSet<Integer>();

    /** A no argument constructor as required by Hibernate. */
    public Category () {};


    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(long id) {
        this.id = id;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public Set<Integer> getInLinks() {
        return inLinks;
    }

    public void setInLinks(Set<Integer> inLinks) {
        this.inLinks = inLinks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Integer> getOutLinks() {
        return outLinks;
    }

    public void setOutLinks(Set<Integer> outLinks) {
        this.outLinks = outLinks;
    }

    public Set<Integer> getPages() {
        return pages;
    }

    public void setPages(Set<Integer> pages) {
        this.pages = pages;
    }
}
