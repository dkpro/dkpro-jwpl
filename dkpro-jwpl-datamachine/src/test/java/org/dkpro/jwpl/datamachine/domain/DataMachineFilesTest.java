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
package org.dkpro.jwpl.datamachine.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.dkpro.jwpl.datamachine.factory.DefaultDataMachineEnvironmentFactory;
import org.dkpro.jwpl.wikimachine.factory.IEnvironmentFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DataMachineFilesTest {

  private static final IEnvironmentFactory factory = DefaultDataMachineEnvironmentFactory.getInstance();

  private static final URL BASE = DataMachineFilesTest.class.getProtectionDomain().getCodeSource().getLocation();
  private static final String TARGET = BASE.getFile().replace("test-classes/","");
  private static final String TEST_OUTPUT_DIR = TARGET + "test-output/";
  private static final String OUTPUT_DIR = TEST_OUTPUT_DIR + "output";

  // SUT
  private DataMachineFiles dmFiles;

  @BeforeAll
  public static void initEnv() throws IOException {
    Files.createDirectories(Path.of(OUTPUT_DIR));
    File mockPageLinks = Path.of(TEST_OUTPUT_DIR, "pagelinks.sql.gz").toFile();
    if (!mockPageLinks.exists()) {
      assertTrue(mockPageLinks.createNewFile());
    }
    File mockCategoryLinks = Path.of(TEST_OUTPUT_DIR, "categorylinks.sql.gz").toFile();
    if (!mockCategoryLinks.exists()) {
      assertTrue(mockCategoryLinks.createNewFile());
    }
    File mockPagesArticles = Path.of(TEST_OUTPUT_DIR, "pages-articles.xml.bz2").toFile();
    if (!mockPagesArticles.exists()) {
      assertTrue(mockPagesArticles.createNewFile());
    }
  }

  @BeforeEach
  void setUp() {
    dmFiles = new DataMachineFiles(factory.getLogger());
    dmFiles.setDataDirectory(TEST_OUTPUT_DIR);
  }

  @Test
  void testCopyConstructor() {
    DataMachineFiles copy = new DataMachineFiles(dmFiles);
    assertNotNull(copy);
    assertNotEquals(dmFiles, copy); // check different objects
    assertEquals(dmFiles.getOutputDirectory(), copy.getOutputDirectory());
    assertEquals(dmFiles.getInputPageLinks(), copy.getInputPageLinks());
    assertEquals(dmFiles.getInputCategoryLinks(), copy.getInputCategoryLinks());
    assertEquals(dmFiles.getInputPagesArticles(), copy.getInputPagesArticles());
    assertEquals(dmFiles.getInputPagesMetaCurrent(), copy.getInputPagesMetaCurrent());
    assertEquals(dmFiles.isCompressGeneratedFiles(), copy.isCompressGeneratedFiles());
  }

  @Test
  void testCheckAll() {
    assertTrue(dmFiles.checkAll());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"\t", "\n", " "})
  void testSetDataDirectoryWithInvalidValue(String input) {
    assertThrows(IllegalArgumentException.class, () -> dmFiles.setDataDirectory(input));
  }

  @ParameterizedTest
  @ValueSource(strings = {"log4j2.xml",})
  void testSetDataDirectoryWithFileShouldKeepPreviousDirectory(String input) {
    dmFiles.setDataDirectory(BASE.getFile() + File.separator + input);
    File outputDir = dmFiles.getOutputDirectory();
    assertNotNull(outputDir);
    assertEquals(OUTPUT_DIR, outputDir.getAbsolutePath());
  }

  @Test
  void testGetOutputDirectory() {
    File outputDir = dmFiles.getOutputDirectory();
    assertNotNull(outputDir);
    assertEquals(OUTPUT_DIR, outputDir.getAbsolutePath());
  }

  @Test
  void testGetOutputCategory() {
    assertEquals(OUTPUT_DIR + File.separator + "Category.txt", dmFiles.getOutputCategory());
  }

  @Test
  void testGetOutputPageCategories() {
    assertEquals(OUTPUT_DIR + File.separator + "page_categories.txt", dmFiles.getOutputPageCategories());
  }

  @Test
  void testGetOutputCategoryPages() {
    assertEquals(OUTPUT_DIR + File.separator + "category_pages.txt", dmFiles.getOutputCategoryPages());
  }

  @Test
  void testGetOutputCategoryInlinks() {
    assertEquals(OUTPUT_DIR + File.separator + "category_inlinks.txt", dmFiles.getOutputCategoryInlinks());
  }

  @Test
  void testGetOutputCategoryOutlinks() {
    assertEquals(OUTPUT_DIR + File.separator + "category_outlinks.txt", dmFiles.getOutputCategoryOutlinks());
  }

  @Test
  void testGetOutputPageInlinks() {
    assertEquals(OUTPUT_DIR + File.separator + "page_inlinks.txt", dmFiles.getOutputPageInlinks());
  }

  @Test
  void testGetOutputPageOutlinks() {
    assertEquals(OUTPUT_DIR + File.separator + "page_outlinks.txt", dmFiles.getOutputPageOutlinks());
  }

  @Test
  void testGetOutputPage() {
    assertEquals(OUTPUT_DIR + File.separator + "Page.txt", dmFiles.getOutputPage());
  }

  @Test
  void testGetOutputPageMapLine() {
    assertEquals(OUTPUT_DIR + File.separator + "PageMapLine.txt", dmFiles.getOutputPageMapLine());
  }

  @Test
  void testGetOutputPageRedirects() {
    assertEquals(OUTPUT_DIR + File.separator + "page_redirects.txt", dmFiles.getOutputPageRedirects());
  }

  @Test
  void testGetOutputMetadata() {
    assertEquals(OUTPUT_DIR + File.separator + "MetaData.txt", dmFiles.getOutputMetadata());
  }

  @Test
  void testGetInputCategoryLinks() {
    assertEquals(TEST_OUTPUT_DIR + "categorylinks.sql.gz", dmFiles.getInputCategoryLinks());
  }

  @Test
  void testGetInputPageLinks() {
    assertEquals(TEST_OUTPUT_DIR + "pagelinks.sql.gz", dmFiles.getInputPageLinks());
  }

  @Test
  void testGetInputPagesArticles() {
    assertEquals(TEST_OUTPUT_DIR + "pages-articles.xml.bz2", dmFiles.getInputPagesArticles());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testGetGeneratedPage(boolean useCompression) {
    if (useCompression) {
      dmFiles.setCompressGeneratedFiles(true);
      assertEquals(TEST_OUTPUT_DIR + "page.bin.gz", dmFiles.getGeneratedPage());
    } else {
      dmFiles.setCompressGeneratedFiles(false);
      assertEquals(TEST_OUTPUT_DIR + "page.bin", dmFiles.getGeneratedPage());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testGetGeneratedRevision(boolean useCompression) {
    if (useCompression) {
      dmFiles.setCompressGeneratedFiles(true);
      assertEquals(TEST_OUTPUT_DIR + "revision.bin.gz", dmFiles.getGeneratedRevision());
    } else {
      dmFiles.setCompressGeneratedFiles(false);
      assertEquals(TEST_OUTPUT_DIR + "revision.bin", dmFiles.getGeneratedRevision());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testGetGeneratedText(boolean useCompression) {
    if (useCompression) {
      dmFiles.setCompressGeneratedFiles(true);
      assertEquals(TEST_OUTPUT_DIR + "text.bin.gz", dmFiles.getGeneratedText());
    } else {
      dmFiles.setCompressGeneratedFiles(false);
      assertEquals(TEST_OUTPUT_DIR + "text.bin", dmFiles.getGeneratedText());
    }
  }

  @Test
  void testGetGeneratedDiscussions() {
    assertEquals(TEST_OUTPUT_DIR + "discussions.bin", dmFiles.getGeneratedDiscussions());
  }
  
}
