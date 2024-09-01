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

import unohelper

from ...unotool import getDialog

from ...configuration import g_identifier


class GranteeView(unohelper.Base):
    def __init__(self, ctx, xdl, handler, parent):
        self._dialog = getDialog(ctx, g_identifier, xdl, handler, parent)
        self._background = {True: 16777215, False: 15790320}

    def execute(self):
        return self._dialog.execute()

    def dispose(self):
        return self._dialog.dispose()

    def getGrantee(self):
        return self._getName().Text

    def getPassword(self):
        return self._getPassword().Text

    def getConfirmation(self):
        return self._getConfirmation().Text

    def enableOk(self, enabled):
        self._getOk().Model.Enabled = enabled

    def enableConfirmation(self, enabled):
        control = self._getConfirmation()
        control.Model.Enabled = enabled
        if not enabled:
            control.Text = ""

    def setRoleBackground(self, enabled):
        self._getName().Model.BackgroundColor = self._background.get(enabled)

    def setConfirmationBackground(self, enabled):
        self._getConfirmation().Model.BackgroundColor = self._background.get(enabled)

    def _getName(self):
        return self._dialog.getControl('TextField1')

    def _getPassword(self):
        return self._dialog.getControl('TextField2')

    def _getConfirmation(self):
        return self._dialog.getControl('TextField3')

    def _getOk(self):
        return self._dialog.getControl('CommandButton2')

