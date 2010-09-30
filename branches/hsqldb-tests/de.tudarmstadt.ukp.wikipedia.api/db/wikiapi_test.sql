-- MySQL dump 10.13  Distrib 5.1.41, for debian-linux-gnu (x86_64)
--
-- Host: bender    Database: wikiapi_test
-- ------------------------------------------------------
-- Server version	5.1.41-3ubuntu12.6
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO,POSTGRESQL,ORACLE' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table "Category"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "Category" ( 
	"id" bigint NOT NULL, 
	"pageId" int DEFAULT NULL, 
	"name" varchar(255) DEFAULT NULL, 
	PRIMARY KEY ("id"), 
	CONSTRAINT "pageId" UNIQUE("pageId") 
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "Category"
--

/*!40000 ALTER TABLE "Category" DISABLE KEYS */;
INSERT INTO "Category" VALUES (1,200,'Disambiguation');
INSERT INTO "Category" VALUES(2,15,'Student_research_assistants_of_UKP');
INSERT INTO "Category" VALUES(3,4,'Publications_of_Telecooperation');
INSERT INTO "Category" VALUES(4,8,'People_of_UKP');
INSERT INTO "Category" VALUES(5,11,'AQUA');
INSERT INTO "Category" VALUES(6,3,'Research_at_Telecooperation');
INSERT INTO "Category" VALUES(7,7,'Projects_of_UKP');
INSERT INTO "Category" VALUES(8,12,'Group_leader_of_UKP');
INSERT INTO "Category" VALUES(9,2,'Teaching_at_Telecooperation');
INSERT INTO "Category" VALUES(10,13,'Research_Staff_of_UKP');
INSERT INTO "Category" VALUES(11,9,'Publications_of_UKP');
INSERT INTO "Category" VALUES(12,6,'UKP');
INSERT INTO "Category" VALUES(13,1,'Telecooperation');
INSERT INTO "Category" VALUES(14,14,'Diploma_Students_of_UKP');
INSERT INTO "Category" VALUES(15,10,'SIR');
INSERT INTO "Category" VALUES(16,5,'People_of_Telecooperation');
INSERT INTO "Category" VALUES(17,30,'Unconnected_category');
/*!40000 ALTER TABLE "Category" ENABLE KEYS */;

--
-- Table structure for table "DistanceCacheLine"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "DistanceCacheLine" (
	"id" bigint NOT NULL, 
	"lowerId" int DEFAULT NULL, 
	"higherId" int DEFAULT NULL,
	"distance" int DEFAULT NULL, 
	PRIMARY KEY ("id") 
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "DistanceCacheLine"
--

/*!40000 ALTER TABLE "DistanceCacheLine" DISABLE KEYS */;
/*!40000 ALTER TABLE "DistanceCacheLine" ENABLE KEYS */;

--
-- Table structure for table "LcsCacheLine"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "LcsCacheLine" ( 
	"id" bigint NOT NULL, 
	"lowerId" int DEFAULT NULL, 
	"higherId" int DEFAULT NULL, 
	"lcsTitle" varchar(255) DEFAULT NULL, 
	PRIMARY KEY ("id") 
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "LcsCacheLine"
--

/*!40000 ALTER TABLE "LcsCacheLine" DISABLE KEYS */;
/*!40000 ALTER TABLE "LcsCacheLine" ENABLE KEYS */;

--
-- Table structure for table "MetaData"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "MetaData" (
  "id" bigint NOT NULL,
  "language" varchar(255) DEFAULT NULL,
  "disambiguationCategory" varchar(255) DEFAULT NULL,
  "mainCategory" varchar(255) DEFAULT NULL,
  "nrofPages" bigint DEFAULT NULL,
  "nrofRedirects" bigint DEFAULT NULL,
  "nrofDisambiguationPages" bigint DEFAULT NULL,
  "nrofCategories" bigint DEFAULT NULL,
  "version" varchar(255) DEFAULT NULL,
  PRIMARY KEY ("id")
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "MetaData"
--

/*!40000 ALTER TABLE "MetaData" DISABLE KEYS */;
INSERT INTO "MetaData" VALUES (1,'test','Disambiguation','Telecooperation',36,6,2,17,NULL);
/*!40000 ALTER TABLE "MetaData" ENABLE KEYS */;

--
-- Table structure for table "Page"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "Page" (
  "id" bigint NOT NULL,
  "pageId" int DEFAULT NULL,
  "name" varchar(255) DEFAULT NULL,
  "text" varchar(1024),
  "isDisambiguation" bit DEFAULT NULL,
  PRIMARY KEY ("id")
  --CONSTRAINT "pageId" UNIQUE("pageId")
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "Page"
--

/*!40000 ALTER TABLE "Page" DISABLE KEYS */;
INSERT INTO "Page" VALUES (1,1014,'Wikipedia_API','Wikipedia API ist die wichtigste [[Software]] √ºberhaupt.\r\n[[JWPL|Wikipedia API]].\r\n\r\n*Nicht zu √ºbertreffen.\r\n*Unglaublich\r\n\r\n*[[http://www.ukp.tu-darmstadt.de]]\r\n\r\n[[en:Wikipedia API]]\r\n[[fi:WikipediaAPI]]','');
INSERT INTO "Page" VALUES (2,1019,'Christoph_Mueller','Christoph_Mueller','\0');
INSERT INTO "Page" VALUES (3,107,'TK3','TK3','\0');
INSERT INTO "Page" VALUES (4,1022,'Torsten_Zesch','Torsten_Zesch','\0');
INSERT INTO "Page" VALUES (5,1011,'Semantic_Information_Retrieval','{{Disambiguation}}_Semantic_Information_Retrieval','');
INSERT INTO "Page" VALUES (6,105,'TK2','TK2','\0');
INSERT INTO "Page" VALUES (7,1016,'Analyzing_and_Accessing_Wikipedia_as_a_Lexical_Semantic_Resource','Analyzing_and_Accessing_Wikipedia_as_a_Lexical_Semantic_Resource','\0');
INSERT INTO "Page" VALUES (8,1017,'Exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval','Exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval','\0');
INSERT INTO "Page" VALUES (9,1027,'Hossein_Rabighomi','Hossein_Rabighomi','\0');
INSERT INTO "Page" VALUES (10,1024,'Niklas_Jakob','Niklas_Jakob','\0');
INSERT INTO "Page" VALUES (11,1030,'Dimitri_Belski','Dimitri_Belski','\0');
INSERT INTO "Page" VALUES (12,1010,'Research_Focus_of_UKP','Research_Focus_of_UKP','\0');
INSERT INTO "Page" VALUES (13,1023,'Christian_Jacobi','Christian_Jacobi','\0');
INSERT INTO "Page" VALUES (14,1025,'Cigdem_Toprak','Cigdem_Toprak','\0');
INSERT INTO "Page" VALUES (15,1018,'Iryna_Gurevych','Iryna_Gurevych','\0');
INSERT INTO "Page" VALUES (16,1015,'Natural_Language_Processing_for_Ambient_Intelligence','Natural_Language_Processing_for_Ambient_Intelligence','\0');
INSERT INTO "Page" VALUES (17,103,'TK1','TK1','\0');
INSERT INTO "Page" VALUES (18,108,'Peer-to-Peer_and_Grid_Computing','Peer-to-Peer_and_Grid_Computing.','\0');
INSERT INTO "Page" VALUES (19,1029,'Atif_Azad','Atif_Azad','\0');
INSERT INTO "Page" VALUES (20,1043,'Demo_of_Wikipedia_API','Demo_of_Wikipedia_API','\0');
INSERT INTO "Page" VALUES (21,1026,'Lars_Lipecki','Lars_Lipecki','\0');
INSERT INTO "Page" VALUES (22,1028,'Anouar_Haha','Anouar_Haha','\0');
INSERT INTO "Page" VALUES (23,1021,'Markus_Weimer','Markus_Weimer','\0');
INSERT INTO "Page" VALUES (24,1020,'Juergen_Steimle','Juergen_Steimle','\0');
INSERT INTO "Page" VALUES (25,1013,'AQUA','AQUA','\0');
INSERT INTO "Page" VALUES (26,1041,'UKP','UKP','\0');
INSERT INTO "Page" VALUES (27,101,'Net_Centric_Systems','Net_Centric_Systems.','\0');
INSERT INTO "Page" VALUES (28,1031,'Max_Muehlhaeuser','Max_Muehlhaeuser','\0');
INSERT INTO "Page" VALUES (29,1032,'Semantic_information_retrieval_(computer_science)','This is semantic information retrieval in the sense of computer science.','\0');
INSERT INTO "Page" VALUES (30,2000,'Unconnected_page','This page is unconnected from all other pages','\0');
/*!40000 ALTER TABLE "Page" ENABLE KEYS */;

--
-- Table structure for table "PageMapLine"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "PageMapLine" (
  "id" bigint NOT NULL,
  "name" varchar(255) DEFAULT NULL,
  "pageID" int DEFAULT NULL,
  "stem" varchar(255) DEFAULT NULL,
  "lemma" varchar(255) DEFAULT NULL,
  PRIMARY KEY ("id")
  -- KEY "name" ("name") --
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "PageMapLine"
--

/*!40000 ALTER TABLE "PageMapLine" DISABLE KEYS */;
INSERT INTO "PageMapLine" VALUES (1,'Net_Centric_Systems',101,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(2,'TK1',103,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(3,'TK2',105,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(4,'TK3',107,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(5,'Peer-to-Peer_and_Grid_Computing',108,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(6,'Research_Focus_of_UKP',1010,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(7,'Semantic_Information_Retrieval',1011,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(8,'AQUA',1013,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(9,'Wikipedia_API',1014,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(10,'Natural_Language_Processing_for_Ambient_Intelligence',1015,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(11,'Analyzing_and_Accessing_Wikipedia_as_a_Lexical_Semantic_Resource',1016,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(12,'Exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval',1017,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(13,'Iryna_Gurevych',1018,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(14,'Christoph_Mueller',1019,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(15,'Juergen_Steimle',1020,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(16,'Markus_Weimer',1021,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(17,'Torsten_Zesch',1022,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(18,'Christian_Jacobi',1023,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(19,'Niklas_Jakob',1024,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(20,'Cigdem_Toprak',1025,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(21,'Lars_Lipecki',1026,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(22,'Hossein_Rabighomi',1027,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(23,'Anouar_Haha',1028,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(24,'Atif_Azad',1029,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(25,'Dimitri_Belski',1030,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(26,'Max_Muehlhaeuser',1031,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(27,'UKP',1041,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(28,'Demo_of_Wikipedia_API',1043,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(29,'NCS',101,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(30,'Rechnernetze,_Verteilte_Systeme_und_Algorithmen',103,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(31,'Web_Engineering,_Web_Cooperation_and_eLearning',105,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(32,'P2P',108,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(33,'SIR',1011,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(34,'Ubiquitous_Knowledge_Processing',1041,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(35,'Semantic_information_retrieval_(computer_science)',1032,NULL,NULL);
INSERT INTO "PageMapLine" VALUES(36,'Unconnected_page',2000,NULL,NULL);
/*!40000 ALTER TABLE "PageMapLine" ENABLE KEYS */;

--
-- Table structure for table "RelatednessCacheLine"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "RelatednessCacheLine" (
  "id" bigint NOT NULL,
  "page1" int DEFAULT NULL,
  "page2" int DEFAULT NULL,
  "PathLengthAverage" double DEFAULT NULL,
  "PathLengthBest" double DEFAULT NULL,
  "PathLengthSelectivity" double DEFAULT NULL,
  "LeacockChodorowAverage" double DEFAULT NULL,
  "LeacockChodorowBest" double DEFAULT NULL,
  "LeacockChodorowSelectivity" double DEFAULT NULL,
  "ResnikAverage" double DEFAULT NULL,
  "ResnikBest" double DEFAULT NULL,
  "ResnikSelectivityLinear" double DEFAULT NULL,
  "ResnikSelectivityLog" double DEFAULT NULL,
  "LinAverage" double DEFAULT NULL,
  "LinBest" double DEFAULT NULL,
  "JiangConrathAverage" double DEFAULT NULL,
  "JiangConrathBest" double DEFAULT NULL,
  "WuPalmerAverage" double DEFAULT NULL,
  "WuPalmerBest" double DEFAULT NULL,
  "LeskFirst" double DEFAULT NULL,
  "LeskFull" double DEFAULT NULL,
  "Random" double DEFAULT NULL,
  PRIMARY KEY ("id")
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "RelatednessCacheLine"
--

/*!40000 ALTER TABLE "RelatednessCacheLine" DISABLE KEYS */;
/*!40000 ALTER TABLE "RelatednessCacheLine" ENABLE KEYS */;

--
-- Table structure for table "category_inlinks"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "category_inlinks" (
  "id" bigint NOT NULL,
  "inLinks" int DEFAULT NULL,
  -- KEY "FK3F4337732A72A718" ("id"),-- 
  CONSTRAINT "FK3F4337732A72A718" FOREIGN KEY ("id") REFERENCES "Category" ("id")
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "category_inlinks"
--

/*!40000 ALTER TABLE "category_inlinks" DISABLE KEYS */;
INSERT INTO "category_inlinks" VALUES (2,8);
INSERT INTO "category_inlinks" VALUES (3,1);
INSERT INTO "category_inlinks" VALUES (4,6);
INSERT INTO "category_inlinks" VALUES (4,5);
INSERT INTO "category_inlinks" VALUES (5,7);
INSERT INTO "category_inlinks" VALUES (6,1);
INSERT INTO "category_inlinks" VALUES (7,6);
INSERT INTO "category_inlinks" VALUES (8,8);
INSERT INTO "category_inlinks" VALUES (9,1);
INSERT INTO "category_inlinks" VALUES (10,8);
INSERT INTO "category_inlinks" VALUES (11,4);
INSERT INTO "category_inlinks" VALUES (11,6);
INSERT INTO "category_inlinks" VALUES (12,3);
INSERT INTO "category_inlinks" VALUES (14,8);
INSERT INTO "category_inlinks" VALUES (15,7);
INSERT INTO "category_inlinks" VALUES (16,1);
INSERT INTO "category_inlinks" VALUES (1,1);
/*!40000 ALTER TABLE "category_inlinks" ENABLE KEYS */;

--
-- Table structure for table "category_outlinks"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "category_outlinks" (
  "id" bigint NOT NULL,
  "outLinks" int DEFAULT NULL,
  -- KEY "FK9885334CE46A97CC" ("id"),-- 
  CONSTRAINT "FK9885334CE46A97CC" FOREIGN KEY ("id") REFERENCES "Category" ("id")
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "category_outlinks"
--

/*!40000 ALTER TABLE "category_outlinks" DISABLE KEYS */;
INSERT INTO "category_outlinks" VALUES (3,9);
INSERT INTO "category_outlinks" VALUES(4,15);
INSERT INTO "category_outlinks" VALUES(4,13);
INSERT INTO "category_outlinks" VALUES(4,14);
INSERT INTO "category_outlinks" VALUES(4,12);
INSERT INTO "category_outlinks" VALUES(6,6);
INSERT INTO "category_outlinks" VALUES(7,11);
INSERT INTO "category_outlinks" VALUES(7,10);
INSERT INTO "category_outlinks" VALUES(12,9);
INSERT INTO "category_outlinks" VALUES(12,8);
INSERT INTO "category_outlinks" VALUES(12,7);
INSERT INTO "category_outlinks" VALUES(13,2);
INSERT INTO "category_outlinks" VALUES(13,4);
INSERT INTO "category_outlinks" VALUES(13,3);
INSERT INTO "category_outlinks" VALUES(13,5);
INSERT INTO "category_outlinks" VALUES(16,8);
INSERT INTO "category_outlinks" VALUES(13,200);
/*!40000 ALTER TABLE "category_outlinks" ENABLE KEYS */;

--
-- Table structure for table "category_pages"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "category_pages" (
  "id" bigint NOT NULL,
  "pages" int DEFAULT NULL,
  -- KEY "FK71E8D943E46A97CC" ("id"),-- 
  CONSTRAINT "FK71E8D943E46A97CC" FOREIGN KEY ("id") REFERENCES "Category" ("id")
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "category_pages"
--

/*!40000 ALTER TABLE "category_pages" DISABLE KEYS */;
INSERT INTO "category_pages" VALUES (1,1014);
INSERT INTO "category_pages" VALUES(1,1011);
INSERT INTO "category_pages" VALUES(2,1028);
INSERT INTO "category_pages" VALUES(2,1030);
INSERT INTO "category_pages" VALUES(2,1027);
INSERT INTO "category_pages" VALUES(2,1029);
INSERT INTO "category_pages" VALUES(5,1021);
INSERT INTO "category_pages" VALUES(5,1020);
INSERT INTO "category_pages" VALUES(5,1013);
INSERT INTO "category_pages" VALUES(8,1018);
INSERT INTO "category_pages" VALUES(9,105);
INSERT INTO "category_pages" VALUES(9,107);
INSERT INTO "category_pages" VALUES(9,108);
INSERT INTO "category_pages" VALUES(9,103);
INSERT INTO "category_pages" VALUES(9,1031);
INSERT INTO "category_pages" VALUES(9,101);
INSERT INTO "category_pages" VALUES(10,1019);
INSERT INTO "category_pages" VALUES(10,1021);
INSERT INTO "category_pages" VALUES(10,1020);
INSERT INTO "category_pages" VALUES(10,1022);
INSERT INTO "category_pages" VALUES(11,1016);
INSERT INTO "category_pages" VALUES(11,1017);
INSERT INTO "category_pages" VALUES(11,1015);
INSERT INTO "category_pages" VALUES(12,1010);
INSERT INTO "category_pages" VALUES(12,1041);
INSERT INTO "category_pages" VALUES(14,1026);
INSERT INTO "category_pages" VALUES(14,1023);
INSERT INTO "category_pages" VALUES(14,1025);
INSERT INTO "category_pages" VALUES(14,1024);
INSERT INTO "category_pages" VALUES(15,1014);
INSERT INTO "category_pages" VALUES(15,1019);
INSERT INTO "category_pages" VALUES(15,1022);
INSERT INTO "category_pages" VALUES(15,1043);
INSERT INTO "category_pages" VALUES(15,1011);
INSERT INTO "category_pages" VALUES(16,1031);
/*!40000 ALTER TABLE "category_pages" ENABLE KEYS */;

--
-- Table structure for table "page_categories"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "page_categories" (
  "id" bigint NOT NULL,
  "pages" int DEFAULT NULL,
 --  KEY "FK72FB59CC1E350EDD" ("id"),-- 
  CONSTRAINT "FK72FB59CC1E350EDD" FOREIGN KEY ("id") REFERENCES "Page" ("id")
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "page_categories"
--

/*!40000 ALTER TABLE "page_categories" DISABLE KEYS */;
INSERT INTO "page_categories" VALUES (1,200);INSERT INTO "page_categories" VALUES (1,10);INSERT INTO "page_categories" VALUES (2,13);INSERT INTO "page_categories" VALUES (2,10);INSERT INTO "page_categories" VALUES (3,2);INSERT INTO "page_categories" VALUES (4,13);INSERT INTO "page_categories" VALUES (4,10);INSERT INTO "page_categories" VALUES (5,200);INSERT INTO "page_categories" VALUES (5,10);INSERT INTO "page_categories" VALUES (6,2);INSERT INTO "page_categories" VALUES (7,9);INSERT INTO "page_categories" VALUES (8,9);INSERT INTO "page_categories" VALUES (9,15);INSERT INTO "page_categories" VALUES (10,14);INSERT INTO "page_categories" VALUES (11,15);INSERT INTO "page_categories" VALUES (12,6);INSERT INTO "page_categories" VALUES (13,14);INSERT INTO "page_categories" VALUES (14,14);INSERT INTO "page_categories" VALUES (15,12);INSERT INTO "page_categories" VALUES (16,9);INSERT INTO "page_categories" VALUES (17,2);INSERT INTO "page_categories" VALUES (18,2);INSERT INTO "page_categories" VALUES (19,15);INSERT INTO "page_categories" VALUES (20,10);INSERT INTO "page_categories" VALUES (21,14);INSERT INTO "page_categories" VALUES (22,15);INSERT INTO "page_categories" VALUES (23,13);INSERT INTO "page_categories" VALUES (23,11);INSERT INTO "page_categories" VALUES (24,13);INSERT INTO "page_categories" VALUES (24,11);INSERT INTO "page_categories" VALUES (25,11);INSERT INTO "page_categories" VALUES (26,6);INSERT INTO "page_categories" VALUES (27,2);INSERT INTO "page_categories" VALUES (28,2);INSERT INTO "page_categories" VALUES (28,5);
/*!40000 ALTER TABLE "page_categories" ENABLE KEYS */;

--
-- Table structure for table "page_inlinks"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "page_inlinks" (
  "id" bigint NOT NULL,
  "inLinks" int DEFAULT NULL,
  -- KEY "FK91C2BC041E350EDD" ("id"),-- 
  CONSTRAINT "FK91C2BC041E350EDD" FOREIGN KEY ("id") REFERENCES "Page" ("id")
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "page_inlinks"
--

/*!40000 ALTER TABLE "page_inlinks" DISABLE KEYS */;
INSERT INTO "page_inlinks" VALUES (1,1028);INSERT INTO "page_inlinks" VALUES(1,1022);INSERT INTO "page_inlinks" VALUES(1,1043);INSERT INTO "page_inlinks" VALUES(2,1017);INSERT INTO "page_inlinks" VALUES(4,1014);INSERT INTO "page_inlinks" VALUES(4,1016);INSERT INTO "page_inlinks" VALUES(4,1043);INSERT INTO "page_inlinks" VALUES(5,1019);INSERT INTO "page_inlinks" VALUES(5,1042);INSERT INTO "page_inlinks" VALUES(5,1010);INSERT INTO "page_inlinks" VALUES(5,1022);INSERT INTO "page_inlinks" VALUES(5,1041);INSERT INTO "page_inlinks" VALUES(6,1028);INSERT INTO "page_inlinks" VALUES(7,1018);INSERT INTO "page_inlinks" VALUES(7,1022);INSERT INTO "page_inlinks" VALUES(7,1031);INSERT INTO "page_inlinks" VALUES(8,1019);INSERT INTO "page_inlinks" VALUES(8,1018);INSERT INTO "page_inlinks" VALUES(8,1031);INSERT INTO "page_inlinks" VALUES(12,1041);INSERT INTO "page_inlinks" VALUES(15,1016);INSERT INTO "page_inlinks" VALUES(15,1017);INSERT INTO "page_inlinks" VALUES(15,1015);INSERT INTO "page_inlinks" VALUES(16,1018);INSERT INTO "page_inlinks" VALUES(16,1031);INSERT INTO "page_inlinks" VALUES(17,1028);INSERT INTO "page_inlinks" VALUES(17,1031);INSERT INTO "page_inlinks" VALUES(18,1028);INSERT INTO "page_inlinks" VALUES(20,1028);INSERT INTO "page_inlinks" VALUES(20,1022);INSERT INTO "page_inlinks" VALUES(22,1043);INSERT INTO "page_inlinks" VALUES(25,1042);INSERT INTO "page_inlinks" VALUES(25,1021);INSERT INTO "page_inlinks" VALUES(25,1010);INSERT INTO "page_inlinks" VALUES(25,1020);INSERT INTO "page_inlinks" VALUES(25,1041);INSERT INTO "page_inlinks" VALUES(26,1010);INSERT INTO "page_inlinks" VALUES(28,1016);INSERT INTO "page_inlinks" VALUES(28,104);INSERT INTO "page_inlinks" VALUES(28,1015);INSERT INTO "page_inlinks" VALUES(28,103);
/*!40000 ALTER TABLE "page_inlinks" ENABLE KEYS */;

--
-- Table structure for table "page_outlinks"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "page_outlinks" (
  "id" bigint NOT NULL,
  "outLinks" int DEFAULT NULL,
  -- KEY "FK95F640DB1E350EDD" ("id"),-- 
  CONSTRAINT "FK95F640DB1E350EDD" FOREIGN KEY ("id") REFERENCES "Page" ("id")
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "page_outlinks"
--

/*!40000 ALTER TABLE "page_outlinks" DISABLE KEYS */;
INSERT INTO "page_outlinks" VALUES (1,1022);INSERT INTO "page_outlinks" VALUES (2,1017);INSERT INTO "page_outlinks" VALUES (2,1012);INSERT INTO "page_outlinks" VALUES (2,1011);INSERT INTO "page_outlinks" VALUES (4,1014);INSERT INTO "page_outlinks" VALUES (4,1016);INSERT INTO "page_outlinks" VALUES (4,1011);INSERT INTO "page_outlinks" VALUES (4,1043);INSERT INTO "page_outlinks" VALUES (5,1012);INSERT INTO "page_outlinks" VALUES (6,106);INSERT INTO "page_outlinks" VALUES (7,1018);INSERT INTO "page_outlinks" VALUES (7,1022);INSERT INTO "page_outlinks" VALUES (7,1031);INSERT INTO "page_outlinks" VALUES (8,1019);INSERT INTO "page_outlinks" VALUES (8,1018);INSERT INTO "page_outlinks" VALUES (12,1012);INSERT INTO "page_outlinks" VALUES (12,1042);INSERT INTO "page_outlinks" VALUES (12,1013);INSERT INTO "page_outlinks" VALUES (12,1011);INSERT INTO "page_outlinks" VALUES (12,1041);INSERT INTO "page_outlinks" VALUES (15,1016);INSERT INTO "page_outlinks" VALUES (15,1017);INSERT INTO "page_outlinks" VALUES (15,1015);INSERT INTO "page_outlinks" VALUES (16,1018);INSERT INTO "page_outlinks" VALUES (16,1031);INSERT INTO "page_outlinks" VALUES (17,104);INSERT INTO "page_outlinks" VALUES (17,1031);INSERT INTO "page_outlinks" VALUES (18,109);INSERT INTO "page_outlinks" VALUES (20,1014);INSERT INTO "page_outlinks" VALUES (20,1028);INSERT INTO "page_outlinks" VALUES (20,1022);INSERT INTO "page_outlinks" VALUES (22,1014);INSERT INTO "page_outlinks" VALUES (22,105);INSERT INTO "page_outlinks" VALUES (22,109);INSERT INTO "page_outlinks" VALUES (22,104);INSERT INTO "page_outlinks" VALUES (22,106);INSERT INTO "page_outlinks" VALUES (22,103);INSERT INTO "page_outlinks" VALUES (22,108);INSERT INTO "page_outlinks" VALUES (22,1043);INSERT INTO "page_outlinks" VALUES (23,1013);INSERT INTO "page_outlinks" VALUES (24,1013);INSERT INTO "page_outlinks" VALUES (26,1012);INSERT INTO "page_outlinks" VALUES (26,1010);INSERT INTO "page_outlinks" VALUES (26,1013);INSERT INTO "page_outlinks" VALUES (26,1011);INSERT INTO "page_outlinks" VALUES (27,102);INSERT INTO "page_outlinks" VALUES (28,1016);INSERT INTO "page_outlinks" VALUES (28,1017);INSERT INTO "page_outlinks" VALUES (28,104);INSERT INTO "page_outlinks" VALUES (28,1015);INSERT INTO "page_outlinks" VALUES (28,103);
/*!40000 ALTER TABLE "page_outlinks" ENABLE KEYS */;

--
-- Table structure for table "page_redirects"
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE "page_redirects" (
  "id" bigint NOT NULL,
  "redirects" varchar(255) DEFAULT NULL,
  -- KEY "FK1484BA671E350EDD" ("id"),-- 
  CONSTRAINT "FK1484BA671E350EDD" FOREIGN KEY ("id") REFERENCES "Page" ("id")
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table "page_redirects"
--

/*!40000 ALTER TABLE "page_redirects" DISABLE KEYS */;
INSERT INTO "page_redirects" VALUES (27,'NCS');
INSERT INTO "page_redirects" VALUES (17,'Rechnernetze,_Verteilte_Systeme_und_Algorithmen');
INSERT INTO "page_redirects" VALUES (6,'Web_Engineering,_Web_Cooperation_and_eLearning');
INSERT INTO "page_redirects" VALUES (18,'P2P');
INSERT INTO "page_redirects" VALUES (5,'SIR');
INSERT INTO "page_redirects" VALUES (26,'Ubiquitous_Knowledge_Processing');

/*!40000 ALTER TABLE "page_redirects" ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-09-24 11:13:37
