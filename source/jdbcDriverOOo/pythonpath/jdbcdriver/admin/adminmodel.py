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

from com.sun.star.sdbcx.PrivilegeObject import TABLE

from com.sun.star.style.HorizontalAlignment import CENTER
from com.sun.star.style.HorizontalAlignment import LEFT

from jdbcdriver import createService
from jdbcdriver import getStringResource

from jdbcdriver import g_identifier
from jdbcdriver import g_extension

import traceback


class AdminModel(unohelper.Base):
    def __init__(self, ctx, user, members, grantees, tables, data, flags):
        self._user = user
        self._members = members
        self._grantees = grantees
        self._tables = tables
        self._resolver = getStringResource(ctx, g_identifier, g_extension)
        self._resources = {'TableHeader'      : 'GranteeDialog.Grid1.Column1',
                           'PrivilegeHeader'  : 'PrivilegesDialog.CheckBox%s.Label',
                           'DropGroupTitle'   : 'MessageBox.DropGroup.Title',
                           'DropGroupMessage' : 'MessageBox.DropGroup.Message',
                           'DropUserTitle'    : 'MessageBox.DropUser.Title',
                           'DropUserMessage'  : 'MessageBox.DropUser.Message',
                           'UsersTitle'       : 'UsersDialog.Title',
                           'GroupsTitle'      : 'GroupsDialog.Title',
                           'RolesTitle'       : 'RolesDialog.Title'}
        self._data = data
        self._column = createService(ctx, "com.sun.star.awt.grid.DefaultGridColumnModel")
        self._column.addColumn(self._getColumn(self._column.createColumn(), self._getTableHeader(), 120, 2, LEFT))
        for index in flags:
            title = self._getPrivilegeHeader(index)
            self._column.addColumn(self._getColumn(self._column.createColumn(), title))

    def _getColumn(self, column, title, width=70, flex=1, align=CENTER):
        column.ColumnWidth = width
        column.MinWidth = width
        column.HorizontalAlign = align
        column.Resizeable = True
        column.Flexibility = flex
        column.Title = title
        return column

    def getInitData(self):
        return self._data, self._column, self._grantees.getElementNames()

    def getDropGroupInfo(self, group):
        return self._getDropGroupMessage(group), self._getDropGroupTitle()

    def getDropUserInfo(self, user):
        return self._getDropUserMessage(user), self._getDropUserTitle()

    def isNameValid(self, name):
        return len(name) and name not in self._grantees.getElementNames()

    def isUserValid(self, user, pwd, confirmation):
        return self.isNameValid(user) and self.isPasswordConfirmed(pwd, confirmation)

    def isPasswordValid(self, pwd):
        return len(pwd)

    def isPasswordConfirmed(self, pwd, confirmation):
        return pwd == confirmation

    def createUser(self, user, password):
        descriptor = self._grantees.createDataDescriptor()
        descriptor.setPropertyValue('Password', password)
        return self._createGrantee(descriptor, user)

    def createGroup(self, group):
        descriptor = self._grantees.createDataDescriptor()
        return self._createGrantee(descriptor, group)

    def _createGrantee(self, descriptor, grantee):
        descriptor.setPropertyValue('Name', grantee)
        self._grantees.appendByDescriptor(descriptor)
        return self._grantees.getElementNames()

    def getUsers(self, name):
        grantee = self._grantees.getByName(name)
        members = grantee.getUsers().getElementNames()
        if self._data.isRecursive():
            members = self._getFilteredMembers(members, self._grantees.getElementNames())
        availables = self._getFilteredMembers(self._members.getElementNames(), members)
        return members, availables

    def getGroups(self, name):
        members = self._grantees.getByName(name).getGroups().getElementNames()
        availables = self._getFilteredMembers(self._members.getElementNames(), members)
        return members, availables

    def getRoles(self, name):
        members = self._grantees.getByName(name).getGroups().getElementNames()
        #TODO: We need to avoid recursive assignment and hence filter the name
        availables = self._getFilteredMembers(self._grantees.getElementNames(), members, name)
        return members, availables

    def getUsersTitle(self, user):
        return self._getUsersTitle(user)

    def getGroupsTitle(self, group):
        return self._getGroupsTitle(group)

    def getRolesTitle(self, group):
        return self._getRolesTitle(group)

    def isMemberModified(self, name, grantees, isgroup):
        members = self._getMembers(name, isgroup)
        for grantee in grantees:
            if grantee not in members:
                return True
        for member in members:
            if member not in grantees:
                return True
        return False

    def setMembers(self, name, elements, isgroup):
        grantee = self._grantees.getByName(name)
        grantees = grantee.getGroups() if isgroup else grantee.getUsers()
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
        # TODO: If it's a user we need to refresh the grid privileges
        if isgroup:
            self._data.refresh()

    def dropGrantee(self, grantee):
        self._grantees.dropByName(grantee)
        return self._grantees.getElementNames()

    def setUserPassword(self, name, pwd):
        self._grantees.getByName(name).changePassword(pwd, pwd)

    def setGrantee(self, grantee):
        return self._data.setGrantee(grantee)

    def getGrantablePrivileges(self, index):
        privileges = 0
        if index != -1:
            privileges = self._user.getGrantablePrivileges(self._getTable(index), TABLE)
        return privileges

    def getPrivileges(self, index):
        name = self._getTable(index)
        privileges, grantables = self._getPrivileges(index, name)
        inherited = self._data.getInheritedPrivileges(name) if self._data.needRecursion() else 0
        return name, privileges, grantables, inherited

    def setPrivileges(self, name, index, grant, revoke):
        table = self._getTable(index)
        grantee = self._grantees.getByName(name)
        if grant != 0:
            grantee.grantPrivileges(table, TABLE, grant)
        if revoke != 0:
            grantee.revokePrivileges(table, TABLE, revoke)
        self._data.refresh(index)

    def _getMembers(self, name, isgroup):
        grantee = self._grantees.getByName(name)
        return grantee.getGroups().getElementNames() if isgroup else grantee.getUsers().getElementNames()

    def _getFilteredMembers(self, members, filters, filter=None):
        return tuple(member for member in members if member not in filters and member != filter)

    def _getPrivileges(self, index, name):
        privileges = self._data.getGranteePrivileges(name)
        grantables = self.getGrantablePrivileges(index)
        return privileges, grantables

    def _getTable(self, index):
        return self._tables.getElementNames()[index]

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

    def _getDropGroupMessage(self, group):
        resource = self._resources.get('DropGroupMessage')
        return self._resolver.resolveString(resource) % group

    def _getDropUserMessage(self, user):
        resource = self._resources.get('DropUserMessage')
        return self._resolver.resolveString(resource) % user

    def _getUsersTitle(self, user):
        resource = self._resources.get('UsersTitle')
        return self._resolver.resolveString(resource) % user

    def _getGroupsTitle(self, group):
        resource = self._resources.get('GroupsTitle')
        return self._resolver.resolveString(resource) % group

    def _getRolesTitle(self, group):
        resource = self._resources.get('RolesTitle')
        return self._resolver.resolveString(resource) % group


