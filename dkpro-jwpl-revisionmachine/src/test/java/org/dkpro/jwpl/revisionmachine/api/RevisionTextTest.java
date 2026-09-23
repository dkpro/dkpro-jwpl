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
package org.dkpro.jwpl.revisionmachine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.jupiter.api.Test;

/**
 * Checks that {@link Revision#getRevisionText()} returns the HTML-unescaped stored text.
 */
public class RevisionTextTest
{

    private static String textOf(final String stored)
    {
        Revision revision = new Revision(1);
        revision.setRevisionText(stored);
        return revision.getRevisionText();
    }

    @Test
    public void testTextWithoutEntitiesIsReturnedUnchanged()
    {
        String stored = "'''Foo''' is a [[bar]] <ref>baz</ref>; 5 < 6 > 4.";
        assertSame(stored, textOf(stored));
        assertEquals("", textOf(""));
    }

    @Test
    public void testTextWithEntitiesIsUnescaped()
    {
        String[] samples = { "a &amp; b", "&lt;ref&gt;x&lt;/ref&gt;", "x&nbsp;y", "&#65;&#x42;",
                "&amp;amp; &amp;nbsp;", "AT&T", "trailing &", "&unknown;" };
        for (String stored : samples) {
            assertEquals(StringEscapeUtils.unescapeHtml4(stored), textOf(stored), stored);
        }
    }

    @Test
    public void testTextIsUpdatedWhenReplaced()
    {
        Revision revision = new Revision(1);
        revision.setRevisionText("a &amp; b");
        assertEquals("a & b", revision.getRevisionText());
        revision.setRevisionText("c &lt; d");
        assertEquals("c < d", revision.getRevisionText());
    }
}
