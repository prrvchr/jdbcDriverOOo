#!
# -*- coding: utf-8 -*-

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

import unohelper

from ..unotool import getFileSequence
from ..unotool import getResourceLocation
from ..unotool import getStringResourceWithLocation

from .loghelper import LogController
from .loghelper import getPool

from ..configuration import g_identifier
from ..configuration import g_resource
from ..configuration import g_basename


class LogModel(LogController):
    def __init__(self, ctx, name, listener):
        self._ctx = ctx
        self._basename = g_basename
        self._pool = getPool(ctx)
        self._url = getResourceLocation(ctx, g_identifier, g_resource)
        self._logger = None
        self._listener = listener
        self._resolver = getStringResourceWithLocation(ctx, self._url, 'Logger')
        self._debug = (True, 7, 'com.sun.star.logging.FileHandler')
        self._settings = None
        self._default = name
        self._pool.addModifyListener(listener)

    # Public getter method
    def getLoggerNames(self, filter=None):
        names = self._pool.getLoggerNames() if filter is None else self._pool.getFilteredLoggerNames(filter)
        if self._default not in names:
            names = list(names)
            names.insert(0, self._default)
        return names

    def isLoggerEnabled(self):
        level = self._getLogConfig().LogLevel
        enabled = self._isLogEnabled(level)
        return enabled

    def getLoggerSetting(self):
        enabled, index, handler = self._getLoggerSetting()
        state = self._getState(handler)
        return enabled, index, state

    def getLoggerUrl(self):
        return self._getLoggerUrl()

    def getLoggerData(self):
        url = self._getLoggerUrl()
        length, sequence = getFileSequence(self._ctx, url)
        text = sequence.value.decode('utf-8')
        return url, text, length

# Public setter method
    def dispose(self):
        self._pool.removeModifyListener(self._listener)

    def setLogger(self, name):
        self._logger =  self._pool.getLocalizedLogger(name, self._url, g_basename)

    def setLoggerSetting(self, enabled, index, state):
        handler = self._getHandler(state)
        self._setLoggerSetting(enabled, index, handler)

# Private getter method
    def _getHandler(self, state):
        handlers = {True: 'ConsoleHandler', False: 'FileHandler'}
        return 'com.sun.star.logging.%s' % handlers.get(state)

    def _getState(self, handler):
        states = {'com.sun.star.logging.ConsoleHandler': 1,
                  'com.sun.star.logging.FileHandler': 2}
        return states.get(handler)

