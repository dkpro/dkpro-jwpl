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
package org.dkpro.jwpl.revisionmachine.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.OutputTypes;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.codec.SQLEncoder;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.codec.SQLEncoding;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer.SQLDatabaseWriter;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionCodecData;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffAction;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.info.ArticleInformation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;

/**
 * Checks the DDL of the revisions table and the article index handling of the DiffTool and the
 * IndexGenerator against a real MariaDB or MySQL server.
 * <p>
 * The {@code jwpl.test.db} system property picks the server ({@code mariadb} or {@code mysql}).
 * The test is skipped for the default {@code hsqldb} engine, which does not understand the MySQL
 * specific DDL, and if Docker is not available.
 */
public class RevisionsTableIndexTest
{

    private static final String PASSWORD = "jwpl";

    private static final String[] INDEX_TABLES = { "index_articleID_rc_ts", "index_revisionID",
            "index_chronological" };

    /**
     * DDL of the revisions table as created by earlier versions, i.e. without the article index
     * (and with the engine syntax fixed, as the original one does not run on current servers).
     */
    private static final String LEGACY_TABLE = "CREATE TABLE revisions ("
            + "PrimaryKey INT UNSIGNED NOT NULL AUTO_INCREMENT, "
            + "FullRevisionID INTEGER UNSIGNED NOT NULL, "
            + "RevisionCounter INTEGER UNSIGNED NOT NULL, "
            + "RevisionID INTEGER UNSIGNED NOT NULL, ArticleID INTEGER UNSIGNED NOT NULL, "
            + "Timestamp BIGINT NOT NULL, Revision MEDIUMTEXT NOT NULL, "
            + "Comment MEDIUMTEXT, Minor TINYINT NOT NULL, "
            + "ContributorName TEXT NOT NULL, ContributorId INTEGER UNSIGNED, "
            + "ContributorIsRegistered TINYINT NOT NULL, Namespace INTEGER, "
            + "PRIMARY KEY(PrimaryKey)"
            + ") ENGINE = MyISAM DEFAULT CHARSET utf8 COLLATE utf8_general_ci;";

    /**
     * Migration of existing databases to the composite timestamp index, as given in the README.
     */
    private static final String MIGRATE_TIMESTAMP_INDEX = "ALTER TABLE revisions"
            + " ADD INDEX articleTsIdx (ArticleID, Timestamp, RevisionCounter);";

    private static JdbcDatabaseContainer<?> container;

    private static ConfigSettings settings;

    private static int databaseCounter;

    @TempDir
    Path tempDir;

    @BeforeAll
    public static void startDatabase()
    {
        String engine = System.getProperty("jwpl.test.db", "hsqldb").trim()
                .toLowerCase(Locale.ROOT);
        assumeTrue(engine.equals("mariadb") || engine.equals("mysql"),
                "Requires jwpl.test.db=mariadb or jwpl.test.db=mysql");
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Requires Docker");

        container = engine.equals("mariadb") ? new MariaDBContainer<>("mariadb:11.4")
                : new MySQLContainer<>("mysql:8.4");
        container.withUsername("root").withPassword(PASSWORD).start();

        settings = new ConfigSettings();
        settings.defaultConfiguration();
        settings.setConfigParameter(ConfigurationKeys.SQL_HOST,
                container.getHost() + ":" + container.getFirstMappedPort());
        settings.setConfigParameter(ConfigurationKeys.SQL_USERNAME, "root");
        settings.setConfigParameter(ConfigurationKeys.SQL_PASSWORD, PASSWORD);
    }

    @BeforeEach
    public void setUpConfiguration()
    {
        new ConfigurationManager(settings);
    }

    @AfterAll
    public static void stopDatabase()
    {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    public void testDiffToolDeclaresArticleIndexAndBuildsItAfterTheLoad() throws Exception
    {
        String database = createDatabase();
        settings.setConfigParameter(ConfigurationKeys.SQL_DATABASE, database);

        SQLDatabaseWriter writer = new SQLDatabaseWriter(null);
        try (Connection connection = connect(database)) {
            assertEquals("MyISAM", engine(connection));
            assertEquals(List.of("ArticleID", "RevisionCounter"), articleIndex(connection));
            assertEquals("disabled", articleIndexComment(connection));

            for (Task<Diff> task : tasks()) {
                writer.process(task);
            }
            writer.close();

            assertEquals("", articleIndexComment(connection));
            assertEquals(9, count(connection, "revisions"));
        }
    }

    @Test
    public void testIndexGeneratorKeepsDeclaredArticleIndex() throws Exception
    {
        String database = createDatabase(new SQLEncoder(null).getTable());
        generate(database, OutputTypes.DATABASE);

        try (Connection connection = connect(database)) {
            assertEquals(List.of("ArticleID", "RevisionCounter"), articleIndex(connection));
            assertEquals("", articleIndexComment(connection));
            assertEquals(List.of("ArticleID", "Timestamp", "RevisionCounter"),
                    indexColumns(connection, "articleTsIdx"));
            assertIndexTables(connection);
        }
    }

    @Test
    public void testIndexGeneratorCreatesMissingArticleIndex() throws Exception
    {
        String database = createDatabase(LEGACY_TABLE);
        generate(database, OutputTypes.DATABASE);

        try (Connection connection = connect(database)) {
            assertEquals(List.of("ArticleID", "RevisionCounter"), articleIndex(connection));
            assertEquals(List.of("ArticleID", "Timestamp", "RevisionCounter"),
                    indexColumns(connection, "articleTsIdx"));
            assertIndexTables(connection);
        }
    }

    @Test
    public void testIndexSqlFileKeepsDeclaredArticleIndex() throws Exception
    {
        String database = createDatabase(new SQLEncoder(null).getTable());
        generate(database, OutputTypes.SQL);

        try (Connection connection = connect(database)) {
            runIndexSqlFile(connection);
            assertEquals(List.of("ArticleID", "RevisionCounter"), articleIndex(connection));
            assertEquals("", articleIndexComment(connection));
            assertEquals(List.of("ArticleID", "Timestamp", "RevisionCounter"),
                    indexColumns(connection, "articleTsIdx"));
            assertIndexTables(connection);
        }
    }

    @Test
    public void testIndexSqlFileCreatesMissingArticleIndex() throws Exception
    {
        String database = createDatabase(LEGACY_TABLE);
        generate(database, OutputTypes.SQL);

        try (Connection connection = connect(database)) {
            runIndexSqlFile(connection);
            assertEquals(List.of("ArticleID", "RevisionCounter"), articleIndex(connection));
            assertEquals(List.of("ArticleID", "Timestamp", "RevisionCounter"),
                    indexColumns(connection, "articleTsIdx"));
            assertIndexTables(connection);
        }
    }

    @Test
    public void testIndexGeneratorKeepsExistingTimestampIndex() throws Exception
    {
        String database = createDatabase(LEGACY_TABLE, MIGRATE_TIMESTAMP_INDEX);
        generate(database, OutputTypes.DATABASE);

        try (Connection connection = connect(database)) {
            assertEquals(List.of("ArticleID", "RevisionCounter"), articleIndex(connection));
            assertEquals(List.of("ArticleID", "Timestamp", "RevisionCounter"),
                    indexColumns(connection, "articleTsIdx"));
            assertIndexTables(connection);
        }
    }

    @Test
    public void testIndexSqlFileKeepsExistingTimestampIndex() throws Exception
    {
        String database = createDatabase(LEGACY_TABLE, MIGRATE_TIMESTAMP_INDEX);
        generate(database, OutputTypes.SQL);

        try (Connection connection = connect(database)) {
            runIndexSqlFile(connection);
            assertEquals(List.of("ArticleID", "RevisionCounter"), articleIndex(connection));
            assertEquals(List.of("ArticleID", "Timestamp", "RevisionCounter"),
                    indexColumns(connection, "articleTsIdx"));
            assertIndexTables(connection);
        }
    }

    /**
     * Creates a new database and fills its revisions table the way the DiffTool does.
     */
    private static String createDatabase(String... tableStatements) throws Exception
    {
        String database = createDatabase();
        try (Connection connection = connect(database);
                Statement statement = connection.createStatement()) {
            for (String sql : tableStatements) {
                statement.execute(sql);
            }
            SQLEncoder encoder = new SQLEncoder(null);
            for (Task<Diff> task : tasks()) {
                for (SQLEncoding encoding : encoder.encodeTask(task)) {
                    statement.executeUpdate(encoding.getQuery());
                }
            }
        }
        return database;
    }

    private static String createDatabase() throws SQLException
    {
        String database = "revisions_" + (++databaseCounter);
        try (Connection connection = connect("");
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + database);
        }
        return database;
    }

    private static Connection connect(String database) throws SQLException
    {
        return DriverManager.getConnection(jdbcUrl(database), "root", PASSWORD);
    }

    private static String jdbcUrl(String database)
    {
        return "jdbc:mysql://" + container.getHost() + ":" + container.getFirstMappedPort() + "/"
                + database + "?allowPublicKeyRetrieval=true&useSSL=false&allowMultiQueries=true";
    }

    private void generate(String database, OutputTypes outputType) throws Exception
    {
        RevisionAPIConfiguration config = new RevisionAPIConfiguration();
        config.setJdbcURL(jdbcUrl(database));
        config.setDatabaseDriver("com.mysql.cj.jdbc.Driver");
        config.setUser("root");
        config.setPassword(PASSWORD);
        config.setOutputType(outputType);
        config.setOutputPath(tempDir.toString());
        new IndexGenerator(config).generate();
    }

    private void runIndexSqlFile(Connection connection) throws Exception
    {
        String sql = Files.readString(tempDir.resolve("revisionIndex.sql"),
                StandardCharsets.UTF_8);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            while (statement.getMoreResults() || statement.getUpdateCount() != -1) {
                // consume the results of all statements of the file
            }
        }
    }

    /**
     * Checks the index tables against the content written for {@link #tasks()}, which does not
     * depend on whether the article index was declared up front or created afterwards.
     */
    private static void assertIndexTables(Connection connection) throws SQLException
    {
        assertEquals(List.of("1|1 2 3|1 1 2 2 3 3", "2|4 5 6|1 1 2 2 3 3", "3|7 8 9|1 1 2 2 3 3"),
                rows(connection, "SELECT ArticleID, FullRevisionPKs, RevisionCounter"
                        + " FROM index_articleID_rc_ts ORDER BY ArticleID"));
        assertEquals(9, count(connection, "index_revisionID"));
        assertEquals(List.of("101|1|1", "102|2|2", "103|3|3", "201|4|4", "301|7|7"),
                rows(connection, "SELECT RevisionID, RevisionPK, FullRevisionPK"
                        + " FROM index_revisionID WHERE RevisionID IN (101, 102, 103, 201, 301)"
                        + " ORDER BY RevisionID"));
        for (String table : INDEX_TABLES) {
            try (Statement statement = connection.createStatement();
                    ResultSet result = statement
                            .executeQuery("SHOW INDEX FROM " + table + " WHERE Comment = 'disabled'")) {
                assertFalse(result.next(), table);
            }
        }
    }

    private static List<String> articleIndex(Connection connection) throws SQLException
    {
        return indexColumns(connection, "articleIdx");
    }

    private static List<String> indexColumns(Connection connection, String index)
        throws SQLException
    {
        List<String> columns = new ArrayList<>();
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(
                        "SHOW INDEX FROM revisions WHERE Key_name = '" + index + "'")) {
            while (result.next()) {
                columns.add(result.getString("Column_name"));
            }
        }
        return columns;
    }

    private static String articleIndexComment(Connection connection) throws SQLException
    {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(
                        "SHOW INDEX FROM revisions WHERE Key_name = 'articleIdx'")) {
            result.next();
            return result.getString("Comment");
        }
    }

    private static String engine(Connection connection) throws SQLException
    {
        return rows(connection, "SELECT ENGINE FROM information_schema.tables"
                + " WHERE table_schema = DATABASE() AND table_name = 'revisions'").get(0);
    }

    private static int count(Connection connection, String table) throws SQLException
    {
        return Integer.parseInt(rows(connection, "SELECT COUNT(*) FROM " + table).get(0));
    }

    private static List<String> rows(Connection connection, String query) throws SQLException
    {
        List<String> rows = new ArrayList<>();
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(query)) {
            int columns = result.getMetaData().getColumnCount();
            while (result.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= columns; i++) {
                    if (i > 1) {
                        row.append('|');
                    }
                    row.append(result.getString(i));
                }
                rows.add(row.toString());
            }
        }
        return rows;
    }

    /**
     * Creates three articles with three full revisions each.
     */
    private static List<Task<Diff>> tasks()
    {
        List<Task<Diff>> tasks = new ArrayList<>();
        for (int articleId = 1; articleId <= 3; articleId++) {
            ArticleInformation header = new ArticleInformation();
            header.setArticleId(articleId);
            header.setArticleName("Article_" + articleId);
            header.setNamespace(0);

            Task<Diff> task = new Task<>(header, 1);
            for (int counter = 1; counter <= 3; counter++) {
                String text = "Text " + articleId + "." + counter;
                DiffPart part = new DiffPart(DiffAction.FULL_REVISION_UNCOMPRESSED);
                part.setText(text);

                RevisionCodecData codecData = new RevisionCodecData();
                codecData.checkBlocksizeL(text.getBytes(StandardCharsets.UTF_8).length);

                Diff diff = new Diff();
                diff.setRevisionCoutner(counter);
                diff.setRevisionID(articleId * 100 + counter);
                diff.setTimeStamp(new Timestamp(1000L * (counter * 10 + articleId)));
                diff.setContributorName("Someone");
                diff.setContributorIsRegistered(true);
                diff.add(part);
                diff.setCodecData(codecData);
                task.add(diff);
            }
            tasks.add(task);
        }
        return tasks;
    }
}
