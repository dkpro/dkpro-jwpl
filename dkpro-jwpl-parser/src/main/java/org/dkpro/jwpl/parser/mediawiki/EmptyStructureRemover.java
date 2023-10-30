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
package org.dkpro.jwpl.parser.mediawiki;

import org.dkpro.jwpl.parser.ContentElement;
import org.dkpro.jwpl.parser.DefinitionList;
import org.dkpro.jwpl.parser.NestedList;
import org.dkpro.jwpl.parser.NestedListContainer;
import org.dkpro.jwpl.parser.Paragraph;
import org.dkpro.jwpl.parser.Section;
import org.dkpro.jwpl.parser.SectionContainer;
import org.dkpro.jwpl.parser.SectionContent;
import org.dkpro.jwpl.parser.Table;
import org.dkpro.jwpl.parser.TableElement;


/**
 * It is possible that some Elements which has been parsed are empty after
 * the Parsing process becaus of the options which has been set. This class
 * can remove these empty elmentens.
 */
class EmptyStructureRemover {

  /**
   * Removes all empty Structures from a SectionContainer and all substructures.
   */
  public static SectionContainer eliminateEmptyStructures(SectionContainer sc) {

    for (int i = sc.nrOfSubSections() - 1; i >= 0; i--) {
      Section ss = sc.getSubSection(i);

      if (ss.getClass() == SectionContainer.class) {
        SectionContainer sci = (SectionContainer) ss;
        eliminateEmptyStructures(sci);
      } else if (ss.getClass() == SectionContent.class)
        eliminateEmptyStructures((SectionContent) ss);

      if (ss.empty()) sc.removeSection(ss);
    }

    //encapsulating Sections
    if (sc.nrOfSubSections() == 1 && sc.getSubSection(0).getClass() == SectionContainer.class) {
      SectionContainer sc0 = (SectionContainer) sc.getSubSection(0);
      if (sc0.getTitleElement() == null) {
        sc.removeSection(sc0);
        for (int i = 0; i < sc0.nrOfSubSections(); i++)
          sc.addSection(sc0.getSubSection(i));
      }
    }

    return sc;
  }

  /**
   * Removes all empty Structures from a SectionContent and all substructures.
   */
  public static SectionContent eliminateEmptyStructures(SectionContent sc) {

    for (int i = sc.nrOfParagraphs() - 1; i >= 0; i--) {
      Paragraph p = sc.getParagraph(i);
      if (p.empty()) sc.removeParagraph(p);
    }

    for (int i = sc.nrOfDefinitionLists() - 1; i >= 0; i--) {
      DefinitionList dl = sc.getDefinitionList(i);
      eliminateEmptyStructures(dl);
      if (dl.empty()) sc.removeDefinitionList(dl);
    }

    for (int i = sc.nrOfNestedLists() - 1; i >= 0; i--) {
      NestedListContainer nl = sc.getNestedList(i);
      eliminateEmptyStructures(nl);
      if (nl.empty()) sc.removeNestedList(nl);
    }

    for (int i = sc.nrOfTables() - 1; i >= 0; i--) {
      Table t = sc.getTable(i);
      eliminateEmptyStructures(t);
      if (t.empty()) sc.removeTable(t);
    }

    return sc;
  }

  /**
   * Removes all empty Structures from a NestedListContainer and all substructures.
   */
  public static NestedListContainer eliminateEmptyStructures(NestedListContainer nlc) {
    for (int i = nlc.size() - 1; i >= 0; i--) {
      NestedList nl = nlc.getNestedList(i);
      if (nl.getClass() == NestedListContainer.class)
        eliminateEmptyStructures((NestedListContainer) nl);

      if (nl.empty()) nlc.remove(nl);
    }
    return nlc;
  }

  /**
   * Removes all empty Structures from a Table and all substructures.
   */
  public static Table eliminateEmptyStructures(Table t) {
    for (int i = t.nrOfTableElements() - 1; i >= 0; i--) {
      TableElement te = t.getTableElement(i);
      eliminateEmptyStructures(te);
      if (te.empty()) t.removeTableElement(te);
    }
    return t;
  }

  /**
   * Removes all empty Structures from a TableElement and all substructures.
   */
  public static TableElement eliminateEmptyStructures(TableElement te) {
    for (int i = te.nrOfSections() - 1; i >= 0; i--) {
      Section s = te.getSection(i);

      if (s.getClass() == SectionContainer.class)
        eliminateEmptyStructures((SectionContainer) s);
      else if (s.getClass() == SectionContent.class)
        eliminateEmptyStructures((SectionContent) s);

      if (s.empty()) te.removeSection(s);
    }
    return te;
  }

  /**
   * Removes all empty Structures from a DefinitionList and all substructures.
   */
  public static DefinitionList eliminateEmptyStructures(DefinitionList dl) {

    ContentElement dt = dl.getDefinedTerm();
    if (dt != null && dt.empty()) dl.setDefinedTerm(null);

    for (int i = dl.nrOfDefinitions() - 1; i >= 0; i--) {
      ContentElement ce = dl.getDefinition(i);
      if (ce.empty()) dl.removeDefinition(ce);
    }
    return dl;
  }
}
