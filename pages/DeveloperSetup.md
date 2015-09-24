---
layout: page-fullwidth
title: "JWPL Developer Setup"
permalink: "/DeveloperSetup/"
---

[Back to the main page](/dkpro-jwpl/documentation/)

# Maven Setup

As of version 0.7.0, all JWPL components are available on [Maven Central](http://search.maven.org/#search|ga|1|tudarmstadt.ukp.wikipedia). If you use Maven as your build tool, then you can add any JWPL component as a dependency to your `pom.xml` without having to perform any additional configuration:

For adding JWPL components in the most recent version to your Maven project, add any of the following dependencies to your `pom.xml`:

{% highlight xml %}
<dependency>
  <groupId>de.tudarmstadt.ukp.wikipedia</groupId>
  <artifactId>de.tudarmstadt.ukp.wikipedia.api</artifactId>
  <version>1.0.0</version>
</dependency>
{% endhighlight xml %}

{% highlight xml %}
<dependency>
  <groupId>de.tudarmstadt.ukp.wikipedia</groupId>
  <artifactId>de.tudarmstadt.ukp.wikipedia.datamachine</artifactId>
  <version>1.0.0</version>
</dependency>
{% endhighlight xml %}

{% highlight xml %}
<dependency>
  <groupId>de.tudarmstadt.ukp.wikipedia</groupId>
  <artifactId>de.tudarmstadt.ukp.wikipedia.revisionmachine</artifactId>
  <version>1.0.0</version>
</dependency>
{% endhighlight xml %}

{% highlight xml %}
<dependency>
  <groupId>de.tudarmstadt.ukp.wikipedia</groupId>
  <artifactId>de.tudarmstadt.ukp.wikipedia.timemachine</artifactId>
  <version>1.0.0</version>
</dependency>
{% endhighlight xml %}

{% highlight xml %}
<dependency>
  <groupId>de.tudarmstadt.ukp.wikipedia</groupId>
  <artifactId>de.tudarmstadt.ukp.wikipedia.util</artifactId>
  <version>1.0.0</version>
</dependency>
{% endhighlight xml %}

{% highlight xml %}
<dependency>
  <groupId>de.tudarmstadt.ukp.wikipedia</groupId>
  <artifactId>de.tudarmstadt.ukp.wikipedia.parser</artifactId>
  <version>1.0.0</version>
</dependency>
{% endhighlight xml %}

# Eclipse Setup

We use the following tools for development:

    * [Eclipse](http://eclipse.org/) 3.6 or higher. We recommend the _Eclipse Classic_ distribution.
    * [m2e](http://eclipse.org/m2e/) 1.0 or higher.

### For Windows Users

**Note:** On your machine the Java path may be different, e.g. because you are using a localized Windows version it may be `C:\Programme\...` -or- because you may have a Java version other than 1.6.0.01.

  1. Edit your `eclipse.ini` and add/change the following lines (the `-vmargs` line should be present already):

{% highlight bat %}
-vm
C:/Program Files/Java/jdk1.6.0_01/bin/javaw.exe
-vmargs
{% endhighlight bat %}

    * Make sure that the linebreaks are as shown above (the formatting is actually necessary)
    
  1. Open Eclipse
  
    * Open the **preferences**
    * Go to **Java -> Installed JREs**
    * Click **Search** and choose your Java directory
    * **Close** the preferences and **re-open** them
    * **Select** _jdk1.6.0\_01_ as your JRE (this should match the entry you added in the eclipse.ini)

# Checking out

tbd.

<!--
  * Open the **SVN Repositories** perspective in Eclipse (Menu -> Window -> Show View -> Other... -> SVN -> SVN Repositories)
  * **Add** a SVN repository with the URL `http://jwpl.googlecode.com/svn`
  * **Expand** the new repository node in the SVN Repositories view
  * Right-click on **trunk** and select **Check out as Maven project**
    * **Note:** if you do not see this menu item, make sure you have installed the _Maven SCM handler for Subclipse_.
  * (optional) Eclipse will create a large number of projects now. We recommend to group these projects into a _working set_:
    * Select **Next**
    * Check **Add project(s) to working set**
    * Click **More...**
    * Click **New...**
    * Double-click **Java**
    * Enter the working set name `JWPL`
    * Click **Finish**
    * Click **OK**
    * Select the working set `JWPL` from the working set drop-down box
    * **Note:** when you are completely through with these and the following steps, remember to go to the Package Explorer view. There is a small triangular icon in its top right corner. Click on that and select Top Level Elements -> Working Sets.
  * Click **Finish**. -->