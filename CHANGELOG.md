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

Regarding installation, configuration and use, please consult the **[documentation][4]**.

### What has been done for version 0.0.1:

- The writing of this driver was facilitated by a [discussion with Villeroy][5], on the OpenOffice forum, which I would like to thank, because knowledge is only worth if it is shared...
- Using the new version of HsqlDB 2.5.1.
- Many other fix...

### What has been done for version 0.0.2:

- Added a dialog box allowing to update the driver (hsqldb.jar) in: Tools -> Options -> Base drivers -> HsqlDB driver
- Many other fix...

### What has been done for version 0.0.3:

- I especially want to thank fredt at [hsqldb.org][6] for:
    - His welcome for this project and his permission to use the HsqlDB logo in the extension.
    - Its involvement in the test phase which made it possible to produce this version 0.0.3.
    - The quality of its HsqlDB database.
- Now works with OpenOffice on Windows.
- An unsupported protocol now displays an accurate error.
- A non-parsable url now displays a precise error.
- Now correctly handles spaces in filenames and paths.
- Many other fix...

### What has been done for version 0.0.4:

- Rewrite of [Driver][7] in Java version 11 OpenJDK amd64 under Eclipse IDE for Java Developers version 4.23.0 with the plugins:
    - LOEclipse or LibreOffice Eclipse plugin for extension development version 4.0.1.
    - PyDev or Python IDE for Eclipse version 9.3.0.
- Writing the `Statement`, `PreparedStatement`, `CallableStatement`, `ResultSet`, `...` services of JDBC (thanks to hanya for [MRI][8] which was of great help to me...)
    - [com.sun.star.sdb.*][9]
    - [com.sun.star.sdbc.*][10]
    - [com.sun.star.sdbcx.*][11]
- Integration in jdbcDriverOOo of **H2** and **Derby** JDBC drivers in addition to **HsqlDB**. Implementation of Java Services:
    - [Driver-HsqlDB.jar][12]
    - [Driver-H2.jar][13]
    - [Driver-Derby.jar][14]
    In order to correct possible defects, or incompatibility with the UNO API, of embedded JDBC drivers.
- Renamed the **HsqlDBDriverOOo** repository and extension to **jdbcDriverOOo**.
- Support in Base for **auto-incrementing primary keys** for HsqlDB, H2 and Derby.
- Writing of [com.sun.star.sdbcx.Driver][15]. This high-level driver must allow the **management of users, roles and privileges in Base**. Its use can be disabled via the menu: **Tools -> Options -> Base drivers -> JDBC Driver**.
- Implemented a Java service provider [UnoLogger.jar][16] for the [SLF4J][17] API to be able to redirect driver logging from the underlying databases to the UNO API [com.sun.star.logging.*][18].
- Rewrite, following the MVC model, of the [Options][19] dialog accessible via the menu: **Tools -> Options -> Base drivers -> JDBC Driver**, to allow:
    - Updating and/or adding Java archives of JDBC drivers.
    - Enabling driver logging of the underlying database.
- Writing, following the MVC model, [administration windows][20] for users and roles (groups) and their associated privileges, accessible in Base via the menu: **Administration -> User administration** and/or **Administration - > Group administration**, allowing:
    - [Management of users][21] and their privileges.
    - [Management of roles][22] (groups) and their privileges.
    These new features have only been tested with the HsqlDB driver so far.
- Many other fix...

### What has been done for version 1.0.0:

- Integration of HyperSQL version 2.7.2.

### What has been done for version 1.0.1:

- Integration of [SQLite JDBC][23] version 3.42.0.0. I especially want to thank [gotson][24] for the [many improvements to the SQLite JDBC driver][25] that made it possible to use SQLite in LibreOffice/OpenOffice.
- This driver can be wrapped by another driver ([HyperSQLOOo][26] or [SQLiteOOo][27]) thanks to a connection url now modifiable.
- It is possible to display or not the system tables in: **Tools -> Options -> Base drivers -> JDBC Driver -> UNO drivers settings -> Show system tables**
- It is possible to disallow the use of updatable resultset in: **Tools -> Options -> Base drivers -> JDBC Driver -> UNO drivers settings -> Use bookmarks**
- Many corrections have been made to make the extension [SQLiteOOo][27] functional.

### What has been done for version 1.0.2:

- Integration of [MariaDB Connector/J][28] version 3.1.4.
- Many other fix...

### What has been done for version 1.0.3:

- Integration of [H2][29] version 2.2.220.
- Integration of logging in the resultset ([ResultSetBase][30] and [ResultSetSuper][31]) in order to learn more about [issue 156512][32].
- Many other fix...

### What has been done for version 1.0.4:

- Support in the creation of tables of the [TypeInfoSettings][33] parameter allowing to recover the precision for SQL types:
    - TIME
    - TIMESTAMP
    - TIME WITH TIME ZONE
    - TIMESTAMP WITH TIME ZONE
    This is only [integrated][34] for the [HsqlDB][35] driver at the moment.

### What has been done for version 1.0.5:

- The result of accessing the [XDatabaseMetaData.getDriverVersion()][36] method is now recorded in the log file.

### What has been done for version 1.0.6:

- Added the Python package `packaging` to the extension's `pythonpath`. Thanks to [artem78][37] for allowing this correction by reporting this oversight in [issue #4][38].

### What has been done for version 1.0.7:

- Now the driver throws an exception if creating a new table fails. This is to address [bug #1][39] on the [HyperSQLOOo][26] extension.

### What has been done for version 1.0.8:

- Using the latest version of the Logging API.

### What has been done for version 1.1.0:

- All Python packages necessary for the extension are now recorded in a [requirements.txt][40] file following [PEP 508][41].
- Now if you are not on Windows then the Python packages necessary for the extension can be easily installed with the command:  
  `pip install requirements.txt`
- Modification of the [Requirement][42] section.

### What has been done for version 1.1.1:

- The driver no longer uses Bookmarkable ResultSets for performance reasons in LibreOffice Base. This can be changed in the extension options.

### What has been done for version 1.1.2:

- Implementation of the UNO interface [com.sun.star.sdbc.XGeneratedResultSet][43]. This interface allows, when inserting several rows (ie: `INSERT INTO mytable (Column1, Column2) VALUES (data1, data2), (data1, data2), ...`) into a table with an auto-incremented primary key, to retrieve a ResultSet from the rows inserted into the table and therefore gives you access to the auto-generated keys in one go.
- Implementation of the UNO interface [com.sun.star.sdbcx.XAlterTable][44]. This interface allows the modification of columns in a table. With HsqlDB it is now possible in Base:
  - Assign a description to table columns.
  - To modify the type of a column if the casting (CAST) of the data contained in this column allows it, otherwise you will be asked to replace this column which results in the deletion of the data...
- All DDL commands (ie: `CREATE TABLE...`, `ALTER TABLE...`) that jdbcDriverOOo generates are now logged.
- SQLite driver updated to latest version 3.45.1.0.
- Many other fix...

### What has been done for version 1.1.3:

- SQLite driver updated to latest version [SQLite-jdbc-3.45.1.3-SNAPSHOT.jar][45]. This new driver has been implemented to support part of the JDBC 4.1 specifications and more particularly the `java.sql.Statement.getGeneratedKeys()` interface and allows the use of the [com.sun.star.sdbc.XGeneratedResultSet][43] interface.

### What has been done for version 1.1.4:

- SQLite driver updated to latest version [SQLite-jdbc-3.45.1.6-SNAPSHOT.jar][46].
- Integration of the driver [PostgreSQL pgJDBC][47] version 42.7.1 in the jdbcDriverOOo archive. This integration was carried out without using a Java service specific to PostgreSQL but only by configuring the [Drivers.xcu][48] file allowing the JDBC driver to be declared to LibreOffice.
- Opened a [bug][49] for the [MariaDB Connector/J][28] driver so that it supports `java.sql.Statement.getGeneratedKeys()` as requested by JDBC 4.1.
- Normally the next versions of jdbcDriverOOo should be able to be updated in the list of extensions installed under LibreOffice: **Tools -> Extension manager... -> Check for Updates**.
- From now on, only the HsqlDB driver has access in Base to the administration of user and group rights. This is determined by the `IgnoreDriverPrivileges` setting in the [Drivers.xcu][48] file.
- Many improvements.

### What has been done for version 1.1.5:

- You can now edit a view in SQL mode with the SQLite driver. For drivers that do not support view alteration, views are deleted and then recreated.

### What has been done for version 1.1.6:

- You can now rename tables and views in Base. All the configuration required for renaming for each embedded JDBC driver is stored only in the [Drivers.xcu][48] file.
- All JDBC drivers integrated into jdbcDriverOOo are capable of renaming tables or views and even some (ie: MariaDB and PostgreSQL) allow modifying the catalog or schema.
- Many improvements.

### What has been done for version 1.2.0:

- All drivers integrated into the extension are **now fully functional in Base** for managing tables and views.
- Smart functions are called to:
  - Move with renaming of tables, for drivers allowing it and using two SQL commands, the order of the SQL commands will be optimized (PostgreSQL).
  - Rename a view if the driver does not support it it will be deleted then recreated (SQLite).
- Use of [generic Java class][50] for managing containers used for managing [tables][51], [views][52], [columns][53], [keys][54] and [indexes][55]. The use of generic classes for [container][56] will make it possible to do without the UNO XPropertySet interface and to be able to transcribe the existing code into pure Java.
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
- When [creating a table][57] with a primary key, if the underlying driver supports it, the creation of the primary key can be done by a separate DDL command. This allows Jaybird to work around [bug #791][58] by creating a named primary key and allows to manage special cases like MariaDB or SQLite for their management of auto-increments.
- If the underlying driver allows it, when [altering columns][59] of a table you can now:
  - Declare it as auto-increment (Identity) without it necessarily being the primary key.
  - Add or remove the Identity constraint (auto-increment).
  - Add comments.
- Many improvements.

### What has been done for version 1.3.0:

- Integration of foreign key management into Base (**Tools -> Relationships...**).
  - When you rename a table, it will also rename that table's referencing in any foreign keys pointing to that table.
  - When you rename a column, it will also rename that column's referencing in any foreign keys pointing to that column.
  - These foreign key updates take into account lazy loading of table and key containers and will only be performed if Base has already accessed the data involved.
  - An issue persists when creating foreign keys between tables that do not have the same catalog and/or schema, see [bug #160375][60]. This issue appears to be related to Base, I hope it gets resolved soon.
- Better exception handling with the ability to know the status, SQL code and message of the exception that was generated by the underlying driver.
- Many fixes and improvements.

Normally, I managed to cover the entire scope of the UNO API ([com.sun.star.sdbc][61], [sdbcx][62] and [sdb][63]), which took quite a while, but I didn't initially think I would get there.  

### What has been done for version 1.3.1:

- Fixed the implementation of the [XRowLocate][64] interface responsible for managing Bookmarks in ResultSet. This new implementation works with all drivers except SQLite which does not support updatable ResultSet. The presence of this interface in ResultSet allows Base to edit tables even in absence of primary key. With certain drivers (HsqlDB, H2 and Derby) refreshing during entry will not be automatic and must be done manually. The use of bookmarks can be disabled in the extension's options.
- Setting up [mock ResultSet][65] (java.sql.ResultSet) to produce ResultSets from connection data provided by the driver, more precisely from the [Drivers.xcu][48] file. The use of these simulated resultsets makes it possible to provide Base with resultsets conforming to what it expects even if the underlying driver is not capable of producing them. They are used to patch the results obtained from the `getTypeInfo()`, `getTableTypes` and `getTablePrivileges()` methods of the java.sql.DatabaseMetaData interface using respectively the `TypeInfoSettings`, `TableTypesSettings` and `TablePrivilegesSettings` properties of the [Drivers.xcu][48] file.
- Writing a [specific container][66] to manage the users of a role or the roles of a role. This container is just a pointer to the elements of the user and/or role containers in the database. When deleting a user or role this container will be updated if necessary.
- Rewrote the **User administration** and **Group administration** windows accessible in Base **Administration** menu. Now, if the `TablePrivilegesSettings` property is provided by the underlying driver, only the privileges declared in this property will be displayed. This allows for easier use. An [improvement request #160516][67] was made to integrate this functionality into the Base code.
- Integration of all drivers embedded in the extension (excluding SQLite) in the management of users, roles and privileges on tables and views. I suppose that many malfunctions remain to be corrected, please let me know, detecting malfunctions takes me more time than correcting them....
- Many corrections and improvements...

### What has been done for version 1.3.2:

The UNO SDBCX API can now be used for creating databases, as is the case for the latest versions of extensions using jdbcDriverOOo. It is possible to create tables, using the UNO API, with the following characteristics:
- Declaration of columns of types TIMESTAMP WITH TIME ZONE, TIMESTAMP, TIME WITH TIME ZONE, TIME with precision management (ie: from 0 to 9).
- Declaration of [temporal system versioned tables][68]. These types of tables are used in the same extensions to facilitate data replication.
- Declaration of [text tables][69]. These tables allow you to use data from files in csv format.
- Declaration of primary keys, foreign keys, indexes, users, roles and associated privileges.

Using the UNO API to create databases will allow you to use code that is independent of the underlying database.

Clients using the jdbcDriverOOo driver can access features of the underlying JDBC driver through the [XDriver.getPropertyInfo()][70] method in order to access the necessary parameter when creating tables and display privileges correctly. These parameters being accessible directly by the driver can be obtained before any connection and therefore allows the creation of the database during the first connection.

### What has been done for version 1.3.3:

- [Modification of the handling][71] of the `JavaDriverClassPath` connection parameter. This parameter can now designate a directory and in this case all contained jar files will be added to the `Java ClassPath`. This allows dynamic loading of JDBC drivers requiring multiple archives (ie: Derby and Jaybird embedded). This change was made to allow the new [JaybirdOOo][72] extension to work.
- Resumed part of the implementation of `javax.sql.rowset.CachedRowSet` in the [ScrollableResultSet.java][73] and [SensitiveResultSet.java][74] ResultSet in order to simulate the `TYPE_SCROLL_SENSITIVE` type from ResultSet of type `TYPE_FORWARD_ONLY` and `TYPE_SCROLL_INSENSITIVE` respectively. This allows LibreOffice Base to use bookmarks (ie: the UNO interface [XRowLocate][64]) which allow positioned insertions, updates and deletions and therefore, for databases supporting it, the possibility of edit tables containing no primary key. In addition, an [SQL mode][75] **allows any ResultSet to be editable.** This mode can be validated in the extension's options, it is very powerful and should therefore be used with caution. Concerning result sets of type `TYPE_FORWARD_ONLY`, their implementation progressively loading the entire data of the result set into memory can lead to a memory overflow. Implementing pagination will eliminate this risk.
- Added MySQL Connector/J version 8.4.0 driver. This driver does not seem to work correctly, quite surprising errors appear... I leave it in place in case people are ready to participate in its integration? Use with caution.
- Following the request of [PeterSchmidt23][76] addition of the driver [Trino][77] version 448. Not knowing Trino, which also looks astonishing, only the beginning of integration has been carried out. Editing the contents of the tables is not yet possible, seer [issue #22306][78]. The name of the tables must be in lowercase in order to authorize their creation.
- The implementation of `CachedRowSet` seems to have solved the problem of inserting cells from Calc, see [issue #7][79].
- Many corrections and improvements...

### What has been done for version 1.4.0:

- Updated Jaybird driver to final version 5.0.5.
- Changed the implementation of the UNO interface [com.sun.star.sdbc.XGeneratedResultSet][43]. This new implementation supports drivers that do not follow the JDBC API but offer a specific implementation (ie: MariaDB and Derby). To be activated when using odb files created with a previous version, if present, it is necessary to modify the parameter: `Query of generated values` accessible by the menu: **Edit -> Database -> Advanced Settings... -> Generated Values** by the value: `SELECT * FROM %s WHERE %s`.
- Added new settings supported by the [Drivers.xcu][48] configuration file. These new parameters allow you to modify the values ​​returned by the drivers regarding the visibility of modifications in the ResultSet (ie: insertion, update and deletion). They also allow you to force SQL mode for the desired modifications in the ResultSet.
- Finalized the emulation implementation making any ResultSet modifiable, if the record is unique in this ResultSet. This implementation, using bookmarks, allows the editing of ResultSet coming from **Base Queries**, this simply makes **LibreOffice Base Queries editable**. Queries joining multiple tables are not yet supported and I am open to any technical proposals regarding a possible implementation.
- In order to make the ResultSet returned by the **Trino** driver modifiable and to precede [feature request #22408][80], a search for the primary key will be launched in order to find the first column, of result set, having no duplicates.
- To work around [issue #368][81] the HsqlDB driver uses SQL mode updates in ResultSet.
- Many fixes and improvements...

### What has been done for version 1.4.1:

- New implementation, which I hope is definitive, of bookmarks. It is based on three files and is taken from Sun's implementation of `javax.sql.rowset.CachedRowSet`:
  - [ScollableResultSet.class][73]
  - [SensitiveResultSet.class][74]
  - [CachedResultSet.class][82]
- **These ResultSets are capable of editing almost all queries created in LibreOffice Base, even views...** But in order to guarantee good functionality, certain rules must be respected in order to make a result set editable. If the query concerns several tables then it is imperative to include the primary keys of each table in the list of columns of the result set. If the query only concerns a single table then the result set will be modifiable if there is a column that does not contain a duplicate (ie: a natural key). This makes it possible to make the result sets coming from the Trino driver editable.
- Removed the use of generic classes where they were not needed. This made the driver faster...
- Added special parameters in: **Edit -> Database -> Advanced parameters... -> Special parameters** in order to respond to the request for integration of the Trino driver (see [improvement request #8 ][83]). It is necessary to recreate the odb files in order to have access to these new parameters.

### What has been done for version 1.4.2:

- Trino JDBC driver updated to version 453.
- Updated the [Python packaging][84] package to version 24.1.
- Updated the [Python setuptools][85] package to version 72.1.0 in order to respond to the [Dependabot security alert][86].

### What has been done for version 1.4.3:

- Updated the [Python setuptools][85] package to version 73.0.1.
- Logging accessible in extension options now displays correctly on Windows.
- The extension options are now accessible via: **Tools -> Options -> LibreOffice Base -> JDBC Driver**
- Changes to extension options that require a restart of LibreOffice will result in a message being displayed.
- Support for LibreOffice version 24.8.x.

### What has been done for version 1.4.4:

- It is now possible to insert data into an empty table when using an `TYPE_FORWARD_ONLY` ResultSet (ie: SQLite, Trino).
- The options button is now accessible in the list of installed extensions obtained by the menu: **Tools -> Extensions Manager...**
- The extension options are now accessible via: **Tools -> Options -> LibreOffice Base -> Pure Java JDBC Driver**
- The extension options: **View system tables**, **Use bookmarks** and **Force SQL mode** will be searched in the information provided when connecting and will take precedence if present.
- Updated Trino driver to version 455.

### What has been done for version 1.4.5:

- Fix to allow the eMailerOOo extension to work properly in version 1.2.5.

### What has been done for version 1.4.6:

- Modification of the implementation of the interface UNO [XPropertySet][87]. This new implementation ensures the uniqueness of [Handle][88] for each property. Since this implementation is shared with the vCardOOo extension, **it makes all existing versions of vCardOOo obsolete**. It is based on three files:
  - [PropertySet.java][89]
  - [PropertySetAdapter.java][90]
  - [PropertyWrapper.java][91]
- Fixed issues in bookmark implementation. These modifications have been tested more particularly with the HsqlDB 2.7.4 and Jaybird 5.0.6 drivers.
- New implementation of the extension options and more specifically the **JDBC Driver Options** tab which should eventually allow the configuration from scratch of a JDBC driver to be able to work with LibreOffice Base. The JDBC driver archive update operation has been simplified. It supports updating drivers that require multiple jar archives to work (ie: Derby, Jaybird 6.x). This new window, which seems quite simple, actually requires quite complicated management, so please do not hesitate to report any malfunctions.
- Many other improvements.

### What has been done for version 1.5.0:

- Updated the [Python packaging][84] package to version 25.0.
- Updated the [Python setuptools][85] package to version 75.3.2.
- Updated the [Python six][92] package to version 1.17.0.
- Passive registration deployment that allows for much faster installation of extensions and differentiation of registered UNO services from those provided by a Java or Python implementation. This passive registration is provided by the [LOEclipse][93] extension via [PR#152][94] and [PR#157][95].
- Modified [LOEclipse][93] to support the new `rdb` file format produced by the `unoidl-write` compilation utility. `idl` files have been updated to support both available compilation tools: idlc and unoidl-write.
- Added support for [Java instrumentation][96] to LibreOffice with [Enhancement Request #165774][97] and then [PR#183280][98]. This will allow, starting with LibreOffice 25.8.x, access to logging for all JDBC drivers using `java.lang.System.Logger` as a logging facade. This new feature can be enabled in the extension options if the LibreOffice version allows it. I was refused to backport to LibreOffice 25.2.x so please be patient.
- All SQL, DDL, or DCL commands now come from the JDBC driver configuration file [Drivers.xcu][48]. The implementation of processing these commands and their parameters has been grouped under the package [io.github.prrvchr.driver.query][99].
- Compilation of all Java archives contained in the extension as modules and with **Java JDK version 17**.
- Updated all embedded JDBC drivers, except SQLite and Trino, to their respective latest versions supporting Java 17.
- Removed all idl files defining the following struct: Date, DateTime, DateTimeWithTimezone, DateWithTimezone, Duration, Time, and TimeWithTimezone. These files were required for compatibility with OpenOffice and are now replaced by the equivalent idl files from the LibreOffice API. **This change makes all versions of extensions using the previous version of jdbcDriverOOo incompatible**.
- User, role, and privilege management has been tested with all drivers built into jdbcDriverOOo, excluding SQLite and Trino.
- It is now possible to build the oxt file of the jdbcDriverOOo extension only with the help of Apache Ant and a copy of the GitHub repository. The [How to build the extension][100] section has been added to the documentation.
- Implemented [PEP 570][101] in [logging][102] to support unique multiple arguments.
- Any errors occurring while loading the driver will be logged in the extension's log if logging has been previously enabled. This makes it easier to identify installation problems on Windows.
- When JDBC drivers embedded with the jdbcDriverOOo extension are registered with `java.sql.DriverManager`, i.e. during the first connection requiring this driver, if this driver is already present in the Java class path then this will be detected, the driver not registred, the connection refused and the error recorded in the log.

### What has been done for version 1.5.1:

- **Java instrumentation is now required for jdbcDriverOOo to work properly.** For LibreOffice versions prior to 25.8.x, it is currently necessary to manually install Java instrumentation. A section explaining [How to install Java instrumentation][103] has been added to the documentation. If Java instrumentation is not present, loading the JDBC drivers will fail and an error message will be present in the log.
- Rewritten Java Service Provider Interface: `javax.sql.rowset.RowSetFactory`. This new SPI service is implemented using the [RowSetFactory.jar][104] archive, which is loaded using Java instrumentation. This new implementation has been modified to support:
    - Identifiers with mixed case in SQL queries.
    - Excluding auto-increment and/or calculated columns in insert queries (required by PostgreSQL).
    - Using the `java.sql.Statement.getGeneratedKeys()` interface after all inserts to retrieve the value of columns using values ​​generated by the underlying database (ie: auto-increment and/or calculated column).
    - Bringing the source code into compliance with the help of CheckStyle and the [checkstyle.xml][105] template. There is still work to be done.
    - Using `java.lang.System.Logger` as a logging facade.
    - Lots of small fixes needed for it to work properly because the basic implementation of the Java 11 SDK does not seem very functional to me, or even fanciful.
    - With this new implementation of [CachedRowSetImpl][106], all columns in the first table of a `java.sql.ResultSet` **are modifiable in Base**. Currently, only the table in the first column of the ResultSet is considered. However, it is required that the columns in the table of this result set meet the following criteria:
        - If these columns come from a table with primary keys then these keys must be part of the result set columns.
        - If these columns come from a table without a primary key, the records in a ResultSet will be editable if they are clearly identifiable by the ResultSet columns. Otherwise, an SQL exception will be thrown during any attempt to update or delete them.
    - This new CachedRowSet, running without a connection, must load the entire contents of the result set into memory and will ensure, before any modification or deletion, that the record has not been modified in the underlying database. If this is the case, an exception will be thrown and the operation will be rolled back.
    - The archive was compiled with Java 17 and as a module.
- We now have the option to use CachedRowSet or not. This option can be configured in the extension options. If this option is forced, then **it is even possible to edit queries in LibreOffice Base. Waw...**
- The ResultSets of the `getTables()`, `getTableTypes()`, `getTypeInfo()` and `getTablePrivileges()` methods of the XDatabaseMetaData interface now use a CachedRowSet whose data is updated according to the [Drivers.xcu][48] configuration file. This allow to achieve the expected results in LibreOffice Base with any underlying driver.
- Thanks to these configuration-dependent ResultSets, the system table display option works with any underlying driver and regardless of the API level used (sdbc, sdbcx, and sdb).
- It is now possible to get source code line numbers in Java traces thanks to the change in LOEclipse [PR#166][107].
- Fixed many regressions related to the last update which brought many changes.
- The new version of the SQLite driver is now compiled under Java 11 and uses `java.lang.System.Logger` as the logging facade, allowing access to it in LibreOffice. This is the only one that requires the use of the CachedRowSet option, otherwise Base will only display read-only tables and views.
- This seems to be the most significant update to JdbcDriverOOo, and I didn't expect it to get this far. The next step will be to integrate Trino and be able to run queries distributed across different databases in LibreOffice Base. `CachedRowSet` is exactly the building block I needed to be able to complete this.

### What has been done for version 1.5.2:

A regression in container management (tables, views, and columns) has been present since the last update. It comes from the new integration of the [ResultColumn][108] service, which requires containers capable of handling name duplication, as is sometimes the case for columns in a ResultSet. This addition caused an error in index management after deleting an item. This issue has just been fixed and implemented in both [ContainerBase.java][109] and [ContainerSuper.java][110] files.

### What has been done for version 1.5.3:

[JaybirdEmbedded][111] archive integration allows for true embedded mode for Jaybird. It is no longer necessary to install the Firebird Server to use Firebird in embedded mode (ie: `embedded:*`).

### What has been done for version 1.5.4:

In order to avoid any regressions on extensions using jdbcDriverOOo:
- `CachedRowSet` will only be used if Bookmarks are used.
- `CachedRowSet` will not be used for the `com.sun.star.sdbc` API level.

### What has been done for version 1.5.5:

- Now, `RowSetFactory` correctly displays and/or logs messages in French and its code is fully compliant with CheckStyle rules.
- Two new entries in the `Drivers.xcu` configuration file allow you to filter system catalogs and schemas if necessary and if they exist:
  - `SystemCatalogSettings`
  - `SystemSchemaSettings`
- As a result, the `getCatalogs()` and `getSchemas()` methods of the `XDatabaseMetaData` interface will be filtered respectively to display only the necessary entries when creating a table in Base.
- JDBC drivers can be added to the Java ClassPath when they are loaded. This option is even required for the Jaybird 6.0.2 driver to work properly in embedded mode.
- To ensure consistent driver loading, drivers are now loaded only using the `Class.forName()` method and then registered in `java.sql.DriverManager`.

### What has been done for version 1.5.6:

Integration of the [Oracle JDBC driver][112] `ojdbc17.jar`. This integration required the following modifications to the underlying code:
- Added two additional parameters to the `Drivers.xcu` file:
  - `QuotedMetaData`, which forces quotes on identifier names if they are not uppercase for method `DatabaseMetaData.getIndexInfo()`.
  - `CompletedMetaData`, which determines whether the underlying driver provides ResultSets with missing metadata.
- [QueryHelper][113] is now able to determine whether the executed SQL query is a `SELECT` query on a single table.
- If so, when constructing an XResultSet, [QueryHelper][113] provides the fully qualified table name used in the SQL `SELECT` query to the `CachedRowSet` emulating this XResultSet.
- When initializing this CachedRowSet, missing data from the Oracle driver's ResultSet metadata (ie: `getTableName(int index)` and `getSchemaName(int index)`) will be extracted from the table name and assigned to the CachedRowSet's metadata.

Due to these limitations of the Oracle driver, only ResultSets from SQL `SELECT` queries that apply to a single table will be editable in LibreOffice Base.

The implementation of containers for tables, views, columns, indexes, keys, users, groups and descriptors has been completely redesigned. Now containers delegate the management of their elements to three classes implementing the [BiMap][114] interface:
- [BiMapMain][115] allows element management using two `java.util.List` lists. This implementation will list elements in insertion order and allows duplicate management.
- [BiMapBase][116] allows element management using three `java.util.List` lists and one `java.util.Set` set. This implementation will list elements using a comparator that can take case-sensitive elements into account when sorting. It will reject duplicate insertions.
- [BiMapSuper][117] allows element management through the use of one list `java.util.List`, one set `java.util.Set` and one `BiMap`. This implementation allows to manage a sublist of elements coming from a `BiMap` interface implementation instance. It is this which ensures the management of groups and/or users for a given group and/or user.

As for the containers themselves, they now use one of the previous classes implementing `BiMap` depending on their needs. This choice is made in one of the following four containers and according to their class inheritance level:
- [ContainerMain][118] uses `BiMapMain` and implements the UNO interfaces: `XNameAccess`, `XIndexAccess` and `XEnumerationAccess`. It allows management of `ResultColumn` elements.
- [ContainerBase][119] extends the previous class and implements the UNO interfaces: `XAppend`, `XDrop`, `XDataDescriptorFactory` and `XRefreshable`. This container has the particularity of allowing addition and deletion, and allows management of `Column`, `Index`, `Key` elements and their associated `Descriptor` services.
- [ContainerSuper][120] uses `BiMapBase` and extends the previous class. It does not implement any additional interfaces and allows the management of the following elements: `Table`, `View`, `User`, and `Group`.
- [RoleContainer][121] uses `BiMapSuper` and extends the previous class. It is simply a facade for filtering the contents of the `UserContainer` and `GroupContainer` containers to allow the management of users and roles belonging to a group and/or user.

The creation of the three classes implementing the `BiMap` interface now allows all containers to inherit from the parent `ContainerMain` class, which was not possible until now. This greatly simplifies the implementation of containers, which have been able to get rid of all this machinery. Everything has become so much simpler that I don't understand why I didn't think of it sooner?  
Furthermore, regarding users and roles, this new implementation will guarantee:
- That there is only one instance of the `Group` or `User` class per user or role, regardless of their access.
- That any necessary updates following the deletion of a user or role are performed by a new [RoleListener][122].

On this same principle, it would be possible to have only one instance of a loaded column, whether it is accessed through a `Table` or a `ResultSet`. Something to think about...

The refresh management following the creation or deletion of an element has problems in LibreOffice Base, see issue [tdf#167920][123]. I don't know yet how to proceed to get rid of this. Use of listener or that Base uses the `XRefresable` interface supported by the containers after any modification requiring it. In the second case it is the code of LibreOffice Base which remains to be improved. In the meantime, to work around this problem I advise you to manually refresh LibreOffice Base via the menu **View -> Refresh tables** after any insertion or deletion.

Many small fixes:
- `CachedRowSet` now allows inserting records with null values if the columns allow it.
- The `cancelRowUpdates` method of the `CachedRowSetImpl` class now supports execution on an empty `RowSet`. This is necessary to work around issue [tdf#167434][124].
- It is again possible to add a column to an existing table with SQLite.

Supporting an additional driver like Oracle's requires a lot of work for functionality testing. I'm counting on you to report any issues, as tracking down these issues is the most time-consuming task. Thanks in advance.

If you use multiple accounts to connect to a database, you will not be able to reconnect to that database again if you opened it with an account other than the one offered and then closed it without saving the file. You must restart LibreOffice. See [tdf#167960][125].

### What has been done for version 1.6.0:

The naming logic for elements requiring compound names, such as tables, views, and columns, has been modified:
- The naming rules [ComposeRule][126] have been extended with two new rules:
  - `InSelectDefinitions` defines the naming rule when composing SQL queries. Two parameters from the configuration file, `UseCatalogInSelect` and `UseSchemaInSelect`, allow you to influence the behavior of this rule.
  - `InViewDefinitions` defines the naming rule when composing a view. Two new parameters from the configuration file, `UseCatalogInView` and `UseSchemaInView`, allow you to influence the behavior of this rule.
- In the absence of parameters in the configuration file, these two new rules will follow the `InTableDefinitions` rule.
- The `InViewDefinitions` rule has been added to allow the creation of views in SQL Server.
- A `ComposeRule` rule allows for [NamedSupport][127] naming support, which can provide a unique name following this rule from a [NamedComponent][128] name composition, and vice versa. All this logic is now grouped into the [ComponentHelper][129] file.

Integration of the [SQL Server JDBC driver][130] `mssql-jdbc-13.2.0.jre11.jar`. This integration required the following modifications to the underlying code:
- Using `UseCatalogInView` parameters in `Drivers.xcu` file with false value.
- Use of the new `InViewDefinitions` naming rule when creating a view to exclude the catalog name from the view name.
- With these changes, the catalog, which is actually the database name in SQL Server, will not be used to name the identifiers of SQL queries managing view creation, as required by the SQL Server JDBC driver.
- Recompilation of SQL Server 13.2.0 driver under Java 17 with correction of [issue#2745][131] to allow the use of relationship management in LibreOffice Base.

Testing of the [UCanAccess JDBC driver][132] allowing reading and writing of Microsoft Access files. This driver is included in this new version, but its integration remains to be finalized.

If Java instrumentation is not installed, jdbcDriverOOo will operate in degraded mode, with many features disabled. A SQL warning message will be issued upon connection, and all extensions using jdbcDriverOOo will display a warning message in their Options dialog.

Fixed the issue with using multiple login accounts with Base [tdf#167960][125]. A fix [fix#189732][133] will be available with LibreOffice 26.2.x and will make Base truly multi-user.

### What remains to be done for version 1.6.0:

- Add new languages for internationalization...

- Anything welcome...

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/CHANGELOG_fr>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/>
[5]: <https://forum.openoffice.org/en/forum/viewtopic.php?f=13&t=103912>
[6]: <http://hsqldb.org/>
[7]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/Driver.java>
[8]: <https://github.com/hanya/MRI>
[9]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdb>
[10]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc>
[11]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx>
[12]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-HsqlDB/source/io/github/prrvchr/jdbcdriver/hsqldb>
[13]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-H2/source/io/github/prrvchr/jdbcdriver/h2>
[14]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-Derby/source/io/github/prrvchr/jdbcdriver/derby>
[15]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Driver.java>
[16]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/UnoLogger/source/io/github/prrvchr/uno/logging>
[17]: <https://www.slf4j.org/>
[18]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/logging/module-ix.html>
[19]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/options>
[20]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/admin>
[21]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/user>
[22]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/group>
[23]: <https://github.com/xerial/sqlite-jdbc>
[24]: <https://github.com/gotson>
[25]: <https://github.com/xerial/sqlite-jdbc/issues/786>
[26]: <https://prrvchr.github.io/HyperSQLOOo>
[27]: <https://prrvchr.github.io/SQLiteOOo>
[28]: <https://mariadb.com/downloads/connectors/connectors-data-access/java8-connector/>
[29]: <https://www.h2database.com/html/main.html>
[30]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetBase.java>
[31]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ResultSetSuper.java>
[32]: <https://bugs.documentfoundation.org/show_bug.cgi?id=156512>
[33]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/JDBCConnectionProperties.html#TypeInfoSettings>
[34]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/metadata/TypeInfoResultSet.java>
[35]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu#L332>
[36]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DatabaseMetaDataBase.java#L444>
[37]: <https://github.com/artem78>
[38]: <https://github.com/prrvchr/jdbcDriverOOo/issues/4>
[39]: <https://github.com/prrvchr/HyperSQLOOo/issues/1>
[40]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/requirements.txt>
[41]: <https://peps.python.org/pep-0508/>
[42]: <https://prrvchr.github.io/jdbcDriverOOo/#requirement>
[43]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/XGeneratedResultSet.html>
[44]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XAlterTable.html>
[45]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.3-SNAPSHOT.jar>
[46]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.6-SNAPSHOT.jar>
[47]: <https://jdbc.postgresql.org/>
[48]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu>
[49]: <https://jira.mariadb.org/browse/CONJ-1160>
[50]: <https://en.wikibooks.org/wiki/Java_Programming/Generics>
[51]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/TableContainerSuper.java>
[52]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ViewContainer.java>
[53]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ColumnContainerBase.java>
[54]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/KeyContainer.java>
[55]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/IndexContainer.java>
[56]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Container.java>
[57]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/helper/TableHelper.java#L199>
[58]: <https://github.com/FirebirdSQL/jaybird/issues/791>
[59]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/helper/TableHelper.java#L490>
[60]: <https://bugs.documentfoundation.org/show_bug.cgi?id=160375>
[61]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/module-ix.html>
[62]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/module-ix.html>
[63]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdb/module-ix.html>
[64]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XRowLocate.html>
[65]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/resultset>
[66]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/RoleContainer.java>
[67]: <https://bugs.documentfoundation.org/show_bug.cgi?id=160516>
[68]: <https://hsqldb.org/doc/guide/management-chapt.html#mtc_system_versioned_tables>
[69]: <https://hsqldb.org/doc/guide/texttables-chapt.html#ttc_table_definition>
[70]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DriverBase.java#L160>
[71]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/provider/DriverManagerHelper.java>
[72]: <https://prrvchr.github.io/JaybirdOOo/>
[73]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/resultset/ScrollableResultSet.java>
[74]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/resultset/SensitiveResultSet.java>
[75]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/rowset/RowSetWriter.java>
[76]: <https://github.com/prrvchr/jdbcDriverOOo/issues/8>
[77]: <https://trino.io/>
[78]: <https://github.com/trinodb/trino/issues/22306>
[79]: <https://github.com/prrvchr/jdbcDriverOOo/issues/7>
[80]: <https://github.com/trinodb/trino/issues/22408>
[81]: <https://sourceforge.net/p/hsqldb/feature-requests/368/>
[82]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/resultset/CachedResultSet.java>
[83]: <https://github.com/prrvchr/jdbcDriverOOo/issues/8#issuecomment-2182445391>
[84]: <https://pypi.org/project/packaging/>
[85]: <https://pypi.org/project/setuptools/>
[86]: <https://github.com/prrvchr/jdbcDriverOOo/pull/9>
[87]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/beans/XPropertySet.html>
[88]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/beans/Property.html#Handle>
[89]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/UnoHelper/source/io/github/prrvchr/uno/helper/PropertySet.java>
[90]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/UnoHelper/source/io/github/prrvchr/uno/helper/PropertySetAdapter.java>
[91]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/UnoHelper/source/io/github/prrvchr/uno/helper/PropertyWrapper.java>
[92]: <https://pypi.org/project/six/>
[93]: <https://github.com/LibreOffice/loeclipse>
[94]: <https://github.com/LibreOffice/loeclipse/pull/152>
[95]: <https://github.com/LibreOffice/loeclipse/pull/157>
[96]: <https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/Instrumentation.html>
[97]: <https://bugs.documentfoundation.org/show_bug.cgi?id=165774>
[98]: <https://gerrit.libreoffice.org/c/core/+/183280>
[99]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/query>
[100]: <https://prrvchr.github.io/jdbcDriverOOo/#how-to-build-the-extension>
[101]: <https://peps.python.org/pep-0570/>
[102]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/uno/lib/uno/logger/logwrapper.py#L109>
[103]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#comment-installer-linstrumentation-java>
[104]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/RowSetFactory/dist/RowSetFactory.jar>
[105]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/checkstyle.xml>
[106]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/RowSetFactory/source/io/github/prrvchr/java/rowset/CachedRowSetImpl.java>
[107]: <https://github.com/LibreOffice/loeclipse/pull/166>
[108]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdb/ResultColumn.html>
[109]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ContainerBase.java>
[110]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ContainerSuper.java>
[111]: <https://prrvchr.github.io/JaybirdEmbedded/>
[112]: <https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html>
[113]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/driver/helper/QueryHelper.java>
[114]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/driver/container/BiMap.java>
[115]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/driver/container/BiMapMain.java>
[116]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/driver/container/BiMapBase.java>
[117]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/driver/container/BiMapSuper.java>
[118]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ContainerMain.java>
[119]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ContainerBase.java>
[120]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ContainerSuper.java>
[121]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/RoleContainer.java>
[122]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/RoleListener.java>
[123]: <https://bugs.documentfoundation.org/show_bug.cgi?id=167920>
[124]: <https://bugs.documentfoundation.org/show_bug.cgi?id=167434>
[125]: <https://bugs.documentfoundation.org/show_bug.cgi?id=167960>
[126]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/driver/provider/ComposeRule.java>
[127]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/driver/helper/ComponentHelper.java#176>
[128]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/driver/helper/ComponentHelper.java#218>
[129]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/driver/helper/ComponentHelper.java>
[130]: <https://github.com/prrvchr/mssql-jdbc>
[131]: <https://github.com/microsoft/mssql-jdbc/issues/2745>
[132]: <https://github.com/spannm/ucanaccess>
[133]: <https://gerrit.libreoffice.org/c/core/+/189732>
