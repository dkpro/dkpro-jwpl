# JWPL API

The runtime library of DKPro JWPL. `org.dkpro.jwpl.api.Wikipedia` is the entry point; a
`org.dkpro.jwpl.api.DatabaseConfiguration` wires it to a database that was imported from a
Wikipedia dump by the DataMachine or the TimeMachine.

## Database schema

The reference layout is `src/test/resources/db/schema-mysql.sql` (with `schema-hsqldb.sql` as its
HSQLDB counterpart used by the unit tests). Note that the table names deliberately preserve the
casing of the entity mappings: MariaDB and MySQL on Linux are case-sensitive for unquoted table
names.

## Upgrading / schema changes

### Recommended indexes on Page.name and PageMapLine.pageID

The reference layout indexes `Page.name` (`page_name_index`) and `PageMapLine.pageID`
(`pageID_index`). Without them, `Wikipedia#getCategories(String)` and `Wikipedia#existsPage(int)`
scan the whole `Page` or `PageMapLine` table on every call. Databases generated before
[issue #606](https://github.com/dkpro/dkpro-jwpl/issues/606) lack both indexes. Adding them is
**optional**: query results do not change, and `hbm2ddl=validate` does not check indexes, so
existing databases keep working without them. On a full-size wiki, building them takes minutes
and several hundred MB to a few GB of disk space:

```sql
ALTER TABLE PageMapLine ADD INDEX pageID_index (pageID);
ALTER TABLE Page        ADD INDEX page_name_index (name);
```

### MetaData.version

**What changed.** The `MetaData` table carries a `version VARCHAR(255)` column. It has always been
mapped and exposed through the public `MetaData#getVersion()`, but only the TimeMachine ever wrote
it. As of the fix for [issue #490](https://github.com/dkpro/dkpro-jwpl/issues/490) the DataMachine
writes it too — the Wikimedia dump date taken from the input dump file names
(`<wiki>-<YYYYMMDD>-<role>...`), normalised to the MediaWiki `yyyyMMddHHmmss` shape the TimeMachine
already used. Consequently:

* databases generated **before** the column existed do not have it at all;
* databases generated **before** this fix by the DataMachine have it, but always `NULL`;
* databases generated **after** this fix by the DataMachine have it populated, unless no dump date
  could be derived from the input file names (for instance because the dumps were renamed), in
  which case it stays `NULL`.

**Automatic handling.** JWPL probes the live schema once, when the Hibernate `SessionFactory` for a
configuration is built, using JDBC `DatabaseMetaData`. When the `version` column is absent, JWPL
binds an eight-column mapping instead of the current one, logs a warning naming the missing column,
and `MetaData#getVersion()` returns `null`. Everything else is unaffected. If the probe itself
cannot be carried out — restricted permissions, an unavailable driver — JWPL assumes the current
layout and behaves exactly as it did before.

**Recommended migration.** Add the column; a `NULL` value is harmless:

```sql
ALTER TABLE MetaData ADD COLUMN version VARCHAR(255) DEFAULT NULL;
```

**Escape hatch.** `DatabaseConfiguration` accepts arbitrary Hibernate settings, which are merged
*last* and therefore override JWPL's own defaults, including `hibernate.hbm2ddl.auto` (which JWPL
sets to `validate` for MySQL/MariaDB and to `none` for HSQLDB):

```java
DatabaseConfiguration dbConfig = new DatabaseConfiguration(
        "org.mariadb.jdbc.Driver", "jdbc:mariadb://localhost/wikiapi_en",
        "localhost", "wikiapi_en", "user", "password", WikiConstants.Language.english);

// Single setting ...
dbConfig.setHibernateProperty("hibernate.hbm2ddl.auto", "none");

// ... or a whole bag at once.
Properties hibernateProperties = new Properties();
hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "none");
dbConfig.setHibernateProperties(hibernateProperties);

Wikipedia wiki = new Wikipedia(dbConfig);
```

Populate the settings **before** the first `new Wikipedia(config)` for a given database
configuration: session factories are cached JVM-wide and built only once per configuration. The
cache key is derived from the connection-affecting values -- language, host, database, JDBC url,
driver, user and password -- so two configurations differing in any of them get their own factory.
The Hibernate settings bag above is deliberately *not* part of that key.

## Connection pooling

By default, JWPL uses Hibernate's built-in connection pool with at most **5** connections. That
pool is not meant for production use: it keeps no prepared-statement cache and, once all of its
connections are in use, it does **not** wait for one to be returned — the next transaction fails
immediately with a `HibernateException` ("The internal connection pool has reached its maximum
size and no connection is currently available").

Single-threaded use never gets near that limit. Parallel use does: session factories are shared
JVM-wide per database configuration, and sessions are bound to the current thread, so every thread
that is inside a JWPL call holds one connection. Threads sharing one configuration therefore need a
pool of at least as many connections as threads.

As with the Hibernate settings above, configure the pool **before** the first
`new Wikipedia(config)` for a given database configuration; the pool settings are not part of the
session-factory cache key.

**Built-in pool.** Raise its size to at least the number of threads:

```java
dbConfig.setConnectionPoolSize(16); // hibernate.connection.pool_size, default 5
```

A `hibernate.connection.pool_size` given through `setHibernateProperty` takes precedence.

**C3P0.** For MySQL and MariaDB, JWPL switches to C3P0 automatically as soon as Hibernate's C3P0
integration is on the classpath (between 3 and 15 connections, 100 cached statements). Unlike the
built-in pool, C3P0 blocks until a connection becomes available. Use the `hibernate-c3p0` artifact
that matches your `hibernate-core` version:

```xml
<dependency>
  <groupId>org.hibernate.orm</groupId>
  <artifactId>hibernate-c3p0</artifactId>
  <version>${hibernate.version}</version>
</dependency>
```

Its defaults can be overridden like any other setting, e.g.
`dbConfig.setHibernateProperty("hibernate.c3p0.max_size", "32")`.

**HikariCP.** Add `hibernate-hikaricp` (again matching your `hibernate-core` version) and select
it explicitly. Selecting it matters when C3P0 is on the classpath too, since Hibernate would pick
C3P0 otherwise. Set the pool size as a `hibernate.hikari.*` setting, because Hibernate 6 does not
map `hibernate.connection.pool_size` onto HikariCP:

```java
dbConfig.setHibernateProperty("hibernate.connection.provider_class", "hikari");
dbConfig.setHibernateProperty("hibernate.hikari.maximumPoolSize", "16");
```

Note that a sufficiently large pool makes only the connection layer usable from several threads.
`Wikipedia` itself is not documented as thread-safe (see
[issue #605](https://github.com/dkpro/dkpro-jwpl/issues/605)); prefer one `Wikipedia` instance per
thread over the same `DatabaseConfiguration`.

## Category cache

Each `Wikipedia` instance keeps the most recently used **1000** categories in memory, so that
repeated lookups via `getCategory(int)` — including those made by `Category.getParents()`,
`getChildren()` and `Page.getCategories()` — do not query the database again. Only the plain
columns of a category (id, page id and name) are cached; its links and pages are read from the
database on each request. Set the bound before creating the `Wikipedia` instance; `0` disables the
cache:

```java
dbConfig.setCategoryCacheSize(10_000); // default 1000, 0 = off
```

Cached categories are never refreshed. If the database is changed while a `Wikipedia` instance is
in use, call `wiki.clearCategoryCache()`.

## Persistence

The entities live in `org.dkpro.jwpl.api.hibernate` and are mapped with JPA annotations. The
Hibernate `SessionFactory` is bootstrapped programmatically by
`org.dkpro.jwpl.api.hibernate.WikiHibernateUtil`, which registers the annotated classes explicitly.
There is no `hibernate.cfg.xml`, no `persistence.xml` and no `*.hbm.xml` mapping resource.

The `MetaData` table is the one exception to "one entity per table": `MetaData` (current layout,
with `version`) and `LegacyMetaData` (layout predating that column) both map onto it and share the
`AbstractMetaData` mapped superclass. Exactly one of the two is registered per `SessionFactory`,
selected by the schema probe described above.
