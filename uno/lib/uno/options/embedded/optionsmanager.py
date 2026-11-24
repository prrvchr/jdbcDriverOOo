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

from .optionsmodel import OptionsModel

from .optionsview import OptionsView

from .optionshandler import OptionsListener

from .option.optionmanager import OptionManager

from .setup.setuphandler import SetupHandler

from .setup.setupmanager import SetupManager

from ..runner.listener import DispatchListener

from ..setup.setupcheck import SetupCheck

from ..config.configuration import State

from ..setup.helper import getSetupListener
from ..setup.helper import removeSetupListener
from ..setup.helper import showSetup

from ..helper import getExtensions

from ..jdbcdriver.configuration import g_agent
from ..jdbcdriver.configuration import g_java
from ..jdbcdriver.configuration import g_protocol
from ..jdbcdriver.configuration import g_script
from ..jdbcdriver.configuration import g_service

from ..configuration import g_extension
from ..configuration import g_defaultlog


class OptionsManager():
    def __init__(self, ctx, logger, window, url=None):
        self._ctx = ctx
        self._checker = None
        self._disposed = False
        self._java = g_extension, g_java, g_script
        self._agent = g_service, g_protocol, g_agent
        self._extensions = getExtensions()
        self._model = OptionsModel(ctx, logger, url)
        self._view = OptionsView(ctx, window)
        self._manager = OptionManager(ctx, self._view.getFrame(), self._model.getConfig(), g_defaultlog)
        self._setup = SetupManager(ctx, self._view.getSetup(), SetupHandler(self), self._model.getConfig())
        self._listener = OptionsListener(self)
        window.addEventListener(self._listener)
        self._slistener = getSetupListener(ctx, self)
        self._view.getLayout().addLayout(self._manager.getLayout()).addLayout(self._setup.getLayout())
        self._loadView(self.init)

    def init(self, state):
        self._checker = None
        if self._disposed:
            return
        self._setup.init()
        self._manager.init(state)
        self._view.refreshView(*self._model.getRefreshData(state))
        if state.java:
            version = self._model.getDriverVersion(self._getConfigApiLevel())
            self._view.setDriverVersion(version)

    def refresh(self, state):
        self._checker = None
        if self._disposed:
            return
        self._manager.refresh(state)
        self._view.refreshView(*self._model.getRefreshData(state))

    def dispose(self, window):
        if self._disposed:
            return
        self._disposed = True
        self._manager.dispose()
        self._setup.dispose()
        self._view.dispose()
        window.removeEventListener(self._listener)
        if self._slistener:
            removeSetupListener(self._ctx, self._slistener)
            self._slistener = None

# OptionsManager setter methods
    def showSetup(self):
        mode = self._setup.getSetupMode()
        showSetup(self._ctx, Dispatch=DispatchListener(self), Mode=mode)

    def closeSetup(self):
        self._slistener = None
        if self._disposed:
            return
        self._loadView(self.refresh)

    def saveSetting(self):
        if self._checker:
            self._checker.cancel()
        self._setup.saveSetting()
        if self._manager.saveSetting():
            State.restart = True

    def loadSetting(self):
        self._loadView(self.init)

    def dispatchFinished(self, _notification):
        if self._disposed:
            return
        self._loadView(self.refresh)

# OptionsManager private getter methods
    def _getConfigApiLevel(self):
        return self._manager.getConfigApiLevel()

# OptionsManager private setter methods
    def _loadView(self, callback):
        if self._disposed:
            return
        mode = self._setup.getSetupMode()
        self._checker = SetupCheck(self._ctx, callback, java=self._java, agent=self._agent, python=True, extensions=self._extensions, mode=mode)
        self._checker.start()
