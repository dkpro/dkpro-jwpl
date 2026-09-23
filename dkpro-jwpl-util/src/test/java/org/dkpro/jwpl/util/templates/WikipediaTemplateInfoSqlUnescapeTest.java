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
package org.dkpro.jwpl.util.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dkpro.jwpl.api.util.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * Checks that {@link WikipediaTemplateInfo#sqlUnescape(String)} reverts
 * {@link StringUtils#sqlEscape(String)}, so {@link WikipediaTemplateInfo#checkTemplateId(String)}
 * keeps accepting SQL escaped names.
 */
class WikipediaTemplateInfoSqlUnescapeTest
{

    @Test
    void testUnescapeRevertsSqlEscape()
    {
        String[] names = { "Infobox_person", "Don't", "a\"b", "back\\slash", "tab\there",
                "new\nline", "cr\rlf", "nul\u0000char", "sub\u001achar", "bs\bchar",
                "\\'mixed\\\\'" };
        for (String name : names) {
            assertEquals(name, WikipediaTemplateInfo.sqlUnescape(StringUtils.sqlEscape(name)),
                    name);
        }
    }

    @Test
    void testUnescapeFollowsMySqlLiteralRules()
    {
        assertEquals("x", WikipediaTemplateInfo.sqlUnescape("\\x"));
        assertEquals("it's", WikipediaTemplateInfo.sqlUnescape("it''s"));
        assertEquals("trailing\\", WikipediaTemplateInfo.sqlUnescape("trailing\\"));
        assertEquals("plain", WikipediaTemplateInfo.sqlUnescape("plain"));
    }
}
