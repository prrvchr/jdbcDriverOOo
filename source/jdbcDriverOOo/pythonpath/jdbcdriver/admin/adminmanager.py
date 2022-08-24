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
from .gridlistener import GridListener
from .privilegeview import PrivilegeView
from .granteeview import GranteeView
from .memberview import MemberView
from .grouphandler import GroupsHandler
from .grouphandler import NewGroupHandler
from .userhandler import UsersHandler
from .userhandler import NewUserHandler
from .userhandler import PasswordHandler
from .passwordview import PasswordView

from jdbcdriver import createMessageBox

import traceback


class AdminManager(unohelper.Base):
    def __init__(self, ctx, view, connection, members, grantees, recursive, isuser):
        self._ctx = ctx
        tables = connection.getTables()
        users = connection.getUsers()
        user = users.getByName(connection.getMetaData().getUserName())
        self._flags = {1: SELECT, 2: INSERT, 3: UPDATE, 4: DELETE, 5: READ, 6: CREATE, 7: ALTER, 8: REFERENCE, 9: DROP}
        data = GridData(ctx, grantees, tables.getElementNames(), self._flags, recursive, isuser)
        self._model = AdminModel(ctx, user, members, grantees, tables, data, self._flags)
        self._dialog = None
        self._disabled = True
        self._view = view
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
        self._view.enableButton(self._model.setGrantee(grantee))
        index = self._view.getSelectedGridIndex()
        enabled = self._model.getGrantablePrivileges(index) != 0
        self._view.enableSetPrivileges(enabled)

    def createGroup(self):
        self._dialog = GranteeView(self._ctx, 'NewGroupDialog', NewGroupHandler(self), self._view.getPeer())
        if self._dialog.execute() == OK:
            grantee = self._dialog.getGrantee()
            grantees = self._model.createGroup(grantee)
            self._updateGrantee(grantees, grantee)
        self._dialog.dispose()
        self._dialog = None

    def createUser(self):
        self._dialog = GranteeView(self._ctx, 'NewUserDialog', NewUserHandler(self), self._view.getPeer())
        if self._dialog.execute() == OK:
            grantee = self._dialog.getGrantee()
            grantees = self._model.createUser(grantee, self._dialog.getPassword())
            self._updateGrantee(grantees, grantee)
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
        grantee = self._view.getSelectedGrantee()
        title = self._model.getUsersTitle(grantee)
        members = self._model.getUsers(grantee)
        self._setMembers(grantee, title, members, 'UsersDialog', UsersHandler(self), False)

    def setGroups(self):
        grantee = self._view.getSelectedGrantee()
        title = self._model.getGroupsTitle(grantee)
        members = self._model.getGroups(grantee)
        self._setMembers(grantee, title, members, 'GroupsDialog', GroupsHandler(self), True)

    def setRoles(self):
        grantee = self._view.getSelectedGrantee()
        title = self._model.getRolesTitle(grantee)
        members = self._model.getRoles(grantee)
        self._setMembers(grantee, title, members, 'RolesDialog', GroupsHandler(self), True)

    def _setMembers(self, grantee, title, members, xdl, handler, isgroup):
        self._dialog = MemberView(self._ctx, xdl, handler, self._view.getPeer(), title, *members)
        if self._dialog.execute() == OK:
            self._model.setMembers(grantee, self._dialog.getMembers(), isgroup)
        self._dialog.dispose()
        self._dialog = None

    def toogleRemove(self, enabled):
        self._dialog.toogleRemove(enabled)

    def removeUser(self):
        self._dialog.removeMember()
        self._toogleOk(False)

    def removeGroup(self):
        self._dialog.removeMember()
        self._toogleOk(True)

    def toogleAdd(self, enabled):
        self._dialog.toogleAdd(enabled)

    def addUser(self):
        self._dialog.addMember()
        self._toogleOk(False)

    def addGroup(self):
        self._dialog.addMember()
        self._toogleOk(True)

    def changePassword(self):
        self._dialog = PasswordView(self._ctx, PasswordHandler(self), self._view.getPeer())
        if self._dialog.execute() == OK:
            self._model.setUserPassword(self._view.getSelectedGrantee(), self._dialog.getPassword())
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

    def changeGridSelection(self, index):
        enabled = self._model.getGrantablePrivileges(index) != 0
        self._view.enableSetPrivileges(enabled)

    def setPrivileges(self):
        index = self._view.getSelectedGridIndex()
        table, privileges, grantables, inherited = self._model.getPrivileges(index)
        dialog = PrivilegeView(self._ctx, self._flags, table, privileges, grantables, inherited)
        if dialog.execute() == OK:
            flags = dialog.getPrivileges(self._flags)
            if flags != privileges:
                self._model.setPrivileges(self._view.getSelectedGrantee(), index, *self._getPrivileges(privileges, flags))
        dialog.dispose()

    def _toogleOk(self, isgroup):
        name = self._view.getSelectedGrantee()
        enabled = self._model.isMemberModified(name, self._dialog.getMembers(), isgroup)
        self._dialog.enableOk(enabled)

    def _dropGrantee(self, grantee, message, title):
        dialog = createMessageBox(self._view.getPeer(), message, title, 'query')
        if dialog.execute() == OK:
            grantees = self._model.dropGrantee(grantee)
            self._updateGrantee(grantees)
        dialog.dispose()

    def _updateGrantee(self, grantees, grantee=None):
        self._disableHandler()
        self._view.initGrantees(grantees, grantee)

    def _getPrivileges(self, privileges, flags):
        grant = revoke = 0
        for flag in self._flags.values():
            old = flag == privileges & flag
            new = flag == flags & flag
            if old == new:
                continue
            if new:
                grant |= flag
            else:
                revoke |= flag
        return grant, revoke

