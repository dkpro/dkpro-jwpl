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
package org.dkpro.jwpl.timemachine.dump.version;

import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersionTimeFactory;
import org.dkpro.jwpl.wikimachine.hashing.StringHashCodeDisabled;

public class DumpVersionJDKStringKeyFactory
    implements IDumpVersionTimeFactory
{

    @Override
    public IDumpVersion getDumpVersion()
    {
        IDumpVersion dumpVersion;
        try {
            dumpVersion = new DumpVersionJDKGeneric<String, StringHashCodeDisabled>(
                    StringHashCodeDisabled.class);
        }
        catch (Exception e) {
            // Only reflective instantiation failures of the hard-wired hash algorithm class can
            // occur here, which cannot happen for a type with a public no-arg constructor. The
            // cause is intentionally not propagated because the factory contract does not permit
            // throwing; a 'null' result signals the (unreachable) failure to the caller.
            dumpVersion = null;
        }
        return dumpVersion;
    }

}
