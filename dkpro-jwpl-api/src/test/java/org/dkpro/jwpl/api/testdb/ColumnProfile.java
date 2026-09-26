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

import static org.dkpro.jwpl.api.testdb.JwplTestDatabase.Engine.HSQLDB;
import static org.dkpro.jwpl.api.testdb.JwplTestDatabase.Engine.MARIADB;
import static org.dkpro.jwpl.api.testdb.JwplTestDatabase.Engine.MYSQL;
import static org.dkpro.jwpl.api.testdb.JwplTestDatabase.Engine.POSTGRESQL;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.dkpro.jwpl.api.testdb.JwplTestDatabase.Engine;

/**
 * A collation or charset of the name columns ({@code PageMapLine.name}, {@code Page.name} and
 * {@code Category.name}) that the name lookups are tested against. Each profile gets its own
 * database, see {@link JwplTestDatabase#provisionVariant(ColumnProfile)}; the shared fixture is
 * never altered.
 * <p>
 * The profiles run for an engine can be narrowed with the system property
 * {@value #PROFILES_PROPERTY}, a comma separated list of profile names.
 */
public enum ColumnProfile
{
    /** The collation the server gives a column that declares none, as the test schemas do. */
    DEFAULT(EnumSet.allOf(Engine.class), null, null),
    /** HSQLDB's case-insensitive string type; runs the Java-side filters without Docker. */
    IGNORECASE(EnumSet.of(HSQLDB), null, null),
    /** The binary collation the old queries forced; the only one the prefix seek applies to. */
    MB4_BIN(EnumSet.of(MARIADB, MYSQL), "utf8mb4", "utf8mb4_bin"),
    /** Folds all supplementary characters together; the default of MariaDB 10.x. */
    MB4_GENERAL_CI(EnumSet.of(MARIADB, MYSQL), "utf8mb4", "utf8mb4_general_ci"),
    /** The legacy JWPL layout, {@code DEFAULT CHARSET=utf8}. */
    MB3_GENERAL_CI(EnumSet.of(MARIADB, MYSQL), "utf8mb3", "utf8mb3_general_ci"),
    /** A single byte charset that cannot hold most titles. */
    LATIN1_SWEDISH_CI(EnumSet.of(MARIADB, MYSQL), "latin1", "latin1_swedish_ci"),
    /** A case-insensitive, nondeterministic ICU collation. */
    ICU_CI_ND(EnumSet.of(POSTGRESQL), null, "jwpl_ci");

    public static final String PROFILES_PROPERTY = "jwpl.test.collation.profiles";

    static final List<String> NAME_COLUMNS = List.of("PageMapLine", "Page", "Category");

    private final Set<Engine> engines;
    private final String charset;
    private final String collation;

    ColumnProfile(Set<Engine> engines, String charset, String collation)
    {
        this.engines = engines;
        this.charset = charset;
        this.collation = collation;
    }

    /**
     * @param engine The engine under test.
     * @return The profiles to test on {@code engine}, narrowed by {@value #PROFILES_PROPERTY}.
     */
    public static List<ColumnProfile> forEngine(Engine engine)
    {
        String raw = System.getProperty(PROFILES_PROPERTY, "").trim();
        Set<String> selected = raw.isEmpty() ? Set.of()
                : Arrays.stream(raw.split(",")).map(s -> s.trim().toUpperCase(Locale.ROOT))
                        .collect(Collectors.toSet());
        List<ColumnProfile> result = new ArrayList<>();
        for (ColumnProfile p : values()) {
            if (p.engines.contains(engine) && (selected.isEmpty() || selected.contains(p.name()))) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * @param engine An engine.
     * @return Whether this profile can be applied on {@code engine}.
     */
    public boolean appliesTo(Engine engine)
    {
        return engines.contains(engine);
    }

    /**
     * @param engine The engine the profile is applied on.
     * @return The statements that give the name columns this profile, run after the schema and
     *         before any data is loaded.
     */
    public List<String> ddl(Engine engine)
    {
        List<String> ddl = new ArrayList<>();
        switch (this) {
            case DEFAULT -> {
                // the columns keep the server default
            }
            case IGNORECASE -> NAME_COLUMNS.forEach(t -> ddl.add("ALTER TABLE " + t
                    + " ALTER COLUMN name SET DATA TYPE VARCHAR_IGNORECASE(255)"));
            case MB4_BIN, MB4_GENERAL_CI, MB3_GENERAL_CI, LATIN1_SWEDISH_CI -> NAME_COLUMNS
                    .forEach(t -> ddl.add("ALTER TABLE " + t + " MODIFY name VARCHAR(255)"
                            + " CHARACTER SET " + charset + " COLLATE " + collation));
            case ICU_CI_ND -> {
                ddl.add("CREATE COLLATION IF NOT EXISTS jwpl_ci (provider = icu,"
                        + " locale = 'und-u-ks-level2', deterministic = false)");
                NAME_COLUMNS.forEach(t -> ddl.add("ALTER TABLE " + t
                        + " ALTER COLUMN name TYPE VARCHAR(255) COLLATE jwpl_ci"));
            }
        }
        return ddl;
    }

    /**
     * @param engine The engine under test.
     * @return The collation the name columns of a database with this profile must report, see
     *         {@code SchemaAssumptionTest}.
     */
    public String expectedCollation(Engine engine)
    {
        if (collation != null) {
            return collation;
        }
        if (this == IGNORECASE) {
            return "SQL_TEXT_UCC";
        }
        return switch (engine) {
            case HSQLDB -> "SQL_TEXT";
            case MARIADB -> "utf8mb4_uca1400_ai_ci";
            case MYSQL -> "utf8mb4_0900_ai_ci";
            case POSTGRESQL -> "en_US.utf8";
        };
    }

    /**
     * @param name A name.
     * @return Whether a name column of this profile can hold {@code name}. Rows it cannot hold are
     *         not stored; probes with such names are still run.
     */
    public boolean canStore(String name)
    {
        if ("latin1".equals(charset)) {
            return StandardCharsets.ISO_8859_1.newEncoder().canEncode(name);
        }
        if ("utf8mb3".equals(charset)) {
            return name.codePoints().allMatch(Character::isBmpCodePoint);
        }
        return true;
    }
}
