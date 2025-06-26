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

from ...unotool import getConfiguration
from ...unotool import getLibreOfficeVersion

from ...configuration import g_identifier

import traceback


class TabModel():
    def __init__(self, ctx):
        self._key = 'EnableJavaSystemLogger'
        self._config = getConfiguration(ctx, g_identifier, True)
        self._version = getLibreOfficeVersion(ctx)
        self._javalogger = self._getJavaLogger()

# TabModel getter methods
    def getJavaLogger(self):
        return self._javalogger

    def saveSetting(self):
        changed = False
        javalogger = self._getJavaLogger()
        if javalogger != self._javalogger:
            self._config.replaceByName(self._key, self._javalogger)
            if self._config.hasPendingChanges():
                self._config.commitChanges()
            changed = True
        return changed

# TabModel setter methods
    def setJavaLogger(self, enabled):
        self._javalogger = enabled

# TabModel private getter methods
    def _getJavaLogger(self):
        return self._config.getByName(self._key)

