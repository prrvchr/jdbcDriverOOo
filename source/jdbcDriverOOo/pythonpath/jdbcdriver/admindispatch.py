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

from jdbcdriver import createService

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
        if url.Path == 'users':
            state, result = self._showUser(arguments)
        elif url.Path == 'groups':
            state, result = self._showGroup(arguments)
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
    #Users methods
    def _showUser(self, arguments):
        state = FAILURE
        try:
            print("AdminDispatch._showUser() 1")
            close = False
            connection = self._frame.Controller.ActiveConnection
            if connection is None:
                print("AdminDispatch._showUser() 2")
                datasource = self._frame.Controller.DataSource
                connection = datasource.getConnection(datasource.User, datasource.Password)
                close = True
            print("AdminDispatch._showUser() 3")
            manager = UserManager(self._ctx, connection, self._frame.getContainerWindow())
            print("AdminDispatch._showUser() 4")
            if manager.execute() == OK:
                print("AdminDispatch._showUser() 5")
                state = SUCCESS
                pass
            #mri = createService(self._ctx, 'mytools.Mri')
            #mri.inspect(connection)
            if close:
                print("AdminDispatch._showGroup() 6")
                connection.close()
            print("AdminDispatch._showGroup() 7")
        except Exception as e:
            msg = "Error: %s" % traceback.print_exc()
            print(msg)
        return state, None

    #Groups methods
    def _showGroup(self, arguments):
        state = FAILURE
        try:
            print("AdminDispatch._showGroup() 1")
            close = False
            connection = self._frame.Controller.ActiveConnection
            if connection is None:
                print("AdminDispatch._showGroup() 2")
                datasource = self._frame.Controller.DataSource
                connection = datasource.getConnection(datasource.User, datasource.Password)
                close = True
            print("AdminDispatch._showGroup() 3")
            manager = GroupManager(self._ctx, connection, self._frame.getContainerWindow())
            print("AdminDispatch._showGroup() 4")
            if manager.execute() == OK:
                print("AdminDispatch._showGroup() 5")
                state = SUCCESS
                pass
            #mri = createService(self._ctx, 'mytools.Mri')
            #mri.inspect(connection)
            if close:
                print("AdminDispatch._showGroup() 6")
                connection.close()
            print("AdminDispatch._showGroup() 7")
        except Exception as e:
            msg = "Error: %s" % traceback.print_exc()
            print(msg)
        return state, None

    def _getConnection(self, controller):
        print("AdminDispatch._getConnection() 1")
        close = False
        print("AdminDispatch._getConnection() 2")
        connection = controller.ActiveConnection
        if connection is None:
            print("AdminDispatch._getConnection() 3")
            datasource = controller.DataSource
            connection = datasource.getConnection(datasource.User, datasource.Password)
            close = True
        print("AdminDispatch._getConnection() 4")
        return close, connection

