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

/**
 * Defines the minimal API of a factory to retrieve {@link IDumpVersion} objects.
 */
public interface IDumpVersionFactory
{
    /**
     * Defines several factory variants for different key access strategies.
     */
    enum FactoryType {
        /** The key for: JDK Integer. */
        JDK_INT_KEY,
        /** The key for: JDK Long. */
        JDK_LONG_KEY,
        /** The key for: JDK String. */
        JDK_STRING_KEY
    }

    /**
     * @return Retrieves the {@link IDumpVersion}.
     */
    IDumpVersion getDumpVersion();
}
