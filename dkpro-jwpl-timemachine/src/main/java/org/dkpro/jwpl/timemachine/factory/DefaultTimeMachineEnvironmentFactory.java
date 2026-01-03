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
package org.dkpro.jwpl.timemachine.factory;

import org.dkpro.jwpl.timemachine.domain.TimeMachineGenerator;
import org.dkpro.jwpl.timemachine.dump.version.DumpVersionFastUtilIntKey;
import org.dkpro.jwpl.timemachine.dump.xml.TimeMachineRevisionParser;
import org.dkpro.jwpl.timemachine.dump.xml.XMLDumpTableInputStream;
import org.dkpro.jwpl.wikimachine.domain.ISnapshotGenerator;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableInputStream;
import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;
import org.dkpro.jwpl.wikimachine.factory.AbstractEnvironmentFactory;
import org.dkpro.jwpl.wikimachine.factory.IEnvironmentFactory;

public class DefaultTimeMachineEnvironmentFactory extends AbstractEnvironmentFactory
    implements IEnvironmentFactory
{

    private static ISnapshotGenerator SNAPSHOTGENERATOR_BEAN;
    private static RevisionParser REVISIONPARSER_BEAN;

    private static final DefaultTimeMachineEnvironmentFactory INSTANCE = new DefaultTimeMachineEnvironmentFactory();

    // Note: Can't enforce a singleton-like private ctor, as ServiceLoader mechanism requires public no-arg form...
    public DefaultTimeMachineEnvironmentFactory() {}

    public static DefaultTimeMachineEnvironmentFactory getInstance()
    {
        return INSTANCE;
    }

    @Override
    public synchronized ISnapshotGenerator getSnapshotGenerator()
    {
        if (SNAPSHOTGENERATOR_BEAN == null) {
            SNAPSHOTGENERATOR_BEAN = new TimeMachineGenerator(INSTANCE);
        }
        return SNAPSHOTGENERATOR_BEAN;
    }

    @Override
    public synchronized IDumpVersion getDumpVersion()
    {
        // prototype beans required
        DumpVersionFastUtilIntKey d = new DumpVersionFastUtilIntKey();
        d.setLogger(getLogger());
        return d;
    }

    @Override
    public synchronized DumpTableInputStream getDumpTableInputStream()
    {
        // prototype beans required
        return new XMLDumpTableInputStream();
    }

    @Override
    public synchronized RevisionParser getRevisionParser()
    {
        if (REVISIONPARSER_BEAN == null) {
            REVISIONPARSER_BEAN = new TimeMachineRevisionParser();
        }
        return REVISIONPARSER_BEAN;
    }

}
