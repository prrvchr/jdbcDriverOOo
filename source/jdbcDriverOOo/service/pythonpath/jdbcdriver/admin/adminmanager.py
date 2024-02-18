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

from .adminmodel import AdminModel

from .dialog import GranteeView
from .dialog import MemberView
from .dialog import PasswordView
from .dialog import PrivilegeView

from .adminhandler import GroupsHandler
from .adminhandler import NewGroupHandler

from .adminhandler import UsersHandler
from .adminhandler import NewUserHandler
from .adminhandler import PasswordHandler

from ..grid import GridModel
from ..grid import GridListener

from ..unotool import createMessageBox
from ..unotool import getResourceLocation

from ..configuration import g_extension
from ..configuration import g_identifier

import traceback


class AdminManager(unohelper.Base):
    def __init__(self, ctx, view, connection, members, grantees, recursive, isuser):
        self._ctx = ctx
        datasource = connection.getParent().Name
        tables = connection.getTables()
        self._flags = {1: SELECT, 2: INSERT, 3: UPDATE, 4: DELETE, 5: READ, 6: CREATE, 7: ALTER, 8: REFERENCE, 9: DROP}
        self._view = view
        window = self._view.getGridWindow()
        url = getResourceLocation(ctx, g_identifier, g_extension)
        model = GridModel(ctx, grantees, tables, self._flags, recursive, isuser, url)
        user = connection.getMetaData().getUserName()
        self._model = AdminModel(ctx, datasource, model, window, GridListener(self), url, user, members, tables, self._flags, 'AdminGrid')
        self._dialog = None
        self._disabled = True
        self._view.initGrantees(self._model.getGrantees())

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
        self._model.dispose()

    def setGrantee(self, grantee):
        self._view.enableButton(*self._model.setGrantee(grantee))
        enabled = False
        if self._model.hasGridSelectedRows():
            enabled = self._model.hasGrantablePrivileges()
        self._view.enableSetPrivileges(enabled)

    def createGroup(self):
        self._dialog = GranteeView(self._ctx, 'NewGroupDialog', NewGroupHandler(self), self._view.getPeer())
        if self._dialog.execute() == OK:
            grantees = self._model.createGroup(self._dialog.getGrantee())
            if grantees is not None:
                self._updateGrantee(*self._model.getNewGrantees(grantees))
        self._dialog.dispose()
        self._dialog = None

    def createUser(self):
        self._dialog = GranteeView(self._ctx, 'NewUserDialog', NewUserHandler(self), self._view.getPeer())
        if self._dialog.execute() == OK:
            grantees = self._model.createUser(self._dialog.getGrantee(), self._dialog.getPassword())
            if grantees is not None:
                self._updateGrantee(*self._model.getNewGrantees(grantees))
        self._dialog.dispose()
        self._dialog = None

    def setGroupName(self, group):
        self._dialog.enableOk(self._model.isNameValid(group))

    def setUserName(self, user):
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
        self._setMembers('UsersDialog', UsersHandler(self), self._model.getUsers(), False)

    def setGroups(self):
        self._setMembers('GroupsDialog', GroupsHandler(self), self._model.getGroups(), True)

    def setRoles(self):
        self._setMembers('RolesDialog', GroupsHandler(self), self._model.getRoles(), True)

    def _setMembers(self, xdl, handler, data, isgroup):
        self._dialog = MemberView(self._ctx, xdl, handler, self._view.getPeer(), *data)
        if self._dialog.execute() == OK:
            self._model.setMembers(self._dialog.getMembers(), isgroup)
        self._dialog.dispose()
        self._dialog = None

    def toogleRemoveUser(self, enabled, user):
        self._dialog.toogleRemove(self._model.isRemovableUser(enabled, user))

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
        self._dropGrantee(*self._model.getDropGroupInfo())

    def dropUser(self):
        self._dropGrantee(*self._model.getDropUserInfo())

    def changeGridSelection(self, index, grid):
        enabled = False
        if index != -1:
            enabled = self._model.hasGrantablePrivileges()
        self._view.enableSetPrivileges(enabled)

    def setPrivileges(self):
        table, privileges, grantables, inherited = self._model.getPrivileges()
        dialog = PrivilegeView(self._ctx, self._flags, table, privileges, grantables, inherited)
        if dialog.execute() == OK:
            flags = dialog.getPrivileges(self._flags)
            if flags != privileges:
                self._model.setPrivileges(table, *self._getPrivileges(privileges, flags))
        dialog.dispose()

    def _toogleOk(self, isgroup):
        enabled = self._model.isMemberModified(self._dialog.getMembers(), isgroup)
        self._dialog.enableOk(enabled)

    def _dropGrantee(self, message, title):
        dialog = createMessageBox(self._view.getPeer(), message, title, 'query')
        if dialog.execute() == OK:
            grantees = self._model.dropGrantee()
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

