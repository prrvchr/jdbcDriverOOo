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

import unohelper

from com.sun.star.awt import XCallback

from com.sun.star.ui.dialogs.ExecutableDialogResults import OK

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from .tabmodel import TabModel
from .tabview import TabView
from .tabhandler import TabHandler

from .properties import PropertiesManager
from .properties import WindowHandler

from .dialog import DriverView
from .dialog import DialogHandler

from ...unotool import getFilePicker
from ...unotool import getSimpleFile
from ...unotool import getUrl

from ...dbconfig import g_jar

from threading import RLock
import traceback


class TabManager(unohelper.Base,
                 XCallback):
    def __init__(self, ctx, window, restart):
        self._ctx = ctx
        self._lock = RLock()
        self._disposed = False
        self._dialog = None
        xdl = 'Option2Dialog'
        self._model = TabModel(ctx, self._lock, restart, xdl)
        self._view = TabView(ctx, window, TabHandler(self), restart, xdl)
        self._properties = PropertiesManager(ctx, self._view.getWindow(), self)
        self._view.setDrivers(self._model.getDrivers())
        # FIXME: If we changed the driver selection then the
        # FIXME: handler will fire twice, we disable the first one
        self._disabled = True
        self._view.selectDriver(0)

# XCallback
    def notify(self, versions):
        if not self._disposed:
            driver = self._view.getDriver()
            if driver in versions:
                self._view.setVersion(versions[driver])

# TabManager setter methods
    def dispose(self):
        with self._lock:
            self._properties.dispose()
            self._view.dispose()
            self._disposed = True

    def setDriverVersions(self, apilevel):
        self._model.setDriverVersions(apilevel, self)

    def setRestart(self, state):
        self._model.setRestart(state)
        self._view.setRestart(state)

    def loadSetting(self):
        self._model.loadSetting()
        self._view.setDrivers(self._model.getDrivers())

# TabManager getter methods
    def saveSetting(self):
        return self._model.saveSetting()

    # Option2Dialog.xdl handler entries
    def setDriver(self, driver):
        # FIXME: If we changed the driver selection then the handler
        # FIXME: will fire twice, the first one is ignored
        if self._disabled:
            self._disabled = False
            return
        self._switchDriverEditMode(False)
        group = self._view.getGroup()
        protocol, name, groups, version, updatable = self._model.getDriver(driver)
        self._view.setDriver(protocol, name, groups, version, updatable)
        index = groups.index(group) if group in groups else 0
        self._disabled = True
        self._view.selectGroup(index)

    def editDriver(self):
        self._view.enableConfirm(False)
        self._switchDriverEditMode(True)

    def addDriver(self):
        self._model.clearJarPath()
        self._dialog = DriverView(self._ctx, self._view.getTab2(), DialogHandler(self), 'DriverDialog')
        if self._dialog.execute() == OK:
            subprotocol = self._dialog.getProtocol()
            name = self._dialog.getDriver()
            javaclass = self._dialog.getJavaClass()
            driver = self._model.addDriver(subprotocol, name, javaclass)
            drivers = self._model.getDrivers()
            self._view.setDrivers(drivers)
            self._view.selectDriver(drivers.index(driver))
        self._dialog.dispose()
        self._dialog = None

    def removeDriver(self):
        self._model.removeDriver(self._view.getDriver())
        self._view.setDrivers(self._model.getDrivers())
        self._view.selectDriver(0)

    def confirmDriver(self):
        driver = self._view.getDriver()
        name = self._view.getDriverName()
        self._model.updateDriverName(driver, name)
        self._switchDriverEditMode(False)

    def cancelDriver(self):
        driver = self._view.getDriver()
        self._view.setDriverName(self._model.getDriverName(driver))
        self._model.cancelDriver()
        # XXX: The Cancel button cannot have focus and not be displayed
        self._view.setDefaultFocus()
        self._switchDriverEditMode(False)

    def updateDriverName(self, name):
        self._view.enableConfirm(self._model.isDriverNameValid(name))

    def updateArchive(self):
        archives = self._getArchives()
        if archives:
            driver = self._view.getDriver()
            self._model.updateArchive(driver, archives)

    def viewArchive(self):
        fp = getFilePicker(self._ctx)
        fp.setDisplayDirectory(self._model.getArchivePath())
        fp.execute()
        fp.dispose()

    def setGroup(self, group):
        # FIXME: If we changed the group selection then the handler
        # FIXME: will fire twice, the first one is ignored
        if self._disabled:
            self._disabled = False
            return
        self._properties.exitEditProperty()
        driver = self._view.getDriver()
        property = self._properties.getPropertiesItem()
        properties, updatable = self._model.getProperties(driver, group)
        self._properties.setProperties(properties, updatable)
        if properties:
            index = properties.index(property) if property in properties else 0
            self._disabled = True
            self._properties.selectProperty(index)

    # PropertiesWindow.xdl handler entries
    def setProperty(self, property):
        # FIXME: If we changed the property selection then the handler
        # FIXME: will fire twice, the first one is ignored
        if self._disabled:
            self._disabled = False
            return
        driver = self._view.getDriver()
        group = self._view.getGroup()
        # FIXME: If we are in edit value mode, we must exit it
        if self._model.isEditValue():
            self._properties.cancelValue()
        value, updatable, index, enable = self._model.getProperty(driver, group, property)
        # FIXME: The handler should only be disabled if the type is actually changed.
        self._disabled = index != self._properties.getTypesIndex()
        self._properties.selectType(index)
        self._properties.enableTypes(enable)
        self._setPropertyValue(value, updatable)

    def editProperty(self):
        self._model.setPropertyMode(1)
        self._properties.editProperty()

    def addProperty(self):
        self._model.setAddPropertyValue(self._getPropertyValue())
        self._model.setPropertyMode(2)
        self._properties.addProperty()
        value = self._model.getDefaultPropertyValue(self._properties.getTypesIndex())
        self._setPropertyValue(value)

    def removeProperty(self):
        driver = self._view.getDriver()
        group = self._view.getGroup()
        property = self._properties.getPropertiesItem()
        properties = self._model.removeProperty(driver, group, property)
        self._properties.setProperties(properties, True)
        if properties:
            self._disabled = True
            self._properties.selectProperty(0)

    def setPropertyName(self, name):
        driver = self._view.getDriver()
        group = self._view.getGroup()
        self._properties.enableConfirm(self._model.isPropertyNameValid(driver, group, name))

    def confirmProperty(self):
        driver = self._view.getDriver()
        group = self._view.getGroup()
        new = self._properties.getPropertyName()
        value = self._getPropertyValue()
        if self._model.isNewProperty():
            properties = self._model.addProperty(driver, group, new, value)
            index = len(properties) - 1
            self._properties.enableTypes(False)
        else:
            old = self._properties.getPropertiesItem()
            properties = self._model.editProperty(driver, group, new, old, value)
            index = properties.index(new)
        self._model.setPropertyMode(0)
        self._properties.exitEditProperty()
        self._properties.setProperties(properties, True)
        self._disabled = True
        self._properties.selectProperty(index)

    def cancelProperty(self):
        value = self._model.getAddPropertyValue()
        self._model.setPropertyMode(0)
        # XXX: The Cancel button cannot have focus and not be displayed
        self._properties.setPropertyFocus()
        self._properties.exitEditProperty()
        self._properties.enableTypes(False)
        if value is not None:
            # FIXME: If we cancel the addition of a new property then
            # FIXME: we must also cancel the editing of any values.
            if self._model.isEditValue():
                self._properties.cancelValue()
            self._setPropertyValue(value)

    # PropertyWindow.xdl handler entries
    def setType(self, index):
        # FIXME: If we changed the type selection then the handler
        # FIXME: will fire twice, the first one is ignored
        if self._disabled:
            self._disabled = False
            return
        self._properties.setType(index)

    def setPropertyValue(self, value):
        driver = self._view.getDriver()
        group = self._view.getGroup()
        property = self._properties.getPropertiesItem()
        self._model.setPropertyValue(driver, group, property, value)

    def editValue(self):
        self._model.setValueMode(1)
        self._properties.editValue()

    def addValue(self):
        self._model.setValueMode(2)
        self._properties.addValue()

    def removeValue(self):
        driver = self._view.getDriver()
        group = self._view.getGroup()
        property = self._properties.getPropertiesItem()
        index = self._properties.getValuesIndex()
        values = self._model.removePropertyValue(driver, group, property, index)
        self._setPropertyValue(values)
        if values:
            self._properties.selectPropertyValue(max(0, index - 1))

    def confirmValue(self):
        value =  self._properties.getPropertyValue()
        # FIXME: If we edit the value of a new property, we save the result
        # FIXME: of this edition not in the model but only on the view.
        if self._model.isNewProperty():
            values = list(self._properties.getListBoxValue())
            if self._model.isNewValue():
                values.append(value)
                index = len(values) - 1
            else:
                index = self._properties.getValuesIndex()
                values[index] = value
            values = tuple(values)
        else:
            driver = self._view.getDriver()
            group = self._view.getGroup()
            property = self._properties.getPropertiesItem()
            if self._model.isNewValue():
                values = self._model.addPropertyValue(driver, group, property, value)
                index = len(values) - 1
            else:
                index = self._properties.getValuesIndex()
                values = self._model.editPropertyValue(driver, group, property, value, index)
        self._setPropertyValue(values)
        if values:
            self._properties.selectPropertyValue(index)
        self._properties.cancelValue()

    def cancelValue(self):
        # XXX: The Cancel button cannot have focus and not be displayed
        self._properties.setValueFocus()
        self._properties.cancelValue()

    # DriverDialog.xdl handler entries
    def setSubProtocol(self, subprotocol):
        name = self._dialog.getDriver()
        javaclass = self._dialog.getJavaClass()
        self._dialog.enableConfirm(self._model.isDriverValid(subprotocol, name, javaclass))

    def setDriverName(self, name):
        subprotocol = self._dialog.getProtocol()
        javaclass = self._dialog.getJavaClass()
        self._dialog.enableConfirm(self._model.isDriverValid(subprotocol, name, javaclass))

    def setJavaClass(self, javaclass):
        subprotocol = self._dialog.getProtocol()
        name = self._dialog.getDriver()
        self._dialog.enableConfirm(self._model.isDriverValid(subprotocol, name, javaclass))

    def setArchive(self):
        archives = self._getArchives()
        if archives:
            subprotocol = self._dialog.getProtocol()
            name = self._dialog.getDriver()
            javaclass = self._dialog.getJavaClass()
            self._model.setArchive(subprotocol, archives)
            self._dialog.enableConfirm(self._model.isDriverValid(subprotocol, name, javaclass))

# TabManager private getter methods
    def _getPropertyValue(self):
        cls = self._model.getType(self._properties.getTypesIndex())
        if cls == bool:
            value = self._properties.getCheckBoxValue()
        elif cls == int:
            value = self._properties.getNumFieldValue()
        elif cls == tuple:
            value = self._properties.getListBoxValue()
        else:
            value = self._properties.getTextFieldValue()
        return value

    def _getArchives(self):
        archives = ()
        fp = getFilePicker(self._ctx)
        fp.setDisplayDirectory(self._model.getDisplayDirectory())
        fp.setMultiSelectionMode(True)
        fp.appendFilter(g_jar, g_jar)
        fp.setCurrentFilter(g_jar)
        if fp.execute() == OK:
            archives = fp.getSelectedFiles()
        # XXX: We want to be able to preserve the last path used by the FilePicker
        self._model.setDisplayDirectory(fp.getDisplayDirectory())
        fp.dispose()
        return archives

# TabManager private setter methods
    def _switchDriverEditMode(self, enabled):
        self._view.enableDriverName(enabled)
        step = 2 if enabled else 1
        self._view.setStep(step, self._model.getRestart())

    def _setPropertyValue(self, value, updatable=True):
        cls = type(value)
        if cls == bool:
            self._properties.setCheckBoxValue(value, updatable)
        elif cls == int:
            self._properties.setNumFieldValue(value, updatable)
        elif cls == tuple:
            index = self._properties.getValuesIndex()
            self._properties.setListBoxValue(value, updatable)
            if value:
                index = 0 if index > len(value) else max(0, index)
                self._properties.selectPropertyValue(index)
        else:
            self._properties.setTextFieldValue(value, updatable)

