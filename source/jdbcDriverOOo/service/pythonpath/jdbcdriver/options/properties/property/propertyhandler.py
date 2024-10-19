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
            if method == 'SetType':
                self._manager.setType(event.Source.getSelectedItemPos())
                handled = True
            elif method == 'SetStringValue':
                self._manager.setValue(event.Source.Text)
                handled = True
            elif method == 'SetBooleanValue':
                self._manager.setValue(bool(event.Source.State))
                handled = True
            elif method == 'EditValue':
                self._manager.editValue()
                handled = True
            elif method == 'AddValue':
                self._manager.addValue()
                handled = True
            elif method == 'RemoveValue':
                self._manager.removeValue()
                handled = True
            elif method == 'SetValue':
                self._manager.setListValue(event.Source.getText())
                handled = True
            elif method == 'Confirm':
                self._manager.confirm()
                handled = True
            elif method == 'Cancel':
                self._manager.cancel()
                handled = True
            return handled
        except Exception as e:
            msg = "TabHandler.callHandlerMethod() Error: %s" % traceback.format_exc()
            print(msg)

    def getSupportedMethodNames(self):
        return ('SetType',
                'SetBooleanValue',
                'SetStringValue',
                'EditValue',
                'AddValue',
                'RemoveValue',
                'SetValue',
                'Confirm',
                'Cancel')

