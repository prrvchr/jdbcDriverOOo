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

from ....unotool import getContainerWindow

from ....configuration import g_identifier

import traceback


class PropertiesView():
    def __init__(self, ctx, window, handler):
        self._window = getContainerWindow(ctx, window.getPeer(), handler, g_identifier, 'PropertiesWindow')
        self._window.setVisible(True)
        self.setStep(1)

# PropertiesView getter methods
    def getWindow(self):
        return self._window

    def getPropertiesItem(self):
        return self._getProperties().getSelectedItem()

    def getPropertyName(self):
        return self._getPropertyName().Text

# PropertiesView setter methods
    def dispose(self):
        self._window.dispose()

    def setProperties(self, values, updatable):
        control = self._getProperties()
        # XXX: Need to clear ListBox so that the Handle fires for the same selection
        control.Model.StringItemList = ()
        if values:
            control.Model.StringItemList = values
            self._enableEdit(updatable)
            self._getRemoveProperty().Model.Enabled = updatable
        else:
            self._enableEdit(False)
            self._getRemoveProperty().Model.Enabled = False
        self._getAddProperty().Model.Enabled = updatable

    def selectProperty(self, index):
        self._getProperties().selectItemPos(index, True)

    def enableConfirm(self, enable):
        self._getConfirm().Model.Enabled = enable

    def setDefaultFocus(self):
        self._getEditProperty().setFocus()

    def setStep(self, step):
        self._window.Model.Step = step

    def clearPropertyName(self):
        self.setPropertyName('')

    def setPropertyName(self, value):
        self._getPropertyName().Text = value

# PropertiesView private setter methods
    def _enableEdit(self, enable):
        self._getEditProperty().Model.Enabled = enable

# PropertiesView private control methods
    def _getProperties(self):
        return self._window.getControl('ListBox1')

    def _getPropertyName(self):
        return self._window.getControl('TextField1')

    def _getEditProperty(self):
        return self._window.getControl('CommandButton1')

    def _getAddProperty(self):
        return self._window.getControl('CommandButton2')

    def _getRemoveProperty(self):
        return self._window.getControl('CommandButton3')

    def _getConfirm(self):
        return self._window.getControl('CommandButton4')

