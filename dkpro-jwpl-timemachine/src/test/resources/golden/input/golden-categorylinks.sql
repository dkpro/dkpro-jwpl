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
INSERT INTO `categorylinks` VALUES (1,'ALPHA','2021-06-01 12:00:00','','page',1,1),(3,'GAMMA','2021-04-01 12:00:00','','page',1,2),(6,'GREEK','2021-08-01 12:00:00','','subcat',1,1),(7,'BOB','2020-01-15 12:00:00','','page',1,1);
