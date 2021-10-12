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
from com.sun.star.lang import XUnoTunnel

from com.sun.star.sdb import XDatabaseContext

#from hsqldbdriver import DataSource

from hsqldbdriver import createService

import traceback

# pythonloader looks for a static g_ImplementationHelper variable
g_ImplementationHelper = unohelper.ImplementationHelper()
g_ImplementationName = 'com.sun.star.comp.dba.ODatabaseContext2'


class DatabaseContext(unohelper.Base,
                      XComponent,
                      XDatabaseContext,
                      XServiceInfo,
                      XUnoTunnel):

    def __init__(self, ctx):
        self._ctx = ctx
        service = 'com.sun.star.comp.dba.ODatabaseContext'
        self._dbcontext = createService(ctx, service)

# XComponent
    def dispose(self):
        self._dbcontext.dispose()
    def addEventListener(self, listener):
        self._dbcontext.addEventListener(listener)
    def removeEventListener(self, listener):
        self._dbcontext.removeEventListener(listener)

# XContainer <- XDatabaseContext
    def addContainerListener(self, listener):
        self._dbcontext.addContainerListener(listener)
    def removeContainerListener(self, listener):
        self._dbcontext.removeContainerListener(listener)

# XDatabaseRegistrations <- XDatabaseContext
    def addDatabaseRegistrationsListener(self, listener):
        self._dbcontext.addDatabaseRegistrationsListener(listener)
    def changeDatabaseLocation(self, name, location):
        self._dbcontext.changeDatabaseLocation(name, location)
    def getDatabaseLocation(self, name):
        return self._dbcontext.getDatabaseLocation(name)
    def getRegistrationNames(self):
        return self._dbcontext.getRegistrationNames()
    def hasRegisteredDatabase(self, name):
        return self._dbcontext.hasRegisteredDatabase(name)
    def isDatabaseRegistrationReadOnly(self, name):
        return self._dbcontext.isDatabaseRegistrationReadOnly(name)
    def registerDatabaseLocation(self, name, location):
        self._dbcontext.registerDatabaseLocation(name, location)
    def removeDatabaseRegistrationsListener(self, listener):
        self._dbcontext.removeDatabaseRegistrationsListener(listener)
    def revokeDatabaseLocation(self, name):
        self._dbcontext.revokeDatabaseLocation(name)

# XElementAccess <- XEnumerationAccess / XNameAccess
    def getElementType(self):
        return self._dbcontext.getElementType()
    def hasElements(self):
        return self._dbcontext.hasElements()

# XEnumerationAccess <- XDatabaseContext
    def createEnumeration(self):
        return self._dbcontext.createEnumeration()

# XNameAccess <- XDatabaseContext
    def getByName(self, name):
        datasource = self._dbcontext.getByName(name)
        return DataSource(self._ctx, datasource)
    def getElementNames(self):
        return self._dbcontext.getElementNames()
    def hasByName(self, name):
        return self._dbcontext.hasByName(name)

# XNamingService <- XDatabaseContext
    def getRegisteredObject(self, name):
        return self._dbcontext.getRegisteredObject(name)
    def registerObject(self, name, obj):
        self._dbcontext.registerObject(name, obj)
    def revokeObject(self, name):
        self._dbcontext.revokeObject(name)

# XServiceInfo
    def supportsService(self, service):
        return g_ImplementationHelper.supportsService(g_ImplementationName, service)
    def getImplementationName(self):
        return g_ImplementationName
    def getSupportedServiceNames(self):
        return g_ImplementationHelper.getSupportedServiceNames(g_ImplementationName)

# XSingleServiceFactory <- XDatabaseContext
    def createInstance(self):
        return self._dbcontext.createInstance()
    def createInstanceWithArguments(self, arguments):
        return self._dbcontext.createInstanceWithArguments(arguments)

# XUnoTunnel
    def getSomething(self, identifier):
        return self._dbcontext.getSomething(identifier)


g_ImplementationHelper.addImplementation(DatabaseContext,
                                         g_ImplementationName,
                                        ('com.sun.star.sdb.DatabaseContext', ))
