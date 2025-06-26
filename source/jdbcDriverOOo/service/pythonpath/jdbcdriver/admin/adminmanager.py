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

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from com.sun.star.sdbc import SQLException
from com.sun.star.container import ElementExistException
from com.sun.star.container import NoSuchElementException

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

from com.sun.star.view.SelectionType import SINGLE

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

from .gridmanager import GridManager
from .gridmodel import GridModel

from ..grid import GridListener

from ..unotool import createMessageBox
from ..unotool import getResourceLocation
from ..unotool import getStringResource

from ..configuration import g_extension
from ..configuration import g_identifier

from collections import OrderedDict
import traceback


class AdminManager(unohelper.Base):
    def __init__(self, ctx, view, connection, groups, isuser):
        self._ctx = ctx
        users = connection.getUsers()
        name = connection.getMetaData().getUserName()
        user = users.getByName(name) if users.hasByName(name) else None
        members = groups if isuser else users
        grantees = users if isuser else groups
        datasource = connection.getParent().Name
        tables = connection.getTables()
        columns = self._getTablePrivilegesSetting(connection)
        self._columns = tuple(columns.keys())
        self._flags = tuple(columns.values())
        self._view = view
        window = self._view.getGridWindow()
        url = getResourceLocation(ctx, g_identifier, 'img')
        model = GridModel(ctx, user, groups, grantees, tables, self._flags, isuser, url)
        quote = connection.getMetaData().getIdentifierQuoteString()
        resolver = getStringResource(ctx, g_identifier, 'dialogs', 'PrivilegesDialog')
        resources = (resolver, 'PrivilegesDialog.CheckBox%s.Label')
        manager = GridManager(ctx, datasource, GridListener(self), self._columns, url, model, window, quote, 'AdminGrid', SINGLE, resources, None, True)
        self._model = AdminModel(ctx, manager, users, members, tables, name)
        self._dialog = None
        self._disabled = True
        self._view.initGrantees(self._model.getGrantees())
        self._view.enableRoleCreation(self._model.supportsCreateRole())

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
        enabled = grantee is not None
        user, role, removable = self._model.setGrantee(grantee)
        self._view.enableButton(enabled, user, role, removable)
        enabled = False
        if self._model.hasGridSelectedRows():
            enabled = self._model.hasGrantablePrivileges()
        self._view.enableSetPrivileges(enabled)

    def createGroup(self):
        group = None
        self._dialog = GranteeView(self._ctx, 'NewGroupDialog', NewGroupHandler(self), self._view.getPeer())
        if self._dialog.execute() == OK:
            group = self._dialog.getGrantee()
        self._dialog.dispose()
        self._dialog = None
        if group is not None:
            try:
                group = self._model.createGroup(group)
                if group is not None:
                    self._updateGrantee(self._model.getGrantees(), group)
            except (SQLException, ElementExistException) as e:
                title = self._model.getCreateGroupErrorTitle()
                self._showDialogError(title, e)

    def createUser(self):
        user = None
        self._dialog = GranteeView(self._ctx, 'NewUserDialog', NewUserHandler(self), self._view.getPeer())
        if self._dialog.execute() == OK:
            user = self._dialog.getGrantee()
            password = self._dialog.getPassword()
        self._dialog.dispose()
        self._dialog = None
        if user is not None:
            try:
                user = self._model.createUser(user, password)
                if user is not None:
                    self._updateGrantee(self._model.getGrantees(), user)
            except (SQLException, ElementExistException) as e:
                title = self._model.getCreateUserErrorTitle()
                self._showDialogError(title, e)

    def setGroupName(self, group):
        self._dialog.enableOk(self._model.isNameValid(group))

    def setUserName(self, user):
        pwd = self._dialog.getPassword()
        confirmation = self._dialog.getConfirmation()
        enabled = self._model.isUserValid(user, pwd, confirmation)
        self._dialog.setRoleBackground(enabled)
        self._dialog.enableOk(enabled)

    def setUserPassword(self, pwd):
        user = self._dialog.getGrantee()
        confirmation = self._dialog.getConfirmation()
        self._dialog.enableOk(self._model.isUserValid(user, pwd, confirmation))
        self._dialog.enableConfirmation(self._model.isPasswordValid(pwd))

    def setUserPasswordConfirmation(self, confirmation):
        user = self._dialog.getGrantee()
        pwd = self._dialog.getPassword()
        self._dialog.setConfirmationBackground(confirmation == pwd)
        self._dialog.enableOk(self._model.isUserValid(user, pwd, confirmation))

    # XXX: Show group's user members.
    def setUsers(self):
        try:
            grantees, title, availables = self._model.getUsers()
        except SQLException as e:
            title = self._model.getSetMembersErrorTitle()
            self._showDialogError(title, e)
        else:
            roles = self._getRoles('UsersDialog', UsersHandler(self), grantees, title, availables, False)
            if roles is not None:
                self._setRoles(grantees, roles, False)

    # XXX: Show user's group members.
    def setGroups(self):
        try:
            grantees, title, availables = self._model.getGroups()
        except SQLException as e:
            title = self._model.getSetMembersErrorTitle()
            self._showDialogError(title, e)
        else:
            roles = self._getRoles('GroupsDialog', GroupsHandler(self), grantees, title, availables)
            if roles is not None:
                self._setRoles(grantees, roles)

    # XXX: Show group's role members.
    def setRoles(self):
        try:
            grantees, title, availables = self._model.getRoles()
        except SQLException as e:
            title = self._model.getSetMembersErrorTitle()
            self._showDialogError(title, e)
        else:
            roles = self._getRoles('RolesDialog', GroupsHandler(self), grantees, title, availables)
            if roles is not None:
                self._setRoles(grantees, roles)

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
        password = None
        self._dialog = PasswordView(self._ctx, PasswordHandler(self), self._view.getPeer())
        if self._dialog.execute() == OK:
            password = self._dialog.getPassword()
        self._dialog.dispose()
        self._dialog = None
        if password is not None:
            self._savePassword(password)

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
        enabled = self._model.hasGrantablePrivileges() if index != -1 else False
        self._view.enableSetPrivileges(enabled)

    def setPrivileges(self):
        try:
            table, privileges, flags = self._getPrivileges(*self._model.getPrivileges())
            if privileges == flags:
                return
            self._model.setPrivileges(table, *self._getPrivilegeValues(privileges, flags))
        except SQLException as e:
            title = self._model.getPrivilegeErrorTitle()
            self._showDialogError(title, e)

    def _savePassword(self, password):
        try:
            self._model.setUserPassword(password)
        except SQLException as e:
            title = self._model.getSetPasswordErrorTitle()
            self._showDialogError(title, e)

    def _getPrivileges(self, table, privileges, grantables, inherited):
        flags = privileges
        dialog = PrivilegeView(self._ctx, self._flags, table, self._columns, privileges, grantables, inherited)
        if dialog.execute() == OK:
            flags = dialog.getPrivileges(self._flags)
        dialog.dispose()
        return table, privileges, flags

    def _showDialogError(self, title, error):
        box = uno.Enum('com.sun.star.awt.MessageBoxType', 'ERRORBOX')
        msgbox = createMessageBox(self._view.getPeer(), box, 1, title, error.Message)
        msgbox.execute()
        msgbox.dispose()

    def _getRoles(self, xdl, handler, grantees, title, availables, isgroup=True):
        roles = None
        parent = self._view.getPeer()
        members = grantees.getElementNames()
        self._dialog = MemberView(self._ctx, xdl, handler, parent, title, members, availables)
        if self._dialog.execute() == OK:
            roles = self._dialog.getMembers()
        self._dialog.dispose()
        self._dialog = None
        return roles

    def _setRoles(self, grantees, roles, isgroup=True):
        try:
            self._model.setMembers(grantees, roles, isgroup)
        except (SQLException, ElementExistException, NoSuchElementException) as e:
            title = self._model.getSetMembersErrorTitle()
            self._showDialogError(title, e)

    def _getTablePrivilegesSetting(self, connection):
        columns = OrderedDict()
        for info in connection.getMetaData().getConnectionInfo():
            if info.Name == 'PrivilegesSettings':
                index = 0
                infos = info.Value
                count = self._getEvenLength(len(infos))
                while index < count:
                    key = infos[index].strip()
                    value = infos[index + 1]
                    if key and value.isdigit():
                        columns[key.title()] = int(value)
                    index += 2
                if len(columns):
                    break
        else:
            columns = {'Select': SELECT, 'Insert':     INSERT,    'Update': UPDATE,
                       'Delete': DELETE, 'Read':       READ,      'Create': CREATE,
                       'Alter':  ALTER,  'References': REFERENCE, 'Drop':   DROP}
        return columns

    def _getEvenLength(self, length):
        if (length % 2) != 0:
            return length - 1
        return length

    def _toogleOk(self, isgroup):
        enabled = self._model.isMemberModified(self._dialog.getMembers(), isgroup)
        self._dialog.enableOk(enabled)

    def _dropGrantee(self, message, title):
        box = uno.Enum('com.sun.star.awt.MessageBoxType', 'QUERYBOX')
        dialog = createMessageBox(self._view.getPeer(), box, 2, title, message)
        if dialog.execute() == OK:
            grantees = self._model.dropGrantee()
            self._updateGrantee(grantees)
        dialog.dispose()

    def _updateGrantee(self, grantees, grantee=None):
        self._disableHandler()
        self._view.initGrantees(grantees, grantee)

    def _getPrivilegeValues(self, privileges, flags):
        grant = revoke = 0
        for flag in self._flags:
            old = flag == privileges & flag
            new = flag == flags & flag
            if old == new:
                continue
            if new:
                grant |= flag
            else:
                revoke |= flag
        return grant, revoke

