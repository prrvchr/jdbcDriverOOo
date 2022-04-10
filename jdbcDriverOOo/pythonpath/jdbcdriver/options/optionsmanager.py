#!
# -*- coding: utf_8 -*-

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

import unohelper

from com.sun.star.ui.dialogs.ExecutableDialogResults import OK

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from .optionsmodel import OptionsModel
from .optionsview import OptionsView

from ..unotool import getDesktop
from ..unotool import getFilePicker
from ..unotool import getPathSettings
from ..unotool import getResourceLocation
from ..unotool import getSimpleFile
from ..unotool import getUrl

from ..logger import LogManager

from ..dbconfig import g_folder
from ..dbconfig import g_jar

from ..configuration import g_extension
from ..configuration import g_identifier

import os
import sys
import traceback


class OptionsManager(unohelper.Base):
    def __init__(self, ctx):
        self._ctx = ctx
        self._model = OptionsModel(ctx)
        self._view = None
        self._logger = None
        self._disabled = False

    # TODO: One shot disabler handler
    def isHandlerEnabled(self):
        if self._disabled:
            self._disabled = False
            return False
        return True

# OptionsManager setter methods
    def initialize(self, window):
        reboot = self._model.needReboot()
        self._view = OptionsView(window, reboot)
        version  = ' '.join(sys.version.split())
        path = os.pathsep.join(sys.path)
        loggers = ('Driver', )
        infos = {111: version, 112: path}
        self._logger = LogManager(self._ctx, window.Peer, g_extension, loggers, infos)
        self._initView()

    def saveSetting(self):
        self._logger.saveLoggerSetting()
        if self._model.saveSetting():
            #self._view.setReboot(True)
            if self._model.isLevelUpdated():
                self._view.disableLevel()

    def reloadSetting(self):
        # XXX: We need to exit from Add new Driver mode if needed...
        reboot = self._model.needReboot()
        self._view.exitAdd(reboot)
        self._model.loadConfiguration()
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
        protocol = self._model.saveDriver(subprotocol, name, clazz, archive)
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
        print("OptionsManager.checkDriver() '%s'" % enabled)
        self._view.enableSave(enabled)

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
        path = getPathSettings(self._ctx).Work
        fp = getFilePicker(self._ctx)
        fp.setDisplayDirectory(path)
        fp.appendFilter(g_jar, g_jar)
        fp.setCurrentFilter(g_jar)
        if fp.execute() == OK:
            url = getUrl(self._ctx, fp.getFiles()[0])
            location = '%s/%s' % (g_folder, url.Name)
            target = getResourceLocation(self._ctx, g_identifier, location)
            getSimpleFile(self._ctx).copy(url.Main, target)
            return url.Name
        return None

    def _setDriver(self, protocol):
        self._view.setVersion(self._model.getDriverVersion(protocol))
        self._view.setSubProtocol(self._model.getSubProtocol(protocol))
        self._view.setName(self._model.getDriverName(protocol))
        self._view.setClass(self._model.getDriverClass(protocol))
        self._view.setArchive(self._model.getDriverArchive(protocol))
        self._view.enableButton(self._model.isNotRoot(protocol))

    def _addDriver(self):
        reboot = self._model.needReboot()
        self._view.enableAdd(reboot)
        self.checkDriver()

