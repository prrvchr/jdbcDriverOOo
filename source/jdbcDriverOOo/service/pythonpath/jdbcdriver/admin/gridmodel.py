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

from com.sun.star.sdbcx.PrivilegeObject import TABLE

from .grid import GridModel as GridModelBase

from ..unotool import createService
from ..unotool import getPropertyValueSet

import traceback


class GridModel(GridModelBase):
    def __init__(self, ctx, user, groups, grantees, tables, flags, isuser, url):
        GridModelBase.__init__(self)
        self._ctx = ctx
        self._user = user
        self._grantee = None
        self._groups = groups
        self._grantees = grantees
        self._tables = tables
        self._indexes = tables.getElementNames()
        self._flags = flags
        self._isuser = isuser
        self._rows = {}
        self._row = tables.getCount()
        self._column = len(flags) + 1
        self._url = url
        self._images = self._getImages()

    @property
    def RowCount(self):
        return self._row
    @property
    def ColumnCount(self):
        return self._column

# com.sun.star.util.XCloneable
    def createClone(self):
        return GridModel(self._ctx, self._user, self._grantees, self._tables, self._flags, self._isuser, self._url)

# com.sun.star.awt.grid.XGridDataModel
    def getCellData(self, column, row):
        table = self._indexes[row]
        if column == 0:
            return table
        return self._getPrivilegeImage(table, column)

    def getCellToolTip(self, column, row):
        return ""

    def getRowData(self, row):
        table = self._indexes[row]
        values = [table]
        for index in range(1, self.ColumnCount + 1):
            values.append(self._getPrivilegeImage(table, index))
        return tuple(values)

# GridModel getter methods
    def getGrantee(self):
        return self._grantee

    def getGrantees(self):
        return self._grantees

    def getGranteePrivileges(self, table):
        return self._getTablePrivileges(table)

    def isGroup(self):
        return not self._isuser

# GridModel setter methods
    def refresh(self, identifier=None):
        if identifier is None:
            self._rows = {}
        else:
            del self._rows[identifier]
        self.dataChanged(-1, -1)

    def setGrantee(self, grantee):
        self._grantee = None if grantee is None else self._grantees.getByName(grantee)
        self.refresh()

# GridModel private getter methods
    def _getImages(self):
        images = []
        provider = createService(self._ctx, 'com.sun.star.graphic.GraphicProvider')
        for index in range(6):
            images.append(self._getImage(provider, index))
        return tuple(images)

    def _getImage(self, provider, index):
        url = '%s/privilege-%s.png' % (self._url, index)
        properties = getPropertyValueSet({'URL': url})
        image = provider.queryGraphic(properties)
        return image

    def _getPrivilegeImage(self, table, column):
        privilege, grantable, inherited = self._getDataPrivileges(table, column)
        if privilege:
            index = 2
        elif inherited:
            index = 1
        else:
            index = 0
        if grantable:
            index += 3
        return self._images[index]

    def _getDataPrivileges(self, table, column):
        flag = self._flags[column -1]
        privilege = grantable = inherited = 0
        if self._grantee is not None:
            privilege, grantable, inherited = self._getTablePrivileges(table)
        return flag == privilege & flag, flag == grantable & flag, flag == inherited & flag

    def _getTablePrivileges(self, table):
        if table not in self._rows:
            self._rows[table] = self._getGranteePrivileges(table)
        return self._rows[table]

    def _getGranteePrivileges(self, table):
        privilege = grantable = inherited = 0
        if self._grantee is not None:
            privilege = self._grantee.getPrivileges(table, TABLE)
            if self._user is not None:
                grantable = self._user.getGrantablePrivileges(table, TABLE)
                groups = self._user.getGroups()
                if groups is not None:
                    grantable |= self._getGrantablePrivileges(groups, table)
            else:
                grantable = sum(self._flags)
            inherited = self._getInheritedPrivileges(table)
        return privilege, grantable, inherited

    def _getGrantablePrivileges(self, groups, table):
        privileges = 0
        if self._groups is not None:
            parents = []
            for group in groups.getElementNames():
                privileges |= self._getGrantableRoles(group, table, parents)
        return privileges

    def _getInheritedPrivileges(self, table):
        privileges = 0
        if self._groups is not None:
            if self._isuser:
                privileges = self._getUserInheritedRoles(table)
            else:
                parents = []
                privileges = self._getInheritedRoles(self._grantee.Name, table, parents)
        return privileges

    def _getGrantableRoles(self, role, table, parents):
        privileges = 0
        for group in self._getParentRoles(role, parents):
            privileges |= group.getGrantablePrivileges(table, TABLE)
        return privileges

    def _getUserInheritedRoles(self, table):
        parents = []
        privileges = 0
        groups = self._grantee.getGroups()
        if groups is not None:
            for role in groups.getElementNames():
                privileges |= self._getInheritedRoles(role, table, parents)
        return privileges

    def _getInheritedRoles(self, role, table, parents):
        privileges = 0
        for group in self._getParentRoles(role, parents):
            privileges |= group.getPrivileges(table, TABLE)
        return privileges

    def _getParentRoles(self, role, parents):
        for name in self._groups.getElementNames():
            if name in parents:
                continue
            group = self._groups.getByName(name)
            groups = group.getGroups()
            if groups is not None and groups.hasByName(role):
                parents.append(name)
                yield from self._getParentRoles(name, parents)
        if self._groups.hasByName(role):
            yield self._groups.getByName(role)

