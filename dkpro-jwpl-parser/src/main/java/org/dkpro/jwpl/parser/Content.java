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

import java.util.List;

/**
 * This is a main Interface used by nearly all classes of this package.<br>
 * <br>
 * Be aware, that all retured Spans refer to the String returned by getText()<br>
 * this is true for any implementing class!<br>
 */
public interface Content {

  enum FormatType {
    /**
     * Bold Text
     */
    BOLD,
    /**
     * Italic Text
     */
    ITALIC,
    /**
     * The Content between Math Tags
     */
    MATH,
    /**
     * The Content between NoWiki Tags
     */
    NOWIKI,
    /**
     * The begin and end position of an unknown Tag defined by &lt; and &gt;
     */
    TAG,
  }

  /**
   * Returns the Text of the Element
   */
  String getText();

  /**
   * Content.getText().length() == Content.length()
   */
  int length();

  /**
   * Returns true, if there is no content in the element.
   */
  boolean empty();

  /**
   * returns the Format Spans of the Specified Type.
   */
  List<Span> getFormatSpans(FormatType t);

  /**
   * returns the Format Spans of the Specified Type, in the Range from start to end.
   */
  List<Span> getFormatSpans(FormatType t, int start, int end);

  /**
   * returns the Format Spans of the Specified Type, in the Range of s.
   */
  List<Span> getFormatSpans(FormatType t, Span s);

  /**
   * returns the Formats uses in this element.
   */
  List<FormatType> getFormats();

  /**
   * returns the Formats uses in this element, in the Range from start to end.
   */
  List<FormatType> getFormats(int start, int end);

  /**
   * returns the Formats uses in this element, in the Range of s.
   */
  List<FormatType> getFormats(Span s);

  /**
   * returns all Links of this element.
   */
  List<Link> getLinks();

  /**
   * returns all Links of this element of the specified type.
   */
  List<Link> getLinks(Link.type t);

  /**
   * returns all Links of this element of the specified type, in the Range from start to end.
   */
  List<Link> getLinks(Link.type t, int start, int end);

  /**
   * returns all Links of this element of the specified type, in the Range of s
   */
  List<Link> getLinks(Link.type t, Span s);

  /**
   * returns all Templates.
   */
  List<Template> getTemplates();

  /**
   * returns all Templates, in the Range from start to end.
   */
  List<Template> getTemplates(int start, int end);

  /**
   * returns all Templates, in the Range of s.
   */
  List<Template> getTemplates(Span s);
}
