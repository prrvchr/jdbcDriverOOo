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

import uno
import unohelper


from com.sun.star.frame import FeatureStateEvent
from com.sun.star.frame import XNotifyingDispatch

from com.sun.star.frame.DispatchResultState import SUCCESS
from com.sun.star.frame.DispatchResultState import FAILURE

from com.sun.star.ui.dialogs.ExecutableDialogResults import OK

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from .admin import UserManager
from .admin import GroupManager

from jdbcdriver import createMessageBox
from jdbcdriver import createService
from jdbcdriver import hasInterface
from jdbcdriver import getStringResource
from jdbcdriver import g_extension
from jdbcdriver import g_identifier

import traceback


class AdminDispatch(unohelper.Base,
                    XNotifyingDispatch):
    def __init__(self, ctx, frame):
        self._ctx = ctx
        self._frame = frame
        self._listeners = []

# XNotifyingDispatch
    def dispatchWithNotification(self, url, arguments, listener):
        state, result = self.dispatch(url, arguments)
        struct = 'com.sun.star.frame.DispatchResultEvent'
        notification = uno.createUnoStruct(struct, self, state, result)
        listener.dispatchFinished(notification)

    def dispatch(self, url, arguments):
        state = FAILURE
        result = None
        users = "com.sun.star.sdbcx.XUsersSupplier"
        groups = "com.sun.star.sdbcx.XGroupsSupplier"
        parent = self._frame.getContainerWindow()
        close, connection = self._getConnection()
        if not hasInterface(connection, users) or not hasInterface(connection, groups):
            resolver = getStringResource(self._ctx, g_identifier, g_extension)
            dialog = createMessageBox(parent, self._getAdminMessage(resolver), self._getAdminTitle(resolver), 'error')
            dialog.execute()
            dialog.dispose()
        elif url.Path == 'users':
            state, result = self._showUser(connection, parent)
        elif url.Path == 'groups':
            state, result = self._showGroup(connection, parent)
        if close:
            connection.close()
        return state, result

    def addStatusListener(self, listener, url):
        #state = FeatureStateEvent()
        #state.FeatureURL = url
        #state.IsEnabled = True
        #state.State = True
        #listener.statusChanged(state)
        self._listeners.append(listener);

    def removeStatusListener(self, listener, url):
        if listener in self._listeners:
            self._listeners.remove(listener)

# AdminDispatch private methods
    def _showUser(self, connection, parent):
        state = FAILURE
        try:
            manager = UserManager(self._ctx, connection, parent)
            manager.execute()
            state = SUCCESS
            manager.dispose()
        except Exception as e:
            msg = "Error: %s" % traceback.print_exc()
            print(msg)
        return state, None

    def _showGroup(self, connection, parent):
        state = FAILURE
        try:
            manager = GroupManager(self._ctx, connection, parent)
            manager.execute()
            state = SUCCESS
            manager.dispose()
        except Exception as e:
            msg = "Error: %s" % traceback.print_exc()
            print(msg)
        return state, None

    def _getConnection(self):
        close = False
        connection = self._frame.Controller.ActiveConnection
        if connection is None:
            datasource = self._frame.Controller.DataSource
            connection = datasource.getConnection(datasource.User, datasource.Password)
            close = True
        return close, connection

    def _getAdminTitle(self, resolver):
        return resolver.resolveString('MessageBox.Admin.Title')

    def _getAdminMessage(self, resolver):
        return resolver.resolveString('MessageBox.Admin.Message')

