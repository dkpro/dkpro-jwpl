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
