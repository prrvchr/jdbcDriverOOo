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

from .gridmodelbase import GridModelBase

from ..dbtool import getValueFromResult

import traceback


class GridModel(GridModelBase):
    def __init__(self, ctx):
        GridModelBase.__init__(self, ctx)
        self._resultset = None

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

# GridModel setter methods
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
            sort = self._sortable.getCurrentSortOrder()
            self._removeRow(self._row, row -1)
            if self._row > 0:
                self._changeData(0, self._row -1)
            self._sortable.removeColumnSort()
            if sort.First != -1:
                self._sortable.sortByColumn(sort.First, sort.Second)
        elif self._row > row:
            sort = self._sortable.getCurrentSortOrder()
            self._insertRow(row, self._row -1)
            if row > 0:
                self._changeData(0, row -1)
            self._sortable.removeColumnSort()
            if sort.First != -1:
                self._sortable.sortByColumn(sort.First, sort.Second)
        elif self._row > 0:
            sort = self._sortable.getCurrentSortOrder()
            self._changeData(0, row -1)
            self._sortable.removeColumnSort()
            if sort.First != -1:
                self._sortable.sortByColumn(sort.First, sort.Second)

