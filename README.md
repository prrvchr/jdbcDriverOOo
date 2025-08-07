<!--
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
║                                                                                    ║
║   Permission is hereby granted, free of charge, to any person obtaining            ║
║   a copy of this software and associated documentation files (the "Software"),     ║
║   to deal in the Software without restriction, including without limitation        ║
║   the rights to use, copy, modify, merge, publish, distribute, sublicense,         ║
║   and/or sell copies of the Software, and to permit persons to whom the Software   ║
║   is furnished to do so, subject to the following conditions:                      ║
║                                                                                    ║
║   The above copyright notice and this permission notice shall be included in       ║
║   all copies or substantial portions of the Software.                              ║
║                                                                                    ║
║   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,                  ║
║   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES                  ║
║   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.        ║
║   IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY             ║
║   CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,             ║
║   TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE       ║
║   OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                                    ║
║                                                                                    ║
╚════════════════════════════════════════════════════════════════════════════════════╝
-->
# [![jdbcDriverOOo logo][1]][2] Documentation

**Ce [document][3] en français.**

**The use of this software subjects you to our [Terms Of Use][4]**

# version [1.5.6][5]

## Introduction:

**jdbcDriverOOo** is part of a [Suite][6] of [LibreOffice][7] ~~and/or [OpenOffice][8]~~ extensions allowing to offer you innovative services in these office suites.  

This extension is the transcription in pure Java of the [java.sql.*][9] API to the SDBC API of UNO (ie: [com.sun.star.sdbc][10], [com.sun.star.sdbcx][11] and [com.sun.star.sdb][12]).
**It allows you to use the JDBC driver of your choice directly in Base.**

It embeds the drivers for the following databases:
- [HyperSQL or HsqlDB][13] version 2.7.4
- [SQLite via xerial sqlite-jdbc][14] version 3.50.2.1-SNAPSHOT
- [MariaDB via Connector/J][15] version 3.5.3
- [PostgreSQL via pgJDBC][16] version 42.7.5
- [H2 Database Engine][17] version 2.2.224
- [Apache Derby][18] version 11.16.1.1
- Firebird via [Jaybird][19] version 6.0.2 and [JaybirdEmbedded][20] version 1.0.0
- [MySQL via Connector/J][21] version 9.3.0
- [Trino or PrestoSQL][22] version 458-SNAPSHOT (currently being integrated, use with caution)

Thanks to drivers providing an integrated database engine such as: HsqlDB, H2, SQLite, Derby or Jaybird, it is possible in Base to very easily create and manage databases, as easily as creating Writer documents.  
You will find the information needed to create a database with these drivers in the section: [Connection URL][30]

Being free software I encourage you:
- To duplicate its [source code][31].
- To make changes, corrections, improvements.
- To open [issue][32] if needed.

In short, to participate in the development of this extension.  
Because it is together that we can make Free Software smarter.

___

## Requirement:

jdbcDriverOOo is a JDBC driver written in Java.  
Its use requires the [installation and configuration][33] in LibreOffice of a **Java JRE or JDK version 17 or later**.  
I recommend [Adoptium][34] as your Java installation source.

**If you are using a version of LibreOffice lower than 25.8.x, then you must manually install the Java instrumentation.** To install the Java instrumentation with LibreOffice, please refer to the [How to install Java Instrumentation][35] section.

The minimum version of LibreOffice supported by the jdbcDriverOOo extension depends on how you installed LibreOffice on your computer:

- **Regardless of platform**, if you installed LibreOffice from the [LibreOffice download site][36], **the minimum version of LibreOffice is 7.0**.

- **On Linux**, if you used the package manager to install LibreOffice, **the minimum version of LibreOffice is 6.0**. However, you must ensure that the system-provided Python version is not lower than 3.8.  
  In addition, you may experience the following issues:
  - You are subject to [bug #139538][37]. To work around the problem, please **uninstall the packages** with commands:
    - `sudo apt remove libreoffice-sdbc-hsqldb` to uninstall the libreoffice-sdbc-hsqldb package.
    - `sudo apt remove libhsqldb1.8.0-java` to uninstall the libhsqldb1.8.0-java package.  
  If you still want to use the Embedded HsqlDB functionality provided by LibreOffice, then install the [HyperSQLOOo][38] extension.
  - Your system-provided Python packages are out of date. The extension's logging will allow you to check if this is the case. It is accessible via the menu: **Tools -> Options -> LibreOffice Base -> Pure Java JDBC driver -> UNO driver settings -> View log -> System Info** and requires restarting LibreOffice after activation.  
  If outdated packages appear, you can update them with the command:  
  `pip install --upgrade <package-name>`  
  For more information see: [What has been done for version 1.1.0][39].

___

## Installation:

It seems important that the file was not renamed when it was downloaded.  
If necessary, rename it before installing it.

- ![jdbcDriverOOo logo][40] Install **[jdbcDriverOOo.oxt][41]** extension [![Version][42]][41]

Restart LibreOffice after installation.  
**Be careful, restarting LibreOffice may not be enough.**
- **On Windows** to ensure that LibreOffice restarts correctly, use Windows Task Manager to verify that no LibreOffice services are visible after LibreOffice shuts down (and kill it if so).
- **Under Linux or macOS** you can also ensure that LibreOffice restarts correctly, by launching it from a terminal with the command `soffice` and using the key combination `Ctrl + C` if after stopping LibreOffice, the terminal is not active (no command prompt).

After restarting LibreOffice, you can ensure that the extension and its driver are correctly installed by checking that the `io.github.prrvchr.jdbcDriverOOo.Driver` driver is listed in the **Connection Pool**, accessible via the menu: **Tools -> Options -> LibreOffice Base -> Connections**. It is not necessary to enable the connection pool.

If the driver is not listed, the reason for the driver failure can be found in the extension's logging. This log is accessible via the menu: **Tools -> Options -> LibreOffice Base -> Pure Java JDBC Driver -> Logging Options**.  
The `Driver` logging must first be enabled and then LibreOffice restarted to get the error message in the log.

**Warning don't forget:**
- To first update the version of the Java JRE or JDK installed on your computer if needed, this new version of jdbcDriverOOo requires **Java version 17 or later** instead of Java 11 previously.
- To install Java instrumentation if LibreOffice is lower than 25.8, please follow the description in section [How to install Java instrumentation][35].

___

## Use:

This explains how to use an HsqlDB database.  
The protocols supported by HsqlDB are: hsql://, hsqls://, http://, https://, mem://, file:// and res://.  
This mode of use explains how to connect with the **file://** and **hsql://** protocols.

### How to create a new database:

In LibreOffice / OpenOffice go to menu: **File -> New -> Database**

![jdbcDriverOOo screenshot 1][43]

In step: **Select database**
- select: Connect to an existing database
- choose: **HsqlDB Driver**
- click on button: Next

![jdbcDriverOOo screenshot 2][44]

In step: **Connection settings**

- for the protocol: **file://**
    - in Datasource URL put:
        - for **Linux**: `file:///tmp/testdb;shutdown=true;hsqldb.default_table_type=cached;get_column_name=false`
        - for **Windows**: `file:///c:/tmp/testdb;shutdown=true;hsqldb.default_table_type=cached;get_column_name=false`

- for the protocol: **hsql://**
    - In a terminal, go to a folder containing the hsqldb.jar archive and run:
        - for **Linux**: `java -cp hsqldb.jar org.hsqldb.server.Server --database.0 file:///tmp/testdb --silent false`
        - for **Windows**: `java -cp hsqldb.jar org.hsqldb.server.Server --database.0 file:///c:/tmp/testdb --silent false`
    - in Datasource URL put: `hsql://localhost/`

- click on button: Next

![jdbcDriverOOo screenshot 3][45]

In step: **Set up user authentication**
- click on button: Test connection

![jdbcDriverOOo screenshot 4][46]

If the connection was successful, you should see this dialog window:

![jdbcDriverOOo screenshot 5][47]

Have fun...

### How to update the JDBC driver:

If you want to update an embedded HsqlDB database (single odb file), please refer to the section: [How to migrate an embedded database][48].

It is possible to update the JDBC driver (hsqldb.jar, h2.jar, derbytools.jar) to a newer version.  
If you use HsqlDB as database, follow these steps:
1. Make a copy (backup) of the folder containing your database.
2. Start LibreOffice / OpenOffice and change the version of the HsqlDB driver via menu: **Tools -> Options -> LibreOffice Base -> Pure Java JDBC Driver -> JDBC drivers settings**, by a more recent version.
3. Restart LibreOffice / OpenOffice after changing the driver (hsqldb.jar).
4. In Base, after opening your database, go to menu: **Tools -> SQL** and type the SQL command: `SHUTDOWN COMPACT` or `SHUTDOWN SCRIPT`.

Now your database is up to date.

___

## LibreOffice/OpenOffice Base improvement:

This driver allows in LibreOffice / OpenOffice Base the management of **users**, **roles** (groups) and their associated **privileges** of the underlying database.

### Managing Users and Privileges in Base:

User management of the underlying database is accessible in Base via the menu: **Administration -> User administration**

![jdbcDriverOOo screenshot 6][49]

The privileges management of the users of the underlying database is accessible in this window by the button: **Change privileges**  
If the privilege is inherited from an assigned role, the checkbox is a three-state type.

![jdbcDriverOOo screenshot 7][50]

### Managing roles (groups) in Base:

The management of the roles (groups) of the underlying database is accessible in Base via the menu: **Administration -> Group administration**

![jdbcDriverOOo screenshot 8][51]

The management of users who are members of the group of the underlying database is accessible in this window via the button: **Group users**

![jdbcDriverOOo screenshot 9][52]

The management of roles assigned to the group of the underlying database is accessible in this window via the button: **Group roles**  
This functionality is an extension of the UNO API and will only be available if the underlying LibreOffice / OpenOffice driver allows it.

![jdbcDriverOOo screenshot 10][53]

___

## Connection URL:

Certain databases such as HsqlDB, H2, SQLite Derby or Firebird via Jaybird allow the creation of the database during connection if this database does not yet exist.
This feature makes it as easy to create databases as Writer documents. Generally it is enough to add the option expected by the driver to the connection URL.
This connection URL may be different depending on the operating system of your computer (Windows, Linux or MacOS).  
To create a database, in LibreOffice go to the menu: **File -> New -> Database -> Connect to an existing database**, then according to your choice:
- **HsqlDB pure Java**:
  - Linux: `file:///home/prrvchr/testdb/hsqldb/db;hsqldb.default_table_type=cached;create=true`
  - Windows: `C:\Utilisateurs\prrvc\testdb\hsqldb\db;hsqldb.default_table_type=cached;create=true`
- **H2 pure Java**:
  - Linux: `file:///home/prrvchr/testdb/h2/db`
  - Windows: `C:\Utilisateurs\prrvc\testdb\h2\db`
- **SQLite pure Java**:
  - Linux: `file:///home/prrvchr/testdb/sqlite/test.db`
  - Windows: `C:/Utilisateurs/prrvc/testdb/sqlite/test.db`
- **Derby pure Java**:
  - Linux: `/home/prrvchr/testdb/derby;create=true`
  - Windows: `C:\Utilisateurs\prrvc\testdb\derby;create=true`
- **Firebird pure Java**:
  - Linux: `embedded:/home/prrvchr/testdb/firebird?createDatabaseIfNotExist=true`
  - Windows: `embedded:C:\Utilisateurs\prrvc\testdb\firebird?createDatabaseIfNotExist=true`

    Firebird uses [JaybirdEmbedded][20] for its embedded mode to work. You can find the supported platforms in the [JaybirdEmbedded][20] documentation.  
    For unsupported platforms, you can always install the [Firebird Server][54] for your platform.

___

## How to install Java Instrumentation:

In order to use the Java SPI services offered by the `RowSetFactory.jar` implementation, the Java instrumentation must be installed under LibreOffice.  
This is done automatically starting from LibreOffice version 25.8.x, but must be done manually for lower versions.  
Here are the different steps:
- Download the archive [InstrumentationAgent.jar][55] and place it in a folder.
- In LibreOffice, go to **Tools -> Options -> LibreOffice -> Advanced -> Java Options -> Settings -> Java Startup Settings** and add the following command:
    - For Windows: `-javaagent:c:\folder\InstrumentationAgent.jar`.
    - For Linux: `-javaagent:/folder/InstrumentationAgent.jar`.

    Of course, the path to the archive remains to be adapted to your use case.
- Restart LibreOffice to apply these changes.

If you think it would be good to avoid this manipulation, then ask LibreOffice to [backport the Java instrumentation][56].

___

## How to build the extension:

Normally, the extension is created with Eclipse for Java and [LOEclipse][57]. To work around Eclipse, I modified LOEclipse to allow the extension to be created with Apache Ant.  
To create the jdbcDriverOOo extension with the help of Apache Ant, you need to:
- Install the [Java SDK][58] version 17 or higher.
- Install [Apache Ant][59] version 1.10.0 or higher.
- Install [LibreOffice and its SDK][60] version 7.x or higher.
- Clone the [jdbcDriverOOo][61] repository on GitHub into a folder.
- From this folder, move to the directory: `source/jdbcDriverOOo/`
- In this directory, edit the file: `build.properties` so that the `office.install.dir` and `sdk.dir` properties point to the folders where LibreOffice and its SDK were installed, respectively.
- Start the archive creation process using the command: `ant`
- You will find the generated archive in the subfolder: `dist/`

___

## Has been tested with:

* LibreOffice 24.2.1.2 (x86_64)- Windows 10

* LibreOffice 7.3.7.2 - Lubuntu 22.04

* LibreOffice 24.2.1.2 - Lubuntu 22.04

* LibreOffice 24.8.0.3 (x86_64) - Windows 10(x64) - Python version 3.9.19 (under Lubuntu 22.04 / VirtualBox 6.1.38)

I encourage you in case of problem :confused:  
to create an [issue][32]  
I will try to solve it :smile:

___

## Historical:

### Introduction:

This driver was written to work around certain problems inherent in the UNO implementation of the JDBC driver built into LibreOffice / OpenOffice, namely: 

- The inability to provide the path to the Java driver archive (hsqldb.jar) when loading the JDBC driver.
- Not being able to use prepared SQL statements (PreparedStatement) see [bug #132195][62].

In order to take advantage of the latest features offered by databases and among others HsqlDB, it was necessary to write a new driver.

Until version 0.0.3, this new driver is just a wrapper in Python around the UNO services provided by the defective LibreOffice / OpenOffice JDBC driver.  
Since version 0.0.4, it has been completely rewritten in Java under Eclipse, because who better than Java can provide access to JDBC in the UNO API?  
In order not to prevent the native JDBC driver from working, it loads when calling the following protocols:

- `xdbc:*`
- `xdbc:hsqldb:*`
- `xdbc:sqlite:*`
- `xdbc:mariadb:*`
- `xdbc:...`

but uses the `jdbc:*` protocol internally to connect.

It also provides functionality that the JDBC driver implemented in LibreOffice does not provide, namely:

- The management of users, roles (groups) and privileges in Base.
- The use of the SQL Array type in the queries.
- Everything we are ready to implement.

### [All changes are logged in the version History][63]

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_en>
[5]: <https://prrvchr.github.io/jdbcDriverOOo/CHANGELOG#what-has-been-done-for-version-156>
[6]: <https://prrvchr.github.io/>
[7]: <https://www.libreoffice.org/download/download-libreoffice/>
[8]: <https://www.openoffice.org/download/index.html>
[9]: <https://devdocs.io/openjdk~17/java.sql/java/sql/package-summary>
[10]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/module-ix.html>
[11]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/module-ix.html>
[12]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdb/module-ix.html>
[13]: <http://hsqldb.org/>
[14]: <https://prrvchr.github.io/sqlite-jdbc/>
[15]: <https://mariadb.com/downloads/connectors/connectors-data-access/java8-connector/>
[16]: <https://jdbc.postgresql.org/>
[17]: <https://www.h2database.com/html/main.html>
[18]: <https://db.apache.org/derby/>
[19]: <https://firebirdsql.org/en/jdbc-driver/>
[20]: <https://prrvchr.github.io/JaybirdEmbedded/>
[21]: <https://dev.mysql.com/downloads/connector/j/>
[22]: <https://trino.io/docs/current/client/jdbc.html#installing>
[30]: <https://prrvchr.github.io/jdbcDriverOOo/#connection-url>
[31]: <https://github.com/prrvchr/jdbcDriverOOo/>
[32]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[33]: <https://wiki.documentfoundation.org/Documentation/HowTo/Install_the_correct_JRE_-_LibreOffice_on_Windows_10>
[34]: <https://adoptium.net/temurin/releases/?version=17&package=jre>
[35]: <https://prrvchr.github.io/jdbcDriverOOo#how-to-install-java-instrumentation>
[36]: <https://www.libreoffice.org/download/download-libreoffice/>
[37]: <https://bugs.documentfoundation.org/show_bug.cgi?id=139538>
[38]: <https://prrvchr.github.io/HyperSQLOOo/>
[39]: <https://prrvchr.github.io/jdbcDriverOOo/CHANGELOG#what-has-been-done-for-version-110>
[40]: <img/jdbcDriverOOo.svg#middle>
[41]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/jdbcDriverOOo.oxt>
[42]: <https://img.shields.io/github/downloads/prrvchr/jdbcDriverOOo/latest/total?label=v1.5.6#right>
[43]: <img/jdbcDriverOOo-1.png>
[44]: <img/jdbcDriverOOo-2.png>
[45]: <img/jdbcDriverOOo-3.png>
[46]: <img/jdbcDriverOOo-4.png>
[47]: <img/jdbcDriverOOo-5.png>
[48]: <https://prrvchr.github.io/HyperSQLOOo/#how-to-migrate-an-embedded-database>
[49]: <img/jdbcDriverOOo-6.png>
[50]: <img/jdbcDriverOOo-7.png>
[51]: <img/jdbcDriverOOo-8.png>
[52]: <img/jdbcDriverOOo-9.png>
[53]: <img/jdbcDriverOOo-10.png>
[54]: <https://firebirdsql.org/en/firebird-5-0-3>
[55]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/InstrumentationAgent.jar>
[56]: <https://bugs.documentfoundation.org/show_bug.cgi?id=167071>
[57]: <https://github.com/LibreOffice/loeclipse>
[58]: <https://adoptium.net/temurin/releases/?version=17&package=jdk>
[59]: <https://ant.apache.org/manual/install.html>
[60]: <https://downloadarchive.documentfoundation.org/libreoffice/old/7.6.7.2/>
[61]: <https://github.com/prrvchr/jdbcDriverOOo.git>
[62]: <https://bugs.documentfoundation.org/show_bug.cgi?id=132195>
[63]: <https://prrvchr.github.io/jdbcDriverOOo/CHANGELOG>
