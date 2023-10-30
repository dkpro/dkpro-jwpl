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
package org.dkpro.jwpl.api;

import java.util.Comparator;

import org.dkpro.jwpl.api.exception.WikiTitleParsingException;

/**
 * Compares two pages based on the lexicographic ordering of their titles.
 */
public class PageTitleComparator implements Comparator<Page> {

  public int compare(Page o1, Page o2) {

    int retVal = 0;
    try {
      retVal = o1.getTitle().getPlainTitle().compareTo(o2.getTitle().getPlainTitle());
    } catch (WikiTitleParsingException e) {
      e.printStackTrace();
    }
    return retVal;
  }
}
