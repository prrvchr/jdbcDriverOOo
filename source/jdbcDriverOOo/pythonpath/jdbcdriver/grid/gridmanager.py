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

from com.sun.star.sdbc.DataType import VARCHAR

from .gridmanagerbase import GridManagerBase

from collections import OrderedDict
import traceback


class GridManager(GridManagerBase):
    def __init__(self, ctx, datasource, listener, flags, model, parent, possize, setting, selection, resource, maxi=None, multi=False, name='Grid1'):
        GridManagerBase.__init__(self, ctx, model, parent, possize, setting, selection, resource, maxi, multi, name)
        self._datasource = datasource
        self._identifier = 'Table'
        self._view.showGridColumnHeader(False)
        self._headers, self._index, self._type = self._getHeadersInfo(flags)
        identifiers = self._initColumnModel(datasource, '')
        self._view.initColumns(self._url, self._headers, identifiers)
        self._model.sortByColumn(*self._getSavedOrders(datasource, ''))
        self._view.showGridColumnHeader(True)
        self._view.addSelectionListener(listener)

# GridManager setter methods
    def setDataModel(self, rowset, identifier):
        datasource = rowset.ActiveConnection.Parent.Name
        query = rowset.UpdateTableName
        changed = self._isDataSourceChanged(datasource, query)
        if changed:
            if self._isGridLoaded():
                self._saveWidths()
                self._saveOrders()
            # We can hide GridColumnHeader and reset GridDataModel
            # but after saving GridColumnModel Widths
            self._view.showGridColumnHeader(False)
            #self._grid.resetRowSetData()
            self._identifier = identifier
            self._columns, self._index, self._type = self._getColumns(rowset.getMetaData())
            identifiers = self._initColumnModel(datasource, query)
            self._view.initColumns(self._url, self._columns, identifiers)
            self._query = query
            self._datasource = datasource
            self._view.showGridColumnHeader(True)
        self._view.setWindowVisible(False)
        self._grid.setRowSetData(rowset)
        self._view.setWindowVisible(True)
        if changed:
            self._grid.sortByColumn(*self._getSavedOrders(datasource, query))

# GridManager private methods
    def _getHeadersInfo(self, flags):
        index = 0
        type = VARCHAR
        headers = OrderedDict()
        title = self._resolver.resolveString('GranteeDialog.Grid1.Column1')
        headers[title] = title
        for index in flags:
            title = self._getColumnTitle(index)
            headers[title] = title
        return headers, index, type

