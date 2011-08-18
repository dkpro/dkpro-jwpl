/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api.hibernate;

import java.util.HashSet;
import java.util.Set;

/**
 * The page class that is actually persisted by Hibernate.
 * It is accessed via a equally named class in the api package to hide session management from the user.
 * @author zesch
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
