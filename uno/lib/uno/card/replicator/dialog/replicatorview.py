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

from ...unotool import Layout
from ...unotool import Indicator

from ...unotool import getContainerWindow
from ...unotool import getDesktop
from ...unotool import getTopWindow
from ...unotool import getWindowPosSize
from ...unotool import setWindowPosSize

from ...configuration import g_replicator
from ...configuration import g_identifier
from ...configuration import g_offset


class ReplicatorView():
    def __init__(self, ctx, handler, listener, point, title):
        self._ctx = ctx
        self._frame = getTopWindow(ctx, g_replicator)
        window = self._frame.getContainerWindow()
        self._window = getContainerWindow(ctx, window, handler, g_identifier, 'Replicator')
        # XXX: setComponent is needed if we want a StatusIndicator at the bottom
        self._frame.setComponent(self._window, None)
        self._frame.addCloseListener(listener)
        setWindowPosSize(ctx, window, self._window, point)
        bottoms = (self._getCloseButton(), )
        self._window.addWindowListener(Layout(self._window, bottoms=bottoms))
        self._indicator = Indicator(frame=self._frame, offset=g_offset)
        self._frame.setTitle(title)
        self._window.setVisible(True)
        window.setVisible(True)

# ReplicatorView getter methods
    def getWindowPosSize(self):
        return getWindowPosSize(self._frame.getContainerWindow())

    def getWindow(self):
        return self._frame.getContainerWindow()

    def getIndicator(self):
        return self._indicator.createIndicator()

# ReplicatorView setter methods
    def resetIndicator(self):
        self._indicator.resetIndicator()

    def setVisible(self, enabled):
        self._frame.getContainerWindow().setVisible(enabled)
        self._window.setVisible(enabled)

    def close(self):
        self._frame.close(True)

    def dispose(self):
        if self._frame:
            self._frame.setComponent(None, None)
            self._frame.getContainerWindow().dispose()
            getDesktop(self._ctx).getFrames().remove(self._frame)
            self._frame = None

    def enableButtons(self, enabled):
        self._getStopButton().Model.Enabled = enabled
        self._getCloseButton().Model.Enabled = enabled

    def enableStopButton(self, enabled):
        self._getStopButton().Model.Enabled = enabled

    def setLabelState(self, text):
        self._getLabelState().Text = text

    def setState(self, state, label, text):
        control = self._getStopButton().Model
        control.State = state
        control.Label = label
        self._getLabelState().Text = text

# ReplicatorView private control methods
    def _getLabelState(self):
        return self._window.getControl('Label1')

    def _getStopButton(self):
        return self._window.getControl('CommandButton1')

    def _getCloseButton(self):
        return self._window.getControl('CommandButton2')
