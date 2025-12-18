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
package org.dkpro.jwpl.wikimachine.dump.version;

import java.lang.reflect.InvocationTargetException;

/**
 * A simple marker interface for datamachine-specific aspects.
 */
public interface IDumpVersionDataFactory extends IDumpVersionFactory {

  String DATAMACHINE_PACKAGE = "org.dkpro.jwpl.datamachine.dump.version.";

  static IDumpVersionDataFactory defaultFactory() {
    return byClass(DATAMACHINE_PACKAGE + "SingleDumpVersionJDKStringKeyFactory");
  }

  static IDumpVersionDataFactory byType(FactoryType type) {
    return switch (type) {
      case JDK_INT_KEY -> byClass(DATAMACHINE_PACKAGE + "SingleDumpVersionJDKIntKeyFactory");
      case JDK_LONG_KEY -> byClass(DATAMACHINE_PACKAGE + "SingleDumpVersionJDKLongKeyFactory");
      case JDK_STRING_KEY -> byClass(DATAMACHINE_PACKAGE + "SingleDumpVersionJDKStringKeyFactory");
    };
  }

  static IDumpVersionDataFactory byClass(String className) {
      try {
          Class<IDumpVersionDataFactory> c = (Class<IDumpVersionDataFactory>) Class.forName(className);
          return c.getDeclaredConstructor().newInstance();
      } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
               InvocationTargetException | IllegalAccessException e) {
        throw new RuntimeException("Errors during creation of IDumpVersionDataFactory instance!");
      }
  }
}
