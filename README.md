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

# version [1.4.6][5]

## Introduction:

**jdbcDriverOOo** is part of a [Suite][6] of [LibreOffice][7] ~~and/or [OpenOffice][8]~~ extensions allowing to offer you innovative services in these office suites.  

This extension is the transcription in pure Java of the [java.sql.*][9] API to the [com.sun.star.sdbc][10], [com.sun.star.sdbcx][11] and [com.sun.star.sdb][12] API of UNO.
**It allows you to use the JDBC driver of your choice directly in Base.**  
It embeds the drivers for the following databases:
- [HyperSQL or HsqlDB][13] version 2.7.4
- [SQLite via xerial sqlite-jdbc][14] version 3.45.1.6-SNAPSHOT
- [MariaDB via Connector/J][15] version 3.5.3
- [PostgreSQL via pgJDBC][16] version 42.7.5
- [H2 Database Engine][17] version 2.2.224
- [Apache Derby][18] version 11.16.1.1
- [Firebird via Jaybird][19] version 6.0.1
- [MySQL via Connector/J][20] version 9.3.0
- [Trino or PrestoSQL][21] version 458-SNAPSHOT (currently being integrated, use with caution)

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

If you are using the HsqlDB driver with **LibreOffice on Linux**, then you are subject to [bug #139538][35]. To work around the problem, please **uninstall the packages** with commands:
- `sudo apt remove libreoffice-sdbc-hsqldb` (to uninstall the libreoffice-sdbc-hsqldb package)
- `sudo apt remove libhsqldb1.8.0-java` (to uninstall the libhsqldb1.8.0-java package)

If you still want to use the Embedded HsqlDB functionality provided by LibreOffice, then install the [HyperSQLOOo][36] extension.  

**On Linux and macOS the Python packages** used by the extension, if already installed, may come from the system and therefore **may not be up to date**.  
To ensure that your Python packages are up to date it is recommended to use the **System Info** option in the extension Options accessible by:  
**Tools -> Options -> LibreOffice Base -> Pure Java JDBC driver -> UNO driver settings -> View log -> System Info**  
If outdated packages appear, you can update them with the command:  
`pip install --upgrade <package-name>`

For more information see: [What has been done for version 1.1.0][37].

___

## Installation:

It seems important that the file was not renamed when it was downloaded.  
If necessary, rename it before installing it.

- ![jdbcDriverOOo logo][38] Install **[jdbcDriverOOo.oxt][39]** extension [![Version][40]][39]

Restart LibreOffice after installation.  
**Be careful, restarting LibreOffice may not be enough.**
- **On Windows** to ensure that LibreOffice restarts correctly, use Windows Task Manager to verify that no LibreOffice services are visible after LibreOffice shuts down (and kill it if so).
- **Under Linux or macOS** you can also ensure that LibreOffice restarts correctly, by launching it from a terminal with the command `soffice` and using the key combination `Ctrl + C` if after stopping LibreOffice, the terminal is not active (no command prompt).

After restarting LibreOffice, you can ensure that the extension and its driver are correctly installed by checking that the `io.github.prrvchr.jdbcDriverOOo.Driver` driver is listed in the **Connection Pool**, accessible via the menu: **Tools -> Options... -> LibreOffice Base -> Connections**. It is not necessary to enable the connection pool.

If the driver is not listed, the reason for the driver failure can be found in the extension's logging. This log is accessible via the menu: **Tools -> Options... -> LibreOffice Base -> Pure Java JDBC Driver -> Logging Options**.  
The `Driver` logging must first be enabled, then LibreOffice restarted and the **Connection Pool** checked again to force the driver to load and obtain the error message in the log.

Remember to first update the version of the Java JRE or JDK installed on your computer, this new version of jdbcDriverOOo requires **Java version 17 or later** instead of Java 11 previously.

___

## Use:

This explains how to use an HsqlDB database.  
The protocols supported by HsqlDB are: hsql://, hsqls://, http://, https://, mem://, file:// and res://.  
This mode of use explains how to connect with the **file://** and **hsql://** protocols.

### How to create a new database:

In LibreOffice / OpenOffice go to menu: **File -> New -> Database**

![jdbcDriverOOo screenshot 1][41]

In step: **Select database**
- select: Connect to an existing database
- choose: **HsqlDB Driver**
- click on button: Next

![jdbcDriverOOo screenshot 2][42]

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

![jdbcDriverOOo screenshot 3][43]

In step: **Set up user authentication**
- click on button: Test connection

![jdbcDriverOOo screenshot 4][44]

If the connection was successful, you should see this dialog window:

![jdbcDriverOOo screenshot 5][45]

Have fun...

### How to update the JDBC driver:

If you want to update an embedded HsqlDB database (single odb file), please refer to the section: [How to migrate an embedded database][46].

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

![jdbcDriverOOo screenshot 6][47]

The privileges management of the users of the underlying database is accessible in this window by the button: **Change privileges**  
If the privilege is inherited from an assigned role, the checkbox is a three-state type.

![jdbcDriverOOo screenshot 7][48]

### Managing roles (groups) in Base:

The management of the roles (groups) of the underlying database is accessible in Base via the menu: **Administration -> Group administration**

![jdbcDriverOOo screenshot 8][49]

The management of users who are members of the group of the underlying database is accessible in this window via the button: **Group users**

![jdbcDriverOOo screenshot 9][50]

The management of roles assigned to the group of the underlying database is accessible in this window via the button: **Group roles**  
This functionality is an extension of the UNO API and will only be available if the underlying LibreOffice / OpenOffice driver allows it.

![jdbcDriverOOo screenshot 10][51]

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

## [Historical][52]:

### Introduction:

This driver was written to work around certain problems inherent in the UNO implementation of the JDBC driver built into LibreOffice / OpenOffice, namely: 

- The inability to provide the path to the Java driver archive (hsqldb.jar) when loading the JDBC driver.
- Not being able to use prepared SQL statements (PreparedStatement) see [bug #132195][53].

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

### All changes are logged in the [version History][52]

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_en>
[5]: <https://prrvchr.github.io/jdbcDriverOOo/CHANGELOG#what-has-been-done-for-version-150>
[6]: <https://prrvchr.github.io/>
[7]: <https://www.libreoffice.org/download/download-libreoffice/>
[8]: <https://www.openoffice.org/download/index.html>
[9]: <https://devdocs.io/openjdk~17/java.sql/java/sql/package-summary>
[10]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/module-ix.html>
[11]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/module-ix.html>
[12]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdb/module-ix.html>
[13]: <http://hsqldb.org/>
[14]: <https://github.com/xerial/sqlite-jdbc>
[15]: <https://mariadb.com/downloads/connectors/connectors-data-access/java8-connector/>
[16]: <https://jdbc.postgresql.org/>
[17]: <https://www.h2database.com/html/main.html>
[18]: <https://db.apache.org/derby/>
[19]: <https://firebirdsql.org/en/jdbc-driver/>
[20]: <https://dev.mysql.com/downloads/connector/j/>
[21]: <https://trino.io/docs/current/client/jdbc.html#installing>
[30]: <https://prrvchr.github.io/jdbcDriverOOo/#connection-url>
[31]: <https://github.com/prrvchr/jdbcDriverOOo/>
[32]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[33]: <https://wiki.documentfoundation.org/Documentation/HowTo/Install_the_correct_JRE_-_LibreOffice_on_Windows_10>
[34]: <https://adoptium.net/temurin/releases/?version=17&package=jre>
[35]: <https://bugs.documentfoundation.org/show_bug.cgi?id=139538>
[36]: <https://prrvchr.github.io/HyperSQLOOo/>
[37]: <https://prrvchr.github.io/jdbcDriverOOo/CHANGELOG#what-has-been-done-for-version-110>
[38]: <img/jdbcDriverOOo.svg#middle>
[39]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/jdbcDriverOOo.oxt>
[40]: <https://img.shields.io/github/downloads/prrvchr/jdbcDriverOOo/latest/total?label=v1.4.6#right>
[41]: <img/jdbcDriverOOo-1.png>
[42]: <img/jdbcDriverOOo-2.png>
[43]: <img/jdbcDriverOOo-3.png>
[44]: <img/jdbcDriverOOo-4.png>
[45]: <img/jdbcDriverOOo-5.png>
[46]: <https://prrvchr.github.io/HyperSQLOOo/#how-to-migrate-an-embedded-database>
[47]: <img/jdbcDriverOOo-6.png>
[48]: <img/jdbcDriverOOo-7.png>
[49]: <img/jdbcDriverOOo-8.png>
[50]: <img/jdbcDriverOOo-9.png>
[51]: <img/jdbcDriverOOo-10.png>
[52]: <https://prrvchr.github.io/jdbcDriverOOo/CHANGELOG>
[53]: <https://bugs.documentfoundation.org/show_bug.cgi?id=132195>
