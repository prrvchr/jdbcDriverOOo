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

from .optionshandler import EventListener
from .optionshandler import TabListener

from .tab1 import TabManager as Tab1Manager
from .tab2 import TabManager as Tab2Manager

import traceback


class OptionsManager():
    def __init__(self, ctx, window):
        self._ctx = ctx
        self._disposed = False
        self._listener = TabListener(self)
        window.addEventListener(EventListener(self))
        self._model = OptionsModel(ctx)
        url, instrumented, title1, title2 = self._model.getViewData()
        self._view = OptionsView(ctx, window, self._listener, OptionsManager._restart, url, instrumented, title1, title2)
        self._tab1 = Tab1Manager(ctx, self._view.getTab1(), instrumented, 'Driver')
        self._tab2 = Tab2Manager(ctx, self._view.getTab2())
        self._tab1.initView()
        self._model.loadDriver()

    _restart = False

# OptionsManager setter methods
    def dispose(self):
        if not self._disposed:
            self._tab1.dispose()
            self._tab2.dispose()
            self._view.dispose()
            self._disposed = True

    def activateTab2(self):
        self._view.removeTabListener(self._listener)
        self._tab2.setDriverVersions(self._tab1.getConfigApiLevel())

    def saveSetting(self):
        reboot = self._tab1.saveSetting()
        reboot |= self._tab2.saveSetting()
        if reboot:
            OptionsManager._restart = True
            self._view.setWarning(True, self._model.isInstrumented())

    def loadSetting(self):
        self._tab1.loadSetting()
        self._tab2.loadSetting()

