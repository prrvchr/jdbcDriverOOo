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

from com.sun.star.uno import XWeak
from com.sun.star.uno import XAdapter

from com.sun.star.awt.grid import XMutableGridDataModel

from ..unotool import createService
from ..unotool import hasInterface

from ..dbtool import getValueFromResult

from .gridhandler import RowSetListener

import traceback


class GridModel(unohelper.Base,
                XWeak,
                XAdapter,
                XMutableGridDataModel):
    def __init__(self, ctx):
        self._ctx = ctx
        self._sort = None
        #mri = createService(ctx, 'mytools.Mri')
        #mri.inspect(sort)
        self._events = []
        self._listeners = []
        self._resultset = None
        self._row = 0
        self._column = 0

    @property
    def RowCount(self):
        return self._row
    @property
    def ColumnCount(self):
        return self._column

# XWeak
    def queryAdapter(self):
        return self

# XAdapter
    def queryAdapted(self):
        return self
    def addReference(self, reference):
        pass
    def removeReference(self, reference):
        pass

# XCloneable
    def createClone(self):
        return self

# XGridDataModel
    def getCellData(self, column, row):
        self._resultset.absolute(row +1)
        return  getValueFromResult(self._resultset, column +1)
    def getCellToolTip(self, column, row):
        return self.getCellData(column, row)
    def getRowHeading(self, row):
        return row
    def getRowData(self, row):
        data = []
        self._resultset.absolute(row +1)
        for index in range(self._column):
            data.append(getValueFromResult(self._resultset, index +1))
        return tuple(data)

# XMutableGridDataModel
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
        # FIXME: The service 'com.sun.star.awt.grid.SortableGridDataModel' packaging
        # FIXME: this interface seems to want to register as an XGridDataListener when
        # FIXME: initialized, so it is necessary to filter the listener's interfaces
        if hasInterface(listener, 'com.sun.star.awt.grid.XGridDataListener'):
            self._listeners.append(listener)
        #elif hasInterface(listener, 'com.sun.star.awt.grid.XSortableMutableGridDataModel'):
        #    self._sort = listener

    def removeGridDataListener(self, listener):
        if listener in self._listeners:
            self._listeners.remove(listener)

# XComponent
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

# GridModel getter methods
    def getCurrentSortOrder(self):
        return self._sort.getCurrentSortOrder()

# GridModel setter methods
    def setSortableModel(self, sort):
        self._sort = sort

    def sortByColumn(self, index, ascending):
        print("GridModel.sortByColumn() %s - %s" % (index, ascending))
        if index != -1:
            self._sort.sortByColumn(index, ascending)
        else:
            self._sort.removeColumnSort()

    def resetRowSetData(self):
        row = self._row
        self._row = 0
        if self._row < row:
            self._removeRow(self._row, row -1)

    def setRowSetData(self, rowset):
        self._resultset = rowset.createResultSet()
        row = self._row
        self._row = rowset.RowCount
        self._column = rowset.getMetaData().getColumnCount()
        if self._row < row:
            sort = self._sort.getCurrentSortOrder()
            self._removeRow(self._row, row -1)
            if self._row > 0:
                self._changeData(0, self._row -1)
            self._sort.removeColumnSort()
            if sort.First != -1:
                self._sort.sortByColumn(sort.First, sort.Second)
        elif self._row > row:
            sort = self._sort.getCurrentSortOrder()
            self._insertRow(row, self._row -1)
            if row > 0:
                self._changeData(0, row -1)
            self._sort.removeColumnSort()
            if sort.First != -1:
                self._sort.sortByColumn(sort.First, sort.Second)
        elif self._row > 0:
            sort = self._sort.getCurrentSortOrder()
            self._changeData(0, row -1)
            self._sort.removeColumnSort()
            if sort.First != -1:
                self._sort.sortByColumn(sort.First, sort.Second)

# GridModel private methods
    def _removeRow(self, first, last):
        event = self._getGridDataEvent(first, last)
        for listener in self._listeners:
            listener.rowsRemoved(event)

    def _insertRow(self, first, last):
        event = self._getGridDataEvent(first, last)
        for listener in self._listeners:
            listener.rowsInserted(event)

    def _changeData(self, first, last):
        event = self._getGridDataEvent(first, last)
        for listener in self._listeners:
            listener.dataChanged(event)

    def _getGridDataEvent(self, first, last):
        event = uno.createUnoStruct('com.sun.star.awt.grid.GridDataEvent')
        event.Source = self
        event.FirstColumn = 0
        event.LastColumn = self._column -1
        event.FirstRow = first
        event.LastRow = last
        return event
