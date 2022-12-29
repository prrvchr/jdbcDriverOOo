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

from com.sun.star.sdbcx.PrivilegeObject import TABLE

from .gridmodelbase import GridModelBase

from ..unotool import createService
from ..unotool import getPropertyValueSet

import traceback


class GridModel(GridModelBase):
    def __init__(self, ctx, grantees, tables, flags, recursive, isuser, url):
        GridModelBase.__init__(self, ctx)
        self._grantee = None
        self._grantees = grantees
        self._tables = tables
        self._flags = flags
        self._recursive = recursive
        self._isuser = isuser
        self._rows = {}
        self._row = len(tables)
        self._column = len(flags) + 1
        images = []
        provider = createService(ctx, 'com.sun.star.graphic.GraphicProvider')
        print("GridModel__init__() URL: %s" % url)
        for i in range(3):
            images.append(self._getImage(provider, url, i))
        self._images = tuple(images)

# com.sun.star.util.XCloneable
    def createClone(self):
        return GridModel(self._ctx, self._grantees, self._tables, self._flags, self._recursive, self._isuser)

# com.sun.star.awt.grid.XGridDataModel
    def getCellData(self, column, row):
        table = self._tables[row]
        if column == 0:
            return table
        return self._getPrivilegeImage(table, row, column)

    def getCellToolTip(self, column, row):
        return ""

    def getRowHeading(self, row):
        return ""

    def getRowData(self, row):
        table = self._tables[row]
        values = [table]
        for index in range(0, self.ColumnCount):
            values.append(self._getPrivilegeImage(table, row, index +1))
        return tuple(values)

# GridModel getter methods
    def setGrantee(self, grantee):
        self._grantee = None if grantee is None else self._grantees.getByName(grantee)
        self.refresh()
        return self._recursive

    def getGrantee(self):
        return self._grantee

    def getGrantees(self):
        return self._grantees

    def isRecursive(self):
        return self._recursive

    def isGroup(self):
        return not self._isuser

    def getGranteePrivileges(self, table, recursive=False):
        ascendants = 0
        privileges = self.getGrantee().getPrivileges(table, TABLE)
        if recursive:
            ascendants = self._getInheritedPrivileges(table, self.getGrantee(), 0)
        return privileges, ascendants

    def getInheritedPrivileges(self, table):
        return self._getInheritedPrivileges(table, self.getGrantee(), 0) if self._needInherited() else 0

# GridModel setter methods
    def refresh(self, row=None):
        if row is None:
            self._rows = {}
        else:
            del self._rows[row]
        #first = 0 if row is None else row
        #last = self._row -1 if row is None else row
        #self._changeData(first, last)

# GridModel private getter methods
    def _getImage(self, provider, path, index):
        url = '%s/privilege-%s.png' % (path, index)
        properties = getPropertyValueSet({'URL': url})
        image = provider.queryGraphic(properties)
        return image

    def _getPrivilegeImage(self, table, row, column):
        privilege, ascendant = self._getDataPrivileges(table, row, column)
        if privilege:
            index = 2
        elif ascendant:
            index = 1
        else:
            index = 0
        return self._images[index]

    def _getDataPrivileges(self, table, row, column):
        privileges = ascendants = 0
        flag = self._flags[column]
        if self._grantee is not None:
            privileges, ascendants = self._getRowPrivileges(table, row)
        return flag == privileges & flag, flag == ascendants & flag

    def _getRowPrivileges(self, table, row):
        if row not in self._rows:
            self._rows[row] = self.getGranteePrivileges(table, self._needInherited())
        return self._rows[row]

    def _needInherited(self):
        return self._isuser or self._recursive

    def _getInheritedPrivileges(self, table, role, privileges):
        groups = role.getGroups().createEnumeration()
        while groups.hasMoreElements():
            group = groups.nextElement()
            privileges |= group.getPrivileges(table, TABLE)
            if self._recursive:
                privileges |= self._getInheritedPrivileges(table, group, privileges)
        return privileges

