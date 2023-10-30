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
package org.dkpro.jwpl.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * A Section consists at first of a Title. In MediaWiki a Title can contain
 * e.g. Links, Images or ItalicText. Therefore a simple ContentElement
 * is used as Section Title.<br>
 * The next Point is the hirachical Section Level, which every Section has.<br>
 * <br>
 * Further, a Section can contain other Sections or Content, but not
 * both. This is a difference between the API and MediaWiki. In return, the
 * accsess to the Elements is possible with just a few functions. This fact
 * makes the accest to the provieded Structures very simple.<br>
 * <br>
 * These structure requirements are implemented as SectionContainer an SectionContent.
 */
public abstract class Section extends ContentContainer {

  private int level;
  private ContentElement title;

  public Section(ContentElement title, int level) {
    this.ccl = new ArrayList<>();
    this.level = level;
    this.title = title;
    if (title != null) ccl.add(title);
  }

  /**
   * Look at getLevel() for Details...
   */
  public void setLevel(int level) {
    this.level = level;
  }

  /**
   * Retruns the hirachical Level of this Section.
   */
  public int getLevel() {
    return level;
  }

  /**
   * Returns getTitleElement().getText() without NullPointerException
   */
  public String getTitle() {
    if (title != null)
      return title.getText();
    else
      return null;
  }

  /**
   * Look at getTitleElement() for Details...
   */
  public void setTitleElement(ContentElement title) {
    if (title != null) {
      if (this.title == null) ccl.add(0, title);
      else ccl.set(0, title);
    } else if (this.title != null) ccl.remove(this.title);

    this.title = title;
  }

  /**
   * Returns a ContentElement representing the content, originally given as
   * MediaWiki SourcCode, beween one ore more equality chars at the beginning
   * of a line. This is known as Title.
   */
  public ContentElement getTitleElement() {
    return title;
  }

  /**
   * Return a List with all Content of any Type in Order of appearance.
   */
  public abstract List<Content> getContentList();

  /**
   * Returns the Number of Paragraphs in this Section.
   */
  public abstract int nrOfParagraphs();

  /**
   * Returns the i-th Paragraph of this Section.
   */
  public abstract Paragraph getParagraph(int i);

  /**
   * Retuns a List of all Paragraphs of this Section.
   */
  public abstract List<Paragraph> getParagraphs();

  /**
   * Returns the Number of Tables of this Section.
   */
  public abstract int nrOfTables();

  /**
   * Returns the i-th Table of this Section.
   */
  public abstract Table getTable(int i);

  /**
   * Returns a List of all Tables of this Section.
   */
  public abstract List<Table> getTables();

  /**
   * Returns the Number of NestedLists of this Section.
   */
  public abstract int nrOfNestedLists();

  /**
   * Returns the i-th NestedList of this Section as NestedListContainer.
   */
  public abstract NestedListContainer getNestedList(int i);

  /**
   * Returns a List of all NestedLists of this Section.
   */
  public abstract List<NestedListContainer> getNestedLists();

  /**
   * Returns the Number of DefinitionLists of this Section.
   */
  public abstract int nrOfDefinitionLists();

  /**
   * Returns the i-th Table of this Section.
   */
  public abstract DefinitionList getDefinitionList(int i);

  /**
   * Returns a List of all DefinitionLists of this Section.
   */
  public abstract List<DefinitionList> getDefinitionLists();

  /**
   * Returns a sequence of Chars followed by ZERO.
   * For easy handling the result is of the Type String.
   */
  public abstract String toString();
}
