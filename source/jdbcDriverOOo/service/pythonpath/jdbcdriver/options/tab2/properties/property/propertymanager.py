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

from .propertyhandler import WindowHandler

from .propertyview import PropertyView

import traceback


class PropertyManager():
    def __init__(self, ctx, window, manager):
        self._view = PropertyView(ctx, window, WindowHandler(manager))

# PropertyManager getter methods
    def getPropertyValue(self):
        return self._view.getValue()

    def getValuesIndex(self):
        return self._view.getValuesIndex()

    def getTypesIndex(self):
        return self._view.getTypesIndex()

    def getCheckBoxValue(self):
        return self._view.getCheckBoxValue()

    def getNumFieldValue(self):
        return self._view.getNumFieldValue()

    def getListBoxValue(self):
        return self._view.getListBoxValue()

    def getTextFieldValue(self):
        return self._view.getTextFieldValue()

# PropertyManager setter methods
    def dispose(self):
        self._view.dispose()

    def selectType(self, index):
        self._view.selectType(index)

    def enableTypes(self, enabled):
        self._view.enableTypes(enabled)

    def initAddValues(self):
        self._view.initAddValues()

    def selectPropertyValue(self, index):
        self._view.selectPropertyValue(index)

    def setCheckBoxValue(self, value, updatable):
        self._view.setCheckBoxValue(value, updatable)

    def setNumFieldValue(self, value, updatable):
        self._view.setNumFieldValue(value, updatable)

    def setListBoxValue(self, value, updatable):
        self._view.setListBoxValue(value, updatable)

    def setTextFieldValue(self, value, updatable):
        self._view.setTextFieldValue(value, updatable)

    def setDefaultFocus(self):
        self._view.setDefaultFocus()

# PropertyWindow.xdl handler entries
    def setType(self, index):
        self._view.setType(index)

    def editValue(self):
        self._view.setValue(self._view.getValuesItem())
        self._view.setStep(5)

    def addValue(self):
        self._view.clearValue()
        self._view.setStep(5)

    def cancelValue(self):
        self._view.setStep(4)
