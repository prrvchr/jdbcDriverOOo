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

from com.sun.star.util import CloseVetoException

from .setupmodel import SetupModel

from .setupview import SetupView

from .setuphandler import CloseListener
from .setuphandler import WindowHandler

from .setupdatabase import SetupDataBase

from ...unotool import notifyDispatch

import traceback


class SetupManager():
    def __init__(self, ctx, source, dispatch, name, code, /, database=None, java=None, agent=None, python=False, **extensions):
        self._ctx = ctx
        self._source = source
        self._dispatch = dispatch
        self._database = database is None
        self._java = java is None
        self._python = not python
        self._setup = not python
        self._extension = len(extensions) == 0
        self._closing = False
        self._running = False
        self._model = SetupModel(ctx, name, code, database, java, agent, python, extensions)
        self._listener = CloseListener(self)
        self._view = SetupView(ctx, WindowHandler(self), self._listener, name, *self._model.getViewData())

# XCloseListener
    def queryClosing(self, source, ownership):
        if not ownership:
            self._cancel()
            if self._running:
                raise CloseVetoException()
            self._close()

    def notifyClosing(self, source):
        source.removeCloseListener(self._listener)
        self._close()

    def cancel(self):
        self._cancel()
        if not self._running:
            self._close()

    def next(self):
        step = self._view.getStep()
        if step == 1 or step == 20:
            if self._model.hasExtensions():
                self._checkExtensions()
            elif self._model.hasJava():
                self._checkJava()
            elif self._model.hasDataBase():
                self._checkDataBase()
            elif self._model.hasPython():
                self._checkPython()
            else:
                self._setLastPage()
        elif step == 3:
            if self._model.hasJava():
                self._checkJava()
            elif self._model.hasDataBase():
                self._checkDataBase()
            elif self._model.hasPython():
                self._checkPython()
            else:
                self._setLastPage()
        elif step == 4:
            if self._model.hasPython():
                self._checkPython()
            else:
                self._setLastPage()
        elif step == 6:
            if self._model.hasDataBase():
                self._checkDataBase()
            elif self._model.hasPython():
                self._checkPython()
            else:
                self._setLastPage()
        elif 7 <= step <= 10:
            if self._model.hasPython():
                self._checkPython()
            else:
                self._setLastPage()
        elif 12 <= step <= 14:
            if self._model.hasPython():
                self._checkPython()
            else:
                self._setLastPage()
        elif step == 17:
            self._installPackages()
        elif step == 16 or step == 19:
            sself._setLastPage()
        elif step == 21 or step == 22:
            self._running = True
            self._model.deregisterJob()
            self._running = False
            self._close()
        elif step >= 23:
            self._close()
        else:
            self._setPage(*self._model.getPage(step + 1))

    def setMaxProgress(self, value):
        self._view.setMaxProgress(value)

    def setProgress(self, text, progress):
        self._view.setProgress(text, progress)

    def _cancel(self):
        self._view.enableButtons(False)
        self._closing = True
        self._model.close()

    def _close(self):
        self._model.savePosition(self._view.getWindowPosition())
        if self._dispatch:
            notifyDispatch(self._source, self._dispatch)
        self._view.close()

    def _checkExtensions(self):
        self._running = True
        self._enableNext(False)
        self._setPage(*self._model.getPage(2))
        self._extension, result = self._model.checkExtensions(self.setMaxProgress, self.setProgress)
        if self._closing:
            self._close()
        else:
            if self._extension:
                self._setPage(*self._model.getPage(3))
            else:
                self._setPage(*self._model.getPage(4))
            self._setResult(result)
            self._enableNext(True)
        self._running = False

    def _checkJava(self):
        self._running = True
        self._enableNext(False)
        self._setPage(*self._model.getPage(5))
        self._java, code, minimum, version = self._model.checkJava(self.setMaxProgress, self.setProgress)
        self._running = False
        if self._closing:
            self._close()
        else:
            if self._java:
                self._setPage(*self._model.getPage(6, minimum=minimum))
            else:
                self._setPage(*self._model.getPage(6 + code, minimum=minimum, version=version))
            self._enableNext(True)

    def _checkDataBase(self):
        self._running = True
        self._enableNext(False)
        self._setPage(*self._model.getPage(11))
        setup = SetupDataBase(self._ctx, self.notify, self._view.getIndicator(), self._model.getDataBase())
        setup.start()

    def notify(self, success, created):
        self._running = False
        if self._closing:
            self._close()
        else:
            if success:
                self._setPage(*self._model.getPage(12 + int(created)))
            else:
                self._setPage(*self._model.getPage(14))
            self._database = success
            self._enableNext(True)

    def _checkPython(self):
        self._running = True
        self._enableNext(False)
        self._setPage(*self._model.getPage(15))
        self._python, result = self._model.checkPython(self.setMaxProgress, self.setProgress)
        self._running = False
        if self._closing:
            self._close()
        else:
            if self._python:
                self._setup = True
                self._setPage(*self._model.getPage(16))
            else:
                self._setPage(*self._model.getPage(17))
            self._enableNext(True)
            self._setResult(result)

    def _installPackages(self):
        self._running = True
        self._enableNext(False)
        self._setPage(*self._model.getPage(18))
        self._setup, result = self._model.installPackages(self.setMaxProgress, self.setProgress)
        self._running = False
        if self._closing:
            self._close()
        else:
            if self._setup:
                self._setPage(*self._model.getPage(19))
            else:
                self._setPage(*self._model.getPage(20))
            self._setResult(result)
            self._enableNext(True)

    def _setPage(self, *data):
        if not self._closing:
            self._view.setPage(*data)

    def _enableNext(self, enabled):
        if not self._closing:
            self._view.enableNext(enabled)

    def _setResult(self, result):
        if not self._closing:
            self._view.setResult(result)

    def _setLastPage(self):
        success = all((self._extension, self._java, self._setup, self._database))
        page = self._getLastPage(success)
        self._setPage(*self._model.getPage(page))
        if not success:
            self._setErrorMessage(page)
        
    def _getLastPage(self, success):
        return self._getSuccessPage() if success else self._getErrorPage()

    def _getSuccessPage(self):
        return 22 if self._python else 21

    def _getErrorPage(self):
        if not self._extension:
            page = 23
        elif not self._java:
            page = 24
        elif not self._database:
            page = 25
        else:
            page = 20
        if page != 20:
            self._enableNext(False)
        return page

    def _setErrorMessage(self, page):
        if page == 23:
            self._setResult(self._model.getRequiredExtension())
        if page == 24:
            self._setResult(self._model.getRequiredJava())

