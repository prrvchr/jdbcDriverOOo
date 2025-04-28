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
# [![jdbcDriverOOo logo][1]][2] Historical

**Ce [document][3] en français.**

**The use of this software subjects you to our [Terms Of Use][4]**

## What has been done for version 0.0.1:

- The writing of this driver was facilitated by a [discussion with Villeroy][53], on the OpenOffice forum, which I would like to thank, because knowledge is only worth if it is shared...

- Using the new version of HsqlDB 2.5.1.

- Many other fix...

## What has been done for version 0.0.2:

- Added a dialog box allowing to update the driver (hsqldb.jar) in: Tools -> Options -> Base drivers -> HsqlDB driver

- Many other fix...

## What has been done for version 0.0.3:

- I especially want to thank fredt at [hsqldb.org][54] for:

    - His welcome for this project and his permission to use the HsqlDB logo in the extension.

    - Its involvement in the test phase which made it possible to produce this version 0.0.3.

    - The quality of its HsqlDB database.

- Now works with OpenOffice on Windows.

- An unsupported protocol now displays an accurate error.

- A non-parsable url now displays a precise error.

- Now correctly handles spaces in filenames and paths.

- Many other fix...

## What has been done for version 0.0.4:

- Rewrite of [Driver][55] in Java version 11 OpenJDK amd64 under Eclipse IDE for Java Developers version 4.23.0 with the plugins:
    - LOEclipse or LibreOffice Eclipse plugin for extension development version 4.0.1.
    - PyDev or Python IDE for Eclipse version 9.3.0.

- Writing the `Statement`, `PreparedStatement`, `CallableStatement`, `ResultSet`, `...` services of JDBC (thanks to hanya for [MRI][56] which was of great help to me...)

    - [com.sun.star.sdb.*][57]
    - [com.sun.star.sdbc.*][58]
    - [com.sun.star.sdbcx.*][59]

- Integration in jdbcDriverOOo of **H2** and **Derby** JDBC drivers in addition to **HsqlDB**. Implementation of Java Services:

    - [Driver-HsqlDB.jar][60]
    - [Driver-H2.jar][61]
    - [Driver-Derby.jar][62]

    In order to correct possible defects, or incompatibility with the UNO API, of embedded JDBC drivers.

- Renamed the **HsqlDBDriverOOo** repository and extension to **jdbcDriverOOo**.

- Support in Base for **auto-incrementing primary keys** for HsqlDB, H2 and Derby.

- Writing of [com.sun.star.sdbcx.Driver][63]. This high-level driver must allow the **management of users, roles and privileges in Base**. Its use can be disabled via the menu: **Tools -> Options -> Base drivers -> JDBC Driver**.

- Implemented a Java service provider [UnoLogger.jar][64] for the [SLF4J][65] API to be able to redirect driver logging from the underlying databases to the UNO API [com.sun.star.logging.*][66].

- Rewrite, following the MVC model, of the [Options][67] dialog accessible via the menu: **Tools -> Options -> Base drivers -> JDBC Driver**, to allow:

    - Updating and/or adding Java archives of JDBC drivers.
    - Enabling driver logging of the underlying database.

- Writing, following the MVC model, [administration windows][68] for users and roles (groups) and their associated privileges, accessible in Base via the menu: **Administration -> User administration** and/or **Administration - > Group administration**, allowing:

    - [Management of users][69] and their privileges.
    - [Management of roles][70] (groups) and their privileges.

    These new features have only been tested with the HsqlDB driver so far.

- Many other fix...

## What has been done for version 1.0.0:

- Integration of HyperSQL version 2.7.2.

## What has been done for version 1.0.1:

- Integration of [SQLite JDBC][14] version 3.42.0.0. I especially want to thank [gotson][71] for the [many improvements to the SQLite JDBC driver][72] that made it possible to use SQLite in LibreOffice/OpenOffice.

- This driver can be wrapped by another driver ([HyperSQLOOo][26] or [SQLiteOOo][73]) thanks to a connection url now modifiable.

- It is possible to display or not the system tables in: **Tools -> Options -> Base drivers -> JDBC Driver -> UNO drivers settings -> Show system tables**

- It is possible to disallow the use of updatable resultset in: **Tools -> Options -> Base drivers -> JDBC Driver -> UNO drivers settings -> Use bookmarks**

- Many corrections have been made to make the extension [SQLiteOOo][73] functional.

## What has been done for version 1.0.2:

- Integration of [MariaDB Connector/J][15] version 3.1.4.

- Many other fix...

## What has been done for version 1.0.3:

- Integration of [H2][17] version 2.2.220.

- Integration of logging in the resultset ([ResultSetBase][74] and [ResultSetSuper][75]) in order to learn more about [issue 156512][76].

- Many other fix...

## What has been done for version 1.0.4:

- Support in the creation of tables of the [TypeInfoSettings][77] parameter allowing to recover the precision for SQL types:

    - TIME
    - TIMESTAMP
    - TIME WITH TIME ZONE
    - TIMESTAMP WITH TIME ZONE

    This is only [integrated][78] for the [HsqlDB][79] driver at the moment.

## What has been done for version 1.0.5:

- The result of accessing the [XDatabaseMetaData.getDriverVersion()][80] method is now recorded in the log file.

## What has been done for version 1.0.6:

- Added the Python package `packaging` to the extension's `pythonpath`. Thanks to [artem78][81] for allowing this correction by reporting this oversight in [issue #4][82].

## What has been done for version 1.0.7:

- Now the driver throws an exception if creating a new table fails. This is to address [bug #1][83] on the [HyperSQLOOo][26] extension.

## What has been done for version 1.0.8:

- Using the latest version of the Logging API.

## What has been done for version 1.1.0:

- All Python packages necessary for the extension are now recorded in a [requirements.txt][84] file following [PEP 508][85].
- Now if you are not on Windows then the Python packages necessary for the extension can be easily installed with the command:  
  `pip install requirements.txt`
- Modification of the [Requirement][86] section.

## What has been done for version 1.1.1:

- The driver no longer uses Bookmarkable ResultSets for performance reasons in LibreOffice Base. This can be changed in the extension options.

## What has been done for version 1.1.2:

- Implementation of the UNO interface [com.sun.star.sdbc.XGeneratedResultSet][87]. This interface allows, when inserting several rows (ie: `INSERT INTO mytable (Column1, Column2) VALUES (data1, data2), (data1, data2), ...`) into a table with an auto-incremented primary key, to retrieve a ResultSet from the rows inserted into the table and therefore gives you access to the auto-generated keys in one go.
- Implementation of the UNO interface [com.sun.star.sdbcx.XAlterTable][88]. This interface allows the modification of columns in a table. With HsqlDB it is now possible in Base:
  - Assign a description to table columns.
  - To modify the type of a column if the casting (CAST) of the data contained in this column allows it, otherwise you will be asked to replace this column which results in the deletion of the data...
- All DDL commands (ie: `CREATE TABLE...`, `ALTER TABLE...`) that jdbcDriverOOo generates are now logged.
- SQLite driver updated to latest version 3.45.1.0.
- Many other fix...

## What has been done for version 1.1.3:

- SQLite driver updated to latest version [SQLite-jdbc-3.45.1.3-SNAPSHOT.jar][89]. This new driver has been implemented to support part of the JDBC 4.1 specifications and more particularly the `java.sql.Statement.getGeneratedKeys()` interface and allows the use of the [com.sun.star.sdbc.XGeneratedResultSet][87] interface.

## What has been done for version 1.1.4:

- SQLite driver updated to latest version [SQLite-jdbc-3.45.1.6-SNAPSHOT.jar][90].
- Integration of the driver [PostgreSQL pgJDBC][16] version 42.7.1 in the jdbcDriverOOo archive. This integration was carried out without using a Java service specific to PostgreSQL but only by configuring the [Drivers.xcu][91] file allowing the JDBC driver to be declared to LibreOffice.
- Opened a [bug][92] for the [MariaDB Connector/J][15] driver so that it supports `java.sql.Statement.getGeneratedKeys()` as requested by JDBC 4.1.
- Normally the next versions of jdbcDriverOOo should be able to be updated in the list of extensions installed under LibreOffice: **Tools -> Extension manager... -> Check for Updates**.
- From now on, only the HsqlDB driver has access in Base to the administration of user and group rights. This is determined by the `IgnoreDriverPrivileges` setting in the [Drivers.xcu][91] file.
- Many improvements.

## What has been done for version 1.1.5:

- You can now edit a view in SQL mode with the SQLite driver. For drivers that do not support view alteration, views are deleted and then recreated.

## What has been done for version 1.1.6:

- You can now rename tables and views in Base. All the configuration required for renaming for each embedded JDBC driver is stored only in the [Drivers.xcu][91] file.
- All JDBC drivers integrated into jdbcDriverOOo are capable of renaming tables or views and even some (ie: MariaDB and PostgreSQL) allow modifying the catalog or schema.
- Many improvements.

## What has been done for version 1.2.0:

- All drivers integrated into the extension are **now fully functional in Base** for managing tables and views.
- Smart functions are called to:
  - Move with renaming of tables, for drivers allowing it and using two SQL commands, the order of the SQL commands will be optimized (PostgreSQL).
  - Rename a view if the driver does not support it it will be deleted then recreated (SQLite).
- Use of [generic Java class][93] for managing containers used for managing [tables][94], [views][95], [columns][96], [keys][97] and [indexes][98]. The use of generic classes for [container][99] will make it possible to do without the UNO XPropertySet interface and to be able to transcribe the existing code into pure Java.
- Many improvements.

## What has been done for version 1.2.1:

- Resolution of a regression prohibiting the deletion of columns in a table.
- Updated mariadb-java-client-3.3.3.jar driver.
- Generalization of generic Java classes for all classes needing to be shared at the UNO API level (ie: sdb, sdbc and sdbcx).
- We can now rename table columns in SQLite and MariaDB.
- It is also possible to rename the columns declared as primary key in all embedded drivers.
- Many improvements.

## What has been done for version 1.2.2:

- Implementation of index management.
- Renaming a column declared as a primary key will also rename the index associated with the primary key.
- Only members of Java classes responding to the UNO API have a public visibility level, all other members have protected or private visibility.
- Solved many problems and regression.

## What has been done for version 1.2.3:

- Renaming a column declared as an index will also rename the associated column index.

## What has been done for version 1.2.4:

- Removed SmallSQL.
- Integration of Jaybird 5.0.4 the JDBC driver for Firebird.
- You can now delete a primary key with PostgreSQL.
- Adding or removing a primary key generates an error if the underlying driver does not support it (SQLite).
- When [creating a table][100] with a primary key, if the underlying driver supports it, the creation of the primary key can be done by a separate DDL command. This allows Jaybird to work around [bug #791][101] by creating a named primary key and allows to manage special cases like MariaDB or SQLite for their management of auto-increments.
- If the underlying driver allows it, when [altering columns][102] of a table you can now:
  - Declare it as auto-increment (Identity) without it necessarily being the primary key.
  - Add or remove the Identity constraint (auto-increment).
  - Add comments.
- Many improvements.

## What has been done for version 1.3.0:

- Integration of foreign key management into Base (**Tools -> Relationships...**).
  - When you rename a table, it will also rename that table's referencing in any foreign keys pointing to that table.
  - When you rename a column, it will also rename that column's referencing in any foreign keys pointing to that column.
  - These foreign key updates take into account lazy loading of table and key containers and will only be performed if Base has already accessed the data involved.
  - An issue persists when creating foreign keys between tables that do not have the same catalog and/or schema, see [bug #160375][103]. This issue appears to be related to Base, I hope it gets resolved soon.
- Better exception handling with the ability to know the status, SQL code and message of the exception that was generated by the underlying driver.
- Many fixes and improvements.

Normally, I managed to cover the entire scope of the UNO API ([com.sun.star.sdbc][10], [sdbcx][11] and [sdb][12]), which took quite a while, but I didn't initially think I would get there.  

## What has been done for version 1.3.1:

- Fixed the implementation of the [XRowLocate][104] interface responsible for managing Bookmarks in ResultSet. This new implementation works with all drivers except SQLite which does not support updatable ResultSet. The presence of this interface in ResultSet allows Base to edit tables even in absence of primary key. With certain drivers (HsqlDB, H2 and Derby) refreshing during entry will not be automatic and must be done manually. The use of bookmarks can be disabled in the extension's options.
- Setting up [mock ResultSet][105] (java.sql.ResultSet) to produce ResultSets from connection data provided by the driver, more precisely from the [Drivers.xcu][91] file. The use of these simulated resultsets makes it possible to provide Base with resultsets conforming to what it expects even if the underlying driver is not capable of producing them. They are used to patch the results obtained from the `getTypeInfo()`, `getTableTypes` and `getTablePrivileges()` methods of the java.sql.DatabaseMetaData interface using respectively the `TypeInfoSettings`, `TableTypesSettings` and `TablePrivilegesSettings` properties of the [Drivers.xcu][91] file.
- Writing a [specific container][106] to manage the users of a role or the roles of a role. This container is just a pointer to the elements of the user and/or role containers in the database. When deleting a user or role this container will be updated if necessary.
- Rewrote the **User administration** and **Group administration** windows accessible in Base **Administration** menu. Now, if the `TablePrivilegesSettings` property is provided by the underlying driver, only the privileges declared in this property will be displayed. This allows for easier use. An [improvement request #160516][107] was made to integrate this functionality into the Base code.
- Integration of all drivers embedded in the extension (excluding SQLite) in the management of users, roles and privileges on tables and views. I suppose that many malfunctions remain to be corrected, please let me know, detecting malfunctions takes me more time than correcting them....
- Many corrections and improvements...

## What has been done for version 1.3.2:

The UNO SDBCX API can now be used for creating databases, as is the case for the latest versions of extensions using jdbcDriverOOo. It is possible to create tables, using the UNO API, with the following characteristics:
- Declaration of columns of types TIMESTAMP WITH TIME ZONE, TIMESTAMP, TIME WITH TIME ZONE, TIME with precision management (ie: from 0 to 9).
- Declaration of [temporal system versioned tables][108]. These types of tables are used in the same extensions to facilitate data replication.
- Declaration of [text tables][109]. These tables allow you to use data from files in csv format.
- Declaration of primary keys, foreign keys, indexes, users, roles and associated privileges.

Using the UNO API to create databases will allow you to use code that is independent of the underlying database.

Clients using the jdbcDriverOOo driver can access features of the underlying JDBC driver through the [XDriver.getPropertyInfo()][110] method in order to access the necessary parameter when creating tables and display privileges correctly. These parameters being accessible directly by the driver can be obtained before any connection and therefore allows the creation of the database during the first connection.

## What has been done for version 1.3.3:

- [Modification of the handling][111] of the `JavaDriverClassPath` connection parameter. This parameter can now designate a directory and in this case all contained jar files will be added to the `Java ClassPath`. This allows dynamic loading of JDBC drivers requiring multiple archives (ie: Derby and Jaybird embedded). This change was made to allow the new [JaybirdOOo][112] extension to work.
- Resumed part of the implementation of `javax.sql.rowset.CachedRowSet` in the [ScrollableResultSet.java][113] and [SensitiveResultSet.java][114] ResultSet in order to simulate the `TYPE_SCROLL_SENSITIVE` type from ResultSet of type `TYPE_FORWARD_ONLY` and `TYPE_SCROLL_INSENSITIVE` respectively. This allows LibreOffice Base to use bookmarks (ie: the UNO interface [XRowLocate][104]) which allow positioned insertions, updates and deletions and therefore, for databases supporting it, the possibility of edit tables containing no primary key. In addition, an [SQL mode][115] **allows any ResultSet to be editable.** This mode can be validated in the extension's options, it is very powerful and should therefore be used with caution. Concerning result sets of type `TYPE_FORWARD_ONLY`, their implementation progressively loading the entire data of the result set into memory can lead to a memory overflow. Implementing pagination will eliminate this risk.
- Added MySQL Connector/J version 8.4.0 driver. This driver does not seem to work correctly, quite surprising errors appear... I leave it in place in case people are ready to participate in its integration? Use with caution.
- Following the request of [PeterSchmidt23][116] addition of the driver [Trino][117] version 448. Not knowing Trino, which also looks astonishing, only the beginning of integration has been carried out. Editing the contents of the tables is not yet possible, seer [issue #22306][118]. The name of the tables must be in lowercase in order to authorize their creation.
- The implementation of `CachedRowSet` seems to have solved the problem of inserting cells from Calc, see [issue #7][119].
- Many corrections and improvements...

## What has been done for version 1.4.0:

- Updated Jaybird driver to final version 5.0.5.
- Changed the implementation of the UNO interface [com.sun.star.sdbc.XGeneratedResultSet][87]. This new implementation supports drivers that do not follow the JDBC API but offer a specific implementation (ie: MariaDB and Derby). To be activated when using odb files created with a previous version, if present, it is necessary to modify the parameter: `Query of generated values` accessible by the menu: **Edit -> Database -> Advanced Settings... -> Generated Values** by the value: `SELECT * FROM %s WHERE %s`.
- Added new settings supported by the [Drivers.xcu][91] configuration file. These new parameters allow you to modify the values ​​returned by the drivers regarding the visibility of modifications in the ResultSet (ie: insertion, update and deletion). They also allow you to force SQL mode for the desired modifications in the ResultSet.
- Finalized the emulation implementation making any ResultSet modifiable, if the record is unique in this ResultSet. This implementation, using bookmarks, allows the editing of ResultSet coming from **Base Queries**, this simply makes **LibreOffice Base Queries editable**. Queries joining multiple tables are not yet supported and I am open to any technical proposals regarding a possible implementation.
- In order to make the ResultSet returned by the **Trino** driver modifiable and to precede [feature request #22408][120], a search for the primary key will be launched in order to find the first column, of result set, having no duplicates.
- To work around [issue #368][120] the HsqlDB driver uses SQL mode updates in ResultSet.
- Many fixes and improvements...

## What has been done for version 1.4.1:

- New implementation, which I hope is definitive, of bookmarks. It is based on three files and is taken from Sun's implementation of `javax.sql.rowset.CachedRowSet`:
  - [ScollableResultSet.class][113]
  - [SensitiveResultSet.class][114]
  - [CachedResultSet.class][122]
- **These ResultSets are capable of editing almost all queries created in LibreOffice Base, even views...** But in order to guarantee good functionality, certain rules must be respected in order to make a result set editable. If the query concerns several tables then it is imperative to include the primary keys of each table in the list of columns of the result set. If the query only concerns a single table then the result set will be modifiable if there is a column that does not contain a duplicate (ie: a natural key). This makes it possible to make the result sets coming from the Trino driver editable.
- Removed the use of generic classes where they were not needed. This made the driver faster...
- Added special parameters in: **Edit -> Database -> Advanced parameters... -> Special parameters** in order to respond to the request for integration of the Trino driver (see [improvement request #8 ][123]). It is necessary to recreate the odb files in order to have access to these new parameters.

## What has been done for version 1.4.2:

- Trino JDBC driver updated to version 453.
- Updated the [Python packaging][124] package to version 24.1.
- Updated the [Python setuptools][125] package to version 72.1.0 in order to respond to the [Dependabot security alert][126].

## What has been done for version 1.4.3:

- Updated the [Python setuptools][125] package to version 73.0.1.
- Logging accessible in extension options now displays correctly on Windows.
- The extension options are now accessible via: **Tools -> Options... -> LibreOffice Base -> JDBC Driver**
- Changes to extension options that require a restart of LibreOffice will result in a message being displayed.
- Support for LibreOffice version 24.8.x.

## What has been done for version 1.4.4:

- It is now possible to insert data into an empty table when using an `TYPE_FORWARD_ONLY` ResultSet (ie: SQLite, Trino).
- The options button is now accessible in the list of installed extensions obtained by the menu: **Tools -> Extensions Manager...**
- The extension options are now accessible via: **Tools -> Options... -> LibreOffice Base -> Pure Java JDBC Driver**
- The extension options: **View system tables**, **Use bookmarks** and **Force SQL mode** will be searched in the information provided when connecting and will take precedence if present.
- Updated Trino driver to version 455.

## What has been done for version 1.4.5:

- Fix to allow the eMailerOOo extension to work properly in version 1.2.5.

## What has been done for version 1.4.6:

- Modification of the implementation of the interface UNO [XPropertySet][127]. This new implementation ensures the uniqueness of [Handle][128] for each property. Since this implementation is shared with the vCardOOo extension, **it makes all existing versions of vCardOOo obsolete**. It is based on three files:
  - [PropertySet.java][129]
  - [PropertySetAdapter.java][130]
  - [PropertyWrapper.java][131]
- Fixed issues in bookmark implementation. These modifications have been tested more particularly with the HsqlDB 2.7.4 and Jaybird 5.0.6 drivers.
- New implementation of the extension options and more specifically the **JDBC Driver Options** tab which should eventually allow the configuration from scratch of a JDBC driver to be able to work with LibreOffice Base. The JDBC driver archive update operation has been simplified. It supports updating drivers that require multiple jar archives to work (ie: Derby, Jaybird 6.x). This new window, which seems quite simple, actually requires quite complicated management, so please do not hesitate to report any malfunctions.
- Many other improvements.

## What has been done for version 1.4.7:

- Passive registration deployment allowing for much faster extension installation as well as the ability to differentiate registered UNO services from UNO services provided by a Java or Python implementation. This passive registration is provided by the LOEclipse extension via [PR#152][132] and [PR#157][133].

## What remains to be done for version 1.4.7:

- Add new languages for internationalization...

- Anything welcome...

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_en>
[5]: <https://prrvchr.github.io/jdbcDriverOOo/#what-has-been-done-for-version-146>
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
[34]: <https://adoptium.net/releases.html?variant=openjdk11>
[35]: <https://bugs.documentfoundation.org/show_bug.cgi?id=139538>
[36]: <https://prrvchr.github.io/HyperSQLOOo/>
[37]: <https://prrvchr.github.io/jdbcDriverOOo/#what-has-been-done-for-version-110>
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
[52]: <https://bugs.documentfoundation.org/show_bug.cgi?id=132195>
[53]: <https://forum.openoffice.org/en/forum/viewtopic.php?f=13&t=103912>
[54]: <http://hsqldb.org/>
[55]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/Driver.java>
[56]: <https://github.com/hanya/MRI>
[57]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdb>
[58]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc>
[59]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx>
[60]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-HsqlDB/source/io/github/prrvchr/jdbcdriver/hsqldb>
[61]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-H2/source/io/github/prrvchr/jdbcdriver/h2>
[62]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-Derby/source/io/github/prrvchr/jdbcdriver/derby>
[63]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Driver.java>
[64]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/UnoLogger/source/io/github/prrvchr/uno/logging>
[65]: <https://www.slf4j.org/>
[66]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/logging/module-ix.html>
[67]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/options>
[68]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/admin>
[69]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/user>
[70]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/group>
[71]: <https://github.com/gotson>
[72]: <https://github.com/xerial/sqlite-jdbc/issues/786>
[73]: <https://prrvchr.github.io/SQLiteOOo>
[74]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetBase.java>
[75]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetSuper.java>
[76]: <https://bugs.documentfoundation.org/show_bug.cgi?id=156512>
[77]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/JDBCConnectionProperties.html#TypeInfoSettings>
[78]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/CustomTypeInfo.java>
[79]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu#L332>
[80]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DatabaseMetaDataBase.java#L444>
[81]: <https://github.com/artem78>
[82]: <https://github.com/prrvchr/jdbcDriverOOo/issues/4>
[83]: <https://github.com/prrvchr/HyperSQLOOo/issues/1>
[84]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/requirements.txt>
[85]: <https://peps.python.org/pep-0508/>
[86]: <https://prrvchr.github.io/jdbcDriverOOo/#requirement>
[87]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/XGeneratedResultSet.html>
[88]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XAlterTable.html>
[89]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.3-SNAPSHOT.jar>
[90]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.6-SNAPSHOT.jar>
[91]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu>
[92]: <https://jira.mariadb.org/browse/CONJ-1160>
[93]: <https://en.wikibooks.org/wiki/Java_Programming/Generics>
[94]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/TableContainerSuper.java>
[95]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ViewContainer.java>
[96]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ColumnContainerBase.java>
[97]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/KeyContainer.java>
[98]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/IndexContainer.java>
[99]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Container.java>
[100]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/DBTableHelper.java#L178>
[101]: <https://github.com/FirebirdSQL/jaybird/issues/791>
[102]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/DBTableHelper.java#L276>
[103]: <https://bugs.documentfoundation.org/show_bug.cgi?id=160375>
[104]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XRowLocate.html>
[105]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/resultset>
[106]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/RoleContainer.java>
[107]: <https://bugs.documentfoundation.org/show_bug.cgi?id=160516>
[108]: <https://hsqldb.org/doc/guide/management-chapt.html#mtc_system_versioned_tables>
[109]: <https://hsqldb.org/doc/guide/texttables-chapt.html#ttc_table_definition>
[110]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DriverBase.java#L185>
[111]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DriverBase.java#L395>
[112]: <https://prrvchr.github.io/JaybirdOOo/>
[113]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/resultset/ScrollableResultSet.java#L57>
[114]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/resultset/SensitiveResultSet.java#L60>
[115]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/rowset/RowSetWriter.java#L41>
[116]: <https://github.com/prrvchr/jdbcDriverOOo/issues/8>
[117]: <https://trino.io/>
[118]: <https://github.com/trinodb/trino/issues/22306>
[119]: <https://github.com/prrvchr/jdbcDriverOOo/issues/7>
[120]: <https://github.com/trinodb/trino/issues/22408>
[121]: <https://sourceforge.net/p/hsqldb/feature-requests/368/>
[122]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/resultset/CachedResultSet.java#L55>
[123]: <https://github.com/prrvchr/jdbcDriverOOo/issues/8#issuecomment-2182445391>
[124]: <https://pypi.org/project/packaging/>
[125]: <https://pypi.org/project/setuptools/>
[126]: <https://github.com/prrvchr/jdbcDriverOOo/pull/9>
[127]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/beans/XPropertySet.html>
[128]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/beans/Property.html#Handle>
[129]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/uno/lib/java/helper/PropertySet.java>
[130]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/uno/lib/java/helper/PropertySetAdapter.java>
[131]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/uno/lib/java/helper/PropertyWrapper.java>
[132]: <https://github.com/LibreOffice/loeclipse/pull/152>
[133]: <https://github.com/LibreOffice/loeclipse/pull/157>
