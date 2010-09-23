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

public class MetaData {

    private long id;

    private String language;
    private String disambiguationCategory;
    private String mainCategory;
    private String version;    
    
    private long nrofPages;
    private long nrofRedirects;
    private long nrofDisambiguationPages;
    private long nrofCategories;
    
    /** A no argument constructor as required by Hibernate. */
    public MetaData() {}

    public String getDisambiguationCategory() {
        return disambiguationCategory;
    }

    public void setDisambiguationCategory(String disambiguationCategory) {
        this.disambiguationCategory = disambiguationCategory;
    }

    public String getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    public long getNrofCategories() {
        return nrofCategories;
    }

    public long getNrofDisambiguationPages() {
        return nrofDisambiguationPages;
    }

    public long getNrofPages() {
        return nrofPages;
    }

    public long getNrofRedirects() {
        return nrofRedirects;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setNrofCategories(long nrofCategories) {
        this.nrofCategories = nrofCategories;
    }

    public void setNrofDisambiguationPages(long nrofDisambiguationPages) {
        this.nrofDisambiguationPages = nrofDisambiguationPages;
    }

    public void setNrofPages(long nrofPages) {
        this.nrofPages = nrofPages;
    }

    public void setNrofRedirects(long nrofRedirects) {
        this.nrofRedirects = nrofRedirects;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
