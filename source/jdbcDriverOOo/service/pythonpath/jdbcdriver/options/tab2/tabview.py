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

from ...unotool import getContainerWindow

from ...configuration import g_identifier

import traceback


class TabView():
    def __init__(self, ctx, window, handler, xdl):
        self._window = getContainerWindow(ctx, window.getPeer(), handler, g_identifier, xdl)
        self._window.setVisible(True)

# TabView getter methods
    def getWindow(self):
        return self._window

    def getDriver(self):
        return self._getDrivers().getSelectedItem()

    def getDriverIndex(self):
        return self._getDrivers().getSelectedItemPos()

    def getDriverName(self):
        return self._getDriverName().Text

    def getGroup(self):
        return self._getGroups().getSelectedItem()

    def getGroupIndex(self):
        return self._getGroups().getSelectedItemPos()

# TabView setter methods
    def dispose(self):
        self._window.dispose()

    def setVersion(self, version):
        self._getVersion().Text = version

    def setDefaultFocus(self):
        self._getEditDriver().setFocus()

    def setStep(self, step):
        self._window.Model.Step = step

    def enableConfirm(self, enable):
        self._getConfirm().Model.Enabled = enable

    def enableDriverName(self, enable):
        self._getDriverName().Model.Enabled = enable

    def setDrivers(self, drivers):
        control = self._getDrivers()
        item = control.getSelectedItem()
        # XXX: Need to clear ListBox so that the Handle fires for the same selection
        control.Model.StringItemList = ()
        if drivers:
            control.Model.StringItemList = drivers
            if item and item in drivers:
                control.selectItemPos(drivers.index(item), True)

    def setDriver(self, protocol, name, groups, version, updatable):
        self._getSubProtocol().Text = protocol
        self._getDriverName().Text = name
        control = self._getGroups()
        # XXX: Need to clear ListBox so that the Handle fires for the same selection
        control.Model.StringItemList = ()
        if groups:
            control.Model.StringItemList = groups
        self.setVersion(version)
        self.enableDriver(updatable)

    def setDriverName(self, name):
        self._getDriverName().Text = name

    def selectDriver(self, index):
        self._getDrivers().selectItemPos(index, True)

    def selectGroup(self, index):
        self._getGroups().selectItemPos(index, True)

    def selectProperty(self, index):
        self._getProperties().selectItemPos(index, True)

    def enableDriver(self, enabled):
        self._getEditDriver().Model.Enabled = enabled
        self._getRemoveDriver().Model.Enabled = enabled
        self._getUpdateDriver().Model.Enabled = enabled

# TabView private control methods
    def _getDrivers(self):
        return self._window.getControl('ListBox1')

    def _getEditDriver(self):
        return self._window.getControl('CommandButton1')

    def _getAddDriver(self):
        return self._window.getControl('CommandButton2')

    def _getRemoveDriver(self):
        return self._window.getControl('CommandButton3')

    def _getConfirm(self):
        return self._window.getControl('CommandButton4')

    def _getUpdateDriver(self):
        return self._window.getControl('CommandButton6')

    def _getSubProtocol(self):
        return self._window.getControl('TextField1')

    def _getDriverName(self):
        return self._window.getControl('TextField2')

    def _getGroups(self):
        return self._window.getControl('ListBox2')

    def _getVersion(self):
        return self._window.getControl('Label6')

