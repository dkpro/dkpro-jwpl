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

import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.TaskTransmitterInterface;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests that the {@link DiffCalculator} fetches (and thereby unescapes) the text of a revision
 * only once (see issue #582).
 */
class DiffCalculatorRevisionTextTest
{

    @BeforeAll
    static void setUpConfiguration()
    {
        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        new ConfigurationManager(settings);
    }

    @Test
    void revisionTextIsFetchedOnce() throws Exception
    {
        CountingRevision revision = new CountingRevision("K&auml;se &amp; Brot");

        Diff diff = calculator().processRevision(revision);

        assertEquals(1, revision.calls);
        assertEquals("Käse & Brot", diff.buildRevision((String) null));
    }

    @Test
    void revisionWithoutTextIsSkipped() throws Exception
    {
        CountingRevision revision = new CountingRevision(null);

        assertNull(calculator().processRevision(revision));
        assertEquals(1, revision.calls);
    }

    @Test
    void revisionWithInaccessibleTextIsSkipped() throws Exception
    {
        // Neither text nor RevisionApi: loading the text lazily fails with a NullPointerException.
        assertNull(calculator().processRevision(new Revision(1)));
    }

    private static DiffCalculator calculator() throws Exception
    {
        return new DiffCalculator(new TaskTransmitterInterface()
        {
            @Override
            public void transmitDiff(Task<Diff> result)
            {
                // not used
            }

            @Override
            public void transmitPartialDiff(Task<Diff> result)
            {
                // not used
            }

            @Override
            public void close()
            {
                // not used
            }
        });
    }

    private static final class CountingRevision
        extends Revision
    {

        private static final long serialVersionUID = 1L;

        private final String text;

        private int calls;

        CountingRevision(String text)
        {
            super(1);
            this.text = text;
        }

        @Override
        public String getRevisionText()
        {
            calls++;
            if (text == null) {
                return null;
            }
            setRevisionText(text);
            return super.getRevisionText();
        }
    }
}
