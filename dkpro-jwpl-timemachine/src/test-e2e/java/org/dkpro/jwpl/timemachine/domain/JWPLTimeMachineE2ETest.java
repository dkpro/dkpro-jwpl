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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

  // Command under test
  private List<String> cmd;

  @BeforeAll
  public static void initEnv() throws IOException {
    Files.createDirectories(Path.of(EXEC_DIR));
    Files.createDirectories(Path.of(OUTPUT_DIR));
    Stream<Path> results = Files.find(Path.of(BASE.getFile()), Integer.MAX_VALUE,
            (path, basicFileAttributes)
                    -> path.toFile().getName().startsWith(WIKI_NAME)
    );

    results.forEach(p -> {
      try {
        Files.move(p, Path.of(EXEC_DIR, p.getFileName().toString()));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
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
    Stream<Path> results = Files.find(Path.of(EXEC_DIR + File.separator + "output"), Integer.MAX_VALUE,
            (path, basicFileAttributes)
                    -> basicFileAttributes.isDirectory() && path.toFile().getName().endsWith("100")
    );
    assertEquals(3,  results.count());
  }

  @Test
  void testExecJWPLTimeMachineWithNoArgumentsShouldFail() {
    // Simulating an execution without config file
    int exitCode = execTool(cmd);
    assertEquals(255,  exitCode);
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
