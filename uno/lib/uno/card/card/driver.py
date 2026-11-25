#!
# -*- coding: utf-8 -*-

"""
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
"""

import unohelper

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from com.sun.star.lang import XServiceInfo

from com.sun.star.sdbc import SQLException
from com.sun.star.sdbc import XDriver

from com.sun.star.sdbcx import XCreateCatalog
from com.sun.star.sdbcx import XDataDefinitionSupplier
from com.sun.star.sdbcx import XDropCatalog

from .helper import getSetupState

from .helper import getLogException

from ..datasource import DataSource

from ..config.configuration import State

from ..dbtool import getDriverPropertyInfos

from ..configuration import g_protocol
from ..configuration import g_version

import traceback


class Driver(unohelper.Base,
             XCreateCatalog,
             XDataDefinitionSupplier,
             XDriver,
             XDropCatalog,
             XServiceInfo):
    def __init__(self, ctx, logger, implementation, services):
        self._ctx = ctx
        self._cls = 'Driver'
        mtd = '__init__'
        self._implementation = implementation
        self._services = services
        state = getSetupState(ctx)
        self._error = False
        if state.runnable:
            try:
                datasource = DataSource(ctx, logger, self)
            except SQLException as e:
                self._error = True
                State.restart = True
                self._datasource = None
                logger.logp(SEVERE, self._cls, '__init__', e.Message)
            else:
                self._datasource = datasource
                logger.logprb(INFO, self._cls, mtd, 101, implementation)
        else:
            State.restart = True
            self._datasource = None
            logger.logp(SEVERE, self._cls, mtd, state.getMessage(ctx))
        self._logger = logger
        self._version = [int(x) for x in g_version.split('.')] + [0, 0]

# XCreateCatalog
    def createCatalog(self, info):
        pass

# XDropCatalog
    def dropCatalog(self, name, info):
        pass

# XDataDefinitionSupplier
    def getDataDefinitionByConnection(self, connection):
        return connection

    def getDataDefinitionByURL(self, url, infos):
        return self.connect(url, infos)

# XDriver
    def connect(self, url, infos):
        try:
            mtd = 'connect'
            self._logger.logprb(INFO, self._cls, mtd, 1111, url)
            params = self._getConnectionParams(url, infos, mtd)
            connection = self._getDataSource(mtd).getConnection(self, self._logger, *params)
            metadata = connection.getMetaData()
            self._logger.logprb(INFO, self._cls, mtd, 1115, metadata.getDriverVersion(), metadata.getUserName())
            return connection
        except SQLException as e:
            raise
        except Exception as e:
            raise getLogException(self._logger, self, 1005, 1116, self._cls, mtd, str(e), traceback.format_exc())

    def acceptsURL(self, url):
        return url and url.startswith(g_protocol)

    def getPropertyInfo(self, url, _infos):
        if not self.acceptsURL(url) or self._datasource is None:
            if self._datasource is None:
                state = getSetupState(self._ctx)
                self._logger.logp(SEVERE, self._cls, 'getPropertyInfo', state.getMessage(self._ctx))
            return ()
        return getDriverPropertyInfos()

    def getMajorVersion(self):
        return self._version[0]

    def getMinorVersion(self):
        return self._version[1]

# XServiceInfo
    def supportsService(self, service):
        return service in self._services

    def getImplementationName(self):
        return self._implementation

    def getSupportedServiceNames(self):
        return self._services

# Private getter methods
    def _getConnectionParams(self, url, infos, mtd):
        raise NotImplementedError

    def _getDataSource(self, mtd):
        if self._datasource is None:
            if self._error:
                try:
                    datasource = DataSource(self._ctx, self._logger, self)
                except SQLException as e:
                    self._logger.logp(SEVERE, self._cls, mtd, e.Message)
                    raise
                else:
                    self._error = False
                    self._datasource = datasource
            else:
                state = getSetupState(self._ctx)
                msg = state.getMessage(self._ctx)
                self._logger.logp(SEVERE, self._cls, mtd, msg)
                raise SQLException(msg, self, '08001', state.code, None)
        return self._datasource
