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

from com.sun.star.style.HorizontalAlignment import CENTER

from .gridmanagerbase import GridManagerBase

from ..unotool import getNamedValue

from collections import OrderedDict
import traceback


class GridManager(GridManagerBase):
    def __init__(self, ctx, datasource, listener, flags, url, model, parent, possize, setting, selection, resource, maxi=None, multi=False, name='Grid1'):
        GridManagerBase.__init__(self, ctx, url, model, parent, possize, setting, selection, resource, maxi, multi, name)
        self._datasource = datasource
        self._identifier = '0'
        self._index = 0
        self._type = VARCHAR
        self._view.showGridColumnHeader(False)
        self._properties, self._headers = self._getGridInfos(flags)
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

# GridManager private getter methods
    def _getGridInfos(self, flags):
        properties = {}
        headers = OrderedDict()
        property = getNamedValue('HorizontalAlign', CENTER)
        # FIXME: The key must be a String to be able to save in json format.
        headers['0'] = self._getColumnTitle(0)
        for i in flags:
            identifier = '%s' % i
            properties[identifier] = (property, )
            headers[identifier] = self._getColumnTitle(i)
        return properties, headers

