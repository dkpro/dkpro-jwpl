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
package org.dkpro.jwpl.datamachine.dump.xml;

import java.io.EOFException;
import java.io.IOException;

import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;

public class DataMachineRevisionParser extends RevisionParser {

  @Override
  public boolean next() throws IOException {
    boolean hasNext = true;
    try {
      revPage = stream.readInt();
      revTextId = stream.readInt();
    } catch (EOFException e) {
      hasNext = false;
    }
    return hasNext;
  }
}
