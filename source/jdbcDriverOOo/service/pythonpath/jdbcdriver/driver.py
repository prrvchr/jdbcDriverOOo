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

from .helper import getSetupState

from .unotool import createService

from .config.configuration import State

from .configuration import g_url
from .configuration import g_version


class Driver(unohelper.Base,
             XServiceInfo,
             XDriver):
    def __init__(self, ctx, logger, service, implementation, services):
        self._ctx = ctx
        self._cls = 'Driver'
        self._services = services
        self._implementation = implementation
        mtd = '__init__'
        state = getSetupState(ctx)
        if state.runnable:
            self._driver = createService(ctx, service)
            logger.logprb(INFO, self._cls, mtd, 101, implementation, service)
        else:
            self._driver = None
            State.restart = True
            logger.logp(SEVERE, self._cls, mtd, state.getMessage(ctx))
        self._logger = logger
        self._version = [int(x) for x in g_version.split('.')] + [0, 0]

    # XDriver
    def connect(self, url, infos):
        return self._getDriver('connect').connect(url, infos)

    def acceptsURL(self, url):
        return url.startswith(g_url)

    def getPropertyInfo(self, url, infos):
        mtd = 'getPropertyInfo'
        if not self.acceptsURL(url) or self._driver is None:
            if self._driver is None:
                state = getSetupState(self._ctx)
                self._logger.logp(SEVERE, self._cls, mtd, state.getMessage(self._ctx))
            return ()
        return self._getDriver(mtd).getPropertyInfo(url, infos)

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
    def _getDriver(self, mtd):
        if self._driver is None:
            state = getSetupState(self._ctx)
            msg = state.getMessage(self._ctx)
            self._logger.logp(SEVERE, self._cls, mtd, msg)
            raise SQLException(msg, self, '08001', state.code, None)
        return self._driver
