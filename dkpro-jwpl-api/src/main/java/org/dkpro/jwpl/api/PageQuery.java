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

/**
 * Represents a query for retrieving pages that meet the given conditions.
 * Conditions are represented by the fields of a query object.
 */
public class PageQuery implements WikiConstants {

  /**
   * Whether only article pages should be retrieved.
   */
  private boolean onlyArticlePages;

  /**
   * Whether only disambiguation pages should be retrieved.
   */
  private boolean onlyDisambiguationPages;

  /**
   * A regular expression style titlePattern for the page's title
   */
  private String titlePattern;

  /**
   * The minimum in-degree of the page
   */
  private int minIndegree;
  /**
   * The maximum out-degree of the page
   */
  private int maxIndegree;

  /**
   * The minimum out-degree of the page
   */
  private int minOutdegree;
  /**
   * The maximum out-degree of the page
   */
  private int maxOutdegree;

  /**
   * The minimum number of redirects of the page
   */
  private int minRedirects;
  /**
   * The maximum number of redirects of the page
   */
  private int maxRedirects;

  /**
   * The minimum number of categories of the page
   */
  private int minCategories;
  /**
   * The maximum number of categories of the page
   */
  private int maxCategories;

  /**
   * The minimum number of tokens in the page
   */
  private int minTokens;
  /**
   * The minimum number of tokens in the page
   */
  private int maxTokens;

  public PageQuery() {
    onlyDisambiguationPages = false;

    titlePattern = "";

    minIndegree = 0;
    maxIndegree = Integer.MAX_VALUE;

    minOutdegree = 0;
    maxOutdegree = Integer.MAX_VALUE;

    minRedirects = 0;
    maxRedirects = Integer.MAX_VALUE;

    minCategories = 0;
    maxCategories = Integer.MAX_VALUE;

    minTokens = 0;
    maxTokens = Integer.MAX_VALUE;

  }

  protected int getMaxCategories() {
    return maxCategories;
  }

  protected int getMaxIndegree() {
    return maxIndegree;
  }

  protected int getMaxOutdegree() {
    return maxOutdegree;
  }

  protected int getMaxRedirects() {
    return maxRedirects;
  }

  protected int getMinCategories() {
    return minCategories;
  }

  protected int getMinIndegree() {
    return minIndegree;
  }

  protected int getMinOutdegree() {
    return minOutdegree;
  }

  protected int getMinRedirects() {
    return minRedirects;
  }

  protected boolean onlyArticlePages() {
    return onlyArticlePages;
  }

  protected boolean onlyDisambiguationPages() {
    return onlyDisambiguationPages;
  }

  protected int getMinTokens() {
    return minTokens;
  }

  protected int getMaxTokens() {
    return maxTokens;
  }

  protected String getTitlePattern() {
    return titlePattern;
  }

  /**
   * Sets the minimum number of categories that queried articles should have.
   *
   * @param minCategories The minimum number of categories.
   */
  public void setMinCategories(int minCategories) {
    this.minCategories = minCategories;
  }

  /**
   * Sets the maximum number of categories that queried articles should have.
   *
   * @param maxCategories The maximum number of categories.
   */
  public void setMaxCategories(int maxCategories) {
    this.maxCategories = maxCategories;
  }

  /**
   * Sets the minimum number of ingoing links that queried articles should have.
   *
   * @param minIndegree The minimum number of ingoing links.
   */
  public void setMinIndegree(int minIndegree) {
    this.minIndegree = minIndegree;
  }

  /**
   * Sets the maximum number of ingoing links that queried articles should have.
   *
   * @param maxIndegree The maximum number of ingoing links.
   */
  public void setMaxIndegree(int maxIndegree) {
    this.maxIndegree = maxIndegree;
  }

  /**
   * Sets the minimum number of outgoing links that queried articles should have.
   *
   * @param minOutdegree The minimum number of outgoing links.
   */
  public void setMinOutdegree(int minOutdegree) {
    this.minOutdegree = minOutdegree;
  }

  /**
   * Sets the maximum number of outgoing links that queried articles should have.
   *
   * @param maxOutdegree The maximum number of outgoing links.
   */
  public void setMaxOutdegree(int maxOutdegree) {
    this.maxOutdegree = maxOutdegree;
  }

  /**
   * Sets the minimum number of redirects that queried articles should have.
   *
   * @param minRedirects The minimum number of redirects.
   */
  public void setMinRedirects(int minRedirects) {
    this.minRedirects = minRedirects;
  }

  /**
   * Sets the maximum number of redirects that queried articles should have.
   *
   * @param maxRedirects The maximum number of redirects.
   */
  public void setMaxRedirects(int maxRedirects) {
    this.maxRedirects = maxRedirects;
  }

  /**
   * Sets whether only be articles should be retrieved.
   *
   * @param onlyArticlePages If set to true, only article pages are returned.
   */
  public void setOnlyArticlePages(boolean onlyArticlePages) {
    this.onlyArticlePages = onlyArticlePages;
  }

  /**
   * Sets whether only disambiguation pages should be retrieved.
   *
   * @param onlyDisambiguationPages If set to true, only disambiguation pages are returned.
   */
  public void setOnlyDisambiguationPages(boolean onlyDisambiguationPages) {
    this.onlyDisambiguationPages = onlyDisambiguationPages;
  }

  /**
   * Sets the minimum number of tokens that queried articles should have.
   *
   * @param minTokens The minimum number of tokens.
   */
  public void setMinTokens(int minTokens) {
    this.minTokens = minTokens;
  }

  /**
   * Sets the maximum number of tokens that queried articles should have.
   *
   * @param maxTokens The maximum number of tokens.
   */
  public void setMaxTokens(int maxTokens) {
    this.maxTokens = maxTokens;
  }

  /**
   * Sets a regular expression that pages have to match.
   * % for any number of arbitrary characters (can only be used at the end of a string)
   * _ for a single arbitrary character (can also be used inside a string)
   *
   * @param pattern A regular expression pattern.
   */
  public void setTitlePattern(String pattern) {
    this.titlePattern = pattern;
  }

  /**
   * @return A string that shows the current values of the query members.
   */
  public String getQueryInfo() {
    StringBuilder sb = new StringBuilder();

    sb.append("MaxCategories: ").append(maxCategories).append(LF);
    sb.append("MinCategories: ").append(minCategories).append(LF);
    sb.append("MaxIndegree:   ").append(maxIndegree).append(LF);
    sb.append("MinIndegree:   ").append(minIndegree).append(LF);
    sb.append("MaxOutdegree:  ").append(maxOutdegree).append(LF);
    sb.append("MinOutdegree:  ").append(minOutdegree).append(LF);
    sb.append("MaxRedirects:  ").append(maxRedirects).append(LF);
    sb.append("MinRedirects:  ").append(minRedirects).append(LF);
    sb.append("MaxTokens:     ").append(maxTokens).append(LF);
    sb.append("MinTokens:     ").append(minTokens).append(LF);
    sb.append("Only article pages:        ").append(onlyArticlePages).append(LF);
    sb.append("Only disambiguation pages: ").append(onlyDisambiguationPages).append(LF);
    sb.append("Title pattern: ").append(titlePattern).append(LF);

    return sb.toString();
  }
}
