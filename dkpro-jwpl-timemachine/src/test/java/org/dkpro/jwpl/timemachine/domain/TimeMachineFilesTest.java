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
package org.dkpro.jwpl.timemachine.domain;

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

import org.dkpro.jwpl.timemachine.factory.DefaultTimeMachineEnvironmentFactory;
import org.dkpro.jwpl.wikimachine.factory.IEnvironmentFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TimeMachineFilesTest {

  private static final IEnvironmentFactory factory = DefaultTimeMachineEnvironmentFactory.getInstance();

  private static final URL BASE = TimeMachineFilesTest.class.getProtectionDomain().getCodeSource().getLocation();
  private static final String TARGET = BASE.getFile().replace("test-classes/","");
  private static final String TEST_OUTPUT_DIR = TARGET + "test-output/";
  private static final String OUTPUT_DIR = TEST_OUTPUT_DIR + "output";

  private static File mockPageLinks;
  private static File mockCategoryLinks;
  private static File mockMetaHistory;

  // SUT
  private TimeMachineFiles tmFiles;

  @BeforeAll
  public static void initEnv() throws IOException {
    Files.createDirectories(Path.of(OUTPUT_DIR));
    mockPageLinks = Path.of(TEST_OUTPUT_DIR, "pagelinks.sql.gz").toFile();
    if (!mockPageLinks.exists()) {
      assertTrue(mockPageLinks.createNewFile());;
    }
    mockCategoryLinks = Path.of(TEST_OUTPUT_DIR, "categorylinks.sql.gz").toFile();
    if (!mockCategoryLinks.exists()) {
      assertTrue(mockCategoryLinks.createNewFile());;
    }
    mockMetaHistory = Path.of(TEST_OUTPUT_DIR, "pages-meta-history.xml.7z").toFile();
    if (!mockMetaHistory.exists()) {
      assertTrue(mockMetaHistory.createNewFile());;
    }
  }

  @BeforeEach
  void setUp() {
    tmFiles = new TimeMachineFiles(factory.getLogger());
    tmFiles.setOutputDirectory(OUTPUT_DIR);
    tmFiles.setCategoryLinksFile(mockCategoryLinks.getAbsolutePath());
    tmFiles.setPageLinksFile(mockPageLinks.getAbsolutePath());
    tmFiles.setMetaHistoryFile(mockMetaHistory.getAbsolutePath());
  }

  @Test
  void testCopyConstructor() {
    TimeMachineFiles copy = new TimeMachineFiles(tmFiles);
    assertNotNull(copy);
    assertNotEquals(tmFiles, copy); // check different objects
    assertEquals(tmFiles.getOutputDirectory(), copy.getOutputDirectory());
    assertEquals(tmFiles.getCategoryLinksFile(), copy.getCategoryLinksFile());
    assertEquals(tmFiles.getPageLinksFile(), copy.getPageLinksFile());
    assertEquals(tmFiles.getMetaHistoryFile(), copy.getMetaHistoryFile());
  }

  @Test
  void testCheckAll() {
    assertTrue(tmFiles.checkAll());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"\t", "\n", " "})
  void testSetOutputDirectoryWithInvalidValue(String input) {
    assertThrows(IllegalArgumentException.class, () -> tmFiles.setOutputDirectory(input));
  }

  @ParameterizedTest
  @ValueSource(strings = {"log4j2.xml",})
  void testSetOutputDirectoryWithFileShouldKeepPreviousDirectory(String input) {
    tmFiles.setOutputDirectory(OUTPUT_DIR + File.separator + input);
    File outputDir = tmFiles.getOutputDirectory();
    assertNotNull(outputDir);
    assertEquals(OUTPUT_DIR, outputDir.getAbsolutePath());
  }

  @Test
  void testGetOutputDirectory() {
    File outputDir = tmFiles.getOutputDirectory();
    assertNotNull(outputDir);
    assertEquals(OUTPUT_DIR, outputDir.getAbsolutePath());
  }

  @Test
  void testGetOutputCategory() {
    assertEquals(OUTPUT_DIR + File.separator + "Category.txt", tmFiles.getOutputCategory());
  }

  @Test
  void testGetOutputPageCategories() {
    assertEquals(OUTPUT_DIR + File.separator + "page_categories.txt", tmFiles.getOutputPageCategories());
  }

  @Test
  void testGetOutputCategoryPages() {
    assertEquals(OUTPUT_DIR + File.separator + "category_pages.txt", tmFiles.getOutputCategoryPages());
  }

  @Test
  void testGetOutputCategoryInlinks() {
    assertEquals(OUTPUT_DIR + File.separator + "category_inlinks.txt", tmFiles.getOutputCategoryInlinks());
  }

  @Test
  void testGetOutputCategoryOutlinks() {
    assertEquals(OUTPUT_DIR + File.separator + "category_outlinks.txt", tmFiles.getOutputCategoryOutlinks());
  }

  @Test
  void testGetOutputPageInlinks() {
    assertEquals(OUTPUT_DIR + File.separator + "page_inlinks.txt", tmFiles.getOutputPageInlinks());
  }

  @Test
  void testGetOutputPageOutlinks() {
    assertEquals(OUTPUT_DIR + File.separator + "page_outlinks.txt", tmFiles.getOutputPageOutlinks());
  }

  @Test
  void testGetOutputPage() {
    assertEquals(OUTPUT_DIR + File.separator + "Page.txt", tmFiles.getOutputPage());
  }

  @Test
  void testGetOutputPageMapLine() {
    assertEquals(OUTPUT_DIR + File.separator + "PageMapLine.txt", tmFiles.getOutputPageMapLine());
  }

  @Test
  void testGetOutputPageRedirects() {
    assertEquals(OUTPUT_DIR + File.separator + "page_redirects.txt", tmFiles.getOutputPageRedirects());
  }

  @Test
  void testGetOutputMetadata() {
    assertEquals(OUTPUT_DIR + File.separator + "MetaData.txt", tmFiles.getOutputMetadata());
  }
  
}
