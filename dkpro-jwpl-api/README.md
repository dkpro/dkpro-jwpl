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

## Persistence

The entities live in `org.dkpro.jwpl.api.hibernate` and are mapped with JPA annotations. The
Hibernate `SessionFactory` is bootstrapped programmatically by
`org.dkpro.jwpl.api.hibernate.WikiHibernateUtil`, which registers the annotated classes explicitly.
There is no `hibernate.cfg.xml`, no `persistence.xml` and no `*.hbm.xml` mapping resource.

The `MetaData` table is the one exception to "one entity per table": `MetaData` (current layout,
with `version`) and `LegacyMetaData` (layout predating that column) both map onto it and share the
`AbstractMetaData` mapped superclass. Exactly one of the two is registered per `SessionFactory`,
selected by the schema probe described above.
