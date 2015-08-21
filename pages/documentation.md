---
layout: page-fullwidth
title: "JWPL Documentation"
permalink: "/documentation/"
---

<table cellspacing='25'>
<tr>
<td>
<h2>Description</h2>

Lately, Wikipedia has been recognized as a promising lexical semantic resource. If Wikipedia is to be used for large-scale NLP tasks, efficient programmatic access to the knowledge therein is required.<br>
The high-performance Wikipedia API provides structured access to information nuggets like redirects, categories, articles and link structure.<br>
JWPL contains a Mediawiki Markup parser that can be used to further analyze the contents of a Wikipedia page. The parser can also be used stand-alone with other texts using MediaWiki markup.<br>
Further, JWPL contains the tool JWPLDataMachine that can be used to create JWPL dumps from the publicly available dumps at <a href='http://download.wikimedia.org'>http://download.wikimedia.org</a>.<br>
<br>
In addition to that, JWPL now contains the <i>Wikipedia Revision Toolkit</i>, which consists of two tools, the <i>TimeMachine</i> and the <i>RevisionMachine</i>. The TimeMachine can be used to reconstruct a snapshot of Wikipedia from a specific date, or to create multiple snapshots from a time span. The RevisionMachine offers efficient access to the edit history of Wikipedia articles while storing the revisions in a dedicated storage format which decreases the demand of storage space by 98%.<br>
<br>
<h2>Is JWPL for you?</h2>

JWPL is for you:<br>
<ul><li>If you need structured access to Wikipedia in Java.</li></ul>

JWPL is not for you:<br>
<ul><li>If you need to query live data. JWPL works on an optimized database, i.e. you are querying a static Wikipedia dump. This gives much better performance and lightens the load on the Wikipedia servers.</li></ul>

<h2>Documentation</h2>
<ul><li>Documentation for <a href='/JWPL_Core'>JWPL Core</a>
</li><li>Documentation for the <a href='/WikipediaRevisionToolkit'>Wikipedia Revision Toolkit</a>
</li><li>How to get JWPL: <a href='/HowToGetJWPL'>Learn about the different ways to obtain JWPL</a>
</li><li>Developer Setup: <a href='/DeveloperSetup'>Setting up Maven and Eclipse for JWPL development</a></li></ul>

<h2>Support</h2>
If you have any technical questions, please write to the <a href='http://groups.google.com/group/jwpl'>JWPL Mailing List</a>.<br>
<br>
<h2>JWPL and UIMA</h2>
<table cellspacing='10'>
<tr>
<td><img src='/images/UIMA.png' /></td>
<td>Are you using UIMA?<br />Then you might be interested in the JWPL integration provided by <a href='https://dkpro.github.io/dkpro-core'>DKPro Core</a></td>
</tr>
</table>

<h2>Overview Poster</h2>
For a first overview over the JWPL components, have a look at the ACL 2011 poster. Its main focus is the Wikipedia Revision Toolkit, but it also contains some information about JWPL Core.<br>
<br>
<a href='/images/ACL_2011_Poster.pdf'><img src='/images/ACL_2011_Poster_thumb.png' /></a>

</td>
<td align='right' valign='top'>
<img src='/images/jwpl_overview.jpg' />
</td>
</tr>
</table>