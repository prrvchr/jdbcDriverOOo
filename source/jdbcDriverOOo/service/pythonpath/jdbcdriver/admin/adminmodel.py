#!
# -*- coding: utf-8 -*-

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

from com.sun.star.sdbcx.PrivilegeObject import TABLE

from com.sun.star.style.HorizontalAlignment import CENTER
from com.sun.star.style.HorizontalAlignment import LEFT

from com.sun.star.view.SelectionType import SINGLE

from com.sun.star.container import ElementExistException

from ..grid import GridManager

from ..unotool import createService
from ..unotool import getStringResource

from ..configuration import g_identifier
from ..configuration import g_extension

import traceback


class AdminModel(unohelper.Base):
    def __init__(self, ctx, datasource, model, window, listener, url, user, members, tables, flags, setting):
        self._grantee = '';
        self._user = user
        self._members = members
        self._tables = tables
        self._resolver = getStringResource(ctx, g_identifier, g_extension)
        self._resources = {'DropGroupTitle'   : 'MessageBox.DropGroup.Title',
                           'DropGroupMessage' : 'MessageBox.DropGroup.Message',
                           'DropUserTitle'    : 'MessageBox.DropUser.Title',
                           'DropUserMessage'  : 'MessageBox.DropUser.Message',
                           'UsersTitle'       : 'UsersDialog.Title',
                           'GroupsTitle'      : 'GroupsDialog.Title',
                           'RolesTitle'       : 'RolesDialog.Title'}
        resources = (self._resolver, 'PrivilegesDialog.CheckBox%s.Label')
        self._grid = GridManager(ctx, datasource, listener, flags, url, model, window, setting, SINGLE, resources, None, True)

    def _getColumn(self, column, title, index, width=70, flex=1, align=CENTER):
        column.ColumnWidth = width
        column.MinWidth = width
        column.HorizontalAlign = align
        column.Resizeable = True
        column.Flexibility = flex
        column.Title = title
        column.Identifier = index
        return column

    def dispose(self):
        self._grid.dispose()

    def getSelectedIdentifier(self):
        return self._grid.getSelectedIdentifier('0')

    def getGrantees(self):
        return self._grid.Model.getGrantees().getElementNames()

    def getDropGroupInfo(self):
        return self._getDropGroupMessage(), self._getDropGroupTitle()

    def getDropUserInfo(self):
        return self._getDropUserMessage(), self._getDropUserTitle()

    def isNameValid(self, name):
        return len(name) and name not in self._grid.Model.getGrantees().getElementNames()

    def isUserValid(self, user, pwd, confirmation):
        return self.isNameValid(user) and self.isPasswordConfirmed(pwd, confirmation)

    def isPasswordValid(self, pwd):
        return len(pwd)

    def isPasswordConfirmed(self, pwd, confirmation):
        return pwd == confirmation

    def createUser(self, user, password):
        descriptor = self._grid.Model.getGrantees().createDataDescriptor()
        descriptor.setPropertyValue('Password', password)
        return self._createGrantee(descriptor, user)

    def createGroup(self, group):
        descriptor = self._grid.Model.getGrantees().createDataDescriptor()
        return self._createGrantee(descriptor, group)

    def _createGrantee(self, descriptor, grantee):
        grantees = self._grid.Model.getGrantees().getElementNames()
        descriptor.setPropertyValue('Name', grantee)
        try:
            self._grid.Model.getGrantees().appendByDescriptor(descriptor)
        except ElementExistException as e:
            return None
        return grantees

    def getNewGrantees(self, olds):
        grantee = None
        grantees = self._grid.Model.getGrantees()
        grantees.refresh()
        elements = grantees.getElementNames()
        for element in elements:
            if element not in olds:
                grantee = element
                break
        return elements, grantee

    def getUsers(self):
        users = self._grid.Model.getGrantee().getUsers()
        members = users.getElementNames()
        if self._grid.Model.isRecursive():
            members = self._getFilteredMembers(users, self._grid.Model.getGrantees().getElementNames())
        availables = self._getFilteredMembers(self._members, members)
        return self._getUsersTitle(), members, availables

    def getGroups(self):
        members = self._grid.Model.getGrantee().getGroups().getElementNames()
        availables = self._getFilteredMembers(self._members, members)
        return self._getGroupsTitle(), members, availables

    def getRoles(self):
        members = self._grid.Model.getGrantee().getGroups().getElementNames()
        #TODO: We must avoid recursive assignments and therefore filter the current Grantee
        #TODO: as well as the groups having the current Grantee in assigned roles
        availables = self._getFilteredMembers(self._grid.Model.getGrantees(), members, True)
        return self._getRolesTitle(), members, availables

    def isMemberModified(self, grantees, isgroup):
        members = self._getMembers(isgroup)
        for grantee in grantees:
            if grantee not in members:
                return True
        for member in members:
            if member not in grantees:
                return True
        return False

    def setMembers(self, elements, isgroup):
        grantees = self._grid.Model.getGrantee().getGroups() if isgroup else self._grid.Model.getGrantee().getUsers()
        members = grantees.getElementNames()
        for element in elements:
            if element not in members:
                descriptor = grantees.createDataDescriptor()
                descriptor.setPropertyValue('Name', element)
                grantees.appendByDescriptor(descriptor)
        for member in members:
            if member not in elements:
                grantees.dropByName(member)
        # TODO: We need to refresh the members of members
        self._members.refresh()
        # TODO: If it is a group to update privilege inheritance, we need to refresh grid privileges
        if isgroup:
            self._grid.refresh()

    def dropGrantee(self):
        self._grid.Model.getGrantees().dropByName(self._grantee)
        return self._grid.Model.getGrantees().getElementNames()

    def setUserPassword(self, pwd):
        self._grid.Model.getGrantee().changePassword(pwd, pwd)

    def setGrantee(self, grantee):
        self._grantee = grantee
        self._grid.setGridVisible(False)
        recursive = self._grid.Model.setGrantee(grantee)
        self._grid.setGridVisible(True)
        enabled = grantee is not None
        return enabled, recursive, self._isRemovable(grantee)

    def hasGrantablePrivileges(self, table):
        privileges = 0
        if table is not None:
            privileges = self._user.getGrantablePrivileges(table, TABLE)
        return privileges != 0

    def getPrivileges(self):
        table = self._grid.getSelectedIdentifier('0')
        privileges, grantables = self._getPrivileges(table)
        inherited = self._grid.Model.getInheritedPrivileges(table)
        return table, privileges, grantables, inherited

    def setPrivileges(self, table, grant, revoke):
        grantee = self._grid.Model.getGrantee()
        if grant != 0:
            grantee.grantPrivileges(table, TABLE, grant)
        if revoke != 0:
            grantee.revokePrivileges(table, TABLE, revoke)
        self._grid.refresh(table)

    def _getMembers(self, isgroup):
        grantee = self._grid.Model.getGrantee()
        return grantee.getGroups().getElementNames() if isgroup else grantee.getUsers().getElementNames()

    def _getFilteredMembers(self, grantees, filters, recursive=False):
        members = grantees.getElementNames()
        return tuple(member for member in members if member not in filters and self._isValidMember(grantees, member, recursive))

    def _isValidMember(self, grantees, grantee, recursive):
        if recursive:
            return grantee != self._grantee and self._grantee not in grantees.getByName(grantee).getGroups().getElementNames()
        return True

    def _getPrivileges(self, table):
        privileges, ascendants = self._grid.Model.getGranteePrivileges(table)
        grantables = self._user.getGrantablePrivileges(table, TABLE)
        return privileges, grantables

    def _getTable(self, index):
        return self._grid.getRowIdentifier(index, '0')

    def _isRemovable(self, user):
        return self._grid.Model.isGroup() or self._user.getPropertyValue('Name') != user

# GroupModel StringResource methods
    def _getTableHeader(self):
        resource = self._resources.get('TableHeader')
        return self._resolver.resolveString(resource)

    def _getPrivilegeHeader(self, index):
        resource = self._resources.get('PrivilegeHeader') % index
        return self._resolver.resolveString(resource)

    def _getDropGroupTitle(self):
        resource = self._resources.get('DropGroupTitle')
        return self._resolver.resolveString(resource)

    def _getDropUserTitle(self):
        resource = self._resources.get('DropUserTitle')
        return self._resolver.resolveString(resource)

    def _getDropGroupMessage(self):
        resource = self._resources.get('DropGroupMessage')
        return self._resolver.resolveString(resource) % self._grantee

    def _getDropUserMessage(self):
        resource = self._resources.get('DropUserMessage')
        return self._resolver.resolveString(resource) % self._grantee

    def _getUsersTitle(self):
        resource = self._resources.get('UsersTitle')
        return self._resolver.resolveString(resource) % self._grantee

    def _getGroupsTitle(self):
        resource = self._resources.get('GroupsTitle')
        return self._resolver.resolveString(resource) % self._grantee

    def _getRolesTitle(self):
        resource = self._resources.get('RolesTitle')
        return self._resolver.resolveString(resource) % self._grantee

