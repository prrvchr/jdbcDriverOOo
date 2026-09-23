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

from .tab1 import TabManager as Tab1Manager
from .tab2 import TabManager as Tab2Manager

from ..runner import DispatchListener

from ..setup import SetupCheck

from ..configuration import State
from ..configuration import g_agent
from ..configuration import g_extension
from ..configuration import g_java
from ..configuration import g_protocol
from ..configuration import g_script
from ..configuration import g_service

import traceback


class OptionsManager():
    def __init__(self, ctx, window):
        self._ctx = ctx
        self._disposed = False
        self._java = g_extension, g_java, g_script
        self._agent = g_service, g_protocol, g_agent
        self._listener = TabListener(self)
        self._model = OptionsModel(ctx)
        self._view = OptionsView(ctx, window, self._listener, *self._model.getViewData())
        self._tab1 = Tab1Manager(ctx, self._view.getTab1(), DispatchListener(self), 'Driver')
        self._tab2 = Tab2Manager(ctx, self._view.getTab2())
        window.addEventListener(OptionsListener(self))
        self._loadView(self.init)

    def init(self, state):
        if not self._disposed:
            self._tab1.init(state)
            self._view.refreshView(state)
            if state.java:
                self._model.loadDriver()

    def refresh(self, state):
        if not self._disposed:
            self._tab1.refresh(state)
            self._view.refreshView(state)

# OptionsManager setter methods
    def dispose(self):
       if not self._disposed:
            self._disposed = True
            self._tab1.dispose()
            self._tab2.dispose()
            self._view.dispose()

    def activateTab2(self):
        self._view.removeTabListener(self._listener)
        self._tab2.setDriverVersions()

    def saveSetting(self):
        reboot = self._tab1.saveSetting()
        reboot |= self._tab2.saveSetting()
        if reboot:
            State.restart = True
            self._loadView(self.refresh)

    def loadSetting(self):
        self._loadView(self.init)

    def dispatchFinished(self, notification):
        if not self._disposed:
            self._loadView(self.refresh)

    def _loadView(self, callback):
        mode = self._tab1.getSetupMode()
        setup = SetupCheck(self._ctx, callback, java=self._java, agent=self._agent, python=True, mode=mode)
        setup.start()

