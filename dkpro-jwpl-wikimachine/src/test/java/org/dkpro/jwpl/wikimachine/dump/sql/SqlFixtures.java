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
package org.dkpro.jwpl.wikimachine.dump.sql;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Inline SQL dump fragments used by the parser tests. They reproduce the shapes MediaWiki has
 * actually shipped over the years, so that both the legacy and the normalised probing paths are
 * covered without adding binary fixtures to the repository.
 */
final class SqlFixtures
{

    /** Legacy {@code categorylinks} as dumped between MediaWiki 1.17 and 1.42. */
    static final String CATEGORYLINKS_LEGACY = """
            -- MariaDB dump 10.19
            DROP TABLE IF EXISTS `categorylinks`;
            /*!40101 SET @saved_cs_client     = @@character_set_client */;
            CREATE TABLE `categorylinks` (
              `cl_from` int(8) unsigned NOT NULL DEFAULT 0,
              `cl_to` varbinary(255) NOT NULL DEFAULT '',
              `cl_sortkey` varbinary(230) NOT NULL DEFAULT '',
              `cl_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
              `cl_sortkey_prefix` varbinary(255) NOT NULL DEFAULT '',
              `cl_collation` varbinary(32) NOT NULL DEFAULT '',
              `cl_type` enum('page','subcat','file') NOT NULL DEFAULT 'page',
              PRIMARY KEY (`cl_from`,`cl_to`),
              KEY `cl_sortkey` (`cl_to`,`cl_type`,`cl_sortkey`,`cl_from`)
            ) ENGINE=InnoDB DEFAULT CHARSET=binary;
            INSERT INTO `categorylinks` VALUES \
            (1341,'Wikipedians_by_language','DIAGRAPH01','2013-01-01 00:00:00','','uppercase','subcat'),\
            (1343,'User_aa','SJ\\nSJ','2013-01-01 00:00:00','Sj','uppercase','page'),\
            (1506,'D\\'Arcy_\\\\_Co','X','2013-01-01 00:00:00','','uppercase','page');
            """;

    /** Legacy {@code categorylinks} without a {@code cl_type} column (pre MediaWiki 1.17). */
    static final String CATEGORYLINKS_LEGACY_NO_TYPE = """
            CREATE TABLE `categorylinks` (
              `cl_from` int(8) unsigned NOT NULL DEFAULT 0,
              `cl_to` varbinary(255) NOT NULL DEFAULT '',
              `cl_sortkey` varbinary(255) NOT NULL DEFAULT '',
              `cl_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
              PRIMARY KEY (`cl_from`,`cl_to`)
            ) ENGINE=InnoDB;
            INSERT INTO `categorylinks` VALUES (7,'Articles','A','2009-01-01 00:00:00');
            """;

    /** Normalised {@code categorylinks} as dumped by MediaWiki 1.43 and later. */
    static final String CATEGORYLINKS_NORMALISED = """
            DROP TABLE IF EXISTS `categorylinks`;
            CREATE TABLE `categorylinks` (
              `cl_from` int(8) unsigned NOT NULL DEFAULT 0,
              `cl_sortkey` varbinary(230) NOT NULL DEFAULT '',
              `cl_timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
              `cl_sortkey_prefix` varbinary(255) NOT NULL DEFAULT '',
              `cl_type` enum('page','subcat','file') NOT NULL DEFAULT 'page',
              `cl_collation_id` smallint(5) unsigned NOT NULL DEFAULT 0,
              `cl_target_id` bigint(20) unsigned NOT NULL,
              PRIMARY KEY (`cl_from`,`cl_target_id`),
              KEY `cl_sortkey_id` (`cl_target_id`,`cl_type`,`cl_sortkey`,`cl_from`),
              KEY `cl_timestamp_id` (`cl_target_id`,`cl_timestamp`)
            ) ENGINE=InnoDB DEFAULT CHARSET=binary ROW_FORMAT=COMPRESSED;
            INSERT INTO `categorylinks` VALUES \
            (1341,'DIAGRAPH01','2025-02-17 13:31:43','','page',1,208),\
            (1343,'SJ\\nSJ','2025-02-17 13:31:43','Sj','subcat',1,212),\
            (1506,'GONE','2025-02-17 13:31:43','','file',1,999),\
            (1507,'BIG','2025-02-17 13:31:43','','page',1,4294967297);
            """;

    /** Normalised {@code pagelinks} as dumped by MediaWiki 1.43 and later. */
    static final String PAGELINKS_NORMALISED = """
            CREATE TABLE `pagelinks` (
              `pl_from` int(8) unsigned NOT NULL DEFAULT 0,
              `pl_from_namespace` int(11) NOT NULL DEFAULT 0,
              `pl_target_id` bigint(20) unsigned NOT NULL,
              PRIMARY KEY (`pl_from`,`pl_target_id`),
              KEY `pl_target_id` (`pl_target_id`,`pl_from`)
            ) ENGINE=InnoDB DEFAULT CHARSET=binary ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;
            INSERT INTO `pagelinks` VALUES (1426,0,208),(1271,1,212),(1341,2,999);
            """;

    /** {@code pagelinks} as dumped before July 2014: three columns. */
    static final String PAGELINKS_PRE_2014 = """
            CREATE TABLE `pagelinks` (
              `pl_from` int(8) unsigned NOT NULL DEFAULT 0,
              `pl_namespace` int(11) NOT NULL DEFAULT 0,
              `pl_to` varbinary(255) NOT NULL DEFAULT '',
              UNIQUE KEY `pl_from` (`pl_from`,`pl_namespace`,`pl_to`)
            ) ENGINE=InnoDB;
            INSERT INTO `pagelinks` VALUES (1341,0,'Main_Page'),(1343,14,'User_aa'),(1506,0,'Sj');
            """;

    /** {@code pagelinks} as dumped after July 2014: four columns. */
    static final String PAGELINKS_POST_2014 = """
            CREATE TABLE `pagelinks` (
              `pl_from` int(8) unsigned NOT NULL DEFAULT 0,
              `pl_namespace` int(11) NOT NULL DEFAULT 0,
              `pl_to` varbinary(255) NOT NULL DEFAULT '',
              `pl_from_namespace` int(11) NOT NULL DEFAULT 0,
              UNIQUE KEY `pl_from` (`pl_from`,`pl_namespace`,`pl_to`)
            ) ENGINE=InnoDB;
            INSERT INTO `pagelinks` VALUES (1341,0,'Main_Page',0),(1343,14,'User_aa',2),(1506,0,'Sj',0);
            """;

    /** {@code linktarget} as introduced by MediaWiki 1.43. */
    static final String LINKTARGET = """
            DROP TABLE IF EXISTS `linktarget`;
            CREATE TABLE `linktarget` (
              `lt_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
              `lt_namespace` int(11) NOT NULL,
              `lt_title` varbinary(255) NOT NULL,
              PRIMARY KEY (`lt_id`),
              UNIQUE KEY `lt_namespace_title` (`lt_namespace`,`lt_title`)
            ) ENGINE=InnoDB DEFAULT CHARSET=binary;
            INSERT INTO `linktarget` VALUES \
            (208,14,'User_aa-0'),(212,14,'User_es'),(1779,10,'User_aa-0'),\
            (4294967297,0,'Big_Target');
            """;

    private SqlFixtures()
    {
        // static-only
    }

    static InputStream stream(String sql)
    {
        return new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8));
    }
}
