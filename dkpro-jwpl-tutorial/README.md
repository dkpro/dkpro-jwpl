<!--
  Licensed to the Technische Universität Darmstadt under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The Technische Universität Darmstadt
  licenses this file to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# JWPL Tutorial

This module contains small, self-contained example programs. Each class has a `main` method and
is compiled as part of the regular build.

| Package                                  | Topic                                              |
|------------------------------------------|----------------------------------------------------|
| `org.dkpro.jwpl.tutorial.api`            | Accessing a JWPL database with the `Wikipedia` API |
| `org.dkpro.jwpl.tutorial.parser`         | Parsing MediaWiki markup                           |
| `org.dkpro.jwpl.tutorial.revisionmachine`| Building and querying a RevisionMachine database   |

The examples use placeholder connection settings (`SERVER_URL`, `DATABASE`, `USER`, `PASSWORD`).
Replace them with your own before running an example.

## RevisionMachine tutorial

The RevisionMachine stores the complete edit history of a Wikipedia in a MySQL or MariaDB
database. Instead of the full text of every revision, it stores the difference to the previous
revision. Every n-th revision of an article is stored in full. The Revision API rebuilds the
text of any revision from the closest full revision and the diffs after it.

Building and using a revision database takes four steps:

```text
 pages-meta-history dump
          |
          |  1. DiffTool           (org.dkpro.jwpl.revisionmachine.difftool.DiffTool)
          v
 SQL or CSV files with the revisions table
          |
          |  2. import             (mysql / LOAD DATA INFILE)
          v
 database with the revisions table
          |
          |  3. IndexGenerator     (org.dkpro.jwpl.revisionmachine.index.IndexGenerator)
          v
 database with the revisions table and the index tables
          |
          |  4. Revision API       (RevisionApi, RevisionIterator, ChronoRevisionIterator)
          v
 your code
```

The example classes for each step are in `org.dkpro.jwpl.tutorial.revisionmachine`:

| Class                        | Shows how to                                                           |
|------------------------------|------------------------------------------------------------------------|
| `T1_DiffTool`                | run the DiffTool with an XML configuration file                        |
| `T2_IndexGenerator`          | generate the index tables with a `RevisionAPIConfiguration`            |
| `T3_RevisionApi`             | look up the revisions of one article with `RevisionApi`                |
| `T4_RevisionIterator`        | iterate over all revisions with `RevisionIterator`                     |
| `T5_ChronoRevisionIterator`  | iterate over the revisions of articles by timestamp with `ChronoRevisionIterator` |
| `T6_PageHistory`             | combine the JWPL `Wikipedia` API with `RevisionApi`                    |

Sample configuration files are in `src/main/resources/revisionmachine/`.

### Prerequisites

* Java 17 or later.
* A MySQL or MariaDB server. The DiffTool creates the `revisions` table with the MyISAM engine.
* A JDBC driver on the classpath of the IndexGenerator and of your own code. The RevisionMachine
  does not ship one. Without a JDBC URL in the configuration it loads `com.mysql.jdbc.Driver`,
  which MySQL Connector/J provides. For another driver, set `setJdbcURL(...)` and
  `setDatabaseDriver(...)` on the `RevisionAPIConfiguration`.
* The executable jar of the RevisionMachine. Build it with `mvn package` in
  `dkpro-jwpl-revisionmachine`. This creates
  `target/dkpro-jwpl-revisionmachine-<version>-jar-with-dependencies.jar`. The commands below
  call it `revisionmachine.jar`.

### Step 1: Download a meta-history dump

The DiffTool reads the XML dumps that contain the complete history of each page. These are the
`pages-meta-history` files on the [Wikimedia download site](https://dumps.wikimedia.org/), for
example `simplewiki-YYYYMMDD-pages-meta-history.xml.bz2`. Large Wikipedias split the history
into many files. The DiffTool reads XML files as they are, bzip2 archives, and 7-Zip archives.
For 7-Zip archives it needs the `7z` command line program.

### Step 2: Configure the DiffTool

The DiffTool reads an XML configuration file. You can edit a copy of
`src/main/resources/revisionmachine/difftool-config.xml`, or create a file with the Swing
application `org.dkpro.jwpl.revisionmachine.difftool.config.gui.ConfigGUI`:

```sh
java -cp revisionmachine.jar org.dkpro.jwpl.revisionmachine.difftool.config.gui.ConfigGUI
```

The sections of the file:

| Element                                         | Meaning                                                                                                                                                         |
|-------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `values/COUNTER_FULL_REVISION`                  | Every n-th revision of an article is stored in full (default 1000).                                                                                            |
| `values/VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING` | Minimum length of a common substring used by the diff algorithm (default 12).                                                                                  |
| `externals/sevenzip`                            | Path to the `7z` program. Needed only for 7-Zip input or output.                                                                                                |
| `input/MODE_SURROGATES`                         | How to handle UTF-16 surrogate characters: `REPLACE`, `THROW_ERROR`, `DISCARD_REST` or `DISCARD_REVISION`.                                                     |
| `input/WIKIPEDIA_ENCODING`                      | Character encoding of the dump, normally `UTF-8`.                                                                                                              |
| `input/archive`                                 | One element per dump file, with `type` (`XML`, `BZIP2` or `SEVENZIP`), `path` and `start`. Archives are processed one after another.                          |
| `output/OUTPUT_MODE`                            | Compression of the output files: `UNCOMPRESSED`, `BZIP2` or `SEVENZIP`.                                                                                         |
| `output/PATH`                                   | Output directory.                                                                                                                                               |
| `output/LIMIT_SQL_FILE_SIZE`                    | Size in bytes at which a new uncompressed output file is started. `LIMIT_SQL_ARCHIVE_SIZE` does the same for compressed output.                                |
| `output/MODE_ZIP_COMPRESSION_ENABLED`           | Compresses the stored diffs.                                                                                                                                    |
| `output/MODE_DATAFILE_OUTPUT`                   | `true` writes CSV files instead of SQL files.                                                                                                                   |
| `cache/*`                                       | Buffer sizes. `LIMIT_SQLSERVER_MAX_ALLOWED_PACKET` limits the size of one `INSERT` statement and should not exceed `max_allowed_packet` of your database server. |
| `logging/root_folder`, `logging/diff_tool/level`| Log directory and log level.                                                                                                                                    |
| `debug/*`                                       | Verification of diffs and encodings and statistical output. Makes processing slower.                                                                             |
| `filter/namespaces/ns`                          | Namespaces to keep. If no `ns` element is given, all namespaces are kept.                                                                                       |

Paths (`archive/path`, `output/PATH`, `logging/root_folder`, `externals/sevenzip`) must be
enclosed in double quotes, for example `<PATH>"/data/revisionmachine/"</PATH>`.

### Step 3: Run the DiffTool

```sh
java -Xmx4g -cp revisionmachine.jar org.dkpro.jwpl.revisionmachine.difftool.DiffTool difftool-config.xml
```

`T1_DiffTool` does the same from Java: it reads the configuration with `ConfigurationReader` and
runs a `DiffToolThread`.

Give the process enough heap space. Processing a large Wikipedia takes days. If the history is
split into several dump files, you can create several configuration files and run one DiffTool
per configuration in parallel.

The DiffTool writes its output to the directory `output/PATH`:

* SQL output: `output_1.sql`, `output_2.sql`, and so on. With `BZIP2` or `SEVENZIP` compression,
  the files end in `.sql.bz2` or `.sql.7z`. Each file starts with a `CREATE TABLE IF NOT EXISTS
  revisions` statement.
* CSV output: `output_1.csv`, and so on. With compression, the files end in `.csv.bz2` or
  `.csv.7z`.

### Step 4: Import the revisions

Create a database with a UTF-8 character set:

```sql
CREATE DATABASE simplewiki_rev DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
```

You can also import the revisions into a JWPL database that the DataMachine created from the
same dump (see `T6_PageHistory`).

**SQL output**: import all files with the MySQL client, for example
`mysql -u USER -p simplewiki_rev < output_1.sql`.

**CSV output**: create the `revisions` table first. This is the same table that the SQL
output creates:

```sql
CREATE TABLE IF NOT EXISTS revisions (
  PrimaryKey INT UNSIGNED NOT NULL AUTO_INCREMENT,
  FullRevisionID INTEGER UNSIGNED NOT NULL,
  RevisionCounter INTEGER UNSIGNED NOT NULL,
  RevisionID INTEGER UNSIGNED NOT NULL,
  ArticleID INTEGER UNSIGNED NOT NULL,
  Timestamp BIGINT NOT NULL,
  Revision MEDIUMTEXT NOT NULL,
  Comment MEDIUMTEXT,
  Minor TINYINT NOT NULL,
  ContributorName TEXT NOT NULL,
  ContributorId INTEGER UNSIGNED,
  ContributorIsRegistered TINYINT NOT NULL,
  Namespace INTEGER,
  PRIMARY KEY(PrimaryKey),
  KEY articleIdx (ArticleID, RevisionCounter),
  KEY articleTsIdx (ArticleID, Timestamp, RevisionCounter)
) ENGINE = MyISAM DEFAULT CHARSET utf8 COLLATE utf8_general_ci;
ALTER TABLE revisions DISABLE KEYS;
```

Then load each file. `LOAD DATA LOCAL INFILE` needs `local_infile` to be enabled on the server
and in the client.

```sql
LOAD DATA LOCAL INFILE '/data/revisionmachine/output_1.csv' INTO TABLE revisions
  CHARACTER SET utf8
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' ESCAPED BY '\\'
  LINES TERMINATED BY ';'
  (PrimaryKey, FullRevisionID, RevisionCounter, RevisionID, ArticleID, Timestamp, Revision,
   Comment, Minor, ContributorName, ContributorId, ContributorIsRegistered, Namespace);
```

The `revisions` table declares its secondary indexes `articleIdx (ArticleID, RevisionCounter)`
and `articleTsIdx (ArticleID, Timestamp, RevisionCounter)` from the start, and its keys are
disabled during the import. MyISAM builds both indexes in one pass when the keys are enabled
again. This is faster than adding each index later with `CREATE INDEX`, because every
`CREATE INDEX` on MyISAM copies the whole table. The IndexGenerator enables the keys in the next
step.

### Step 5: Generate the indexes

The Revision API needs three index tables: `index_articleID_rc_ts`, `index_revisionID` and
`index_chronological`. The IndexGenerator creates them from the `revisions` table. It reads a
properties file, for example `src/main/resources/revisionmachine/indexgenerator.properties`:

```properties
host=localhost
db=simplewiki_rev
user=USER
password=PASSWORD
output=/data/revisionmachine/
outputDatabase=true
outputDatafile=false
charset=UTF-8
buffer=15000
maxAllowedPackets=16760832
```

```sh
java -cp revisionmachine.jar:mysql-connector-j.jar org.dkpro.jwpl.revisionmachine.index.IndexGenerator indexgenerator.properties
```

The output options:

* `outputDatabase=true` writes the index tables directly into the database.
* `outputDatafile=true` writes `articleIndex.csv`, `revisionIndex.csv` and `chronoIndex.csv`
  into the directory `output`.
* If neither option is set, the IndexGenerator writes `revisionIndex.sql` into the directory
  `output`. Import it like the DiffTool output.

After the index tables, the IndexGenerator enables the keys of the `revisions` table. For
`revisions` tables created by older versions of the DiffTool, it also creates `articleIdx` and
`articleTsIdx` if they are missing. With SQL output, these statements are at the end of
`revisionIndex.sql`.

`T2_IndexGenerator` does the same from Java with a `RevisionAPIConfiguration` and
`new IndexGenerator(config).generate()`.

### Step 6: Access the revisions

Create a `RevisionAPIConfiguration` with the connection settings of the revision database:

```java
RevisionAPIConfiguration config = new RevisionAPIConfiguration();
config.setHost("localhost");
config.setDatabase("simplewiki_rev");
config.setUser("USER");
config.setPassword("PASSWORD");
```

`RevisionApi`, `RevisionIterator` and `ChronoRevisionIterator` each open their own database
connection. Close them when you are done. `RevisionApi` and `RevisionIterator` are
`AutoCloseable`.

**RevisionApi** (`T3_RevisionApi`) gives random access to the revisions of an article. The
article is identified by its page id:

```java
try (RevisionApi revisionApi = new RevisionApi(config)) {
    int count = revisionApi.getNumberOfRevisions(articleId);
    Timestamp created = revisionApi.getFirstDateOfAppearance(articleId);

    Revision first = revisionApi.getRevision(articleId, 1);        // by revision counter
    Revision latest = revisionApi.getRevision(articleId, count);
    Revision byId = revisionApi.getRevision(latest.getRevisionID()); // by revision id
    Revision atTime = revisionApi.getRevision(articleId, timestamp); // current at that time

    System.out.println(latest.getRevisionText());
}
```

A `Revision` provides the revision id, revision counter, timestamp, contributor, comment, minor
flag, namespace and the text of the revision. `getRevisionTimestamps(articleId)` returns the
timestamps of all revisions of an article. The `articleTsIdx` index answers this query without
reading the revisions.

**RevisionIterator** (`T4_RevisionIterator`) iterates over all revisions in the database, article
by article. It rebuilds each revision from the previous one, which is the fastest way to process
the complete history. `next()` returns `null` for a revision that cannot be rebuilt. With
`new RevisionIterator(config, true)`, the iterator does not rebuild the text. The text is then
loaded when `getRevisionText()` is called.

```java
try (RevisionIterator it = new RevisionIterator(config)) {
    while (it.hasNext()) {
        Revision revision = it.next();
        if (revision != null) {
            process(revision.getArticleID(), revision.getRevisionText());
        }
    }
}
```

**ChronoRevisionIterator** (`T5_ChronoRevisionIterator`) returns the revisions of each article
ordered by timestamp. The revision counter follows the order of the revisions in the dump, which
is not always chronological. For such articles, the IndexGenerator stores a mapping in
`index_chronological`, and the iterator uses it. `new ChronoRevisionIterator(config, first, last)`
limits the iteration to the article ids from `first` to `last`. `setChronoStorageSpace(...)` on
the configuration limits the memory used to buffer revisions while they are reordered.

**Combining JWPL and the RevisionMachine** (`T6_PageHistory`): if the revision tables are in the
same database as the JWPL data of the same dump, `new RevisionApi(dbConfig)` accepts the
`DatabaseConfiguration` of the `Wikipedia` object. `Page.getPageId()` gives the article id for the
Revision API.
