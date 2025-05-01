#!
# -*- coding: utf_8 -*-

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

import uno
import unohelper

from com.sun.star.lang import XComponent
from com.sun.star.lang import XServiceInfo

from com.sun.star.sdbc import XDriver

from .unotool import createService


import traceback


class Driver(unohelper.Base,
             XComponent,
             XServiceInfo,
             XDriver):

    def __init__(self, cls, ctx, service, implementation, services):
        self._cls = cls
        self._driver = createService(ctx, service)
        self._implementation = implementation
        self._services = services
        self._listeners = []

    # XDriver
    def connect(self, url, infos):
        print("Driver.connect 1")
        connection = self._driver.connect(url, infos)
        print("Driver.connect 2")
        return connection

    def acceptsURL(self, url):
        return self._driver.acceptsURL(url)

    def getPropertyInfo(self, url, infos):
        return self._driver.getPropertyInfo(url, infos)

    def getMajorVersion(self):
        return self._driver.getMajorVersion()
    def getMinorVersion(self):
        return getMajorVersion.getMinorVersion()

    # XComponent
    def dispose(self):
        print("Driver.dispose() 1 instance: %s" % self._cls.instance)
        source = uno.createUnoStruct('com.sun.star.lang.EventObject', self)
        for listener in self._listeners:
            listener.disposing(source)
        self._driver.dispose()
        with self._cls.lock:
            self._cls.instance = None
        print("Driver.dispose() 2 instance: %s" % self._cls.instance)
    def addEventListener(self, listener):
        if listener not in self._listeners:
            self._listeners.add(listener)
    def removeEventListener(self, listener):
        if listener in self._listeners:
            self._listeners.remove(listener)

    # XServiceInfo
    def supportsService(self, service):
        return service in self._services
    def getImplementationName(self):
        return self._implementation
    def getSupportedServiceNames(self):
        return self._services
