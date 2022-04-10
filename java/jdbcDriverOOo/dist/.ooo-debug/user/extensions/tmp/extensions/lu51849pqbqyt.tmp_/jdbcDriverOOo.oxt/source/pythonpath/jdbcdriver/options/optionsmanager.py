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
    def _disableHandler(self):
        self._disabled = True

    def initialize(self, window):
        self._view = OptionsView(window)
        version  = ' '.join(sys.version.split())
        path = os.pathsep.join(sys.path)
        loggers = ('Driver', )
        infos = {111: version, 112: path}
        self._logger = LogManager(self._ctx, window.Peer, g_extension, loggers, infos)
        self._initView()

    def saveSetting(self):
        self._logger.saveLoggerSetting()
        if self._model.saveSetting() and self._model.isLevelUpdated():
            self._view.disableLevel()

    def reloadSetting(self):
        self._initView()
        self._logger.setLoggerSetting()

    def _initView(self):
        self._view.setLevel(*self._model.getLevel())
        self._disableHandler()
        self._view.setProtocols(self._model.getProtocols())

    def setLevel(self, level):
        self._model.setLevel(level)

    def upload(self):
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
            self._model.setUpdated()
            self._loadVersion()

    def setProtocol(self, protocol):
        self._view.setSubProtocol(self._model.getSubProtocol(protocol))
        self._view.setDriverName(self._model.getDriverName(protocol))
        self._view.setDriverClass(self._model.getDriverClass(protocol))
        self._view.setDriverArchive(self._model.getDriverArchive(protocol))
        print("OptionsManager.setProtocol() 1")

    def changeProtocol(self):
   	    print("OptionsManager.changeProtocol() 1")

    def _loadVersion(self):
        version = self._model.getVersion()
        self._view.setVersion(version)
