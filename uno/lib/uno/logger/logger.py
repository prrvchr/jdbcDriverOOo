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
from ..unotool import getStringResource

from ..configuration import g_identifier
from ..configuration import g_resource


class Logger(unohelper.Base):
    def __init__(self, ctx, name='Logger', resource=None):
        self._ctx = ctx
        self._name = '%s.%s' % (g_identifier, name)
        self._resolver = self._getStringResource(resource)

    _loggerpool = {}
    _logsetting = {}

# Public getter method
    def isDebugMode(self):
        return self._name in Logger._logsetting

    def isLoggerEnabled(self):
        level = self._getLogConfig().LogLevel
        enabled = self._isLogEnabled(level)
        return enabled

    def getMessage(self, resource, format=None):
        if self._resolver is not None:
            msg = self._resolver.resolveString(resource)
            if format is not None:
                msg = msg % format
        else:
            msg = 'Logger must be initialized with a string resource file'
        return msg

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
        url = url.replace('$(loggername)', self._name)
        return path.substituteVariables(url, True)

# Public setter method
    def addLogHandler(self, handler):
        self._getLogger().addLogHandler(handler)

    def removeLogHandler(self, handler):
        self._getLogger().removeLogHandler(handler)

    def setDebugMode(self, mode):
        if mode:
            self._setDebugModeOn()
        else:
            self._setDebugModeOff()

    def logMessage(self, level, msg, clazz=None, method=None):
        print("Logger.logMessage() %s - %s - %s - %s" % (level, msg, clazz, method))
        logger = self._getLogger()
        if logger.isLoggable(level):
            if clazz is None or method is None:
                logger.log(level, msg)
            else:
                logger.logp(level, clazz, method, msg)

    def clearLogger(self):
        if self._name in Logger._loggerpool:
            del Logger._loggerpool[self._name]

    def setLoggerSetting(self, enabled, index, state):
        handler = self._getHandler(state)
        self._setLoggerSetting(enabled, index, handler)

# Private getter method
    def _getLogger(self):
        if self._name not in Logger._loggerpool:
            service = '/singletons/com.sun.star.logging.LoggerPool'
            pool = self._ctx.getValueByName(service)
            Logger._loggerpool[self._name] = pool.getNamedLogger(self._name)
        return Logger._loggerpool[self._name]

    def _getStringResource(self, resource):
        if resource is not None:
            return getStringResource(self._ctx, g_identifier, g_resource, resource)
        return None

    def _getLoggerSetting(self):
        configuration = self._getLogConfig()
        enabled, index = self._getLogIndex(configuration)
        handler = configuration.DefaultHandler
        return enabled, index, handler

    def _getLogConfig(self):
        nodepath = '/org.openoffice.Office.Logging/Settings'
        configuration = getConfiguration(self._ctx, nodepath, True)
        if not configuration.hasByName(self._name):
            configuration.insertByName(self._name, configuration.createInstance())
            configuration.commitChanges()
        nodepath += '/%s' % self._name
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
            self.clearLogger()

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
        Logger._logsetting[self._name] = self._getLoggerSetting()
        self._setLoggerSetting(True, 7, 'com.sun.star.logging.FileHandler')

    def _setDebugModeOff(self):
        if self.isDebugMode():
            settings = Logger._logsetting[self._name]
            self._setLoggerSetting(*settings)
            del Logger._logsetting[self._name]
