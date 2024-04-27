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

# version [1.3.2][5]

## Introduction:

**jdbcDriverOOo** is part of a [Suite][6] of [LibreOffice][7] ~~and/or [OpenOffice][8]~~ extensions allowing to offer you innovative services in these office suites.  

This extension is the transcription in pure Java of the [java.sql.*][9] API to the [com.sun.star.sdbc][10], [com.sun.star.sdbcx][11] and [com.sun.star.sdb][12] API of UNO.
It allows you to use the JDBC driver of your choice directly in Base.  
It embeds the drivers for the following databases:
- [HyperSQL or HsqlDB][13] version 2.7.2
- [SQLite via xerial sqlite-jdbc][14] version 3.45.1.6-SNAPSHOT
- [MariaDB via Connector/J][15] version 3.3.3
- [PostgreSQL via pgJDBC][16] version 42.7.1
- [H2 Database Engine][17] version 2.2.224 (2023-09-17)
- [Apache Derby][18] version 10.15.2.0
- [Firebird via Jaybird][19] version 5.0.4

Thanks to drivers providing an integrated database engine such as: HsqlDB, H2, SQLite or Derby, it is possible in Base to very easily create and manage databases, as easily as creating Writer documents.  
You will find the information needed to create a database with these drivers in the section: [Connection URL][20]

Being free software I encourage you:
- To duplicate its [source code][21].
- To make changes, corrections, improvements.
- To open [issue][22] if needed.

In short, to participate in the development of this extension.  
Because it is together that we can make Free Software smarter.

___

## Requirement:

jdbcDriverOOo is a JDBC driver written in Java.  
Its use requires the [installation and configuration][23] in LibreOffice of a **JRE version 11 or later**.  
I recommend [Adoptium][24] as your Java installation source.

If you are using the HsqlDB driver with **LibreOffice on Linux**, then you are subject to [bug #139538][25]. To work around the problem, please **uninstall the packages** with commands:
- `sudo apt remove libreoffice-sdbc-hsqldb` (to uninstall the libreoffice-sdbc-hsqldb package)
- `sudo apt remove libhsqldb1.8.0-java` (to uninstall the libhsqldb1.8.0-java package)

If you still want to use the Embedded HsqlDB functionality provided by LibreOffice, then install the [HyperSQLOOo][26] extension.  

**On Linux and macOS the Python packages** used by the extension, if already installed, may come from the system and therefore **may not be up to date**.  
To ensure that your Python packages are up to date it is recommended to use the **System Info** option in the extension Options accessible by:  
**Tools -> Options -> Base drivers -> JDBC driver -> View log -> System Info**  
If outdated packages appear, you can update them with the command:  
`pip install --upgrade <package-name>`

For more information see: [What has been done for version 1.1.0][27].

___

## Installation:

It seems important that the file was not renamed when it was downloaded.  
If necessary, rename it before installing it.

- ![jdbcDriverOOo logo][28] Install **[jdbcDriverOOo.oxt][29]** extension [![Version][30]][29]

Restart LibreOffice after installation.  
**Be careful, restarting LibreOffice may not be enough.**
- **On Windows** to ensure that LibreOffice restarts correctly, use Windows Task Manager to verify that no LibreOffice services are visible after LibreOffice shuts down (and kill it if so).
- **Under Linux or macOS** you can also ensure that LibreOffice restarts correctly, by launching it from a terminal with the command `soffice` and using the key combination `Ctrl + C` if after stopping LibreOffice, the terminal is not active (no command prompt).

___

## Use:

This explains how to use an HsqlDB database.  
The protocols supported by HsqlDB are: hsql://, hsqls://, http://, https://, mem://, file:// and res://.  
This mode of use explains how to connect with the **file://** and **hsql://** protocols.

### How to create a new database:

In LibreOffice / OpenOffice go to menu: **File -> New -> Database**

![jdbcDriverOOo screenshot 1][31]

In step: **Select database**
- select: Connect to an existing database
- choose: **HsqlDB Driver**
- click on button: Next

![jdbcDriverOOo screenshot 2][32]

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

![jdbcDriverOOo screenshot 3][33]

In step: **Set up user authentication**
- click on button: Test connection

![jdbcDriverOOo screenshot 4][34]

If the connection was successful, you should see this dialog window:

![jdbcDriverOOo screenshot 5][35]

Have fun...

### How to update the JDBC driver:

If you want to update an embedded HsqlDB database (single odb file), please refer to the section: [How to migrate an embedded database][36].

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

![jdbcDriverOOo screenshot 6][37]

The privileges management of the users of the underlying database is accessible in this window by the button: **Change privileges**  
If the privilege is inherited from an assigned role, the checkbox is a three-state type.

![jdbcDriverOOo screenshot 7][38]

### Managing roles (groups) in Base:

The management of the roles (groups) of the underlying database is accessible in Base via the menu: **Administration -> Group administration**

![jdbcDriverOOo screenshot 8][39]

The management of users who are members of the group of the underlying database is accessible in this window via the button: **Group users**

![jdbcDriverOOo screenshot 9][40]

The management of roles assigned to the group of the underlying database is accessible in this window via the button: **Group roles**  
This functionality is an extension of the UNO API and will only be available if the underlying LibreOffice / OpenOffice driver allows it.

![jdbcDriverOOo screenshot 10][41]

___

## Connection URL:

Certain databases such as hsqlDB, H2, SQLite or Derby allow the creation of the database during connection if this database does not yet exist.
This feature makes it as easy to create databases as Writer documents. Generally it is enough to add the option expected by the driver to the connection URL.
This connection URL may be different depending on the operating system of your computer (Windows, Linux or MacOS).  
To create a database, in LibreOffice go to the menu: **File -> New -> Database -> Connect to an existing database**, then according to your choice:
- **Pilote HsqlDB**:
  - Linux: `file:///home/prrvchr/testdb/hsqldb/db;hsqldb.default_table_type=cached;create=true`
  - Windows: `C:\Utilisateurs\prrvc\testdb\hsqldb\db;hsqldb.default_table_type=cached;create=true`
- **Pilote H2**:
  - Linux: `file:///home/prrvchr/testdb/h2/db`
  - Windows: `C:\Utilisateurs\prrvc\testdb\h2\db`
- **Pilote SQLite**:
  - Linux: `file:///home/prrvchr/testdb/sqlite/test.db`
  - Windows: `C:/Utilisateurs/prrvc/testdb/sqlite/test.db`
- **Pilote Derby**:
  - Linux: `/home/prrvchr/testdb/derby;create=true`
  - Windows: `C:\Utilisateurs\prrvc\testdb\derby;create=true`

___

## Has been tested with:

* LibreOffice 7.0.4.2 - Ubuntu 20.04 - LxQt 0.14.1

* LibreOffice 6.4.4.2 - Windows 7 SP1

I encourage you in case of problem :confused:  
to create an [issue][22]  
I will try to solve it :smile:

___

## Historical:

### Introduction:

This driver was written to work around certain problems inherent in the UNO implementation of the JDBC driver built into LibreOffice / OpenOffice, namely: 

- The inability to provide the path to the Java driver archive (hsqldb.jar) when loading the JDBC driver.
- Not being able to use prepared SQL statements (PreparedStatement) see [bug #132195][42].

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

- The writing of this driver was facilitated by a [discussion with Villeroy][43], on the OpenOffice forum, which I would like to thank, because knowledge is only worth if it is shared...

- Using the new version of HsqlDB 2.5.1.

- Many other fix...

### What has been done for version 0.0.2:

- Added a dialog box allowing to update the driver (hsqldb.jar) in: Tools -> Options -> Base drivers -> HsqlDB driver

- Many other fix...

### What has been done for version 0.0.3:

- I especially want to thank fredt at [hsqldb.org][44] for:

    - His welcome for this project and his permission to use the HsqlDB logo in the extension.

    - Its involvement in the test phase which made it possible to produce this version 0.0.3.

    - The quality of its HsqlDB database.

- Now works with OpenOffice on Windows.

- An unsupported protocol now displays an accurate error.

- A non-parsable url now displays a precise error.

- Now correctly handles spaces in filenames and paths.

- Many other fix...

### What has been done for version 0.0.4:

- Rewrite of [Driver][45] in Java version 11 OpenJDK amd64 under Eclipse IDE for Java Developers version 4.23.0 with the plugins:
    - LOEclipse or LibreOffice Eclipse plugin for extension development version 4.0.1.
    - PyDev or Python IDE for Eclipse version 9.3.0.

- Writing the `Statement`, `PreparedStatement`, `CallableStatement`, `ResultSet`, `...` services of JDBC (thanks to hanya for [MRI][46] which was of great help to me...)

    - [com.sun.star.sdb.*][47]
    - [com.sun.star.sdbc.*][48]
    - [com.sun.star.sdbcx.*][49]

- Integration in jdbcDriverOOo of **H2** and **Derby** JDBC drivers in addition to **HsqlDB**. Implementation of Java Services:

    - [Driver-HsqlDB.jar][50]
    - [Driver-H2.jar][51]
    - [Driver-Derby.jar][52]

    In order to correct possible defects, or incompatibility with the UNO API, of embedded JDBC drivers.

- Renamed the **HsqlDBDriverOOo** repository and extension to **jdbcDriverOOo**.

- Support in Base for **auto-incrementing primary keys** for HsqlDB, H2 and Derby.

- Writing of [com.sun.star.sdbcx.Driver][53]. This high-level driver must allow the **management of users, roles and privileges in Base**. Its use can be disabled via the menu: **Tools -> Options -> Base drivers -> JDBC Driver**.

- Implemented a Java service provider [UnoLogger.jar][54] for the [SLF4J][55] API to be able to redirect driver logging from the underlying databases to the UNO API [com.sun.star.logging.*][56].

- Rewrite, following the MVC model, of the [Options][57] dialog accessible via the menu: **Tools -> Options -> Base drivers -> JDBC Driver**, to allow:

    - Updating and/or adding Java archives of JDBC drivers.
    - Enabling driver logging of the underlying database.

- Writing, following the MVC model, [administration windows][58] for users and roles (groups) and their associated privileges, accessible in Base via the menu: **Administration -> User administration** and/or **Administration - > Group administration**, allowing:

    - [Management of users][59] and their privileges.
    - [Management of roles][60] (groups) and their privileges.

    These new features have only been tested with the HsqlDB driver so far.

- Many other fix...

### What has been done for version 1.0.0:

- Integration of HyperSQL version 2.7.2.

### What has been done for version 1.0.1:

- Integration of [SQLite JDBC][14] version 3.42.0.0. I especially want to thank [gotson][61] for the [many improvements to the SQLite JDBC driver][62] that made it possible to use SQLite in LibreOffice/OpenOffice.

- This driver can be wrapped by another driver ([HyperSQLOOo][26] or [SQLiteOOo][63]) thanks to a connection url now modifiable.

- It is possible to display or not the system tables in: **Tools -> Options -> Base drivers -> JDBC Driver -> UNO drivers settings -> Show system tables**

- It is possible to disallow the use of updatable resultset in: **Tools -> Options -> Base drivers -> JDBC Driver -> UNO drivers settings -> Use bookmarks**

- Many corrections have been made to make the extension [SQLiteOOo][63] functional.

### What has been done for version 1.0.2:

- Integration of [MariaDB Connector/J][15] version 3.1.4.

- Many other fix...

### What has been done for version 1.0.3:

- Integration of [H2][17] version 2.2.220.

- Integration of logging in the resultset ([ResultSetBase][64] and [ResultSetSuper][65]) in order to learn more about [issue 156512][66].

- Many other fix...

### What has been done for version 1.0.4:

- Support in the creation of tables of the [TypeInfoSettings][67] parameter allowing to recover the precision for SQL types:

    - TIME
    - TIMESTAMP
    - TIME WITH TIME ZONE
    - TIMESTAMP WITH TIME ZONE

    This is only [integrated][68] for the [HsqlDB][69] driver at the moment.

### What has been done for version 1.0.5:

- The result of accessing the [XDatabaseMetaData.getDriverVersion()][70] method is now recorded in the log file.

### What has been done for version 1.0.6:

- Added the Python package `packaging` to the extension's `pythonpath`. Thanks to [artem78][71] for allowing this correction by reporting this oversight in [issue #4][72].

### What has been done for version 1.0.7:

- Now the driver throws an exception if creating a new table fails. This is to address [bug #1][73] on the [HyperSQLOOo][26] extension.

### What has been done for version 1.0.8:

- Using the latest version of the Logging API.

### What has been done for version 1.1.0:

- All Python packages necessary for the extension are now recorded in a [requirements.txt][74] file following [PEP 508][75].
- Now if you are not on Windows then the Python packages necessary for the extension can be easily installed with the command:  
  `pip install requirements.txt`
- Modification of the [Requirement][76] section.

### What has been done for version 1.1.1:

- The driver no longer uses Bookmarkable ResultSets for performance reasons in LibreOffice Base. This can be changed in the extension options.

### What has been done for version 1.1.2:

- Implementation of the UNO interface [com.sun.star.sdbc.XGeneratedResultSet][77]. This interface allows, when inserting several rows (ie: `INSERT INTO mytable (Column1, Column2) VALUES (data1, data2), (data1, data2), ...`) into a table with an auto-incremented primary key, to retrieve a ResultSet from the rows inserted into the table and therefore gives you access to the auto-generated keys in one go.
- Implementation of the UNO interface [com.sun.star.sdbcx.XAlterTable][78]. This interface allows the modification of columns in a table. With HsqlDB it is now possible in Base:
  - Assign a description to table columns.
  - To modify the type of a column if the casting (CAST) of the data contained in this column allows it, otherwise you will be asked to replace this column which results in the deletion of the data...
- All DDL commands (ie: `CREATE TABLE...`, `ALTER TABLE...`) that jdbcDriverOOo generates are now logged.
- SQLite driver updated to latest version 3.45.1.0.
- Many other fix...

### What has been done for version 1.1.3:

- SQLite driver updated to latest version [SQLite-jdbc-3.45.1.3-SNAPSHOT.jar][79]. This new driver has been implemented to support part of the JDBC 4.1 specifications and more particularly the `java.sql.Statement.getGeneratedKeys()` interface and allows the use of the [com.sun.star.sdbc.XGeneratedResultSet][77] interface.

### What has been done for version 1.1.4:

- SQLite driver updated to latest version [SQLite-jdbc-3.45.1.6-SNAPSHOT.jar][80].
- Integration of the driver [PostgreSQL pgJDBC][16] version 42.7.1 in the jdbcDriverOOo archive. This integration was carried out without using a Java service specific to PostgreSQL but only by configuring the [Drivers.xcu][81] file allowing the JDBC driver to be declared to LibreOffice.
- Opened a [bug][82] for the [MariaDB Connector/J][15] driver so that it supports `java.sql.Statement.getGeneratedKeys()` as requested by JDBC 4.1.
- Normally the next versions of jdbcDriverOOo should be able to be updated in the list of extensions installed under LibreOffice: **Tools -> Extension manager... -> Check for Updates**.
- From now on, only the HsqlDB driver has access in Base to the administration of user and group rights. This is determined by the `IgnoreDriverPrivileges` setting in the [Drivers.xcu][81] file.
- Many improvements.

### What has been done for version 1.1.5:

- You can now edit a view in SQL mode with the SQLite driver. For drivers that do not support view alteration, views are deleted and then recreated.

### What has been done for version 1.1.6:

- You can now rename tables and views in Base. All the configuration required for renaming for each embedded JDBC driver is stored only in the [Drivers.xcu][81] file.
- All JDBC drivers integrated into jdbcDriverOOo are capable of renaming tables or views and even some (ie: MariaDB and PostgreSQL) allow modifying the catalog or schema.
- Many improvements.

### What has been done for version 1.2.0:

- All drivers integrated into the extension are **now fully functional in Base** for managing tables and views.
- Smart functions are called to:
  - Move with renaming of tables, for drivers allowing it and using two SQL commands, the order of the SQL commands will be optimized (PostgreSQL).
  - Rename a view if the driver does not support it it will be deleted then recreated (SQLite).
- Use of [generic Java class][83] for managing containers used for managing [tables][84], [views][85], [columns][86], [keys][87] and [indexes][88]. The use of generic classes for [container][89] will make it possible to do without the UNO XPropertySet interface and to be able to transcribe the existing code into pure Java.
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

### What has been done for version 1.2.3:

- Renaming a column declared as an index will also rename the associated column index.

### What has been done for version 1.2.4:

- Removed SmallSQL.
- Integration of Jaybird 5.0.4 the JDBC driver for Firebird.
- You can now delete a primary key with PostgreSQL.
- Adding or removing a primary key generates an error if the underlying driver does not support it (SQLite).
- When [creating a table][90] with a primary key, if the underlying driver supports it, the creation of the primary key can be done by a separate DDL command. This allows Jaybird to work around [bug #791][91] by creating a named primary key and allows to manage special cases like MariaDB or SQLite for their management of auto-increments.
- If the underlying driver allows it, when [altering columns][92] of a table you can now:
  - Declare it as auto-increment (Identity) without it necessarily being the primary key.
  - Add or remove the Identity constraint (auto-increment).
  - Add comments.
- Many improvements.

### What has been done for version 1.3.0:

- Integration of foreign key management into Base (**Tools -> Relationships...**).
  - When you rename a table, it will also rename that table's referencing in any foreign keys pointing to that table.
  - When you rename a column, it will also rename that column's referencing in any foreign keys pointing to that column.
  - These foreign key updates take into account lazy loading of table and key containers and will only be performed if Base has already accessed the data involved.
  - An issue persists when creating foreign keys between tables that do not have the same catalog and/or schema, see [bug #160375][93]. This issue appears to be related to Base, I hope it gets resolved soon.
- Better exception handling with the ability to know the status, SQL code and message of the exception that was generated by the underlying driver.
- Many fixes and improvements.

Normally, I managed to cover the entire scope of the UNO API ([com.sun.star.sdbc][10], [sdbcx][11] and [sdb][12]), which took quite a while, but I didn't initially think I would get there.  

### What has been done for version 1.3.1:

- Fixed the implementation of the [XRowLocate][94] interface responsible for managing Bookmarks in ResultSet. This new implementation works with all drivers except SQLite which does not support updatable ResultSet. The presence of this interface in ResultSet allows Base to edit tables even in absence of primary key. With certain drivers (HsqlDB, H2 and Derby) refreshing during entry will not be automatic and must be done manually. The use of bookmarks can be disabled in the extension's options.
- Setting up [mock ResultSet][95] (java.sql.ResultSet) to produce ResultSets from connection data provided by the driver, more precisely from the [Drivers.xcu][81] file. The use of these simulated resultsets makes it possible to provide Base with resultsets conforming to what it expects even if the underlying driver is not capable of producing them. They are used to patch the results obtained from the `getTypeInfo()`, `getTableTypes` and `getTablePrivileges()` methods of the java.sql.DatabaseMetaData interface using respectively the `TypeInfoSettings`, `TableTypesSettings` and `TablePrivilegesSettings` properties of the [Drivers.xcu][81] file.
- Writing a [specific container][96] to manage the users of a role or the roles of a role. This container is just a pointer to the elements of the user and/or role containers in the database. When deleting a user or role this container will be updated if necessary.
- Rewrote the **User administration** and **Group administration** windows accessible in Base **Administration** menu. Now, if the `TablePrivilegesSettings` property is provided by the underlying driver, only the privileges declared in this property will be displayed. This allows for easier use. An [improvement request #160516][97] was made to integrate this functionality into the Base code.
- Integration of all drivers embedded in the extension (excluding SQLite) in the management of users, roles and privileges on tables and views. I suppose that many malfunctions remain to be corrected, please let me know, detecting malfunctions takes me more time than correcting them....
- Many corrections and improvements...

### What has been done for version 1.3.2:

The UNO SDBCX API can now be used for creating databases, as is the case for the latest versions of extensions using jdbcDriverOOo. It is possible to create tables, using the UNO API, with the following characteristics:
- Declaration of columns of types TIMESTAMP WITH TIME ZONE, TIMESTAMP, TIME WITH TIME ZONE, TIME with precision management (ie: from 0 to 9).
- Declaration of [temporal system versioned tables][98]. These types of tables are used in the same extensions to facilitate data replication.
- Declaration of [text tables][99]. These tables allow you to use data from files in csv format.
- Declaration of primary keys, foreign keys, indexes, users, roles and associated privileges.

Using the UNO API to create databases will allow you to use code that is independent of the underlying database.

Clients using the jdbcDriverOOo driver can access features of the underlying JDBC driver through the [XDriver.getPropertyInfo()][100] method in order to access the necessary parameter when creating tables and display privileges correctly. These parameters being accessible directly by the driver can be obtained before any connection and therefore allows the creation of the database before the first connection.

### What remains to be done for version 1.3.2:

- Add new languages for internationalization...

- Anything welcome...

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_en>
[5]: <https://prrvchr.github.io/jdbcDriverOOo/#what-has-been-done-for-version-132>
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
[19]: <https://firebirdsql.org/en/jdbc-driver/>
[20]: <https://prrvchr.github.io/jdbcDriverOOo/#connection-url>
[21]: <https://github.com/prrvchr/jdbcDriverOOo/>
[22]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[23]: <https://wiki.documentfoundation.org/Documentation/HowTo/Install_the_correct_JRE_-_LibreOffice_on_Windows_10>
[24]: <https://adoptium.net/releases.html?variant=openjdk11>
[25]: <https://bugs.documentfoundation.org/show_bug.cgi?id=139538>
[26]: <https://prrvchr.github.io/HyperSQLOOo/>
[27]: <https://prrvchr.github.io/jdbcDriverOOo/#what-has-been-done-for-version-110>
[28]: <img/jdbcDriverOOo.svg#middle>
[29]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/jdbcDriverOOo.oxt>
[30]: <https://img.shields.io/github/downloads/prrvchr/jdbcDriverOOo/latest/total?label=v1.3.2#right>
[31]: <img/jdbcDriverOOo-1.png>
[32]: <img/jdbcDriverOOo-2.png>
[33]: <img/jdbcDriverOOo-3.png>
[34]: <img/jdbcDriverOOo-4.png>
[35]: <img/jdbcDriverOOo-5.png>
[36]: <https://prrvchr.github.io/HyperSQLOOo/#how-to-migrate-an-embedded-database>
[37]: <img/jdbcDriverOOo-6.png>
[38]: <img/jdbcDriverOOo-7.png>
[39]: <img/jdbcDriverOOo-8.png>
[40]: <img/jdbcDriverOOo-9.png>
[41]: <img/jdbcDriverOOo-10.png>
[42]: <https://bugs.documentfoundation.org/show_bug.cgi?id=132195>
[43]: <https://forum.openoffice.org/en/forum/viewtopic.php?f=13&t=103912>
[44]: <http://hsqldb.org/>
[45]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/Driver.java>
[46]: <https://github.com/hanya/MRI>
[47]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdb>
[48]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc>
[49]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx>
[50]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-HsqlDB/source/io/github/prrvchr/jdbcdriver/hsqldb>
[51]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-H2/source/io/github/prrvchr/jdbcdriver/h2>
[52]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-Derby/source/io/github/prrvchr/jdbcdriver/derby>
[53]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Driver.java>
[54]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/UnoLogger/source/io/github/prrvchr/uno/logging>
[55]: <https://www.slf4j.org/>
[56]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/logging/module-ix.html>
[57]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/options>
[58]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/admin>
[59]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/user>
[60]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/group>
[61]: <https://github.com/gotson>
[62]: <https://github.com/xerial/sqlite-jdbc/issues/786>
[63]: <https://prrvchr.github.io/SQLiteOOo>
[64]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetBase.java>
[65]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetSuper.java>
[66]: <https://bugs.documentfoundation.org/show_bug.cgi?id=156512>
[67]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/JDBCConnectionProperties.html#TypeInfoSettings>
[68]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/CustomTypeInfo.java>
[69]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu#L332>
[70]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DatabaseMetaDataBase.java#L444>
[71]: <https://github.com/artem78>
[72]: <https://github.com/prrvchr/jdbcDriverOOo/issues/4>
[73]: <https://github.com/prrvchr/HyperSQLOOo/issues/1>
[74]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/requirements.txt>
[75]: <https://peps.python.org/pep-0508/>
[76]: <https://prrvchr.github.io/jdbcDriverOOo/#requirement>
[77]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/XGeneratedResultSet.html>
[78]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XAlterTable.html>
[79]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.3-SNAPSHOT.jar>
[80]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.6-SNAPSHOT.jar>
[81]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu>
[82]: <https://jira.mariadb.org/browse/CONJ-1160>
[83]: <https://en.wikibooks.org/wiki/Java_Programming/Generics>
[84]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/TableContainerSuper.java>
[85]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ViewContainer.java>
[86]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ColumnContainerBase.java>
[87]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/KeyContainer.java>
[88]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/IndexContainer.java>
[89]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Container.java>
[90]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/DBTableHelper.java#L178>
[91]: <https://github.com/FirebirdSQL/jaybird/issues/791>
[92]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/DBTableHelper.java#L276>
[93]: <https://bugs.documentfoundation.org/show_bug.cgi?id=160375>
[94]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XRowLocate.html>
[95]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/resultset>
[96]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/RoleContainer.java>
[97]: <https://bugs.documentfoundation.org/show_bug.cgi?id=160516>
[98]: <https://hsqldb.org/doc/guide/management-chapt.html#mtc_system_versioned_tables>
[99]: <https://hsqldb.org/doc/guide/texttables-chapt.html#ttc_table_definition>
[100]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DriverBase.java#L367>
