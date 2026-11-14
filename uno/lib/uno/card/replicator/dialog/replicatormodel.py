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

from com.sun.star.awt import Rectangle

from ...unotool import getConfiguration
from ...unotool import getStringResource
from ...unotool import saveWindowPosSize

from ...configuration import g_identifier


class ReplicatorModel():
    def __init__(self, ctx):
        self._ctx = ctx
        self._indicator = None
        self._config = getConfiguration(ctx, g_identifier, True)
        self._resolver = getStringResource(ctx, g_identifier, 'dialogs', 'Replicator')
        self._resources = {'Title':  'Replicator.Title',
                           'Button': 'Replicator.CommandButton1.Label.%s',
                           'Label':  'Replicator.Label1.Label.%s'}

# ReplicatorModel getter methods
    def getViewData(self):
        return self._getDialogPosition(), self._getDialogTitle()

    def getLabelState(self, state):
        return self._getLabelState(state)

    def initState(self, state):
        return state, self._getToggleState(state), self._getLabelState(state)

    def getState(self, state):
        return state, self._getToggleState(state), self._getLabelState(state)

    def hasIndicator(self):
        return self._indicator is not None

    def getSpooler(self):
        return self._spooler

# ReplicatorModel setter methods
    def initReplicator(self):
        print("ReplicatorModel.initReplicator()")

    def startReplicator(self, indicator):
        self._indicator = indicator
        # self._spooler.start(indicator)

    def stopReplicator(self):
        # self._spooler.stop()
        pass

    def resetIndicator(self):
        self._indicator = None

    def addSpoolerIndicator(self, indicator):
        if self._spooler:
            self._indicator = indicator
            self._spooler.addStatusIndicator(indicator)

    def disposeSpooler(self, listener):
        if self._spooler:
            self._spooler.removeSpoolerListener(listener)
            if self._indicator:
                self._spooler.removeStatusIndicator(self._indicator)
            self._spooler.dispose()
        self._spooler = None
        self._indicator = None

    def savePosSize(self, possize):
        saveWindowPosSize(self._config, possize, 'ReplicatorPosSize')

# ReplicatorModel private getter methods
    def _getDialogPosition(self):
        return Rectangle(*self._config.getByName('ReplicatorPosSize'))

# ReplicatorModel private setter methods
    def _getDialogTitle(self):
        resource = self._resources.get('Title')
        return self._resolver.resolveString(resource)

    def _getLabelState(self, state):
        resource = self._resources.get('Label') % state
        return self._resolver.resolveString(resource)

    def _getToggleState(self, state):
        resource = self._resources.get('Button') % state
        return self._resolver.resolveString(resource)
