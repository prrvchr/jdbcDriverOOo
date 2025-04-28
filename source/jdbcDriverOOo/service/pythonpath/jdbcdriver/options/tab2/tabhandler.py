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

from com.sun.star.awt import XContainerWindowEventHandler

import traceback

class TabHandler(unohelper.Base,
                 XContainerWindowEventHandler):
    def __init__(self, manager):
        self._manager = manager

    # XContainerWindowEventHandler
    def callHandlerMethod(self, window, event, method):
        try:
            handled = False
            if method == 'SetDriver':
                self._manager.setDriver(event.Source.getSelectedItem())
                handled = True
            elif method == 'EditDriver':
                self._manager.editDriver()
                handled = True
            elif method == 'AddDriver':
                self._manager.addDriver()
                handled = True
            elif method == 'RemoveDriver':
                self._manager.removeDriver()
                handled = True
            elif method == 'Confirm':
                self._manager.confirmDriver()
                handled = True
            elif method == 'Cancel':
                self._manager.cancelDriver()
                handled = True
            elif method == 'UpdateDriverName':
                self._manager.updateDriverName(event.Source.Text)
                handled = True
            elif method == 'UpdateArchive':
                self._manager.updateArchive()
                handled = True
            elif method == 'ViewArchive':
                self._manager.viewArchive()
                handled = True
            elif method == 'SetGroup':
                self._manager.setGroup(event.Source.getSelectedItem())
                handled = True
            return handled
        except Exception as e:
            msg = "TabHandler.callHandlerMethod() Error: %s" % traceback.format_exc()
            print(msg)

    def getSupportedMethodNames(self):
        return ('SetDriver',
                'EditDriver'
                'AddDriver',
                'RemoveDriver',
                'Confirm',
                'Cancel',
                'UpdateDriverName',
                'UpdateArchive',
                'ViewArchive',
                'SetGroup')

