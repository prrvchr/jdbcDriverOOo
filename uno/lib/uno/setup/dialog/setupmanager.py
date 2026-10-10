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

from ...configuration import State

from ...runner import Cancel

from ...unotool import Transferable

from ...unotool import getSystemClipboard
from ...unotool import notifyDispatch

import traceback


class SetupManager():
    def __init__(self, ctx, source, dispatch, name, /, database=None, java=None, agent=None, python=False, **extensions):
        self._ctx = ctx
        self._source = source
        self._dispatch = dispatch
        self._cancel = Cancel()
        self._running = False
        self._step = 1
        self._model = SetupModel(ctx, name, database, java, agent, python, extensions)
        self._listener = CloseListener(self)
        self._view = SetupView(ctx, WindowHandler(self), self._listener, name, *self._model.getViewData())

# XCloseListener
    def queryClosing(self, source, ownership):
        self._close()
        if self._running:
            raise CloseVetoException()
        if not ownership:
            self._dispose()

    def notifyClosing(self, source):
        source.removeCloseListener(self._listener)

    def copy(self):
        transferable = Transferable(self._ctx).getByString(self._view.getTraceBack())
        getSystemClipboard(self._ctx).setContents(transferable, None)

    def cancel(self):
        self._close()
        if not self._running:
            self._dispose()

    def next(self):
        if self._step == 1:
            if self._model.hasExtensions():
                self._checkExtension()
            elif self._model.hasJava():
                self._checkJava()
            elif self._model.hasDataBase():
                self._checkDataBase()
            elif self._model.hasPython():
                self._checkPython()
            else:
                self._setReport()
        elif self._step == 2:
            if self._model.hasJava():
                self._checkJava()
            elif self._model.hasDataBase():
                self._checkDataBase()
            elif self._model.hasPython():
                self._checkPython()
            else:
                self._setReport()
        elif self._step == 3:
            if self._model.hasDataBase():
                self._checkDataBase()
            elif self._model.hasPython():
                self._checkPython()
            else:
                self._setReport()
        elif self._step == 4:
            if self._model.hasPython():
                self._checkPython()
            else:
                self._setReport()
        elif self._step == 5:
            self._installPackages()
        elif self._step == 6:
            self._setReport()
        elif self._step == 7:
            self._running = True
            self._model.deregisterJob()
            self._running = False
            self._close()
            self._dispose()
        else:
            print("SetupManager.next() step: %s " + self._step)

    def setMaxProgress(self, value):
        self._view.setMaxProgress(value)

    def setProgress(self, text, progress):
        self._view.setProgress(text, progress)

    def _close(self):
        self._cancel.set()
        self._view.enableButtons(False)
        self._model.savePosition(self._view.getWindowPosition())

    def _dispose(self):
        if self._dispatch:
            notifyDispatch(self._source, self._dispatch)
        self._view.dispose()

    def _checkExtension(self):
        self._running = True
        self._model.setCheckExtension()
        self._setHeader()
        self._model.startCheck(self.notifyExtension, self._view.getIndicator(), self._cancel)

    def notifyExtension(self, success, error):
        self._running = False
        if self._cancel.isSet():
            self._dispose()
        elif error:
            self._setError(error)
        else:
            self._step = 2 if success else 4
            self._setResult(success)

    def _checkJava(self):
        self._running = True
        self._model.setCheckJava()
        self._setHeader()
        self._model.startCheck(self.notifyJava, self._view.getIndicator(), self._cancel)

    def notifyJava(self, success, error):
        self._running = False
        if self._cancel.isSet():
            self._dispose()
        elif error:
            self._setError(error)
        else:
            self._step = 3 if success else 4
            self._setResult(success)

    def _checkDataBase(self):
        self._running = True
        self._model.setCheckDataBase()
        self._setHeader()
        self._model.startCheck(self.notifyDataBase, self._view.getIndicator(), Cancel())

    def notifyDataBase(self, success, error=None):
        self._running = False
        if self._cancel.isSet():
            self._dispose()
        elif error:
            self._setError(error)
        else:
            self._step = 4
            self._setResult(success)

    def _checkPython(self):
        self._running = True
        self._model.setCheckPython()
        self._setHeader()
        self._model.startCheck(self.notifyPython, self._view.getIndicator(), self._cancel)

    def notifyPython(self, success, checked, error):
        self._running = False
        if self._cancel.isSet():
            self._dispose()
        elif error:
            self._setError(error)
        elif success:
            self._step = 6 if checked else 5
            self._setResult(checked)

    def _installPackages(self):
        self._step = 6
        self._running = True
        self._model.setCheckPypi()
        self._setHeader()
        self._model.startCheck(self.notifyPypi, self._view.getIndicator(), self._cancel)

    def notifyPypi(self, success, error):
        self._running = False
        if success:
            State.restart = True
        if self._cancel.isSet():
            self._dispose()
        elif error:
            self._setError(error)
        else:
            self._setResult(success)

    def _setHeader(self):
        if not self._cancel.isSet():
            self._view.setHeader(self._model.getHeader())

    def _setResult(self, success):
        if not self._cancel.isSet():
            self._view.setResult(*self._model.getResult(success))

    def _setError(self, error):
        if not self._cancel.isSet():
            self._view.setError(self._model.getError(error.cause), error.trace)

    def _setReport(self):
        if not self._cancel.isSet():
            success, *result = self._model.getReport()
            self._view.setResult(*result)
            if success:
                self._step = 7

