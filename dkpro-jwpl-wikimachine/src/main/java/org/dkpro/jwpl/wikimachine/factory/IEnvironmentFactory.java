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
package org.dkpro.jwpl.wikimachine.factory;

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.decompression.IDecompressor;
import org.dkpro.jwpl.wikimachine.domain.DumpVersionProcessor;
import org.dkpro.jwpl.wikimachine.domain.ISnapshotGenerator;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableInputStream;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;

/**
 * An abstraction for a factory which defines methods to retrieve environment specific bean instances.
 */
public interface IEnvironmentFactory
{

    /**
     * @return Retrieves the current {@link ILogger} instance.
     */
    ILogger getLogger();

    /**
     * @return Retrieves the current {@link IDecompressor} instance.
     */
    IDecompressor getDecompressor();

    /**
     * @return Retrieves the current {@link ISnapshotGenerator} instance.
     */
    ISnapshotGenerator getSnapshotGenerator();

    /**
     * @return Retrieves the current {@link DumpVersionProcessor} instance.
     */
    DumpVersionProcessor getDumpVersionProcessor();

    /**
     * @return Retrieves a valid {@link IDumpVersion} instance.
     */
    IDumpVersion getDumpVersion();

    /**
     * @return Retrieves a valid {@link DumpTableInputStream} instance.
     */
    DumpTableInputStream getDumpTableInputStream();

    /**
     * @return Retrieves the current {@link PageParser} instance.
     */
    PageParser getPageParser();

    /**
     * @return Retrieves a valid {@link RevisionParser} instance.
     */
    RevisionParser getRevisionParser();

    /**
     * @return Retrieves a valid {@link TextParser} instance.
     */
    TextParser getTextParser();
}
