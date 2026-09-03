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
package org.dkpro.jwpl.timemachine.dump.version;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.List;

import org.dkpro.jwpl.timemachine.domain.TimeMachineFiles;
import org.dkpro.jwpl.wikimachine.debug.Slf4JLogger;
import org.dkpro.jwpl.wikimachine.domain.MetaData;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Regression guard for issue #490: hoisting {@code writeMetaData()} into
 * {@code AbstractDumpVersion} must leave the TimeMachine's output byte-identical, i.e. nine
 * columns whose last one is the snapshot timestamp in MediaWiki notation.
 */
class WriteMetaDataTest {

  @Test
  void writesTheSnapshotTimestampAsTheNinthColumn(@TempDir Path outputDirectory)
          throws IOException {
    Timestamp snapshot = Timestamp.valueOf("2026-01-01 00:00:00");

    MetaData metaData = new MetaData();
    metaData.setId("NULL");
    metaData.setLanguage("test");
    metaData.setDisambiguationCategory("Disambiguation");
    metaData.setMainCategory("Telecooperation");
    metaData.setNrOfPages(36);
    metaData.setNrOfRedirects(6);
    metaData.setNrOfCategories(17);
    metaData.setTimestamp(snapshot);

    TimeMachineFiles files = new TimeMachineFiles(new Slf4JLogger());
    files.setOutputDirectory(outputDirectory.toAbsolutePath().toString());
    files.setTimestamp(snapshot);

    IDumpVersion version = new DumpVersionFastUtilIntKey();
    version.setFiles(files);
    version.setMetaData(metaData);
    version.writeMetaData();

    List<String> lines = Files.readAllLines(Path.of(files.getOutputMetadata()),
            StandardCharsets.UTF_8);
    assertEquals(1, lines.size(), "MetaData.txt is expected to hold exactly one row");

    List<String> fields = List.of(lines.get(0).split("\t", -1));
    assertEquals(9, fields.size());
    assertEquals("20260101000000", fields.get(8));
  }
}
