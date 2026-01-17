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
package org.dkpro.jwpl.datamachine.factory;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.dkpro.jwpl.datamachine.dump.xml.BinaryDumpTableInputStream;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableInputStream;
import org.dkpro.jwpl.wikimachine.factory.IEnvironmentFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultDataMachineEnvironmentFactoryTest {

  // SUT
  private IEnvironmentFactory factory;

  @BeforeEach
  public void setUp() {
    factory = DefaultDataMachineEnvironmentFactory.getInstance();
  }

  @Test
  public void testGetLogger() {
    assertNotNull(factory.getLogger());
  }

  @Test
  public void testGetPageParser() {
    assertNotNull(factory.getPageParser());
  }

  @Test
  public void testGetRevisionParser() {
    assertNotNull(factory.getRevisionParser());
  }

  @Test
  public void testGetTextParser() {
    assertNotNull(factory.getTextParser());
  }

  @Test
  public void testGetSnapshotGenerator() {
    assertNotNull(factory.getSnapshotGenerator());
  }

  @Test
  public void testGetDecompressor() {
    assertNotNull(factory.getDecompressor());
  }

  @Test
  public void testGetDumpVersionProcessor() {
    assertNotNull(factory.getDumpVersionProcessor());
  }

  @Test
  public void testGetDumpVersion() {
    assertNotNull(factory.getDumpVersion());
  }

  @Test
  public void testGetDumpTableStream() {
    DumpTableInputStream dtInputStream = factory.getDumpTableInputStream();
    assertNotNull(dtInputStream);
    assertInstanceOf(BinaryDumpTableInputStream.class, dtInputStream);
  }
}
