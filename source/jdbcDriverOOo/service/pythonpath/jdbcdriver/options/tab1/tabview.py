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

from ...unotool.layout import Layout

from ...unotool.sublayout import SubLayout

from ...unotool import getContainerWindow

from ...configuration import g_identifier


class TabWindow():
    def __init__(self, ctx, window, handler):
        self._window = getContainerWindow(ctx, window.getPeer(), handler, g_identifier, 'Option1Dialog')
        self._layout = SubLayout(Layout(self._window, **self._getLayouts()))
        self._window.setVisible(True)

# TabWindow getter methods
    def getLayout(self):
        return self._layout

    def getFrame(self):
        return self._getFrame()

    def getSetup(self):
        return self._getSetup()

# TabWindow setter methods
    def dispose(self):
        if self._layout:
            self._layout.dispose()
            self._layout = None
        if self._window:
            self._window.dispose()
            self._window = None

    def initView(self, log, cls, state):
        self._setControl(self._getJavaLogger(), log, state.java)
        self._setControl(self._getClassPath(), cls, state.java)

# TabWindow private getter methods
    def _getLayouts(self):
        return {'horizontals': self._getHorizontals(),
                'tops': self._getTops()}

    def _getHorizontals(self):
        yield self._getFrame()
        yield self._getSetup()

    def _getTops(self):
        yield self._getClassPath()

    def _setControl(self, control, state, java):
        control.State = int(state and java)
        control.Model.Enabled = java

# TabWindow private control methods
    def _getFrame(self):
        return self._window.getControl('FrameControl1')

    def _getSetup(self):
        return self._window.getControl('FrameControl2')

    def _getJavaLogger(self):
        return self._window.getControl('CheckBox1')

    def _getClassPath(self):
        return self._window.getControl('CheckBox2')
