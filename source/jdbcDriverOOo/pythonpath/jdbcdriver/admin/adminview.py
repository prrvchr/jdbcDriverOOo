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

import unohelper

from com.sun.star.view.SelectionType import SINGLE

from jdbcdriver import getDialog
from jdbcdriver import g_extension


class AdminView(unohelper.Base):
    def __init__(self, ctx, dlg, handler, parent):
        self._dialog = getDialog(ctx, g_extension, dlg, handler, parent)

    def init(self, model, columns, grantees, listener):
        self._createGrid(model, columns)
        self._getGrid().addSelectionListener(listener)
        control = self._getGrantees()
        control.Model.StringItemList = grantees
        if control.ItemCount > 0:
            control.selectItemPos(0, True)

    def execute(self):
        return self._dialog.execute()

    def enableButton(self):
        self._getSetGrantee().Model.Enabled = True
        self._getDropGrantee().Model.Enabled = True

    def enableSetPrivileges(self, enabled):
        self._getSetPrivileges().Model.Enabled = enabled

    def getSelectedGridIndex(self):
        index = -1
        grid = self._getGrid()
        if grid.hasSelectedRows():
            index = grid.getSelectedRows()[0]
        return index

    def getSelectedGrantee(self):
        return self._getGrantees().getSelectedItem()

    def _createGrid(self, model, columns):
        grid = self._dialog.Model.createInstance("com.sun.star.awt.grid.UnoControlGridModel")
        grid.Name = "Grid1"
        grid.PositionX = 10
        grid.PositionY = 82
        grid.Width = 265
        grid.Height = 150
        grid.GridDataModel = model
        grid.ColumnModel = columns
        model.SelectionModel = SINGLE
        model.ShowColumnHeader = True
        model.BackgroundColor = 16777215
        self._dialog.Model.insertByName(grid.Name, grid)

    def _getGrantees(self):
        return self._dialog.getControl('ListBox1')

    def _getAddGrantee(self):
        return self._dialog.getControl('CommandButton1')

    def _getSetGrantee(self):
        return self._dialog.getControl('CommandButton2')

    def _getDropGrantee(self):
        return self._dialog.getControl('CommandButton3')

    def _getGrid(self):
        return self._dialog.getControl('Grid1')

    def _getSetPrivileges(self):
        return self._dialog.getControl('CommandButton4')
