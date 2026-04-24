-- Test fixture schema for dkpro-jwpl-api, MariaDB / MySQL dialect.
-- Matches the Hibernate mappings in org/dkpro/jwpl/api/hibernate/*.hbm.xml.
-- Table names preserve the exact case declared in the mappings; MariaDB on
-- Linux treats unquoted table names case-sensitively by default.

CREATE TABLE Category (
    id      BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    pageId  INTEGER,
    name    VARCHAR(255),
    UNIQUE (pageId)
);
CREATE INDEX nameIndex ON Category (name);

CREATE TABLE category_inlinks (
    id       BIGINT  NOT NULL,
    inLinks  INTEGER,
    CONSTRAINT fk_category_inlinks FOREIGN KEY (id) REFERENCES Category (id)
);

CREATE TABLE category_outlinks (
    id        BIGINT  NOT NULL,
    outLinks  INTEGER,
    CONSTRAINT fk_category_outlinks FOREIGN KEY (id) REFERENCES Category (id)
);

CREATE TABLE category_pages (
    id     BIGINT  NOT NULL,
    pages  INTEGER,
    CONSTRAINT fk_category_pages FOREIGN KEY (id) REFERENCES Category (id)
);

CREATE TABLE MetaData (
    id                       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    language                 VARCHAR(255),
    disambiguationCategory   VARCHAR(255),
    mainCategory             VARCHAR(255),
    nrofPages                BIGINT,
    nrofRedirects            BIGINT,
    nrofDisambiguationPages  BIGINT,
    nrofCategories           BIGINT,
    version                  VARCHAR(255)
);

CREATE TABLE Page (
    id                BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    pageId            INTEGER,
    name              VARCHAR(255),
    text              LONGTEXT,
    isDisambiguation  BOOLEAN,
    UNIQUE (pageId)
);

CREATE TABLE page_categories (
    id     BIGINT  NOT NULL,
    pages  INTEGER,
    CONSTRAINT fk_page_categories FOREIGN KEY (id) REFERENCES Page (id)
);

CREATE TABLE page_inlinks (
    id       BIGINT  NOT NULL,
    inLinks  INTEGER,
    CONSTRAINT fk_page_inlinks FOREIGN KEY (id) REFERENCES Page (id)
);

CREATE TABLE page_outlinks (
    id        BIGINT  NOT NULL,
    outLinks  INTEGER,
    CONSTRAINT fk_page_outlinks FOREIGN KEY (id) REFERENCES Page (id)
);

CREATE TABLE page_redirects (
    id         BIGINT        NOT NULL,
    redirects  VARCHAR(255),
    CONSTRAINT fk_page_redirects FOREIGN KEY (id) REFERENCES Page (id)
);

CREATE TABLE PageMapLine (
    id      BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(255),
    pageID  INTEGER,
    stem    VARCHAR(255),
    lemma   VARCHAR(255)
);
CREATE INDEX name_index ON PageMapLine (name);
