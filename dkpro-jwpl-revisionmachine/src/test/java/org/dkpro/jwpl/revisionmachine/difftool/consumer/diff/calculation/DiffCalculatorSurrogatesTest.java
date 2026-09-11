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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dkpro.jwpl.revisionmachine.common.exceptions.DiffException;
import org.dkpro.jwpl.revisionmachine.difftool.data.SurrogateModes;
import org.junit.jupiter.api.Test;

/**
 * Tests how each surrogate mode treats the text of a revision before it is diffed (see issue
 * #282).
 */
class DiffCalculatorSurrogatesTest
{

    private static final int REVISION_ID = 4711;

    // U+1F600 (grinning face) is encoded as the surrogate pair D83D DE00.
    private static final String PAIR = "\uD83D\uDE00";
    private static final String LONE_HIGH = "\uD83D";

    private static final String WITH_PAIR = "before " + PAIR + " after";
    private static final String WITH_LONE_HIGH = "before " + LONE_HIGH + " after";

    private static String handle(SurrogateModes mode, String text) throws DiffException
    {
        char[] result = DiffCalculator.handleSurrogates(mode, text.toCharArray(), REVISION_ID);
        return result == null ? null : new String(result);
    }

    @Test
    void everyModeKeepsTextWithoutSurrogates() throws DiffException
    {
        String text = "K\u00E4se \u65E5\u672C";
        for (SurrogateModes mode : SurrogateModes.values()) {
            assertEquals(text, handle(mode, text), mode.name());
        }
    }

    @Test
    void noModeKeepsTextWithSurrogates() throws DiffException
    {
        assertEquals(WITH_PAIR, handle(null, WITH_PAIR));
    }

    @Test
    void replaceModeReplacesTheSurrogates() throws DiffException
    {
        assertEquals("before ?? after", handle(SurrogateModes.REPLACE, WITH_PAIR));
        assertEquals("before ? after", handle(SurrogateModes.REPLACE, WITH_LONE_HIGH));
    }

    @Test
    void discardRestModeKeepsTheTextBeforeTheFirstSurrogate() throws DiffException
    {
        assertEquals("before ", handle(SurrogateModes.DISCARD_REST, WITH_PAIR));
        assertEquals("before ", handle(SurrogateModes.DISCARD_REST, WITH_LONE_HIGH));
    }

    @Test
    void discardRevisionModeDiscardsTheRevision() throws DiffException
    {
        assertNull(handle(SurrogateModes.DISCARD_REVISION, WITH_PAIR));
        assertNull(handle(SurrogateModes.DISCARD_REVISION, WITH_LONE_HIGH));
    }

    @Test
    void throwErrorModeThrowsNamingTheRevision()
    {
        DiffException e = assertThrows(DiffException.class,
                () -> handle(SurrogateModes.THROW_ERROR, WITH_PAIR));
        assertTrue(e.getMessage().contains(String.valueOf(REVISION_ID)), e.getMessage());
        assertThrows(DiffException.class,
                () -> handle(SurrogateModes.THROW_ERROR, WITH_LONE_HIGH));
    }
}
