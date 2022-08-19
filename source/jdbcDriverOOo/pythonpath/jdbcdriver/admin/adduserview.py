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

from jdbcdriver import getDialog
from jdbcdriver import g_extension


class AddUserView(unohelper.Base):
    def __init__(self, ctx, handler, parent, title, members, users):
        self._dialog = getDialog(ctx, g_extension, 'AddUserDialog', handler, parent)
        self._dialog.setTitle(title)
        self._getSelectedUsers().Model.StringItemList = members
        self._getAvailableUsers().Model.StringItemList = users

    def execute(self):
        return self._dialog.execute()

    def dispose(self):
        self._dialog.dispose()

    def toogleRemoveUser(self, enabled):
        self._getRemoveButton().Model.Enabled = enabled

    def toogleAddUser(self, enabled):
        self._getAddButton().Model.Enabled = enabled

    def getUsers(self):
        return self._getSelectedUsers().Model.StringItemList

    def removeUser(self):
        control = self._getSelectedUsers()
        user = control.getSelectedItem()
        self._removeUser(control, user)
        self._addUser(self._getAvailableUsers(), user)
        self.toogleRemoveUser(False)

    def addUser(self):
        control = self._getAvailableUsers()
        user = control.getSelectedItem()
        self._removeUser(control, user)
        control = self._getSelectedUsers()
        self._addUser(control, user)
        self.toogleAddUser(False)

    def enableOk(self, enabled):
        self._getOkButton().Model.Enabled = enabled

    def _addUser(self, control, user):
        users = list(control.Model.StringItemList)
        users.append(user)
        control.Model.StringItemList = tuple(users)

    def _removeUser(self, control, user):
        users = control.Model.StringItemList
        control.Model.StringItemList = tuple(u for u in users if u != user)

    def _getSelectedUsers(self):
        return self._dialog.getControl('ListBox1')

    def _getAvailableUsers(self):
        return self._dialog.getControl('ListBox2')

    def _getRemoveButton(self):
        return self._dialog.getControl('CommandButton1')

    def _getAddButton(self):
        return self._dialog.getControl('CommandButton2')

    def _getOkButton(self):
        return self._dialog.getControl('CommandButton4')

