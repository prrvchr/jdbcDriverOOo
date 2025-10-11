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

from com.sun.star.sdbc.DataType import VARCHAR

from com.sun.star.style.HorizontalAlignment import CENTER

from .grid import GridManager as GridManagerBase

from ..unotool import getNamedValue

from collections import OrderedDict
import traceback


class GridManager(GridManagerBase):
    def __init__(self, ctx, datasource, listener, columns, url, model, window, quote, setting, selection, resources, maxi=None, multi=False, factor=4):
        GridManagerBase.__init__(self, ctx, url, model, window, quote, setting, selection, resources, maxi, multi, factor)
        self._datasource = datasource
        self._indexes = {'0': 0}
        self._types = {'0': VARCHAR}
        self._view.showGridColumnHeader(False)
        self._properties, self._headers = self._getGridInfos(columns)
        identifiers = self._initColumnModel(datasource)
        self._view.initColumns(self._url, self._headers, identifiers)
        index, ascending = self._getSavedOrders(datasource)
        if index != -1:
            self.Model.sortByColumn(index, ascending)
        self._view.showGridColumnHeader(True)
        self._view.addSelectionListener(listener)

# GridManager getter methods
    def getGrantee(self):
        return self._model.getGrantee()

    def getGrantees(self):
        return self._model.getGrantees()

    def getGranteePrivileges(self, table):
        return self._model.getGranteePrivileges(table)

    def isGroup(self):
        return self._model.isGroup()

# GridManager setter methods
    def refresh(self, identifier=None):
        # FIXME: Since using the 'com.sun.star.awt.grid.SortableGridDataModel' service,
        # FIXME: the only way to refresh the grid display is to toggle its visibility
        notified = self._model.hasGridDataListener()
        if not notified:
            self.setGridVisible(False)
        self._model.refresh(identifier)
        if not notified:
            self.setGridVisible(True)

    def setGrantee(self, grantee):
        self._model.setGrantee(grantee)

# GridManagerBase overloaded setter methods
    def _setDefaultWidths(self):
        count = self.Column.getColumnCount() + 1
        width = self._view.getGrid().Model.Width // count
        index = 0
        for column in self.Column.getColumns():
            column.ColumnWidth = width if index != 0 else 2 * width
            column.MinWidth = column.ColumnWidth
            column.Flexibility = 0
            index += 1

# GridManager private getter methods
    def _getGridInfos(self, columns):
        properties = {}
        headers = OrderedDict()
        property = getNamedValue('HorizontalAlign', CENTER)
        # FIXME: The key must be a String to be able to save in json format.
        headers['0'] = self._getColumnTitle(0)
        index = 1
        for column in columns:
            identifier = '%s' % index
            properties[identifier] = (property, )
            headers[identifier] = column
            index += 1
        return properties, headers

