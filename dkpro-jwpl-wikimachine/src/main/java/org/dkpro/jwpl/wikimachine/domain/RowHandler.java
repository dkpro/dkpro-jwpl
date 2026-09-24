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

import java.io.IOException;

import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;

/**
 * Hands the row a parser is positioned on to a single {@link IDumpVersion}, e.g.
 * {@link IDumpVersion#processPageRow}.
 *
 * @param <P> The type of the parser.
 */
@FunctionalInterface
interface RowHandler<P>
{

    /**
     * @param version The version to process the row.
     * @param parser  The parser positioned on the row.
     * @throws IOException Thrown if the version fails to process the row.
     */
    void handle(IDumpVersion version, P parser) throws IOException;
}
