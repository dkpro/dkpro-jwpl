DROP TABLE IF EXISTS `pagelinks`;
CREATE TABLE `pagelinks` (
  `pl_from` int(8) unsigned NOT NULL DEFAULT 0,
  `pl_from_namespace` int(11) NOT NULL DEFAULT 0,
  `pl_target_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`pl_from`,`pl_target_id`),
  KEY `pl_target_id` (`pl_target_id`,`pl_from`),
  KEY `pl_backlinks_namespace_target_id` (`pl_from_namespace`,`pl_target_id`,`pl_from`)
) ENGINE=InnoDB DEFAULT CHARSET=binary ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;
INSERT INTO `pagelinks` VALUES (1,0,4),(1,0,5),(2,0,3),(3,0,3),(4,1,3),(7,2,3),(8,0,3);
