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
package org.dkpro.jwpl.wikimachine.dump.sql;

import java.util.HashMap;
import java.util.Map;

/**
 * A trivial in-memory {@link LinkTargetResolver} for the parser tests.
 */
final class StubLinkTargetResolver
    implements LinkTargetResolver
{

    private final Map<Long, String> titles = new HashMap<>();
    private final Map<Long, Integer> namespaces = new HashMap<>();

    StubLinkTargetResolver add(long ltId, int namespace, String title)
    {
        titles.put(ltId, title);
        namespaces.put(ltId, namespace);
        return this;
    }

    @Override
    public String getTitle(long ltId)
    {
        return titles.get(ltId);
    }

    @Override
    public int getNamespace(long ltId)
    {
        return namespaces.getOrDefault(ltId, NAMESPACE_UNKNOWN);
    }

    @Override
    public long size()
    {
        return titles.size();
    }
}
