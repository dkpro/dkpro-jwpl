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
package org.dkpro.jwpl.api.sweble;

import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;

/**
 * Lazily created, shared English Sweble {@link WikiConfig} used by the no-arg visitor
 * constructors. The instance is only read after creation and is therefore safe to share across
 * threads; it must not be exposed to callers that could mutate it.
 */
final class DefaultSwebleConfig
{
    private DefaultSwebleConfig()
    {
        // utility class
    }

    private static final class Holder
    {
        static final WikiConfig INSTANCE = DefaultConfigEnWp.generate();
    }

    static WikiConfig get()
    {
        return Holder.INSTANCE;
    }
}
