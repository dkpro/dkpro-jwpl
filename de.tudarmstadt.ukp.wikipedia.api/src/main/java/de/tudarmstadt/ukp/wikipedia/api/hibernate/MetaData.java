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
