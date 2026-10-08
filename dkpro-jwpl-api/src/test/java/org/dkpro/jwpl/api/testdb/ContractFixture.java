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
package org.dkpro.jwpl.api.testdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The names the name lookup contract is tested against. They are loaded next to
 * {@code db/data.sql} into the variant databases only, so the counts the other tests assert on the
 * shared fixture stay untouched.
 * <p>
 * Names are written with Unicode escapes on purpose: several differ from others only by
 * characters that look alike or are invisible.
 */
public final class ContractFixture
{
    /** The kind of row a {@link Stored} name is. */
    public enum Kind
    {
        /** A {@code PageMapLine} entry plus the {@code Page} it maps to. */
        PAGE,
        /** A {@code Category}. */
        CATEGORY
    }

    /**
     * A stored name.
     *
     * @param kind      The kind of row.
     * @param id        The primary key; for {@link Kind#PAGE} the id of the {@code PageMapLine}
     *                  entry, which is also the id of its {@code Page} for rows of this fixture.
     * @param pageId    The page id.
     * @param name      The name as stored.
     * @param preloaded Whether the row comes from {@code db/data.sql} and is only listed here so
     *                  the oracle knows it.
     */
    public record Stored(Kind kind, int id, int pageId, String name, boolean preloaded)
    {
    }

    /** The number of generated filler rows per table, so that a scan is measurably worse. */
    public static final int FILLER_ROWS = 10_000;

    private static final int FILLER_BASE_ID = 900_000;

    /** The stored names, in insertion order. */
    public static final List<Stored> STORED;

    /** The categories of the pages of this fixture: page id to category page ids. */
    public static final Map<Integer, List<Integer>> PAGE_CATEGORIES = Map.of(800001,
            List.of(800001));

    static {
        List<Stored> s = new ArrayList<>();
        // rows of db/data.sql that probes of the contract hit
        s.add(new Stored(Kind.PAGE, 3, 105, "TK2", true));
        s.add(new Stored(Kind.PAGE, 9, 1014, "Wikipedia_API", true));
        s.add(new Stored(Kind.PAGE, 41, 101, "Ambiguous_Title", true));
        s.add(new Stored(Kind.PAGE, 42, 1017, "Ambiguous_Title", true));

        // stored both as page and as category
        String[][] titles = {
            { "800001", "Mixed_Case" },
            { "800002", "Mixed_case" },
            { "800003", "MIXED_CASE" },
            { "800010", "Foo_bar_probe" },
            { "800011", "Foo_Bar_probe" },
            { "800020", "Köln_probe" },
            { "800021", "Straße_probe" },
            { "800022", "Café_probe" },
            { "800023", "Æther_probe" },
            { "800024", "İstanbul_probe" },
            { "800030", "Ｆullwidth_probe" },
            { "800031", "Soft­hyphen_probe" },
            { "800032", "Zw​sp_probe" },
            { "800040", "Emoji_😀_probe" },
            { "800041", "𠀀" },
            { "800050", "Trailing_probe " },
        };
        for (String[] t : titles) {
            int id = Integer.parseInt(t[0]);
            s.add(new Stored(Kind.PAGE, id, id, t[1], false));
            s.add(new Stored(Kind.CATEGORY, id, id, t[1], false));
        }

        // duplicate category names: the higher id is inserted first
        s.add(new Stored(Kind.CATEGORY, 9004, 9004, "Dup_Cat", false));
        s.add(new Stored(Kind.CATEGORY, 9003, 9003, "Dup_Cat", false));

        // LIKE traps
        s.add(new Stored(Kind.PAGE, 800060, 800060, "Under_scoreA", false));
        s.add(new Stored(Kind.PAGE, 800061, 800061, "UnderXscoreA", false));
        s.add(new Stored(Kind.PAGE, 800062, 800062, "Pct%Literal", false));
        s.add(new Stored(Kind.PAGE, 800063, 800063, "Discussion:Under_scoreA/Archive_1", false));
        s.add(new Stored(Kind.PAGE, 800064, 800064, "Discussion:UnderXscoreA/Archive_1", false));
        STORED = Collections.unmodifiableList(s);
    }

    private ContractFixture()
    {
        // constants and loader only
    }

    /**
     * Loads the rows of this fixture that {@code profile} can hold, plus the filler rows.
     *
     * @param c       A connection to the variant database.
     * @param profile The profile of the name columns.
     * @throws SQLException If a row cannot be inserted.
     */
    static void load(Connection c, ColumnProfile profile) throws SQLException
    {
        try (PreparedStatement pml = c.prepareStatement(
                "INSERT INTO PageMapLine (id, name, pageID) VALUES (?, ?, ?)");
                PreparedStatement page = c.prepareStatement("INSERT INTO Page"
                        + " (id, pageId, name, text, isDisambiguation) VALUES (?, ?, ?, ?, ?)");
                PreparedStatement cat = c.prepareStatement(
                        "INSERT INTO Category (id, pageId, name) VALUES (?, ?, ?)");
                PreparedStatement pageCat = c.prepareStatement(
                        "INSERT INTO page_categories (id, pages) VALUES (?, ?)")) {
            // one row at a time, so the insertion order is the listed one
            for (Stored s : STORED) {
                if (s.preloaded() || !profile.canStore(s.name())) {
                    continue;
                }
                switch (s.kind()) {
                    case PAGE -> {
                        pml.setInt(1, s.id());
                        pml.setString(2, s.name());
                        pml.setInt(3, s.pageId());
                        pml.executeUpdate();
                        page.setInt(1, s.id());
                        page.setInt(2, s.pageId());
                        page.setString(3, s.name());
                        page.setString(4, "text of " + s.id());
                        page.setBoolean(5, false);
                        page.executeUpdate();
                    }
                    case CATEGORY -> {
                        cat.setInt(1, s.id());
                        cat.setInt(2, s.pageId());
                        cat.setString(3, s.name());
                        cat.executeUpdate();
                    }
                }
            }
            for (Map.Entry<Integer, List<Integer>> e : PAGE_CATEGORIES.entrySet()) {
                for (int category : e.getValue()) {
                    pageCat.setInt(1, e.getKey());
                    pageCat.setInt(2, category);
                    pageCat.executeUpdate();
                }
            }
            for (int i = 0; i < FILLER_ROWS; i++) {
                int id = FILLER_BASE_ID + i;
                String name = String.format("Filler_%05d_%s", i, i % 2 == 0 ? "a" : "B");
                pml.setInt(1, id);
                pml.setString(2, name);
                pml.setInt(3, id);
                pml.addBatch();
                cat.setInt(1, id);
                cat.setInt(2, id);
                cat.setString(3, name);
                cat.addBatch();
            }
            pml.executeBatch();
            cat.executeBatch();
        }
    }
}
