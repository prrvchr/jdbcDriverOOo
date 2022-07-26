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

from .griddata import GridData
from .adminmodel import AdminModel
from .adminview import AdminView
from .gridlistener import GridListener
from .privilegeview import PrivilegeView
from .addgranteeview import AddGranteeView
from .grouphandler import AddGroupHandler
from .grouphandler import SetGroupHandler
from .userhandler import AddUserHandler
from .userhandler import ChangePasswordHandler
from .userview import UserView

from jdbcdriver import createMessageBox

import traceback


class AdminManager(unohelper.Base):
    def __init__(self, ctx, tables, grantees, user, xdl, handler, parent):
        self._ctx = ctx
        self._flags = {1: SELECT, 2: INSERT, 3: UPDATE, 4: DELETE, 5: READ, 6: CREATE, 7: ALTER, 8: REFERENCE, 9: DROP}
        data = GridData(ctx, grantees, tables.getElementNames(), self._flags)
        self._model = AdminModel(ctx, user, grantees, tables, data, self._flags)
        self._dialog = None
        self._disabled = True
        self._view = AdminView(ctx, xdl, handler, parent)
        self._view.init(*self._model.getInitData(), GridListener(self))

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

    def dispose(self):
        self._view.dispose()

    def setGrantee(self, grantee):
        self._model.setGrantee(grantee)
        self._view.enableButton()
        index = self._view.getSelectedGridIndex()
        enabled = self._model.getGrantablePrivileges(index) != 0
        self._view.enableSetPrivileges(enabled)

    def addGroup(self):
        self._addGrantee('AddGroupDialog', AddGroupHandler(self))

    def addUser(self):
        self._addGrantee('AddUserDialog', AddUserHandler(self))

    def _addGrantee(self, xdl, handler):
        self._dialog = AddGranteeView(self._ctx, xdl, handler, self._view.getPeer())
        if self._dialog.execute() == OK:
            self._model.addGrantee(self._dialog.getGrantee())
        self._dialog.dispose()
        self._dialog = None

    def setGroup(self, group):
        self._dialog.enableOk(self._model.isNameValid(group))

    def setUser(self, user):
        pwd = self._dialog.getPassword()
        confirmation = self._dialog.getConfirmation()
        self._dialog.enableOk(self._model.isUserValid(user, pwd, confirmation))

    def setUserPassword(self, pwd):
        user = self._dialog.getGrantee()
        confirmation = self._dialog.getConfirmation()
        self._dialog.enableOk(self._model.isUserValid(user, pwd, confirmation))
        self._dialog.enableConfirmation(self._model.isPasswordValid(pwd))

    def setUserPasswordConfirmation(self, confirmation):
        user = self._dialog.getGrantee()
        pwd = self._dialog.getPassword()
        self._dialog.enableOk(self._model.isUserValid(user, pwd, confirmation))

    def setUsers(self):
        print("AdminManager.setUsers()")

    def changePassword(self):
        peer = self._view.getPeer()
        self._dialog = UserView(self._ctx, ChangePasswordHandler(self), peer)
        if self._dialog.execute() == OK:
            self._model.setUserPassword(self._dialog.getPassword())
        self._dialog.dispose()
        self._dialog = None

    def setPassword(self, pwd):
        confirmation = self._dialog.getConfirmation()
        self._dialog.enableOk(self._model.isPasswordConfirmed(pwd, confirmation))
 
    def setPasswordConfirmation(self, confirmation):
        pwd = self._dialog.getPassword()
        self._dialog.enableOk(self._model.isPasswordConfirmed(pwd, confirmation))

    def dropGroup(self):
        grantee = self._view.getSelectedGrantee()
        self._dropGrantee(grantee, *self._model.getDropGroupInfo(grantee))

    def dropUser(self):
        grantee = self._view.getSelectedGrantee()
        self._dropGrantee(grantee, *self._model.getDropUserInfo(grantee))

    def _dropGrantee(self, grantee, message, title):
        dialog = createMessageBox(self._view.getPeer(), message, title, 'query')
        if dialog.execute() == OK:
            self._model.dropGrantee(grantee)
        dialog.dispose()

    def changeGridSelection(self, index):
        enabled = self._model.getGrantablePrivileges(index) != 0
        self._view.enableSetPrivileges(enabled)

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

