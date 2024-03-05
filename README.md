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

# version [1.2.1][5]

## Introduction:

**jdbcDriverOOo** is part of a [Suite][6] of [LibreOffice][7] ~~and/or [OpenOffice][8]~~ extensions allowing to offer you innovative services in these office suites.  

This extension is the transcription in pure Java of the [java.sql.*][9] API to the [com.sun.star.sdbc][10], [com.sun.star.sdbcx][11] and [com.sun.star.sdb][12] API of UNO.
It allows you to use the JDBC driver of your choice directly in Base.  
It embeds the drivers for the following databases:
- [HyperSQL or HsqlDB][13] version 2.7.2  
  The supported HsqlDB managed protocols are: hsql://, hsqls://, http://, https://, mem://, file:// and res://
- [SQLite JDBC Driver][14] version 3.45.1.6-SNAPSHOT
- [MariaDB Connector/J][15] version 3.3.3
- [PostgreSQL JDBC Driver][16] version 42.7.1
- [H2 Database Engine][17] version 2.2.224 (2023-09-17)
- [Apache Derby][18] version 10.15.2.0
- [SmallSQL][19] version 0.22

Being free software I encourage you:
- To duplicate its [source code][20].
- To make changes, corrections, improvements.
- To open [issue][21] if needed.

In short, to participate in the development of this extension.  
Because it is together that we can make Free Software smarter.

___

## Requirement:

jdbcDriverOOo is a JDBC driver written in Java.  
Its use requires the [installation and configuration][22] in LibreOffice of a **JRE version 11 or later**.  
I recommend [Adoptium][23] as your Java installation source.

If you are using the HsqlDB driver with **LibreOffice on Linux**, then you are subject to [bug #139538][24]. To work around the problem, please **uninstall the packages** with commands:
- `sudo apt remove libreoffice-sdbc-hsqldb` (to uninstall the libreoffice-sdbc-hsqldb package)
- `sudo apt remove libhsqldb1.8.0-java` (to uninstall the libhsqldb1.8.0-java package)

If you still want to use the Embedded HsqlDB functionality provided by LibreOffice, then install the [HyperSQLOOo][25] extension.  

**On Linux and macOS the Python packages** used by the extension, if already installed, may come from the system and therefore **may not be up to date**.  
To ensure that your Python packages are up to date it is recommended to use the **System Info** option in the extension Options accessible by:  
**Tools -> Options -> Base drivers -> JDBC driver -> View log -> System Info**  
If outdated packages appear, you can update them with the command:  
`pip install --upgrade <package-name>`

For more information see: [What has been done for version 1.1.0][72].

___

## Installation:

It seems important that the file was not renamed when it was downloaded.  
If necessary, rename it before installing it.

- ![jdbcDriverOOo logo][26] Install **[jdbcDriverOOo.oxt][27]** extension [![Version][28]][27]

Restart LibreOffice after installation.  
**Be careful, restarting LibreOffice may not be enough.**
- **On Windows** to ensure that LibreOffice restarts correctly, use Windows Task Manager to verify that no LibreOffice services are visible after LibreOffice shuts down (and kill it if so).
- **Under Linux or macOS** you can also ensure that LibreOffice restarts correctly, by launching it from a terminal with the command `soffice` and using the key combination `Ctrl + C` if after stopping LibreOffice, the terminal is not active (no command prompt).

___

## Use:

This mode of use uses an HsqlDB database.

### How to create a new database:

In LibreOffice / OpenOffice go to menu: **File -> New -> Database**

![jdbcDriverOOo screenshot 1][29]

In step: **Select database**
- select: Connect to an existing database
- choose: HsqlDB Driver
- click on button: Next

![jdbcDriverOOo screenshot 2][30]

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

![jdbcDriverOOo screenshot 3][31]

In step: **Set up user authentication**
- click on button: Test connection

![jdbcDriverOOo screenshot 4][32]

If the connection was successful, you should see this dialog window:

![jdbcDriverOOo screenshot 5][33]

Have fun...

### How to update the JDBC driver:

If you want to update an embedded HsqlDB database (single odb file), please refer to the section: [How to migrate an embedded database][34].

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

![jdbcDriverOOo screenshot 6][35]

The privileges management of the users of the underlying database is accessible in this window by the button: **Change privileges**  
If the privilege is inherited from an assigned role, the checkbox is a three-state type.

![jdbcDriverOOo screenshot 7][36]

### Managing roles (groups) in Base:

The management of the roles (groups) of the underlying database is accessible in Base via the menu: **Administration -> Group administration**

![jdbcDriverOOo screenshot 8][37]

The management of users who are members of the group of the underlying database is accessible in this window via the button: **Group users**

![jdbcDriverOOo screenshot 9][38]

The management of roles assigned to the group of the underlying database is accessible in this window via the button: **Group roles**  
This functionality is an extension of the UNO API and will only be available if the underlying LibreOffice / OpenOffice driver allows it.

![jdbcDriverOOo screenshot 10][39]

___

## Has been tested with:

* LibreOffice 7.0.4.2 - Ubuntu 20.04 - LxQt 0.14.1

* LibreOffice 6.4.4.2 - Windows 7 SP1

I encourage you in case of problem :confused:  
to create an [issue][21]  
I will try to solve it :smile:

___

## Historical:

### Introduction:

This driver was written to work around certain problems inherent in the UNO implementation of the JDBC driver built into LibreOffice / OpenOffice, namely: 

- The inability to provide the path to the Java driver archive (hsqldb.jar) when loading the JDBC driver.
- Not being able to use prepared SQL statements (PreparedStatement) see [bug #132195][40].

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

It also provides functionality that the JDBC driver implemented in LibreOffice does not provide, namely:

- The management of users, roles (groups) and privileges in Base.
- The use of the SQL Array type in the queries.
- Everything we are ready to implement.

### What has been done for version 0.0.1:

- The writing of this driver was facilitated by a [discussion with Villeroy][41], on the OpenOffice forum, which I would like to thank, because knowledge is only worth if it is shared...

- Using the new version of HsqlDB 2.5.1.

- Many other fix...

### What has been done for version 0.0.2:

- Added a dialog box allowing to update the driver (hsqldb.jar) in: Tools -> Options -> Base drivers -> HsqlDB driver

- Many other fix...

### What has been done for version 0.0.3:

- I especially want to thank fredt at [hsqldb.org][42] for:

    - His welcome for this project and his permission to use the HsqlDB logo in the extension.

    - Its involvement in the test phase which made it possible to produce this version 0.0.3.

    - The quality of its HsqlDB database.

- Now works with OpenOffice on Windows.

- An unsupported protocol now displays an accurate error.

- A non-parsable url now displays a precise error.

- Now correctly handles spaces in filenames and paths.

- Many other fix...

### What has been done for version 0.0.4:

- Rewrite of [Driver][43] in Java version 11 OpenJDK amd64 under Eclipse IDE for Java Developers version 4.23.0 with the plugins:
    - LOEclipse or LibreOffice Eclipse plugin for extension development version 4.0.1.
    - PyDev or Python IDE for Eclipse version 9.3.0.

- Writing the `Statement`, `PreparedStatement`, `CallableStatement`, `ResultSet`, `...` services of JDBC (thanks to hanya for [MRI][44] which was of great help to me...)

    - [com.sun.star.sdb.*][45]
    - [com.sun.star.sdbc.*][46]
    - [com.sun.star.sdbcx.*][47]

- Integration in jdbcDriverOOo of **H2** and **Derby** JDBC drivers in addition to **HsqlDB**. Implementation of Java Services:

    - [Driver-HsqlDB.jar][48]
    - [Driver-H2.jar][49]
    - [Driver-Derby.jar][50]

    In order to correct possible defects, or incompatibility with the UNO API, of embedded JDBC drivers.

- Renamed the **HsqlDBDriverOOo** repository and extension to **jdbcDriverOOo**.

- Support in Base for **auto-incrementing primary keys** for HsqlDB, H2 and Derby.

- Writing of [com.sun.star.sdbcx.Driver][51]. This high-level driver must allow the **management of users, roles and privileges in Base**. Its use can be disabled via the menu: **Tools -> Options -> Base drivers -> JDBC Driver**.

- Implemented a Java service provider [UnoLogger.jar][52] for the [SLF4J][53] API to be able to redirect driver logging from the underlying databases to the UNO API [com.sun.star.logging.*][54].

- Rewrite, following the MVC model, of the [Options][55] dialog accessible via the menu: **Tools -> Options -> Base drivers -> JDBC Driver**, to allow:

    - Updating and/or adding Java archives of JDBC drivers.
    - Enabling driver logging of the underlying database.

- Writing, following the MVC model, [administration windows][56] for users and roles (groups) and their associated privileges, accessible in Base via the menu: **Administration -> User administration** and/or **Administration - > Group administration**, allowing:

    - [Management of users][57] and their privileges.
    - [Management of roles][58] (groups) and their privileges.

    These new features have only been tested with the HsqlDB driver so far.

- Many other fix...

### What has been done for version 1.0.0:

- Integration of HyperSQL version 2.7.2.

### What has been done for version 1.0.1:

- Integration of [SQLite JDBC][14] version 3.42.0.0. I especially want to thank [gotson][59] for the [many improvements to the SQLite JDBC driver][60] that made it possible to use SQLite in LibreOffice/OpenOffice.

- This driver can be wrapped by another driver ([HyperSQLOOo][25] or [SQLiteOOo][61]) thanks to a connection url now modifiable.

- It is possible to display or not the system tables in: **Tools -> Options -> Base drivers -> JDBC Driver -> UNO drivers settings -> Show system tables**

- It is possible to disallow the use of updatable resultset in: **Tools -> Options -> Base drivers -> JDBC Driver -> UNO drivers settings -> Use bookmarks**

- Many corrections have been made to make the extension [SQLiteOOo][61] functional.

### What has been done for version 1.0.2:

- Integration of [MariaDB Connector/J][15] version 3.1.4.

- Many other fix...

### What has been done for version 1.0.3:

- Integration of [H2][17] version 2.2.220.

- Integration of logging in the resultset ([ResultSetBase][62] and [ResultSetSuper][63]) in order to learn more about [issue 156512][64].

- Many other fix...

### What has been done for version 1.0.4:

- Support in the creation of tables of the [TypeInfoSettings][65] parameter allowing to recover the precision for SQL types:

    - TIME
    - TIMESTAMP
    - TIME WITH TIME ZONE
    - TIMESTAMP WITH TIME ZONE

    This is only [integrated][66] for the [HsqlDB][67] driver at the moment.

### What has been done for version 1.0.5:

- The result of accessing the [XDatabaseMetaData.getDriverVersion()][68] method is now recorded in the log file.

### What has been done for version 1.0.6:

- Added the Python package `packaging` to the extension's `pythonpath`. Thanks to [artem78][69] for allowing this correction by reporting this oversight in [issue #4][70].

### What has been done for version 1.0.7:

- Now the driver throws an exception if creating a new table fails. This is to address [bug #1][71] on the [HyperSQLOOo][25] extension.

### What has been done for version 1.0.8:

- Using the latest version of the Logging API.

### What has been done for version 1.1.0:

- All Python packages necessary for the extension are now recorded in a [requirements.txt][73] file following [PEP 508][74].
- Now if you are not on Windows then the Python packages necessary for the extension can be easily installed with the command:  
  `pip install requirements.txt`
- Modification of the [Requirement][75] section.

### What has been done for version 1.1.1:

- The driver no longer uses Bookmarkable ResultSets for performance reasons in LibreOffice Base. This can be changed in the extension options.

### What has been done for version 1.1.2:

- Implementation of the UNO interface [com.sun.star.sdbc.XGeneratedResultSet][76]. This interface allows, when inserting several rows (ie: `INSERT INTO mytable (Column1, Column2) VALUES (data1, data2), (data1, data2), ...`) into a table with an auto-incremented primary key, to retrieve a ResultSet from the rows inserted into the table and therefore gives you access to the auto-generated keys in one go.
- Implementation of the UNO interface [com.sun.star.sdbcx.XAlterTable][77]. This interface allows the modification of columns in a table. With HsqlDB it is now possible in Base:
  - Assign a description to table columns.
  - To modify the type of a column if the casting (CAST) of the data contained in this column allows it, otherwise you will be asked to replace this column which results in the deletion of the data...
- All DDL commands (ie: `CREATE TABLE...`, `ALTER TABLE...`) that Base generates are now logged.
- SQLite driver updated to latest version 3.45.1.0.
- Many other fix...

### What has been done for version 1.1.3:

- SQLite driver updated to latest version [SQLite-jdbc-3.45.1.3-SNAPSHOT.jar][78]. This new driver has been implemented to support part of the JDBC 4.1 specifications and more particularly the `java.sql.Statement.getGeneratedKeys()` interface and allows the use of the [com.sun.star.sdbc.XGeneratedResultSet][75] interface.

### What has been done for version 1.1.4:

- SQLite driver updated to latest version [SQLite-jdbc-3.45.1.6-SNAPSHOT.jar][79].
- Integration of the driver [PostgreSQL pgJDBC][16] version 42.7.1 in the jdbcDriverOOo archive. This integration was carried out without using a Java service specific to PostgreSQL but only by configuring the [Drivers.xcu][80] file allowing the JDBC driver to be declared to LibreOffice.
- Opened a [bug][81] for the [MariaDB Connector/J][15] driver so that it supports `java.sql.Statement.getGeneratedKeys()` as requested by JDBC 4.1.
- Normally the next versions of jdbcDriverOOo should be able to be updated in the list of extensions installed under LibreOffice: **Tools -> Extension manager... -> Check for Updates**.
- From now on, only the HsqlDB driver has access in Base to the administration of user and group rights. This is determined by the `IgnoreDriverPrivileges` setting in the [Drivers.xcu][80] file.
- Many improvements.

### What has been done for version 1.1.5:

- You can now edit a view in SQL mode with the SQLite driver. For drivers that do not support view alteration, views are deleted and then recreated.

### What has been done for version 1.1.6:

- You can now rename tables and views in Base. All the configuration required for renaming for each embedded JDBC driver is stored only in the [Drivers.xcu][80] file.
- All JDBC drivers integrated into jdbcDriverOOo are capable of renaming tables or views and even some (ie: MariaDB and PostgreSQL) allow modifying the catalog or schema.
- Many improvements.

### What has been done for version 1.2.0:

- All drivers integrated into the extension are **now fully functional in Base** for managing tables and views.
- Smart functions are called to:
  - Move with renaming of tables, for drivers allowing it and using two SQL commands, the order of the SQL commands will be optimized (PostgreSQL).
  - Rename a view if the driver does not support it it will be deleted then recreated (SQLite).
- Use of [generic Java class][82] for managing containers used for managing [tables][83], [views][84], [columns][85], [keys][86] and [indexes][87]. The use of generic classes for [container][88] will make it possible to do without the UNO XPropertySet interface and to be able to transcribe the existing code into pure Java.
- Many improvements.

### What has been done for version 1.2.1:

- Resolution of a regression prohibiting the deletion of columns in a table.
- Updated mariadb-java-client-3.3.3.jar driver.
- Generalization of generic Java classes for all classes needing to be shared at the UNO API level (ie: sdb, sdbc and sdbcx).
- We can now rename table columns in SQLite and MariaDB.
- It is also possible to rename the columns declared as primary key in all embedded drivers.
- Many improvements.

### What has been done for version 1.2.2:

- Implementation of index management.
- Renaming a column declared as a primary key will also rename the index associated with the primary key.
- Only members of Java classes responding to the UNO API have a public visibility level, all other members have protected or private visibility.
- Solved many problems and regression.

### What remains to be done for version 1.2.2:

- Add new languages for internationalization...

- Anything welcome...

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_en>
[5]: <https://prrvchr.github.io/jdbcDriverOOo/#what-has-been-done-for-version-120>
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
[16]: <https://jdbc.postgresql.org/>
[17]: <https://www.h2database.com/html/main.html>
[18]: <https://db.apache.org/derby/>
[19]: <https://github.com/CptTZ/SmallSQL>
[20]: <https://github.com/prrvchr/jdbcDriverOOo/>
[21]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[22]: <https://wiki.documentfoundation.org/Documentation/HowTo/Install_the_correct_JRE_-_LibreOffice_on_Windows_10>
[23]: <https://adoptium.net/releases.html?variant=openjdk11>
[24]: <https://bugs.documentfoundation.org/show_bug.cgi?id=139538>
[25]: <https://prrvchr.github.io/HyperSQLOOo/>
[26]: <img/jdbcDriverOOo.svg#middle>
[27]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/jdbcDriverOOo.oxt>
[28]: <https://img.shields.io/github/downloads/prrvchr/jdbcDriverOOo/latest/total?label=v1.2.2#right>
[29]: <img/jdbcDriverOOo-1.png>
[30]: <img/jdbcDriverOOo-2.png>
[31]: <img/jdbcDriverOOo-3.png>
[32]: <img/jdbcDriverOOo-4.png>
[33]: <img/jdbcDriverOOo-5.png>
[34]: <https://prrvchr.github.io/HyperSQLOOo/#how-to-migrate-an-embedded-database>
[35]: <img/jdbcDriverOOo-6.png>
[36]: <img/jdbcDriverOOo-7.png>
[37]: <img/jdbcDriverOOo-8.png>
[38]: <img/jdbcDriverOOo-9.png>
[39]: <img/jdbcDriverOOo-10.png>
[40]: <https://bugs.documentfoundation.org/show_bug.cgi?id=132195>
[41]: <https://forum.openoffice.org/en/forum/viewtopic.php?f=13&t=103912>
[42]: <http://hsqldb.org/>
[43]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/Driver.java>
[44]: <https://github.com/hanya/MRI>
[45]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdb>
[46]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc>
[47]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx>
[48]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-HsqlDB/source/io/github/prrvchr/jdbcdriver/hsqldb>
[49]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-H2/source/io/github/prrvchr/jdbcdriver/h2>
[50]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-Derby/source/io/github/prrvchr/jdbcdriver/derby>
[51]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Driver.java>
[52]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/UnoLogger/source/io/github/prrvchr/uno/logging>
[53]: <https://www.slf4j.org/>
[54]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/logging/module-ix.html>
[55]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/options>
[56]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/admin>
[57]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/user>
[58]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/group>
[59]: <https://github.com/gotson>
[60]: <https://github.com/xerial/sqlite-jdbc/issues/786>
[61]: <https://prrvchr.github.io/SQLiteOOo>
[62]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetBase.java>
[63]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetSuper.java>
[64]: <https://bugs.documentfoundation.org/show_bug.cgi?id=156512>
[65]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/JDBCConnectionProperties.html#TypeInfoSettings>
[66]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/CustomTypeInfo.java>
[67]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu#L332>
[68]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DatabaseMetaDataBase.java#L444>
[69]: <https://github.com/artem78>
[70]: <https://github.com/prrvchr/jdbcDriverOOo/issues/4>
[71]: <https://github.com/prrvchr/HyperSQLOOo/issues/1>
[72]: <https://prrvchr.github.io/jdbcDriverOOo/#what-has-been-done-for-version-110>
[73]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/requirements.txt>
[74]: <https://peps.python.org/pep-0508/>
[75]: <https://prrvchr.github.io/jdbcDriverOOo/#requirement>
[76]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/XGeneratedResultSet.html>
[77]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XAlterTable.html>
[78]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.3-SNAPSHOT.jar>
[79]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.6-SNAPSHOT.jar>
[80]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu>
[81]: <https://jira.mariadb.org/browse/CONJ-1160>
[82]: <https://en.wikibooks.org/wiki/Java_Programming/Generics>
[83]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/TableContainerSuper.java>
[84]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ViewContainer.java>
[85]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ColumnContainerBase.java>
[86]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/KeyContainer.java>
[87]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/IndexContainer.java>
[88]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Container.java>
