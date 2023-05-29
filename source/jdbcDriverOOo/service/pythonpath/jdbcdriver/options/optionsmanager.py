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
from .optionshandler import OptionsListener
from .optionshandler import Tab1Handler
from .optionshandler import Tab2Handler

from ..unotool import createService
from ..unotool import getFilePicker
from ..unotool import getSimpleFile
from ..unotool import getUrl

from ..logger import LogManager

from ..dbconfig import g_jar

from ..configuration import g_identifier

import os
import sys
from threading import Condition
from collections import OrderedDict
import traceback


class OptionsManager(unohelper.Base):
    def __init__(self, ctx, window):
        self._ctx = ctx
        self._lock = Condition()
        self._disposed = False
        self._disabled = False
        self._model = OptionsModel(ctx, self._lock)
        window.addEventListener(OptionsListener(self))
        self._view = OptionsView(ctx, window, Tab1Handler(self), Tab2Handler(self), *self._model.getTabData())
        self._logger = LogManager(ctx, self._view.getLoggerParent(), self._getInfos(), g_identifier, 'Driver')
        self._model.loadConfiguration(self.updateView)
        self._initView()

    def dispose(self):
        self._logger.dispose()
        self._disposed = True

    # TODO: One shot disabler handler
    def isHandlerEnabled(self):
        if self._disabled:
            self._disabled = False
            return False
        return True

# OptionsManager setter methods
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
        self._logger.saveSetting()
        if self._model.saveSetting() and self._model.isUpdated():
            self._view.disableDriverLevel()

    def loadSetting(self):
        self._logger.loadSetting()
        # XXX: We need to exit from Add new Driver mode if needed...
        reboot = self._model.needReboot()
        self._view.exitAdd(reboot)
        self._model.loadConfiguration(self.updateView)
        self._initView()

    def setDriverService(self, driver):
        self._view.setConnectionLevel(*self._model.setDriverService(driver))

    def setConnectionService(self, level):
        self._model.setConnectionService(level)

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
        print("OptionsManager._initView() 1")
        driver, connection, upadated, enabled = self._model.getServicesLevel()
        self._view.setDriverLevel(driver, upadated)
        self._view.setConnectionLevel(connection, enabled)
        self._initViewProtocol()
        print("OptionsManager._initView() 2")

    def _getInfos(self):
        infos = OrderedDict()
        version  = ' '.join(sys.version.split())
        infos[111] = version
        path = os.pathsep.join(sys.path)
        infos[112] = path
        # FIXME: Need to known if ssl is installed
        try:
            import ssl
        except Exception as e:
            infos[125] = self._getExceptionMsg(e)
        else:
            infos[126] = ssl.OPENSSL_VERSION, ssl.__file__
        return infos

    def _getExceptionMsg(self, e):
        error = repr(e)
        trace = repr(traceback.format_exc())
        return error, trace

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

