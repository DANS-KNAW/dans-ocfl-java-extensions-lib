MANUAL
======

Extensions for the [ocfl-java]{:target=_blank} library.

[ocfl-java]: https://github.com/OCFL/ocfl-java

DESCRIPTION
-----------
TODO

INSTALLATION
------------

To use this library in a Maven-based project:

1. Include in your `pom.xml` a declaration for the DANS maven repository:

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

2. Include a dependency on this library.

        <dependency>
            <groupId>nl.knaw.dans.lib</groupId>
            <artifactId>dans-ocfl-java-extensions-lib</artifactId>
            <version>{version}</version> <!-- <=== FILL LIBRARY VERSION TO USE HERE -->
        </dependency>
