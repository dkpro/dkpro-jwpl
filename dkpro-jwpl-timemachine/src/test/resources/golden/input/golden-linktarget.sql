DROP TABLE IF EXISTS `linktarget`;
CREATE TABLE `linktarget` (
  `lt_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `lt_namespace` int(11) NOT NULL,
  `lt_title` varbinary(255) NOT NULL,
  PRIMARY KEY (`lt_id`),
  UNIQUE KEY `lt_namespace_title` (`lt_namespace`,`lt_title`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=binary ROW_FORMAT=COMPRESSED;
INSERT INTO `linktarget` VALUES (1,14,'Letters'),(2,14,'Greek'),(3,0,'Alpha'),(4,0,'Beta'),(5,0,'Gamma'),(6,0,'Delta');
