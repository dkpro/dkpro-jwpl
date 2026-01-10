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
package org.dkpro.jwpl.wikimachine.domain;

/**
 * An abstraction for a snapshot generation.
 *
 * @see Files
 * @see Configuration
 */
public interface ISnapshotGenerator
{
    /**
     * Specifies the {@link Files} instance to use for snapshot generation.
     * @param files A fully initialized {@link Files} instance.
     */
    void setFiles(Files files);

    /**
     * Specifies the {@link Configuration} instance to use for snapshot generation.
     * @param config A fully initialized {@link Configuration} instance.
     */
    void setConfiguration(Configuration config);

    /**
     * Triggers the snapshot generator process.
     *
     * @throws Exception Thrown if errors occurred during start.
     */
    void start() throws Exception;
}
