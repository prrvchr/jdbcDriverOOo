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

from .gridmodel import GridModel

from .gridview import GridView

from .gridhandler import WindowHandler
from .gridhandler import GridDataListener

from ..unotool import createService
from ..unotool import getConfiguration
from ..unotool import getResourceLocation
from ..unotool import getStringResource

from ..configuration import g_extension
from ..configuration import g_identifier

import json
from threading import Thread
from collections import OrderedDict
import traceback


class GridManager(unohelper.Base):
    def __init__(self, ctx, rowset, parent, possize, identifier, setting, resource=None, maxi=None, multi=False, name='Grid1'):
        self._ctx = ctx
        self._factor = 5
        # We need to save the DataSource Name to be able to save
        # Columns Widths after DataSource is disposed
        self._name = None
        self._datasource = None
        self._query = None
        self._resource = resource
        if resource is not None:
            self._resolver = getStringResource(ctx, g_identifier, g_extension)
        self._setting = setting
        self._config = getConfiguration(ctx, g_identifier, True)
        widths = self._config.getByName(self._getConfigWidthName())
        self._widths = json.loads(widths, object_pairs_hook=OrderedDict)
        orders = self._config.getByName(self._getConfigOrderName())
        self._orders = json.loads(orders)
        self._max = maxi
        self._multi = multi
        self._identifier = identifier
        self._index = -1
        self._type = -1
        self._rowset = rowset
        self._url = getResourceLocation(ctx, g_identifier, g_extension)
        model = createService(ctx, 'com.sun.star.awt.grid.SortableGridDataModel')
        self._grid = GridModel(ctx, model)
        self._columns = {}
        model.initialize((self._grid, ))
        # TODO: We can use an XGridDataListener to be notified when the row display order is changed
        #model.addGridDataListener(GridDataListener(self))
        self._view = GridView(ctx, name, model, parent, WindowHandler(self), possize)
        self._model = self._view.getGridColumnModel()

# GridManager getter methods
    def getGridModels(self):
        return self._grid, self._model

    def getGridModel(self):
        return self._view.getGridDataModel()

    def getSelectedRows(self):
        return self._view.getSelectedRows()

    def getSelectedIdentifiers(self):
        identifiers = []
        for row in self._view.getSelectedRows():
            identifiers.append(self.getRowIdentifier(row))
        return tuple(identifiers)

    def getRowIdentifier(self, row):
        return self._view.getGridDataModel().getCellData(self._index, row)

    def getIdentifierIndex(self):
        return self._index

    def getIdentifierDataType(self):
        return self._type

# GridManager setter methods
    def dispose(self):
        self._model.dispose()
        self._grid.dispose()

    def addSelectionListener(self, listener):
        self._view.getGrid().addSelectionListener(listener)

    def removeSelectionListener(self, listener):
        self._view.getGrid().removeSelectionListener(listener)

    def showControls(self, state):
        self._view.setWindowPosSize(state)

    def deselectAllRows(self):
        self._view.deselectAllRows()

    def setRowSetData(self, rowset):
        datasource = rowset.ActiveConnection.Parent
        name = datasource.Name
        query = rowset.UpdateTableName
        changed = self._isDataSourceChanged(name, query)
        if changed:
            if self._isGridLoaded():
                self._saveWidths()
                self._saveOrders()
            # We can hide GridColumnHeader and reset GridDataModel
            # but after saving GridColumnModel Widths
            self._view.showGridColumnHeader(False)
            #self._grid.resetRowSetData()
            self._columns, self._index, self._type = self._getColumns(rowset.getMetaData())
            identifiers = self._initColumnModel(name, query)
            self._view.initColumns(self._url, self._columns, identifiers)
            self._name = name
            self._query = query
            self._datasource = datasource
            self._view.showGridColumnHeader(True)
        self._view.setWindowVisible(False)
        self._grid.setRowSetData(rowset)
        self._view.setWindowVisible(True)
        if changed:
            self._grid.sortByColumn(*self._getSavedOrders(name, query))

    def saveColumnSettings(self):
        self.saveColumnWidths()
        self.saveColumnOrders()

    def saveColumnWidths(self):
        self._saveWidths()
        name = self._getConfigWidthName()
        widths = json.dumps(self._widths)
        self._config.replaceByName(name, widths)
        self._config.commitChanges()

    def saveColumnOrders(self):
        self._saveOrders()
        name = self._getConfigOrderName()
        orders = json.dumps(self._orders)
        self._config.replaceByName(name, orders)
        self._config.commitChanges()

    def setColumn(self, identifier, add, reset, index):
        self._view.deselectColumn(index)
        if reset:
            modified, identifiers = self._resetColumn()
        else:
            identifiers = [column.Identifier for column in self._model.getColumns()]
            if add:
                modified = self._addColumn(identifiers, identifier)
            else:
                modified = self._removeColumn(identifiers, identifier)
        if modified:
            self._setDefaultWidths()
            self._view.setColumns(self._url, identifiers)

    def isSelected(self, image):
        return image.endswith(self._view.getSelected())

    def isUnSelected(self, image):
        return image.endswith(self._view.getUnSelected())

    def setColumnOrder(self):
        model = self._view.getGridDataModel()
        pair = model.getCurrentSortOrder()
        print("GridManager.setColumnOrder() First: %s Second: %s " % (pair.First, pair.Second))

# GridManager private methods
    def _isDataSourceChanged(self, name, query):
        return self._name != name or self._query != query

    def _isGridLoaded(self):
        return self._datasource is not None

    def _saveWidths(self):
        widths = self._getColumnWidths()
        if self._multi:
            name = self._getDataSourceName(self._name, self._query)
            self._widths[name] = widths
        else:
            self._widths = widths

    def _saveOrders(self):
        orders = self._getColumnOrders()
        if self._multi:
            name = self._getDataSourceName(self._name, self._query)
            self._orders[name] = orders
        else:
            self._orders = orders

    def _getDataSourceName(self, datasource, query):
        if self._multi:
            name = '%s.%s' % (datasource, query)
        else:
            name = datasource
        return name

    def _resetColumn(self):
        self._removeColumns()
        identifiers = self._getDefaultIdentifiers()
        for identifier in identifiers:
            self._createColumn(identifier)
        return True, identifiers

    def _addColumn(self, identifiers, identifier):
        modified = False
        if identifier not in identifiers:
            if self._createColumn(identifier):
                identifiers.append(identifier)
                modified = True
        return modified

    def _removeColumn(self, identifiers, identifier):
        modified = False
        if identifier in identifiers:
            if self._removeIdentifier(identifier):
                identifiers.remove(identifier)
                modified = True
        return modified

    def _saveConfigOrder(self, order):
        name = self._getConfigOrderName()
        self._config.replaceByName(name, order)
        self._config.commitChanges()

    def _getColumns(self, metadata):
        index = type = -1
        columns = OrderedDict()
        for i in range(metadata.getColumnCount()):
            column = metadata.getColumnLabel(i +1)
            title = self._getColumnTitle(column)
            if self._identifier == column:
                index = i
                type = metadata.getColumnType(i +1)
            columns[column] = title
        return columns, index, type

    def _initColumnModel(self, datasource, query):
        # TODO: ColumnWidth should be assigned after all columns have 
        # TODO: been added to the GridColumnModel
        self._removeColumns()
        widths = self._getSavedWidths(datasource, query)
        identifiers = self._getIdentifiers(widths)
        if widths:
            for identifier in widths:
                self._createColumn(identifier)
            self._setSavedWidths(widths)
        else:
            for identifier in identifiers:
                self._createColumn(identifier)
            self._setDefaultWidths()
        return identifiers

    def _getSavedWidths(self, datasource, query):
        widths = {}
        if self._multi:
            name = self._getDataSourceName(datasource, query)
            if name in self._widths:
                widths = self._widths[name]
        else:
            widths = self._widths
        return widths

    def _getSavedOrders(self, datasource, query):
        orders = (-1, True)
        if self._multi:
            name = self._getDataSourceName(datasource, query)
            if name in self._orders:
                orders = self._orders[name]
        else:
            orders = self._orders
        return orders

    def _getIdentifiers(self, widths):
        identifiers = []
        for identifier in widths:
            if identifier in self._columns:
                identifiers.append(identifier)
        if not identifiers:
            identifiers = self._getDefaultIdentifiers()
        return identifiers

    def _removeColumns(self):
        for index in range(self._model.getColumnCount() -1, -1, -1):
            self._model.removeColumn(index)

    def _createColumn(self, identifier):
        created = False
        if identifier in self._columns:
            column = self._model.createColumn()
            column.Identifier = identifier
            column.Title = self._columns[identifier]
            indexes = tuple(self._columns.keys())
            column.DataColumnIndex = indexes.index(identifier)
            self._model.addColumn(column)
            created = True
        return created

    def _removeIdentifier(self, identifier):
        removed = False
        for index in range(self._model.getColumnCount() -1, -1, -1):
            column = self._model.getColumn(index)
            if column.Identifier == identifier:
                self._model.removeColumn(index)
                removed = True
                break
        return removed

    def _setSavedWidths(self, widths):
        for column in self._model.getColumns():
            identifier = column.Identifier
            flex = len(column.Title)
            column.MinWidth = flex * self._factor
            column.Flexibility = 0
            if identifier in widths:
                column.ColumnWidth = widths[identifier]
            else:
                column.ColumnWidth = flex * self._factor

    def _setDefaultWidths(self):
        for column in self._model.getColumns():
            flex = len(column.Title)
            width = flex * self._factor
            column.ColumnWidth = width
            column.MinWidth = width
            column.Flexibility = flex

    def _getColumnWidths(self):
        widths = OrderedDict()
        for column in self._model.getColumns():
            widths[column.Identifier] = column.ColumnWidth
        return widths

    def _getColumnOrders(self):
        pair = self._grid.getCurrentSortOrder()
        return pair.First, pair.Second

    def _getDefaultIdentifiers(self):
        identifiers = tuple(self._columns.keys())
        return identifiers[slice(self._max)]

    def _getColumnTitle(self, identifier):
        if self._resource is None:
            title = identifier
        else:
            title = self._resolver.resolveString(self._resource % identifier)
        return title

    def _getConfigWidthName(self):
        return '%s%s' % (self._setting, 'Columns')

    def _getConfigOrderName(self):
        return '%s%s' % (self._setting, 'Orders')
