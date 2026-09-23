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

from ..setup.setupmanager import SetupManager

from ..option.optionmanager import OptionManager


class TabManager():
    def __init__(self, ctx, window, handler, logger, *loggers):
        self._ctx = ctx
        self._model = TabModel(ctx)
        self._view = TabWindow(ctx, window, TabHandler(self))
        self._manager = OptionManager(ctx, self._view.getFrame(), self._model.getConfig(), logger, *loggers)
        self._setup = SetupManager(ctx, self._view.getSetup(), handler, self._model.getConfig())
        self._view.getLayout().addLayout(self._manager.getLayout()).addLayout(self._setup.getLayout())

# TabManager setter methods
    def init(self, state):
        self._manager.init(state)
        self._setup.init()
        self._view.initView(*self._model.getInitData(state))

    def refresh(self, state):
        self._manager.refresh(state)
        self._view.initView(*self._model.getRefreshData(state))

    def dispose(self):
        self._setup.dispose()
        self._manager.dispose()
        self._view.dispose()

# TabManager getter methods
    def getLayout(self):
        return self._view.getLayout()

    def getConfigApiLevel(self):
        return self._manager.getConfigApiLevel()

    def getSetupMode(self):
        return self._setup.getSetupMode()

    # Option1Dialog.xdl handler entry
    def setJavaLogger(self, enabled):
        self._model.setJavaLogger(enabled)

    def setClassPath(self, enabled):
        self._model.setClassPath(enabled)

    def saveSetting(self):
        self._setup.saveSetting()
        reboot = self._model.saveSetting()
        reboot |= self._manager.saveSetting()
        return reboot
