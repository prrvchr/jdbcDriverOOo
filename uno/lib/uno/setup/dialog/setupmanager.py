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

from ...unotool import notifyDispatch

import traceback


class SetupManager():
    def __init__(self, ctx, source, dispatch, name, /, database=None, java=None, agent=None, python=False, **extensions):
        self._ctx = ctx
        self._source = source
        self._dispatch = dispatch
        self._database = database is None
        self._java = java is None
        self._python = not python
        self._setup = False
        self._extension = len(extensions) == 0
        self._closing = False
        self._running = False
        self._step = 1
        self._model = SetupModel(ctx, name, database, java, agent, python, extensions)
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
        if self._step == 1:
            if self._model.hasExtensions():
                self._checkExtension()
            elif self._hasJava():
                self._checkJava()
            elif self._hasDataBase():
                self._checkDataBase()
            elif self._model.hasPython():
                self._checkPython()
            else:
                self._setLastPage()
        elif self._step == 2:
            if self._hasJava():
                self._checkJava()
            elif self._hasDataBase():
                self._checkDataBase()
            elif self._model.hasPython():
                self._checkPython()
            else:
                self._setLastPage()
        elif self._step == 3:
            if self._hasDataBase():
                self._checkDataBase()
            elif self._model.hasPython():
                self._checkPython()
            else:
                self._setLastPage()
        elif self._step == 4:
            if self._model.hasPython():
                self._checkPython()
            else:
                self._setLastPage()
        elif self._step == 5:
            if self._setup:
                self._installPackages()
            else:
                self._setLastPage()
        elif self._step == 6:
            self._setLastPage()
        elif self._step == 7:
            self._running = True
            self._model.deregisterJob()
            self._running = False
            self._close()
        else:
            print("SetupManager.next() step: %s " + self._step)

    def setMaxProgress(self, value):
        self._view.setMaxProgress(value)

    def setProgress(self, text, progress):
        self._view.setProgress(text, progress)

    def _cancel(self):
        self._view.enableButtons(False)
        self._closing = True
        self._model.close()

    def _close(self):
        print("SetupManager._close() 1")
        self._model.savePosition(self._view.getWindowPosition())
        if self._dispatch:
            notifyDispatch(self._source, self._dispatch)
        self._view.close()
        print("SetupManager._close() 2")

    def _hasDataBase(self):
        return self._extension and self._model.hasDataBase()

    def _hasJava(self):
        return self._extension and self._model.hasJava()

    def _checkExtension(self):
        self._step = 2
        self._running = True
        self._enableNext(False)
        self._model.setCheckExtension(self.notifyExtension)
        self._setHeader(self._model.getHeader())
        self._model.startCheck(self._view.getIndicator())

    def notifyExtension(self, success):
        self._running = False
        if self._closing:
            self._close()
        else:
            self._setResults(*self._model.getResults(success))
            self._extension = success
            #self._enableNext(True)

    def _checkJava(self):
        self._step = 3
        self._running = True
        self._enableNext(False)
        self._model.setCheckJava(self.notifyJava)
        self._setHeader(self._model.getHeader())
        self._model.startCheck(self._view.getIndicator())

    def notifyJava(self, success):
        self._running = False
        if self._closing:
            self._close()
        else:
            self._setResults(*self._model.getResults(success))
            self._java = success
            #self._enableNext(True)

    def _checkDataBase(self):
        self._step = 4
        self._running = True
        self._enableNext(False)
        setup = self._model.getCheckDataBase(self.notifyDataBase, self._view.getIndicator())
        self._setHeader(self._model.getHeader())
        setup.start()

    def notifyDataBase(self, success):
        self._running = False
        if self._closing:
            self._close()
        else:
            self._setResults(*self._model.getResults(success))
            self._database = success
            #self._enableNext(True)

    def _checkPython(self):
        self._step = 5
        self._running = True
        self._enableNext(False)
        self._model.setCheckPython(self.notifyPython)
        self._setHeader(self._model.getHeader())
        self._model.startCheck(self._view.getIndicator())

    def notifyPython(self, success):
        self._running = False
        if self._closing:
            self._close()
        else:
            if success:
                self._python = True
            else:
                self._setup = True
            self._setResults(*self._model.getResults(success, False))
            #self._enableNext(True)

    def _installPackages(self):
        self._step = 6
        self._running = True
        self._enableNext(False)
        self._model.setCheckPypi(self.notifyPypi)
        self._setHeader(self._model.getHeader())
        self._model.startCheck(self._view.getIndicator())

    def notifyPypi(self, success):
        self._running = False
        if self._closing:
            self._close()
        else:
            self._setResults(*self._model.getResults(success))
            self._python = success
            #self._enableNext(True)

    def _setHeader(self, header):
        if not self._closing:
            self._view.setHeader(header)

    def _enableNext(self, enabled):
        if not self._closing:
            self._view.enableNext(enabled)

    def _setResults(self, *results):
        if not self._closing:
            self._view.setResults(*results)

    def _setLastPage(self):
        success = all((self._extension, self._java, self._python, self._database))
        self._setResults(*self._model.getLastPage(success, self._setup))
        if not success:
            pass
            #self._setErrorMessage(page)

