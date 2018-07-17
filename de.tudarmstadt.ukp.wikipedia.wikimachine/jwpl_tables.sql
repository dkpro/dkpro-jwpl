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
-- MySQL dump 10.11
--
-- Host: localhost    Database: jwpl_tables
-- ------------------------------------------------------
-- Server version	5.0.37-community-nt

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Category`
--

DROP TABLE IF EXISTS `Category`;
CREATE TABLE `Category` (
  `id` bigint(20) NOT NULL auto_increment,
  `pageId` int(11) default NULL,
  `name` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `pageId` (`pageId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Category`
--

LOCK TABLES `Category` WRITE;
/*!40000 ALTER TABLE `Category` DISABLE KEYS */;
/*!40000 ALTER TABLE `Category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category_inlinks`
--

DROP TABLE IF EXISTS `category_inlinks`;
CREATE TABLE `category_inlinks` (
  `id` bigint(20) NOT NULL,
  `inLinks` int(11) default NULL,
  KEY `FK3F433773E46A97CC` (`id`),
  KEY `FK3F433773BB482769` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `category_inlinks`
--

LOCK TABLES `category_inlinks` WRITE;
/*!40000 ALTER TABLE `category_inlinks` DISABLE KEYS */;
/*!40000 ALTER TABLE `category_inlinks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category_outlinks`
--

DROP TABLE IF EXISTS `category_outlinks`;
CREATE TABLE `category_outlinks` (
  `id` bigint(20) NOT NULL,
  `outLinks` int(11) default NULL,
  KEY `FK9885334CE46A97CC` (`id`),
  KEY `FK9885334CBB482769` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `category_outlinks`
--

LOCK TABLES `category_outlinks` WRITE;
/*!40000 ALTER TABLE `category_outlinks` DISABLE KEYS */;
/*!40000 ALTER TABLE `category_outlinks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category_pages`
--

DROP TABLE IF EXISTS `category_pages`;
CREATE TABLE `category_pages` (
  `id` bigint(20) NOT NULL,
  `pages` int(11) default NULL,
  KEY `FK71E8D943E46A97CC` (`id`),
  KEY `FK71E8D943BB482769` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `category_pages`
--

LOCK TABLES `category_pages` WRITE;
/*!40000 ALTER TABLE `category_pages` DISABLE KEYS */;
/*!40000 ALTER TABLE `category_pages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `MetaData`
--

DROP TABLE IF EXISTS `MetaData`;
CREATE TABLE `MetaData` (
  `id` bigint(20) NOT NULL auto_increment,
  `language` varchar(255) default NULL,
  `disambiguationCategory` varchar(255) default NULL,
  `mainCategory` varchar(255) default NULL,
  `nrofPages` bigint(20) default NULL,
  `nrofRedirects` bigint(20) default NULL,
  `nrofDisambiguationPages` bigint(20) default NULL,
  `nrofCategories` bigint(20) default NULL,
  `version` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `MetaData`
--

LOCK TABLES `MetaData` WRITE;
/*!40000 ALTER TABLE `MetaData` DISABLE KEYS */;
/*!40000 ALTER TABLE `MetaData` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Page`
--

DROP TABLE IF EXISTS `Page`;
CREATE TABLE `Page` (
  `id` bigint(20) NOT NULL auto_increment,
  `pageId` int(11) default NULL,
  `name` varchar(255) default NULL,
  `text` longtext,
  `isDisambiguation` bit(1) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `pageId` (`pageId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Page`
--

LOCK TABLES `Page` WRITE;
/*!40000 ALTER TABLE `Page` DISABLE KEYS */;
/*!40000 ALTER TABLE `Page` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `page_categories`
--

DROP TABLE IF EXISTS `page_categories`;
CREATE TABLE `page_categories` (
  `id` bigint(20) NOT NULL,
  `pages` int(11) default NULL,
  KEY `FK72FB59CC1E350EDD` (`id`),
  KEY `FK72FB59CC75DCF4FA` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `page_categories`
--

LOCK TABLES `page_categories` WRITE;
/*!40000 ALTER TABLE `page_categories` DISABLE KEYS */;
/*!40000 ALTER TABLE `page_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `page_inlinks`
--

DROP TABLE IF EXISTS `page_inlinks`;
CREATE TABLE `page_inlinks` (
  `id` bigint(20) NOT NULL,
  `inLinks` int(11) default NULL,
  KEY `FK91C2BC041E350EDD` (`id`),
  KEY `FK91C2BC0475DCF4FA` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `page_inlinks`
--

LOCK TABLES `page_inlinks` WRITE;
/*!40000 ALTER TABLE `page_inlinks` DISABLE KEYS */;
/*!40000 ALTER TABLE `page_inlinks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `page_outlinks`
--

DROP TABLE IF EXISTS `page_outlinks`;
CREATE TABLE `page_outlinks` (
  `id` bigint(20) NOT NULL,
  `outLinks` int(11) default NULL,
  KEY `FK95F640DB1E350EDD` (`id`),
  KEY `FK95F640DB75DCF4FA` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `page_outlinks`
--

LOCK TABLES `page_outlinks` WRITE;
/*!40000 ALTER TABLE `page_outlinks` DISABLE KEYS */;
/*!40000 ALTER TABLE `page_outlinks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `page_redirects`
--

DROP TABLE IF EXISTS `page_redirects`;
CREATE TABLE `page_redirects` (
  `id` bigint(20) NOT NULL,
  `redirects` varchar(255) default NULL,
  KEY `FK1484BA671E350EDD` (`id`),
  KEY `FK1484BA6775DCF4FA` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `page_redirects`
--

LOCK TABLES `page_redirects` WRITE;
/*!40000 ALTER TABLE `page_redirects` DISABLE KEYS */;
/*!40000 ALTER TABLE `page_redirects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PageMapLine`
--

DROP TABLE IF EXISTS `PageMapLine`;
CREATE TABLE `PageMapLine` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) default NULL,
  `pageID` int(11) default NULL,
  `stem` varchar(255) default NULL,
  `lemma` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `PageMapLine`
--

LOCK TABLES `PageMapLine` WRITE;
/*!40000 ALTER TABLE `PageMapLine` DISABLE KEYS */;
/*!40000 ALTER TABLE `PageMapLine` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2008-02-11 12:33:30
