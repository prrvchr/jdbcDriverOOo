# Documentation

**Ce [document][2] en français.**

**The use of this software subjects you to our [Terms Of Use][3]**

# version [1.0.3][4]

## Introduction:

**jdbcDriverOOo** is part of a [Suite][5] of [LibreOffice][6] and/or [OpenOffice][7] extensions allowing to offer you innovative services in these office suites.  

This extension is the transcription in pure Java of the [java.sql.*][8] API to the [com.sun.star.sdbc][9], [com.sun.star.sdbcx][10] and [com.sun.star.sdb][11] API of UNO.
It allows you to use the JDBC driver of your choice directly in Base.  
It embeds the drivers for the following databases:
- [HyperSQL or HsqlDB][12] version 2.72
    The supported HsqlDB managed protocols are: hsql://, hsqls://, http://, https://, mem://, file:// and res://
- [SQLite JDBC Driver][13] version 3.42.0.0
- [MariaDB Connector/J][14] version 3.1.4
- [H2 Database Engine][15] version 2.2.220 (2023-07-04)
- [Apache Derby][16] version 10.15.2.0
- [SmallSQL][17] version 0.22

Being free software I encourage you:
- To duplicate its [source code][18].
- To make changes, corrections, improvements.
- To open [issue][19] if needed.

In short, to participate in the development of this extension.  
Because it is together that we can make Free Software smarter.

___
## Requirement:

jdbcDriverOOo is a JDBC driver written in Java.  
Its use requires the [installation and configuration][20] in LibreOffice / OpenOffice of a **JRE version 11 or later**.  
I recommend [Adoptium][21] as your Java installation source.

If you are using the HsqlDB driver with **LibreOffice on Linux**, then you are subject to [bug 139538][22]. To work around the problem, please **uninstall the packages** with commands:
- `sudo apt remove libreoffice-sdbc-hsqldb` (to uninstall the libreoffice-sdbc-hsqldb package)
- `sudo apt remove libhsqldb1.8.0-java` (to uninstall the libhsqldb1.8.0-java package)

If you still want to use the Embedded HsqlDB functionality provided by LibreOffice, then install the [HyperSQLOOo][23] extension.  

___
## Installation:

It seems important that the file was not renamed when it was downloaded.
If necessary, rename it before installing it.

- Install ![jdbcDriverOOo logo][1] **[jdbcDriverOOo.oxt][24]** extension version 1.0.3.

Restart LibreOffice / OpenOffice after installation.

___
## Use:

This mode of use uses an HsqlDB database.

### How to create a new database:

In LibreOffice / OpenOffice go to menu: **File -> New -> Database**

![jdbcDriverOOo screenshot 1][25]

In step: **Select database**
- select: Connect to an existing database
- choose: HsqlDB Driver
- click on button: Next

![jdbcDriverOOo screenshot 2][26]

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

![jdbcDriverOOo screenshot 3][27]

In step: **Set up user authentication**
- click on button: Test connection

![jdbcDriverOOo screenshot 4][28]

If the connection was successful, you should see this dialog window:

![jdbcDriverOOo screenshot 5][29]

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

![jdbcDriverOOo screenshot 6][30]

The privileges management of the users of the underlying database is accessible in this window by the button: **Change privileges**  
If the privilege is inherited from an assigned role, the checkbox is a three-state type.

![jdbcDriverOOo screenshot 7][31]

### Managing roles (groups) in Base:

The management of the roles (groups) of the underlying database is accessible in Base via the menu: **Administration -> Group administration**

![jdbcDriverOOo screenshot 8][32]

The management of users who are members of the group of the underlying database is accessible in this window via the button: **Group users**

![jdbcDriverOOo screenshot 9][33]

The management of roles assigned to the group of the underlying database is accessible in this window via the button: **Group roles**  
This functionality is an extension of the UNO API and will only be available if the underlying LibreOffice / OpenOffice driver allows it.

![jdbcDriverOOo screenshot 10][34]

___
## Has been tested with:

* LibreOffice 7.0.4.2 - Ubuntu 20.04 - LxQt 0.14.1

* LibreOffice 6.4.4.2 - Windows 7 SP1

* Apache OpenOffice 4.1.13 - Lubuntu 22.04

I encourage you in case of problem :confused:  
to create an [issue][35]  
I will try to solve it :smile:

___
## Historical:

### Introduction:

This driver was written to work around certain problems inherent in the UNO implementation of the JDBC driver built into LibreOffice / OpenOffice, namely: 

- The inability to provide the path to the Java driver archive (hsqldb.jar) when loading the JDBC driver.
- Not being able to use prepared SQL statements (PreparedStatement) see [bug 132195][36].

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

- The writing of this driver was facilitated by a [discussion with Villeroy][37], on the OpenOffice forum, which I would like to thank, because knowledge is only worth if it is shared...

- Using the new version of HsqlDB 2.5.1.

- Many other fix...

### What has been done for version 0.0.2:

- Added a dialog box allowing to update the driver (hsqldb.jar) in: Tools -> Options -> Base drivers -> HsqlDB driver

- Many other fix...

### What has been done for version 0.0.3:

- I especially want to thank fredt at [hsqldb.org][38] for:

    - His welcome for this project and his permission to use the HsqlDB logo in the extension.

    - Its involvement in the test phase which made it possible to produce this version 0.0.3.

    - The quality of its HsqlDB database.

- Now works with OpenOffice on Windows.

- An unsupported protocol now displays an accurate error.

- A non-parsable url now displays a precise error.

- Now correctly handles spaces in filenames and paths.

- Many other fix...

### What has been done for version 0.0.4:

- Rewrite of [Driver][39] in Java version 11 OpenJDK amd64 under Eclipse IDE for Java Developers version 4.23.0 with the plugins:
    - LOEclipse or LibreOffice Eclipse plugin for extension development version 4.0.1.
    - PyDev or Python IDE for Eclipse version 9.3.0.

- Writing the `Statement`, `PreparedStatement`, `CallableStatement`, `ResultSet`, `...` services of JDBC (thanks to hanya for [MRI][40] which was of great help to me...)

    - [com.sun.star.sdb.*][41]
    - [com.sun.star.sdbc.*][42]
    - [com.sun.star.sdbcx.*][43]

- Integration in jdbcDriverOOo of **H2** and **Derby** JDBC drivers in addition to **HsqlDB**. Implementation of Java Services:

    - [Driver-HsqlDB.jar][44]
    - [Driver-H2.jar][45]
    - [Driver-Derby.jar][46]

    In order to correct possible defects, or incompatibility with the UNO API, of embedded JDBC drivers.

- Renamed the **HsqlDBDriverOOo** repository and extension to **jdbcDriverOOo**.

- Support in Base for **auto-incrementing primary keys** for HsqlDB, H2 and Derby.

- Writing of [com.sun.star.sdbcx.Driver][47]. This high-level driver must allow the **management of users, roles and privileges in Base**. Its use can be disabled via the menu: **Tools -> Options -> Base drivers -> JDBC Driver**.

- Implemented a Java service provider [UnoLogger.jar][48] for the [SLF4J][49] API to be able to redirect driver logging from the underlying databases to the UNO API [com.sun.star.logging.*][50].

- Rewrite, following the MVC model, of the [Options][51] dialog accessible via the menu: **Tools -> Options -> Base drivers -> JDBC Driver**, to allow:

    - Updating and/or adding Java archives of JDBC drivers.
    - Enabling driver logging of the underlying database.

- Writing, following the MVC model, [administration windows][52] for users and roles (groups) and their associated privileges, accessible in Base via the menu: **Administration -> User administration** and/or **Administration - > Group administration**, allowing:

    - [Management of users][53] and their privileges.
    - [Management of roles][54] (groups) and their privileges.

    These new features have only been tested with the HsqlDB driver so far.

- Many other fix...

### What has been done for version 1.0.0:

- Integration of HyperSQL version 2.7.2.

### What has been done for version 1.0.1:

- Integration of [SQLite JDBC][13] version 3.42.0.0. I especially want to thank [gotson][55] for the [many improvements to the SQLite JDBC driver][56] that made it possible to use SQLite in LibreOffice/OpenOffice.

- This driver can be wrapped by another driver ([HyperSQLOOo][23] or [SQLiteOOo][57]) thanks to a connection url now modifiable.

- It is possible to display or not the system tables in: **Tools -> Options -> Base drivers -> JDBC Driver -> UNO drivers settings -> Show system tables**

- It is possible to disallow the use of updatable resultset in: **Tools -> Options -> Base drivers -> JDBC Driver -> UNO drivers settings -> Use bookmarks**

- Many corrections have been made to make the extension [SQLiteOOo][57] functional.

### What has been done for version 1.0.2:

- Integration of [MariaDB Connector/J][14] version 3.1.4.

- Many other fix...

### What has been done for version 1.0.3:

- Integration of [H2][15] version 2.2.220.

- Integration of logging in the resultset ([ResultSetBase][58] and [ResultSetSuper][59]) in order to learn more about [issue 156512][60].

- Many other fix...

### What remains to be done for version 1.0.3:

- Add new languages for internationalization...

- Anything welcome...

[1]: <img/jdbcDriverOOo.svg>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_en>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/#what-has-been-done-for-version-101>
[5]: <https://prrvchr.github.io/>
[6]: <https://www.libreoffice.org/download/download-libreoffice/>
[7]: <https://www.openoffice.org/download/index.html>
[8]: <https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/package-summary.html>
[9]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/module-ix.html>
[10]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/module-ix.html>
[11]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdb/module-ix.html>
[12]: <http://hsqldb.org/>
[13]: <https://github.com/xerial/sqlite-jdbc>
[14]: <https://mariadb.com/downloads/connectors/connectors-data-access/java8-connector/>
[15]: <https://www.h2database.com/html/main.html>
[16]: <https://db.apache.org/derby/>
[17]: <https://github.com/CptTZ/SmallSQL>
[18]: <https://github.com/prrvchr/jdbcDriverOOo/>
[19]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[20]: <https://wiki.documentfoundation.org/Documentation/HowTo/Install_the_correct_JRE_-_LibreOffice_on_Windows_10>
[21]: <https://adoptium.net/releases.html?variant=openjdk11>
[22]: <https://bugs.documentfoundation.org/show_bug.cgi?id=139538>
[23]: <https://prrvchr.github.io/HyperSQLOOo/>
[24]: <https://github.com/prrvchr/jdbcDriverOOo/raw/master/jdbcDriverOOo.oxt>
[25]: <img/jdbcDriverOOo-1.png>
[26]: <img/jdbcDriverOOo-2.png>
[27]: <img/jdbcDriverOOo-3.png>
[28]: <img/jdbcDriverOOo-4.png>
[29]: <img/jdbcDriverOOo-5.png>
[30]: <img/jdbcDriverOOo-6.png>
[31]: <img/jdbcDriverOOo-7.png>
[32]: <img/jdbcDriverOOo-8.png>
[33]: <img/jdbcDriverOOo-9.png>
[34]: <img/jdbcDriverOOo-10.png>
[35]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[36]: <https://bugs.documentfoundation.org/show_bug.cgi?id=132195>
[37]: <https://forum.openoffice.org/en/forum/viewtopic.php?f=13&t=103912>
[38]: <http://hsqldb.org/>
[39]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/Driver.java>
[40]: <https://github.com/hanya/MRI>
[41]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdb>
[42]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc>
[43]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx>
[44]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-HsqlDB/source/io/github/prrvchr/jdbcdriver/hsqldb>
[45]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-H2/source/io/github/prrvchr/jdbcdriver/h2>
[46]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-Derby/source/io/github/prrvchr/jdbcdriver/derby>
[47]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Driver.java>
[48]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/UnoLogger/source/io/github/prrvchr/uno/logging>
[49]: <https://www.slf4j.org/>
[50]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/logging/module-ix.html>
[51]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/options>
[52]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/admin>
[53]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/user>
[54]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/group>
[55]: <https://github.com/gotson>
[56]: <https://github.com/xerial/sqlite-jdbc/issues/786>
[57]: <https://prrvchr.github.io/SQLiteOOo>
[58]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetBase.java>
[59]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetSuper.java>
[60]: <https://bugs.documentfoundation.org/show_bug.cgi?id=156512>
