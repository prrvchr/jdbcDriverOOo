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

import uno

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from com.sun.star.uno import Exception as UnoException

from ..unotool import createService
from ..unotool import getConfiguration
from ..unotool import getPathSettings
from ..unotool import getSimpleFile
from ..unotool import getStringResource
from ..unotool import getResourceLocation
from ..unotool import getUrl

from ..dbconfig import g_folder

from ..logger import getLogger
g_basename = 'OptionsDialog'

from ..configuration import g_identifier
from ..configuration import g_extension

from threading import Thread
import traceback


class OptionsModel():
    def __init__(self, ctx, lock):
        self._ctx = ctx
        self._lock = lock
        self._new = False
        self._protocol = 'xdbc:'
        self._root = self._protocol + '*'
        self._version = 'Version: %s'
        self._default = 'Version: N/A'
        self._groups = 'Groups'
        self._name = 'DriverTypeDisplayName'
        self._class = 'JavaDriverClass'
        self._path = None
        self._tmp = '/tmp'
        self._jarpath = None
        self._classpath = 'JavaDriverClassPath'
        self._versions = {}
        path = 'org.openoffice.Office.DataAccess.Drivers'
        self._config = getConfiguration(ctx, path, True)
        # FIXME: The supported types must follow the display order of the Property Type ListBox
        self._types = (bool, str, tuple)
        self._resolver = getStringResource(ctx, g_identifier, 'dialogs', 'OptionsDialog')
        self._resources = {'TabTitle1' : 'OptionsDialog.Tab1.Title',
                           'TabTitle2' : 'OptionsDialog.Tab2.Title'}
        self._logger = getLogger(ctx, 'Driver', g_basename)
        self._logger.logprb(INFO, 'OptionsDialog', '__init__()', 101)
        self._drivers = self._getDriverConfigurations()
        self._item = ''

    _path = None

# OptionsModel getter methods
    def saveSetting(self):
        print("OptionsModel.saveSetting() 1")
        self._saveConfiguration()
        return self._saveArchives()

    def getTabTitles(self):
        return self._getTabTitle(1), self._getTabTitle(2)

    def getDrivers(self):
        return tuple((k for k, v in self._drivers.items() if v is not None))

    def getDriver(self, name):
        subprotocol = self._getSubProtocol(name)
        driver = self._drivers.get(name)
        groups = tuple(driver.get(self._groups).keys())
        version = self._versions.get(name, self._default)
        updatable = self._isDriverUpdatable(name)
        return subprotocol, driver.get(self._name), groups, version, updatable

    def getDriverName(self, driver):
        return self._drivers.get(driver, {}).get(self._name, '')

    def getGroups(self, name):
        return tuple(self._drivers.get(name).get(self._groups).keys())

    def getProperties(self, driver, group, property):
        properties = self._getPropertyNames(driver, group)
        index = properties.index(property) if property in properties else 0
        updatable = self._isDriverUpdatable(driver)
        return properties, index, updatable

    def getProperty(self, driver, group, property):
        value = self._drivers.get(driver).get(self._groups).get(group).get(property)
        updatable = self._isDriverUpdatable(driver) and self._isPropertyUpdatable(property)
        return value, updatable

    def getPath(self):
        # XXX: We want to keep the last accessed path for the LibreOffice session
        if OptionsModel._path is None:
            OptionsModel._path = getPathSettings(self._ctx).Work
        return OptionsModel._path

    def isNew(self):
        return self._new

    def isDriverValid(self, subprotocol, name, javaclass):
        return self._jarpath is not None and \
               self._isProtocolValid(subprotocol) and \
               self.isDriverNameValid(name) and \
               self._isJavaClassValid(javaclass)

    def isDriverNameValid(self, name):
        names = (v.get(self._name) for v in self._drivers.values() if v is not None)
        return len(name) > 0 and name not in names

    def addDriver(self, subprotocol, name, javaclass):
        properties = {self._classpath: self._jarpath, self._class: javaclass}
        groups = {'MetaData': {}, 'Properties': properties}
        driver = self._getProtocol(subprotocol)
        self._drivers[driver] = {self._name: name, self._groups: groups}
        return driver

    def isPropertyNameValid(self, driver, group, property):
        group = self._drivers.get(driver).get(self._groups).get(group)
        properties = (k for k, v in group.items() if v is not None)
        return len(property) > 0 and property not in properties

    def editProperty(self, driver, group, new, old):
        properties = self._drivers.get(driver).get(self._groups).get(group)
        properties[new] = properties[old]
        properties[old] = None
        return tuple((k for k, v in properties.items() if v is not None))

    def addProperty(self, driver, group, property, default):
        properties = self._drivers.get(driver).get(self._groups).get(group)
        properties[property] = default
        return tuple((k for k, v in properties.items() if v is not None))

    def isPropertyItemModified(self, item):
        return self._item != item

    def getDriverVersion(self, protocol):
        version = self._default
        if protocol in self._versions:
            version = self._versions[protocol]
        return version

# OptionsModel setter methods
    def loadSetting(self):
        if self._path is not None:
            sf = getSimpleFile(self._ctx)
            self._deleteFolderContent(sf, self._path + self._tmp)
            self._path = None
        self._drivers = self._getDriverConfigurations()
 
    def removeProperty(self, driver, group, property):
        self._drivers.get(driver).get(self._groups).get(group)[property] = None

    def updateDriverName(self, driver, name):
        self._drivers.get(driver)[self._name] = name

    def setNew(self, new):
        self._new = new

    def clearJarPath(self):
        self._jarpath = None

    def removeDriver(self, driver):
        self._drivers[driver] = None

    def setArchive(self, subprotocol, archives):
        protocol = self._getProtocol(subprotocol)
        self._jarpath = self._updateArchive(protocol, archives)

    def updateArchive(self, driver, archives):
        # XXX: Jar files have been copied, we need to save the configuration
        properties = self._drivers.get(driver).get(self._groups).get('Properties')
        properties[self._classpath] = self._updateArchive(driver, archives)

    def setPropertyItem(self, item):
        self._item = item

    def setDriverVersions(self, *args):
        with self._lock:
            self._versions = {}
            Thread(target=self._setDriverVersions, args=args).start()

# OptionsModel private getter methods
    def _isProtocolValid(self, subprotocol):
        protocol = self._getProtocol(subprotocol)
        protocols = (k for k, v in self._drivers.items() if v is not None)
        return len(subprotocol) > 0 and protocol not in protocols

    def _isJavaClassValid(self, javaclass):
        return len(javaclass) > 0 and \
               '.' in javaclass and \
               not javaclass.endswith('.') and \
               not javaclass.startswith('.')

    def _getPropertyNames(self, driver, group):
        groups = self._drivers.get(driver).get(self._groups)
        properties = groups.get(group)
        if properties is None:
            properties = self._getProperties(driver, group)
            groups[group] = properties
        return tuple((k for k, v in properties.items() if v is not None))

    def _saveArchives(self):
        if self._path is None:
            return False
        sf = getSimpleFile(self._ctx)
        source = self._path + self._tmp
        target = '%s/%s/' % (self._path, g_folder)
        #sf.copy(source, target)
        reboot = self._copyArchive(sf, source, target)
        self._deleteFolderContent(sf, source)
        self._path = None
        return reboot

    def _copyArchive(self, sf, source, target):
        count = 0
        for path in sf.getFolderContents(source, True):
            url = getUrl(self._ctx, path)
            sf.copy(path, target + url.Name)
            count += 1
        return count > 0

    def _saveConfiguration(self):
        config = self._config.getByName('Installed')
        for protocol, driver in self._drivers.items():
            if protocol == self._root:
                continue
            if driver is None:
                if config.hasByName(protocol):
                    config.removeByName(protocol)
            else:
                if not config.hasByName(protocol):
                    config.insertByName(protocol, config.createInstance())
                    self._saveProperty(config.getByName(protocol), 'ParentURLPattern', self._root)
                self._saveDriver(config.getByName(protocol), driver)
        if self._config.hasPendingChanges():
            self._config.commitChanges()

    def _getUnoType(self, value):
        if isinstance(value, tuple):
            unotype = '[]string'
        elif isinstance(value, bool):
            unotype = 'boolean'
        else:
            unotype = 'string'
        return unotype

    def _isDriverUpdatable(self, driver):
        return driver != self._root

    def _isPropertyUpdatable(self, property):
        return property != self._classpath

    def _getConfiguration(self):
        path = 'org.openoffice.Office.DataAccess.Drivers'
        return getConfiguration(self._ctx, path, True)

    def _getDriverConfigurations(self):
        drivers = {}
        config = self._config.getByName('Installed')
        #mri = createService(self._ctx, 'mytools.Mri')
        #mri.inspect(config)
        for name in config.getElementNames():
            driver = config.getByName(name)
            if self._isRootDriver(driver, name):
                drivers[name] = {self._groups: self._getGroups(driver, name),
                                 self._name: driver.getByName(self._name)}
        return drivers

    def _isRootDriver(self, driver, name):
        return name == self._root or self._isChildDriver(driver)

    def _isChildDriver(self, driver):
        return driver.hasByName('ParentURLPattern') and driver.getByName('ParentURLPattern') == self._root

    def _getGroups(self, driver, name):
        groups = {}
        for name in driver.getElementNames():
            group = driver.getByName(name)
            if not group or isinstance(group, str):
                continue
            groups[group.Name] = None
        return groups

    def _getProperties(self, driver, group):
        properties = {}
        config = self._config.getByName('Installed')
        group = config.getByName(driver).getByName(group)
        for name in group.getElementNames():
            property = group.getByName(name)
            properties[property.Name] = property.getByName('Value')
        return properties

    def _updateArchive(self, driver, archives):
        sf = getSimpleFile(self._ctx)
        multi = len(archives) > 1
        subprotocol = self._getSubProtocol(driver)
        tmp = self._getTmpPath(sf, subprotocol, multi)
        if multi:
            # XXX: If we have multiple archives, all the contents of the folder will be
            # XXX: added to the Java ClassLoader so we need to clean that folder first.
            self._deleteFolderContent(sf, tmp)
        for archive in archives:
            url = getUrl(self._ctx, archive)
            target = '%s/%s' % (tmp, url.Name)
            print("OptionsModel._updateArchive() source: %s - target: %s" % (url.Main, target))
            sf.copy(url.Main, target)
        # XXX: We want to be able to preserve the last path used by the FilePicker
        OptionsModel._path = url.Protocol + url.Path
        return self._getTargetPath(subprotocol, multi, url)

    def _getTmpPath(self, sf, subprotocol, multi):
        if self._path is None:
            self._path = getResourceLocation(self._ctx, g_identifier)
            self._deleteFolderContent(sf, self._path + self._tmp)
        path = self._path + self._tmp
        return '%s/%s' % (path, subprotocol) if multi else path

    def _getTargetPath(self, subprotocol, multi, url):
        path = '%s/%s/' % (self._path, g_folder)
        return path + subprotocol if multi else path + url.Name

    def _getProtocol(self, subprotocol):
        return '%s:*' % self._protocol + subprotocol

    def _getSubProtocol(self, driver):
        return driver.split(':')[1] if driver else ''

    def _getPropertyValue(self, driver, group, property):
        return self._drivers.get(driver).getByName(group).getByName(property).Value

    def _getTabTitle(self, tab):
        resource = self._resources.get('TabTitle%s' % tab)
        return self._resolver.resolveString(resource)

# OptionsModel private setter methods
    def _saveDriver(self, config, driver):
        self._saveProperty(config, self._name, driver.get(self._name))
        groups = driver.get(self._groups)
        for property in groups:
            if not config.hasByName(property):
                config.insertByName(property, config.createInstance())
            properties = groups.get(property)
            if properties is not None:
                self._saveProperties(config.getByName(property), properties)

    def _saveProperties(self, config, properties):
        print("OptionsModel._saveProperties() 1")
        for name, value in properties.items():
            if value is None:
                if config.hasByName(name):
                    config.removeByName(name)
            else:
                if not config.hasByName(name):
                    config.insertByName(name, config.createInstance())
                property = config.getByName(name)
                if not property.hasByName('Value'):
                    property.insertByName('Value', property.createInstance())
                if property.getByName('Value') != value:
                    arguments = ('Value', uno.Any(self._getUnoType(value), value))
                    uno.invoke(property, 'replaceByName', arguments)

    def _saveProperty(self, config, name, value):
        print("OptionsModel._saveProperty() 1 Name: %s" % name)
        if not config.hasByName(name):
            config.insertByName(name, config.createInstance())
        if config.getByName(name) != value:
            config.replaceByName(name, value)

    def _deleteFolderContent(self, sf, folder):
        if sf.exists(folder) and sf.isFolder(folder):
            sf.kill(folder)
        sf.createFolder(folder)

    def _setDriverVersions(self, name, update):
        versions = {}
        config = self._config.getByName('Installed')
        property = 'Properties/InMemoryDataBase/Value'
        service = createService(self._ctx, name)
        for protocol in config.getElementNames():
            driver = config.getByName(protocol)
            if driver.hasByHierarchicalName(property):
                url = driver.getByHierarchicalName(property)
                if len(url) > 0:
                    version = self._getDriverVersion(service, url)
                    versions[protocol] = version
        with self._lock:
            self._versions = versions
        update(versions)

    def _getDriverVersion(self, driver, subprotocol):
        version = self._default
        try:
            url = self._protocol + subprotocol
            connection = driver.connect(url, ())
            if connection is not None:
                version = self._version % connection.getMetaData().getDriverVersion()
                connection.close()
        except UnoException as e:
            self._logger.logprb(SEVERE, 'OptionsDialog', '_getDriverVersion()', 111, e.Message)
        except Exception as e:
            self._logger.logprb(SEVERE, 'OptionsDialog', '_getDriverVersion()', 112, e, traceback.format_exc())
        return version

