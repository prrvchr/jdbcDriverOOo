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

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from com.sun.star.uno import Exception as UnoException

from ..unotool import createService
from ..unotool import getConfiguration
from ..unotool import getPathSettings
from ..unotool import getStringResource
from ..unotool import getResourceLocation
from ..unotool import getUrl

from ..dbconfig import g_folder

from ..logger import getLogger
g_basename = 'OptionsDialog'

from ..configuration import g_identifier
from ..configuration import g_extension

from threading import Thread
from collections import OrderedDict
import traceback


class OptionsModel():
    def __init__(self, ctx, lock):
        self._ctx = ctx
        self._lock = lock
        self._path = None
        self._version = 'Version: %s'
        self._default = 'Version: N/A'
        self._versions = {}
        self._config = getConfiguration(ctx, g_identifier, True)
        self._service = self._getDriverService()
        path = 'org.openoffice.Office.DataAccess.Drivers'
        self._configuration = getConfiguration(ctx, path, True)
        self._dbloggers = ('h2', 'derby', 'hsqldb')
        self._dbversions = {'h2': 'mem:dbversion',
                            'derby': 'memory:dbversion;create=true',
                            'hsqldb': 'mem:dbversion',
                            'smallsql': None}
        self._services = {'Driver': ('io.github.prrvchr.jdbcdriver.sdbc.Driver',
                                     'io.github.prrvchr.jdbcdriver.sdbcx.Driver'),
                          'Connection': ('com.sun.star.sdbc.Connection',
                                         'com.sun.star.sdbcx.Connection',
                                         'com.sun.star.sdb.Connection')}
        self._connectProtocol = 'jdbc:'
        self._registeredProtocol = 'xdbc:'
        self._resolver = getStringResource(ctx, g_identifier, 'dialogs', 'OptionsDialog')
        self._resources = {'TabTitle1' : 'OptionsDialog.Tab1.Title',
                           'TabTitle2' : 'OptionsDialog.Tab2.Title'}
        config = self._configuration.getByName('Installed')
        root = self._getRootProtocol(False)
        self._driver = config.getByName(root)
        self._drivers = self._getDriverConfigurations(config, root)
        self._logger = getLogger(ctx, 'Driver', g_basename)
        self._logger.logprb(INFO, 'OptionsDialog', '__init__()', 101)

# OptionsModel getter methods
    def getTabTitles(self):
        return self._getTabTitle(1), self._getTabTitle(2)

    def getPath(self):
        if self._path is None:
            self._path = getPathSettings(self._ctx).Work
        return self._path

    def getTarget(self, url):
        location = '%s/%s' % (g_folder, url.Name)
        return getResourceLocation(self._ctx, g_identifier, location)

    def getServicesLevel(self):
        driver = self._services.get('Driver').index(self._getDriverService())
        connection = self._services.get('Connection').index(self._getConnectionService())
        system = self._config.getByName('ShowSystemTable')
        bookmark = self._config.getByName('UseBookmark')
        mode = self._config.getByName('SQLMode')
        return driver, connection, self._isConnectionLevelEnabled(driver), system, bookmark, mode

    def getProtocols(self):
        return tuple(self._drivers.keys())

    def getSubProtocol(self, protocol):
        return protocol.split(':')[1]

    def _getDriverService(self):
        return self._config.getByName('DriverService')

    def _getConnectionService(self):
        return self._config.getByName('ConnectionService')

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
    def setDriverVersions(self, *args):
        with self._lock:
            self._versions = {}
            Thread(target=self._setDriverVersions, args=args).start()

    def setDriverService(self, driver):
        self._config.replaceByName('DriverService', self._services.get('Driver')[driver])
        connection = self._services.get('Connection').index(self._getConnectionService())
        if driver and not connection:
            connection = 1
            self.setConnectionService(connection)
        return connection, self._isConnectionLevelEnabled(driver)

    def setConnectionService(self, level):
        self._config.replaceByName('ConnectionService', self._services.get('Connection')[level])

    def setSystemTable(self, state):
        self._config.replaceByName('ShowSystemTable', bool(state))

    def setBookmark(self, state):
        self._config.replaceByName('UseBookmark', bool(state))

    def setSQLMode(self, state):
        self._config.replaceByName('SQLMode', bool(state))

    def setPath(self, url):
        self._path = url.Protocol + url.Path

    def removeProtocol(self, protocol):
        name = self._getProtocolName(protocol)
        config = self._configuration.getByName('Installed')
        if config.hasByName(name):
            config.removeByName(name)
            self._drivers = self._getRootConfigurations(config)
            return True
        return False

    def saveSetting(self):
        driver = self._configuration.hasPendingChanges()
        if driver:
            self._configuration.commitChanges()
        config = self._config.hasPendingChanges()
        if config:
            self._config.commitChanges()
            if self._service != self._getDriverService():
                return True
        return driver

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
    def _isConnectionLevelEnabled(self, driver):
        return driver == 0

    def _getLevelValue(self, level):
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

    def _setDriverVersions(self, update):
        versions = {}
        property = 'Properties/InMemoryDataBase/Value'
        service = createService(self._ctx, self._getDriverService())
        for protocol, driver in self._drivers.items():
            if driver.hasByHierarchicalName(property):
                url = driver.getByHierarchicalName(property)
                version = self._getDriverVersion(service, url)
                versions[protocol] = version
        with self._lock:
            self._versions = versions
        update(versions)

    def _getDriverVersion(self, driver, protocol):
        version = self._default
        try:
            url = '%s%s' % (self._registeredProtocol, protocol)
            connection = driver.connect(url, ())
            if connection is not None:
                version = self._version % connection.getMetaData().getDriverVersion()
                connection.close()
        except UnoException as e:
            self._logger.logprb(SEVERE, 'OptionsDialog', '_getDriverVersion()', 111, e.Message)
        except Exception as e:
            self._logger.logprb(SEVERE, 'OptionsDialog', '_getDriverVersion()', 112, e, traceback.format_exc())
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

    def _getTabTitle(self, tab):
        resource = self._resources.get('TabTitle%s' % tab)
        return self._resolver.resolveString(resource)
