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
 * A simple marker interface for timemachine-specific aspects.
 */
public interface IDumpVersionTimeFactory extends IDumpVersionFactory {

  /** The fully qualified timemachine-specific package. */
  String TIMEMACHINE_PACKAGE = "org.dkpro.jwpl.timemachine.dump.version.";

  /**
   * Retrieves a {@link IDumpVersionDataFactory} instance of
   * the default {@code DumpVersionJDKStringKeyFactory} class.
   *
   * @return A default {@link IDumpVersionDataFactory} instance.
   */
  static IDumpVersionDataFactory defaultFactory() {
    return byClass(TIMEMACHINE_PACKAGE + "DumpVersionJDKStringKeyFactory");
  }

  /**
   * Retrieves a {@link IDumpVersionDataFactory} instance by {@link FactoryType}.
   *
   * @param type The {@link FactoryType} to specify the factory instance with.
   *             
   * @return A corresponding {@link IDumpVersionDataFactory} instance.
   */
  static IDumpVersionDataFactory byType(FactoryType type) {
    return switch (type) {
      case JDK_INT_KEY -> byClass(TIMEMACHINE_PACKAGE + "DumpVersionJDKIntKeyFactory");
      case JDK_LONG_KEY -> byClass(TIMEMACHINE_PACKAGE + "DumpVersionJDKLongKeyFactory");
      case JDK_STRING_KEY -> byClass(TIMEMACHINE_PACKAGE + "DumpVersionJDKStringKeyFactory");
    };
  }

  /**
   * Retrieves a {@link IDumpVersionDataFactory} instance by its class name.
   *
   * @param className The fully qualified name of the class to specify the factory instance with.
   *                  
   * @return A corresponding {@link IDumpVersionDataFactory} instance.
   */
  @SuppressWarnings("unchecked")
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
