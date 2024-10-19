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

from ..unotool import getContainerWindow

from ..configuration import g_identifier

import traceback


class OptionsView():
    def __init__(self, ctx, window, handler, listener, restart, title1, title2):
        self._tab = 'Tab1'
        self._window = window
        self._tab1, tab2 = self._getTabPages(window, title1, title2)
        self._tab2 = getContainerWindow(ctx, tab2.getPeer(), handler, g_identifier, 'Option2Dialog')
        self._tab2.Model.Step = 2
        self._tab2.setVisible(True)
        self._getTab().addTabPageContainerListener(listener)
        print("OptionsView.__init__() restart: %s" % restart)
        self.setStep(1, restart)

# OptionsView getter methods
    def getTab1(self):
        return self._tab1

    def getTab2(self):
        return self._tab2

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

# OptionsView setter methods
    def dispose(self):
        self._tab2.dispose()

    def removeTabListener(self, listener):
        self._getTab().removeTabPageContainerListener(listener)

    def setVersion(self, version):
        self._getVersion().Text = version

    def setStep(self, step, restart):
        self._tab2.Model.Step = step
        # XXX: If we change the step then we have to restore
        # XXX: the visibility of the controls because it was lost
        self.setRestart(restart)

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

    def setRestart(self, enabled):
        self._getRestart().setVisible(enabled)

# OptionsView private methods
    def _getTabPages(self, window, title1, title2, i=1):
        model = self._getTabModel(window)
        window.Model.insertByName(self._tab, model)
        tab = self._getTab()
        tab1 = self._getTabPage(model, tab, title1)
        tab2 = self._getTabPage(model, tab, title2)
        tab.ActiveTabPageID = i
        return tab1, tab2

    def _getTabModel(self, window):
        service = 'com.sun.star.awt.tab.UnoControlTabPageContainerModel'
        model = window.Model.createInstance(service)
        model.PositionX = window.Model.PositionX
        model.PositionY = window.Model.PositionY
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

    def _getDrivers(self):
        return self._tab2.getControl('ListBox1')

    def _getEditDriver(self):
        return self._tab2.getControl('CommandButton1')

    def _getAddDriver(self):
        return self._tab2.getControl('CommandButton2')

    def _getRemoveDriver(self):
        return self._tab2.getControl('CommandButton3')

    def _getConfirm(self):
        return self._tab2.getControl('CommandButton4')

    def _getUpdateDriver(self):
        return self._tab2.getControl('CommandButton6')

    def _getSubProtocol(self):
        return self._tab2.getControl('TextField1')

    def _getDriverName(self):
        return self._tab2.getControl('TextField2')

    def _getGroups(self):
        return self._tab2.getControl('ListBox2')

    def _getVersion(self):
        return self._tab2.getControl('Label6')

    def _getRestart(self):
        return self._tab2.getControl('Label8')

