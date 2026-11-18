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

from com.sun.star.awt import Size

from ..unotool import Layout
from ..unotool import SubLayout


class OptionsView():
    def __init__(self, window, label, links, update):
        self._window = window
        self._getSetupLabel().Model.Label = label
        for i, link in enumerate(links, start=1):
            self._getLink(i).URL = link
        self._getSetupMode().Model.Enabled = update
        self._layout = SubLayout(Layout(window, size=Size(279, 225), **self._getLayouts(links)))
        window.addWindowListener(self._layout)

# OptionsView getter methods
    def getLayout(self):
        return self._layout

    def getFrame(self):
        return self._getFrame()

    def getSetupMode(self):
        return self._getSetupMode().getSelectedItemPos()

# OptionsView setter methods
    def refreshView(self, setup, state):
        self._window.Model.Step = state.step
        self._setSetup(setup)

    def setDriverVersion(self, version):
        self._getVersion().Text = version

# OptionsView private methods
    def _getLayouts(self, links):
        return {'horizontals': self._getHorizontals(links),
                'tops': self._getTops()}

    def _getHorizontals(self, links):
        yield self._getFrame()
        yield self._getSetupLabel()
        yield self._getSetupMode()
        for index in range(4, 16):
            yield self._getLabel(index)
        for index, _ in enumerate(links, start=1):
            yield self._getLink(index)

    def _getTops(self):
        yield self._getSetup()
        yield self._getVersion()

    def _setSetup(self, setup):
        control = self._getSetupMode()
        if control.Model.Enabled:
            control.selectItemPos(setup, True)

# OptionsView private control methods
    def _getFrame(self):
        return self._window.getControl('FrameControl1')

    def _getVersion(self):
        return self._window.getControl('Label2')

    def _getSetupLabel(self):
        return self._window.getControl('FixedLine1')

    def _getSetupMode(self):
        return self._window.getControl('ListBox1')

    def _getSetup(self):
        return self._window.getControl('CommandButton1')

    def _getLabel(self, index):
        return self._window.getControl(f'Label{index}')

    def _getLink(self, index):
        return self._window.getControl(f'Hyperlink{index}')
