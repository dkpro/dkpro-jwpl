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

/**
 * Resolves the {@code lt_id} values of the MediaWiki {@code linktarget} table to the namespace and
 * title of the link target they denote.
 * <p>
 * MediaWiki 1.43 normalised the {@code categorylinks} and {@code pagelinks} tables: the target
 * title columns {@code cl_to} and {@code pl_namespace}/{@code pl_to} were replaced by the foreign
 * keys {@code cl_target_id} and {@code pl_target_id} into the new {@code linktarget} table. A
 * resolver is therefore mandatory when reading such a dump and unused when reading a legacy one.
 *
 * @see LinktargetParser
 * @see FastUtilLinkTargetResolver
 */
public interface LinkTargetResolver
{

    /** Returned by {@link #getNamespace(long)} for an unknown link target id. */
    int NAMESPACE_UNKNOWN = Integer.MIN_VALUE;

    /**
     * @param ltId A {@code linktarget.lt_id} value.
     * @return The SQL escaped, underscore-form, namespace-stripped title registered for
     *         {@code ltId}, or {@code null} if that id is unknown.
     */
    String getTitle(long ltId);

    /**
     * @param ltId A {@code linktarget.lt_id} value.
     * @return The namespace registered for {@code ltId}, or {@link #NAMESPACE_UNKNOWN}.
     */
    int getNamespace(long ltId);

    /**
     * @return The number of link targets held by this resolver.
     */
    long size();
}
