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

from .progress import Progress

from ...unotool import getContainerWindow
from ...unotool import getDesktop
from ...unotool import getTopWindow
from ...unotool import getWindowPosition
from ...unotool import setWindowPosition

from ...configuration import g_identifier

import traceback


class SetupView():
    def __init__(self, ctx, handler, listener, name, point, title, header):
        self._ctx = ctx
        self._frame = getTopWindow(ctx, name)
        peer = self._frame.getContainerWindow()
        self._window = getContainerWindow(ctx, peer, handler, g_identifier, 'SetupWindow')
        # XXX: setComponent is needed if we want a StatusIndicator at the bottom
        self._frame.setComponent(self._window, None)
        self._frame.addCloseListener(listener)
        setWindowPosition(ctx, self._frame, self._window, point, title)
        self._getPageHeader().Text = header

# SetupView getter methods
    def getWindowPosition(self):
        return getWindowPosition(self._frame.getContainerWindow())

    def getWindow(self):
        return self._window

    def getDialogWindow(self):
        return self._frame.getContainerWindow()

    def getStep(self):
        return self._window.Model.Step

    def getIndicator(self):
        return Progress(self._getProgressBar().Model, self._getProgressText())

# SetupView setter methods
    def dispose(self):
        self._frame.close(True)
 
    def dispose1(self):
        print("SetupView.dispose() 1")
        try:
            # Récupère le Desktop (utilisez votre méthode getDesktop(ctx) si disponible)
            desktop = getDesktop(self._ctx)
            frames = desktop.getFrames()
            
            # On vérifie si la frame est bien présente avant de la supprimer
            frames.remove(self._frame)
            print("Frame retirée avec succès de la collection du Desktop.")
            #self._frame.dispose()
            #self._window.dispose()
        except Exception as e:
            print(f"Erreur lors du retrait de la frame du Desktop : {e}")
        print("SetupView.dispose() 2")

    def setHeader(self, header):
        self._window.Model.Step = 2
        self._getPageHeader().Text = header

    def setResults(self, header, text='', enabled=True):
        self._window.Model.Step = 3
        self._getPageHeader().Text = header
        self._getResult().Text = text
        self._getNextButton().Model.Enabled = enabled

    def setMaxProgress(self, value):
        model = self._getProgressBar().Model
        model.ProgressValue = 0
        model.ProgressValueMax = value
        try:
            peer = self._window.getPeer()
            if peer:
                peer.paintImmediately()
        except Exception:
            pass

    def setProgress(self, text, progress):
        # FIXME: To ensure the label is correctly updated, the progress bar must be updated last.
        self._getProgressText().Text = text
        self._getProgressBar().Model.ProgressValue = progress
        try:
            peer = self._window.getPeer()
            if peer:
                peer.paintImmediately()
        except Exception:
            pass

    def enableButtons(self, enabled):
        self._getCancelButton().Model.Enabled = enabled
        self.enableNext(enabled)

    def enableNext(self, enabled):
        self._getNextButton().Model.Enabled = enabled

# SetupView private control methods
    def _getPageHeader(self):
        return self._window.getControl('Label1')

    def _getProgressBar(self):
        return self._window.getControl('ProgressBar1')

    def _getProgressText(self):
        return self._window.getControl('Label2')

    def _getResult(self):
        return self._window.getControl('Label3')

    def _getCancelButton(self):
        return self._window.getControl('CommandButton1')

    def _getNextButton(self):
        return self._window.getControl('CommandButton2')

