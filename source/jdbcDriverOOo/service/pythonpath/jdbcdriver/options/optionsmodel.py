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
        self._propertymode = 0
        self._value = None
        self._valuemode = 0
        self._protocol = 'xdbc:'
        self._root = self._protocol + '*'
        self._path = None
        self._tmp = '/tmp'
        self._origin = '%origin%'
        self._jarpath = None
        self._versions = {}
        self._groups = 'Groups'
        self._name = 'DriverTypeDisplayName'
        self._class = 'JavaDriverClass'
        self._classpath = 'JavaDriverClassPath'
        path = 'org.openoffice.Office.DataAccess.Drivers'
        self._config = getConfiguration(ctx, path, True)
        # FIXME: The supported types must follow the display order of the Property Type ListBox
        self._types = (bool, str, tuple)
        self._default = str
        self._resolver = getStringResource(ctx, g_identifier, 'dialogs', 'Option2Dialog')
        self._resources = {'TabTitle1' : 'Option2Dialog.Tab1.Title',
                           'TabTitle2' : 'Option2Dialog.Tab2.Title',
                           'Version1'  : 'Option2Dialog.Label6.Label.0',
                           'Version2'  : 'Option2Dialog.Label6.Label.1'}
        self._logger = getLogger(ctx, 'Driver', g_basename)
        self._logger.logprb(INFO, 'OptionsDialog', '__init__()', 101)
        self._drivers = self._getDriverConfigurations()

    _path = None

# OptionsModel getter methods
    def saveSetting(self):
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
        version = self.getDriverVersion(name)
        updatable = self._isDriverUpdatable(name)
        return subprotocol, driver.get(self._name), groups, version, updatable

    def getDriverName(self, driver):
        return self._drivers.get(driver, {}).get(self._name, '')

    def getGroups(self, name):
        return tuple(self._drivers.get(name).get(self._groups).keys())

    def getProperties(self, driver, group):
        properties = self._getPropertyNames(driver, group)
        updatable = self._isDriverUpdatable(driver)
        return properties, updatable

    def getProperty(self, driver, group, property):
        value = self._drivers.get(driver).get(self._groups).get(group).get(property)
        updatable = self._isDriverUpdatable(driver) and self._isPropertyUpdatable(property)
        return value, updatable, *self._getTypeIndex(value)

    def getDisplayDirectory(self):
        # XXX: We want to keep the last accessed FilePicker path for the LibreOffice session
        if OptionsModel._path is None:
            OptionsModel._path = getPathSettings(self._ctx).Work
        return OptionsModel._path

    def getType(self, index):
        return self._types[index]

    def isNewProperty(self):
        return self._propertymode == 2

    def isEditValue(self):
        return self._valuemode != 0

    def isNewValue(self):
        return self._valuemode == 2

    def getDefaultValue(self):
        return self._types.index(self._default), ''

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

    def editProperty(self, driver, group, new, old, value):
        properties = self._drivers.get(driver).get(self._groups).get(group)
        properties[new] = value
        properties[old] = None
        return tuple((k for k, v in properties.items() if v is not None))

    def addProperty(self, driver, group, property, value):
        properties = self._drivers.get(driver).get(self._groups).get(group)
        properties[property] = value
        return tuple((k for k, v in properties.items() if v is not None))

    def getDriverVersion(self, protocol):
        with self._lock:
            if protocol in self._versions:
                version = self._versions[protocol]
            else:
                version = self._getDefaultVersion()
            return version

    def getDefaultPropertyValue(self, index):
        cls = self._types[index]
        if cls == bool:
            value = False
        elif cls == tuple:
            value = ()
        else:
            value = ''
        return value

    def addPropertyValue(self, driver, group, property, value):
        values = list(self._drivers.get(driver).get(self._groups).get(group).get(property))
        values.append(value)
        newvalues = tuple(values)
        self._drivers.get(driver).get(self._groups).get(group)[property] = newvalues
        return newvalues

    def removePropertyValue(self, driver, group, property, index):
        values = list(self._drivers.get(driver).get(self._groups).get(group).get(property))
        values.pop(index)
        newvalues = tuple(values)
        self._drivers.get(driver).get(self._groups).get(group)[property] = newvalues
        return newvalues

    def editPropertyValue(self, driver, group, property, value, index):
        values = list(self._drivers.get(driver).get(self._groups).get(group).get(property))
        values[index] = value
        newvalues = tuple(values)
        self._drivers.get(driver).get(self._groups).get(group)[property] = newvalues
        return newvalues

    def getAddPropertyValue(self):
        return self._value if self.isNewProperty() else None

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

    def setPropertyMode(self, mode):
        self._propertymode = mode

    def setAddPropertyValue(self, value):
        self._value = value

    def setValueMode(self, mode):
        self._valuemode = mode

    def setPropertyValue(self, driver, group, property, value):
        if self._propertymode:
            print("OptionsModel.setPropertyValue() *********************************")
            return
        self._drivers.get(driver).get(self._groups).get(group)[property] = value

    def clearJarPath(self):
        self._jarpath = None

    def removeDriver(self, driver):
        self._drivers[driver] = None

    def setArchive(self, subprotocol, archives):
        protocol = self._getProtocol(subprotocol)
        self._jarpath = self._updateArchive(protocol, archives)

    def setDisplayDirectory(self, path):
        OptionsModel._path = path

    def updateArchive(self, driver, archives):
        # XXX: Jar files have been copied, we need to save the configuration
        properties = self._drivers.get(driver).get(self._groups).get('Properties')
        path = self._updateArchive(driver, archives)
        print("OptionsModel.updateArchive() path: %s - Properties: %s" % (path, properties))
        properties[self._classpath] = path

    def setDriverVersions(self, *args):
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

    def _getTypeIndex(self, value):
        enabled = value is None
        if enabled:
            index = self._types.index(self._default)
        else:
            index = self._types.index(type(value))
        return index, enabled

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
        #return property != self._classpath
        return True

    def _getConfiguration(self):
        path = 'org.openoffice.Office.DataAccess.Drivers'
        return getConfiguration(self._ctx, path, True)

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
        path = self._getExtensionPath(sf)
        multi = len(archives) > 1
        subprotocol = self._getSubProtocol(driver)
        tmp = self._getTmpPath(path, multi, subprotocol)
        if multi:
            # XXX: If we have multiple archives, all the contents of the folder will be
            # XXX: added to the Java ClassLoader so we need to clean that folder first.
            self._deleteFolderContent(sf, tmp)
        for archive in archives:
            url = getUrl(self._ctx, archive)
            target = '%s/%s' % (tmp, url.Name)
            sf.copy(url.Main, target)
        return self._getTargetPath(path, multi, subprotocol, url)

    def _getExtensionPath(self, sf):
        if self._path is None:
            self._path = getResourceLocation(self._ctx, g_identifier)
            self._deleteFolderContent(sf, self._path + self._tmp)
        return self._path

    def _getTmpPath(self, path, multi, subprotocol):
        tmp = path + self._tmp
        return '%s/%s' % (tmp, subprotocol) if multi else tmp

    def _getTargetPath(self, path, multi, subprotocol, url):
        target = '%s/%s/' % (path, g_folder)
        return target + subprotocol if multi else target + url.Name

    def _getProtocol(self, subprotocol):
        return '%s:*' % self._protocol + subprotocol

    def _getSubProtocol(self, driver):
        return driver.split(':')[1] if driver else ''

    def _getPropertyValue(self, driver, group, property):
        return self._drivers.get(driver).getByName(group).getByName(property).Value

    def _getDriverVersion(self, driver, subprotocol, default):
        try:
            url = self._protocol + subprotocol
            connection = driver.connect(url, ())
            version = self._getVersion(connection.getMetaData().getDriverVersion())
            connection.close()
            return version
        except UnoException as e:
            self._logger.logprb(SEVERE, 'OptionsDialog', '_getDriverVersion()', 111, e.Message)
        except Exception as e:
            self._logger.logprb(SEVERE, 'OptionsDialog', '_getDriverVersion()', 112, e, traceback.format_exc())
        return default

    def _getTabTitle(self, tab):
        resource = self._resources.get('TabTitle%s' % tab)
        return self._resolver.resolveString(resource)

    def _getDefaultVersion(self):
        resource = self._resources.get('Version1')
        return self._resolver.resolveString(resource)

    def _getVersion(self, version):
        resource = self._resources.get('Version2')
        return self._resolver.resolveString(resource) % version

# OptionsModel private setter methods
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

    def _setDriverVersions(self, name, update):
        versions = {}
        default = self._getDefaultVersion()
        config = self._config.getByName('Installed')
        property = 'Properties/InMemoryDataBase/Value'
        service = createService(self._ctx, name)
        for protocol in config.getElementNames():
            driver = config.getByName(protocol)
            if driver.hasByHierarchicalName(property):
                url = driver.getByHierarchicalName(property)
                if len(url) > 0:
                    versions[protocol] = self._getDriverVersion(service, url, default)
                    continue
            versions[protocol] = default
        with self._lock:
            self._versions = versions
        update(versions)

