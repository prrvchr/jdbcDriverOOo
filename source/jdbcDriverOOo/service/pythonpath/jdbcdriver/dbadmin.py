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

from com.sun.star.frame import FeatureStateEvent

from com.sun.star.frame import XNotifyingDispatch

from com.sun.star.frame.DispatchResultState import SUCCESS
from com.sun.star.frame.DispatchResultState import FAILURE

from .user import UserManager

from .group import GroupManager

from .unotool import createMessageBox
from .unotool import getInteractionHandler
from .unotool import getStringResource
from .unotool import hasInterface

from .configuration import g_extension
from .configuration import g_identifier

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
        parent = self._frame.getContainerWindow()
        close, connection = self._getConnection()
        if url.Path == 'users':
            if not self._supportUsers(connection):
                self._showDialog(parent)
            else:
                state, result = self._showUsers(connection, parent, connection.getGroups())
        elif url.Path == 'groups':
            if not self._supportUsers(connection) or not self._supportGroups(connection):
                self._showDialog(parent)
            else:
                state, result = self._showGroups(connection, parent, connection.getGroups())
        if close:
            connection.close()
        return state, result

    def addStatusListener(self, listener, url):
        state = FeatureStateEvent()
        state.FeatureURL = url
        state.IsEnabled = True
        #state.State = True
        listener.statusChanged(state)
        self._listeners.append(listener);

    def removeStatusListener(self, listener, url):
        if listener in self._listeners:
            self._listeners.remove(listener)

# AdminDispatch private methods
    def _supportUsers(self, connection):
        interface = 'com.sun.star.sdbcx.XUsersSupplier'
        return hasInterface(connection, interface) and connection.getUsers() is not None

    def _supportGroups(self, connection):
        interface = 'com.sun.star.sdbcx.XGroupsSupplier'
        return hasInterface(connection, interface) and connection.getGroups() is not None

    def _showDialog(self, parent):
        dialog = createMessageBox(parent, *self._getDialogData())
        dialog.execute()
        dialog.dispose()

    def _showUsers(self, connection, parent, groups):
        state = FAILURE
        try:
            manager = UserManager(self._ctx, connection, parent, groups)
            manager.execute()
            state = SUCCESS
            manager.dispose()
        except Exception as e:
            msg = "Error: %s" % traceback.format_exc()
            print(msg)
        return state, None

    def _showGroups(self, connection, parent, groups):
        state = FAILURE
        try:
            manager = GroupManager(self._ctx, connection, parent, groups)
            manager.execute()
            state = SUCCESS
            manager.dispose()
        except Exception as e:
            msg = "Error: %s" % traceback.format_exc()
            print(msg)
        return state, None

    def _getConnection(self):
        close = False
        connection = self._frame.Controller.ActiveConnection
        # FIXME: In Base the connection to the database is not necessarily open, if this is
        # FIXME: the case we open an isolated connection in order to be able to close it.
        if connection is None:
            datasource = self._frame.Controller.DataSource
            # FIXME: If password is required then we need an InteractionHandler to get it...
            if datasource.IsPasswordRequired:
                handler = getInteractionHandler(self._ctx)
                connection = datasource.getIsolatedConnectionWithCompletion(handler)
            else:
                connection = datasource.getIsolatedConnection(datasource.User, datasource.Password)
            close = True
        return close, connection

    def _getDialogData(self):
        resolver = getStringResource(self._ctx, g_identifier, 'dialogs', 'MessageBox')
        message = resolver.resolveString('MessageBox.Admin.Message')
        title = resolver.resolveString('MessageBox.Admin.Title')
        return message, title, 'error', 1

