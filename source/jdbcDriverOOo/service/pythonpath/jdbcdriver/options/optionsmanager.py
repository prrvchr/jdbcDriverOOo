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

from com.sun.star.ui.dialogs.ExecutableDialogResults import OK

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from .optionsmodel import OptionsModel
from .optionsview import OptionsView
from .optionshandler import OptionsHandler
from .optionshandler import Tab1Handler
from .optionshandler import Tab2Handler

from ..unotool import getFilePicker
from ..unotool import getSimpleFile
from ..unotool import getUrl

from ..logger import LogManager

from ..dbconfig import g_jar

from ..configuration import g_extension

import os
import sys
import traceback
from threading import Condition


class OptionsManager(unohelper.Base):
    def __init__(self, ctx):
        self._ctx = ctx
        self._lock = Condition()
        self._view = None
        self._logger = None
        self._disposed = False
        self._disabled = False
        self._model = OptionsModel(ctx, self._lock)

    def dispose(self):
        with self._lock:
            self._disposed = True

    # TODO: One shot disabler handler
    def isHandlerEnabled(self):
        if self._disabled:
            self._disabled = False
            return False
        return True

# OptionsManager setter methods
    def updateView(self, loggers, versions):
        with self._lock:
            self.updateLogger(loggers)
            self.updateVersion(versions)

    def updateLogger(self, loggers):
        if not self._disposed:
            self._logger.updateLoggers(loggers)

    def updateVersion(self, versions):
        if not self._disposed:
            protocol = self._view.getSelectedProtocol()
            if protocol in versions:
                self._view.setVersion(versions[protocol])

    def initialize(self, window):
        print("OptionsManager.() 1")
        window.addEventListener(OptionsHandler(self))
        rectangle = uno.createUnoStruct('com.sun.star.awt.Rectangle', 0, 0, 260, 180)
        title1, title2, title3, rebbot = self._model.getTabData()
        tab, tab1, tab2 = self._getTabPages(window, 'Tab1', rectangle, title1, title2, title3)
        version  = ' '.join(sys.version.split())
        path = os.pathsep.join(sys.path)
        loggers = self._model.getLoggerNames('Driver')
        infos = {111: version, 112: path}
        self._logger = LogManager(self._ctx, tab.getPeer(), loggers, infos)
        self._view = OptionsView(self._ctx, window, tab1.getPeer(), Tab1Handler(self), tab2.getPeer(), Tab2Handler(self), rebbot)
        self._model.loadConfiguration(self.updateView, 'Driver')
        self._initView()
        print("OptionsManager.() 2")

    def saveSetting(self):
        self._logger.saveLoggerSetting()
        if self._model.saveSetting() and self._model.isLevelUpdated():
            self._view.disableLevel()

    def reloadSetting(self):
        # XXX: We need to exit from Add new Driver mode if needed...
        reboot = self._model.needReboot()
        self._view.exitAdd(reboot)
        self._model.loadConfiguration(self.updateView, 'Driver')
        self._initView()
        self._logger.setLoggerSetting()

    def setLevel(self, level):
        self._model.setLevel(level)

    def updateArchive(self):
        archive = self._updateArchive()
        if archive is not None:
            protocol = self._view.getSelectedProtocol()
            self._model.updateArchive(protocol, archive)
            self._initViewProtocol(protocol)

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
            self._initViewProtocol()

    def saveDriver(self):
        subprotocol = self._view.getNewSubProtocol()
        name = self._view.getNewName()
        clazz = self._view.getNewClass()
        archive = self._view.getNewArchive()
        logger = self._view.getLogger()
        protocol = self._model.saveDriver(subprotocol, name, clazz, archive, logger)
        reboot = self._model.needReboot()
        self._view.clearAdd(reboot)
        self._initViewProtocol(protocol)

    def cancelDriver(self):
        self._view.enableProtocols(True)
        protocol = self._view.getSelectedProtocol()
        root = self._model.isNotRoot(protocol)
        reboot = self._model.needReboot()
        self._view.disableAdd(root, reboot)

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

    def _initView(self):
        self._view.setLevel(*self._model.getLevel())
        self._initViewProtocol()

    def _initViewProtocol(self, driver=None):
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
        reboot = self._model.needReboot()
        self._view.enableAdd(reboot)
        self.checkDriver()

    def _getTabPages(self, window, name, rectangle, title1, title2, title3, i=1):
        model = self._getTabModel(window, rectangle)
        window.Model.insertByName(name, model)
        tab = window.getControl(name)
        tab1 = self._getTabPage(model, tab, title1)
        tab2 = self._getTabPage(model, tab, title2)
        tab3 = self._getTabPage(model, tab, title3)
        tab.ActiveTabPageID = i
        return tab1, tab2, tab3

    def _getTabModel(self, window, rectangle):
        service = 'com.sun.star.awt.tab.UnoControlTabPageContainerModel'
        model = window.Model.createInstance(service)
        model.PositionX = rectangle.X
        model.PositionY = rectangle.Y
        model.Width = rectangle.Width
        model.Height = rectangle.Height
        return model

    def _getTabPage(self, model, tab, title):
        index = model.getCount()
        page = model.createTabPage(index +1)
        page.Title = title
        model.insertByIndex(index, page)
        return tab.getControls()[index]

