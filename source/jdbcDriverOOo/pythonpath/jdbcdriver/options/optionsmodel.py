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

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from com.sun.star.uno import Exception as UnoException

from ..unotool import createService
from ..unotool import getConfiguration
from ..unotool import getPathSettings
from ..unotool import getResourceLocation
from ..unotool import getUrl

from ..configuration import g_identifier

from ..dbconfig import g_folder

from ..logger import logMessage
from ..logger import getMessage
g_message = 'OptionsDialog'

from threading import Thread
from collections import OrderedDict
import traceback


class OptionsModel(unohelper.Base):

    _level = None
    _reboot = False

    def __init__(self, ctx, lock, update, loggers):
        self._ctx = ctx
        self._lock = lock
        self._path = None
        self._driver = None
        self._drivers = {}
        self._version = 'Version: %s'
        self._default = 'Version: N/A'
        self._versions = {}
        self._dbloggers = ('h2', 'derby', 'hsqldb')
        self._dbversions = {'h2': 'mem:dbversion',
                            'derby': 'memory:dbversion;create=true',
                            'hsqldb': 'mem:dbversion',
                            'smallsql': None}
        self._services = ('io.github.prrvchr.jdbcdriver.sdbc.Driver',
                          'io.github.prrvchr.jdbcdriver.sdbcx.Driver')
        self._connectProtocol = 'jdbc:'
        self._registeredProtocol = 'xdbc:'
        self.loadConfiguration(update, loggers)

    def loadConfiguration(self, *args):
        with self._lock:
            self._versions = {}
        path = 'org.openoffice.Office.DataAccess.Drivers'
        self._configuration = getConfiguration(self._ctx, path, True)
        config = self._configuration.getByName('Installed')
        root = self._getRootProtocol(False)
        self._driver = config.getByName(root)
        self._drivers = self._getDriverConfigurations(config, root)
        if not self.needReboot():
            Thread(target=self._setDriverVersions, args=args).start()

# OptionsModel getter methods
    def getLoggerNames(self, *args):
        service = 'io.github.prrvchr.jdbcdriver.logging.DBLoggerPool'
        loggers = args + createService(self._ctx, service).getLoggerNames()
        return {name: name in args for name in loggers}

    def needReboot(self):
        return OptionsModel._reboot

    def getPath(self):
        if self._path is None:
            self._path = getPathSettings(self._ctx).Work
        print("OptionsModel.getPath() %s" % self._path)
        return self._path

    def getTarget(self, url):
        location = '%s/%s' % (g_folder, url.Name)
        return getResourceLocation(self._ctx, g_identifier, location)

    def getLevel(self):
        level = self._services.index(self._getDriverService()) +1
        updated = OptionsModel._level
        return level, updated

    def isLevelUpdated(self):
        return OptionsModel._level

    def getProtocols(self):
        return tuple(self._drivers.keys())

    def getSubProtocol(self, protocol):
        return protocol.split(':')[1]

    def _getDriverService(self):
        return self._driver.getByName('Driver')

    def getDriverName(self, protocol):
        return self._drivers[protocol].getByName('DriverTypeDisplayName')

    def getDriverClass(self, protocol):
        return self._drivers[protocol].getByHierarchicalName('Properties/JavaDriverClass/Value')

    def getLogger(self, protocol):
        # XXX: level can have the following values:
        # XXX: None: Logging is not supported by the underlying database and will be disabled.
        # XXX: -1: Correspond to the UNO Logger API LogLevel com.sun.star.logging.LogLevel.OFF
        # XXX: 0 to 7: for all other LogLevel from com.sun.star.logging.LogLevel
        level = None
        property ='Properties/DriverLoggerLevel/Value'
        if self._drivers[protocol].hasByHierarchicalName(property):
            level = int(self._drivers[protocol].getByHierarchicalName(property))
        return level

    def getDriverVersion(self, protocol):
        version = self._default
        with self._lock:
            if protocol in self._versions:
                version = self._versions[protocol]
        return version

    def getDriverClassPath(self, protocol):
        return self._drivers[protocol].getByHierarchicalName('Properties/JavaDriverClassPath/Value')

    def getDriverArchive(self, protocol):
        path = self.getDriverClassPath(protocol)
        if path:
            path = self._getArchiveFromPath(path)
        return path

    def isNotRoot(self, protocol):
        return protocol != self._getRootProtocol()

    def isDriverValide(self, sub, name, clazz, archive):
        return (sub != '' and
                name != '' and
                clazz != '' and
                archive != '' and
                self._getProtocol(sub) not in self._drivers)

# OptionsModel setter methods
    def setLevel(self, level):
        OptionsModel._level = False
        self._driver.replaceByName('Driver', self._services[level])

    def setPath(self, url):
        self._path = url.Protocol + url.Path
        print("OptionsModel.setPath() %s" % self._path)

    def removeProtocol(self, protocol):
        name = self._getProtocolName(protocol)
        config = self._configuration.getByName('Installed')
        if config.hasByName(name):
            config.removeByName(name)
            self._drivers = self._getRootConfigurations(config)
            return True
        return False

    def saveSetting(self):
        if self._configuration.hasPendingChanges():
            if OptionsModel._level is not None:
                OptionsModel._level = True
            self._configuration.commitChanges()
            OptionsModel._reboot = True
            return True
        return False

    def saveDriver(self, subprotocol, name, clazz, archive, level):
        protocol = self._getProtocol(subprotocol)
        driver = self._saveDriver(subprotocol, name, clazz, archive, level)
        self._drivers[protocol] = driver
        return protocol

    def updateArchive(self, protocol, archive):
        self._updateArchive(self._drivers[protocol], archive)

    def setLogger(self, protocol, level):
        property = 'Properties/DriverLoggerLevel/Value'
        self._drivers[protocol].setHierarchicalPropertyValue(property, self._getLevelValue(level))

# OptionsModel private methods
    def _getLevelValue(self, level):
        print("OptionsModel._getLevelValue() %d" % level)
        return '%d' % level

    def _getRoot(self, jdbc=True):
        return self._connectProtocol if jdbc else self._registeredProtocol

    def _getRootConfigurations(self, config):
        root = self._getRootProtocol(False)
        return self._getDriverConfigurations(config, root)

    def _getDriverConfigurations(self, config, root):
        drivers = OrderedDict()
        for name in config.getElementNames():
            element = config.getByName(name)
            if name == root:
                drivers[self._getProtocolDisplayName(name)] = element
            elif element.hasByName('ParentURLPattern') and element.getByName('ParentURLPattern') == root:
                drivers[self._getProtocolDisplayName(name)] = element
        return drivers

    def _getRootProtocol(self, jdbc=True):
        return '%s*' % self._getRoot(jdbc)

    def _getProtocol(self, subprotocol, jdbc=True):
        return '%s%s:*' % (self._getRoot(jdbc), subprotocol)

    def _getProtocolName(self, name):
        if name.startswith(self._connectProtocol):
            name = name.replace(self._connectProtocol, self._registeredProtocol, 1)
        return name

    def _getProtocolDisplayName(self, name):
        if name.startswith(self._registeredProtocol):
            name = name.replace(self._registeredProtocol, self._connectProtocol, 1)
        return name

    def _getArchiveFromPath(self, path):
        if path.startswith('vnd.sun.star.expand:'):
            path = path.replace('vnd.sun.star.expand:', 'file://', 1)
        url = getUrl(self._ctx, path)
        return url.Name

    def _setDriverVersions(self, update, loggers):
        property = 'Properties/InMemoryDataBase/Value'
        service = createService(self._ctx, self._getDriverService())
        for protocol, driver in self._drivers.items():
            if driver.hasByHierarchicalName(property):
                url = driver.getByHierarchicalName(property)
                version = self._getDriverVersion(service, url)
                with self._lock:
                    self._versions[protocol] = version
        with self._lock:
            update(self.getLoggerNames(loggers), self._versions)

    def _getDriverVersion(self, driver, protocol):
        version = self._default
        try:
            url = '%s%s' % (self._registeredProtocol, protocol)
            connection = driver.connect(url, ())
            version = self._version % connection.getMetaData().getDriverVersion()
            connection.close()
        except UnoException as e:
            msg = getMessage(self._ctx, g_message, 141, e.Message)
            logMessage(self._ctx, SEVERE, msg, 'OptionsDialog', '_getDriverVersion()')
        except Exception as e:
            msg = getMessage(self._ctx, g_message, 142, (e, traceback.print_exc()))
            logMessage(self._ctx, SEVERE, msg, 'OptionsDialog', '_getDriverVersion()')
        return version

    def _updateArchive(self, driver, archive):
        property = 'Properties/JavaDriverClassPath/Value'
        location = '%s/%s' % (g_folder, archive)
        path = getResourceLocation(self._ctx, g_identifier, location)
        driver.setHierarchicalPropertyValue(property, path)

    def _saveDriver(self, subprotocol, name, clazz, archive, level):
        config = self._configuration.getByName('Installed')
        protocol = self._getProtocol(subprotocol, False)
        if not config.hasByName(protocol):
            config.insertByName(protocol, config.createInstance())
        driver = config.getByName(protocol)
        root = self._getRootProtocol(False)
        try:
            driver.setHierarchicalPropertyValue('ParentURLPattern', root)
            driver.setHierarchicalPropertyValue('DriverTypeDisplayName', name)
            properties = driver.getByName('Properties')
            self._createDriverProperty(properties, 'JavaDriverClass', clazz)
            self._createDriverProperty(properties, 'JavaDriverClassPath')
            self._updateArchive(driver, archive)
            if self._supportLogger(subprotocol):
                self._createDriverProperty(properties, 'DriverLoggerLevel', self._getLevelValue(level))
            if self._supportVersion(subprotocol):
                url = self._getUrlVersion(subprotocol)
                self._createDriverProperty(properties, 'InMemoryDataBase', url)
        except Exception as e:
            print("OptionsModel._saveDriver() ERROR")
            print("OptionsModel._saveDriver() ERROR %s" % e)
        return driver

    def _supportLogger(self, subprotocol):
        return subprotocol in self._dbloggers

    def _supportVersion(self, subprotocol):
        return subprotocol in self._dbversions

    def _getUrlVersion(self, subprotocol):
        url = self._dbversions[subprotocol]
        if url is not None:
            subprotocol += ':%s' % url
        return subprotocol

    def _createDriverProperty(self, properties, name, value=None):
        if not properties.hasByName(name):
            properties.insertByName(name, properties.createInstance())
        property = properties.getByName(name)
        if not property.hasByName('Value'):
            property.insertByName('Value', property.createInstance())
        if value is not None:
            property.replaceByName('Value', value)

