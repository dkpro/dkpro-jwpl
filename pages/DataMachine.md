---
layout: page-fullwidth
title: "DataMachine"
permalink: "/DataMachine/"
---

[Back to overview page.](/dkpro-jwpl/documentation/)

# Usage

  * Learn about the different ways to [get JWPL](/dkpro-jwpl/HowToGetJWPL) and choose the one that is right for you!
  * [Download the Wikipedia data](/dkpro-jwpl/HowToGetWikipediaDumps) from the Wikimedia Download Site. You need 3 files: 
    * `[LANGCODE]wiki-[DATE]-pages-articles.xml.bz2` or `[LANGCODE]wiki-[DATE]-pages-meta-current.xml.bz2`
    * `[LANGCODE]wiki-[DATE]-pagelinks.sql.gz`
    * `[LANGCODE]wiki-[DATE]-categorylinks.sql.gz`. 
    * **Note**: If you want to add discussion pages to the database, use `[LANGCODE]wiki-[DATE]-pages-meta-current.xml.bz2`, otherwise `[LANGCODE]wiki-[DATE]-pages-articles.xml.bz2` suffices.
  * Run the transformation: 
    * `java -jar JWPLDataMachine.jar [LANGUAGE] [MAIN_CATEGORY_NAME] [DISAMBIGUATION_CATEGORY_NAME] [SOURCE_DIRECTORY]` or 
    * `org.dkpro.jwpl.wikipedia.datamachine.domain.JWPLDataMachine [LANGUAGE] [MAIN_CATEGORY_NAME] [DISAMBIGUATION_CATEGORY_NAME] [SOURCE_DIRECTORY]`
    * **Attention:** For large dumps you need to increase the amount of memory assigned to the JVM using e.g. the "-Xmx2g" flag to assign 2GB of memory. For the recent English dump you will need at least 4GB. If your system uses a different file encoding you might need to add the `-Dfile.encoding=utf8` flag.
      * LANGUAGE - a language string matching one the [JWPL\_Languages](/dkpro-jwpl/JWPL_Languages).
      * MAIN\_CATEGORY\_NAME - the name of the main (top) category of the Wikipedia category hierarchy
      * DISAMBIGUATION\_CATEGORY\_NAME - the name of the category that directly contains the disambiguation pages (see [Choosing the disambiguation category](#choosing-the-disambiguation-category))
      * SOURCE\_DIRECTORY - the path to the directory containing the source files
  * Patience :) This is going to take a while... While the DataMachine is running, it creates three temporary .bin files. These are only needed during processing time and can be deleted once all files in the output directory have been produced.
  * Eventually, you should get 11 txt files in an "output" subfolder
  * Create a database with the necessary tables
    * If you are using MySQL 4.x or previous - please see [JWPL\_MySQL4](/dkpro-jwpl/JWPL_MySQL4).
    * Make sure the database encoding is set to UTF8.
    * Create a database: `mysqladmin -u[USER] -p create [DB_NAME] DEFAULT CHARACTER SET utf8;`, or, when on the mysql shell: `CREATE DATABASE [DB_NAME] DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;`
    * Create all necessary tables using [jwpl\_tables.sql](https://github.com/dkpro/dkpro-jwpl/blob/master/org.dkpro.jwpl.wikipedia.wikimachine/jwpl_tables.sql)
  * Import the data files into the database: `mysqlimport -uUSER -p --local --default-character-set=utf8 {database_name} *.txt`
  * Now you are ready to use the database with the JWPL Core API (also see [JWPLCore:GettingStarted](/dkpro-jwpl/JWPLCore_GettingStarted)). When first connecting to a newly imported database, indexes are created. This takes some time (up to 30 minutes), depending on the server and the size of your Wikipedia. Subsequent connects won't have this delay.

## Example Transformation Commands
(Note: increase heap space for large Wikipedia versions with the -Xmx flag)

  * `java -Xmx4g -jar JWPLDataMachine.jar english Contents All_article_disambiguation_pages ~/enwiki/20081013`
  * `java -jar JWPLDataMachine.jar simple_english Contents All_article_disambiguation_pages ./`
  * `java -jar JWPLDataMachine.jar spanish Índice_de_categorías Wikipedia:Desambiguación ~/eswiki/20080416/`
  * `java -Xmx2g -jar JWPLDataMachine.jar german !Hauptkategorie Begriffsklärung ~/dewiki/20080422/`
  * `java -jar JWPLDataMachine.jar dutch Alles Doorverwijspagina ~/nlwiki/200810408`

Mind that the names of the main category or the category marking disambiguation pages may change over time. E.g. the English category for disambiguation pages was called "Disambiguation" for a long time, and later "Disambiguation\_pages".

## Choosing the disambiguation category

The DataMachine marks a page as a disambiguation page only if the page is *directly* in the category passed as DISAMBIGUATION\_CATEGORY\_NAME. Pages that are only in a subcategory of it are not marked.

Many disambiguation pages are only in such a subcategory. In the English Wikipedia, for example, pages using the human name disambiguation template are in "Human name disambiguation pages", a subcategory of "Disambiguation\_pages". As of September 2026, "Disambiguation\_pages" directly contains about 235,000 pages, while the English Wikipedia has about 380,000 disambiguation pages. With "Disambiguation\_pages", about 145,000 of them are therefore not marked (see [#80](https://github.com/dkpro/dkpro-jwpl/issues/80)).

The disambiguation templates also add every disambiguation page to a hidden tracking category, which is not split into subcategories. Use that category instead:

  * English and Simple English Wikipedia: `All_article_disambiguation_pages`, which contains the disambiguation pages in the article namespace. `All_disambiguation_pages` also contains those in other namespaces.

For other language versions, check which category directly contains all disambiguation pages.

## Discussion Pages

Discussion pages can only be included if the source file contains these pages (see above).<br />
All discussion pages will be marked with the prefix "Discussion:" in the page title (for all Wikipedia language versions).
