#!
# -*- coding: utf_8 -*-

import uno
import unohelper

from com.sun.star.lang import XComponent
from com.sun.star.util import XCancellable
from com.sun.star.sdbc import XConnection
from com.sun.star.sdbc import XCloseable
from com.sun.star.sdbc import XWarningsSupplier
from com.sun.star.sdbc import XDatabaseMetaData2
from com.sun.star.sdbc import XResultSetMetaData
from com.sun.star.sdb import XCommandPreparation
from com.sun.star.sdb import XQueriesSupplier
from com.sun.star.sdb import XSQLQueryComposerFactory
from com.sun.star.lang import XMultiServiceFactory
from com.sun.star.container import XChild
from com.sun.star.sdb.application import XTableUIProvider
from com.sun.star.sdb.tools import XConnectionTools

from com.sun.star.sdbcx import XTablesSupplier
from com.sun.star.sdbcx import XViewsSupplier
from com.sun.star.sdbcx import XUsersSupplier
from com.sun.star.sdbcx import XGroupsSupplier

from com.sun.star.sdb import XCompletedConnection
from com.sun.star.sdbc import XIsolatedConnection
from com.sun.star.util import XFlushable
from com.sun.star.sdb import XQueryDefinitionsSupplier
from com.sun.star.sdb import XBookmarksSupplier
from com.sun.star.sdbc import XDataSource

from com.sun.star.sdbc import XStatement
from com.sun.star.sdbc import XBatchExecution
from com.sun.star.sdbc import XPreparedBatchExecution
from com.sun.star.sdbc import XMultipleResults
from com.sun.star.sdbc import XPreparedStatement
from com.sun.star.sdbc import XResultSetMetaDataSupplier
from com.sun.star.sdbc import XParameters
from com.sun.star.sdbc import XResultSet
from com.sun.star.sdbc import XRow
from com.sun.star.sdbc import XOutParameters
from com.sun.star.sdbc import XColumnLocate
from com.sun.star.sdbc import SQLException
from com.sun.star.sdbcx import XColumnsSupplier
from com.sun.star.sdbc.ResultSetType import SCROLL_INSENSITIVE
from com.sun.star.sdbc.ResultSetType import SCROLL_SENSITIVE
from com.sun.star.sdbc.ResultSetType import FORWARD_ONLY
from com.sun.star.sdbc.ResultSetConcurrency import READ_ONLY
from com.sun.star.sdbc.ResultSetConcurrency import UPDATABLE
from com.sun.star.beans.PropertyAttribute import BOUND
from com.sun.star.beans.PropertyAttribute import READONLY

from unolib import PropertySet
from unolib import getProperty

import traceback


class Connection(unohelper.Base,
                 XComponent,
                 XWarningsSupplier,
                 XConnection,
                 XCloseable,
                 XCommandPreparation,
                 XQueriesSupplier,
                 XSQLQueryComposerFactory,
                 XMultiServiceFactory,
                 XChild,
                 XTablesSupplier,
                 XViewsSupplier,
                 XUsersSupplier,
                 XGroupsSupplier,
                 XTableUIProvider,
                 XConnectionTools):
    def __init__(self, ctx, datasource, user, protocols):
        self.ctx = ctx
        self.datasource = datasource
        self.connection = user.Connection
        self.protocols = protocols
        self.userid = user.People
        self.username = user.Resource
        self.listeners = []
        event = uno.createUnoStruct('com.sun.star.lang.EventObject')
        event.Source = self
        self.event = event

    # XComponent
    def dispose(self):
        print("Connection.dispose()")
        for listener in self.listeners:
            litener.disposing(self.event)
    def addEventListener(self, listener):
        print("Connection.addEventListener()")
        self.listeners.append(listener)
    def removeEventListener(self, listener):
        print("Connection.removeEventListener()")
        if listener in self.listeners:
            self.listeners.remove(listener)

    # XTableUIProvider
    def getTableIcon(self, tablename, colormode):
        print("Connection.getTableIcon()")
        return self.connection.getTableIcon(tablename, colormode)
    def getTableEditor(self, documentui, tablename):
        print("Connection.getTableEditor()")
        return self.connection.getTableEditor(documentui, tablename)

    # XConnectionTools
    def createTableName(self):
        print("Connection.createTableName()")
        return self.connection.createTableName()
    def getObjectNames(self):
        print("Connection.getObjectNames()")
        return self.connection.getObjectNames()
    def getDataSourceMetaData(self):
        print("Connection.getDataSourceMetaData()")
        return self.connection.getDataSourceMetaData()
    def getFieldsByCommandDescriptor(self, commandtype, command, keep):
        print("Connection.getFieldsByCommandDescriptor() 1")
        fields, keep = self.connection.getFieldsByCommandDescriptor(commandtype, command, keep)
        print("Connection.getFieldsByCommandDescriptor() 2")
        return fields, keep
    def getComposer(self, commandtype, command):
        print("Connection.getComposer()")
        return self.connection.getComposer(commandtype, command)

    # XCloseable
    def close(self):
        print("Connection.close()********* 1")
        #self.datasource.closeConnection(self.connection, self.userid)
        print("Connection.close()********* 2")

    # XCommandPreparation
    def prepareCommand(self, command, commandtype):
        print("Connection.prepareCommand()")
        return self.connection.prepareCommand(command, commandtype)
    # XQueriesSupplier
    def getQueries(self):
        print("Connection.getQueries()")
        return self.connection.getQueries()
    # XSQLQueryComposerFactory
    def createQueryComposer(self):
        print("Connection.getQueries()")
        return self.connection.createQueryComposer()
    # XMultiServiceFactory
    def createInstance(self, service):
        print("Connection.createInstance()")
        return self.connection.createInstance(service)
    def createInstanceWithArguments(self, service, arguments):
        print("Connection.createInstanceWithArguments()")
        return self.connection.createInstanceWithArguments(service, arguments)
    def getAvailableServiceNames(self):
        print("Connection.getAvailableServiceNames()")
        return self.connection.getAvailableServiceNames()
    # XChild
    def getParent(self):
        return DataSource(self, self.protocols, self.username)
    def setParent(self):
        pass
    # XTablesSupplier
    def getTables(self):
        print("Connection.getTables()")
        return self.connection.getTables()
    # XViewsSupplier
    def getViews(self):
        print("Connection.getViews()")
        return self.connection.getViews()
    # XUsersSupplier
    def getUsers(self):
        print("Connection.getUsers()")
        return self.connection.getUsers()
    # XGroupsSupplier
    def getGroups(self):
        print("Connection.getGroups()")
        return self.connection.getGroups()

    # XWarningsSupplier
    def getWarnings(self):
        print("Connection.getWarnings() 1")
        warning = self.connection.getWarnings()
        print("Connection.getWarnings() 2 %s" % warning)
        return warning
    def clearWarnings(self):
        print("Connection.clearWarnings()")
        self.connection.clearWarnings()

    # XConnection
    def createStatement(self):
        print("Connection.createStatement()")
        return Statement(self)
    def prepareStatement(self, sql):
        print("Connection.prepareStatement(): %s" % sql)
        statement = PreparedStatement(self, sql)
        #mri = self.ctx.ServiceManager.createInstance('mytools.Mri')
        #mri.inspect(statement)
        return statement
    def prepareCall(self, sql):
        print("Connection.prepareCall(): %s" % sql)
        return CallableStatement(self, sql)
    def nativeSQL(self, sql):
        print("Connection.nativeSQL()")
        return self.connection.nativeSQL(sql)
    def setAutoCommit(self, auto):
        print("Connection.setAutoCommit()")
        self.connection.setAutoCommit(auto)
    def getAutoCommit(self):
        print("Connection.getAutoCommit()")
        return self.connection.getAutoCommit()
    def commit(self):
        print("Connection.commit()")
        self.connection.commit()
    def rollback(self):
        print("Connection.rollback()")
        self.connection.rollback()
    def isClosed(self):
        print("Connection.isClosed()")
        return self.connection.isClosed()
    def getMetaData(self):
        print("Connection.getMetaData()")
        metadata = self.connection.getMetaData()
        dbdata = DatabaseMetaData(self, metadata, self.protocols, self.username)
        #mri = self.ctx.ServiceManager.createInstance('mytools.Mri')
        #mri.inspect(dbdata)
        return dbdata
    def setReadOnly(self, readonly):
        print("Connection.setReadOnly()")
        self.connection.setReadOnly(readonly)
    def isReadOnly(self):
        print("Connection.isReadOnly()")
        return self.connection.isReadOnly()
    def setCatalog(self, catalog):
        print("Connection.setCatalog()")
        self.connection.setCatalog(catalog)
    def getCatalog(self):
        print("Connection.getCatalog()")
        return self.connection.getCatalog()
    def setTransactionIsolation(self, level):
        print("Connection.setTransactionIsolation()")
        self.connection.setTransactionIsolation(level)
    def getTransactionIsolation(self):
        print("Connection.getTransactionIsolation()")
        return self.connection.getTransactionIsolation()
    def getTypeMap(self):
        print("Connection.getTypeMap()")
        return self.connection.getTypeMap()
    def setTypeMap(self, typemap):
        print("Connection.setTypeMap()")
        self.connection.setTypeMap(typemap)


class DataSource(unohelper.Base,
                 XCompletedConnection,
                 XIsolatedConnection,
                 XFlushable,
                 XQueryDefinitionsSupplier,
                 XBookmarksSupplier,
                 XDataSource,
                 PropertySet):
    def __init__(self, connection, protocols, username):
        self.connection = connection
        self.protocols = protocols
        self.username = username

    @property
    def Name(self):
        return self.connection.connection.Parent.Name
    @property
    def URL(self):
        return ':'.join(self.protocols)
    @URL.setter
    def URL(self, url):
        self.connection.connection.Parent.URL = url
    @property
    def Info(self):
        return self.connection.connection.Parent.Info
    @Info.setter
    def Info(self, info):
        self.connection.connection.Parent.Info = info
    @property
    def Settings(self):
        return self.connection.connection.Parent.Settings
    @property
    def User(self):
        return self.username
    @User.setter
    def User(self, user):
        pass
        #self.connection.connection.Parent.User = user
    @property
    def Password(self):
        return self.connection.connection.Parent.Password
    @Password.setter
    def Password(self, password):
        self.connection.connection.Parent.Password = password
    @property
    def IsPasswordRequired(self):
        return self.connection.connection.Parent.IsPasswordRequired
    @IsPasswordRequired.setter
    def IsPasswordRequired(self, state):
        self.connection.connection.Parent.IsPasswordRequired = state
    @property
    def SuppressVersionColumns(self):
        return self.connection.connection.Parent.SuppressVersionColumns
    @SuppressVersionColumns.setter
    def SuppressVersionColumns(self, state):
        self.connection.connection.Parent.SuppressVersionColumns = state
    @property
    def IsReadOnly(self):
        return self.connection.connection.Parent.IsReadOnly
    @property
    def NumberFormatsSupplier(self):
        return self.connection.connection.Parent.NumberFormatsSupplier
    @property
    def TableFilter(self):
        return self.connection.connection.Parent.TableFilter
    @TableFilter.setter
    def TableFilter(self, filter):
        self.connection.connection.Parent.TableFilter = filter
    @property
    def TableTypeFilter(self):
        return self.connection.connection.Parent.TableTypeFilter
    @TableTypeFilter.setter
    def TableTypeFilter(self, filter):
        self.connection.connection.Parent.TableTypeFilter = filter

    # XCompletedConnection
    def connectWithCompletion(self, handler):
        return self.connection.connection.Parent.connectWithCompletion(handler)
    # XIsolatedConnection
    def getIsolatedConnectionWithCompletion(self, handler):
        return self.connection.connection.Parent.getIsolatedConnectionWithCompletion(handler)
    def getIsolatedConnection(self, user, pasword):
        return self.connection.connection.Parent.getIsolatedConnection(user, pasword)
    # XFlushable
    def flush(self):
        self.connection.connection.Parent.flush()
    def addFlushListener(self, listener):
        self.connection.connection.Parent.addFlushListenerlistener(listener)
    def removeFlushListener(self, listener):
        self.connection.connection.Parent.removeFlushListener(listener)
    # XQueryDefinitionsSupplier
    def getQueryDefinitions(self):
        return self.connection.connection.Parent.getQueryDefinitions()
    # XBookmarksSupplier
    def getBookmarks(self):
        return self.connection.connection.Parent.getBookmarks()
    # XDataSource
    def getConnection(self, user, password):
        return self.connection
    def setLoginTimeout(self, timeout):
        self.connection.connection.Parent.setLoginTimeout(timeout)
    def getLoginTimeout(self):
        return self.connection.connection.Parent.getLoginTimeout()

    # XPropertySet
    def _getPropertySetInfo(self):
        properties = {}
        properties['Name'] = getProperty('Name', 'string', READONLY)
        properties['URL'] = getProperty('URL', 'string', BOUND)
        infotype = '[]com.sun.star.beans.PropertyValue'
        properties['Info'] = getProperty('Info', infotype, BOUND)
        settingtype = '[]com.sun.star.beans.XPropertySet'
        properties['Settings'] = getProperty('Settings', settingtype, READONLY)
        properties['User'] = getProperty('User', 'string', BOUND)
        properties['Password'] = getProperty('Password', 'string', BOUND)
        properties['IsPasswordRequired'] = getProperty('IsPasswordRequired', 'boolean', BOUND)
        properties['SuppressVersionColumns'] = getProperty('SuppressVersionColumns', 'boolean', BOUND)
        properties['IsReadOnly'] = getProperty('IsReadOnly', 'boolean', READONLY)
        numbertype = 'com.sun.star.util.XNumberFormatsSupplier'
        properties['NumberFormatsSupplier'] = getProperty('NumberFormatsSupplier', numbertype, READONLY)
        properties['TableFilter'] = getProperty('TableFilter', '[]string', BOUND)
        properties['TableTypeFilter'] = getProperty('TableTypeFilter', '[]string', BOUND)
        return properties


class BaseStatement(unohelper.Base,
                    XCloseable,
                    XWarningsSupplier,
                    XMultipleResults,
                    XCancellable,
                    PropertySet):

    @property
    def QueryTimeOut(self):
        return self.statement.QueryTimeOut
    @QueryTimeOut.setter
    def QueryTimeOut(self, timeout):
        self.statement.QueryTimeOut = timeout

    @property
    def MaxFieldSize(self):
        return self.statement.MaxFieldSize
    @MaxFieldSize.setter
    def MaxFieldSize(self, size):
        self.statement.MaxFieldSize = size

    @property
    def MaxRows(self):
        return self.statement.MaxRows
    @MaxRows.setter
    def MaxRows(self, row):
        self.statement.MaxRows = row

    @property
    def CursorName(self):
        return self.statement.CursorName
    @CursorName.setter
    def CursorName(self, name):
        self.statement.CursorName = name

    @property
    def ResultSetConcurrency(self):
        return self.statement.ResultSetConcurrency
    @ResultSetConcurrency.setter
    def ResultSetConcurrency(self, constant):
        self.statement.ResultSetConcurrency = constant

    @property
    def ResultSetType(self):
        return self.statement.ResultSetType
    @ResultSetType.setter
    def ResultSetType(self, constant):
        self.statement.ResultSetType = constant

    @property
    def FetchDirection(self):
        return self.statement.FetchDirection
    @FetchDirection.setter
    def FetchDirection(self, row):
        self.statement.FetchDirection = row

    @property
    def FetchSize(self):
        return self.statement.FetchSize
    @FetchSize.setter
    def FetchSize(self, size):
        self.statement.FetchSize = size

    @property
    def UseBookmarks(self):
        return self.statement.UseBookmarks
    @UseBookmarks.setter
    def UseBookmarks(self, state):
        self.statement.UseBookmarks = state

    # XCloseable
    def close(self):
        print("BaseStatement.close()")
        self.statement.close()

    # XCancellable
    def cancel(self):
        print("BaseStatement.cancel()")
        self.statement.cancel()

    # XWarningsSupplier
    def getWarnings(self):
        print("BaseStatement.getWarnings() 1")
        warning = self.statement.getWarnings()
        print("BaseStatement.getWarnings() 2 %s" % warning)
        return warning
    def clearWarnings(self):
        print("BaseStatement.clearWarnings()")
        self.statement.clearWarnings()

    # XMultipleResults
    def getResultSet(self):
        print("BaseStatement.getResultSet()")
        return self.statement.getResultSet()
    def getUpdateCount(self):
        print("BaseStatement.getUpdateCount()")
        return self.statement.getUpdateCount()
    def getMoreResults(self):
        print("BaseStatement.getMoreResults()")
        return self.statement.getMoreResults()

   # XBatchExecution / XPreparedBatchExecution
    def addBatch(self, sql):
        self.statement.addBatch(sql)
    def clearBatch(self):
        self.statement.clearBatch()
    def executeBatch(self):
        return self.statement.executeBatch()

    # XPropertySet
    def _getPropertySetInfo(self):
        properties = {}
        properties['QueryTimeOut'] = getProperty('QueryTimeOut', 'long', BOUND)
        properties['MaxFieldSize'] = getProperty('MaxFieldSize', 'long', BOUND)
        properties['MaxRows'] = getProperty('MaxRows', 'long', BOUND)
        properties['CursorName'] = getProperty('CursorName', 'string', BOUND)
        properties['ResultSetConcurrency'] = getProperty('ResultSetConcurrency', 'long', BOUND)
        properties['ResultSetType'] = getProperty('ResultSetType', 'long', BOUND)
        properties['FetchDirection'] = getProperty('FetchDirection', 'long', BOUND)
        properties['FetchSize'] = getProperty('FetchSize', 'long', BOUND)
        properties['UseBookmarks'] = getProperty('UseBookmarks', 'boolean', BOUND)
        return properties


class Statement(BaseStatement,
                XStatement,
                XBatchExecution):
    def __init__(self, connection):
        self.connection = connection
        self.statement = connection.connection.createStatement()

    @property
    def EscapeProcessing(self):
        return self.statement.EscapeProcessing
    @EscapeProcessing.setter
    def EscapeProcessing(self, state):
        self.statement.EscapeProcessing = state

    # XStatement
    def executeQuery(self, sql):
        print("Connection.Statement.executeQuery(): %s" % sql)
        result = self.statement.executeQuery(sql)
        #result = ResultSet(self, sql)
        return result
    def executeUpdate(self, sql):
        return self.statement.executeUpdate(sql)
    def execute(self, sql):
        return self.statement.execute(sql)
    def getConnection(self):
        print("Connection.Statement.getConnection()")
        return self.connection

    # XPropertySet
    def _getPropertySetInfo(self):
        properties = {}
        #properties['QueryTimeOut'] = getProperty('QueryTimeOut', 'long', BOUND)
        properties['MaxFieldSize'] = getProperty('MaxFieldSize', 'long', BOUND)
        properties['MaxRows'] = getProperty('MaxRows', 'long', BOUND)
        #properties['CursorName'] = getProperty('CursorName', 'string', BOUND)
        properties['ResultSetConcurrency'] = getProperty('ResultSetConcurrency', 'long', BOUND)
        properties['ResultSetType'] = getProperty('ResultSetType', 'long', BOUND)
        properties['FetchDirection'] = getProperty('FetchDirection', 'long', BOUND)
        properties['FetchSize'] = getProperty('FetchSize', 'long', BOUND)
        properties['EscapeProcessing'] = getProperty('EscapeProcessing', 'boolean', BOUND)
        properties['UseBookmarks'] = getProperty('UseBookmarks', 'boolean', BOUND)
        return properties


class PreparedStatement(BaseStatement,
                        XPreparedStatement,
                        XResultSetMetaDataSupplier,
                        XParameters,
                        XColumnsSupplier,
                        XPreparedBatchExecution):
    def __init__(self, connection, sql):
        self.connection = connection
        self.sql = sql
        self.statement = connection.connection.prepareStatement(sql)
        self.datasource = connection.datasource
        #self.statement.ResultSetConcurrency = READ_ONLY
        #self.statement.ResultSetType = SCROLL_INSENSITIVE

    # XColumnsSupplier
    def getColumns(self):
        return self.statement.getColumns()

    # XPreparedStatement
    def executeQuery(self):
        # TODO: cannot use: result = self.statement.executeQuery()
        # TODO: it trow a: java.lang.IncompatibleClassChangeError
        # TODO: fallback to: self.statement.execute()
        print("Connection.PreparedStatement.executeQuery() hack")
        if self.statement.execute():
            print("Connection.PreparedStatement.executeQuery() hack")
            return self.statement.getResultSet()
        raise SQLException()
    def executeUpdate(self):
        return self.statement.executeUpdate()
    def execute(self):
        return self.statement.execute()
    def getConnection(self):
        print("Connection.PreparedStatement.getConnection()")
        return self.connection

    # XResultSetMetaDataSupplier
    def getMetaData(self):
        print("Connection.PreparedStatement.getMetaData()")
        metadata = ResultSetMetaData(self.statement)
        return metadata

    # XParameters
    def setNull(self, index, sqltype):
        print("PreparedStatement.setNull()")
        self.statement.setNull(index, sqltype)
    def setObjectNull(self, index, sqltype, typename):
        self.statement.setObjectNull(index, sqltype, typename)
    def setBoolean(self, index, value):
        print("PreparedStatement.setBoolean()")
        self.statement.setBoolean(index, value)
    def setByte(self, index, value):
        self.statement.setByte(index, value)
    def setShort(self, index, value):
        print("PreparedStatement.setShort()")
        self.statement.setShort(index, value)
    def setInt(self, index, value):
        print("PreparedStatement.setInt()")
        self.statement.setInt(index, value)
    def setLong(self, index, value):
        print("PreparedStatement.setLong()")
        self.statement.setLong(index, value)
    def setFloat(self, index, value):
        print("PreparedStatement.setFloat()")
        self.statement.setFloat(index, value)
    def setDouble(self, index, value):
        print("PreparedStatement.setDouble()")
        self.statement.setDouble(index, value)
    def setString(self, index, value):
        print("PreparedStatement.setString()")
        self.statement.setString(index, value)
    def setBytes(self, index, value):
        self.statement.setBytes(index, value)
    def setDate(self, index, value):
        self.statement.setDate(index, value)
    def setTime(self, index, value):
        self.statement.setTime(index, value)
    def setTimestamp(self, index, value):
        self.statement.setTimestamp(index, value)
    def setBinaryStream(self, index, value, length):
        self.statement.setBinaryStream(index, value, length)
    def setCharacterStream(self, index, value, length):
        self.statement.setCharacterStream(index, value, length)
    def setObject(self, index, value):
        print("PreparedStatement.setObject()")
        self.statement.setObject(index, value)
    def setObjectWithInfo(self, index, value, sqltype, scale):
        self.statement.setObjectWithInfo(index, value, sqltype, scale)
    def setRef(self, index, value):
        self.statement.setRef(index, value)
    def setBlob(self, index, value):
        self.statement.setBlob(index, value)
    def setClob(self, index, value):
        self.statement.setClob(index, value)
    def setArray(self, index, value):
        self.statement.setArray(index, value)
    def clearParameters(self):
        self.statement.clearParameters()


class CallableStatement(PreparedStatement,
                        XOutParameters,
                        XRow):
    def __init__(self, connection, sql):
        self.connection = connection
        self.sql = sql
        self.statement = connection.connection.prepareCall(sql)

    # XOutParameters
    def registerOutParameter(self, index, sqltype, typename):
        self.statement.registerOutParameter(index, sqltype, typename)
    def registerNumericOutParameter(self, index, sqltype, scale):
        self.statement.registerNumericOutParameter(index, sqltype, scale)

    # XRow
    def wasNull(self):
        return self.statement.wasNull()
    def getString(self, index):
        print("CallableStatement.getString()")
        return self.statement.getString(index)
    def getBoolean(self, index):
        return self.statement.getBoolean(index)
    def getByte(self, index):
        return self.statement.getByte(index)
    def getShort(self, index):
        return self.statement.getShort(index)
    def getInt(self, index):
        return self.statement.getInt(index)
    def getLong(self, index):
        return self.statement.getLong(index)
    def getFloat(self, index):
        return self.statement.getFloat(index)
    def getDouble(self, index):
        return self.statement.getDouble(index)
    def getBytes(self, index):
        return self.statement.getBytes(index)
    def getDate(self, index):
        return self.statement.getDate(index)
    def getTime(self, index):
        return self.statement.getTime(index)
    def getTimestamp(self, index):
        return self.statement.getTimestamp(index)
    def getBinaryStream(self, index):
        return self.statement.getBinaryStream(index)
    def getCharacterStream(self, index):
        return self.statement.getCharacterStream(index)
    def getObject(self, index, typemap):
        print("CallableStatement.getObject()")
        return self.statement.getObject(index, typemap)
    def getRef(self, index):
        return self.statement.getRef(index)
    def getBlob(self, index):
        return self.statement.getBlob(index)
    def getClob(self, index):
        return self.statement.getClob(index)
    def getArray(self, index):
        return self.statement.getArray(index)


class ResultSet(unohelper.Base,
                XResultSet,
                XCloseable,
                XWarningsSupplier,
                XResultSetMetaDataSupplier,
                XColumnLocate,
                XRow,
                PropertySet):
    def __init__(self, statement, sql=None):
        try:
            print("ResultSet.__init__() 1")
            self.statement = statement
            if sql:
                self.result = statement.statement.executeQuery(sql)
            else:
                self.result = statement.statement.executeQuery()
            #self.result = statement.statement.executeQuery()
            print("ResultSet.__init__() 2")
        except Exception as e:
            print("ResultSet.__init__() ERROR: %s - %s" % (e, traceback.print_exc()))

    @property
    def CursorName(self):
        return self.result.CursorName
    @property
    def ResultSetConcurrency(self):
        return self.result.ResultSetConcurrency
    @property
    def ResultSetType(self):
        return self.result.ResultSetType

    @property
    def FetchDirection(self):
        return self.result.FetchDirection
    @FetchDirection.setter
    def FetchDirection(self, row):
        self.result.FetchDirection = row

    @property
    def FetchSize(self):
        return self.result.FetchSize
    @FetchSize.setter
    def FetchSize(self, size):
        self.result.FetchSize = size

    # XCloseable
    def close(self):
        print("ResultSet.close()")
        self.result.close()

    # XWarningsSupplier
    def getWarnings(self):
        print("ResultSet.getWarnings() 1")
        warning = self.result.getWarnings()
        print("ResultSet.getWarnings() 2 %s" % warning)
        return warning
    def clearWarnings(self):
        print("ResultSet.clearWarnings()")
        self.result.clearWarnings()

    # XResultSet
    def next(self):
        print("ResultSet.next()")
        return self.result.next()
    def isBeforeFirst(self):
        return self.result.isBeforeFirst()
    def isAfterLast(self):
        return self.result.isAfterLast()
    def isFirst(self):
        return self.result.isFirst()
    def isLast(self):
        return self.result.isLast()
    def beforeFirst(self):
        self.result.beforeFirst()
    def afterLast(self):
        self.result.afterLast()
    def first(self):
        return self.result.first()
    def last(self):
        return self.result.last()
    def getRow(self):
        return self.result.getRow()
    def absolute(self, row):
        return self.result.absolute(row)
    def relative(self, row):
        return self.result.relative(row)
    def previous(self):
        return self.result.previous()
    def refreshRow(self):
        self.result.refreshRow()
    def rowUpdated(self):
        return self.result.rowUpdated()
    def rowInserted(self):
        return self.result.rowInserted()
    def rowDeleted(self):
        return self.result.rowDeleted()
    def getStatement(self):
        print("Connection.ResultSet.getStatement()")
        return self.statement

    # XResultSetMetaDataSupplier
    def getMetaData(self):
        print("Connection.ResultSet.getMetaData()")
        return self.result.getMetaData()

    # XRow
    def wasNull(self):
        return self.result.wasNull()
    def getString(self, index):
        print("ResultSet.getString()")
        return self.result.getString(index)
    def getBoolean(self, index):
        return self.result.getBoolean(index)
    def getByte(self, index):
        return self.result.getByte(index)
    def getShort(self, index):
        return self.result.getShort(index)
    def getInt(self, index):
        return self.result.getInt(index)
    def getLong(self, index):
        return self.result.getLong(index)
    def getFloat(self, index):
        return self.result.getFloat(index)
    def getDouble(self, index):
        return self.result.getDouble(index)
    def getBytes(self, index):
        return self.result.getBytes(index)
    def getDate(self, index):
        return self.result.getDate(index)
    def getTime(self, index):
        return self.result.getTime(index)
    def getTimestamp(self, index):
        return self.result.getTimestamp(index)
    def getBinaryStream(self, index):
        return self.result.getBinaryStream(index)
    def getCharacterStream(self, index):
        return self.result.getCharacterStream(index)
    def getObject(self, index, typemap):
        print("ResultSet.getObject()")
        return self.result.getObject(index, typemap)
    def getRef(self, index):
        return self.result.getRef(index)
    def getBlob(self, index):
        return self.result.getBlob(index)
    def getClob(self, index):
        return self.result.getClob(index)
    def getArray(self, index):
        return self.result.getArray(index)

    # XColumnLocate
    def findColumn(self, name):
        print("ResultSet.findColumn()")
        return self.result.findColumn(name)

    # XPropertySet
    def _getPropertySetInfo(self):
        properties = {}
        properties['CursorName'] = getProperty('CursorName', 'string', BOUND | READONLY)
        properties['ResultSetConcurrency'] = getProperty('ResultSetConcurrency', 'long', BOUND | READONLY)
        properties['ResultSetType'] = getProperty('ResultSetType', 'long', BOUND | READONLY)
        properties['FetchDirection'] = getProperty('FetchDirection', 'long', BOUND)
        properties['FetchSize'] = getProperty('FetchSize', 'long', BOUND)
        return properties


class DatabaseMetaData(unohelper.Base,
                       XDatabaseMetaData2):
    def __init__(self, connection, metadata, protocols, username):
        self.connection = connection
        self.metadata = metadata
        self.protocols = protocols
        self.username = username

    # XDatabaseMetaData2
    def getConnectionInfo(self):
        print("Connection.MetaData.getConnectionInfo()")
        return self.metadata.getConnectionInfo()
    def allProceduresAreCallable(self):
        print("Connection.MetaData.allProceduresAreCallable()")
        return self.metadata.allProceduresAreCallable()
    def allTablesAreSelectable(self):
        print("Connection.MetaData.allTablesAreSelectable()")
        return self.metadata.allTablesAreSelectable()
    def getURL(self):
        print("Connection.MetaData.getURL()")
        return ':'.join(self.protocols)
    def getUserName(self):
        print("Connection.MetaData.getUserName()")
        return self.username
    def isReadOnly(self):
        value = self.metadata.isReadOnly()
        print("Connection.MetaData.isReadOnly() %s" % value)
        return value
    def nullsAreSortedHigh(self):
        value = self.metadata.nullsAreSortedHigh()
        print("Connection.MetaData.isReadOnly() %s" % value)
        return value
    def nullsAreSortedLow(self):
        value = self.metadata.nullsAreSortedLow()
        print("Connection.MetaData.isReadOnly() %s" % value)
        return value
    def nullsAreSortedAtStart(self):
        value = self.metadata.nullsAreSortedAtStart()
        print("Connection.MetaData.isReadOnly() %s" % value)
        return value
    def nullsAreSortedAtEnd(self):
        value = self.metadata.nullsAreSortedAtEnd()
        print("Connection.MetaData.isReadOnly() %s" % value)
        return value
    def getDatabaseProductName(self):
        print("Connection.MetaData.getDatabaseProductName()")
        return self.metadata.getDatabaseProductName()
    def getDatabaseProductVersion(self):
        print("Connection.MetaData.getDatabaseProductVersion()")
        return self.metadata.getDatabaseProductVersion()
    def getDriverName(self):
        print("Connection.MetaData.getDriverName()")
        return self.metadata.getDriverName()
    def getDriverVersion(self):
        print("Connection.MetaData.getDriverVersion()")
        return self.metadata.getDriverVersion()
    def getDriverMajorVersion(self):
        print("Connection.MetaData.getDriverMajorVersion()")
        return self.metadata.getDriverMajorVersion()
    def getDriverMinorVersion(self):
        print("Connection.MetaData.getDriverMinorVersion()")
        return self.metadata.getDriverMinorVersion()
    def usesLocalFiles(self):
        return self.metadata.usesLocalFiles()
    def usesLocalFilePerTable(self):
        return self.metadata.usesLocalFilePerTable()
    def supportsMixedCaseIdentifiers(self):
        return self.metadata.supportsMixedCaseIdentifiers()
    def storesUpperCaseIdentifiers(self):
        return self.metadata.storesUpperCaseIdentifiers()
    def storesLowerCaseIdentifiers(self):
        return self.metadata.storesLowerCaseIdentifiers()
    def storesMixedCaseIdentifiers(self):
        return self.metadata.storesMixedCaseIdentifiers()
    def supportsMixedCaseQuotedIdentifiers(self):
        return self.metadata.supportsMixedCaseQuotedIdentifiers()
    def storesUpperCaseQuotedIdentifiers(self):
        return self.metadata.storesUpperCaseQuotedIdentifiers()
    def storesLowerCaseQuotedIdentifiers(self):
        return self.metadata.storesLowerCaseQuotedIdentifiers()
    def storesMixedCaseQuotedIdentifiers(self):
        return self.metadata.storesMixedCaseQuotedIdentifiers()
    def getIdentifierQuoteString(self):
        return self.metadata.getIdentifierQuoteString()
    def getSQLKeywords(self):
        return self.metadata.getSQLKeywords()
    def getNumericFunctions(self):
        return self.metadata.getNumericFunctions()
    def getStringFunctions(self):
        return self.metadata.getStringFunctions()
    def getSystemFunctions(self):
        return self.metadata.getSystemFunctions()
    def getTimeDateFunctions(self):
        return self.metadata.getTimeDateFunctions()
    def getSearchStringEscape(self):
        return self.metadata.getSearchStringEscape()
    def getExtraNameCharacters(self):
        return self.metadata.getExtraNameCharacters()
    def supportsAlterTableWithAddColumn(self):
        return self.metadata.supportsAlterTableWithAddColumn()
    def supportsAlterTableWithDropColumn(self):
        return self.metadata.supportsAlterTableWithDropColumn()
    def supportsColumnAliasing(self):
        return self.metadata.supportsColumnAliasing()
    def nullPlusNonNullIsNull(self):
        return self.metadata.nullPlusNonNullIsNull()
    def supportsTypeConversion(self):
        return self.metadata.supportsTypeConversion()
    def supportsConvert(self, fromtype, totype):
        return self.metadata.supportsConvert(fromtype, totype)
    def supportsTableCorrelationNames(self):
        return self.metadata.supportsTableCorrelationNames()
    def supportsDifferentTableCorrelationNames(self):
        return self.metadata.supportsDifferentTableCorrelationNames()
    def supportsExpressionsInOrderBy(self):
        return self.metadata.supportsExpressionsInOrderBy()
    def supportsOrderByUnrelated(self):
        return self.metadata.supportsOrderByUnrelated()
    def supportsGroupBy(self):
        return self.metadata.supportsGroupBy()
    def supportsGroupByUnrelated(self):
        return self.metadata.supportsGroupByUnrelated()
    def supportsGroupByBeyondSelect(self):
        return self.metadata.supportsGroupByBeyondSelect()
    def supportsLikeEscapeClause(self):
        return self.metadata.supportsLikeEscapeClause()
    def supportsMultipleResultSets(self):
        return self.metadata.supportsMultipleResultSets()
    def supportsMultipleTransactions(self):
        return self.metadata.supportsMultipleTransactions()
    def supportsNonNullableColumns(self):
        return self.metadata.supportsNonNullableColumns()
    def supportsMinimumSQLGrammar(self):
        return self.metadata.supportsMinimumSQLGrammar()
    def supportsCoreSQLGrammar(self):
        return self.metadata.supportsCoreSQLGrammar()
    def supportsExtendedSQLGrammar(self):
        return self.metadata.supportsExtendedSQLGrammar()
    def supportsANSI92EntryLevelSQL(self):
        return self.metadata.supportsANSI92EntryLevelSQL()
    def supportsANSI92IntermediateSQL(self):
        return self.metadata.supportsANSI92IntermediateSQL()
    def supportsANSI92FullSQL(self):
        return self.metadata.supportsANSI92FullSQL()
    def supportsIntegrityEnhancementFacility(self):
        return self.metadata.supportsIntegrityEnhancementFacility()
    def supportsOuterJoins(self):
        return self.metadata.supportsOuterJoins()
    def supportsFullOuterJoins(self):
        return self.metadata.supportsFullOuterJoins()
    def supportsLimitedOuterJoins(self):
        return self.metadata.supportsLimitedOuterJoins()
    def getSchemaTerm(self):
        return self.metadata.getSchemaTerm()
    def getProcedureTerm(self):
        return self.metadata.getProcedureTerm()
    def getCatalogTerm(self):
        return self.metadata.getCatalogTerm()
    def isCatalogAtStart(self):
        return self.metadata.isCatalogAtStart()
    def getCatalogSeparator(self):
        return self.metadata.getCatalogSeparator()
    def supportsSchemasInDataManipulation(self):
        return self.metadata.supportsSchemasInDataManipulation()
    def supportsSchemasInProcedureCalls(self):
        return self.metadata.supportsSchemasInProcedureCalls()
    def supportsSchemasInTableDefinitions(self):
        return self.metadata.supportsSchemasInTableDefinitions()
    def supportsSchemasInIndexDefinitions(self):
        return self.metadata.supportsSchemasInIndexDefinitions()
    def supportsSchemasInPrivilegeDefinitions(self):
        return self.metadata.supportsSchemasInPrivilegeDefinitions()
    def supportsCatalogsInDataManipulation(self):
        return self.metadata.supportsCatalogsInDataManipulation()
    def supportsCatalogsInProcedureCalls(self):
        return self.metadata.supportsCatalogsInProcedureCalls()
    def supportsCatalogsInTableDefinitions(self):
        return self.metadata.supportsCatalogsInTableDefinitions()
    def supportsCatalogsInIndexDefinitions(self):
        return self.metadata.supportsCatalogsInIndexDefinitions()
    def supportsCatalogsInPrivilegeDefinitions(self):
        return self.metadata.supportsCatalogsInPrivilegeDefinitions()
    def supportsPositionedDelete(self):
        return self.metadata.supportsPositionedDelete()
    def supportsPositionedUpdate(self):
        return self.metadata.supportsPositionedUpdate()
    def supportsSelectForUpdate(self):
        return self.metadata.supportsSelectForUpdate()
    def supportsStoredProcedures(self):
        print("Connection.MetaData.supportsStoredProcedures() %s" % self.metadata.supportsStoredProcedures())
        return self.metadata.supportsStoredProcedures()
    def supportsSubqueriesInComparisons(self):
        return self.metadata.supportsSubqueriesInComparisons()
    def supportsSubqueriesInExists(self):
        return self.metadata.supportsSubqueriesInExists()
    def supportsSubqueriesInIns(self):
        return self.metadata.supportsSubqueriesInIns()
    def supportsSubqueriesInQuantifieds(self):
        return self.metadata.supportsSubqueriesInQuantifieds()
    def supportsCorrelatedSubqueries(self):
        return self.metadata.supportsCorrelatedSubqueries()
    def supportsUnion(self):
        return self.metadata.supportsUnion()
    def supportsUnionAll(self):
        return self.metadata.supportsUnionAll()
    def supportsOpenCursorsAcrossCommit(self):
        return self.metadata.supportsOpenCursorsAcrossCommit()
    def supportsOpenCursorsAcrossRollback(self):
        return self.metadata.supportsOpenCursorsAcrossRollback()
    def supportsOpenStatementsAcrossCommit(self):
        return self.metadata.supportsOpenStatementsAcrossCommit()
    def supportsOpenStatementsAcrossRollback(self):
        return self.metadata.supportsOpenStatementsAcrossRollback()
    def getMaxBinaryLiteralLength(self):
        return self.metadata.getMaxBinaryLiteralLength()
    def getMaxCharLiteralLength(self):
        return self.metadata.getMaxCharLiteralLength()
    def getMaxColumnNameLength(self):
        return self.metadata.getMaxColumnNameLength()
    def getMaxColumnsInGroupBy(self):
        return self.metadata.getMaxColumnsInGroupBy()
    def getMaxColumnsInIndex(self):
        return self.metadata.getMaxColumnsInIndex()
    def getMaxColumnsInOrderBy(self):
        return self.metadata.getMaxColumnsInOrderBy()
    def getMaxColumnsInSelect(self):
        return self.metadata.getMaxColumnsInSelect()
    def getMaxColumnsInTable(self):
        return self.metadata.getMaxColumnsInTable()
    def getMaxConnections(self):
        return self.metadata.getMaxConnections()
    def getMaxCursorNameLength(self):
        return self.metadata.getMaxCursorNameLength()
    def getMaxIndexLength(self):
        return self.metadata.getMaxIndexLength()
    def getMaxSchemaNameLength(self):
        return self.metadata.getMaxSchemaNameLength()
    def getMaxProcedureNameLength(self):
        return self.metadata.getMaxProcedureNameLength()
    def getMaxCatalogNameLength(self):
        return self.metadata.getMaxCatalogNameLength()
    def getMaxRowSize(self):
        return self.metadata.getMaxRowSize()
    def doesMaxRowSizeIncludeBlobs(self):
        return self.metadata.doesMaxRowSizeIncludeBlobs()
    def getMaxStatementLength(self):
        return self.metadata.getMaxStatementLength()
    def getMaxStatements(self):
        return self.metadata.getMaxStatements()
    def getMaxTableNameLength(self):
        return self.metadata.getMaxTableNameLength()
    def getMaxTablesInSelect(self):
        return self.metadata.getMaxTablesInSelect()
    def getMaxUserNameLength(self):
        return self.metadata.getMaxUserNameLength()
    def getDefaultTransactionIsolation(self):
        return self.metadata.getDefaultTransactionIsolation()
    def supportsTransactions(self):
        return self.metadata.supportsTransactions()
    def supportsTransactionIsolationLevel(self, level):
        return self.metadata.supportsTransactionIsolationLevel(level)
    def supportsDataDefinitionAndDataManipulationTransactions(self):
        return self.metadata.supportsDataDefinitionAndDataManipulationTransactions()
    def supportsDataManipulationTransactionsOnly(self):
        return self.metadata.supportsDataManipulationTransactionsOnly()
    def dataDefinitionCausesTransactionCommit(self):
        return self.metadata.dataDefinitionCausesTransactionCommit()
    def dataDefinitionIgnoredInTransactions(self):
        return self.metadata.dataDefinitionIgnoredInTransactions()
    def getProcedures(self, catalog, schema, procedure):
        print("Connection.MetaData.getProcedures()")
        return self.metadata.getProcedures(catalog, schema, procedure)
    def getProcedureColumns(self, catalog, schema, procedure, column):
        print("Connection.MetaData.getProcedureColumns()")
        return self.metadata.getProcedureColumns(catalog, schema, procedure, column)
    def getTables(self, catalog, schema, table, types):
        print("Connection.MetaData.getTables()")
        return self.metadata.getTables(catalog, schema, table, types)
    def getSchemas(self):
        print("Connection.MetaData.getSchemas()")
        return self.metadata.getSchemas()
    def getCatalogs(self):
        print("Connection.MetaData.getCatalogs()")
        return self.metadata.getCatalogs()
    def getTableTypes(self):
        print("Connection.MetaData.getTableTypes()")
        return self.metadata.getTableTypes()
    def getColumns(self, catalog, schema, table, column):
        print("Connection.MetaData.getColumns()")
        return self.metadata.getColumns(catalog, schema, table, column)
    def getColumnPrivileges(self, catalog, schema, table, column):
        print("Connection.MetaData.getColumnPrivileges()")
        return self.metadata.getColumnPrivileges(catalog, schema, table, column)
    def getTablePrivileges(self, catalog, schema, table):
        print("Connection.MetaData.getTablePrivileges()")
        return self.metadata.getTablePrivileges(catalog, schema, table)
    def getBestRowIdentifier(self, catalog, schema, table, scope, nullable):
        return self.metadata.getBestRowIdentifier(catalog, schema, table, scope, nullable)
    def getVersionColumns(self, catalog, schema, table):
        return self.metadata.getVersionColumns(catalog, schema, table)
    def getPrimaryKeys(self, catalog, schema, table):
        return self.metadata.getPrimaryKeys(catalog, schema, table)
    def getImportedKeys(self, catalog, schema, table):
        return self.metadata.getImportedKeys(catalog, schema, table)
    def getExportedKeys(self, catalog, schema, table):
        return self.metadata.getExportedKeys(catalog, schema, table)
    def getCrossReference(self, catalog, schema, table, foreigncatalog, foreignschema, foreigntable):
        return self.metadata.getCrossReference(catalog, schema, table, foreigncatalog, foreignschema, foreigntable)
    def getTypeInfo(self):
        print("Connection.MetaData.supportsResultSetType()")
        return self.metadata.getTypeInfo()
    def getIndexInfo(self, catalog, schema, table, unique, approximate):
        print("Connection.MetaData.getIndexInfo()")
        return self.metadata.getIndexInfo(catalog, schema, table, unique, approximate)
    def supportsResultSetType(self, settype):
        print("Connection.MetaData.supportsResultSetType()")
        return self.metadata.supportsResultSetType(settype)
    def supportsResultSetConcurrency(self, settype, concurrency):
        print("Connection.MetaData.supportsResultSetConcurrency()")
        return self.metadata.supportsResultSetConcurrency(settype, concurrency)
    def ownUpdatesAreVisible(self, settype):
        return self.metadata.ownUpdatesAreVisible(settype)
    def ownDeletesAreVisible(self, settype):
        return self.metadata.ownDeletesAreVisible(settype)
    def ownInsertsAreVisible(self, settype):
        return self.metadata.ownInsertsAreVisible(settype)
    def othersUpdatesAreVisible(self, settype):
        return self.metadata.othersUpdatesAreVisible(settype)
    def othersDeletesAreVisible(self, settype):
        return self.metadata.othersDeletesAreVisible(settype)
    def othersInsertsAreVisible(self, settype):
        return self.metadata.othersInsertsAreVisible(settype)
    def updatesAreDetected(self, settype):
        return self.metadata.updatesAreDetected(settype)
    def deletesAreDetected(self, settype):
        return self.metadata.deletesAreDetected(settype)
    def insertsAreDetected(self, settype):
        return self.metadata.insertsAreDetected(settype)
    def supportsBatchUpdates(self):
        return self.metadata.supportsBatchUpdates()
    def getUDTs(self, catalog, schema, typename, types):
        print("Connection.MetaData.getUDTs()")
        return self.metadata.getUDTs(catalog, schema, typename, types)
    def getConnection(self):
        print("Connection.MetaData.getConnection()")
        return self.connection


class ResultSetMetaData(unohelper.Base,
                        XResultSetMetaData):
    def __init__(self, statement):
        self.metadata = statement.getMetaData()

    # XResultSetMetaData
    def getColumnCount(self):
        print("ResultSetMetaData.getColumnCount() %s" % self.metadata.getColumnCount())
        return self.metadata.getColumnCount()
    def isAutoIncrement(self, column):
        print("ResultSetMetaData.isAutoIncrement() %s" % self.metadata.isAutoIncrement(column))
        return self.metadata.isAutoIncrement(column)
    def isCaseSensitive(self, column):
        print("ResultSetMetaData.isCaseSensitive() %s" % self.metadata.isCaseSensitive(column))
        return self.metadata.isCaseSensitive(column)
    def isSearchable(self, column):
        print("ResultSetMetaData.isSearchable() %s" % self.metadata.isSearchable(column))
        return self.metadata.isSearchable(column)
    def isCurrency(self, column):
        print("ResultSetMetaData.isCurrency() %s" % self.metadata.isCurrency(column))
        return self.metadata.isCurrency(column)
    def isNullable(self, column):
        print("ResultSetMetaData.isNullable() %s" % self.metadata.isNullable(column))
        return self.metadata.isNullable(column)
    def isSigned(self, column):
        print("ResultSetMetaData.isSigned() %s" % self.metadata.isSigned(column))
        return self.metadata.isSigned(column)
    def getColumnDisplaySize(self, column):
        print("ResultSetMetaData.getColumnDisplaySize() %s" % self.metadata.getColumnDisplaySize(column))
        return self.metadata.getColumnDisplaySize(column)
    def getColumnLabel(self, column):
        print("ResultSetMetaData.getColumnLabel() %s" % self.metadata.getColumnLabel(column))
        return self.metadata.getColumnLabel(column)
    def getColumnName(self, column):
        print("ResultSetMetaData.getColumnName() %s" % self.metadata.getColumnName(column))
        return self.metadata.getColumnName(column)
    def getSchemaName(self, column):
        print("ResultSetMetaData.getSchemaName() %s" % self.metadata.getSchemaName(column))
        return self.metadata.getSchemaName(column)
    def getPrecision(self, column):
        print("ResultSetMetaData.getPrecision() %s" % self.metadata.getPrecision(column))
        return self.metadata.getPrecision(column)
    def getScale(self, column):
        print("ResultSetMetaData.getScale() %s" % self.metadata.getScale(column))
        return self.metadata.getScale(column)
    def getTableName(self, column):
        print("ResultSetMetaData.getTableName() %s" % self.metadata.getTableName(column))
        return self.metadata.getTableName(column)
    def getCatalogName(self, column):
        print("ResultSetMetaData.getCatalogName() %s" % self.metadata.getCatalogName(column))
        return self.metadata.getCatalogName(column)
    def getColumnType(self, column):
        print("ResultSetMetaData.getColumnType() %s" % self.metadata.getColumnType(column))
        return self.metadata.getColumnType(column)
    def getColumnTypeName(self, column):
        print("ResultSetMetaData.getColumnTypeName() %s" % self.metadata.getColumnTypeName(column))
        return self.metadata.getColumnTypeName(column)
    def isReadOnly(self, column):
        print("ResultSetMetaData.isReadOnly() %s" % self.metadata.isReadOnly(column))
        return self.metadata.isReadOnly(column)
    def isWritable(self, column):
        print("ResultSetMetaData.isWritable() %s" % self.metadata.isWritable(column))
        return self.metadata.isWritable(column)
    def isDefinitelyWritable(self, column):
        print("ResultSetMetaData.isDefinitelyWritable() %s" % self.metadata.isDefinitelyWritable(column))
        return self.metadata.isDefinitelyWritable(column)
    def getColumnServiceName(self, column):
        print("ResultSetMetaData.getColumnServiceName() %s" % self.metadata.getColumnServiceName(column))
        return self.metadata.getColumnServiceName(column)
