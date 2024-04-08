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

from com.sun.star.sdbc import SQLException

from com.sun.star.sdbcx.PrivilegeObject import TABLE

from com.sun.star.style.HorizontalAlignment import CENTER
from com.sun.star.style.HorizontalAlignment import LEFT

from com.sun.star.container import ElementExistException

from ..unotool import hasInterface

import traceback


class AdminModel(unohelper.Base):
    def __init__(self, manager, users, members, tables, username, resolver):
        self._grid = manager
        self._users = users
        self._members = members
        self._tables = tables
        self._username = username
        self._grantee = ''
        self._resolver = resolver
        self._resources = {'DropGroupTitle'        : 'MessageBox.DropGroup.Title',
                           'DropGroupMessage'      : 'MessageBox.DropGroup.Message',
                           'DropUserTitle'         : 'MessageBox.DropUser.Title',
                           'DropUserMessage'       : 'MessageBox.DropUser.Message',
                           'UsersTitle'            : 'UsersDialog.Title',
                           'GroupsTitle'           : 'GroupsDialog.Title',
                           'RolesTitle'            : 'RolesDialog.Title',
                           'PrivilegeErrorTitle'   : 'MessageBox.Privilege.Error.Title',
                           'CreateUserErrorTitle'  : 'MessageBox.CreateUser.Error.Title',
                           'CreateGroupErrorTitle' : 'MessageBox.CreateGroup.Error.Title',
                           'SetMembersErrorTitle'  : 'MessageBox.SetMembers.Error.Title'}

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
        return len(name) and name not in self.getGrantees()

    def isUserValid(self, user, pwd, confirmation):
        return self.isNameValid(user) and self.isPasswordConfirmed(pwd, confirmation)

    def isPasswordValid(self, pwd):
        return len(pwd) > 0

    def isPasswordConfirmed(self, pwd, confirmation):
        return pwd == confirmation

    def supportsCreateRole(self):
        return self._grid.Model.getGrantees().createDataDescriptor() is not None

    def createUser(self, user, password):
        roles = self._grid.Model.getGrantees()
        descriptor = roles.createDataDescriptor()
        descriptor.setPropertyValue('Password', password)
        return self._createRole(roles, descriptor, user)

    def createGroup(self, group):
        roles = self._grid.Model.getGrantees()
        descriptor = roles.createDataDescriptor()
        return self._createRole(roles, descriptor, group)

    def _createRole(self, roles, descriptor, role):
        descriptor.setPropertyValue('Name', role)
        try:
            roles.appendByDescriptor(descriptor)
        except (SQLException, ElementExistException) as e:
            print("AdminModel._createRole() ERROR ******************")
            return None
        return role

    # XXX: Show group's user members.
    def getUsers(self):
        print("AdminModel.getUsers() ******************")
        users = self._grid.Model.getGrantee().getUsers()
        members = users.getElementNames()
        availables = self._getFilteredMembers(self._users, members)
        print("AdminModel.getUsers() Members: %s-  Availables: %s" % (members, availables))
        return users, self._getUsersTitle(), availables

    # XXX: Show user's group members.
    def getGroups(self):
        print("AdminModel.getGroups() ******************")
        groups = self._grid.Model.getGrantee().getGroups()
        members = groups.getElementNames()
        availables = self._getFilteredMembers(self._members, members)
        return groups, self._getGroupsTitle(), availables

    # XXX: Show group's role members.
    def getRoles(self):
        print("AdminModel.getRoles() ******************")
        groups = self._grid.Model.getGrantee().getGroups()
        members = groups.getElementNames()
        #TODO: We must avoid recursive assignments and therefore filter the current Grantee
        #TODO: as well as the groups having the current Grantee in assigned roles
        availables = self._getFilteredMembers(self._grid.Model.getGrantees(), members, True)
        return groups, self._getRolesTitle(), availables

    def isMemberModified(self, grantees, isgroup):
        members = self._getMembers(isgroup)
        for grantee in grantees:
            if grantee not in members:
                return True
        for member in members:
            if member not in grantees:
                return True
        return False

    def setMembers(self, grantees, elements, isgroup):
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
        #self._members.refresh()
        # TODO: If it is a group to update privilege inheritance, we need to refresh grid privileges
        if isgroup:
            self._grid.refresh()

    def dropGrantee(self):
        self._grid.Model.getGrantees().dropByName(self._grantee)
        return self.getGrantees()

    def setUserPassword(self, pwd):
        self._grid.Model.getGrantee().changePassword(pwd, pwd)

    def setGrantee(self, grantee):
        self._grantee = grantee
        self._grid.setGridVisible(False)
        self._grid.Model.setGrantee(grantee)
        self._grid.setGridVisible(True)
        isgroup = self._grid.Model.isGroup()
        return self._supportRole(isgroup), self._isRemovable(grantee, isgroup)

    def hasGridSelectedRows(self):
        return self._grid.hasSelectedRows()

    def hasGrantablePrivileges(self):
        table = self._grid.getSelectedIdentifier('0')
        privilege, grantable, inherited = self._grid.Model.getGranteePrivileges(table)
        return grantable != 0

    def getPrivileges(self):
        table = self._grid.getSelectedIdentifier('0')
        privilege, grantable, inherited = self._grid.Model.getGranteePrivileges(table)
        return table, privilege, grantable, inherited

    def setPrivileges(self, table, grant, revoke):
        grantee = self._grid.Model.getGrantee()
        if grant != 0:
            grantee.grantPrivileges(table, TABLE, grant)
        if revoke != 0:
            grantee.revokePrivileges(table, TABLE, revoke)
        self._grid.refresh(table)

    def getPrivilegeErrorTitle(self):
        resource = self._resources.get('PrivilegeErrorTitle')
        return self._resolver.resolveString(resource)

    def getCreateUserErrorTitle(self):
        resource = self._resources.get('CreateUserErrorTitle')
        return self._resolver.resolveString(resource)

    def getCreateGroupErrorTitle(self):
        resource = self._resources.get('CreateGroupErrorTitle')
        return self._resolver.resolveString(resource)

    def getSetMembersErrorTitle(self):
        resource = self._resources.get('SetMembersErrorTitle')
        return self._resolver.resolveString(resource)

    def _supportRole(self, isgroup):
        if isgroup:
            grantee = self._grid.Model.getGrantee()
            inferface = 'com.sun.star.sdbcx.XGroupsSupplier'
            support = hasInterface(grantee, inferface) and grantee.getGroups() is not None
        else:
            support = self._members is not None
        return support

    def _getMembers(self, isgroup):
        grantee = self._grid.Model.getGrantee()
        return grantee.getGroups().getElementNames() if isgroup else grantee.getUsers().getElementNames()

    def _getFilteredMembers(self, grantees, filters, recursive=False):
        if grantees is None:
            return ()
        members = grantees.getElementNames()
        return tuple(member for member in members if member not in filters and self._isValidMember(grantees, member, recursive))

    def _isValidMember(self, grantees, grantee, recursive):
        if recursive:
            return grantee != self._grantee and self._grantee not in grantees.getByName(grantee).getGroups().getElementNames()
        return True

    def _isRemovable(self, user, isgroup):
        return user is not None and self._username != user or isgroup

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


