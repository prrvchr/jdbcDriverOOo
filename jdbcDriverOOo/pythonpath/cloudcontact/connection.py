#!
# -*- coding: utf_8 -*-

import uno
import unohelper

from com.sun.star.lang import XServiceInfo
from com.sun.star.lang import XComponent
from com.sun.star.sdbc import XConnection
from com.sun.star.sdbc import XCloseable
from com.sun.star.sdbc import XWarningsSupplier
from com.sun.star.sdb import XCommandPreparation
from com.sun.star.sdb import XQueriesSupplier
from com.sun.star.sdb import XSQLQueryComposerFactory
from com.sun.star.lang import XMultiServiceFactory
from com.sun.star.container import XChild
from com.sun.star.sdb.application import XTableUIProvider
from com.sun.star.sdb.tools import XConnectionTools

from com.sun.star.uno import XWeak
from com.sun.star.uno import XAdapter

from com.sun.star.sdbcx import XTablesSupplier
from com.sun.star.sdbcx import XViewsSupplier
from com.sun.star.sdbcx import XUsersSupplier
from com.sun.star.sdbcx import XGroupsSupplier

from com.sun.star.sdbcx import XUser
from com.sun.star.container import XNameAccess
from com.sun.star.container import XIndexAccess
from com.sun.star.container import XEnumerationAccess
from com.sun.star.container import XElementAccess

from unolib import PropertySet
from unolib import getProperty
from unolib import createService

from .dbtools import getSequenceFromResult
from .dbqueries import getSqlQuery

from .unodb.documentdatasource import DocumentDataSource
from .unodb.databasemetadata import DatabaseMetaData
from .unodb.statement import Statement
from .unodb.statement import PreparedStatement
from .unodb.statement import CallableStatement

import traceback


class Connection(unohelper.Base,
                 XServiceInfo,
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
                 XConnectionTools,
                 XWeak):
    def __init__(self, ctx, connection, protocols, username):
        self.ctx = ctx
        self._connection = connection
        self._protocols = protocols
        self._username = username

    # XComponent
    def dispose(self):
        self._connection.dispose()
    def addEventListener(self, listener):
        self._connection.addEventListener(listener)
    def removeEventListener(self, listener):
        self._connection.removeEventListener(listener)

    # XWeak
    def queryAdapter(self):
        return self._connection.queryAdapter()

    # XTableUIProvider
    def getTableIcon(self, tablename, colormode):
        return self._connection.getTableIcon(tablename, colormode)
    def getTableEditor(self, documentui, tablename):
        return self._connection.getTableEditor(documentui, tablename)

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

    # XCloseable
    def close(self):
        if not self._connection.isClosed():
            self._connection.close()

    # XCommandPreparation
    def prepareCommand(self, command, commandtype):
        return self._connection.prepareCommand(command, commandtype)

    # XQueriesSupplier
    def getQueries(self):
        return self._connection.getQueries()

    # XSQLQueryComposerFactory
    def createQueryComposer(self):
        return self._connection.createQueryComposer()

    # XMultiServiceFactory
    def createInstance(self, service):
        return self._connection.createInstance(service)
    def createInstanceWithArguments(self, service, arguments):
        return self._connection.createInstanceWithArguments(service, arguments)
    def getAvailableServiceNames(self):
        return self._connection.getAvailableServiceNames()

    # XChild
    def getParent(self):
        parent = self._connection.getParent()
        return DocumentDataSource(parent, self._protocols, self._username)
    def setParent(self):
        pass

    # XTablesSupplier
    def getTables(self):
        return self._connection.getTables()

    # XViewsSupplier
    def getViews(self):
        return self._connection.getViews()

    # XUsersSupplier
    def getUsers(self):
        return self._connection.getUsers()

    # XGroupsSupplier
    def getGroups(self):
        return self._connection.getGroups()

    # XWarningsSupplier
    def getWarnings(self):
        warning = self._connection.getWarnings()
        return warning
    def clearWarnings(self):
        self._connection.clearWarnings()

    # XConnection
    def createStatement(self):
        return Statement(self)
    def prepareStatement(self, sql):
        return PreparedStatement(self, sql)
    def prepareCall(self, sql):
        return CallableStatement(self, sql)
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
        metadata = self._connection.getMetaData()
        return DatabaseMetaData(self, metadata, self._protocols, self._username)
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

    # XServiceInfo
    def supportsService(self, service):
        return self._connection.supportsService(service)
    def getImplementationName(self):
        return self._connection.getImplementationName()
    def getSupportedServiceNames(self):
        return self._connection.getSupportedServiceNames()
