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
package org.dkpro.jwpl.revisionmachine.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

/**
 * Tests that a directory from a configuration and a generated file name are joined with exactly
 * one separator, whether or not the configuration ends in one (see issue #14).
 */
class FilePathsTest
{

    private static final String SEP = File.separator;

    @Test
    void insertsTheSeparatorAConfigurationDoesNotCarry()
    {
        assertEquals("out" + SEP + "dewiki_1.sql", FilePaths.resolve("out", "dewiki_1.sql"));
    }

    @Test
    void keepsTheSeparatorAConfigurationDoesCarry()
    {
        assertEquals("out" + SEP + "dewiki_1.sql",
                FilePaths.resolve("out" + SEP, "dewiki_1.sql"));
    }

    @Test
    void doesNotDoubleASeparatorTheFragmentCarries()
    {
        assertEquals("out" + SEP + "dewiki_1.sql",
                FilePaths.resolve("out" + SEP, SEP + "dewiki_1.sql"));
        assertEquals("out" + SEP + "dewiki_1.sql", FilePaths.resolve("out", SEP + "dewiki_1.sql"));
    }

    @Test
    void acceptsTheSeparatorOfTheOtherPlatform()
    {
        assertEquals("out/dewiki_1.sql", FilePaths.resolve("out/", "dewiki_1.sql"));
        assertEquals("out\\dewiki_1.sql", FilePaths.resolve("out\\", "dewiki_1.sql"));
    }

    @Test
    void joinsMoreThanTwoFragments()
    {
        assertEquals("log" + SEP + "debug" + SEP + "Anarchism.dbg",
                FilePaths.resolve("log", "debug", "Anarchism.dbg"));
        assertEquals("log" + SEP + "debug" + SEP + "Anarchism.dbg",
                FilePaths.resolve("log" + SEP, "debug" + SEP, "Anarchism.dbg"));
    }

    @Test
    void skipsFragmentsThatAreEmptyOrAbsent()
    {
        assertEquals("log" + SEP + "Anarchism.dbg",
                FilePaths.resolve("log", "", "Anarchism.dbg"));
        assertEquals("log" + SEP + "Anarchism.dbg",
                FilePaths.resolve("log", null, "Anarchism.dbg"));
        assertEquals("Anarchism.dbg", FilePaths.resolve("", "Anarchism.dbg"));
        assertEquals("Anarchism.dbg", FilePaths.resolve(null, "Anarchism.dbg"));
    }
}
