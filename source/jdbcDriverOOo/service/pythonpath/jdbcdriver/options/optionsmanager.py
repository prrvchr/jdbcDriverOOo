#!
# -*- coding: utf-8 -*-

"""
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
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

from com.sun.star.ui.dialogs.ExecutableDialogResults import OK

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from .optionsmodel import OptionsModel

from .optionsview import OptionsView

from .optionshandler import OptionsListener
from .optionshandler import TabListener
from .optionshandler import TabHandler

from ..option import OptionManager

from ..unotool import getFilePicker
from ..unotool import getSimpleFile
from ..unotool import getUrl

from ..dbconfig import g_jar

from threading import Condition
import traceback


class OptionsManager():
    def __init__(self, ctx, window):
        self._ctx = ctx
        self._lock = Condition()
        self._disposed = False
        self._disabled = False
        self._listener = TabListener(self)
        self._model = OptionsModel(ctx, self._lock)
        window.addEventListener(OptionsListener(self))
        self._view = OptionsView(ctx, window, TabHandler(self), self._listener, OptionsManager._restart, *self._model.getTabTitles())
        self._manager = OptionManager(ctx, self._view.getTab1(), 0, 'Driver')
        self._initView()

    _restart = False

    def dispose(self):
        self._manager.dispose()
        self._view.dispose()
        self._disposed = True

    # TODO: One shot disabler handler
    def isHandlerEnabled(self):
        if self._disabled:
            self._disabled = False
            return False
        return True

# OptionsManager setter methods
    def activateTab2(self):
        self._view.removeTabListener(self._listener)
        self._model.setDriverVersions(self._manager.getDriverService(), self.updateView)

    def updateView(self, versions):
        with self._lock:
            self.updateVersion(versions)

    def updateVersion(self, versions):
        with self._lock:
            if not self._disposed:
                protocol = self._view.getSelectedProtocol()
                if protocol in versions:
                    self._view.setVersion(versions[protocol])

    def saveSetting(self):
        saved = self._manager.saveSetting()
        if self._model.saveSetting() or saved:
            OptionsManager._restart = True
            self._view.setRestart(True)

    def loadSetting(self):
        self._manager.loadSetting()
        # XXX: We need to exit from Add new Driver mode if needed...
        self._view.exitAdd()
        self._initView()

    def setDriverService(self, driver):
        level = self._view.getApiLevel()
        self._view.setApiLevel(*self._model.setDriverService(driver, level))

    def setApiLevel(self, level):
        self._view.enableOptions(*self._model.setApiLevel(level))

    def setSystemTable(self, state):
        self._model.setSystemTable(state)

    def setBookmark(self, state):
        self._model.setBookmark(state)
        self._view.enableSQLMode(state)

    def setSQLMode(self, state):
        self._model.setSQLMode(state)

    def updateArchive(self):
        archive = self._updateArchive()
        if archive is not None:
            protocol = self._view.getSelectedProtocol()
            self._model.updateArchive(protocol, archive)
            self._initView(protocol)

    def searchArchive(self):
        archive = self._updateArchive()
        if archive is not None:
            self._view.setNewArchive(archive)

    def newDriver(self):
        # XXX: New button deselect any item in the driver's ListBox, as a result 
        # XXX: setDriver() will be called by the handler with an empty selection
        self._view.enableProtocols(False)

    def setDriver(self, protocol):
        # XXX: If selection is empty we are in Add driver mode
        if protocol:
            self._setDriver(protocol)
        else:
            self._addDriver()

    def removeDriver(self):
        protocol = self._view.getSelectedProtocol()
        if self._model.removeProtocol(protocol):
            self._initView()

    def saveDriver(self):
        subprotocol = self._view.getNewSubProtocol()
        name = self._view.getNewName()
        clazz = self._view.getNewClass()
        archive = self._view.getNewArchive()
        logger = self._view.getLogger()
        protocol = self._model.saveDriver(subprotocol, name, clazz, archive, logger)
        self._view.clearAdd()
        self._initView(protocol)

    def cancelDriver(self):
        self._view.enableProtocols(True)
        protocol = self._view.getSelectedProtocol()
        root = self._model.isNotRoot(protocol)
        self._view.disableAdd(root)

    def checkDriver(self):
        protocol = self._view.getNewSubProtocol()
        name = self._view.getNewName()
        clazz = self._view.getNewClass()
        archive = self._view.getNewArchive()
        enabled = self._model.isDriverValide(protocol, name, clazz, archive)
        self._view.enableSave(enabled)

    def setLogger(self, level):
        protocol = self._view.getSelectedProtocol()
        self._model.setLogger(protocol, level)

    def toggleLogger(self, enabled, state):
         self._view.enableLogger(enabled, state)
         if enabled and not state:
             self.setLogger(-1)

# OptionsManager private methods
    def _disableHandler(self):
        self._disabled = True

    def _initView(self, driver=None):
        self._disableHandler()
        self._view.setProtocols(self._model.getProtocols(), driver)

    def _updateArchive(self):
        fp = getFilePicker(self._ctx)
        fp.setDisplayDirectory(self._model.getPath())
        fp.appendFilter(g_jar, g_jar)
        fp.setCurrentFilter(g_jar)
        if fp.execute() == OK:
            url = getUrl(self._ctx, fp.getFiles()[0])
            getSimpleFile(self._ctx).copy(url.Main, self._model.getTarget(url))
            self._model.setPath(url)
            return url.Name
        return None

    def _setDriver(self, protocol):
        self._view.setVersion(self._model.getDriverVersion(protocol))
        self._view.setSubProtocol(self._model.getSubProtocol(protocol))
        self._view.setName(self._model.getDriverName(protocol))
        self._view.setClass(self._model.getDriverClass(protocol))
        self._view.setArchive(self._model.getDriverArchive(protocol))
        self._view.setLogger(self._model.getLogger(protocol))
        self._view.enableButton(self._model.isNotRoot(protocol))

    def _addDriver(self):
        self._view.enableAdd()
        self.checkDriver()

