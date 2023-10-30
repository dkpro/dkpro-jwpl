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
package org.dkpro.jwpl.timemachine.dump.version;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dkpro.jwpl.timemachine.domain.Revision;
import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;
import org.dkpro.jwpl.wikimachine.dump.version.AbstractDumpVersion;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;
import org.dkpro.jwpl.wikimachine.hashing.IStringHashCode;
import org.dkpro.jwpl.wikimachine.util.Redirects;
import org.dkpro.jwpl.wikimachine.util.TimestampUtil;
import org.dkpro.jwpl.wikimachine.util.TxtFileWriter;

/**
 * <i>Please be sure, that {@code hashCode(String)} of the provided HashAlgorithm type returns the
 * same type as KeyType</i>
 *
 * @param <KeyType>       the type of the HashMap's key
 * @param <HashAlgorithm> hashing algorithm, returning KeyType <br>
 */
public class DumpVersionJDKGeneric<KeyType, HashAlgorithm extends IStringHashCode> extends AbstractDumpVersion {

  private static final String SQL_NULL = "NULL";

  /**
   * maps page id's to Revision objects
   */
  private HashMap<Integer, Long> pageIdRevMap;
  /**
   * after revision parsing the map will be erased and the keys sorted in the
   * array list
   */
  private Set<Integer> pageIdRevList;

  /**
   * caches the page id's of disambiguation pages.
   */
  private Set<Integer> disambiguations;
  /**
   * maps text id's to the page id's.
   */
  private Map<Integer, Integer> textIdPageIdMap;
  /**
   * maps page id's of pages to their names
   */
  private Map<Integer, String> pPageIdNameMap;
  /**
   * maps names of pages to their page id's.
   */
  private Map<KeyType, Integer> pNamePageIdMap;

  /**
   * maps names of categories to their page id's.
   */
  private Map<KeyType, Integer> cNamePageIdMap;

  /**
   * maps page id's of redirects to their names.
   */
  private Map<Integer, String> rPageIdNameMap;

  private final IStringHashCode hashAlgorithm;

  @SuppressWarnings("unchecked")
  public DumpVersionJDKGeneric(Class<HashAlgorithm> hashAlgorithmClass)
          throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

    hashAlgorithm = hashAlgorithmClass.getDeclaredConstructor().newInstance();
    @SuppressWarnings("unused")
    KeyType hashAlgorithmResult = (KeyType) hashAlgorithm.hashCode("test");
  }

  @Override
  public void freeAfterCategoryLinksParsing() {
    logger.log("clearing cNamePageIdMap of " + cNamePageIdMap.size() + " objects");
    cNamePageIdMap.clear();
  }

  @Override
  public void freeAfterPageLinksParsing() {
    // nothing to free
  }

  @Override
  public void freeAfterPageParsing() {
    logger.log("clearing pageIdRevSet of " + pageIdRevList.size() + " objects");
    pageIdRevList.clear();
  }

  @Override
  public void freeAfterRevisonParsing() {
    pageIdRevList = new HashSet<>(pageIdRevMap.keySet().size());
    pageIdRevList.addAll(pageIdRevMap.keySet());
    pageIdRevMap.clear();
  }

  @Override
  public void freeAfterTextParsing() {
    pageIdRevMap.clear();
    pageIdRevList.clear();
    disambiguations.clear();
    textIdPageIdMap.clear();
    pPageIdNameMap.clear();
    pNamePageIdMap.clear();
    cNamePageIdMap.clear();
    rPageIdNameMap.clear();
  }

  @Override
  public void initialize(Timestamp timestamp) {
    this.timestamp = Revision.compressTime(timestamp.getTime());

    /*
     * filled in revisions
     */
    pageIdRevMap = new HashMap<>();
    textIdPageIdMap = new HashMap<>();

    /*
     * filled in pages
     */
    pPageIdNameMap = new HashMap<>();
    pNamePageIdMap = new HashMap<>();

    cNamePageIdMap = new HashMap<>();
    rPageIdNameMap = new HashMap<>();

    /*
     * filled in categories
     */
    disambiguations = new HashSet<>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void processCategoryLinksRow(CategorylinksParser clParser)
          throws IOException {
    String cl_to_text = clParser.getClTo();
    if (cl_to_text != null) {
      KeyType cl_to_textHashcode = (KeyType) hashAlgorithm.hashCode(cl_to_text);
      // if category exists

      Integer cl_to = cNamePageIdMap.get(cl_to_textHashcode);
      if (cl_to != null) {
        // if the link source is a page then write the link in
        // category_pages and page_categories
        int cl_from = clParser.getClFrom();
        // if exists page
        if (pPageIdNameMap.containsKey(cl_from)) {
          processCategoryLinksRowPageExists(cl_from, cl_to, cl_to_text);
        } else {
          processCateforyLinksRowPageMiss(cl_from, cl_to);
        }
      }
    }
  }

  private void processCategoryLinksRowPageExists(Integer cl_from, Integer cl_to, String cl_to_text) throws IOException {
    categoryPages.addRow(cl_to, cl_from);
    pageCategories.addRow(cl_from, cl_to);
    if (cl_to_text.equals(metaData.getDisambiguationCategory())) {
      disambiguations.add(cl_from);
      metaData.addDisamb();
    }
  }

  private void processCateforyLinksRowPageMiss(Integer cl_from, Integer cl_to) throws IOException {
    // if category page id exists
    if (cNamePageIdMap.containsValue(cl_from)) {
      categoryOutlinks.addRow(cl_to, cl_from);
      categoryInlinks.addRow(cl_from, cl_to);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void processPageLinksRow(PagelinksParser plParser) throws IOException {
    int pl_from = plParser.getPlFrom();
    String pl_to = plParser.getPlTo();
    if (pl_to != null) {
      KeyType pl_toHashcode = (KeyType) hashAlgorithm.hashCode(pl_to);

      // if page name and page page id exists
      Integer id = pNamePageIdMap.get(pl_toHashcode);
      if (id != null
              && (!skipPage || pPageIdNameMap.containsKey(pl_from))) {
        pageOutlinks.addRow(pl_from, id);
        pageInlinks.addRow(id, pl_from);
      }
    }
  }

  @Override
  public void processPageRow(PageParser pageParser) throws IOException {
    switch (pageParser.getPageNamespace()) {
      case NS_CATEGORY: {
        processPageRowCategory(pageParser);
        break;
      }
      case NS_MAIN: {
        processPageRowPage(pageParser);
        break;
      }
    }

  }

  @SuppressWarnings("unchecked")
  private void processPageRowCategory(PageParser pageParser) throws IOException {
    if (!(skipCategory && pageParser.getPageIsRedirect())) {
      // retrieve page id and page title
      int page_id = pageParser.getPageId();
      // ignore categories, which have no revisions before the time stamp
      if (pageIdRevList.contains(page_id)) {
        String page_title = pageParser.getPageTitle();
        // cache the retrieved values
        // record category
        if (page_title != null) {
          KeyType page_titleHashcode = (KeyType) hashAlgorithm
                  .hashCode(page_title);
          cNamePageIdMap.put(page_titleHashcode, page_id);
          // write a new row in the table Category.
          // Note that we also consider the page_id as id
          txtFW.addRow(page_id, page_id, page_title);
          metaData.addCategory();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void processPageRowPage(PageParser pageParser) throws IOException {
    // retrieve page id and title
    int page_id = pageParser.getPageId();
    // ignore pages, which have no revisions prior to the time stamp
    String page_title = pageParser.getPageTitle();
    if (page_title != null && pageIdRevList.contains(page_id)) {
      // distinguish redirects
      if (pageParser.getPageIsRedirect()) {
        // record redirect
        rPageIdNameMap.put(page_id, page_title);
      } else {
        // record page
        KeyType page_titleHashcode = (KeyType) hashAlgorithm
                .hashCode(page_title);
        pPageIdNameMap.put(page_id, page_title);
        pNamePageIdMap.put(page_titleHashcode, page_id);
      }
    }
  }

  @Override
  public void processRevisionRow(RevisionParser revisionParser) {
    // get the time stamp of the revision
    int rev_timestamp = revisionParser.getRevTimestamp();
    if (rev_timestamp < timestamp) {
      // get the rev_page (corresponds to page_id in the table page)
      int rev_page = revisionParser.getRevPage();
      if (pageIdRevMap.containsKey(rev_page)) {
        processRevisionRowContainsKey(revisionParser, rev_page,
                rev_timestamp);
      } else {
        processRevisionRowMissKey(revisionParser, rev_page,
                rev_timestamp);
      }
    }

  }

  private void processRevisionRowContainsKey(RevisionParser revisionParser, int rev_page, int rev_timestamp) {

    long revisionRecord = pageIdRevMap.get(rev_page);
    int old_timestamp = Revision.getTimestamp(revisionRecord);

    // is it a better time stamp ?
    if (rev_timestamp > old_timestamp) {
      int old_text_id = Revision.getTextId(revisionRecord);
      pageIdRevMap.put(rev_page, Revision.createRevision(revisionParser
              .getRevTextId(), rev_timestamp));
      textIdPageIdMap.remove(old_text_id);
      textIdPageIdMap.put(revisionParser.getRevTextId(), rev_page);
    }
  }

  private void processRevisionRowMissKey(RevisionParser revisionParser, int rev_page, int rev_timestamp) {
    // this is the first recorded time stamp for that page id
    pageIdRevMap.put(rev_page, Revision.createRevision(revisionParser.getRevTextId(), rev_timestamp));
    textIdPageIdMap.put(revisionParser.getRevTextId(), rev_page);
  }

  @Override
  public void processTextRow(TextParser textParser) throws IOException {
    int text_id = textParser.getOldId();

    if (textIdPageIdMap.containsKey(text_id)) {
      int page_id = textIdPageIdMap.get(text_id);
      // if exists page page id -> page
      if (pPageIdNameMap.containsKey(page_id)) {
        processTextRowPage(textParser, page_id);
      } else if (rPageIdNameMap.containsKey(page_id)) {
        // if exists redirect -> redirect
        processTextRowRedirect(textParser, page_id);
      }
    }

  }

  private void processTextRowPage(TextParser textParser, int page_id) throws IOException {
    // get page name
    String pageName = pPageIdNameMap.get(page_id);

    page.addRow(page_id, page_id, pageName, textParser.getOldText(), formatBoolean(disambiguations.contains(page_id)));
    pageMapLine.addRow(page_id, pageName, page_id, SQL_NULL, SQL_NULL);
    metaData.addPage();
  }

  @SuppressWarnings("unchecked")
  private void processTextRowRedirect(TextParser textParser, int page_id) throws IOException {
    String destination = Redirects.getRedirectDestination(textParser.getOldText());

    if (destination != null) {
      // if page name exists

      KeyType destinationHashcode = (KeyType) hashAlgorithm.hashCode(destination);
      Integer id = pNamePageIdMap.get(destinationHashcode);
      if (id != null) {
        String redirectName = rPageIdNameMap.get(page_id);
        pageRedirects.addRow(id, redirectName);
        pageMapLine.addRow(page_id, redirectName, id, SQL_NULL, SQL_NULL);
        metaData.addRedirect();
      }
    }
  }

  @Override
  public void writeMetaData() throws IOException {
    TxtFileWriter outputFile = new TxtFileWriter(versionFiles.getOutputMetadata());
    // ID,LANGUAGE,DISAMBIGUATION_CATEGORY,MAIN_CATEGORY,nrOfPages,nrOfRedirects,nrOfDisambiguationPages,nrOfCategories,timestamp
    outputFile.addRow(metaData.getId(), metaData.getLanguage(), metaData.getDisambiguationCategory(),
            metaData.getMainCategory(), metaData.getNrOfPages(), metaData.getNrOfRedirects(),
            metaData.getNrOfDisambiguations(), metaData.getNrOfCategories(),
            TimestampUtil.toMediaWikiString(metaData.getTimestamp()));
    outputFile.flush();
    outputFile.close();
  }

}
