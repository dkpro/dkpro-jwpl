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
package org.dkpro.jwpl.datamachine.dump.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.stream.Stream;

import org.dkpro.jwpl.datamachine.domain.DataMachineFiles;
import org.dkpro.jwpl.wikimachine.debug.Slf4JLogger;
import org.dkpro.jwpl.wikimachine.domain.MetaData;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersionFactory;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Feeds a fixed set of page and text rows through {@link SingleDumpVersionJDKGeneric}
 * and pins both the lookup results and the exact content of the tables written by the page and the
 * text pass. The expected content was recorded with the boxed JDK collections used before issue
 * #554, so the primitive collections used since then have to reproduce it byte for byte. Since
 * issue #555 the text rows are keyed by page id rather than by revision id, which must not change
 * that content either.
 */
class SingleDumpVersionJDKGenericTest {

  private static final String CATEGORY = "5\t5\tTop_Level\n";

  // the disambiguation flag is written as a single 0x01 byte for true and as nothing for false
  private static final String PAGE = "1\t1\tMain_Page\tWelcome\t\n"
          + "2\t2\tDiscussion:Main_Page\tLet us talk\t\n"
          + "9\t9\tMercury\tMercury may refer to\t\u0001\n";

  private static final String PAGE_MAP_LINE = "1\tMain_Page\t1\tNULL\tNULL\n"
          + "2\tDiscussion:Main_Page\t2\tNULL\tNULL\n"
          + "3\tMain_page\t1\tNULL\tNULL\n"
          + "9\tMercury\t9\tNULL\tNULL\n";

  private static final String PAGE_REDIRECTS = "1\tMain_page\n";

  static Stream<IDumpVersionFactory> factories() {
    return Stream.of(new SingleDumpVersionJDKStringKeyFactory(),
            new SingleDumpVersionJDKIntKeyFactory(), new SingleDumpVersionJDKLongKeyFactory());
  }

  @ParameterizedTest
  @MethodSource("factories")
  void producesIdenticalTablesAndLookups(IDumpVersionFactory factory, @TempDir Path outputDirectory)
          throws IOException {
    DataMachineFiles files = new DataMachineFiles(new Slf4JLogger());
    files.setOutputDirectory(outputDirectory.toAbsolutePath().toString());
    MetaData metaData = new MetaData();

    SingleDumpVersionJDKGeneric<?, ?> version =
            (SingleDumpVersionJDKGeneric<?, ?>) factory.getDumpVersion();
    version.setFiles(files);
    version.setMetaData(metaData);
    version.initialize(new Timestamp(0));

    // page pass
    version.initPageParsing();
    version.processPageRow(new FixedPageParser(1, 0, "Main_Page", false));
    version.processPageRow(new FixedPageParser(2, 1, "Main_Page", false));
    version.processPageRow(new FixedPageParser(3, 0, "Main_page", true));
    version.processPageRow(new FixedPageParser(4, 0, "Broken", true));
    version.processPageRow(new FixedPageParser(5, 14, "Top_Level", false));
    version.processPageRow(new FixedPageParser(6, 14, "Redirected_Cat", true));
    version.processPageRow(new FixedPageParser(7, 2, "Someone", false));
    version.processPageRow(new FixedPageParser(8, 0, null, false));
    version.processPageRow(new FixedPageParser(9, 0, "Mercury", false));
    version.exportAfterPageParsing();
    version.freeAfterPageParsing();
    assertEquals(1, metaData.getNrOfCategories());
    assertEquals(5, metaData.getNrOfPages());

    // lookups used by the categorylinks and pagelinks passes
    assertEquals(Integer.valueOf(1), version.pageIdByTitle("Main_Page"));
    assertEquals(Integer.valueOf(2), version.pageIdByTitle("Discussion:Main_Page"));
    assertEquals(Integer.valueOf(9), version.pageIdByTitle("Mercury"));
    assertNull(version.pageIdByTitle("Main_page"));
    assertNull(version.pageIdByTitle("Top_Level"));
    assertNull(version.pageIdByTitle("Nowhere"));
    assertEquals(Integer.valueOf(5), version.categoryIdByTitle("Top_Level"));
    assertNull(version.categoryIdByTitle("Redirected_Cat"));
    assertNull(version.categoryIdByTitle("Main_Page"));
    assertTrue(version.isKnownArticleId(1));
    assertTrue(version.isKnownArticleId(2));
    assertFalse(version.isKnownArticleId(3));
    assertFalse(version.isKnownArticleId(5));
    assertFalse(version.isKnownArticleId(7));
    assertTrue(version.isKnownCategoryId(5));
    assertFalse(version.isKnownCategoryId(6));
    assertFalse(version.isKnownCategoryId(1));
    version.recordDisambiguation(9);
    version.freeAfterCategoryLinksParsing();
    version.freeAfterPageLinksParsing();

    // text pass, keyed by page id, including a category (5), a page that is never imported (7)
    // and a page id unknown to the page table (999)
    version.initTextParsing();
    version.processTextRow(new FixedTextParser(1, "Welcome"));
    version.processTextRow(new FixedTextParser(2, "Let us talk"));
    version.processTextRow(new FixedTextParser(3, "#REDIRECT [[Main Page]]"));
    version.processTextRow(new FixedTextParser(4, "#REDIRECT [[Nowhere]]"));
    version.processTextRow(new FixedTextParser(5, "Category text"));
    version.processTextRow(new FixedTextParser(7, "User page"));
    version.processTextRow(new FixedTextParser(9, "Mercury may refer to"));
    version.processTextRow(new FixedTextParser(999, "Orphaned text"));
    version.exportAfterTextParsing();
    version.freeAfterTextParsing();
    assertEquals(1, metaData.getNrOfDisambiguations());
    assertEquals(1, metaData.getNrOfRedirects());

    assertEquals(CATEGORY, read(files.getOutputCategory()));
    assertEquals(PAGE, read(files.getOutputPage()));
    assertEquals(PAGE_MAP_LINE, read(files.getOutputPageMapLine()));
    assertEquals(PAGE_REDIRECTS, read(files.getOutputPageRedirects()));
  }

  private static String read(String file) throws IOException {
    return Files.readString(Path.of(file), StandardCharsets.UTF_8);
  }

  private static final class FixedPageParser extends PageParser {

    private final int id;
    private final int namespace;
    private final String title;
    private final boolean redirect;

    private FixedPageParser(int id, int namespace, String title, boolean redirect) {
      this.id = id;
      this.namespace = namespace;
      this.title = title;
      this.redirect = redirect;
    }

    @Override
    public int getPageId() {
      return id;
    }

    @Override
    public int getPageNamespace() {
      return namespace;
    }

    @Override
    public String getPageTitle() {
      return title;
    }

    @Override
    public boolean getPageIsRedirect() {
      return redirect;
    }
  }

  private static final class FixedTextParser extends TextParser {

    private final int id;
    private final String text;

    private FixedTextParser(int id, String text) {
      this.id = id;
      this.text = text;
    }

    @Override
    public int getOldId() {
      return id;
    }

    @Override
    public String getOldText() {
      return text;
    }
  }
}
