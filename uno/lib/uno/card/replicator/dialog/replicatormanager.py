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

from .replicatormodel import ReplicatorModel

from .replicatorview import ReplicatorView

from .replicatorhandler import CloseListener
from .replicatorhandler import WindowHandler

from ..replicator import Replicator

from ...dbsetup import DBSetup

from ...runner import Cancel
from ...runner import Runner

# from ...setup import SetupCheck
from ...setup import SetupManager

# from ...dbinit import getDataSourceUrl

from ...helper import getExtensions

from ...logger import getLogger

from ...jdbcdriver import g_java
from ...jdbcdriver import g_script

from ...configuration import g_extension
from ...configuration import g_setup
from ...configuration import g_synclog


class ReplicatorManager():
    def __init__(self, ctx, queue):
        print("ReplicatorManager.__init__() 1")
        self._ctx = ctx
        self._model = ReplicatorModel(ctx)
        self._closing = False
        self._cancel = None
        self._closelistener = CloseListener(self)
        self._view = ReplicatorView(ctx, WindowHandler(self), self._closelistener, *self._model.getViewData())
        self._logger = getLogger(ctx, g_synclog)
        self._java = g_extension, g_java, g_script
        self._extensions = getExtensions()
        print("ReplicatorManager.__init__() 2")
        # self._check = SetupCheck(ctx, self.notify, database=getDataSourceUrl(ctx), java=self._java, python=True, extensions=self._extensions, ctx=ctx, queue=queue)
        # self._check.start()

    def notify(self, state, ctx, queue):
        if not state.runnable:
            database = DBSetup(self._ctx)
            parent = self._view.getWindow()
            setup = SetupManager(self._ctx, None, database=database, java=self._java, python=True, extensions=self._extensions, parent=parent, name=g_setup, state=state)
            if not setup.isRunnable():
                setup.dispose()
                self._closing = True
                self._close()
                return
            setup.dispose()
        if not self._closing:
            # self._startReplicator(ctx, queue)
            self._model.initReplicator()
            self._view.setToggleState(*self._model.initToggleState(0))
            self._view.enableButtons(True)

# XCloseListener
    def queryClosing(self, _source, ownership):
        print("ReplicatorManager.queryClosing() ownership: %s" % ownership)
        self._closing = True
        self._dispose()

    def notifyClosing(self, source):
        print("ReplicatorManager.notifyClosing() *******************************************")
        source.removeCloseListener(self._closelistener)
        self._view.dispose()

    def stopReplicator(self):
        if self._closing:
            return
        if self._cancel is not None:
            self._view.setLabelState(self._model.getLabelState(2))
            self._cancel.set()
        self._view.enableStopButton(False)

    def closeReplicator(self):
        self._close()

    def _startReplicator(self, ctx, queue):
        if self._closing or self._cancel:
            return
        self._view.setLabelState(self._model.getLabelState(2))
        self._cancel = Cancel()
        indicator = self._view.getIndicator()
        logger = getLogger(ctx, g_synclog)
        task = Replicator(ctx, queue)
        runner = Runner(ctx, task, self._cancel, indicator, logger)
        runner.start()
        self._view.setState(*self._model.getState(1))
        self._view.enableStopButton(True)

    def _close(self):
        self._closing = True
        self._view.close()

    def _dispose(self):
        self._view.enableButtons(False)
        self._model.savePosSize(self._view.getWindowPosSize())
        self._model.disposeSpooler(self._spoolerlistener)
        # getCallBack(self._ctx).addCallback(CallBack(self._view.close), None)
