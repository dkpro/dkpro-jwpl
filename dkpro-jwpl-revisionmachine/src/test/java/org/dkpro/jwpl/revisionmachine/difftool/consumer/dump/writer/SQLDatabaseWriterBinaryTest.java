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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.charset.StandardCharsets;
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

import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.api.RevisionApi;
import org.dkpro.jwpl.revisionmachine.api.RevisionIterator;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.OutputTypes;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.SQLEscape;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionCodecData;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffAction;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.info.ArticleInformation;
import org.dkpro.jwpl.revisionmachine.index.IndexGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;

/**
 * Writes the same revisions with the textual and with the binary mode of the
 * {@link SQLDatabaseWriter} to a real MariaDB or MySQL server and reads them back through the
 * {@link RevisionApi} and the {@link RevisionIterator}.
 * <p>
 * The {@code jwpl.test.db} system property picks the server ({@code mariadb} or {@code mysql}).
 * The test is skipped for the default {@code hsqldb} engine, which does not understand the MySQL
 * specific DDL, and if Docker is not available.
 */
public class SQLDatabaseWriterBinaryTest
{

    private static final String PASSWORD = "jwpl";

    private static final int ARTICLES = 3;

    private static final int REVISIONS = 4;

    private static JdbcDatabaseContainer<?> container;

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
    }

    @AfterAll
    public static void stopDatabase()
    {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    public void testBinaryModeStoresTheSameRevisionsAsTextMode() throws Exception
    {
        write("revisions_text", false);
        write("revisions_binary", true);

        try (Connection text = connect("revisions_text");
                Connection binary = connect("revisions_binary")) {
            assertEquals("mediumtext", columnType(text));
            assertEquals("mediumblob", columnType(binary));

            // the binary column saves the base 64 overhead
            assertTrue(revisionBytes(binary) < revisionBytes(text),
                    revisionBytes(binary) + " < " + revisionBytes(text));

            // all columns but the revision are stored identically
            String metaData = "SELECT PrimaryKey, FullRevisionID, RevisionCounter, RevisionID,"
                    + " ArticleID, Timestamp, Comment, Minor, ContributorName, ContributorId,"
                    + " ContributorIsRegistered, Namespace FROM revisions ORDER BY PrimaryKey";
            List<String> rows = rows(text, metaData);
            assertEquals(ARTICLES * REVISIONS, rows.size());
            assertEquals(rows, rows(binary, metaData));
            assertEquals(List.of(comment(1, 1), "Tab\there", name(1, 1)),
                    List.of(value(binary, "SELECT Comment FROM revisions WHERE RevisionID = 101"),
                            value(binary, "SELECT Comment FROM revisions WHERE RevisionID = 102"),
                            value(binary,
                                    "SELECT ContributorName FROM revisions WHERE RevisionID = 101")));
        }

        generateIndexes("revisions_text");
        generateIndexes("revisions_binary");

        List<String> textRevisions = readWithApi("revisions_text");
        assertEquals(expectedTexts(), textRevisions);
        assertEquals(textRevisions, readWithApi("revisions_binary"));

        List<String> iterated = readWithIterator("revisions_text");
        assertEquals(expectedTexts(), iterated);
        assertEquals(iterated, readWithIterator("revisions_binary"));
    }

    private static void write(String database, boolean binary) throws Exception
    {
        try (Connection connection = connect("");
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + database);
        }

        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        settings.setConfigParameter(ConfigurationKeys.SQL_HOST,
                container.getHost() + ":" + container.getFirstMappedPort());
        settings.setConfigParameter(ConfigurationKeys.SQL_USERNAME, "root");
        settings.setConfigParameter(ConfigurationKeys.SQL_PASSWORD, PASSWORD);
        settings.setConfigParameter(ConfigurationKeys.SQL_DATABASE, database);
        settings.setConfigParameter(ConfigurationKeys.VERIFICATION_ENCODING, true);
        settings.setConfigParameter(ConfigurationKeys.MODE_BINARY_OUTPUT_ENABLED, binary);
        new ConfigurationManager(settings);

        SQLDatabaseWriter writer = new SQLDatabaseWriter(null);
        for (Task<Diff> task : tasks()) {
            writer.process(task);
        }
        writer.close();
    }

    private void generateIndexes(String database) throws Exception
    {
        RevisionAPIConfiguration config = apiConfig(database);
        config.setOutputType(OutputTypes.DATABASE);
        config.setOutputPath(tempDir.toString());
        new IndexGenerator(config).generate();
    }

    /**
     * Reads every revision through its revision ID.
     */
    private static List<String> readWithApi(String database) throws Exception
    {
        List<String> texts = new ArrayList<>();
        try (Connection connection = connect(database)) {
            RevisionApi api = new RevisionApi(apiConfig(database), connection);
            for (int article = 1; article <= ARTICLES; article++) {
                for (int counter = 1; counter <= REVISIONS; counter++) {
                    Revision revision = api.getRevision(article * 100 + counter);
                    assertEquals(article, revision.getArticleID());
                    assertEquals(counter, revision.getRevisionCounter());
                    texts.add(revision.getRevisionText());
                }
            }
        }
        return texts;
    }

    /**
     * Reads every revision in the order of the primary key.
     */
    private static List<String> readWithIterator(String database) throws Exception
    {
        List<String> texts = new ArrayList<>();
        RevisionIterator iterator = new RevisionIterator(apiConfig(database));
        try {
            while (iterator.hasNext()) {
                texts.add(iterator.next().getRevisionText());
            }
        }
        finally {
            iterator.close();
        }
        return texts;
    }

    private static RevisionAPIConfiguration apiConfig(String database)
    {
        RevisionAPIConfiguration config = new RevisionAPIConfiguration();
        config.setJdbcURL(jdbcUrl(database));
        config.setDatabaseDriver("com.mysql.cj.jdbc.Driver");
        config.setUser("root");
        config.setPassword(PASSWORD);
        return config;
    }

    private static Connection connect(String database) throws SQLException
    {
        return DriverManager.getConnection(jdbcUrl(database), "root", PASSWORD);
    }

    private static String jdbcUrl(String database)
    {
        return "jdbc:mysql://" + container.getHost() + ":" + container.getFirstMappedPort() + "/"
                + database + "?allowPublicKeyRetrieval=true&useSSL=false";
    }

    private static String columnType(Connection connection) throws SQLException
    {
        return value(connection, "SELECT DATA_TYPE FROM information_schema.columns"
                + " WHERE table_schema = DATABASE() AND table_name = 'revisions'"
                + " AND column_name = 'Revision'");
    }

    private static long revisionBytes(Connection connection) throws SQLException
    {
        return Long.parseLong(value(connection, "SELECT SUM(LENGTH(Revision)) FROM revisions"));
    }

    private static String value(Connection connection, String query) throws SQLException
    {
        return rows(connection, query).get(0);
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

    private static String comment(int article, int counter)
    {
        return "Rev " + article + "." + counter + ": it's a \"quote\" and a back\\slash";
    }

    private static String name(int article, int counter)
    {
        return "O'Neil \\ " + article + "." + counter;
    }

    private static String text(int article)
    {
        return "Text of article " + article + " with 'quotes' and ümläuts.";
    }

    private static String insertion(int counter)
    {
        return " Added " + counter + ".";
    }

    /**
     * Returns the text of each revision in the order of {@link #tasks()}.
     */
    private static List<String> expectedTexts()
    {
        List<String> texts = new ArrayList<>();
        for (int article = 1; article <= ARTICLES; article++) {
            String text = text(article);
            texts.add(text);
            for (int counter = 2; counter <= REVISIONS; counter++) {
                text = text + insertion(counter);
                texts.add(text);
            }
        }
        return texts;
    }

    /**
     * Creates the articles with a full revision each, followed by revisions that append text.
     * Comments and contributor names are SQL-escaped, as done by the XML reader.
     */
    private static List<Task<Diff>> tasks()
    {
        List<Task<Diff>> tasks = new ArrayList<>();
        for (int article = 1; article <= ARTICLES; article++) {
            ArticleInformation header = new ArticleInformation();
            header.setArticleId(article);
            header.setArticleName("Article_" + article);
            header.setNamespace(article == ARTICLES ? null : 0);

            RevisionCodecData codecData = new RevisionCodecData();
            String text = text(article);

            Task<Diff> task = new Task<>(header, 1);
            for (int counter = 1; counter <= REVISIONS; counter++) {
                DiffPart part;
                if (counter == 1) {
                    part = new DiffPart(DiffAction.FULL_REVISION_UNCOMPRESSED);
                    part.setText(text);
                    codecData.checkBlocksizeL(text.getBytes(StandardCharsets.UTF_8).length);
                }
                else {
                    part = new DiffPart(DiffAction.INSERT);
                    part.setStart(text.length());
                    part.setText(insertion(counter));
                    codecData.checkBlocksizeS(text.length());
                    codecData.checkBlocksizeL(
                            insertion(counter).getBytes(StandardCharsets.UTF_8).length);
                    text = text + insertion(counter);
                }

                Diff diff = new Diff();
                diff.setRevisionCoutner(counter);
                diff.setRevisionID(article * 100 + counter);
                diff.setTimeStamp(new Timestamp(1000L * (counter * 10 + article)));
                diff.setComment(counter == 2 ? SQLEscape.escape("Tab\there")
                        : counter == 3 ? null : SQLEscape.escape(comment(article, counter)));
                diff.setMinor(counter % 2 == 0);
                diff.setContributorName(SQLEscape.escape(name(article, counter)));
                diff.setContributorId(counter == 4 ? null : counter);
                diff.setContributorIsRegistered(counter != 4);
                diff.add(part);
                diff.setCodecData(codecData);
                task.add(diff);
            }
            tasks.add(task);
        }
        return tasks;
    }
}
