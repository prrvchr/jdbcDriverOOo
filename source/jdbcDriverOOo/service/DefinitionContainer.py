#!
# -*- coding: utf-8 -*-

"""
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
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
from com.sun.star.lang import XInitialization

from jdbcdriver import createService

from jdbcdriver import g_identifier

import traceback

# pythonloader looks for a static g_ImplementationHelper variable
g_ImplementationHelper = unohelper.ImplementationHelper()
g_ImplementationName1 = 'io.github.prrvchr.jdbcDriverOOo.DefinitionContainer'
g_ServiceNames1 = ('com.sun.star.sdb.DefinitionContainer', )


class DefinitionContainer(unohelper.Base,
                          XComponent,
                          XInitialization,
                          XServiceInfo):

    def __init__(self, ctx, *args):
        print("DefinitionContainer.__init__() 1")
        self._ctx = ctx
        self._listeners = []
        if args:
            print("DefinitionContainer.__init__() 2")
            if not DefinitionContainer._init:
                DefinitionContainer._init = True
                print("DefinitionContainer.__init__() 3 *************************************")
                #self.initialize(args)

    _init = False

# XComponent
    def dispose(self):
        pass
    def addEventListener(self, listener):
        self._listeners.append(listener)
    def removeEventListener(self, listener):
        if listener in self._listeners:
            self._listeners.remove(listener)

# XServiceInfo
    def supportsService(self, service):
        return g_ImplementationHelper.supportsService(g_ServiceName1, service)
    def getImplementationName(self):
        return g_ServiceName1
    def getSupportedServiceNames(self):
        return g_ImplementationHelper.getSupportedServiceNames(g_ServiceName1)

# XInitialization
    def initialize(self, arguments):
        print("DefinitionContainer.initialize() 1")
        mri = createService(self._ctx, 'mytools.Mri')
        for argument in arguments:
            if argument.Name == 'DatabaseDocument':
                print("DefinitionContainer.initialize() 2")
                database = argument.Value
                mri.inspect(database)
            elif argument.Name == 'DataSource':
                print("DefinitionContainer.initialize() 3 ********************************************************")
                datasource = argument.Value
                mri.inspect(datasource)


g_ImplementationHelper.addImplementation(DefinitionContainer,             # UNO object class
                                         g_ImplementationName1,           # Implementation name
                                         g_ServiceNames1)                 # List of implemented services
