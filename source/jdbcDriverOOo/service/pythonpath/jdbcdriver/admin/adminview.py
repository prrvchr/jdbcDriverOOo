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

import uno
import unohelper

from com.sun.star.view.SelectionType import SINGLE

from ..unotool import getDialog

from ..configuration import g_identifier


class AdminView(unohelper.Base):
    def __init__(self, ctx, xdl, handler, parent):
        self._dialog = getDialog(ctx, g_identifier, xdl, handler, parent)

    def initGrantees(self, grantees, grantee=None):
        control = self._getGrantees()
        control.Model.StringItemList = grantees
        if grantee is not None:
            control.selectItem(grantee, True)
        elif control.ItemCount > 0:
            control.selectItemPos(0, True)
        else:
            self.enableButton(False, False, False, False)

    def execute(self):
        return self._dialog.execute()

    def dispose(self):
        self._dialog.dispose()

    def getGridWindow(self):
        return self._getGridWindow()

    def enableButton(self, enabled, user, role, removable):
        self._getSetGrantee().Model.Enabled = enabled
        self._getDropGrantee().Model.Enabled = enabled and removable
        self._enableSetRole(role)

    def enableSetPrivileges(self, enabled):
        self._getSetPrivileges().Model.Enabled = enabled

    def enableRoleCreation(self, enabled):
        self._getAddGrantee().Model.Enabled = enabled
        self._getDropGrantee().Model.Enabled = enabled

    def getPeer(self):
        return self._dialog.getPeer()

    def _getGrantees(self):
        return self._dialog.getControl('ListBox1')

    def _getAddGrantee(self):
        return self._dialog.getControl('CommandButton1')

    def _getSetGrantee(self):
        return self._dialog.getControl('CommandButton2')

    def _getDropGrantee(self):
        return self._dialog.getControl('CommandButton3')

    def _getGridWindow(self):
        return self._dialog.getControl('FrameControl1')

    def _getSetPrivileges(self):
        return self._dialog.getControl('CommandButton4')

