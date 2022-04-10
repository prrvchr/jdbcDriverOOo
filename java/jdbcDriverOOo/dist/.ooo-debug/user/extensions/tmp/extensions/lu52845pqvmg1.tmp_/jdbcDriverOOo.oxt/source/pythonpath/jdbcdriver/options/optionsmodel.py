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
from ..unotool import getSimpleFile
from ..unotool import getUrl

from ..configuration import g_identifier
from ..dbconfig import g_folder

from ..logger import logMessage
from ..logger import getMessage
g_message = 'OptionsDialog'

import traceback


class OptionsModel(unohelper.Base):

    _level = None

    def __init__(self, ctx):
        self._ctx = ctx
        self._updated = False
        self._driver = None
        self._drivers = {}
        self._archives = []
        self._levels = ['io.github.prrvchr.jdbcdriver.sdbc.Driver', 'io.github.prrvchr.jdbcdriver.sdbcx.Driver']
        path = 'org.openoffice.Office.DataAccess.Drivers'
        self._connectProtocol = 'jdbc:'
        self._registeredProtocol = 'sdbc:'
        self._configuration = getConfiguration(ctx, path, True)
        self.loadConfiguration()
        print("OptionsModel.__init__() 4 %s" % (self._archives, ))
        #mri = createService(self._ctx, 'mytools.Mri')
        #mri.inspect(self._configuration)

# OptionsModel getter methods
    def getLevel(self):
        level = self._levels.index(self._driver.getByName('Driver')) +1
        updated = OptionsModel._level
        return level, updated

    def isLevelUpdated(self):
        return OptionsModel._level

    def getProtocols(self):
        protocols = tuple(self._drivers.keys())
        print("OptionsModel.getProtocols() 1 %s" % (protocols, ))
        return protocols

    def getSubProtocol(self, protocol):
        return protocol.split(':')[1]

    def getDriverName(self, protocol):
        return self._drivers[protocol].getByName('DriverTypeDisplayName')

    def getDriverClass(self, protocol):
        return self._drivers[protocol].getByHierarchicalName('Properties/JavaDriverClass/Value')

    def getDriverArchive(self, protocol):
        return self._getDriverArchive(self._drivers[protocol])

    def getVersion(self):
        if self._updated:
            version = getMessage(self._ctx, g_message, 131)
        else:
            #version = self._getDriverVersion()
            version = '2.51'
        return version

    def _getDriverVersion(self):
        try:
            service = '%s.sdbc.Driver' % g_identifier
            print("OptionsModel._getDriverVersion() 1 %s" % service)
            driver = createService(self._ctx, service)
            print("OptionsModel._getDriverVersion() 2 %s" % driver)
            url = 'sdbc:hsqldb:mem:///dbversion'
            connection = driver.connect(url, ())
            version = connection.getMetaData().getDriverVersion()
            print("OptionsModel._getDriverVersion() 3 %s" % version)
            connection.close()
            return version
        except UnoException as e:
            msg = getMessage(self._ctx, g_message, 141, e.Message)
            logMessage(self._ctx, SEVERE, msg, 'OptionsDialog', '_getDriverVersion()')
        except Exception as e:
            msg = getMessage(self._ctx, g_message, 142, (e, traceback.print_exc()))
            logMessage(self._ctx, SEVERE, msg, 'OptionsDialog', '_getDriverVersion()')

# OptionsModel setter methods
    def setLevel(self, level):
        OptionsModel._level = False
        self._driver.replaceByName('Driver', self._levels[level])

    def setUpdated(self):
        self._updated = True

    def loadConfiguration(self):
        self._driver, self._drivers = self._getDriverConfigurations()
        self._archives = self._getDriverArchives()
        print("OptionsModel._loadConfiguration() 4 %s" % (self._archives, ))

    def saveSetting(self):
        if self._configuration.hasPendingChanges():
            if OptionsModel._level is not None:
                OptionsModel._level = True
            self._configuration.commitChanges()
            return True
        return False

    def _getDriverConfigurations(self):
        driver = None
        drivers = {}
        pattern = 'sdbc:hsqldb:*'
        configuration = self._configuration.getByName('Installed')
        for name in configuration.getElementNames():
            element = configuration.getByName(name)
            if name == pattern:
                driver = element
                drivers[self._getProtocolDisplayName(name)] = element
                print("OptionsModel._getDriverConfiguration() 1 %s" % element.getName())
            elif element.hasByName('ParentURLPattern') and element.getByName('ParentURLPattern') == pattern:
                drivers[self._getProtocolDisplayName(name)] = element
        print("OptionsModel._getDriverConfiguration() 2 %s - %s" % (driver.getByName('Driver'), drivers.keys()))
        return driver, drivers

    def _getProtocolDisplayName(self, name):
        if name.startswith(self._registeredProtocol):
            name = name.replace(self._registeredProtocol, self._connectProtocol)
        return name

    def _getDriverArchive(self, configuration):
        path = configuration.getByHierarchicalName('Properties/JavaDriverClassPath/Value')
        if path.startswith('vnd.sun.star.expand:'):
            path = path.replace('vnd.sun.star.expand:', 'file://', 1)
        url = getUrl(self._ctx, path)
        return url.Name

    def _getDriverArchives(self):
        archives = []
        location = getResourceLocation(self._ctx, g_identifier, g_folder)
        for url in getSimpleFile(self._ctx).getFolderContents(location, False):
            archives.append(getUrl(self._ctx, url).Name)
        return archives
