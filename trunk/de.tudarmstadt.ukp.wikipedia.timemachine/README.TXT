JWPLTimeMachine

USAGE:

StartDBMapping <configuration.xml>

EXAMPLE FILE:

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
  <comment>This a configuration formular for the JWPL TimeMachine</comment>
  <entry key="language">greek</entry>
  <entry key="mainCategory"></entry>
  <entry key="disambiguationCategory"></entry>
  <entry key="fromTimestamp">20060101000000</entry>
  <entry key="toTimestamp">20060102000000</entry>
  <entry key="each">1</entry>
  <entry key="metaHistoryFile">/home/zesch/wiki_data/elwiki/elwiki-20080205-pages-meta-history.xml.bz2</entry>
  <entry key="categoryLinksFile">/home/zesch/wiki_data/elwiki/elwiki-20080205-categorylinks.sql.gz</entry>
  <entry key="pageLinksFile">/home/zesch/wiki_data/elwiki/elwiki-20080205-pagelinks.sql.gz</entry>
  <entry key="outputDirectory">/home/zesch/wiki_data/elwiki_test</entry>
  <entry key="removeInputFilesAfterProcessing">false</entry>
</properties>

 * language - The used language. The language string must correspond to one of the values enumerated in WikiConstants.Language in the JWPL. Examples: english, german, frensh, arabic.
 * mainCategory - The title of the main category of the Wikipedia language version used. For example, "Categories" for the English Wikipedia or "!Hauptkategorie" for the German Wikipedia.
 * isambiguationCategory - The title of the disambiguation category of the Wikipedia language version used. For example, "Disambiguation" for the English Wikipedia or "Begriffsklärung" for the German Wikipedia.
 * fromTimestamp - yyyymmddhhmmss - The timestamp of the first version to be extracted.
 * toTimestamp - yyyymmddhhmmss - The timestamp of the last version to be extracted.
 * each - The number of days to be used as regular interval for extracting versions.
 * metaHistoryFile - The absolute path to the pages-meta-history file. Only .xml and .xml.bz2 extensions are supported.
 * pageLinksFile - The absolute path to the pagelinks file only .sql and .sql.gz extensions are supported.
 * categoryLinksFile - The absolute path to the categorylinks file only .sql and .sql.gz extensions are supported.
 * outputDirectory - The absolute path to the directory to which the transformed files will be written. The outputDirectory will be created if it does not exist. However its parent directory must exist.
 * removeInputFilesAfterProcessing - A boolean that specifies whether the meta-history file, the pagelinks file and the categorylinks file should be removed after the processing.
