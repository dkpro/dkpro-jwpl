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
package de.tudarmstadt.ukp.wikipedia.api.hibernate;

import java.util.HashSet;
import java.util.Set;

/**
 * The page class that is actually persisted by Hibernate.
 * It is accessed via a equally named class in the api package to hide session management from the user.
 *
 */
public class Page {
    private long id;
    private int pageId;
    private String name;
    private String text;
    private boolean isDisambiguation;
    private Set<Integer> inLinks = new HashSet<Integer>();
    private Set<Integer> outLinks = new HashSet<Integer>();
    private Set<Integer> categories = new HashSet<Integer>();
    private Set<String> redirects = new HashSet<String>();

    /** A no argument constructor as required by Hibernate. */
    public Page () {};

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

    public Set<Integer> getCategories() {
        return categories;
    }

    public void setCategories(Set<Integer> categories) {
        this.categories = categories;
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

    public int getOutDegree() {
        return outLinks.size();
    }

    public void setOutLinks(Set<Integer> outLinks) {
        this.outLinks = outLinks;
    }

    public Set<String> getRedirects() {
        return redirects;
    }

    public void setRedirects(Set<String> redirects) {
        this.redirects = redirects;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getIsDisambiguation() {
        return isDisambiguation;
    }

	public void setIsDisambiguation(Boolean isDisambiguation)
	{
		if (isDisambiguation == null) {
			isDisambiguation = false;
		}
		this.isDisambiguation = isDisambiguation;
	}
}
