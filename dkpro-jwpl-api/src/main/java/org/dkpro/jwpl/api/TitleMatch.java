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
package org.dkpro.jwpl.api;

/**
 * How {@link Wikipedia#getPageByExactTitle(String, TitleMatch)} matches the given title against
 * the stored wiki-style names. Neither mode converts blanks to underscores or changes the case of
 * any other letter.
 */
public enum TitleMatch
{
    /**
     * The first letter of the title is upper-cased, as Wikipedia titles start with an upper-case
     * letter; the rest is matched as given. {@code tK2} finds the page {@code TK2}. This is the
     * mode of {@link Wikipedia#getPageByExactTitle(String)}.
     */
    CAPITALIZE_FIRST_LETTER,

    /**
     * The title is matched strictly as given. {@code tK2} does not find the page {@code TK2}, but
     * finds a page stored as {@code tK2}.
     */
    AS_GIVEN
}
