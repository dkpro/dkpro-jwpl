[Back to the JWPL Core overview page](JWPL_Core.md)

<b>Q: Is it possible to load the data into $DATABASE-SERVER$ ?</b>
> Probably yes. It should always work with MySQL. However, the data is shipped as SQL insert statements that should be database vendor independent, but we have not tested it with all database servers out there. So, just try it and let us know whether it works with a certain system.

<b>Q: Why does the first connection to a newly installed database take so much time?</b>
> When first connecting to a newly installed database, indexes are created. This takes some time (up to 30 minutes), depending on the server and the size of your Wikipedia. Subsequent connects will be much faster.

<b>Q: Why can't I use my already running Mediawiki/Wikipedia installation with JWPL?</b>
> JWPL was designed for accessing Wikipedia in the scope of large-scale NLP applications. Thus, JWPL accesses an optimized data representation to allow for efficient access.
> If you need to query a running Wikipedia installation, you should use another API, e.g., <a href='http://www.mediawiki.org/wiki/API'><a href='http://www.mediawiki.org/wiki/API'>http://www.mediawiki.org/wiki/API</a></a> or <a href='http://search.cpan.org/~bricas/WWW-Wikipedia-1.92/lib/WWW/Wikipedia.pm'><a href='http://search.cpan.org/~bricas/WWW-Wikipedia-1.92/lib/WWW/Wikipedia.pm'>http://search.cpan.org/~bricas/WWW-Wikipedia-1.92/lib/WWW/Wikipedia.pm</a></a>

<b>Q: How can I verify the integrity of the downloaded data.</b>
> For each Wikipedia file there is another file with the same name and the suffix .md5. Compute the md5-HashSum of the file you downloaded (e.g. `md5sum` under `*`nix) and compare it with the one in the file.

<b>Q: I have a question that is not answered by any of the Wiki pages. Whom should I contact?</b>
> For any technical questions about JWPL, please use the [JWPL Mailing List](http://groups.google.com/group/jwpl)