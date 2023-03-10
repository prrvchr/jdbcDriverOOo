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
from ..unotool import getStringResourceWithLocation

from ..configuration import g_identifier
from ..configuration import g_resource
from ..configuration import g_basename

import traceback


def getLogger(ctx, logger, basename=g_basename):
    return LogWrapper(ctx, logger, basename)

def getLoggerName(name):
    return '%s.%s' % (g_identifier, name)

def getPool(ctx):
    return createService(ctx, 'io.github.prrvchr.jdbcDriverOOo.LoggerPool')


# This logger wrapper allows using variable number of argument in python
# while the UNO API does not allow it
class LogWrapper(object):
    def __init__(self, ctx, name, basename):
        pool = getPool(ctx)
        self._init(ctx, pool, name, basename)

    def _init(self, ctx, pool, name, basename):
        self._ctx = ctx
        self._basename = basename
        self._url, self._logger = _getLogger(ctx, pool, name, basename)

    _debug = {}

    # XLogger
    @property
    def Name(self):
        return self._logger.Name

    @property
    def Level(self):
        return self._logger.Level
    @Level.setter
    def Level(self, value):
        self._logger.Level = value

    # Public getter method
    def isDebugMode(self):
        return LogWrapper._debug.get(self.Name, False)

    def addLogHandler(self, handler):
        self._logger.addLogHandler(handler)

    def removeLogHandler(selfself, handler):
        self._logger.removeLogHandler(handler)

    def isLoggable(self, level):
        return self._logger.isLoggable(level)

    def log(self, level, message):
        self._logger.log(level, message)

    def logp(self, level, clazz, method, message):
        self._logger.logp(level, clazz, method, message)

    def logrb(self, level, resource, *args):
        if self._logger.hasEntryForId(resource):
            self._logger.logrb(level, resource, args)
        else:
            self._logger.log(level, self._getErrorMessage(resource))

    def logprb(self, level, clazz, method, resource, *args):
        if self._logger.hasEntryForId(resource):
            self._logger.logprb(level, clazz, method, resource, args)
        else:
            self._logger.logp(level, clazz, method, self._getErrorMessage(resource))

    def resolveString(self, resource, *args):
        if self._logger.hasEntryForId(resource):
            return self._logger.resolveString(resource, args)
        else:
            return self._getErrorMessage(resource)

    def addModifyListener(self, listener):
        self._logger.addModifyListener(listener)

    def removeModifyListener(self, listener):
        self._logger.removeModifyListener(listener)

    def _getErrorMessage(self, resource):
        resolver = getStringResourceWithLocation(self._ctx, self._url, 'Logger')
        return resolver.resolveString(141) % (resource, self._url, self._basename)


# This logger controller allows using listener and access content of logger
class LogController(LogWrapper):
    def __init__(self, ctx, name, basename=g_basename, listener=None):
        pool = getPool(ctx)
        self._init(ctx, pool, name, basename)
        self._listener = listener
        self._resolver = getStringResourceWithLocation(ctx, self._url, 'Logger')
        self._debug = (True, 7, 'com.sun.star.logging.FileHandler')
        self._settings = None
        if listener is not None:
            self._logger.addModifyListener(listener)

    def getLogContent(self):
        url = self._getLoggerUrl()
        length, sequence = getFileSequence(self._ctx, url)
        text = sequence.value.decode('utf-8')
        return text, length

# Public setter method
    def dispose(self):
        if self._listener is not None:
            self._logger.removeModifyListener(self._listener)

    def logInfos(self, level, infos, clazz, method):
        for resource, info in infos.items():
            msg = self._resolver.resolveString(resource) % info
            self._logger.logp(level, msg, clazz, method)

    def clearLogger(self):
        name = self._logger.Name
        self._logger = None
        pool = createService(self._ctx, 'io.github.prrvchr.jdbcDriverOOo.LoggerPool')
        self._logger = pool.getLocalizedLogger(name, self._url, self._basename)
        msg = self._resolver.resolveString(131)
        self._logMessage(SEVERE, msg, 'Logger', 'clearLogger')

    def setDebugMode(self, mode):
        if mode:
            self._setDebugModeOn()
        else:
            self._setDebugModeOff()

    def addListener(self, listener):
        self._logger.addModifyListener(listener)

    def removeListener(self, listener):
        self._logger.removeModifyListener(listener)

# Private getter method
    def _getErrorMessage(self, resource):
        return self._resolver.resolveString(141) % (resource, self._url, self._basename)

    def _getLoggerUrl(self):
        url = '$(userurl)/$(loggername).log'
        settings = self._getLogConfig().getByName('HandlerSettings')
        if settings.hasByName('FileURL'):
            url = settings.getByName('FileURL')
        service = 'com.sun.star.util.PathSubstitution'
        path = createService(self._ctx, service)
        url = url.replace('$(loggername)', self._logger.Name)
        return path.substituteVariables(url, True)

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
        LogWrapper._debug[self.Name] = True

    def _setDebugModeOff(self):
        if self._settings is not None:
            self._setLoggerSetting(*self._settings)
            self._settings = None
        LogWrapper._debug[self.Name] = False

    def _logMessage(self, level, msg, clazz=None, method=None):
        if clazz is None or method is None:
            self._logger.log(level, msg)
        else:
            self._logger.logp(level, clazz, method, msg)


def _getLogger(ctx, pool, name, basename):
    url = getResourceLocation(ctx, g_identifier, g_resource)
    logger = pool.getLocalizedLogger(getLoggerName(name), url, basename)
    return url, logger

