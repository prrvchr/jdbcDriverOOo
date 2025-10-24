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

from ..jdbcdriver import isInstrumented

from ..unotool import createService
from ..unotool import getStringResource

from ..configuration import g_identifier
from ..configuration import g_service


import traceback


class OptionsModel():
    def __init__(self, ctx):
        self._ctx = ctx
        self._instrumented = isInstrumented(ctx, 'xdbc:jdbc')
        self._resolver = getStringResource(ctx, g_identifier, 'dialogs', 'OptionsDialog')
        self._resources = {'TabTitle1': 'OptionsDialog.Tab1.Title',
                           'TabTitle2': 'OptionsDialog.Tab2.Title',
                           'Link': 'OptionsDialog.Hyperlink1.Url'}

# OptionsModel setter methods
    def loadDriver(self):
        try:
            driver = createService(self._ctx, g_service)
        except:
            # Nothing to do the error is already logged
            pass

# OptionsModel getter methods
    def isInstrumented(self):
        return self._instrumented

    def getViewData(self):
        resource = self._resources.get('Link')
        url = self._resolver.resolveString(resource)
        return url, self._instrumented, self._getTabTitle(1), self._getTabTitle(2)

# OptionsModel private getter methods
    def _getTabTitle(self, tab):
        resource = self._resources.get('TabTitle%s' % tab)
        return self._resolver.resolveString(resource)

