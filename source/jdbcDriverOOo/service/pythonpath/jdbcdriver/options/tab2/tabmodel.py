#!
# -*- coding: utf-8 -*-

"""
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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

from ...unotool import createService
from ...unotool import getConfiguration
from ...unotool import getPathSettings
from ...unotool import getSimpleFile
from ...unotool import getStringResource
from ...unotool import getResourceLocation
from ...unotool import getUrl

from ...dbconfig import g_folder

from ...logger import getLogger

from ...jdbcdriver import g_service

from ...configuration import g_basename
from ...configuration import g_defaultlog
from ...configuration import g_identifier

from threading import Thread
import traceback


class TabModel():
    def __init__(self, ctx, lock, restart, xdl):
        self._ctx = ctx
        self._lock = lock
        self._restart = restart
        self._pmode = 0
        self._vmode = 0
        self._value = None
        self._protocol = 'xdbc:'
        self._root = self._protocol + '*'
        self._tmp = '/tmp'
        self._url = None
        self._path = None
        self._folder = None
        self._versions = {}
        self._groups = 'Groups'
        self._name = 'DriverTypeDisplayName'
        self._class = 'JavaDriverClass'
        self._classpath = 'JavaDriverClassPath'
        self._package = 'vnd.sun.star.expand:$UNO_USER_PACKAGES_CACHE/uno_packages'
        path = 'org.openoffice.Office.DataAccess.Drivers'
        self._config = getConfiguration(ctx, path, True)
        # FIXME: The supported types must follow the display order of the Property Type ListBox
        self._types = (bool, int, str, tuple)
        self._unotypes = ('boolean', 'long', 'string', '[]string')
        self._defaults = (False, 0, '', ())
        self._default = str
        self._resolver = getStringResource(ctx, g_identifier, 'dialogs', xdl)
        self._resources = {'Version1': 'Option2Dialog.Label6.Label.0',
                           'Version2': 'Option2Dialog.Label6.Label.1'}
        self._drivers = self._getDriverConfigurations()
        self._logger = getLogger(ctx, g_defaultlog, g_basename)
        self._logger.logprb(INFO, 'OptionsModel', '__init__', 301)

    _directory = None

# TabModel getter methods
    def saveSetting(self):
        self._saveConfiguration()
        return self._saveArchives()

    def getRestart(self):
        return self._restart

    def getArchivePath(self):
        return '%s/%s' % (self._getUrl().Main, g_folder)

    def getDrivers(self):
        return tuple((k for k, v in self._drivers.items() if v is not None))

    def getDriver(self, name):
        subprotocol = self._getSubProtocol(name)
        driver = self._drivers.get(name)
        groups = tuple(driver.get(self._groups).keys())
        version = self._getDriverVersion(name)
        updatable = self._isDriverUpdatable(name)
        return subprotocol, driver.get(self._name), groups, version, updatable

    def getDriverName(self, driver):
        return self._drivers.get(driver, {}).get(self._name, '')

    def getProperties(self, driver, group):
        properties = self._getPropertyNames(driver, group)
        updatable = self._isDriverUpdatable(driver)
        return properties, updatable

    def getProperty(self, driver, group, property):
        value = self._getGroup(driver, group).get(property)
        updatable = self._isDriverUpdatable(driver)
        return value, updatable, *self._getTypeIndex(value)

    def getDisplayDirectory(self):
        # XXX: We want to keep the last accessed FilePicker path for the LibreOffice session
        if TabModel._directory is None:
            TabModel._directory = getPathSettings(self._ctx).Work
        return TabModel._directory

    def getType(self, index):
        return self._types[index]

    def isNewProperty(self):
        return self._pmode == 2

    def isEditValue(self):
        return self._vmode != 0

    def isNewValue(self):
        return self._vmode == 2

    def isDriverValid(self, subprotocol, name, javaclass):
        return self._path is not None and \
               self._isProtocolValid(subprotocol) and \
               self.isDriverNameValid(name) and \
               self._isJavaClassValid(javaclass)

    def isDriverNameValid(self, name):
        names = (v.get(self._name) for v in self._drivers.values() if v is not None)
        return len(name) > 0 and name not in names

    def addDriver(self, subprotocol, name, javaclass):
        properties = {self._classpath: self._path, self._class: javaclass}
        groups = {'MetaData': {}, 'Properties': properties}
        driver = self._getProtocol(subprotocol)
        self._drivers[driver] = {self._name: name, self._groups: groups}
        self._path = None
        return driver

    def isPropertyNameValid(self, driver, group, property):
        group = self._getGroup(driver, group)
        properties = (k for k, v in group.items() if v is not None)
        return len(property) > 0 and property not in properties

    def editProperty(self, driver, group, new, old, value):
        properties = self._getGroup(driver, group)
        properties[new] = value
        properties[old] = None
        return tuple((k for k, v in properties.items() if v is not None))

    def addProperty(self, driver, group, property, value):
        properties = self._getGroup(driver, group)
        properties[property] = value
        return tuple((k for k, v in properties.items() if v is not None))

    def removeProperty(self, driver, group, property):
        properties = self._getGroup(driver, group)
        properties[property] = None
        return tuple((k for k, v in properties.items() if v is not None))

    def getDefaultPropertyValue(self, index):
        return self._defaults[index]

    def addPropertyValue(self, driver, group, property, value):
        properties = self._getGroup(driver, group)
        values = list(properties.get(property))
        values.append(value)
        newvalues = tuple(values)
        properties[property] = newvalues
        return newvalues

    def removePropertyValue(self, driver, group, property, index):
        properties = self._getGroup(driver, group)
        values = list(properties.get(property))
        values.pop(index)
        newvalues = tuple(values)
        properties[property] = newvalues
        return newvalues

    def editPropertyValue(self, driver, group, property, value, index):
        properties = self._getGroup(driver, group)
        values = list(properties.get(property))
        values[index] = value
        newvalues = tuple(values)
        properties[property] = newvalues
        return newvalues

    def getAddPropertyValue(self):
        return self._value if self.isNewProperty() else None

# TabModel setter methods
    def loadSetting(self):
        if self._folder is not None:
            sf = getSimpleFile(self._ctx)
            self._deleteFolderContent(sf, self._url.Main + self._tmp)
            self._folder = None
        self._drivers = self._getDriverConfigurations()

    def setRestart(self, restart):
        self._restart = restart

    def cancelDriver(self):
        self._path = None

    def updateDriverName(self, driver, name):
        self._drivers.get(driver)[self._name] = name

    def setPropertyMode(self, mode):
        self._pmode = mode

    def setAddPropertyValue(self, value):
        self._value = value

    def setValueMode(self, mode):
        self._vmode = mode

    def setPropertyValue(self, driver, group, property, value):
        # FIXME: We don't save anything if we are adding a new property
        if not self.isNewProperty():
            self._getGroup(driver, group)[property] = value

    def clearJarPath(self):
        self._path = None

    def removeDriver(self, driver):
        self._drivers[driver] = None

    def setArchive(self, subprotocol, archives):
        protocol = self._getProtocol(subprotocol)
        self._path = self._updateArchive(protocol, archives)

    def setDisplayDirectory(self, folder):
        TabModel._directory = folder

    def updateArchive(self, driver, archives):
        path = self._updateArchive(driver, archives)
        # XXX: Jar files have been copied, we need to save the configuration
        group = 'Properties'
        properties = self._getGroup(driver, group)
        # FIXME: As we are doing lazy loading on properties if the
        # FIXME: properties have not been displayed yet they are null.
        if properties is None:
            properties = self._getProperties(driver, group)
            self._drivers.get(driver).get(self._groups)[group] = properties
        properties[self._classpath] = path

    def setDriverVersions(self, *args):
        Thread(target=self._setDriverVersions, args=args).start()

# TabModel private getter methods
    def _getGroup(self, driver, group):
        return self._drivers.get(driver).get(self._groups).get(group)

    def _getGroups(self, driver, name):
        groups = {}
        for name in driver.getElementNames():
            group = driver.getByName(name)
            if not group or isinstance(group, str):
                continue
            groups[group.Name] = None
        return groups

    def _getDriverVersion(self, protocol):
        with self._lock:
            if protocol in self._versions:
                version = self._versions[protocol]
            else:
                version = self._getDefaultVersion()
            return version

    def _isProtocolValid(self, subprotocol):
        protocol = self._getProtocol(subprotocol)
        protocols = (k for k, v in self._drivers.items() if v is not None)
        return len(subprotocol) > 0 and protocol not in protocols

    def _isJavaClassValid(self, javaclass):
        return len(javaclass) > 0 and \
               '.' in javaclass and \
               not javaclass.endswith('.') and \
               not javaclass.startswith('.')

    def _getTypeIndex(self, value):
        cls = type(value)
        if value is not None and cls in self._types:
            enabled = False
            index = self._types.index(cls)
        else:
            enabled = True
            index = self._types.index(self._default)
        return index, enabled

    def _getPropertyNames(self, driver, group):
        groups = self._drivers.get(driver).get(self._groups)
        properties = groups.get(group)
        if properties is None:
            properties = self._getProperties(driver, group)
            groups[group] = properties
        return tuple((k for k, v in properties.items() if v is not None))

    def _saveArchives(self):
        if self._folder is None:
            return False
        sf = getSimpleFile(self._ctx)
        source = self._url.Main + self._tmp
        target = '%s/%s/' % (self._url.Main, g_folder)
        reboot = self._copyArchive(sf, source, target)
        self._deleteFolderContent(sf, source)
        self._url = None
        return reboot

    def _copyArchive(self, sf, source, target):
        count = 0
        for path in sf.getFolderContents(source, True):
            url = getUrl(self._ctx, path)
            sf.copy(path, target + url.Name)
            count += 1
        return count > 0

    def _getUnoType(self, value):
        cls = type(value)
        if cls in self._types:
            index = self._types.index(cls)
        else:
            index = self._types.index(self._default)
        return self._unotypes[index]

    def _isDriverUpdatable(self, driver):
        return driver != self._root

    def _getDriverConfigurations(self):
        drivers = {}
        config = self._config.getByName('Installed')
        for name in config.getElementNames():
            driver = config.getByName(name)
            if self._isRootDriver(driver, name):
                drivers[name] = {self._groups: self._getGroups(driver, name),
                                 self._name: driver.getByName(self._name)}
        return drivers

    def _isRootDriver(self, driver, name):
        return name == self._root or self._isChildDriver(driver)

    def _isChildDriver(self, driver):
        property = 'ParentURLPattern'
        return driver.hasByName(property) and driver.getByName(property) == self._root

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
        url = self._getExtensionUrl(sf)
        multi = len(archives) > 1
        subprotocol = self._getSubProtocol(driver)
        tmp = self._getTmpPath(url.Main, multi, subprotocol)
        if multi:
            # XXX: If we have multiple archives, all the contents of the folder will be
            # XXX: added to the Java ClassLoader so we need to clean that folder first.
            self._deleteFolderContent(sf, tmp)
        for archive in archives:
            source = getUrl(self._ctx, archive)
            target = '%s/%s' % (tmp, source.Name)
            sf.copy(source.Main, target)
        return self._getJavaClassPath(url, multi, subprotocol, source.Name)

    def _getUrl(self):
        if self._url is None:
            self._url = getUrl(self._ctx, getResourceLocation(self._ctx, g_identifier))
        return self._url

    def _getExtensionUrl(self, sf):
        url = self._getUrl()
        if self._folder is None:
            self._deleteFolderContent(sf, url.Main + self._tmp)
            self._folder = url.Main.split('/')[-2]
        return url

    def _getTmpPath(self, path, multi, subprotocol):
        tmp = path + self._tmp
        return '%s/%s' % (tmp, subprotocol) if multi else tmp

    def _getJavaClassPath(self, url, multi, subprotocol, name):
        path = '%s/%s/%s/%s/' % (self._package, self._folder, url.Name, g_folder)
        return path + subprotocol if multi else path + name

    def _getProtocol(self, subprotocol):
        return '%s:*' % self._protocol + subprotocol

    def _getSubProtocol(self, driver):
        return driver.split(':')[1] if driver else ''

    def _getDefaultVersion(self):
        resource = self._resources.get('Version1')
        return self._resolver.resolveString(resource)

    def _getVersion(self, version):
        resource = self._resources.get('Version2')
        return self._resolver.resolveString(resource) % version

# TabModel private setter methods
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
        if not config.hasByName(name):
            config.insertByName(name, config.createInstance())
        if config.getByName(name) != value:
            config.replaceByName(name, value)

    def _deleteFolderContent(self, sf, folder):
        if sf.exists(folder) and sf.isFolder(folder):
            sf.kill(folder)
        sf.createFolder(folder)

    def _setDriverVersions(self, apilevel, update):
        driver = None
        try:
            versions = {}
            default = self._getDefaultVersion()
            config = self._config.getByName('Installed')
            name = 'Properties/InMemoryDataBase/Value'
            pool = createService(self._ctx, 'com.sun.star.sdbc.ConnectionPool')
            driver = createService(self._ctx, g_service)
            for protocol in config.getElementNames():
                setting = config.getByName(protocol)
                if setting.hasByHierarchicalName(name):
                    url = setting.getByHierarchicalName(name)
                    if url:
                        try:
                            connection = driver.connect(self._protocol + url, ())
                            version = connection.getMetaData().getDriverVersion()
                            versions[protocol] = self._getVersion(version)
                            connection.close()
                            continue
                        except UnoException as e:
                            # If the connection fails, the error is already logged
                            pass
                versions[protocol] = default
            with self._lock:
                self._versions = versions
            update(versions)
        except UnoException as e:
            # If the driver fails, the error is already logged
            pass

