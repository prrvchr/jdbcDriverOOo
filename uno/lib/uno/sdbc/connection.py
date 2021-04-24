#!
# -*- coding: utf_8 -*-

"""
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
"""



import uno
import unohelper

from com.sun.star.beans import XFastPropertySet
from com.sun.star.beans import XMultiPropertySet
from com.sun.star.beans import XPropertySet

from com.sun.star.container import XChild
from com.sun.star.container import XContainerListener

from com.sun.star.lang import XComponent
from com.sun.star.lang import XInitialization
from com.sun.star.lang import XMultiServiceFactory
from com.sun.star.lang import XServiceInfo
from com.sun.star.lang import XUnoTunnel

from com.sun.star.sdb.CommandType import TABLE
from com.sun.star.sdb.CommandType import QUERY
from com.sun.star.sdb.CommandType import COMMAND

from com.sun.star.sdb import XBookmarksSupplier
from com.sun.star.sdb import XCommandPreparation
from com.sun.star.sdb import XCompletedConnection
from com.sun.star.sdb import XDocumentDataSource
from com.sun.star.sdb import XQueriesSupplier
from com.sun.star.sdb import XQueryDefinitionsSupplier
from com.sun.star.sdb import XSQLQueryComposerFactory
from com.sun.star.sdb.application import XTableUIProvider
from com.sun.star.sdb.tools import XConnectionTools

from com.sun.star.sdbc import SQLException

from com.sun.star.sdbc import XConnection
from com.sun.star.sdbc import XDataSource
from com.sun.star.sdbc import XIsolatedConnection
from com.sun.star.sdbc import XWarningsSupplier

from com.sun.star.sdbcx import XGroupsSupplier
from com.sun.star.sdbcx import XTablesSupplier
from com.sun.star.sdbcx import XUsersSupplier
from com.sun.star.sdbcx import XViewsSupplier

from com.sun.star.uno import XWeak

from com.sun.star.util import XFlushable
from com.sun.star.util import XFlushListener

from ..dbqueries import getSqlQuery

from .metadata import MetaData

from .database import DataBase

from .users import Users

from .statement import Statement
from .statement import PreparedStatement
from .statement import CallableStatement

import traceback


class Connection(unohelper.Base,
                 XChild,
                 XCommandPreparation,
                 XComponent,
                 XConnection,
                 XConnectionTools,
                 XGroupsSupplier,
                 XMultiServiceFactory,
                 XQueriesSupplier,
                 XServiceInfo,
                 XSQLQueryComposerFactory,
                 XTablesSupplier,
                 XTableUIProvider,
                 XUnoTunnel,
                 XUsersSupplier,
                 XViewsSupplier,
                 XWarningsSupplier,
                 XWeak):
    def __init__(self, ctx, connection, datasource=None):
        self._ctx = ctx
        self._connection = connection
        self._datasource = datasource
        self._listeners = []
        # TODO: We cannot use: connection.prepareStatement(sql).
        # TODO: It trow a: receiver class org.hsqldb.jdbc.JDBCPreparedStatement does not implement
        # TODO: The interface java.sql.CallableStatement defining the method to be called
        # TODO: (org.hsqldb.jdbc.JDBCPreparedStatement is in unnamed module of loader
        # TODO: java.net.URLClassLoader @4eb26a26; java.sql.CallableStatement is in module
        # TODO: java.sql of loader 'platform').
        # TODO: If self._patched: fallback to connection.prepareCall(sql).
        self._patched = True

# XChild
    def getParent(self):
        # TODO: This wrapping is only there for the following lines:
        if self._datasource is None:
            parent = self._connection.getParent()
            datasource = DataSource(self._ctx, parent)
        else:
            datasource =  self._datasource
        return datasource
    def setParent(self, parent):
        self._connection.setParent(parent)

# XCloseable <- XConnection
    def close(self):
        event = uno.createUnoStruct('com.sun.star.lang.EventObject')
        event.Source = self
        for listener in self._listeners:
            listener.disposing(event)
        if not self._connection.isClosed():
            self._connection.close()

# XCloseBroadcaster <- XCloseable <- XConnection
    def addCloseListener(self, listener):
        self._listeners.append(listener)
    def removeCloseListener(self, listener):
        if listener in self._listeners:
            self._listeners.remove(listener)

# XCommandPreparation
    def prepareCommand(self, command, commandtype):
        sql = None
        if commandtype == TABLE:
            sql = getSqlQuery(self._ctx, 'prepareCommand', command)
        elif commandtype == QUERY:
            if self.getQueries().hasByName(command):
                sql = self.getQueries().getByName(command).Command
        elif commandtype == COMMAND:
            sql = command
        if sql is None:
            raise SQLException()
        return self.prepareStatement(sql)

# XComponent
    def dispose(self):
        self._connection.dispose()
    def addEventListener(self, listener):
        self._connection.addEventListener(listener)
    def removeEventListener(self, listener):
        self._connection.removeEventListener(listener)

# XConnection
    def createStatement(self):
        statement = self._connection.createStatement()
        return Statement(self, statement)
    def prepareStatement(self, sql):
        # TODO: We cannot use: connection.prepareStatement(sql).
        # TODO: It trow a: java.lang.IncompatibleClassChangeError.
        # TODO: If self._patched: fallback to connection.prepareCall(sql).
        if self._patched:
            statement = self._connection.prepareCall(sql)
        else:
            statement = self._connection.prepareStatement(sql)
        return PreparedStatement(self, statement)
    def prepareCall(self, sql):
        statement = self._connection.prepareCall(sql)
        return CallableStatement(self, statement)
    def nativeSQL(self, sql):
        return self._connection.nativeSQL(sql)
    def setAutoCommit(self, auto):
        self._connection.setAutoCommit(auto)
    def getAutoCommit(self):
        return self._connection.getAutoCommit()
    def commit(self):
        self._connection.commit()
    def rollback(self):
        self._connection.rollback()
    def isClosed(self):
        return self._connection.isClosed()
    def getMetaData(self):
        # TODO: This wrapping is only there for the following lines:
        metadata = self._connection.getMetaData()
        url = self._connection.getParent().URL
        return MetaData(self, metadata, url)
    def setReadOnly(self, readonly):
        self._connection.setReadOnly(readonly)
    def isReadOnly(self):
        return self._connection.isReadOnly()
    def setCatalog(self, catalog):
        self._connection.setCatalog(catalog)
    def getCatalog(self):
        return self._connection.getCatalog()
    def setTransactionIsolation(self, level):
        self._connection.setTransactionIsolation(level)
    def getTransactionIsolation(self):
        return self._connection.getTransactionIsolation()
    def getTypeMap(self):
        return self._connection.getTypeMap()
    def setTypeMap(self, typemap):
        self._connection.setTypeMap(typemap)

# XConnectionTools
    def createTableName(self):
        return self._connection.createTableName()
    def getObjectNames(self):
        return self._connection.getObjectNames()
    def getDataSourceMetaData(self):
        return self._connection.getDataSourceMetaData()
    def getFieldsByCommandDescriptor(self, commandtype, command, keep):
        fields, keep = self._connection.getFieldsByCommandDescriptor(commandtype, command, keep)
        return fields, keep
    def getComposer(self, commandtype, command):
        return self._connection.getComposer(commandtype, command)

# XGroupsSupplier
    def getGroups(self):
        return self._connection.getGroups()

# XMultiServiceFactory
    def createInstance(self, service):
        return self._connection.createInstance(service)
    def createInstanceWithArguments(self, service, arguments):
        return self._connection.createInstanceWithArguments(service, arguments)
    def getAvailableServiceNames(self):
        return self._connection.getAvailableServiceNames()

# XQueriesSupplier
    def getQueries(self):
        return self._connection.getQueries()

# XServiceInfo
    def supportsService(self, service):
        return self._connection.supportsService(service)
    def getImplementationName(self):
        return self._connection.getImplementationName()
    def getSupportedServiceNames(self):
        return self._connection.getSupportedServiceNames()

# XSQLQueryComposerFactory
    def createQueryComposer(self):
        return self._connection.createQueryComposer()

# XTablesSupplier
    def getTables(self):
        return self._connection.getTables()

# XTableUIProvider
    def getTableIcon(self, tablename, colormode):
        return self._connection.getTableIcon(tablename, colormode)
    def getTableEditor(self, document, tablename):
        return self._connection.getTableEditor(document, tablename)

# XUnoTunnel
    def getSomething(self, identifier):
        return self._connection.getSomething(identifier)

# XUsersSupplier
    def getUsers(self):
        return Users(self._ctx, self._connection)

# XViewsSupplier
    def getViews(self):
        return self._connection.getViews()

# XWarningsSupplier
    def getWarnings(self):
        warning = self._connection.getWarnings()
        return warning
    def clearWarnings(self):
        self._connection.clearWarnings()

# XWeak
    def queryAdapter(self):
        return self


class DataSource(unohelper.Base,
                 XBookmarksSupplier,
                 XCompletedConnection,
                 XComponent,
                 XContainerListener,
                 XDataSource,
                 XDocumentDataSource,
                 XFastPropertySet,
                 XFlushable,
                 XFlushListener,
                 XInitialization,
                 XIsolatedConnection,
                 XMultiPropertySet,
                 XPropertySet,
                 XQueryDefinitionsSupplier,
                 XServiceInfo,
                 XTablesSupplier,
                 XWeak):
    def __init__(self, ctx, datasource):
        self._ctx = ctx
        self._datasource = datasource

    @property
    def Info(self):
        return self._datasource.Info
    @Info.setter
    def Info(self, info):
        self._datasource.Info = info
    @property
    def IsPasswordRequired(self):
        return self._datasource.IsPasswordRequired
    @IsPasswordRequired.setter
    def IsPasswordRequired(self, state):
        self._datasource.IsPasswordRequired = state
    @property
    def IsReadOnly(self):
        return self._datasource.IsReadOnly
    @property
    def LayoutInformation(self):
        return self._datasource.LayoutInformation
    @LayoutInformation.setter
    def LayoutInformation(self, layout):
        self._datasource.LayoutInformation = layout
    @property
    def Name(self):
        return self._datasource.Name
    @property
    def NumberFormatsSupplier(self):
        return self._datasource.NumberFormatsSupplier
    @property
    def Password(self):
        return self._datasource.Password
    @Password.setter
    def Password(self, password):
        self._datasource.Password = password
    @property
    def Settings(self):
        return self._datasource.Settings
    @property
    def SuppressVersionColumns(self):
        return self._datasource.SuppressVersionColumns
    @SuppressVersionColumns.setter
    def SuppressVersionColumns(self, state):
        self._datasource.SuppressVersionColumns = state
    @property
    def TableFilter(self):
        return self._datasource.TableFilter
    @TableFilter.setter
    def TableFilter(self, filter):
        self._datasource.TableFilter = filter
    @property
    def TableTypeFilter(self):
        return self._datasource.TableTypeFilter
    @TableTypeFilter.setter
    def TableTypeFilter(self, filter):
        self._datasource.TableTypeFilter = filter
    @property
    def URL(self):
        return self._datasource.URL
    @URL.setter
    def URL(self, url):
        self._datasource.URL = url
    @property
    def User(self):
        return self._datasource.User
    @User.setter
    def User(self, user):
        self._datasource.User = user

# XBookmarksSupplier
    def getBookmarks(self):
        return self._datasource.getBookmarks()

# XCompletedConnection
    def connectWithCompletion(self, handler):
        # TODO: This wrapping is only there for the following lines:
        connection = self._datasource.connectWithCompletion(handler)
        return Connection(self._ctx, connection, self)

# XComponent
    def dispose(self):
        self._datasource.dispose()
    def addEventListener(self, listener):
        self._datasource.addEventListener(listener)
    def removeEventListener(self, listener):
        self._datasource.removeEventListener(listener)

# XContainerListener
    def elementInserted(self, event):
       self._datasource.elementInserted(event)
    def elementRemoved(self, event):
       self._datasource.elementRemoved(event)
    def elementReplaced(self, event):
       self._datasource.elementReplaced(event)

# XDataSource
    def getConnection(self, user, password):
        # TODO: This wrapping is only there for the following lines:
        connection = self._datasource.getConnection(user, password)
        return Connection(self._ctx, connection, self)
    def setLoginTimeout(self, seconds):
        self._datasource.setLoginTimeout(seconds)
    def getLoginTimeout(self):
        return self._datasource.getLoginTimeout()

# XDocumentDataSource
    @property
    def DatabaseDocument(self):
        # TODO: This wrapping is only there for the following lines:
        database = self._datasource.DatabaseDocument
        return DataBase(database, self)

# XEventListener <- XContainerListener
    def disposing(self, source):
        self._datasource.disposing(source)

# XFastPropertySet
    def setFastPropertyValue(self, handle, value):
        self._datasource.setFastPropertyValue(handle, value)
    def getFastPropertyValue(self, handle):
        return self._datasource.getFastPropertyValue(handle)

# XFlushable
    def flush(self):
        self._datasource.flush()
    def addFlushListener(self, listener):
        self._datasource.addFlushListener(listener)
    def removeFlushListener(self, listener):
        self._datasource.removeFlushListener(listener)

# XFlushListener
    def flushed(self, event):
        self._datasource.flushed(event)

# XInitialization
    def initialize(self, arguments):
        self._datasource.initialize(arguments)

# XIsolatedConnection
    def getIsolatedConnectionWithCompletion(self, handler):
        # TODO: This wrapping is only there for the following lines:
        connection = self._datasource.getIsolatedConnectionWithCompletion(handler)
        return Connection(self._ctx, connection, self)
    def getIsolatedConnection(self, user, password):
        # TODO: This wrapping is only there for the following lines:
        connection = self._datasource.getIsolatedConnection(user, password)
        return Connection(self._ctx, connection, self)

# XMultiPropertySet
    def setPropertyValues(self, names, values):
        self._datasource.setPropertyValues(names, values)
    def getPropertyValues(self, names):
        return self._datasource.getPropertyValues(names)
    def addPropertiesChangeListener(self, names, listener):
        self._datasource.addPropertiesChangeListener(names, listener)
    def removePropertiesChangeListener(self, listener):
        self._datasource.removePropertiesChangeListener(listener)
    def firePropertiesChangeEvent(self, names, listener):
        self._datasource.firePropertiesChangeEvent(names, listener)

# XPropertySet
    def getPropertySetInfo(self):
        return self._datasource.getPropertySetInfo()
    def setPropertyValue(self, name, value):
        self._datasource.setPropertyValue(name, value)
    def getPropertyValue(self, name):
        return self._datasource.getPropertyValue(name)
    def addPropertyChangeListener(self, name, listener):
        self._datasource.addPropertyChangeListener(name, value)
    def removePropertyChangeListener(self, name, listener):
        self._datasource.removePropertyChangeListener(name, listener)
    def addVetoableChangeListener(self, name, listener):
        self._datasource.addVetoableChangeListener(name, value)
    def removeVetoableChangeListener(self, name, listener):
        self._datasource.removeVetoableChangeListener(name, listener)

# XQueryDefinitionsSupplier
    def getQueryDefinitions(self):
        return self._datasource.getQueryDefinitions()

# XServiceInfo
    def supportsService(self, service):
        return self._datasource.supportsService(service)
    def getImplementationName(self):
        return self._datasource.getImplementationName()
    def getSupportedServiceNames(self):
        return self._datasource.getSupportedServiceNames()

# XTablesSupplier
    def getTables(self):
        return self._datasource.getTables()

# XWeak
    def queryAdapter(self):
        return self
