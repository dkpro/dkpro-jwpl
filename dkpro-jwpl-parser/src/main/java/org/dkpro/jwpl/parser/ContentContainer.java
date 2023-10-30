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
 * A ContentContainer is used to combine more than one Content element (not only
 * ContentElement.class!) in a new Content element.<br>
 * For a description of the Functions of the Content Interface, take a look at
 * the Content.class documentation.<br>
 */
public abstract class ContentContainer extends ParsedPageObject implements Content {

  protected List<Content> ccl;

  public boolean empty() {
    return ccl.size() == 0;
  }

  public String getText() {

    StringBuilder result = new StringBuilder();
    for (Content cc : ccl) {
      if (cc != null) result.append(cc.getText() + " ");
    }

    final int temp = result.length() - 1;
    if (temp >= 0) result.deleteCharAt(temp);

    return result.toString();
  }

  /**
   * Returns the Text in the Span List in a String...<br>
   * all Spans must refer to the text returned by getText().
   */
  public String getText(List<Span> sl) {
    final String temp = getText();
    StringBuilder result = new StringBuilder();
    for (Span s : sl)
      result.append(s.getText(temp) + ' ');
    result.deleteCharAt(result.length() - 1);
    return result.toString();
  }

  public int length() {
    int length = 0;

    for (Content cc : ccl)
      if (cc != null) length += cc.length() + 1;

    if (length > 0) length--;

    return length;
  }

  /**
   * Retruns the Number of Content elements in this ContentContainer.
   */
  public int size() {
    return ccl.size();
  }

  public List<Span> getFormatSpans(FormatType t) {
    List<Span> result = new ArrayList<>();
    int offset = 0;
    for (Content c : ccl) {
      for (Span b : c.getFormatSpans(t))
        result.add(b.clone().adjust(offset));

      offset += 1 + c.length();
    }
    return result;
  }

  public List<Span> getFormatSpans(FormatType t, int start, int end) {
    return getFormatSpans(t, new Span(start, end));
  }

  public List<Span> getFormatSpans(FormatType t, Span s) {
    List<Span> result = new ArrayList<>();

    Span a = new Span(-1, -1);

    for (Content c : ccl) {
      int offset = a.getEnd() + 1;
      a = new Span(offset, offset + c.length());

      if (a.hits(s)) {
        for (Span b : c.getFormatSpans(t, s.clone().adjust(-offset)))
          result.add(b.clone().adjust(offset));
      }
    }
    return result;
  }

  public List<FormatType> getFormats() {

    boolean bold = false;
    boolean italic = false;
    boolean tag = false;
    boolean math = false;
    boolean nowiki = false;

    for (Content c : ccl) {

      for (FormatType t : c.getFormats())
        switch (t) {
          case BOLD:
            bold = true;
            break;
          case ITALIC:
            italic = true;
            break;
          case TAG:
            tag = true;
            break;
          case MATH:
            math = true;
            break;
          case NOWIKI:
            nowiki = true;
            break;
        }

      if (bold && italic && tag && math && nowiki) break;
    }

    List<FormatType> result = new ArrayList<>();
    if (bold) result.add(FormatType.BOLD);
    if (italic) result.add(FormatType.ITALIC);
    if (tag) result.add(FormatType.TAG);
    if (math) result.add(FormatType.MATH);
    if (nowiki) result.add(FormatType.NOWIKI);
    return result;
  }

  public List<FormatType> getFormats(int start, int end) {
    return getFormats(new Span(start, end));
  }

  public List<FormatType> getFormats(Span s) {
    boolean bold = false;
    boolean italic = false;
    boolean tag = false;
    boolean math = false;
    boolean nowiki = false;

    Span a = new Span(-1, -1);

    for (Content c : ccl) {
      int offset = a.getEnd() + 1;
      a = new Span(offset, offset + c.length());

      if (a.hits(s))
        for (FormatType t : c.getFormats(s.clone().adjust(-offset)))
          switch (t) {
            case BOLD:
              bold = true;
              break;
            case ITALIC:
              italic = true;
              break;
            case TAG:
              tag = true;
              break;
            case MATH:
              math = true;
              break;
            case NOWIKI:
              nowiki = true;
              break;
          }

      if (bold && italic) break;
    }

    List<FormatType> result = new ArrayList<>();
    if (bold) result.add(FormatType.BOLD);
    if (italic) result.add(FormatType.ITALIC);
    if (tag) result.add(FormatType.TAG);
    if (math) result.add(FormatType.MATH);
    if (nowiki) result.add(FormatType.NOWIKI);
    return result;
  }

  public List<Link> getLinks(Link.type linkType) {
    List<Link> result = new ArrayList<>();
    for (Content c : ccl) result.addAll(c.getLinks(linkType));
    return result;
  }

  public List<Link> getLinks(Link.type linkType, int start, int end) {
    return getLinks(linkType, new Span(start, end));
  }

  public List<Link> getLinks(Link.type linkType, Span s) {
    List<Link> result = new ArrayList<>();

    Span a = new Span(-1, -1);

    for (Content c : ccl) {
      int offset = a.getEnd() + 1;
      a = new Span(offset, offset + c.length());

      if (a.hits(s))
        result.addAll(c.getLinks(linkType, s.clone().adjust(-offset)));
    }
    return result;
  }

  public List<Link> getLinks() {
    List<Link> result = new ArrayList<>();
    for (Content c : ccl)
      result.addAll(c.getLinks());
    return result;
  }

  public List<Template> getTemplates() {
    List<Template> result = new ArrayList<>();
    for (Content cc : ccl)
      result.addAll(cc.getTemplates());
    return result;
  }

  public List<Template> getTemplates(int start, int end) {
    return getTemplates(new Span(start, end));
  }

  public List<Template> getTemplates(Span s) {
    List<Template> result = new ArrayList<>();

    Span a = new Span(-1, -1);

    for (Content c : ccl) {
      int offset = a.getEnd() + 1;
      a = new Span(offset, offset + c.length());

      if (a.hits(s))
        result.addAll(c.getTemplates(s.clone().adjust(-offset)));
    }
    return result;
  }
}
