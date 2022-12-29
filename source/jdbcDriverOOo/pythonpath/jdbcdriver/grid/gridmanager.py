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
        identifiers = self._initColumnModel(datasource)
        self._view.initColumns(self._url, self._headers, identifiers)
        self._model.sortByColumn(*self._getSavedOrders(datasource))
        self._view.showGridColumnHeader(True)
        self._view.addSelectionListener(listener)

# GridManager private getter methods
    def refresh(self, row=None):
        self._view.setWindowVisible(False)
        print("GridManager.refresh() *********************************************************")
        self._view.setWindowVisible(True)

    def setGridVisible(self, enabled):
        self._view.setWindowVisible(enabled)

# GridManager private getter methods
    def _getHeadersInfo(self, flags):
        index = 0
        type = VARCHAR
        headers = OrderedDict()
        # FIXME: The key must be a String to be able to save in json format.
        headers['0'] = self._getColumnTitle(0)
        for key in flags:
            headers['%s' % key] = self._getColumnTitle(key)
        return headers, index, type

