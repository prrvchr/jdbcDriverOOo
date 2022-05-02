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

from com.sun.star.awt import XContainerWindowEventHandler
from com.sun.star.lang import XServiceInfo

from jdbcdriver import OptionsManager

from jdbcdriver import g_identifier

import traceback

# pythonloader looks for a static g_ImplementationHelper variable
g_ImplementationHelper = unohelper.ImplementationHelper()
g_ImplementationName = '%s.OptionsHandler' % g_identifier


class OptionsHandler(unohelper.Base,
                     XServiceInfo,
                     XContainerWindowEventHandler):
    def __init__(self, ctx):
        self._manager = OptionsManager(ctx)

    # XContainerWindowEventHandler
    def callHandlerMethod(self, window, event, method):
        try:
            handled = False
            if method == 'external_event':
                if event == 'initialize':
                    self._manager.initialize(window)
                    handled = True
                elif event == 'ok':
                    self._manager.saveSetting()
                    handled = True
                elif event == 'back':
                    self._manager.reloadSetting()
                    handled = True
            elif method == 'Base':
                self._manager.setLevel(0)
                handled = True
            elif method == 'Enhanced':
                self._manager.setLevel(1)
                handled = True
            elif method == 'SetDriver':
                if self._manager.isHandlerEnabled():
                    driver = event.Source.getSelectedItem()
                    self._manager.setDriver(driver)
                handled = True
            elif method == 'New':
                self._manager.newDriver()
                handled = True
            elif method == 'Remove':
                self._manager.removeDriver()
                handled = True
            elif method == 'Save':
                self._manager.saveDriver()
                handled = True
            elif method == 'Cancel':
                self._manager.cancelDriver()
                handled = True
            elif method == 'Check':
                self._manager.checkDriver()
                handled = True
            elif method == 'Update':
                self._manager.updateArchive()
                handled = True
            elif method == 'Search':
                self._manager.searchArchive()
                handled = True
            elif method == 'SetLogger':
                if self._manager.isHandlerEnabled():
                    level = event.Source.getSelectedItem()
                    self._manager.setLogger(level)
                handled = True
            return handled
        except Exception as e:
            msg = "OptionsHandler.callHandlerMethod() Error: %s" % traceback.print_exc()
            print(msg)

    def getSupportedMethodNames(self):
        return ('external_event',
                'Base',
                'Enhanced',
                'SetDriver',
                'New',
                'Remove',
                'Save',
                'Cancel',
                'Check',
                'Update',
                'Search',
                'SetLogger')

    # XServiceInfo
    def supportsService(self, service):
        return g_ImplementationHelper.supportsService(g_ImplementationName, service)
    def getImplementationName(self):
        return g_ImplementationName
    def getSupportedServiceNames(self):
        return g_ImplementationHelper.getSupportedServiceNames(g_ImplementationName)


g_ImplementationHelper.addImplementation(OptionsHandler,                            # UNO object class
                                         g_ImplementationName,                      # Implementation name
                                        (g_ImplementationName,))                    # List of implemented services

