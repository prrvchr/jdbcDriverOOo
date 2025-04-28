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


class WindowHandler(unohelper.Base,
                    XContainerWindowEventHandler):
    def __init__(self, manager):
        self._manager = manager

    # XContainerWindowEventHandler
    def callHandlerMethod(self, window, event, method):
        try:
            handled = False
            if method == 'SetProperty':
                self._manager.setProperty(event.Source.getSelectedItem())
                handled = True
            elif method == 'EditProperty':
                self._manager.editProperty()
                handled = True
            elif method == 'AddProperty':
                self._manager.addProperty()
                handled = True
            elif method == 'RemoveProperty':
                self._manager.removeProperty()
                handled = True
            elif method == 'SetPropertyName':
                self._manager.setPropertyName(event.Source.getText())
                handled = True
            elif method == 'Confirm':
                self._manager.confirmProperty()
                handled = True
            elif method == 'Cancel':
                self._manager.cancelProperty()
                handled = True
            return handled
        except Exception as e:
            msg = "WindowHandler.callHandlerMethod() Error: %s" % traceback.format_exc()
            print(msg)

    def getSupportedMethodNames(self):
        return ('SetProperty',
                'EditProperty',
                'AddProperty',
                'RemoveProperty',
                'SetPropertyName',
                'Confirm',
                'Cancel')

