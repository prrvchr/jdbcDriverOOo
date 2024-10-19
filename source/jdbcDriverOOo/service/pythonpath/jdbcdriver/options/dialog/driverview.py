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

import unohelper

from ...unotool import getDialog

from ...configuration import g_identifier


class DriverView(unohelper.Base):
    def __init__(self, ctx, window, handler, xdl):
        self._dialog = getDialog(ctx, g_identifier, xdl, handler, window.getPeer())

    def execute(self):
        return self._dialog.execute()

    def dispose(self):
        return self._dialog.dispose()

    def getDriver(self):
        return self._getDriver().Text

    def getProtocol(self):
        return self._getProtocol().Text

    def getJavaClass(self):
        return self._getJavaClass().Text

    def enableConfirm(self, enable):
        self._getConfirm().Model.Enabled = enable

# DriverView private control methods
    def _getProtocol(self):
        return self._dialog.getControl('TextField1')

    def _getDriver(self):
        return self._dialog.getControl('TextField2')

    def _getJavaClass(self):
        return self._dialog.getControl('TextField3')

    def _getConfirm(self):
        return self._dialog.getControl('CommandButton3')

