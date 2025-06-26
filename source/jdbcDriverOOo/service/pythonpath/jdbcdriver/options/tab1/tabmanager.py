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

from .tabmodel import TabModel

from .tabview import TabWindow

from .tabhandler import TabHandler

from ...option import OptionManager

import traceback


class TabManager():
    def __init__(self, ctx, window, restart, offset, logger, *loggers):
        self._model = TabModel(ctx)
        self._view = TabWindow(ctx, window, TabHandler(self))
        self._manager = OptionManager(ctx, self._view.getWindow(), (), restart, offset, logger, *loggers)

# TabManager setter methods
    def initView(self):
        self._manager.initView()
        self._initView()

    def dispose(self):
        self._manager.dispose()
        self._view.dispose()

# TabManager getter methods
    def getConfigApiLevel(self):
        return self._manager.getConfigApiLevel()

    # Option1Dialog.xdl handler entry
    def setJavaLogger(self, enabled):
        self._model.setJavaLogger(enabled)

    def saveSetting(self):
        reboot = self._model.saveSetting()
        reboot |= self._manager.saveSetting()
        return reboot

    def setRestart(self, state):
        self._manager.setRestart(state)

    def loadSetting(self):
        self._manager.loadSetting()
        self._initView()

# TabManager private methods
    def _initView(self):
        self._view.setJavaLogger(self._model.getJavaLogger())

