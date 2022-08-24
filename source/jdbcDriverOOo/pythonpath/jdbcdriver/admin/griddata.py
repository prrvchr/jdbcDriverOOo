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

from com.sun.star.uno import XWeak
from com.sun.star.uno import XAdapter

from com.sun.star.awt.grid import XMutableGridDataModel
from com.sun.star.awt.grid import XSortableGridData

from com.sun.star.sdbcx.PrivilegeObject import TABLE

from jdbcdriver import createService

import traceback


class GridData(unohelper.Base,
               XWeak,
               XAdapter,
               XMutableGridDataModel):
    def __init__(self, ctx, grantees, tables, flags, recursive, isuser):
        self._ctx = ctx
        self._events = []
        self._listeners = []
        self._grantee = None
        self._grantees = grantees
        self._tables = tables
        self._flags = flags
        self._recursive = recursive
        self._isuser = isuser
        self._rows = {}
        self.RowCount = len(tables)
        self.ColumnCount = len(flags) + 1

    def setGrantee(self, grantee):
        self._grantee = self._grantees.getByName(grantee)
        self.refresh()
        return self._recursive

    def refresh(self, row=None):
        if row is None:
            self._rows = {}
        else:
            del self._rows[row]
        self._dataChanged(row)

    # FIXME: We can't use XMutableGridDataModel without this interface XWeak
    # com.sun.star.uno.XWeakXWeak
    def queryAdapter(self):
        return self

    # FIXME: We can't use XMutableGridDataModel without this interface XAdapter
    # com.sun.star.uno.XAdapter
    def queryAdapted(self):
        return self
    def addReference(self, reference):
        pass
    def removeReference(self, reference):
        pass

    # com.sun.star.util.XCloneable
    def createClone(self):
        return GridData(self._ctx, self._grantees, self._tables, self._flags, self._recursive, self._isuser)

    # com.sun.star.lang.XComponent
    def dispose(self):
        event = uno.createUnoStruct('com.sun.star.lang.EventObject')
        event.Source = self
        for listener in self._events:
            listener.disposing(event)
    def addEventListener(self, listener):
        self._events.append(listener)
    def removeEventListener(self, listener):
        if listener in self._events:
            self._events.remove(listener)

    # com.sun.star.awt.grid.XGridDataModel
    def getCellData(self, column, row):
        table = self._tables[row]
        if column == 0:
            return table
        return self._getDataPrivileges(table, row, column)

    def getCellToolTip(self, column, row):
        return ""

    def getRowHeading(self, row):
        return ""

    def getRowData(self, row):
        table = self._tables[row]
        values = [table]
        for index in range(0, self.ColumnCount):
            values.append(self._getDataPrivileges(table, row, index +1))
        return tuple(values)

    # FIXME: We need this interface to be able to broadcast the data change to all listener
    # com.sun.star.awt.grid.XMutableGridDataModel
    def addRow(self, heading, data):
        pass
    def addRows(self, headings, data):
        pass
    def insertRow(self, index, heading, data):
        pass
    def insertRows(self, index, headings, data):
        pass
    def removeRow(self, index):
        pass
    def removeAllRows(self):
        pass
    def updateCellData(self, column, row, value):
        pass
    def updateRowData(self, indexes, rows, values):
        pass
    def updateRowHeading(self, index, heading):
        pass
    def updateCellToolTip(self, column, row, value):
        pass
    def updateRowToolTip(self, row, value):
        pass
    def addGridDataListener(self, listener):
        self._listeners.append(listener)
    def removeGridDataListener(self, listener):
        if listener in self._listeners:
            self._listeners.remove(listener)

    def _getDataPrivileges(self, table, row, column):
        privileges = 0
        flag = self._flags[column]
        if self._grantee is not None:
            privileges = self._getRowPrivileges(table, row)
        return flag == privileges & flag

    def _getRowPrivileges(self, table, row):
        if row not in self._rows:
            self._rows[row] = self.getGranteePrivileges(table, self._isuser or self._recursive)
        return self._rows[row]

    def isRecursive(self):
        return self._recursive

    def needRecursion(self):
        return self._isuser or self._recursive

    def getGranteePrivileges(self, table, recursive=False):
        privileges = self._grantee.getPrivileges(table, TABLE)
        if recursive:
            print("GridData.getGranteePrivileges() 1 %s" % privileges)
            privileges |= self.getInheritedPrivileges(table)
        print("GridData.getGranteePrivileges() 2 %s" % privileges)
        return privileges

    def getInheritedPrivileges(self, table):
        return self._getInheritedPrivileges(table, self._grantee, 0)

    def _getInheritedPrivileges(self, table, role, privileges):
        groups = role.getGroups().createEnumeration()
        while groups.hasMoreElements():
            group = groups.nextElement()
            name = group.getPropertyValue('Name')
            privileges |= group.getPrivileges(table, TABLE)
            print("GridData._getInheritedPrivileges() %s - %s" % (name, privileges))
            if self._recursive:
                privileges |= self._getInheritedPrivileges(table, group, privileges)
        return privileges

    # FIXME: Broadcast the data change to all listener
    def _dataChanged(self, row=None):
        event = self._getGridDataEvent(row)
        for listener in self._listeners:
            listener.dataChanged(event)

    def _getGridDataEvent(self, row):
        event = uno.createUnoStruct('com.sun.star.awt.grid.GridDataEvent')
        event.Source = self
        event.FirstColumn = 1
        event.LastColumn = self.ColumnCount -1
        if row is None:
            event.FirstRow = 0
            event.LastRow = self.RowCount -1
        else:
            event.FirstRow = row
            event.LastRow = row
        return event

