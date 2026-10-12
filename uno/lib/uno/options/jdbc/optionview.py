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

from ...unotool import getContainerWindow

from ...configuration import g_identifier

import traceback


class OptionView():
    def __init__(self, ctx, window, handler):
        self._window = getContainerWindow(ctx, window.getPeer(), handler, g_identifier, 'OptionDialog')
        self._window.setVisible(True)

# OptionView setter methods
    def dispose(self):
        self._window.dispose()

    def initView(self, level, crs, system, rst, enabled, state):
        self._getApiLevel(level).State = 1
        self._getCachedRowSet().State = int(crs)
        self._getSytemTable().State = int(system)
        self.refreshView(rst, enabled, state)

    def refreshView(self, rst, enabled, state):
        if state.agent:
            self._getResultSetType(rst).State = 1
        else:
            self._getResultSetType(0).State = 1
        self.enableResultSetType(state.agent and enabled)
        self.enableCachedRowSet(state.agent and enabled and rst != 0)

    def enableResultSetType(self, enabled, options=(0, 1, 2)):
        for index in options:
            self._getResultSetType(index).Model.Enabled = enabled

    def enableCachedRowSet(self, enabled):
        self._getCachedRowSet().Model.Enabled = enabled

# OptionView private control methods
    def _getApiLevel(self, index):
        return self._window.getControl('OptionButton%s' % (index + 1))

    def _getResultSetType(self, index):
        return self._window.getControl('OptionButton%s' % (index + 4))

    def _getCachedRowSet(self):
        return self._window.getControl('CheckBox1')

    def _getSytemTable(self):
        return self._window.getControl('CheckBox2')

