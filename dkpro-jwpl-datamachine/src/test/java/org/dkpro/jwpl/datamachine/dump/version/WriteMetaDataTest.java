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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.dkpro.jwpl.datamachine.domain.DataMachineFiles;
import org.dkpro.jwpl.wikimachine.debug.Slf4JLogger;
import org.dkpro.jwpl.wikimachine.domain.MetaData;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Guards the canonical nine-column {@code MetaData.txt} layout written by the DataMachine's
 * default dump version. Before issue #490 the DataMachine emitted only eight columns, leaving
 * the {@code MetaData.version} database column unpopulated.
 */
class WriteMetaDataTest {

  @Test
  void writesNineColumnsIncludingTheVersion(@TempDir Path outputDirectory) throws IOException {
    MetaData metaData = metaData();
    metaData.setVersion("20260101000000");

    List<String> fields = writeAndReadSingleRow(metaData, outputDirectory);

    assertEquals(9, fields.size());
    assertEquals("NULL", fields.get(0));
    assertEquals("test", fields.get(1));
    assertEquals("Disambiguation", fields.get(2));
    assertEquals("Telecooperation", fields.get(3));
    assertEquals("36", fields.get(4));
    assertEquals("6", fields.get(5));
    assertEquals("2", fields.get(6));
    assertEquals("17", fields.get(7));
    assertEquals("20260101000000", fields.get(8));
  }

  @Test
  void writesTheNullMarkerForAnAbsentVersion(@TempDir Path outputDirectory) throws IOException {
    List<String> fields = writeAndReadSingleRow(metaData(), outputDirectory);

    assertEquals(9, fields.size());
    assertEquals("\\N", fields.get(8));
  }

  private static MetaData metaData() {
    MetaData metaData = new MetaData();
    metaData.setId("NULL");
    metaData.setLanguage("test");
    metaData.setDisambiguationCategory("Disambiguation");
    metaData.setMainCategory("Telecooperation");
    metaData.setNrOfPages(36);
    metaData.setNrOfRedirects(6);
    metaData.setNrOfCategories(17);
    metaData.addDisamb();
    metaData.addDisamb();
    return metaData;
  }

  private static List<String> writeAndReadSingleRow(MetaData metaData, Path outputDirectory)
          throws IOException {
    DataMachineFiles files = new DataMachineFiles(new Slf4JLogger());
    files.setOutputDirectory(outputDirectory.toAbsolutePath().toString());

    IDumpVersion version = new SingleDumpVersionJDKStringKeyFactory().getDumpVersion();
    assertNotNull(version);
    version.setFiles(files);
    version.setMetaData(metaData);
    version.writeMetaData();

    List<String> lines = Files.readAllLines(Path.of(files.getOutputMetadata()),
            StandardCharsets.UTF_8);
    assertEquals(1, lines.size(), "MetaData.txt is expected to hold exactly one row");
    return List.of(lines.get(0).split("\t", -1));
  }
}
