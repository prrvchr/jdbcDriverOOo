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

import unohelper

from com.sun.star.logging.LogLevel import SEVERE
from com.sun.star.logging.LogLevel import WARNING
from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import CONFIG
from com.sun.star.logging.LogLevel import FINE
from com.sun.star.logging.LogLevel import FINER
from com.sun.star.logging.LogLevel import FINEST
from com.sun.star.logging.LogLevel import ALL
from com.sun.star.logging.LogLevel import OFF

from ..unotool import createService
from ..unotool import getConfiguration
from ..unotool import getFileSequence
from ..unotool import getResourceLocation
from ..unotool import getStringResource

from ..configuration import g_identifier
from ..configuration import g_resource
from ..configuration import g_basename


class LogModel():
    def __init__(self, ctx, default):
        self._ctx = ctx
        pool = createService(ctx, 'io.github.prrvchr.jdbcDriverOOo.LoggerPool')
        self._resolver = getStringResource(ctx, g_identifier, g_resource, 'Logger')
        logger = '%s.%s' % (g_identifier, default)
        self._logger = self._getLogger(pool, logger)
        self._debug = (True, 7, 'com.sun.star.logging.FileHandler')
        self._settings = None

    _debug = False

    # Public getter method
    def isDebugMode(self):
        return LogModel._debug

    def isLoggerEnabled(self):
        level = self._getLogConfig().LogLevel
        enabled = self._isLogEnabled(level)
        return enabled

    def getLoggerSetting(self):
        enabled, index, handler = self._getLoggerSetting()
        state = self._getState(handler)
        return enabled, index, state

    def getLoggerUrl(self):
        url = '$(userurl)/$(loggername).log'
        settings = self._getLogConfig().getByName('HandlerSettings')
        if settings.hasByName('FileURL'):
            url = settings.getByName('FileURL')
        service = 'com.sun.star.util.PathSubstitution'
        path = createService(self._ctx, service)
        url = url.replace('$(loggername)', self._logger.Name)
        return path.substituteVariables(url, True)

    def getLogContent(self):
        url = self.getLoggerUrl()
        length, sequence = getFileSequence(self._ctx, url)
        text = sequence.value.decode('utf-8')
        return text, length

    def getLoggerData(self):
        url = self.getLoggerUrl()
        length, sequence = getFileSequence(self._ctx, url)
        text = sequence.value.decode('utf-8')
        return url, text, length

# Public setter method
    def logInfos(self, level, infos, clazz, method):
        for resource, info in infos.items():
            msg = self._resolver.resolveString(resource) % info
            self._logger.logp(level, msg, clazz, method)

    def dispose(self):
        pass

    def clearLogger(self):
        name = self._logger.Name
        self._logger = None
        self._logger = self._getLogger(name)
        msg = self._resolver.resolveString(131)
        self._logMessage(SEVERE, msg, 'Logger', 'clearLogger')

    def addLogHandler(self, handler):
        self._logger.addLogHandler(handler)

    def removeLogHandler(self, handler):
        self._logger.removeLogHandler(handler)

    def setDebugMode(self, mode):
        if mode:
            self._setDebugModeOn()
        else:
            self._setDebugModeOff()

    def setLoggerSetting(self, enabled, index, state):
        handler = self._getHandler(state)
        self._setLoggerSetting(enabled, index, handler)

    def addListener(self, listener):
        self._logger.addModifyListener(listener)

    def removeListener(self, listener):
        self._logger.removeModifyListener(listener)

# Private getter method
    def _getLogger(self, pool, name):
        url = getResourceLocation(self._ctx, g_identifier, g_resource)
        return pool.getLocalizedLogger(name, url, g_basename)

    def _getLoggerSetting(self):
        configuration = self._getLogConfig()
        enabled, index = self._getLogIndex(configuration)
        handler = configuration.DefaultHandler
        return enabled, index, handler

    def _getLogConfig(self):
        name = self._logger.Name
        nodepath = '/org.openoffice.Office.Logging/Settings'
        configuration = getConfiguration(self._ctx, nodepath, True)
        if not configuration.hasByName(name):
            configuration.insertByName(name, configuration.createInstance())
            configuration.commitChanges()
        nodepath += '/%s' % name
        return getConfiguration(self._ctx, nodepath, True)

    def _getLogIndex(self, configuration):
        level = configuration.LogLevel
        enabled = self._isLogEnabled(level)
        if enabled:
            index = self._getLogLevels().index(level)
        else:
            index = 7
        return enabled, index
    
    def _getLogLevels(self):
        levels = (SEVERE,
                  WARNING,
                  INFO,
                  CONFIG,
                  FINE,
                  FINER,
                  FINEST,
                  ALL)
        return levels

    def _isLogEnabled(self, level):
        return level != OFF

    def _getHandler(self, state):
        handlers = {True: 'ConsoleHandler', False: 'FileHandler'}
        return 'com.sun.star.logging.%s' % handlers.get(state)

    def _getState(self, handler):
        states = {'com.sun.star.logging.ConsoleHandler': 1,
                  'com.sun.star.logging.FileHandler': 2}
        return states.get(handler)

# Private setter method
    def _setLoggerSetting(self, enabled, index, handler):
        configuration = self._getLogConfig()
        self._setLogIndex(configuration, enabled, index)
        self._setLogHandler(configuration, handler, index)
        if configuration.hasPendingChanges():
            configuration.commitChanges()

    def _setLogIndex(self, configuration, enabled, index):
        level = self._getLogLevels()[index] if enabled else OFF
        if configuration.LogLevel != level:
            configuration.LogLevel = level

    def _setLogHandler(self, configuration, handler, index):
        if configuration.DefaultHandler != handler:
            configuration.DefaultHandler = handler
        settings = configuration.getByName('HandlerSettings')
        if settings.hasByName('Threshold'):
            if settings.getByName('Threshold') != index:
                settings.replaceByName('Threshold', index)
        else:
            settings.insertByName('Threshold', index)

    def _setDebugModeOn(self):
        if self._settings is None:
            self._settings = self._getLoggerSetting()
        self._setLoggerSetting(*self._debug)
        LogModel._debug = True

    def _setDebugModeOff(self):
        if self._settings is not None:
            self._setLoggerSetting(*self._settings)
            self._settings = None
        LogModel._debug = False

    def _logMessage(self, level, msg, clazz=None, method=None):
        if clazz is None or method is None:
            self._logger.log(level, msg)
        else:
            self._logger.logp(level, clazz, method, msg)


class LoggerModel(LogModel):
    def __init__(self, ctx, default, listener):
        self._ctx = ctx
        self._pool = createService(ctx, 'io.github.prrvchr.jdbcDriverOOo.LoggerPool')
        self._pool.addModifyListener(listener)
        self._listener = listener
        self._default = default
        self._resolver = getStringResource(ctx, g_identifier, g_resource, 'Logger')
        self._logger = None
        self._settings = None

# Public getter method
    def getLoggerNames(self, filter=None):
        names = []
        if filter is None:
            names = self._pool.getLoggerNames()
        else:
            names = self._pool.getFilteredLoggerNames(filter)
        if self._default not in names:
            names = list(names)
            names.insert(0, self._default)
        return names

# Public setter method
    def dispose(self):
        self._pool.removeModifyListener(self._listener)

    def setLogger(self, name):
        url = getResourceLocation(self._ctx, g_identifier, g_resource)
        self._logger =  self._pool.getLocalizedLogger(name, url, g_basename)

