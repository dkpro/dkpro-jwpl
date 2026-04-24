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
package org.dkpro.jwpl.api.testdb;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * Skips tests when the selected DB engine requires Docker but Docker is unavailable.
 * HSQLDB requires no runtime — those tests are always enabled.
 */
public class DbEngineCondition
    implements ExecutionCondition
{
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context)
    {
        JwplTestDatabase.Engine engine = JwplTestDatabase.selectEngine();
        if (engine == JwplTestDatabase.Engine.HSQLDB) {
            return ConditionEvaluationResult.enabled("Using HSQLDB test engine");
        }
        if (DockerClientFactory.instance().isDockerAvailable()) {
            return ConditionEvaluationResult
                    .enabled("Using " + engine + " via Testcontainers");
        }
        return ConditionEvaluationResult.disabled("Disabled: " + JwplTestDatabase.SYSTEM_PROPERTY
                + "=" + engine.name().toLowerCase()
                + " requires Docker, which is not available on this machine.");
    }
}
