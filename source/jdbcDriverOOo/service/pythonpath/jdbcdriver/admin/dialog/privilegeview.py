#!
# -*- coding: utf-8 -*-

"""
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
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

from ...unotool import getDialog

from ...configuration import g_extension


class PrivilegeView():
    def __init__(self, ctx, flags, table, columns, privileges, grantables, inherited):
        self._dialog = getDialog(ctx, g_extension, 'PrivilegesDialog')
        rectangle = uno.createUnoStruct('com.sun.star.awt.Rectangle', 10, 10, 90, 15)
        self._createCheckBox(columns, rectangle)
        self._dialog.Title = table
        self.setPrivileges(flags, privileges, grantables, inherited)

    def setPrivileges(self, flags, privileges, grantables, inherited):
        for index, flag in enumerate(flags):
            state = 1 if flag == privileges & flag else 0
            tristate = state == 0 and flag == inherited & flag
            control = self._getPrivilege(index)
            control.Model.TriState = tristate
            control.State = 2 if tristate else state
            control.Model.Enabled = flag == grantables & flag

    def execute(self):
        return self._dialog.execute()

    def getPrivileges(self, flags):
        privileges = 0
        for index, flag in enumerate(flags):
            control = self._getPrivilege(index)
            privileges += flag if control.State == 1 else 0
        return privileges

    def dispose(self):
        self._dialog.dispose()

    def _createCheckBox(self, columns, rectangle):
        index = 0
        service = 'com.sun.star.awt.UnoControlCheckBoxModel'
        for column in columns:
            model = self._getCheckBoxModel(service, rectangle, column, index)
            self._dialog.Model.insertByName(self._getCheckBoxName(index), model)
            index += 1
        if index:
            height = index * rectangle.Height
            self._dialog.Model.Height += height
            for i in range(2):
                button = self._getButton(i + 1).Model
                button.PositionY += height
                button.TabIndex = index + i

    def _getCheckBoxModel(self, service, rectangle, label, index):
        model = self._dialog.Model.createInstance(service)
        model.PositionX = rectangle.X
        model.PositionY = rectangle.Y
        model.Width = rectangle.Width
        model.Height = rectangle.Height
        model.Label = label
        model.TabIndex = index
        rectangle.Y += rectangle.Height
        return model

    def _getCheckBoxName(self, index):
        return 'CheckBox%s' % (index + 1)

    def _getPrivilege(self, index):
        return self._dialog.getControl(self._getCheckBoxName(index))

    def _getButton(self, index):
        return self._dialog.getControl('CommandButton%s' % index)

