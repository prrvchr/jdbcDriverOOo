<!--
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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

# version [1.0.7][5]

## Introduction:

**jdbcDriverOOo** is part of a [Suite][6] of [LibreOffice][7] and/or [OpenOffice][8] extensions allowing to offer you innovative services in these office suites.  

This extension is the transcription in pure Java of the [java.sql.*][9] API to the [com.sun.star.sdbc][10], [com.sun.star.sdbcx][11] and [com.sun.star.sdb][12] API of UNO.
It allows you to use the JDBC driver of your choice directly in Base.  
It embeds the drivers for the following databases:
- [HyperSQL or HsqlDB][13] version 2.72
    The supported HsqlDB managed protocols are: hsql://, hsqls://, http://, https://, mem://, file:// and res://
- [SQLite JDBC Driver][14] version 3.42.0.0
- [MariaDB Connector/J][15] version 3.1.4
- [H2 Database Engine][16] version 2.2.220 (2023-07-04)
- [Apache Derby][17] version 10.15.2.0
- [SmallSQL][18] version 0.22

Being free software I encourage you:
- To duplicate its [source code][19].
- To make changes, corrections, improvements.
- To open [issue][20] if needed.

In short, to participate in the development of this extension.  
Because it is together that we can make Free Software smarter.

___

## Requirement:

jdbcDriverOOo is a JDBC driver written in Java.  
Its use requires the [installation and configuration][21] in LibreOffice / OpenOffice of a **JRE version 11 or later**.  
I recommend [Adoptium][22] as your Java installation source.

If you are using the HsqlDB driver with **LibreOffice on Linux**, then you are subject to [bug 139538][23]. To work around the problem, please **uninstall the packages** with commands:
- `sudo apt remove libreoffice-sdbc-hsqldb` (to uninstall the libreoffice-sdbc-hsqldb package)
- `sudo apt remove libhsqldb1.8.0-java` (to uninstall the libhsqldb1.8.0-java package)

If you still want to use the Embedded HsqlDB functionality provided by LibreOffice, then install the [HyperSQLOOo][24] extension.  

___

## Installation:

It seems important that the file was not renamed when it was downloaded.
If necessary, rename it before installing it.

- ![jdbcDriverOOo logo][25] Install **[jdbcDriverOOo.oxt][26]** extension [![Version][27]][26]

Restart LibreOffice / OpenOffice after installation.

___

## Use:

This mode of use uses an HsqlDB database.

### How to create a new database:

In LibreOffice / OpenOffice go to menu: **File -> New -> Database**

![jdbcDriverOOo screenshot 1][28]

In step: **Select database**
- select: Connect to an existing database
- choose: HsqlDB Driver
- click on button: Next

![jdbcDriverOOo screenshot 2][29]

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

![jdbcDriverOOo screenshot 3][30]

In step: **Set up user authentication**
- click on button: Test connection

![jdbcDriverOOo screenshot 4][31]

If the connection was successful, you should see this dialog window:

![jdbcDriverOOo screenshot 5][32]

Have fun...

### How to update the JDBC driver:

It is possible to update the JDBC driver (hsqldb.jar, h2.jar, derbytools.jar) to a newer version.  
If you use HsqlDB as database, follow these steps:
1. Make a copy (backup) of the folder containing your database.
2. Start LibreOffice / OpenOffice and change the version of the HsqlDB driver via menu: **Tools -> Options -> Base drivers -> JDBC Driver**, by a more recent version.
3. Restart LibreOffice / OpenOffice after changing the driver (hsqldb.jar).
4. In Base, after opening your database, go to menu: **Tools -> SQL** and type the SQL command: `SHUTDOWN COMPACT` or `SHUTDOWN SCRIPT`.

Now your database is up to date.

___

## LibreOffice/OpenOffice Base improvement:

This driver allows in LibreOffice / OpenOffice Base the management of **users**, **roles** (groups) and their associated **privileges** of the underlying database.

### Managing Users and Privileges in Base:

User management of the underlying database is accessible in Base via the menu: **Administration -> User administration**

![jdbcDriverOOo screenshot 6][33]

The privileges management of the users of the underlying database is accessible in this window by the button: **Change privileges**  
If the privilege is inherited from an assigned role, the checkbox is a three-state type.

![jdbcDriverOOo screenshot 7][34]

### Managing roles (groups) in Base:

The management of the roles (groups) of the underlying database is accessible in Base via the menu: **Administration -> Group administration**

![jdbcDriverOOo screenshot 8][35]

The management of users who are members of the group of the underlying database is accessible in this window via the button: **Group users**

![jdbcDriverOOo screenshot 9][36]

The management of roles assigned to the group of the underlying database is accessible in this window via the button: **Group roles**  
This functionality is an extension of the UNO API and will only be available if the underlying LibreOffice / OpenOffice driver allows it.

![jdbcDriverOOo screenshot 10][37]

___

## Has been tested with:

* LibreOffice 7.0.4.2 - Ubuntu 20.04 - LxQt 0.14.1

* LibreOffice 6.4.4.2 - Windows 7 SP1

* Apache OpenOffice 4.1.13 - Lubuntu 22.04

I encourage you in case of problem :confused:  
to create an [issue][38]  
I will try to solve it :smile:

___

## Historical:

### Introduction:

This driver was written to work around certain problems inherent in the UNO implementation of the JDBC driver built into LibreOffice / OpenOffice, namely: 

- The inability to provide the path to the Java driver archive (hsqldb.jar) when loading the JDBC driver.
- Not being able to use prepared SQL statements (PreparedStatement) see [bug 132195][39].

In order to take advantage of the latest features offered by databases and among others HsqlDB, it was necessary to write a new driver.

Until version 0.0.3, this new driver is just a wrapper in Python around the UNO services provided by the defective LibreOffice / OpenOffice JDBC driver.  
Since version 0.0.4, it has been completely rewritten in Java under Eclipse, because who better than Java can provide access to JDBC in the UNO API...  
In order not to prevent the native JDBC driver from working, it loads when calling the following protocols:

- `xdbc:*`
- `xdbc:hsqldb:*`
- `xdbc:sqlite:*`
- `xdbc:mariadb:*`
- `xdbc:...`

but uses the `jdbc:*` protocol internally to connect.

It also provides functionality that the JDBC driver implemented in LibreOffice / OpenOffice does not provide, namely:

- The management of users, roles (groups) and privileges in Base.
- The use of the SQL Array type in the queries.
- Everything we are ready to implement.

### What has been done for version 0.0.1:

- The writing of this driver was facilitated by a [discussion with Villeroy][40], on the OpenOffice forum, which I would like to thank, because knowledge is only worth if it is shared...

- Using the new version of HsqlDB 2.5.1.

- Many other fix...

### What has been done for version 0.0.2:

- Added a dialog box allowing to update the driver (hsqldb.jar) in: Tools -> Options -> Base drivers -> HsqlDB driver

- Many other fix...

### What has been done for version 0.0.3:

- I especially want to thank fredt at [hsqldb.org][41] for:

    - His welcome for this project and his permission to use the HsqlDB logo in the extension.

    - Its involvement in the test phase which made it possible to produce this version 0.0.3.

    - The quality of its HsqlDB database.

- Now works with OpenOffice on Windows.

- An unsupported protocol now displays an accurate error.

- A non-parsable url now displays a precise error.

- Now correctly handles spaces in filenames and paths.

- Many other fix...

### What has been done for version 0.0.4:

- Rewrite of [Driver][42] in Java version 11 OpenJDK amd64 under Eclipse IDE for Java Developers version 4.23.0 with the plugins:
    - LOEclipse or LibreOffice Eclipse plugin for extension development version 4.0.1.
    - PyDev or Python IDE for Eclipse version 9.3.0.

- Writing the `Statement`, `PreparedStatement`, `CallableStatement`, `ResultSet`, `...` services of JDBC (thanks to hanya for [MRI][43] which was of great help to me...)

    - [com.sun.star.sdb.*][44]
    - [com.sun.star.sdbc.*][45]
    - [com.sun.star.sdbcx.*][46]

- Integration in jdbcDriverOOo of **H2** and **Derby** JDBC drivers in addition to **HsqlDB**. Implementation of Java Services:

    - [Driver-HsqlDB.jar][47]
    - [Driver-H2.jar][48]
    - [Driver-Derby.jar][49]

    In order to correct possible defects, or incompatibility with the UNO API, of embedded JDBC drivers.

- Renamed the **HsqlDBDriverOOo** repository and extension to **jdbcDriverOOo**.

- Support in Base for **auto-incrementing primary keys** for HsqlDB, H2 and Derby.

- Writing of [com.sun.star.sdbcx.Driver][50]. This high-level driver must allow the **management of users, roles and privileges in Base**. Its use can be disabled via the menu: **Tools -> Options -> Base drivers -> JDBC Driver**.

- Implemented a Java service provider [UnoLogger.jar][51] for the [SLF4J][52] API to be able to redirect driver logging from the underlying databases to the UNO API [com.sun.star.logging.*][53].

- Rewrite, following the MVC model, of the [Options][54] dialog accessible via the menu: **Tools -> Options -> Base drivers -> JDBC Driver**, to allow:

    - Updating and/or adding Java archives of JDBC drivers.
    - Enabling driver logging of the underlying database.

- Writing, following the MVC model, [administration windows][55] for users and roles (groups) and their associated privileges, accessible in Base via the menu: **Administration -> User administration** and/or **Administration - > Group administration**, allowing:

    - [Management of users][56] and their privileges.
    - [Management of roles][57] (groups) and their privileges.

    These new features have only been tested with the HsqlDB driver so far.

- Many other fix...

### What has been done for version 1.0.0:

- Integration of HyperSQL version 2.7.2.

### What has been done for version 1.0.1:

- Integration of [SQLite JDBC][14] version 3.42.0.0. I especially want to thank [gotson][58] for the [many improvements to the SQLite JDBC driver][59] that made it possible to use SQLite in LibreOffice/OpenOffice.

- This driver can be wrapped by another driver ([HyperSQLOOo][24] or [SQLiteOOo][60]) thanks to a connection url now modifiable.

- It is possible to display or not the system tables in: **Tools -> Options -> Base drivers -> JDBC Driver -> UNO drivers settings -> Show system tables**

- It is possible to disallow the use of updatable resultset in: **Tools -> Options -> Base drivers -> JDBC Driver -> UNO drivers settings -> Use bookmarks**

- Many corrections have been made to make the extension [SQLiteOOo][60] functional.

### What has been done for version 1.0.2:

- Integration of [MariaDB Connector/J][15] version 3.1.4.

- Many other fix...

### What has been done for version 1.0.3:

- Integration of [H2][17] version 2.2.220.

- Integration of logging in the resultset ([ResultSetBase][61] and [ResultSetSuper][62]) in order to learn more about [issue 156512][63].

- Many other fix...

### What has been done for version 1.0.4:

- Support in the creation of tables of the [TypeInfoSettings][64] parameter allowing to recover the precision for SQL types:

    - TIME
    - TIMESTAMP
    - TIME WITH TIME ZONE
    - TIMESTAMP WITH TIME ZONE

    This is only [integrated][65] for the [HsqlDB][66] driver at the moment.

### What has been done for version 1.0.5:

- The result of accessing the [XDatabaseMetaData.getDriverVersion()][67] method is now recorded in the log file.

### What has been done for version 1.0.6:

- Added the Python package `packaging` to the extension's `pythonpath`. Thanks to [artem78][68] for allowing this correction by reporting this oversight in [issue #4][69].

### What has been done for version 1.0.7:

- Now the driver throws an exception if creating a new table fails. This is to address [bug #1][70] on the [HyperSQLOOo][24] extension.

### What remains to be done for version 1.0.7:

- Add new languages for internationalization...

- Anything welcome...

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_en>
[5]: <https://prrvchr.github.io/jdbcDriverOOo/#what-has-been-done-for-version-107>
[6]: <https://prrvchr.github.io/>
[7]: <https://www.libreoffice.org/download/download-libreoffice/>
[8]: <https://www.openoffice.org/download/index.html>
[9]: <https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/package-summary.html>
[10]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/module-ix.html>
[11]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/module-ix.html>
[12]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdb/module-ix.html>
[13]: <http://hsqldb.org/>
[14]: <https://github.com/xerial/sqlite-jdbc>
[15]: <https://mariadb.com/downloads/connectors/connectors-data-access/java8-connector/>
[16]: <https://www.h2database.com/html/main.html>
[17]: <https://db.apache.org/derby/>
[18]: <https://github.com/CptTZ/SmallSQL>
[19]: <https://github.com/prrvchr/jdbcDriverOOo/>
[20]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[21]: <https://wiki.documentfoundation.org/Documentation/HowTo/Install_the_correct_JRE_-_LibreOffice_on_Windows_10>
[22]: <https://adoptium.net/releases.html?variant=openjdk11>
[23]: <https://bugs.documentfoundation.org/show_bug.cgi?id=139538>
[24]: <https://prrvchr.github.io/HyperSQLOOo/>
[25]: <img/jdbcDriverOOo.svg#middle>
[26]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/jdbcDriverOOo.oxt>
[27]: <https://img.shields.io/github/downloads/prrvchr/jdbcDriverOOo/latest/total?label=v1.0.7#right>
[28]: <img/jdbcDriverOOo-1.png>
[29]: <img/jdbcDriverOOo-2.png>
[30]: <img/jdbcDriverOOo-3.png>
[31]: <img/jdbcDriverOOo-4.png>
[32]: <img/jdbcDriverOOo-5.png>
[33]: <img/jdbcDriverOOo-6.png>
[34]: <img/jdbcDriverOOo-7.png>
[35]: <img/jdbcDriverOOo-8.png>
[36]: <img/jdbcDriverOOo-9.png>
[37]: <img/jdbcDriverOOo-10.png>
[38]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[39]: <https://bugs.documentfoundation.org/show_bug.cgi?id=132195>
[40]: <https://forum.openoffice.org/en/forum/viewtopic.php?f=13&t=103912>
[41]: <http://hsqldb.org/>
[42]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/Driver.java>
[43]: <https://github.com/hanya/MRI>
[44]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdb>
[45]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc>
[46]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx>
[47]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-HsqlDB/source/io/github/prrvchr/jdbcdriver/hsqldb>
[48]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-H2/source/io/github/prrvchr/jdbcdriver/h2>
[49]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-Derby/source/io/github/prrvchr/jdbcdriver/derby>
[50]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Driver.java>
[51]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/UnoLogger/source/io/github/prrvchr/uno/logging>
[52]: <https://www.slf4j.org/>
[53]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/logging/module-ix.html>
[54]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/options>
[55]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/admin>
[56]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/user>
[57]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/group>
[58]: <https://github.com/gotson>
[59]: <https://github.com/xerial/sqlite-jdbc/issues/786>
[60]: <https://prrvchr.github.io/SQLiteOOo>
[61]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetBase.java>
[62]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetSuper.java>
[63]: <https://bugs.documentfoundation.org/show_bug.cgi?id=156512>
[64]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/JDBCConnectionProperties.html#TypeInfoSettings>
[65]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/CustomTypeInfo.java>
[66]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu#L332>
[67]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DatabaseMetaDataBase.java#L444>
[68]: <https://github.com/artem78>
[69]: <https://github.com/prrvchr/jdbcDriverOOo/issues/4>
[70]: <https://github.com/prrvchr/HyperSQLOOo/issues/1>
