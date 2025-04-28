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

from .propertiesview import PropertiesView

from .propertieshandler import WindowHandler

from .property import PropertyManager

import traceback


class PropertiesManager():
    def __init__(self, ctx, window, manager):
        self._view = PropertiesView(ctx, window, WindowHandler(manager))
        self._property = PropertyManager(ctx, self._view.getWindow(), manager)

# PropertiesManager getter methods
    def getPropertiesItem(self):
        return self._view.getPropertiesItem()

    def getPropertyName(self):
        return self._view.getPropertyName()

    def getPropertyValue(self):
        return self._property.getPropertyValue()

    def getValuesIndex(self):
        return self._property.getValuesIndex()

    def getTypesIndex(self):
        return self._property.getTypesIndex()

    def getCheckBoxValue(self):
        return self._property.getCheckBoxValue()

    def getNumFieldValue(self):
        return self._property.getNumFieldValue()

    def getListBoxValue(self):
        return self._property.getListBoxValue()

    def getTextFieldValue(self):
        return self._property.getTextFieldValue()

# PropertiesManager setter methods
    def dispose(self):
        self._property.dispose()
        self._view.dispose()

    def setProperties(self, properties, updatable=True):
        self._view.setProperties(properties, updatable)

    def selectProperty(self, index):
        self._view.selectProperty(index)

    def editProperty(self):
        self._view.setPropertyName(self._view.getPropertiesItem())
        self._view.enableConfirm(False)
        self._view.setStep(2)

    def addProperty(self):
        self._view.clearPropertyName()
        self._view.enableConfirm(False)
        self._view.setStep(2)
        self._property.initAddValues()
        self._property.enableTypes(True)

    def enableConfirm(self, enable):
        self._view.enableConfirm(enable)

    def exitEditProperty(self):
        self._view.setStep(1)

    def setPropertyFocus(self):
        self._view.setDefaultFocus()

    def setValueFocus(self):
        self._property.setDefaultFocus()

    # PropertyManager entries
    def selectType(self, index):
        self._property.selectType(index)

    def enableTypes(self, enabled):
        self._property.enableTypes(enabled)

    def selectPropertyValue(self, index):
        self._property.selectPropertyValue(index)

    def setCheckBoxValue(self, value, updatable):
        self._property.setCheckBoxValue(value, updatable)

    def setNumFieldValue(self, value, updatable):
        self._property.setNumFieldValue(value, updatable)

    def setListBoxValue(self, value, updatable):
        self._property.setListBoxValue(value, updatable)

    def setTextFieldValue(self, value, updatable):
        self._property.setTextFieldValue(value, updatable)

    # PropertyWindow.xdl handler entries
    def setType(self, index):
        self._property.setType(index)

    def editValue(self):
        self._property.editValue()

    def addValue(self):
        self._property.addValue()

    def cancelValue(self):
        self._property.cancelValue()

