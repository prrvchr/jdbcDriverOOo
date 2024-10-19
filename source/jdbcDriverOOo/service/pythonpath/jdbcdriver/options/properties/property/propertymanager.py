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

from .propertyhandler import WindowHandler

from .propertyview import PropertyView

import traceback


class PropertyManager():
    def __init__(self, ctx, window):
        self._value = None
        self._newvalue = None
        self._isnew = False
        # FIXME: The supported types must follow the display order of the Property Type ListBox
        self._types = (bool, str, tuple)
        self._default = str
        self._view = PropertyView(ctx, window, WindowHandler(self))
        print("PropertyManager.__init__()")
        self._disabled = True
        self._view.selectType(self._types.index(self._default))

    def dispose(self):
        self._view.dispose()

# PropertyManager getter methods
    def hasChange(self):
        return self._value != self._newvalue

    def getValue(self):
        return self._newvalue

# PropertyManager setter methods
    def setProperty(self, value, updatable=True):
        self._value = value
        self._newvalue = value
        if value is None:
            self.setType(self._types.index(self._default))
            self._view.enableType(True)
        else:
            index = self._types.index(type(value))
            self._view.setProperty(value, index, updatable)

    def setType(self, index):
        print("PropertyManager.setType() 1 type: %s _ disabled: %s" % (index, self._disabled))
        if self._disabled:
            self._disabled = False
            return
        print("PropertyManager.setType() 2 type: %s" % index)
        self._view.setType(index)

    def setValue(self, value):
        self._newvalue = value
        print("PropertyManager.setValue() value: %s" % value)

    def setListValue(self, value):
        if self._value is None:
            self._value = ()
        if self._newvalue is None:
            values = list(self._value)
        else:
            values = list(self._newvalue)
        index = self._view.getValuesIndex()
        values[index] = value
        self._newvalue = tuple(values)
        self._view.setValues(self._newvalue, index)
        print("PropertyManager.setListValue() value: %s" % value)

    def editValue(self):
        self._isnew = False
        self._view.setValue(self._view.getValuesItem())
        self._view.setStep(4)

    def addValue(self):
        self._isnew = True
        self._view.clearValue()
        self._view.setStep(4)

    def removeValue(self):
        values = list(self._newvalue)
        index = self._view.getValuesIndex()
        values.pop(index)
        self._newvalue = tuple(values)
        self._view.setValues(self._newvalue, max(0, index - 1))

    def confirm(self):
        if self._isnew:
            values = list(self._newvalue)
            values.append(self._view.getValue())
            self._newvalue = tuple(values)
            index = len(values) - 1
        else:
            values = list(self._newvalue)
            index = self._view.getValuesIndex()
            values[index] = self._view.getValue()
            self._newvalue = tuple(values)
        self._view.setValues(self._newvalue, index)
        self.cancel()

    def cancel(self):
        self._view.setStep(3)
