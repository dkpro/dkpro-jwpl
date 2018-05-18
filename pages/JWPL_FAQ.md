---
layout: page-fullwidth
title: "FAQ"
permalink: "/JWPL_FAQ/"
---

[Back to the JWPL Core overview page](/dkpro-jwpl/JWPL_Core)

**Q: Is it possible to load the data into $DATABASE-SERVER$ ?**

Probably yes. It should always work with MySQL. However, the data is shipped as SQL insert statements that should be database vendor independent, but we have not tested it with all database servers out there. So, just try it and let us know whether it works with a certain system.

**Q: Why does the first connection to a newly installed database take so much time?**

When first connecting to a newly installed database, indexes are created. This takes some time (up to 30 minutes), depending on the server and the size of your Wikipedia. Subsequent connects will be much faster.

**Q: Why can't I use my already running Mediawiki/Wikipedia installation with JWPL?**

JWPL was designed for accessing Wikipedia in the scope of large-scale NLP applications. Thus, JWPL accesses an optimized data representation to allow for efficient access.
If you need to query a running Wikipedia installation, you should use another API, e.g. [http://www.mediawiki.org/wiki/API].

**Q: How can I verify the integrity of the downloaded data?**

For each Wikipedia file there is another file with the same name and the suffix .md5. Compute the md5-HashSum of the file you downloaded (e.g. md5sum under Unix) and compare it with the one in the file.

**Q: I have a question that is not answered by any of the Wiki pages. Whom should I contact?**

For any technical questions about JWPL, please use the [JWPL Mailing List](http://groups.google.com/group/jwpl).