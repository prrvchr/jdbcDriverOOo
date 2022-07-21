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
    def __init__(self, ctx, roles, tables, privileges):
        self._ctx = ctx
        self._events = []
        self._listeners = []
        self._role = None
        self._roles = roles
        self._tables = tables
        self._privileges = privileges
        self._rows = {}
        self.RowCount = len(tables)
        self.ColumnCount = len(privileges) + 1

    def setRole(self, role):
        self._role = role
        self._rows = {}
        self._dataChanged()

    def refresh(self, row):
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
        print("GridData.createClone()")
        return GridData(self._roles, self._tables, self._privileges)

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
        return self._getPrivilegeData(table, row, column)

    def getCellToolTip(self, column, row):
        return ""

    def getRowHeading(self, row):
        return ""

    def getRowData(self, row):
        table = self._tables[row]
        values = [table]
        for index in range(0, self.ColumnCount):
            values.append(self._getPrivilegeData(table, row, index +1))
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

    def _getPrivilegeData(self, table, row, column):
        privileges = 0
        privilege = self._privileges[column]
        if self._role is not None:
            privileges = self._getTablePrivilege(table, row)
        return privilege == privileges & privilege

    def _getTablePrivilege(self, table, row):
        if row not in self._rows:
            self._rows[row] = self._roles.getByName(self._role).getPrivileges(table, TABLE)
        return self._rows[row]

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

