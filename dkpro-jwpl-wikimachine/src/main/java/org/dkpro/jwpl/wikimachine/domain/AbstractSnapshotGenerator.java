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

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.decompression.IDecompressor;
import org.dkpro.jwpl.wikimachine.factory.IEnvironmentFactory;

/**
 * A base {@link ISnapshotGenerator} implementation that defines several common beans
 * for concrete subclasses.
 *
 * @see ISnapshotGenerator
 */
public abstract class AbstractSnapshotGenerator
    implements ISnapshotGenerator
{
    /** The active {@link Configuration}. */
    protected Configuration configuration = null;
    /** The {@link IDecompressor} in use. */
    protected final IDecompressor decompressor;
    /** The {@link ILogger} in use. */
    protected final ILogger logger;
    /** The {@link DumpVersionProcessor}  in use. */
    protected final DumpVersionProcessor dumpVersionProcessor;
    /** The {@link IEnvironmentFactory} in use. */
    protected final IEnvironmentFactory envFactory;

    /**
     * Instantiates a {@link AbstractSnapshotGenerator} via the specified {@code environmentFactory}.
     *
     * @param environmentFactory The {@link IEnvironmentFactory environment factory} to use at runtime.
     * @throws IllegalArgumentException Thrown if arguments were invalid.
     */
    public AbstractSnapshotGenerator(IEnvironmentFactory environmentFactory)
    {
        if (environmentFactory == null) {
            throw new IllegalArgumentException("The specified environmentFactory must not be null.");
        }
        this.envFactory = environmentFactory;
        this.decompressor = envFactory.getDecompressor();
        this.logger = envFactory.getLogger();
        this.dumpVersionProcessor = envFactory.getDumpVersionProcessor();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

}
