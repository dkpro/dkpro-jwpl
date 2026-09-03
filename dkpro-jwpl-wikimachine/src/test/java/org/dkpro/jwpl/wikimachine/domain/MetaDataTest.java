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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Timestamp;

import org.dkpro.jwpl.wikimachine.debug.Slf4JLogger;
import org.junit.jupiter.api.Test;

/**
 * Covers the {@code timestamp} / {@code version} contract of the writer side meta data model.
 */
class MetaDataTest
{

    @Test
    void setTimestampDerivesVersion()
    {
        MetaData metaData = new MetaData();
        metaData.setTimestamp(Timestamp.valueOf("2026-01-01 00:00:00"));

        assertEquals("20260101000000", metaData.getVersion());
    }

    @Test
    void setTimestampWithNullLeavesVersionUnsetAndDoesNotThrow()
    {
        MetaData metaData = new MetaData();

        assertDoesNotThrow(() -> metaData.setTimestamp(null));
        assertNull(metaData.getTimestamp());
        assertNull(metaData.getVersion());
    }

    @Test
    void setVersionAfterSetTimestampWins()
    {
        MetaData metaData = new MetaData();
        metaData.setTimestamp(Timestamp.valueOf("2026-01-01 00:00:00"));
        metaData.setVersion("20250601000000");

        assertEquals("20250601000000", metaData.getVersion());
    }

    @Test
    void initWithConfigLeavesTimestampAndVersionUnset()
    {
        Configuration config = new Configuration(new Slf4JLogger());
        config.setLanguage("test");
        config.setMainCategory("Telecooperation");
        config.setDisambiguationCategory("Disambiguation");

        MetaData metaData = MetaData.initWithConfig(config);

        assertEquals("NULL", metaData.getId());
        assertEquals("test", metaData.getLanguage());
        assertEquals("Telecooperation", metaData.getMainCategory());
        assertEquals("Disambiguation", metaData.getDisambiguationCategory());
        assertNull(metaData.getTimestamp());
        assertNull(metaData.getVersion());
    }
}
