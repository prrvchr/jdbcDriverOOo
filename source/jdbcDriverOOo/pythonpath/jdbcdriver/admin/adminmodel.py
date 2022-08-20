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
    def __init__(self, ctx, user, users, grantees, tables, data, flags):
        self._user = user
        self._users = users
        self._grantees = grantees
        self._tables = tables
        self._resolver = getStringResource(ctx, g_identifier, g_extension)
        self._resources = {'TableHeader'      : 'GroupsDialog.Label2.Label',
                           'PrivilegeHeader'  : 'PrivilegesDialog.CheckBox%s.Label',
                           'PrivilegeTitle'   : 'PrivilegesDialog.Title',
                           'DropGroupTitle'   : 'MessageBox.DropGroup.Title',
                           'DropGroupMessage' : 'MessageBox.DropGroup.Message',
                           'DropUserTitle'    : 'MessageBox.DropUser.Title',
                           'DropUserMessage'  : 'MessageBox.DropUser.Message',
                           'AddUserTitle'     : 'AddUserDialog.Title'}
        self._data = data
        self._column = createService(ctx, "com.sun.star.awt.grid.DefaultGridColumnModel")
        self._column.addColumn(self._getColumn(self._column.createColumn(), self._getTableHeader(), 120, True, LEFT))
        for index in flags:
            title = self._getPrivilegeHeader(index)
            self._column.addColumn(self._getColumn(self._column.createColumn(), title))

    def _getColumn(self, column, title, width=70, resize=False, align=CENTER):
        column.ColumnWidth = width
        column.MinWidth = width
        column.HorizontalAlign = align
        column.Resizeable = resize
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
        print("AdminModel._createGrantee() %s" % grantee)
        return self._grantees.getElementNames()

    def getMembers(self, name):
        group = self._grantees.getByName(name)
        return group.getUsers().getElementNames()

    def getTitle(self, group):
        return self._getAddUserTitle(group)

    def isMemberModified(self, name, users):
        members = self.getMembers(name)
        for user in users:
            if user not in members:
                return True
        for member in members:
            if member not in users:
                return True
        return False

    def getUsers(self, members):
        users = self._users.getElementNames()
        return tuple(user for user in users if user not in members)

    def setUsers(self, group, elements):
        users = self._grantees.getByName(group).getUsers()
        members = users.getElementNames()
        for element in elements:
            if element not in members:
                descriptor = users.createDataDescriptor()
                descriptor.setPropertyValue('Name', element)
                users.appendByDescriptor(descriptor)
        for member in members:
            if member not in elements:
                users.dropByName(member)

    def dropGrantee(self, grantee):
        self._grantees.dropByName(grantee)
        print("AdminModel.dropGrantee() %s" % grantee)
        return self._grantees.getElementNames()

    def setUserPassword(self, name, pwd):
        self._grantees.getByName(name).changePassword(pwd, pwd)
        print("AdminModel.setUserPassword() %s" % pwd)

    def setGrantee(self, grantee):
        self._data.setRole(grantee)

    def getGrantablePrivileges(self, index):
        privileges = 0
        if index != -1:
            privileges = self._user.getGrantablePrivileges(self._getTable(index), TABLE)
        return privileges

    def getTableInfo(self, index):
        name = self._getTable(index)
        table = self._tables.getByName(name)
        catalog = table.getPropertyValue("CatalogName")
        schema = table.getPropertyValue("SchemaName")
        title = self._getPrivilegeTitle(table.getPropertyValue("Name"))
        privileges = self._data._getTablePrivilege(name, index)
        grantables = self.getGrantablePrivileges(index)
        return catalog, schema, title, privileges, grantables

    def setPrivileges(self, name, index, grant, revoke):
        table = self._getTable(index)
        grantee = self._grantees.getByName(name)
        if grant != 0:
            grantee.grantPrivileges(table, TABLE, grant)
        if revoke != 0:
            grantee.revokePrivileges(table, TABLE, revoke)
        self._data.refresh(index)

    def _getTable(self, index):
        return self._tables.getElementNames()[index]

# GroupModel StringResource methods
    def _getTableHeader(self):
        resource = self._resources.get('TableHeader')
        return self._resolver.resolveString(resource)

    def _getPrivilegeHeader(self, index):
        resource = self._resources.get('PrivilegeHeader') % index
        return self._resolver.resolveString(resource)

    def _getPrivilegeTitle(self, table):
        resource = self._resources.get('PrivilegeTitle')
        return self._resolver.resolveString(resource) % table

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

    def _getAddUserTitle(self, group):
        resource = self._resources.get('AddUserTitle')
        return self._resolver.resolveString(resource) % group

