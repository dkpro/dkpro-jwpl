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
package org.dkpro.jwpl.revisionmachine.difftool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.dkpro.jwpl.revisionmachine.common.util.ExitStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DiffToolE2ETest {

  private static final URL BASE = DiffToolE2ETest.class.getProtectionDomain().getCodeSource().getLocation();
  private static final String TARGET = BASE.getFile().replace("test-classes/","");
  private static final String CONF_FILE = BASE.getFile() + "difftool-config-e2e.xml";
  private static final String OUTPUT_DIR = TARGET + "tool-exec";
  private static final String LOGS_DIR = OUTPUT_DIR + File.separator + "logs";

  // A row of the revisions table; group 1 is the ArticleID. The binary SQL path writes a space
  // after the leading null.
  private static final Pattern ROW = Pattern.compile("\\(null, ?-?\\d+,\\d+,\\d+,(\\d+),");

  private static final String TOOL_NAME;
  private static final String EXEC_CLASS;
  private static final String WIKI_NAME;

  static {
      // Note: By default, this is set dynamically by Maven failsafe plugin - if IT is run standalone: set it manually
      TOOL_NAME = System.getProperty("jwpl.tool.name");
      EXEC_CLASS = DiffTool.class.getName();
      WIKI_NAME = System.getProperty("jwpl.wiki.name");
  }

  // Command under test
  private List<String> cmd;

  @BeforeAll
  public static void initEnv() throws IOException {
    // Start from empty logs: the error log of an earlier run would otherwise be appended to
    deleteRecursively(Path.of(LOGS_DIR));
    Files.createDirectories(Path.of(OUTPUT_DIR));
    Files.createDirectories(Path.of(LOGS_DIR));
    // Copy (do not move) the fixtures: keeping the originals in place makes repeated
    // invocations of 'mvn verify' over a populated 'target' directory idempotent.
    try (Stream<Path> results = Files.find(Path.of(BASE.getFile()), Integer.MAX_VALUE,
            (path, basicFileAttributes)
                    -> path.toFile().getName().startsWith(WIKI_NAME)
    )) {
      results.forEach(p -> {
        try {
          Files.copy(p, Path.of(OUTPUT_DIR, p.getFileName().toString()),
                  StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    }
  }

  /**
   * Deletes the given directory including its content, if it exists.
   *
   * @param dir the directory to delete
   * @throws IOException if a file or directory cannot be deleted
   */
  private static void deleteRecursively(Path dir) throws IOException {
    if (Files.exists(dir)) {
      try (Stream<Path> paths = Files.walk(dir)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
          Files.delete(path);
        }
      }
    }
  }

  @BeforeEach
  public void setup() {
    // Define the command to run the JAR file
    cmd = new ArrayList<>(List.of("java", "-cp", TARGET + File.separator + TOOL_NAME, EXEC_CLASS));
  }

  @Test
  void testExecJWPLDiffTool() throws IOException {
    // Add in required arguments
    cmd.add(CONF_FILE);
    int exitCode = execTool(cmd);
    assertEquals(0,  exitCode);
    // The revisions table records the namespace of each revision
    String sql = Files.readString(Path.of(OUTPUT_DIR, "output_1.sql"));
    assertTrue(sql.contains("Namespace INTEGER"), sql);
    // The fixture holds four pages in the configured namespaces 0 and 1, each with a single
    // revision. The text of "Main Page" (1269) is empty (a self-closing <text bytes="0"/>
    // element). Each revision is written as a row: (null, FullRevisionID, RevisionCounter,
    // RevisionID, ArticleID, ...)
    List<String> revisions = ROW.matcher(sql).results().map(r -> r.group(1)).toList();
    assertEquals(List.of("1269", "1271", "1426", "1508"), revisions.stream().sorted().toList(),
            sql);
    // No article failed to be read or processed
    Path errors = Path.of(LOGS_DIR, "DiffToolErrors.log");
    if (Files.exists(errors)) {
      String errorLog = Files.readString(errors);
      assertTrue(errorLog.isBlank(), errorLog);
    }
  }

  @Test
  void testExecJWPLDiffToolWithMissingConfigFileShouldFail() {
    cmd.add(TARGET + File.separator + "non-existent-difftool-config.xml");
    assertEquals(ExitStatus.EXIT_FAILURE, execTool(cmd));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 3})
  void testExecJWPLDiffToolWithTruncatedArchiveShouldFail(int threads, @TempDir Path dir)
          throws IOException {
    // The fixture archive cut in half, and a copy of the configuration that reads it, processed
    // sequentially and by the parallel archive scheduler
    String archiveName = WIKI_NAME + "-20260101-pages-meta-current.xml.bz2";
    byte[] content = Files.readAllBytes(Path.of(OUTPUT_DIR, archiveName));
    Files.write(dir.resolve(archiveName), Arrays.copyOf(content, content.length / 2));
    Path logsDir = Files.createDirectories(dir.resolve("logs"));
    String config = Files.readString(Path.of(CONF_FILE))
            .replace("\"./tool-exec/logs/\"", "\"" + logsDir + File.separator + "\"")
            .replace("\"./tool-exec/" + archiveName + "\"", "\"" + dir.resolve(archiveName) + "\"")
            .replace("\"./tool-exec/\"", "\"" + dir + File.separator + "\"")
            .replace("</cache>", "<LIMIT_ARCHIVE_THREADS>" + threads + "</LIMIT_ARCHIVE_THREADS></cache>");
    Path configFile = dir.resolve("difftool-config-truncated.xml");
    Files.writeString(configFile, config);
    cmd.add(configFile.toString());
    assertEquals(ExitStatus.EXIT_FAILURE, execTool(cmd));
  }

  @Test
  void testExecJWPLDiffToolWithNoArgumentsShouldFail() {
    // Simulating an execution without config file
    int exitCode = execTool(cmd);
    assertEquals(255,  exitCode);
  }

  /**
   * Converts the same set of archives with one thread and with several threads (see issue #548).
   * The parallel run writes one set of output files per archive; the concatenation of their rows
   * in archive order has to be identical to the output of the sequential run. The test generates
   * several archives with many revisions per page instead of using the single-archive fixture, so
   * that the jobs actually run concurrently and full revisions as well as diffs are written.
   */
  @ParameterizedTest
  @ValueSource(strings = {"UNCOMPRESSED", "BZIP2"})
  void testParallelArchivesProduceTheSameOutputAsSequentialRun(String outputMode) throws IOException {
    Path base = Path.of(OUTPUT_DIR, "parallel-" + outputMode.toLowerCase());
    Path input = Files.createDirectories(base.resolve("input"));
    List<Path> archives = new ArrayList<>();
    for (int part = 1; part <= PARALLEL_ARCHIVES; part++) {
      Path archive = input.resolve("testwiki-pages-meta-history" + part + ".xml.bz2");
      try (OutputStream out = new BZip2CompressorOutputStream(Files.newOutputStream(archive))) {
        out.write(historyDump(part).getBytes(StandardCharsets.UTF_8));
      }
      archives.add(archive);
    }

    Path sequential = runDiffTool(base.resolve("sequential"), archives, outputMode, 1);
    Path parallel = runDiffTool(base.resolve("parallel"), archives, outputMode, 3);

    String suffix = "BZIP2".equals(outputMode) ? ".sql.bz2" : ".sql";
    List<String> sequentialFiles = listFiles(sequential);
    assertEquals(List.of("output_1" + suffix), sequentialFiles);
    List<String> expectedParallelFiles = new ArrayList<>();
    for (int part = 1; part <= PARALLEL_ARCHIVES; part++) {
      expectedParallelFiles.add("output_testwiki-pages-meta-history" + part + "_1" + suffix);
    }
    assertEquals(expectedParallelFiles, listFiles(parallel));

    List<String> sequentialLines = readStatements(sequential.resolve(sequentialFiles.get(0)));
    List<String> header = sequentialLines.stream().filter(l -> !l.startsWith("INSERT")).toList();
    List<String> sequentialRows = sequentialLines.stream().filter(l -> l.startsWith("INSERT")).toList();
    assertTrue(header.stream().anyMatch(l -> l.contains("Namespace INTEGER")), header::toString);

    List<String> parallelRows = new ArrayList<>();
    for (String file : expectedParallelFiles) {
      List<String> lines = readStatements(parallel.resolve(file));
      // every archive gets its own complete header, followed by the rows of its articles
      assertEquals(header, lines.subList(0, header.size()), file);
      List<String> rows = lines.subList(header.size(), lines.size());
      assertFalse(rows.isEmpty(), file);
      rows.forEach(row -> assertTrue(row.startsWith("INSERT"), row));
      parallelRows.addAll(rows);
    }
    assertEquals(sequentialRows, parallelRows);
  }

  private static final int PARALLEL_ARCHIVES = 4;

  private Path runDiffTool(Path dir, List<Path> archives, String outputMode, int threads)
          throws IOException {
    Path output = Files.createDirectories(dir.resolve("out"));
    Path logs = Files.createDirectories(dir.resolve("logs"));
    try (Stream<Path> old = Files.list(output)) {
      for (Path p : old.toList()) {
        Files.delete(p);
      }
    }
    StringBuilder input = new StringBuilder();
    for (Path archive : archives) {
      input.append("    <archive>\n      <type>BZIP2</type>\n      <path>\"")
              .append(archive.toAbsolutePath()).append("\"</path>\n      <start>0</start>\n")
              .append("    </archive>\n");
    }
    String config = """
            <config>
              <values>
                <VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING>12</VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING>
                <COUNTER_FULL_REVISION>3</COUNTER_FULL_REVISION>
              </values>
              <input>
                <MODE_SURROGATES>DISCARD_REVISION</MODE_SURROGATES>
                <WIKIPEDIA_ENCODING>UTF-8</WIKIPEDIA_ENCODING>
            %s  </input>
              <output>
                <OUTPUT_MODE>%s</OUTPUT_MODE>
                <PATH>"%s"</PATH>
                <MODE_ZIP_COMPRESSION_ENABLED>true</MODE_ZIP_COMPRESSION_ENABLED>
                <MODE_DATAFILE_OUTPUT>false</MODE_DATAFILE_OUTPUT>
              </output>
              <cache>
                <LIMIT_TASK_SIZE_REVISIONS>5000000</LIMIT_TASK_SIZE_REVISIONS>
                <LIMIT_TASK_SIZE_DIFFS>1000000</LIMIT_TASK_SIZE_DIFFS>
                <LIMIT_SQLSERVER_MAX_ALLOWED_PACKET>1000000</LIMIT_SQLSERVER_MAX_ALLOWED_PACKET>
                <LIMIT_ARCHIVE_THREADS>%d</LIMIT_ARCHIVE_THREADS>
              </cache>
              <logging>
                <root_folder>"%s"</root_folder>
                <diff_tool>
                  <level>INFO</level>
                </diff_tool>
              </logging>
              <debug>
                <verification_diff>true</verification_diff>
                <verification_encoding>true</verification_encoding>
                <statistical_output>false</statistical_output>
                <debug_output>
                  <enabled>false</enabled>
                </debug_output>
              </debug>
              <filter>
                <namespaces>
                  <ns>0</ns>
                  <ns>1</ns>
                </namespaces>
              </filter>
            </config>
            """.formatted(input, outputMode, output.toAbsolutePath() + File.separator, threads,
            logs.toAbsolutePath() + File.separator);
    Path configFile = dir.resolve("difftool-config.xml");
    Files.writeString(configFile, config);

    cmd.add(configFile.toAbsolutePath().toString());
    assertEquals(0, execTool(cmd));
    cmd.remove(cmd.size() - 1);
    return output;
  }

  private static List<String> listFiles(Path dir) throws IOException {
    try (Stream<Path> files = Files.list(dir)) {
      return files.map(p -> p.getFileName().toString()).sorted().toList();
    }
  }

  private static List<String> readStatements(Path file) throws IOException {
    String content;
    if (file.getFileName().toString().endsWith(".bz2")) {
      try (InputStream in = new BZip2CompressorInputStream(Files.newInputStream(file), true)) {
        content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      }
    } else {
      content = Files.readString(file);
    }
    return Arrays.stream(content.split("\r\n")).filter(l -> !l.isEmpty()).toList();
  }

  /**
   * Creates a small full-history dump with several revisions per page, laid out like the
   * Wikipedia dumps: the reader relies on the whitespace between the elements.
   */
  private static String historyDump(int part) {
    StringBuilder dump = new StringBuilder("<mediawiki>\n  <siteinfo>\n    <namespaces>\n"
            + "      <namespace key=\"0\" case=\"first-letter\" />\n"
            + "      <namespace key=\"1\" case=\"first-letter\">Talk</namespace>\n"
            + "    </namespaces>\n  </siteinfo>");
    for (int page = 1; page <= 5; page++) {
      int pageId = part * 100 + page;
      int ns = page % 2;
      dump.append("\n  <page>\n    <title>").append(ns == 1 ? "Talk:" : "").append("Page ")
              .append(pageId).append("</title>\n    <ns>").append(ns).append("</ns>\n    <id>")
              .append(pageId).append("</id>");
      StringBuilder text = new StringBuilder("Article " + pageId + " is part of archive " + part
              + " and starts with a reasonably long first sentence.");
      for (int rev = 1; rev <= 7; rev++) {
        text.append(" Revision ").append(rev).append(" adds sentence number ")
                .append(rev * 7 + pageId).append(" to this article.");
        if (rev % 3 == 0) {
          text.insert(0, "Intro of revision " + rev + ". ");
        }
        dump.append("\n    <revision>\n      <id>").append(pageId * 100 + rev)
                .append("</id>\n      <timestamp>2009-03-").append(String.format("%02d", rev))
                .append("T01:13:23Z</timestamp>\n      <contributor>\n        <username>User")
                .append(rev).append("</username>\n        <id>").append(100 + rev)
                .append("</id>\n      </contributor>\n      <comment>edit ").append(rev)
                .append("</comment>\n      <text xml:space=\"preserve\">").append(text)
                .append("</text>\n    </revision>");
      }
      dump.append("\n  </page>");
    }
    return dump.append("\n</mediawiki>\n").toString();
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
