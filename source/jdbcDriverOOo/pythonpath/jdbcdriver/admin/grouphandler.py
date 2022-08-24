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

from com.sun.star.awt import XDialogEventHandler

import traceback


class DialogHandler(unohelper.Base,
                    XDialogEventHandler):
    def __init__(self, manager):
        self._manager = manager

    # com.sun.star.awt.XDialogEventHandler
    def callHandlerMethod(self, dialog, event, method):
        try:
            handled = False
            if method == 'SetGrantee':
                if self._manager.isHandlerEnabled():
                    self._manager.setGrantee(event.Source.getSelectedItem())
                handled = True
            elif method == 'NewGroup':
                self._manager.createGroup()
                handled = True
            elif method == 'SetUsers':
                self._manager.setUsers()
                handled = True
            elif method == 'SetRoles':
                self._manager.setRoles()
                handled = True
            elif method == 'DropGroup':
                self._manager.dropGroup()
                handled = True
            elif method == 'SetPrivileges':
                self._manager.setPrivileges()
                handled = True
            return handled
        except Exception as e:
            msg = "Error: %s" % traceback.print_exc()
            print(msg)

    def getSupportedMethodNames(self):
        return ('SetGrantee',
                'NewGroup',
                'SetUsers',
                'SetRoles',
                'DropGroup',
                'SetPrivileges')


class NewGroupHandler(unohelper.Base,
                      XDialogEventHandler):
    def __init__(self, manager):
        self._manager = manager

    # com.sun.star.awt.XDialogEventHandler
    def callHandlerMethod(self, dialog, event, method):
        try:
            handled = False
            if method == 'SetGroup':
                self._manager.setGroup(event.Source.Text)
                handled = True
            return handled
        except Exception as e:
            msg = "Error: %s" % traceback.print_exc()
            print(msg)

    def getSupportedMethodNames(self):
        return ('SetGroup', )


class GroupsHandler(unohelper.Base,
                    XDialogEventHandler):
    def __init__(self, manager):
        self._manager = manager

    # com.sun.star.awt.XDialogEventHandler
    def callHandlerMethod(self, dialog, event, method):
        try:
            handled = False
            if method == 'ToogleRemove':
                enabled = event.Source.getSelectedItemPos() != -1
                self._manager.toogleRemove(enabled)
                handled = True
            elif method == 'RemoveMember':
                self._manager.removeGroup()
                handled = True
            elif method == 'ToogleAdd':
                enabled = event.Source.getSelectedItemPos() != -1
                self._manager.toogleAdd(enabled)
                handled = True
            elif method == 'AddMember':
                self._manager.addGroup()
                handled = True
            return handled
        except Exception as e:
            msg = "Error: %s" % traceback.print_exc()
            print(msg)

    def getSupportedMethodNames(self):
        return ('ToogleRemove',
                'RemoveMember',
                'ToogleAdd',
                'AddMember')


