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

from com.sun.star.frame import XDispatchProvider
from com.sun.star.frame import FeatureStateEvent
from com.sun.star.frame import XNotifyingDispatch

from com.sun.star.frame.DispatchResultState import SUCCESS
from com.sun.star.frame.DispatchResultState import FAILURE

from com.sun.star.lang import XInitialization
from com.sun.star.lang import XServiceInfo

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from jdbcdriver import UserManager
from jdbcdriver import GroupManager

from jdbcdriver import createMessageBox
from jdbcdriver import createService
from jdbcdriver import hasInterface
from jdbcdriver import getStringResource

from jdbcdriver import g_extension
from jdbcdriver import g_identifier

import traceback

# pythonloader looks for a static g_ImplementationHelper variable
g_ImplementationHelper = unohelper.ImplementationHelper()
g_ImplementationName = '%s.AdminDispatcher' % g_identifier


class AdminDispatcher(unohelper.Base,
                      XDispatchProvider,
                      XInitialization,
                      XServiceInfo):
    def __init__(self, ctx):
        self._ctx = ctx
        self._frame = None

# XInitialization
    def initialize(self, args):
        service = 'com.sun.star.frame.Frame'
        interface = 'com.sun.star.lang.XServiceInfo'
        if len(args) > 0 and hasInterface(args[0], interface) and args[0].supportsService(service):
            self._frame = args[0]

# XDispatchProvider
    def queryDispatch(self, url, frame, flags):
        dispatch = None
        if url.Path in ('users', 'groups'):
            dispatch = AdminDispatch(self._ctx, self._frame)
        return dispatch

    def queryDispatches(self, requests):
        dispatches = []
        for request in requests:
            dispatch = self.queryDispatch(request.FeatureURL, request.FrameName, request.SearchFlags)
            dispatches.append(dispatch)
        return tuple(dispatches)

    # XServiceInfo
    def supportsService(self, service):
        return g_ImplementationHelper.supportsService(g_ImplementationName, service)
    def getImplementationName(self):
        return g_ImplementationName
    def getSupportedServiceNames(self):
        return g_ImplementationHelper.getSupportedServiceNames(g_ImplementationName)


g_ImplementationHelper.addImplementation(AdminDispatcher,                            # UNO object class
                                         g_ImplementationName,                       # Implementation name
                                         (g_ImplementationName,))                    # List of implemented services


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
        xusers = 'com.sun.star.sdbcx.XUsersSupplier'
        xgroups = 'com.sun.star.sdbcx.XGroupsSupplier'
        parent = self._frame.getContainerWindow()
        close, connection = self._getConnection()
        if not hasInterface(connection, xusers) or not hasInterface(connection, xgroups):
            dialog = createMessageBox(parent, *self._getDialogData())
            dialog.execute()
            dialog.dispose()
        else:
            if url.Path == 'users':
                state, result = self._showUsers(connection, parent, self._getGroups(connection, xgroups))
            elif url.Path == 'groups':
                state, result = self._showGroups(connection, parent, self._getGroups(connection, xgroups))
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
    def _getGroups(self, connection, interface):
        groups = connection.getGroups()
        recursive = False
        if groups.hasElements():
            recursive = hasInterface(groups.getByIndex(0), interface)
        return groups, recursive

    def _showUsers(self, connection, parent, groups):
        state = FAILURE
        try:
            manager = UserManager(self._ctx, connection, parent, *groups)
            manager.execute()
            state = SUCCESS
            manager.dispose()
        except Exception as e:
            msg = "Error: %s" % traceback.print_exc()
            print(msg)
        return state, None

    def _showGroups(self, connection, parent, groups):
        state = FAILURE
        try:
            manager = GroupManager(self._ctx, connection, parent, *groups)
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
        # FIXME: In Base the connection to the database is not necessarily open, if this is
        # FIXME: the case we open an isolated connection in order to be able to close it.
        if connection is None:
            datasource = self._frame.Controller.DataSource
            connection = datasource.getIsolatedConnection(datasource.User, datasource.Password)
            close = True
        return close, connection

    def _getDialogData(self):
        resolver = getStringResource(self._ctx, g_identifier, g_extension)
        message = resolver.resolveString('MessageBox.Admin.Message')
        title = resolver.resolveString('MessageBox.Admin.Title')
        return message, title, 'error'

