# JWPL Util

## Template Schema

```sql
CREATE TABLE IF NOT EXISTS templateId_pageId (templateId INTEGER UNSIGNED NOT NULL,pageId INTEGER UNSIGNED NOT NULL, UNIQUE(templateId, pageId)) ENGINE = MYISAM;
CREATE TABLE IF NOT EXISTS templates (templateId INTEGER NOT NULL AUTO_INCREMENT,templateName TEXT NOT NULL,PRIMARY KEY(templateId)) ENGINE = MYISAM;
CREATE TABLE IF NOT EXISTS templateId_revisionId(templateId INTEGER UNSIGNED NOT NULL,revisionId INTEGER UNSIGNED NOT NULL, UNIQUE(templateId, revisionId)) ENGINE = MYISAM;
```

## Properties Sample

```
#host=dbhost
#db=revisiondb
#user=username
#password=pwd
#output=outputFile
#charset=UTF8 (or others) (optional)
#pagebuffer=5000 (optional)
#maxAllowedPackets=16760832 (optional)


host=bender.ukp.informatik.tu-darmstadt.de
db=wikiapi_pdc_20101109_rev
user=student
password=student
output=/Users/a_vovk/Desktop/Output/
language=english
charset=UTF8
pagebuffer=5000
maxAllowedPackets=16760832

use_revision_iterator=true

!white_list=fr;nummere;de

create_templates_for_pages=true
create_templates_for_revisions=true

pages_white_list=official_schprooche
pages_white_prefix_list=
pages_black_prefix_list=
pages_black_list=

revisions_white_list=official_schprooche
revisions_white_prefix_list=
revisions_black_prefix_list=
revisions_black_list=
```