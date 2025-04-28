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

import unohelper

from ...unotool import getDialog

from ...configuration import g_identifier


class MemberView(unohelper.Base):
    def __init__(self, ctx, xdl, handler, parent, title, members, available):
        self._dialog = getDialog(ctx, g_identifier, xdl, handler, parent)
        self._dialog.setTitle(title)
        self._getSelectedMembers().Model.StringItemList = members
        self._getAvailableMembers().Model.StringItemList = available

    def execute(self):
        return self._dialog.execute()

    def dispose(self):
        self._dialog.dispose()

    def toogleRemove(self, enabled):
        self._getRemoveButton().Model.Enabled = enabled

    def toogleAdd(self, enabled):
        self._getAddButton().Model.Enabled = enabled

    def getMembers(self):
        return self._getSelectedMembers().Model.StringItemList

    def removeMember(self):
        control = self._getSelectedMembers()
        user = control.getSelectedItem()
        self._removeMember(control, user)
        self._addMember(self._getAvailableMembers(), user)
        self.toogleRemove(False)

    def addMember(self):
        control = self._getAvailableMembers()
        user = control.getSelectedItem()
        self._removeMember(control, user)
        control = self._getSelectedMembers()
        self._addMember(control, user)
        self.toogleAdd(False)

    def enableOk(self, enabled):
        self._getOkButton().Model.Enabled = enabled

    def _addMember(self, control, member):
        members = list(control.Model.StringItemList) if control.ItemCount else []
        members.append(member)
        control.Model.StringItemList = tuple(members)

    def _removeMember(self, control, member):
        members = control.Model.StringItemList if control.ItemCount else ()
        control.Model.StringItemList = tuple(m for m in members if m != member)

    def _getSelectedMembers(self):
        return self._dialog.getControl('ListBox1')

    def _getAvailableMembers(self):
        return self._dialog.getControl('ListBox2')

    def _getRemoveButton(self):
        return self._dialog.getControl('CommandButton1')

    def _getAddButton(self):
        return self._dialog.getControl('CommandButton2')

    def _getOkButton(self):
        return self._dialog.getControl('CommandButton4')

