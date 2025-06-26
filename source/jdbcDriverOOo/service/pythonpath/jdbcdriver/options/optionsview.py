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

from ..unotool import getContainerWindow

from ..configuration import g_identifier

import traceback


class OptionsView():
    def __init__(self, ctx, window, listener, title1, title2):
        self._tab = 'Tab1'
        self._window = window
        self._tab1, self._tab2 = self._getTabPages(window, self._tab, title1, title2)
        self._getTab().addTabPageContainerListener(listener)

# OptionsView getter methods
    def getWindow(self):
        return self._window

    def getTab1(self):
        return self._tab1

    def getTab2(self):
        return self._tab2

# OptionsView setter methods
    def dispose(self):
        self._tab1.dispose()
        self._tab2.dispose()

    def removeTabListener(self, listener):
        self._getTab().removeTabPageContainerListener(listener)

# OptionsView private methods
    def _getTabPages(self, window, name, title1, title2, i=1):
        model = self._getTabModel(window)
        window.Model.insertByName(name, model)
        tab = self._getTab()
        tab1 = self._getTabPage(model, tab, title1)
        tab2 = self._getTabPage(model, tab, title2)
        tab.ActiveTabPageID = i
        return tab1, tab2

    def _getTabModel(self, window):
        service = 'com.sun.star.awt.tab.UnoControlTabPageContainerModel'
        model = window.Model.createInstance(service)
        #model.PositionX = window.Model.PositionX
        #model.PositionY = window.Model.PositionY
        model.Width = window.Model.Width
        model.Height = window.Model.Height
        return model

    def _getTabPage(self, model, tab, title):
        index = model.getCount()
        page = model.createTabPage(index +1)
        page.Title = title
        model.insertByIndex(index, page)
        return tab.getControls()[index]

# OptionsView private control methods
    def _getTab(self):
        return self._window.getControl(self._tab)

