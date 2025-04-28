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

from .....unotool import getContainerWindow

from .....configuration import g_identifier

import traceback


class PropertyView():
    def __init__(self, ctx, window, handler):
        self._window = getContainerWindow(ctx, window.getPeer(), handler, g_identifier, 'PropertyWindow')
        self._window.setVisible(True)

# PropertyView getter methods
    def getValuesIndex(self):
        return self._getListBox().getSelectedItemPos()

    def getValuesItem(self):
        return self._getListBox().getSelectedItem()

    def getTypesIndex(self):
        return self._getTypes().getSelectedItemPos()

    def getCheckBoxValue(self):
        return bool(self._getCheckBox().State)

    def getNumFieldValue(self):
        return int(self._getNumField().Value)

    def getListBoxValue(self):
        return self._getListBox().Model.StringItemList

    def getTextFieldValue(self):
        return self._getTextField().Text

    def getValue(self):
        return self._getValue().Text

# PropertyView setter methods
    def dispose(self):
        self._window.dispose()

    def setCheckBoxValue(self, value, updatable):
        control = self._getCheckBox()
        control.State = int(value)
        control.Model.Enabled = updatable

    def setNumFieldValue(self, value, updatable):
        control = self._getNumField()
        # FIXME: numericfield does not display anything if the value of the control
        # FIXME: does not change, in this case it is necessary to change the value.
        if value == int(control.Value):
            control.Value += 1
        control.Value = value
        control.Model.Enabled = updatable

    def setListBoxValue(self, value, updatable):
        control = self._getListBox()
        control.Model.StringItemList = value
        control.Model.Enabled = updatable
        enabled = len(value) > 0
        self._enableButton(updatable, enabled)

    def setTextFieldValue(self, value, updatable):
        control = self._getTextField()
        control.Text = value
        control.Model.HelpText = value
        control.Model.Enabled = updatable

    def setType(self, index):
        self.setStep(index + 1)

    def selectType(self, index):
        self._getTypes().selectItemPos(index, True)

    def selectPropertyValue(self, index):
        if index != -1:
            self._getListBox().selectItemPos(index, True)

    def setDefaultFocus(self):
        self._getEditValue().setFocus()

    def setStep(self, step):
        self._window.Model.Step = step

    def enableTypes(self, enable):
        self._getTypes().Model.Enabled = enable

    def initAddValues(self):
        self._getCheckBox().Model.Enabled = True
        self._getNumField().Model.Enabled = True
        self._getTextField().Model.Enabled = True
        self._getEditValue().Model.Enabled = False
        self._getAddValue().Model.Enabled = True
        self._getRemoveValue().Model.Enabled = False

    def clearValue(self):
        self.setValue('')

    def setValue(self, value):
        self._getValue().Text = value

# PropertyView private methods
    def _enableButton(self, updatable, enabled):
        self._getEditValue().Model.Enabled = updatable and enabled
        self._getAddValue().Model.Enabled = updatable
        self._getRemoveValue().Model.Enabled = updatable and enabled

# PropertyView private control methods
    def _getTypes(self):
        return self._window.getControl('ListBox1')

    def _getCheckBox(self):
        return self._window.getControl('CheckBox1')

    def _getNumField(self):
        return self._window.getControl('NumericField1')

    def _getTextField(self):
        return self._window.getControl('TextField1')

    def _getListBox(self):
        return self._window.getControl('ListBox2')

    def _getValue(self):
        return self._window.getControl('TextField2')

    def _getEditValue(self):
        return self._window.getControl('CommandButton1')

    def _getAddValue(self):
        return self._window.getControl('CommandButton2')

    def _getRemoveValue(self):
        return self._window.getControl('CommandButton3')

