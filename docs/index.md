Description
===========

Extensions for the [ocfl-java]{:target=_blank} library.

[ocfl-java]: https://github.com/OCFL/ocfl-java

Using the library
-----------------

To use this library in a Maven-based project, add the following to your `pom.xml`.

### 1. Declare the DANS maven repository

```xml

<repositories>
    <!-- possibly other repository declarations here ... -->
    <repository>
        <id>DANS</id>
        <releases>
            <enabled>true</enabled>
        </releases>
        <url>https://maven.dans.knaw.nl/releases/</url>
    </repository>
</repositories>
```

### 2. Include a dependency on this library

```xml

<dependency>
    <groupId>nl.knaw.dans.lib</groupId>
    <artifactId>dans-ocfl-java-extensions-lib</artifactId>
    <version>{version}</version> <!-- <=== FILL LIBRARY VERSION TO USE HERE -->
</dependency>
```
