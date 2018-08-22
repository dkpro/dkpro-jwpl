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
package de.tudarmstadt.ukp.wikipedia.api;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An iterator over {@link Page} objects selected by a query.
 *
 */
public class PageQueryIterator implements Iterator<Page> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Wikipedia wiki;
    private int iterPosition;
    private List<Integer> pageIDs;

    public PageQueryIterator(Wikipedia wiki, List<Integer> pPageIDs) {
        this.wiki = wiki;
        this.iterPosition = 0;
        this.pageIDs = pPageIDs;
    }

    public boolean hasNext() {
        if (iterPosition < this.pageIDs.size()) {
            return true;
        }
        else {
            return false;
        }
    }

    public Page next() {
        Page page = null;
        try {
            page = this.wiki.getPage(pageIDs.get(iterPosition));
        } catch (WikiApiException e) {
            logger.error("Could not load page with id {}", pageIDs.get(iterPosition), e);
        }
        iterPosition++;
        return page;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
