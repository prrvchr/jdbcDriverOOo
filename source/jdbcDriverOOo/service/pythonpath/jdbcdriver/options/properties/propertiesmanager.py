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

from .propertiesview import PropertiesView

from .propertieshandler import WindowHandler

from .property import PropertyManager

import traceback


class PropertiesManager():
    def __init__(self, ctx, window, manager):
        self._view = PropertiesView(ctx, window, WindowHandler(manager))
        self._property = PropertyManager(ctx, self._view.getWindow())

        print("PropertiesManager.__init__()")

# PropertiesManager getter methods
    def getPropertiesItem(self):
        return self._view.getPropertiesItem()

    def getPropertiesIndex(self):
        return self._view.getPropertiesIndex()

    def getPropertyName(self):
        return self._view.getPropertyName()

# PropertiesManager setter methods
    def dispose(self):
        self._view.dispose()

    def setProperties(self, properties, index, updatable=True):
        self._view.setProperties(properties, updatable)
        if index < len(properties):
            self._view.setProperty(index)

    def setProperty(self, value, updatable=True):
        self._property.setProperty(value, updatable)

    def editProperty(self):
        self._view.setPropertyName(self._view.getPropertiesItem())
        self._view.enableConfirm(False)
        self._view.setStep(2)

    def addProperty(self):
        self._view.clearPropertyName()
        self._view.enableConfirm(False)
        self._view.setStep(2)
        self._property.setProperty(None)

    def enableConfirm(self, enable):
        self._view.enableConfirm(enable)

    def exitEdit(self):
        self._view.setStep(1)
