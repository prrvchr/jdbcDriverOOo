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

from com.sun.star.lang import XComponent
from com.sun.star.lang import XServiceInfo

from com.sun.star.sdbc import XConnection
from com.sun.star.sdbc import XWarningsSupplier

from com.sun.star.uno import XWeak

from .metadata import MetaData

from .statement import Statement
from .statement import PreparedStatement
from .statement import CallableStatement

import traceback


class Connection(unohelper.Base,
                 XComponent,
                 XConnection,
                 XServiceInfo,
                 XWarningsSupplier,
                 XWeak):
    def __init__(self, ctx, connection, url):
        self._ctx = ctx
        self._connection = connection
        self._url = url
        self._listeners = []
        # TODO: We cannot use: connection.prepareStatement(sql).
        # TODO: It trow a: receiver class org.hsqldb.jdbc.JDBCPreparedStatement does not implement
        # TODO: The interface java.sql.CallableStatement defining the method to be called
        # TODO: (org.hsqldb.jdbc.JDBCPreparedStatement is in unnamed module of loader
        # TODO: java.net.URLClassLoader @4eb26a26; java.sql.CallableStatement is in module
        # TODO: java.sql of loader 'platform').
        # TODO: If self._patched: fallback to connection.prepareCall(sql).
        self._patched = True

# XCloseable <- XConnection
    def close(self):
        if not self.isClosed():
            self.dispose()

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
        return MetaData(self, metadata, self._url)
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
        return service in self.getSupportedServiceNames()
    def getImplementationName(self):
        return self._connection.getImplementationName()
    def getSupportedServiceNames(self):
        return ('com.sun.star.sdbc.Connection', )

# XWarningsSupplier
    def getWarnings(self):
        warning = self._connection.getWarnings()
        return warning
    def clearWarnings(self):
        self._connection.clearWarnings()

# XWeak
    def queryAdapter(self):
        return self
