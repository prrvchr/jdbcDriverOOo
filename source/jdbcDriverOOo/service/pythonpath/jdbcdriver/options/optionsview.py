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

import uno

from ..unotool import getContainerWindow

from ..configuration import g_identifier

import traceback


class OptionsView():
    def __init__(self, ctx, window, handler, listener, restart, title1, title2):
        self._tab = 'Tab1'
        self._window = window
        rectangle = uno.createUnoStruct('com.sun.star.awt.Rectangle', 0, 0, 260, 225)
        self._tab1, tab2 = self._getTabPages(window, rectangle, title1, title2)
        self._tab2 = getContainerWindow(ctx, tab2.getPeer(), handler, g_identifier, 'Option2Dialog')
        self._tab2.setVisible(True)
        self._getTab().addTabPageContainerListener(listener)
        self.setRestart(restart)

# OptionsView getter methods
    def getTab1(self):
        return self._tab1

    def getSelectedProtocol(self):
        return self._getProtocols().getSelectedItem()

    def getArchive(self):
        return self._getArchive().Text.strip()

    def getNewSubProtocol(self):
        return self._getNewSubProtocol().Text.strip().lower()

    def getNewName(self):
        return self._getNewName().Text.strip()

    def getNewClass(self):
        return self._getNewClass().Text.strip()

    def getNewArchive(self):
        return self._getNewArchive().Text.strip()

    def getLogger(self):
        level = -1
        control = self._getLevels()
        if control.Model.Enabled:
            level = control.getSelectedItemPos()
        return level

# OptionsView setter methods
    def dispose(self):
        self._tab2.dispose()

    def removeTabListener(self, listener):
        self._getTab().removeTabPageContainerListener(listener)

    def setProtocols(self, protocols, protocol):
        control = self._getProtocols()
        # XXX: Need to clear ListBox so that the Handle fires for the same selection
        control.Model.StringItemList = ()
        control.Model.StringItemList = protocols
        self._enableProtocols(control, True, protocol)

    def setSubProtocol(self, protocol):
        self._getSubProtocol().Text = protocol

    def setName(self, name):
        self._getName().Text = name

    def setClass(self, name):
        self._getClass().Text = name

    def setArchive(self, name):
        self._getArchive().Text = name

    def setVersion(self, version):
        self._getVersion().Text = version

    def setLogger(self, level=None):
        control = self._getLevels()
        enabled = level is not None
        selected = enabled and level != -1
        self._setLogger(enabled, selected)
        self._enableLoggerLevel(control, selected)
        position = control.getSelectedItemPos()
        if selected:
            control.selectItemPos(level, True)
        elif position != -1:
            control.selectItemPos(position, False)

    def enableLogger(self, enabled, state):
        control = self._getLevels()
        self._enableLoggerLevel(control, state)
        if not state:
            position = control.getSelectedItemPos()
            if position != -1:
                control.selectItemPos(position, False)

    def enableButton(self, enabled):
        self._getRemove().Model.Enabled = enabled
        self._getUpdate().Model.Enabled = enabled

    def enableAdd(self):
        self._setStep(2)
        # XXX: If we modify the Dialog.Model.Step, we need
        # XXX: to restore the visibility of the control
        self._getNew().Model.Enabled = False
        self._getRemove().Model.Enabled = False
        self._getUpdate().Model.Enabled = True
        self.setLogger()
        self._getNewSubProtocol().setFocus()

    def disableAdd(self, enabled):
        self._setStep(1)
        # XXX: If we modify the Dialog.Model.Step, we need
        # XXX: to restore the visibility of the control
        self._getNew().Model.Enabled = True
        self._getRemove().Model.Enabled = enabled
        self._getUpdate().Model.Enabled = enabled

    def exitAdd(self):
        self._setStep(1)
        # XXX: If we modify the Dialog.Model.Step, we need
        # XXX: to restore the visibility of the control
        self._getNew().Model.Enabled = True

    def clearAdd(self):
        self.exitAdd()
        self._getNewSubProtocol().Text = ''
        self._getNewName().Text = ''
        self._getNewClass().Text = ''
        self._getNewArchive().Text = ''

    def enableProtocols(self, enabled):
        control = self._getProtocols()
        self._enableProtocols(control, enabled)

    def enableSave(self, enabled):
        self._getSave().Model.Enabled = enabled

    def setNewArchive(self, archive):
        self._getNewArchive().Text = archive

    def setRestart(self, enabled):
        self._getRestart().setVisible(enabled)

# OptionsView private methods
    def _setLogger(self, enabled, selected):
        control = self._getLogger()
        control.Model.Enabled = enabled
        control.State = int(selected)

    def _enableLoggerLevel(self, control, enabled):
        self._getLevelLabel().Model.Enabled = enabled
        control.Model.Enabled = enabled

    def _enableProtocols(self, control, enabled, protocol=None):
        # XXX: We assume that the root protocol cannot be deleted
        if enabled:
            if protocol is not None:
                control.selectItem(protocol, True)
            else:
                control.selectItemPos(0, True)
            control.Model.Enabled = enabled
        else:
            control.Model.Enabled = enabled
            position = control.getSelectedItemPos()
            if position != -1:
                control.selectItemPos(position, False)

    def _setStep(self, step):
        self._tab2.Model.Step = step

    def _getTabPages(self, window, rectangle, title1, title2, i=1):
        model = self._getTabModel(window, rectangle)
        window.Model.insertByName(self._tab, model)
        tab = self._getTab()
        tab1 = self._getTabPage(model, tab, title1)
        tab2 = self._getTabPage(model, tab, title2)
        tab.ActiveTabPageID = i
        return tab1, tab2

    def _getTabModel(self, window, rectangle):
        service = 'com.sun.star.awt.tab.UnoControlTabPageContainerModel'
        model = window.Model.createInstance(service)
        model.PositionX = rectangle.X
        model.PositionY = rectangle.Y
        model.Width = rectangle.Width
        model.Height = rectangle.Height
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

    def _getRestart(self):
        return self._tab2.getControl('Label10')

    def _getProtocols(self):
        return self._tab2.getControl('ListBox1')

    def _getLevels(self):
        return self._tab2.getControl('ListBox2')

    def _getVersion(self):
        return self._tab2.getControl('Label4')

    def _getLogger(self):
        return self._tab2.getControl('CheckBox1')

    def _getLevelLabel(self):
        return self._tab2.getControl('Label9')

    def _getSubProtocol(self):
        return self._tab2.getControl('TextField1')

    def _getName(self):
        return self._tab2.getControl('TextField3')

    def _getClass(self):
        return self._tab2.getControl('TextField5')

    def _getArchive(self):
        return self._tab2.getControl('TextField7')

    def _getNewSubProtocol(self):
        return self._tab2.getControl('TextField2')

    def _getNewName(self):
        return self._tab2.getControl('TextField4')

    def _getNewClass(self):
        return self._tab2.getControl('TextField6')

    def _getNewArchive(self):
        return self._tab2.getControl('TextField8')

    def _getNew(self):
        return self._tab2.getControl('CommandButton1')

    def _getRemove(self):
        return self._tab2.getControl('CommandButton2')

    def _getSave(self):
        return self._tab2.getControl('CommandButton3')

    def _getUpdate(self):
        return self._tab2.getControl('CommandButton5')

