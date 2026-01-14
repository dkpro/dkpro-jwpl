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

import org.dkpro.jwpl.datamachine.domain.DataMachineGenerator;
import org.dkpro.jwpl.datamachine.dump.xml.BinaryDumpTableInputStream;
import org.dkpro.jwpl.datamachine.dump.xml.DataMachineRevisionParser;
import org.dkpro.jwpl.wikimachine.domain.ISnapshotGenerator;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersionDataFactory;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersionFactory;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableInputStream;
import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;
import org.dkpro.jwpl.wikimachine.factory.AbstractEnvironmentFactory;
import org.dkpro.jwpl.wikimachine.factory.IEnvironmentFactory;

/**
 * A default {@link IEnvironmentFactory} implementation for the DataMachine tool environment.
 *
 * @see IEnvironmentFactory
 */
public class DefaultDataMachineEnvironmentFactory extends AbstractEnvironmentFactory
    implements IEnvironmentFactory
{

    private static ISnapshotGenerator SNAPSHOTGENERATOR_BEAN;
    private static IDumpVersionFactory DUMPVERSION_FACTORY_BEAN;
    private static RevisionParser REVISIONPARSER_BEAN;

    private static final DefaultDataMachineEnvironmentFactory INSTANCE = new DefaultDataMachineEnvironmentFactory();


    /**
     * Do not instantiate this class on your own. Use {@link #getInstance()} instead.
     */
    // Note: Can't enforce a singleton-like private ctor, as ServiceLoader mechanism requires public no-arg form...
    public DefaultDataMachineEnvironmentFactory() {}

    /**
     * @return Retrieves the one and only instance of {@link DefaultDataMachineEnvironmentFactory}.
     */
    public static DefaultDataMachineEnvironmentFactory getInstance()
    {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ISnapshotGenerator getSnapshotGenerator()
    {
        if (SNAPSHOTGENERATOR_BEAN == null) {
            SNAPSHOTGENERATOR_BEAN = new DataMachineGenerator(INSTANCE);
        }
        return SNAPSHOTGENERATOR_BEAN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IDumpVersion getDumpVersion()
    {
        if (DUMPVERSION_FACTORY_BEAN == null) {
            DUMPVERSION_FACTORY_BEAN = IDumpVersionDataFactory.defaultFactory();
        }
        IDumpVersion version = DUMPVERSION_FACTORY_BEAN.getDumpVersion();
        version.setLogger(getLogger());
        version.setCategoryRedirectsSkip(true);
        version.setPageRedirectsSkip(true);
        return version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized DumpTableInputStream getDumpTableInputStream()
    {
        // prototype beans required
        return new BinaryDumpTableInputStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized RevisionParser getRevisionParser()
    {
        if (REVISIONPARSER_BEAN == null) {
            REVISIONPARSER_BEAN = new DataMachineRevisionParser();
        }
        return REVISIONPARSER_BEAN;
    }

}
