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
from ..unotool import getResourceLocation
from ..unotool import getUrl

from ..configuration import g_identifier
from ..dbconfig import g_folder

from ..logger import logMessage
from ..logger import getMessage
g_message = 'OptionsDialog'

from threading import Thread
from threading import Condition
from collections import OrderedDict
import traceback


class OptionsModel(unohelper.Base):

    _level = None
    _reboot = False

    def __init__(self, ctx):
        self._ctx = ctx
        self._updated = False
        self._driver = None
        self._drivers = {}
        self._versions = {}
        self._levels = ['io.github.prrvchr.jdbcDriverOOo.sdbc.Driver', 'io.github.prrvchr.jdbcDriverOOo.sdbcx.Driver']
        self._connectProtocol = 'jdbc:'
        self._registeredProtocol = 'xdbc:'
        self._lock = Condition()
        self.loadConfiguration()

    def loadConfiguration(self):
        with self._lock:
            self._versions = {}
        path = 'org.openoffice.Office.DataAccess.Drivers'
        self._configuration = getConfiguration(self._ctx, path, True)
        config = self._configuration.getByName('Installed')
        root = self._getRootProtocol(False)
        self._driver = config.getByName(root)
        self._drivers = self._getDriverConfigurations(config, root)
        self._setVersions(self._drivers.keys())

    def _setVersions(self, *args):
        Thread(target=self._setDriverVersions, args=args).start()

# OptionsModel getter methods
    def needReboot(self):
        return OptionsModel._reboot

    def getLevel(self):
        level = self._levels.index(self._getDriverService()) +1
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

    def getDriverVersion(self, protocol):
        version = 'Version: N/A'
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
        self._driver.replaceByName('Driver', self._levels[level])

    def setUpdated(self):
        self._updated = True

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

    def saveDriver(self, subprotocol, name, clazz, archive):
        protocol = self._getProtocol(subprotocol)
        driver = self._saveDriver(subprotocol, name, clazz, archive)
        self._drivers[protocol] = driver
        return protocol

    def updateArchive(self, protocol, archive):
        self._updateArchive(self._drivers[protocol], archive)

# OptionsModel private methods
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

    def _setDriverVersions(self, drivers):
        for driver in drivers:
            memdb = self._getInMemoryDataBase(driver)
            if memdb:
                version = self._getDriverVersion(driver, memdb)
                with self._lock:
                    self._versions[driver] = version

    def _getInMemoryDataBase(self, protocol):
        memdb = None
        driver = self._drivers[protocol]
        property = 'Properties/InMemoryDataBase/Value'
        if driver.hasByHierarchicalName(property):
            memdb = driver.getByHierarchicalName(property)
        return memdb

    def _getDriverVersion(self, protocol, memdb):
        version = 'Version: N/A'
        try:
            service = self._getDriverService()
            driver = createService(self._ctx, service)
            subprotocol = self.getSubProtocol(protocol)
            url = '%s%s:%s' % (self._registeredProtocol, subprotocol, memdb)
            connection = driver.connect(url, ())
            version = 'Version: %s' % connection.getMetaData().getDriverVersion()
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

    def _saveDriver(self, subprotocol, name, clazz, archive):
        config = self._configuration.getByName('Installed')
        protocol = self._getProtocol(subprotocol, False)
        if not config.hasByName(protocol):
            config.insertByName(protocol, config.createInstance())
        driver = config.getByName(protocol)
        root = self._getRootProtocol(False)
        driver.setHierarchicalPropertyValue('ParentURLPattern', root)
        driver.setHierarchicalPropertyValue('DriverTypeDisplayName', name)
        properties = driver.getByName('Properties')
        self._createDriverProperty(properties, 'JavaDriverClass')
        self._createDriverProperty(properties, 'JavaDriverClassPath')
        property = 'Properties/JavaDriverClass/Value'
        driver.setHierarchicalPropertyValue(property, clazz)
        self._updateArchive(driver, archive)
        return driver

    def _createDriverProperty(self, properties, name):
        if not properties.hasByName(name):
            properties.insertByName(name, properties.createInstance())
        property = properties.getByName(name)
        if not property.hasByName('Value'):
            property.insertByName('Value', property.createInstance())

