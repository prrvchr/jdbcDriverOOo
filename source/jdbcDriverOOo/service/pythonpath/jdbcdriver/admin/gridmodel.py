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

from com.sun.star.sdbcx.PrivilegeObject import TABLE

from ..grid import GridModel as GridModelBase

from ..unotool import createService
from ..unotool import getPropertyValueSet

import traceback


class GridModel(GridModelBase):
    def __init__(self, ctx, user, grantees, tables, flags, isuser, url):
        GridModelBase.__init__(self, ctx)
        self._user = user
        self._grantee = None
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
    def setGrantee(self, grantee):
        self._grantee = None if grantee is None else self._grantees.getByName(grantee)
        self.refresh()

    def getGrantee(self):
        return self._grantee

    def getGrantees(self):
        return self._grantees

    def isGroup(self):
        return not self._isuser

    def getGranteePrivileges(self, table):
        return self._getTablePrivileges(table)

# GridModel setter methods
    def refresh(self, identifier=None):
        if identifier is None:
            self._rows = {}
        else:
            del self._rows[identifier]
        #first = 0 if row is None else row
        #last = self._row -1 if row is None else row
        #self._changeData(first, last)

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
                grantable |= self._getInheritedGrantablePrivileges(table, self._user)
            else:
                grantable = sum(self._flags)
            inherited = self._getInheritedPrivileges(table, self._grantee)
        return privilege, grantable, inherited

    def _getInheritedPrivileges(self, table, role, privilege=0, level=0, maxi=2):
        groups = role.getGroups().createEnumeration()
        while groups.hasMoreElements():
            group = groups.nextElement()
            privilege |= group.getPrivileges(table, TABLE)
            if level <= maxi:
                level += 1
                privilege |= self._getInheritedPrivileges(table, group, privilege, level, maxi)
        return privilege

    def _getInheritedGrantablePrivileges(self, table, role, privilege=0, level=0, maxi=2):
        groups = role.getGroups().createEnumeration()
        while groups.hasMoreElements():
            group = groups.nextElement()
            privilege |= group.getGrantablePrivileges(table, TABLE)
            if level <= maxi:
                level += 1
                privilege |= self._getInheritedGrantablePrivileges(table, group, privilege, level, maxi)
        return privilege
