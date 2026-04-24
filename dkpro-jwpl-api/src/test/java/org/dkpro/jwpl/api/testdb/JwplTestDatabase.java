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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;

/**
 * Lazily initialized singleton that owns the test database for dkpro-jwpl-api.
 * <p>
 * The {@code jwpl.test.db} system property picks the backend:
 * {@code hsqldb} (default), {@code mariadb}, or {@code mysql}. HSQLDB uses
 * an in-memory instance; {@code mariadb}/{@code mysql} spin up a shared
 * Testcontainers container for the JVM lifetime. All three load the
 * fixture from {@code src/test/resources/db/}.
 */
public final class JwplTestDatabase
{
    public enum Engine { HSQLDB, MARIADB, MYSQL }

    public static final String SYSTEM_PROPERTY = "jwpl.test.db";

    private static final String DB_NAME = "wikiapi_test";
    private static final String DATA_SCRIPT = "db/data.sql";
    private static final String HSQLDB_SCHEMA = "db/schema-hsqldb.sql";
    private static final String MYSQL_SCHEMA = "db/schema-mysql.sql";

    private static volatile JwplTestDatabase instance;

    public static JwplTestDatabase instance()
    {
        JwplTestDatabase local = instance;
        if (local == null) {
            synchronized (JwplTestDatabase.class) {
                local = instance;
                if (local == null) {
                    local = new JwplTestDatabase(selectEngine());
                    local.initialize();
                    instance = local;
                }
            }
        }
        return local;
    }

    public static Engine selectEngine()
    {
        String raw = System.getProperty(SYSTEM_PROPERTY, "hsqldb").trim().toLowerCase(Locale.ROOT);
        return switch (raw) {
            case "", "hsqldb" -> Engine.HSQLDB;
            case "mariadb" -> Engine.MARIADB;
            case "mysql" -> Engine.MYSQL;
            default -> throw new IllegalStateException(
                    "Unknown " + SYSTEM_PROPERTY + " value: '" + raw + "' (expected hsqldb, mariadb, or mysql)");
        };
    }

    private final Engine engine;
    private DatabaseConfiguration configuration;
    private JdbcDatabaseContainer<?> container;

    private JwplTestDatabase(Engine engine)
    {
        this.engine = engine;
    }

    public Engine engine()
    {
        return engine;
    }

    public DatabaseConfiguration configuration()
    {
        return configuration;
    }

    private void initialize()
    {
        switch (engine) {
            case HSQLDB -> initHsqldb();
            case MARIADB -> initContainer(new MariaDBContainer<>("mariadb:11.4"),
                    "org.mariadb.jdbc.Driver");
            case MYSQL -> initContainer(new MySQLContainer<>("mysql:8.4"),
                    "com.mysql.cj.jdbc.Driver");
        }
    }

    private void initHsqldb()
    {
        String jdbcUrl = "jdbc:hsqldb:mem:" + DB_NAME;
        applyScripts(jdbcUrl, "sa", "", HSQLDB_SCHEMA, DATA_SCRIPT);
        configuration = buildConfiguration("org.hsqldb.jdbcDriver", jdbcUrl,
                "localhost", DB_NAME, "sa", "");
    }

    private void initContainer(JdbcDatabaseContainer<?> c, String driverClass)
    {
        this.container = c.withDatabaseName(DB_NAME).withUsername("jwpl").withPassword("jwpl");
        container.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopContainerQuietly,
                "jwpl-testdb-shutdown"));
        applyScripts(container.getJdbcUrl(), container.getUsername(), container.getPassword(),
                MYSQL_SCHEMA, DATA_SCRIPT);
        configuration = buildConfiguration(driverClass, container.getJdbcUrl(),
                container.getHost(), container.getDatabaseName(),
                container.getUsername(), container.getPassword());
    }

    private void stopContainerQuietly()
    {
        if (container != null && container.isRunning()) {
            try {
                container.stop();
            }
            catch (RuntimeException ignored) {
                // best-effort shutdown
            }
        }
    }

    private static DatabaseConfiguration buildConfiguration(String driver, String jdbcUrl,
            String host, String database, String user, String password)
    {
        DatabaseConfiguration db = new DatabaseConfiguration();
        db.setDatabase(database);
        db.setHost(host);
        db.setUser(user);
        db.setPassword(password);
        db.setLanguage(WikiConstants.Language._test);
        db.setJdbcURL(jdbcUrl);
        db.setDatabaseDriver(driver);
        return db;
    }

    private void applyScripts(String jdbcUrl, String user, String password, String... resources)
    {
        try (Connection c = DriverManager.getConnection(jdbcUrl, user, password)) {
            for (String r : resources) {
                executeScript(c, readResource(r));
            }
        }
        catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed to initialize JWPL test database (" + engine + ")", e);
        }
    }

    private static String readResource(String name) throws IOException
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream in = cl.getResourceAsStream(name)) {
            if (in == null) {
                throw new IOException("Test fixture resource not found on classpath: " + name);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Single-pass SQL script runner: splits on {@code ;} while respecting
     * single-quoted strings (with doubled-quote escapes) and {@code --} line
     * comments. This matters because our fixture contains semicolons both in
     * data strings and in header comments.
     */
    private static void executeScript(Connection c, String script) throws SQLException
    {
        StringBuilder stmt = new StringBuilder();
        int i = 0;
        int n = script.length();
        while (i < n) {
            char ch = script.charAt(i);
            // '--' line comment: skip to end of line.
            if (ch == '-' && i + 1 < n && script.charAt(i + 1) == '-') {
                while (i < n && script.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }
            // single-quoted string literal (with doubled-quote escape).
            if (ch == '\'') {
                stmt.append(ch);
                i++;
                while (i < n) {
                    char sc = script.charAt(i);
                    stmt.append(sc);
                    i++;
                    if (sc == '\'') {
                        if (i < n && script.charAt(i) == '\'') {
                            stmt.append('\'');
                            i++;
                        }
                        else {
                            break;
                        }
                    }
                }
                continue;
            }
            if (ch == ';') {
                executeStatement(c, stmt.toString().trim());
                stmt.setLength(0);
                i++;
                continue;
            }
            stmt.append(ch);
            i++;
        }
        executeStatement(c, stmt.toString().trim());
    }

    private static void executeStatement(Connection c, String sql) throws SQLException
    {
        if (sql.isEmpty()) {
            return;
        }
        try (Statement s = c.createStatement()) {
            s.execute(sql);
        }
    }
}
