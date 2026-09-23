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
package org.dkpro.jwpl.revisionmachine.api.chrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.junit.jupiter.api.Test;

/**
 * Checks the size accounting of {@link ChronoStorageBlock}.
 */
public class ChronoStorageBlockTest
{

    @Test
    public void testLengthMatchesEscapedRevisionText()
    {
        Revision revision = new Revision(3);
        revision.setRevisionText("a &amp; b &lt;c&gt;");

        ChronoStorageBlock block = new ChronoStorageBlock(null, 3, revision);

        // The storage keeps the escaped text, which is also the base for rebuilding the next
        // revisions, so its size is accounted with the escaped length.
        assertEquals(revision.byteSize(), block.length());
        assertEquals("a &amp; b &lt;c&gt;".length(), block.length());
    }

    @Test
    public void testLengthIsStableForAddAndRemoveAccounting()
    {
        Revision revision = new Revision(1);
        revision.setRevisionText("plain text");

        ChronoStorageBlock block = new ChronoStorageBlock(null, 1, revision);
        int added = block.length();

        // A consumer may replace the text of a revision it received.
        revision.setRevisionText("a longer replacement text");

        assertEquals(added, block.length());
    }
}
