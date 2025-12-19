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
package org.dkpro.jwpl.wikimachine.decompression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


public class SevenZipDecompressorTest {

  private static final String EXPECTED_CONTENT = "This file is here to test decompression types.";

  @ParameterizedTest
  @ValueSource(strings = {"archive.txt.7z", "src/test/resources/archive.txt.7z"})
  public void testGetInputStream(String input) throws IOException, URISyntaxException {
    final SevenZipDecompressor decompressor = new SevenZipDecompressor();

    try (InputStream szIn = decompressor.getInputStream(input)) {
      assertNotNull(szIn);
      String content = new String(szIn.readAllBytes(), StandardCharsets.UTF_8).trim();
      assertNotNull(content);
      assertEquals(EXPECTED_CONTENT, content);
    }
  }
}
