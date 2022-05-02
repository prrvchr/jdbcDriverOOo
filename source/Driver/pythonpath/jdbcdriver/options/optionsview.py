#!
# -*- coding: utf_8 -*-

"""
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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

import unohelper

import traceback


class OptionsView(unohelper.Base):
    def __init__(self, window, reboot):
        self._window = window
        self._window.Model.Step = 1
        # XXX: If we modify the Dialog.Model.Step, we need
        # XXX: to restore the visibility of the control
        self._getReboot().setVisible(reboot)

# OptionsView getter methods
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
        return self._getLevels().getSelectedItem()

# OptionsView setter methods
    def setLevel(self, level, updated):
        self._getLevel(level).State = 1
        if updated:
            self.disableLevel()

    def disableLevel(self):
        self._getLevel(1).Model.Enabled = False
        self._getLevel(2).Model.Enabled = False

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

    def setLogger(self, enabled, level='0'):
        control = self._getLevels()
        control.Model.Enabled = enabled
        position = control.getSelectedItemPos()
        if enabled:
            control.selectItem(level, True)
        elif position != -1:
            control.selectItemPos(position, False)

    def enableButton(self, enabled):
        self._getRemove().Model.Enabled = enabled
        self._getUpdate().Model.Enabled = enabled

    def enableAdd(self, reboot):
        self._window.Model.Step = 2
        # XXX: If we modify the Dialog.Model.Step, we need
        # XXX: to restore the visibility of the control
        self._getReboot().setVisible(reboot)
        self._getNew().Model.Enabled = False
        self._getRemove().Model.Enabled = False
        self._getUpdate().Model.Enabled = True
        self.setLogger(False)
        self._getNewSubProtocol().setFocus()

    def disableAdd(self, enabled, reboot):
        self._window.Model.Step = 1
        # XXX: If we modify the Dialog.Model.Step, we need
        # XXX: to restore the visibility of the control
        self._getReboot().setVisible(reboot)
        self._getNew().Model.Enabled = True
        self._getRemove().Model.Enabled = enabled
        self._getUpdate().Model.Enabled = enabled

    def exitAdd(self, reboot):
        self._window.Model.Step = 1
        # XXX: If we modify the Dialog.Model.Step, we need
        # XXX: to restore the visibility of the control
        self._getReboot().setVisible(reboot)
        self._getNew().Model.Enabled = True

    def clearAdd(self, reboot):
        self.exitAdd(reboot)
        self._getNewSubProtocol().Text = ''
        self._getNewName().Text = ''
        self._getNewClass().Text = ''
        self._getNewArchive().Text = ''

    def enableProtocols(self, enabled):
        control = self._getProtocols()
        self._enableProtocols(control, enabled)

    def _enableProtocols(self, control, enabled, protocol=None):
        if enabled:
            if protocol is not None:
                control.selectItem(protocol, True)
            elif control.getItemCount() > 0:
                control.selectItemPos(0, True)
            control.Model.Enabled = enabled
        else:
            control.Model.Enabled = enabled
            position = control.getSelectedItemPos()
            if position != -1:
                control.selectItemPos(position, False)

    def enableSave(self, enabled):
        self._getSave().Model.Enabled = enabled

    def setNewArchive(self, archive):
        self._getNewArchive().Text = archive

    def setReboot(self, state):
        self._getReboot().setVisible(state)

# OptionsView private control methods
    def _getLevel(self, index):
        return self._window.getControl('OptionButton%s' % index)

    def _getProtocols(self):
        return self._window.getControl('ListBox1')

    def _getLevels(self):
        return self._window.getControl('ListBox2')

    def _getVersion(self):
        return self._window.getControl('Label5')

    def _getReboot(self):
        return self._window.getControl('Label11')

    def _getSubProtocol(self):
        return self._window.getControl('TextField1')

    def _getName(self):
        return self._window.getControl('TextField3')

    def _getClass(self):
        return self._window.getControl('TextField5')

    def _getArchive(self):
        return self._window.getControl('TextField7')

    def _getNewSubProtocol(self):
        return self._window.getControl('TextField2')

    def _getNewName(self):
        return self._window.getControl('TextField4')

    def _getNewClass(self):
        return self._window.getControl('TextField6')

    def _getNewArchive(self):
        return self._window.getControl('TextField8')

    def _getNew(self):
        return self._window.getControl('CommandButton1')

    def _getRemove(self):
        return self._window.getControl('CommandButton2')

    def _getSave(self):
        return self._window.getControl('CommandButton3')

    def _getUpdate(self):
        return self._window.getControl('CommandButton5')

