---
layout: page-fullwidth
title: "Downloads"
permalink: "/downloads/"
---

{% assign stable = site.data.releases | where: "status", "stable" | first %}

## Maven

{{ site.title }} is available via the Maven infrastructure.

{% highlight xml %}
<properties>
  <dkpro.jwpl.version>{{ stable.version }}</dkpro.jwpl.version>
</properties>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>{{ stable.groupId }}</groupId>
      <artifactId>{{ stable.artifactId }}</artifactId>
      <version>${dkpro.jwpl.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>{{ stable.groupId }}</groupId>
    <artifactId>dkpro-jwpl-api</artifactId>
  </dependency>
</dependencies>
{% endhighlight xml %}

A full list of artifacts is available from [Maven Central][1]! 
  
## Sources

Get the sources from [GitHub](https://github.com/dkpro/dkpro-jwpl/releases/tag/dkpro-jwpl-{{ stable.version }}).

[1]: https://central.sonatype.com/namespace/{{ stable.groupId }}


