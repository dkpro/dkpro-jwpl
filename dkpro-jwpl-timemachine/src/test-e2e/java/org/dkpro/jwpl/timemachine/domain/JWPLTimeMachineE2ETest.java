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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JWPLTimeMachineE2ETest {

  private static final URL BASE = JWPLTimeMachineE2ETest.class.getProtectionDomain().getCodeSource().getLocation();
  private static final String TARGET = BASE.getFile().replace("test-classes/","");
  private static final String CONF_FILE = BASE.getFile() + "timemachine-config-e2e.xml";
  private static final String EXEC_DIR = TARGET + "tool-exec";
  private static final String OUTPUT_DIR = EXEC_DIR + File.separator + "output";

  private static final String TOOL_NAME;
  private static final String WIKI_NAME;

  static {
    // Note: By default, this is set dynamically by Maven failsafe plugin - if IT is run standalone: set it manually
    TOOL_NAME = System.getProperty("jwpl.tool.name");
    WIKI_NAME = System.getProperty("jwpl.wiki.name");
  }

  /**
   * The number of rows each of the eleven generated tables has to carry, in every one of the three
   * generated snapshots.
   * <p>
   * Derived from the bundled fixtures, not from a previous run of the tool:
   * <ul>
   * <li>{@code Category.txt}: the {@code pages-meta-history} fixture holds 29 non-redirect pages in
   * namespace 14, and every one of them has revisions predating the earliest snapshot.</li>
   * <li>{@code category_outlinks.txt} / {@code category_inlinks.txt}: the {@code categorylinks}
   * fixture holds 28 tuples with {@code cl_type='subcat'}, every one of them originating from one of
   * those 29 category pages and pointing at a {@code cl_target_id} that resolves to another one of
   * them through the {@code linktarget} fixture. Only {@code Category:Top Level} never occurs as a
   * source, it is the root of the tree.</li>
   * <li>{@code category_pages.txt} / {@code page_categories.txt}: the remaining 128
   * {@code cl_type='page'} tuples all originate from pages in namespace 2, 3 or 10, none of which the
   * TimeMachine imports, so no membership row survives.</li>
   * <li>{@code Page.txt}: the TimeMachine only imports namespaces 0 and 14, and the fixture holds
   * exactly one non-redirect page in namespace 0, {@code Main Page}.</li>
   * <li>{@code page_redirects.txt} and the second {@code PageMapLine.txt} row: {@code Main page}
   * redirects to {@code Main Page}.</li>
   * <li>{@code page_inlinks.txt} / {@code page_outlinks.txt}: {@code Main Page} is the only imported
   * article and the {@code pagelinks} fixture holds no tuple with {@code pl_from=1269}.</li>
   * </ul>
   */
  private static final Map<String, Integer> EXPECTED_ROWS = expectedRows();

  /**
   * The complete category tree the fixtures describe, rendered as {@code parent -> child} pairs of
   * category titles. Each entry corresponds to exactly one {@code cl_type='subcat'} tuple of the
   * {@code categorylinks} fixture, with the target resolved through the {@code linktarget} fixture.
   * <p>
   * This is the assertion that guards issue #491: before the {@code linktarget} indirection was
   * honoured, the parser read {@code cl_sortkey} as if it were {@code cl_to} and this set was empty.
   */
  private static final Set<String> EXPECTED_CATEGORY_TREE = Set.of(
          "Top_Level -> Articles",
          "Top_Level -> Wikipedia",
          "Wikipedia -> Candidates_for_speedy_deletion",
          "Wikipedia -> Userboxes",
          "Wikipedia -> Wikipedians_by_language",
          "Userboxes -> Language_user_templates",
          "Language_user_templates -> User_templates_de",
          "Language_user_templates -> User_templates_en",
          "Language_user_templates -> User_templates_fr",
          "Language_user_templates -> User_templates_nl",
          "Wikipedians_by_language -> User_aa",
          "Wikipedians_by_language -> User_de",
          "Wikipedians_by_language -> User_en",
          "Wikipedians_by_language -> User_es",
          "Wikipedians_by_language -> User_fr",
          "Wikipedians_by_language -> User_he",
          "Wikipedians_by_language -> User_nl",
          "Wikipedians_by_language -> User_sk",
          "User_aa -> User_aa-0",
          "User_de -> User_de-1",
          "User_en -> User_en-2",
          "User_en -> User_en-3",
          "User_es -> User_es-2",
          "User_fr -> User_fr-1",
          "User_fr -> User_fr-N",
          "User_he -> User_he-N",
          "User_nl -> User_nl-N",
          "User_sk -> User_sk-N");

  // Command under test
  private List<String> cmd;

  @BeforeAll
  public static void initEnv() throws IOException {
    Files.createDirectories(Path.of(EXEC_DIR));
    Files.createDirectories(Path.of(OUTPUT_DIR));
    // Copy (do not move) the fixtures: keeping the originals in place makes repeated
    // invocations of 'mvn verify' over a populated 'target' directory idempotent.
    try (Stream<Path> results = Files.find(Path.of(BASE.getFile()), Integer.MAX_VALUE,
            (path, basicFileAttributes)
                    -> path.toFile().getName().startsWith(WIKI_NAME)
    )) {
      results.forEach(p -> {
        try {
          Files.copy(p, Path.of(EXEC_DIR, p.getFileName().toString()),
                  StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    }
  }

  @BeforeEach
  public void setup() {
    // Define the command to run the JAR file
    cmd = new ArrayList<>(List.of("java", "-jar", TARGET + File.separator + TOOL_NAME));
  }

  @Test
  void testExecJWPLTimeMachine() throws IOException {
    // Add in required arguments
    cmd.add(CONF_FILE);
    assertEquals(0,  execTool(cmd));
    // check resulting directories in 'output' directory are present and count meets expectations
    List<Path> snapshots;
    try (Stream<Path> results = Files.find(Path.of(OUTPUT_DIR), Integer.MAX_VALUE,
            (path, basicFileAttributes)
                    -> basicFileAttributes.isDirectory() && path.toFile().getName().endsWith("100"))) {
      snapshots = results.sorted().toList();
    }
    assertEquals(3,  snapshots.size());
    // every snapshot has to carry the very same category graph
    for (Path snapshot : snapshots) {
      assertEquals(EXPECTED_ROWS, rowCounts(snapshot), snapshot.toString());
      assertEquals(EXPECTED_CATEGORY_TREE, categoryTree(snapshot), snapshot.toString());
      assertEquals(inlinks(snapshot), mirrored(snapshot), snapshot.toString());
    }
    // Regression guard for issue #490: hoisting writeMetaData() into AbstractDumpVersion must
    // leave the TimeMachine output unchanged, i.e. nine columns whose ninth is the 14-digit
    // MediaWiki snapshot timestamp.
    Optional<Path> metaDataFile;
    try (Stream<Path> found = Files.find(Path.of(OUTPUT_DIR), Integer.MAX_VALUE,
            (path, basicFileAttributes) -> path.toFile().getName().equals("MetaData.txt"))) {
      metaDataFile = found.findFirst();
    }
    assertTrue(metaDataFile.isPresent(), "no MetaData.txt was generated");
    List<String> metaData = Files.readAllLines(metaDataFile.get(), StandardCharsets.UTF_8);
    assertEquals(1, metaData.size());
    String[] fields = metaData.get(0).split("\\t", -1);
    assertEquals(9, fields.length);
    assertTrue(fields[8].matches("\\d{14}"), "unexpected version value: " + fields[8]);
  }

  @Test
  void testExecJWPLTimeMachineWithNoArgumentsShouldFail() {
    // Simulating an execution without config file
    int exitCode = execTool(cmd);
    assertEquals(255,  exitCode);
  }

  /**
   * @return The expected number of rows per generated table, keyed by file name.
   */
  private static Map<String, Integer> expectedRows() {
    Map<String, Integer> expected = new LinkedHashMap<>();
    expected.put("Category.txt", 29);
    expected.put("category_inlinks.txt", 28);
    expected.put("category_outlinks.txt", 28);
    expected.put("category_pages.txt", 0);
    expected.put("page_categories.txt", 0);
    expected.put("Page.txt", 1);
    expected.put("PageMapLine.txt", 2);
    expected.put("page_inlinks.txt", 0);
    expected.put("page_outlinks.txt", 0);
    expected.put("page_redirects.txt", 1);
    expected.put("MetaData.txt", 1);
    return expected;
  }

  private static Map<String, Integer> rowCounts(Path snapshot) throws IOException {
    Map<String, Integer> counts = new LinkedHashMap<>();
    for (String table : EXPECTED_ROWS.keySet()) {
      counts.put(table, Files.readAllLines(snapshot.resolve(table), UTF_8).size());
    }
    return counts;
  }

  /**
   * @param snapshot The directory holding the generated tables of one snapshot.
   * @return {@code category_outlinks.txt} rendered as {@code parent -> child} title pairs, with the
   *         page ids resolved through {@code Category.txt}.
   */
  private static Set<String> categoryTree(Path snapshot) throws IOException {
    Map<String, String> titles = new HashMap<>();
    for (String line : Files.readAllLines(snapshot.resolve("Category.txt"), UTF_8)) {
      String[] columns = line.split("\\t");
      titles.put(columns[0], columns[2]);
    }
    Set<String> tree = new HashSet<>();
    for (String edge : Files.readAllLines(snapshot.resolve("category_outlinks.txt"), UTF_8)) {
      String[] columns = edge.split("\\t");
      tree.add(titles.get(columns[0]) + " -> " + titles.get(columns[1]));
    }
    return tree;
  }

  private static Set<String> inlinks(Path snapshot) throws IOException {
    return new HashSet<>(Files.readAllLines(snapshot.resolve("category_inlinks.txt"), UTF_8));
  }

  /**
   * @param snapshot The directory holding the generated tables of one snapshot.
   * @return {@code category_outlinks.txt} with both columns swapped, that is, what
   *         {@code category_inlinks.txt} has to contain.
   */
  private static Set<String> mirrored(Path snapshot) throws IOException {
    Set<String> swapped = new HashSet<>();
    for (String edge : Files.readAllLines(snapshot.resolve("category_outlinks.txt"), UTF_8)) {
      String[] columns = edge.split("\\t");
      swapped.add(columns[1] + "\t" + columns[0]);
    }
    return swapped;
  }

  private int execTool(List<String> call) {
    ProcessBuilder pb = new ProcessBuilder(call);
    pb.directory(new File(TARGET));  // Set working directory
    pb.inheritIO(); // Redirect output to console
    Process p = null;
    try {
      p = pb.start();
      ProcessHandle processHandle = p.toHandle();
      System.out.println("PID '" + processHandle.pid() + "' has started");
      CompletableFuture<ProcessHandle> onProcessExit = processHandle.onExit();
      onProcessExit.get();
      onProcessExit.thenAccept(ph -> {
        System.out.println("PID '" + ph.pid() + "' has stopped");
      });
      // Wait for the process to finish
      return p.waitFor();
    } catch (IOException | ExecutionException | InterruptedException e) {
      System.err.println("Execution error: " + e.getLocalizedMessage());
      int exitCode = -1;
      if (p != null && p.isAlive()) {
        p.destroy(); // Clean up the process
        System.out.println("Process cleaned up");
        try {
          exitCode = p.waitFor();
        } catch (InterruptedException ex) {
          System.err.println("Process cleanup interrupted. Exit code: " + exitCode);
          throw new RuntimeException(ex);
        }
      }
      return exitCode;
    }
  }
}
