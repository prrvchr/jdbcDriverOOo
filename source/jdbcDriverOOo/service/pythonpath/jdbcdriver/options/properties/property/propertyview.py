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

from ....unotool import getContainerWindow

from ....configuration import g_identifier

import traceback


class PropertyView():
    def __init__(self, ctx, window, handler):
        self._window = getContainerWindow(ctx, window.getPeer(), handler, g_identifier, 'PropertyWindow')
        self._window.setVisible(True)

# PropertyView getter methods
    def getValuesIndex(self):
        return self._getValues().getSelectedItemPos()

    def getValuesItem(self):
        return self._getValues().getSelectedItem()

    def getValue(self):
        return self._getValue().Text

# PropertyView setter methods
    def dispose(self):
        self._window.dispose()

    def setProperty(self, value, index, updatable):
        self._getTypes().selectItemPos(index, True)
        #self.setType(index)
        if type(value) == bool:
            control = self._getBooleanValue()
            control.State = int(value)
        elif type(value) == tuple:
            control = self._getValues()
            self._setValues(control, value, 0)
        else:
            control = self._getStringValue()
            control.Text = value
        control.Model.Enabled = updatable
        self.enableTypes(False)

    def setType(self, index):
        print("PropertyView.setType() type: %s" % index)
        self.setStep(index + 1)

    def selectType(self, index):
        print("PropertyView.selectType() type: %s" % index)
        self._getTypes().selectItemPos(index, True)

    def setStep(self, step):
        print("PropertyView.setStep() step: %s" % step)
        self._window.Model.Step = step

    def setValues(self, values, index):
        self._setValues(self._getValues(), values, index)

    def enableTypes(self, enable):
        self._getTypes().Model.Enabled = enable

    def clearValue(self):
        self.setValue('')

    def setValue(self, value):
        self._getValue().Text = value

# PropertyView private methods
    def _setValues(self, control, values, index):
        control.Model.StringItemList = values
        if values:
            control.selectItemPos(index, True)

# PropertyView private control methods
    def _getTypes(self):
        return self._window.getControl('ListBox1')

    def _getBooleanValue(self):
        return self._window.getControl('CheckBox1')

    def _getStringValue(self):
        return self._window.getControl('TextField1')

    def _getValues(self):
        return self._window.getControl('ListBox2')

    def _getValue(self):
        return self._window.getControl('TextField2')

    def _getEditValue(self):
        return self._window.getControl('CommandButton1')

    def _getAddValue(self):
        return self._window.getControl('CommandButton2')

    def _getRemoveValue(self):
        return self._window.getControl('CommandButton3')

    def _getConfirm(self):
        return self._window.getControl('CommandButton4')

    def _getCancel(self):
        return self._window.getControl('CommandButton5')

