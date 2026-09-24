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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.function.IntPredicate;

import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.junit.jupiter.api.Test;

import it.unimi.dsi.fastutil.ints.IntSet;

class DumpVersionProcessorTest
{

    @Test
    void wantedTextIdsAreTheUnionOfAllVersions()
    {
        final IntPredicate wanted = wantedTextIds(version(1, 2), version(2, 3));

        assertTrue(wanted.test(1));
        assertTrue(wanted.test(2));
        assertTrue(wanted.test(3));
        assertFalse(wanted.test(4));
    }

    @Test
    void everyTextIdIsWantedIfAVersionCannotTell()
    {
        final IntPredicate wanted = wantedTextIds(version(1), defaultVersion());

        assertTrue(wanted.test(1));
        assertTrue(wanted.test(4));
    }

    private static IntPredicate wantedTextIds(IDumpVersion... versions)
    {
        final DumpVersionProcessor processor = new DumpVersionProcessor(message -> { });
        processor.setDumpVersions(versions);
        return processor.getWantedTextIds();
    }

    private static IDumpVersion version(int... textIds)
    {
        return proxy((proxy, method, args) -> {
            if (!"addWantedTextIds".equals(method.getName())) {
                throw new UnsupportedOperationException(method.getName());
            }
            for (int textId : textIds) {
                ((IntSet) args[0]).add(textId);
            }
            return true;
        });
    }

    private static IDumpVersion defaultVersion()
    {
        return proxy((proxy, method, args) -> {
            if (!method.isDefault()) {
                throw new UnsupportedOperationException(method.getName());
            }
            return InvocationHandler.invokeDefault(proxy, method, args);
        });
    }

    private static IDumpVersion proxy(InvocationHandler handler)
    {
        return (IDumpVersion) Proxy.newProxyInstance(IDumpVersion.class.getClassLoader(),
                new Class<?>[] { IDumpVersion.class }, handler);
    }
}
