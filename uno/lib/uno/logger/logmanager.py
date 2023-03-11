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

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from .logmodel import LogModel
from .logview import LogWindow
from .logview import LogDialog
from .loghandler import WindowHandler
from .loghandler import DialogHandler
from .loghandler import PoolListener
from .loghandler import LoggerListener

from ..unotool import getDialog
from ..unotool import getFileSequence

from .loghelper import getLoggerName

from ..configuration import g_extension
from ..configuration import g_identifier

import traceback


class LogManager(unohelper.Base):
    def __init__(self, ctx, parent, infos, filter, default):
        self._ctx = ctx
        self._model = LogModel(ctx, default, PoolListener(self))
        self._view = LogWindow(ctx, WindowHandler(self), parent)
        self._infos = infos
        self._filter = filter
        self._dialog = None
        self._view.initLogger(self._model.getLoggerNames(filter))

# LogManager setter methods
    def dispose(self):
        self._model.dispose()

    def updateLoggers(self):
        logger = self._view.getLogger()
        loggers = self._model.getLoggerNames(self._filter)
        self._view.updateLoggers(loggers)
        if logger in loggers:
            self._view.setLogger(logger)

    def saveSetting(self):
        settings = self._view.getLoggerSetting()
        self._model.setLoggerSetting(*settings)

    def loadSetting(self):
        settings = self._model.getLoggerSetting()
        self._view.setLoggerSetting(*settings)

    def changeLogger(self, name):
        logger = name if self._filter is None else getLoggerName(name)
        self._model.setLogger(logger)
        self.reloadSetting()

    def toggleLogger(self, enabled):
        self._view.toggleLogger(enabled)

    def toggleViewer(self, enabled):
        self._view.toggleViewer(enabled)

    def viewLog(self):
        handler = DialogHandler(self)
        parent = self._view.getParent()
        writable = True
        data = self._model.getLoggerData()
        self._dialog = LogDialog(self._ctx, handler, parent, g_extension, True, *data)
        listener = LoggerListener(self)
        self._model.addListener(listener)
        dialog = self._dialog.getDialog()
        dialog.execute()
        dialog.dispose()
        self._model.removeListener(listener)
        self._dialog = None

    def logInfos(self):
        self._model.logInfos(INFO, self._infos, 'LogManager', 'logInfos()')

    def updateLogger(self):
        self._dialog.updateLogger(*self._model.getLogContent())

