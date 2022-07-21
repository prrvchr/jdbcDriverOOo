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

import uno
import unohelper

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from com.sun.star.sdbcx.Privilege import SELECT
from com.sun.star.sdbcx.Privilege import INSERT
from com.sun.star.sdbcx.Privilege import UPDATE
from com.sun.star.sdbcx.Privilege import DELETE
from com.sun.star.sdbcx.Privilege import READ
from com.sun.star.sdbcx.Privilege import CREATE
from com.sun.star.sdbcx.Privilege import ALTER
from com.sun.star.sdbcx.Privilege import REFERENCE
from com.sun.star.sdbcx.Privilege import DROP

from com.sun.star.ui.dialogs.ExecutableDialogResults import OK

from .privilegeview import PrivilegeView

import traceback


class AdminManager(unohelper.Base):
    def __init__(self, ctx):
        self._ctx = ctx
        self._flags = {1: SELECT, 2: INSERT, 3: UPDATE, 4: DELETE, 5: READ, 6: CREATE, 7: ALTER, 8: REFERENCE, 9: DROP}
        self._disabled = True
        self._model = None
        self._view = None

    # TODO: One shot disabler handler
    def isHandlerEnabled(self):
        enabled = True
        if self._disabled:
            self._disabled = enabled = False
        return enabled
    def _disableHandler(self):
        self._disabled = True

    def execute(self):
        return self._view.execute()

    def setGrantee(self, grantee):
        self._model.setGrantee(grantee)
        self._view.enableButton()
        index = self._view.getSelectedGridIndex()
        enabled = self._model.getGrantablePrivileges(index) != 0
        self._view.enableSetPrivileges(enabled)

    def changeGridSelection(self, index):
        enabled = self._model.getGrantablePrivileges(index) != 0
        self._view.enableSetPrivileges(enabled)
        print("AdminManager.changeGridSelection(): %s" % enabled)

    def setPrivileges(self):
        index = self._view.getSelectedGridIndex()
        catalog, schema, title, privileges, grantables = self._model.getTableInfo(index)
        dialog = PrivilegeView(self._ctx, catalog, schema, title, privileges, grantables, self._flags)
        if dialog.execute() == OK:
            flags = dialog.getPrivileges(self._flags)
            if flags != privileges:
                self._model.setPrivileges(self._view.getSelectedGrantee(), index, *self._getPrivileges(privileges, flags))
        dialog.dispose()

    def _getPrivileges(self, privileges, flags):
        grant = revoke = 0
        for flag in self._flags:
            old = flag == privileges & flag
            new = flag == flags & flag
            if old == new:
                continue
            if new:
                grant += flag
                print("AdminManager.setPrivileges(): 1 ADD %s - %s - %s - %s" % (privileges, flags, flag, grant))
            else:
                revoke += flag
                print("AdminManager.setPrivileges(): 1 REVOKE %s - %s - %s - %s" % (privileges, flags, flag, revoke))
        print("AdminManager._getPrivileges(): GRANT / REVOKE: %s / %s" % (grant, revoke))
        return grant, revoke

