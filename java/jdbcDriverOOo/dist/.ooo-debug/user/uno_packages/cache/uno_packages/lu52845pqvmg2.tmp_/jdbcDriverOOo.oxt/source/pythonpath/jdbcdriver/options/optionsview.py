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
    def __init__(self, window):
        self._window = window

# OptionsView setter methods
    def setLevel(self, level, updated):
        self._getLevel(level).State = 1
        if updated:
            self.disableLevel()

    def disableLevel(self):
        self._getLevel(1).Model.Enabled = False
        self._getLevel(2).Model.Enabled = False

    def setProtocols(self, protocols):
        control = self._getProtocols()
        # TODO: We need to reset the control in order the handler been called
        control.Model.removeAllItems()
        control.Model.StringItemList = protocols
        if control.getItemCount() > 0:
            control.selectItemPos(0, True)

    def setSubProtocol(self, protocol):
       self._getSubProtocol().Text = protocol

    def setDriverName(self, name):
       self._getDriverName().Text = name

    def setDriverClass(self, name):
       self._getDriverClass().Text = name

    def setDriverArchive(self, name):
       self._getDriverArchive().Text = name

    def setVersion(self, version):
        self._getVersion().Text = version

# OptionsView private control methods
    def _getLevel(self, index):
        return self._window.getControl('OptionButton%s' % index)

    def _getProtocols(self):
        return self._window.getControl('ListBox1')

    def _getSubProtocol(self):
        return self._window.getControl('TextField1')

    def _getDriverName(self):
        return self._window.getControl('TextField2')

    def _getDriverClass(self):
        return self._window.getControl('TextField3')

    def _getDriverArchive(self):
        return self._window.getControl('TextField4')

    def _getVersion(self):
        return self._window.getControl('Label2')
