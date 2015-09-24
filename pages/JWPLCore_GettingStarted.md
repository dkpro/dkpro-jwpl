---
layout: page-fullwidth
title: "Getting Started with JWPL Core"
permalink: "/JWPLCore_GettingStarted/"
---

[Back to the JWPL Core overview page](/dkpro-jwpl/JWPL_Core)

## Getting Started
  1. Create the JWPL database(s) using the DataMachine or TimeMachine.
  
  * Use the DataMachine, if you just need a single database from a certain date.
  * Use the TimeMachine (part of the Revision Toolkit) to create multiple Wikipedia databases corresponding to past states of Wikipedia
    
  2. You can now access the JWPL databases with the JWPL API.
  
  * You have several possibilities how to use the API
    * Ceck out the API from SVN and build it using Maven
    * Add it as a dependency to your existing Maven project as described in the DeveloperSetup
  * Follow the examples in the [tutorial](/dkpro-jwpl/JwplTutorial). The tutorials are also included in the source code and the archives.
    
  3. If you encounter any problems, please check the [FAQ](/dkpro-jwpl/JWPL_FAQ) and the [JWPL Mailing List](http://groups.google.com/group/jwpl).

**Note:** If you need access to the revision history of the pages in your JWPL database, you might want to check out the RevisionMachine, which adds the revision data to your existing database and provides an additional API (RevisionAPI) to access the data.