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
package de.tudarmstadt.ukp.wikipedia.api;

import java.util.Comparator;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

/**
 * Compares two categories based on the lexicographic ordering of their titles.
 * @author zesch
 *
 */
public class CategoryTitleComparator implements Comparator<Category> {
        
    public int compare(Category o1, Category o2) {

        int retVal = 0;
        try {
            retVal = o1.getTitle().getPlainTitle().compareTo(o2.getTitle().getPlainTitle());
        } catch (WikiTitleParsingException e) {
            e.printStackTrace();
        }
        return retVal;
    }
}
