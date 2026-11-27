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

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Evaluates {@link EnabledOnEngines}.
 */
public class EnabledOnEnginesCondition
    implements ExecutionCondition
{
    private final DbEngineCondition dockerCondition = new DbEngineCondition();

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context)
    {
        Optional<EnabledOnEngines> annotation = context.getElement()
                .map(e -> e.getAnnotation(EnabledOnEngines.class))
                .or(() -> context.getTestClass().map(c -> c.getAnnotation(EnabledOnEngines.class)));
        if (annotation.isEmpty()) {
            return ConditionEvaluationResult.enabled("No @EnabledOnEngines present");
        }
        JwplTestDatabase.Engine engine = JwplTestDatabase.selectEngine();
        if (Arrays.stream(annotation.get().value()).noneMatch(e -> e == engine)) {
            return ConditionEvaluationResult.disabled("Disabled on " + engine + ", enabled on "
                    + Arrays.toString(annotation.get().value()));
        }
        return dockerCondition.evaluateExecutionCondition(context);
    }
}
