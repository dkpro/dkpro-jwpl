---
layout: page-fullwidth
title: "TimeMachine"
permalink: "/TimeMachine/"
---

[Back to overview page.](/dkpro-jwpl/WikipediaRevisionToolkit)

# Usage

  * **Attention:** Only use the TimeMachine, if you really need to reconstruct one (or multiple) version(s) of Wikipedia corresponding to past states. The data files used for this purpose are very large. If you just want a single version of the recent Wikipedia, use the DataMachine instead. If you need access to the whole revision history of all Wikipedia articles (e.g. add revision history access to an existing JWPL database), you probably want to use the RevisionMachine instead. It provides access to the whole revision history for a given JWPL database.
  * Learn about the different ways to [get JWPL](/dkpro-jwpl/HowToGetJWPL) and choose the one that is right for you!
  * [Download the Wikipedia data](/dkpro-jwpl/HowToGetWikipediaDumps) from the Wikimedia Download Site. You need 3 files: 
    * `[LANGCODE]wiki-[DATE]-pages-meta-history.xml.bz2`
    * `[LANGCODE]wiki-[DATE]-pagelinks.sql.gz`
    * `[LANGCODE]wiki-[DATE]-categorylinks.sql.gz`
  * Create a configuration file.
    * You may edit one of the sample configuration files, which come with the source code of the TimeMachine.
    * The configuration file must be UTF8 encoded.
    * Running `org.dkpro.jwpl.wikipedia.timemachine.domain.SettingsXML` will generate a sample file that can be edited.
    * The elements of the configuration file are further explained below or in the [readme](https://github.com/dkpro/dkpro-jwpl/blob/master/org.dkpro.jwpl.wikipedia.timemachine/README.TXT) in the TimeMachine Sources.
  * Start the TimeMachine
    * `org.dkpro.jwpl.wikipedia.timemachine.domain.JWPLTimeMachine CONFIG_FILE`
    * Allocate enough heap size to speed up the execution (use the `-Xmx` JVM parameter to increase heap space; e.g. `-Xmx512m` gives you 512MB heap space).
  * If everything went well (it will take a while), the extracted data files are now available in the output directory (each in a directory with the corresponding timestamp as name).
  * For each directory. create a database with the necessary tables  using [jwpl\_tables.sql](https://github.com/dkpro/dkpro-jwpl/blob/master/org.dkpro.jwpl.wikipedia.wikimachine/jwpl_tables.sql)
  * Import the data files into the databases: `mysqlimport -uUSER -p --local --default-character-set=utf8 {database_name} ````pwd`````/*.txt``    
  * Now you are ready to use the databases with the JWPL Core API (also see [JWPLCore:GettingStarted](/dkpro-jwpl/JWPLCore_GettingStarted)).
   When first connecting to a newly imported database, indexes are created. This takes some time (up to 30 minutes), depending on the server and the size of your Wikipedia. Subsequent connects won't have this delay.

## Example configuration file

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
  <comment>This a configuration for the JWPL TimeMachine</comment>
  <entry key="language">english</entry>
  <entry key="mainCategory">Contents</entry>
  <entry key="disambiguationCategory">Disambiguation_pages</entry>
  <entry key="fromTimestamp">20060101000000</entry>
  <entry key="toTimestamp">20060102000000</entry>
  <entry key="each">1</entry>
  <entry key="metaHistoryFile">/PATH/enwiki-2110405-pages-meta-history.xml.bz2</entry>
  <entry key="categoryLinksFile">/PATH/enwiki-20110405-categorylinks.sql.gz</entry>
  <entry key="pageLinksFile">/PATH/enwiki-20110405-pagelinks.sql.gz</entry>
  <entry key="outputDirectory">/PATH/output</entry>
  <entry key="removeInputFilesAfterProcessing">false</entry>
</properties>
{% endhighlight xml %}

### Parameters

| **Parameter** | **Description** | **Comment / Example** |
|:--------------|:----------------|:----------------------|
| language      | The used language. | The language string must correspond to one of the values enumerated in WikiConstants.Language in the JWPL. Examples: english, german, frensh, arabic. |
| mainCategory  | The title of the main category of the Wikipedia language version used. | For example, "Contents" for the English Wikipedia or "!Hauptkategorie" for the German Wikipedia. |
| disambiguationCategory | The title of the disambiguation category of the Wikipedia language version used. | For example, "Disambiguation\_pages" for the English Wikipedia or "Begriffsklärung" for the German Wikipedia. |
| fromTimestamp | yyyymmddhhmmss  | The timestamp of the first version to be extracted. |
| toTimestamp   | yyyymmddhhmmss  | The timestamp of the last version to be extracted. |
| each          | The number of days to be used as regular interval for extracting versions.|                       |
| metaHistoryFile | The absolute path to the pages-meta-history file. | Only .xml and .xml.bz2 extensions are supported. |
| pageLinksFile | The absolute path to the pagelinks file. | Only .sql and .sql.gz extensions are supported. |
| categoryLinksFile | The absolute path to the categorylinks file. | Only .sql and .sql.gz extensions are supported. |
| outputDirectory | The absolute path to the directory to which the transformed files will be written. | The outputDirectory will be created if it does not exist. However its parent directory must exist. |
| removeInputFilesAfterProcessing | A boolean that specifies whether the meta-history file, the pagelinks file and the categorylinks file should be removed after the processing. |                       |