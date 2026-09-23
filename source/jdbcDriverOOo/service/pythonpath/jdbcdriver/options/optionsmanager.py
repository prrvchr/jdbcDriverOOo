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

from .optionshandler import TabListener

from .tab1.tabmanager import TabManager as Tab1Manager
from .tab2.tabmanager import TabManager as Tab2Manager

from .setup.setuphandler import SetupHandler

from ..runner.listener import DispatchListener

from ..setup.setupcheck import SetupCheck

from ..config.configuration import State

from ..setup.helper import getSetupListener
from ..setup.helper import removeSetupListener
from ..setup.helper import showSetup

from ..configuration import g_agent
from ..configuration import g_extension
from ..configuration import g_java
from ..configuration import g_protocol
from ..configuration import g_script
from ..configuration import g_service


class OptionsManager():
    def __init__(self, ctx, window):
        self._ctx = ctx
        self._checker = None
        self._disposed = False
        self._java = g_extension, g_java, g_script
        self._agent = g_service, g_protocol, g_agent
        self._model = OptionsModel(ctx)
        self._view = OptionsView(ctx, window, TabListener(self), *self._model.getViewData())
        self._tab1 = Tab1Manager(ctx, self._view.getTab1(), SetupHandler(self), 'Driver')
        self._tab2 = Tab2Manager(ctx, self._view.getTab2())
        self._listener = OptionsListener(self)
        window.addEventListener(OptionsListener(self))
        self._slistener = getSetupListener(ctx, self)
        self._loadView(self.init)
        self._view.getLayout().addLayout(self._tab1.getLayout()).addLayout(self._tab2.getLayout())

    def init(self, state):
        self._checker = None
        if not self._disposed:
            self._tab1.init(state)
            self._view.refreshView(*self._model.getRefreshData(state))
            if state.java:
                self._model.loadDriver()

    def refresh(self, state):
        self._checker = None
        if not self._disposed:
            self._tab1.refresh(state)
            self._view.refreshView(*self._model.getRefreshData(state))

    def showSetup(self):
        mode = self._tab1.getSetupMode()
        dispatch = DispatchListener(self)
        showSetup(self._ctx, Dispatch=dispatch, Mode=mode)

# OptionsManager setter methods
    def closeSetup(self):
        self._slistener = None
        if self._disposed:
            return
        self._loadView(self.refresh)

    def dispose(self, window):
        if self._disposed:
            return
        self._disposed = True
        self._tab1.dispose()
        self._tab2.dispose()
        self._view.dispose()
        window.removeEventListener(self._listener)
        if self._slistener:
            removeSetupListener(self._ctx, self._slistener)
            self._slistener = None

    def activateTab2(self):
        self._view.removeTabListener()
        self._tab2.setDriverVersions()

    def saveSetting(self):
        if self._checker:
            self._checker.cancel()
        reboot = self._tab1.saveSetting()
        reboot |= self._tab2.saveSetting()
        if reboot:
            State.restart = True

    def loadSetting(self):
        self._loadView(self.init)

    def dispatchFinished(self, _notification):
        if not self._disposed:
            self._loadView(self.refresh)

    def _loadView(self, callback):
        if not self._disposed:
            mode = self._tab1.getSetupMode()
            self._checker = SetupCheck(self._ctx, callback, java=self._java, agent=self._agent, python=True, mode=mode)
            self._checker.start()
