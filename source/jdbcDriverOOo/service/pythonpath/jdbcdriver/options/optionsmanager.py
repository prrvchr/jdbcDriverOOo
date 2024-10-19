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

from com.sun.star.ui.dialogs.ExecutableDialogResults import OK

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from .optionsmodel import OptionsModel

from .optionsview import OptionsView

from .optionshandler import OptionsListener
from .optionshandler import TabListener
from .optionshandler import TabHandler

from ..option import OptionManager

from .properties import PropertiesManager
from .properties import WindowHandler

from .dialog import DriverView
from .dialog import DialogHandler

from ..unotool import getFilePicker
from ..unotool import getSimpleFile
from ..unotool import getUrl

from ..dbconfig import g_jar

from threading import Condition
import traceback


class OptionsManager():
    def __init__(self, ctx, window):
        self._ctx = ctx
        self._lock = Condition()
        self._disposed = False
        self._dialog = None
        self._listener = TabListener(self)
        self._model = OptionsModel(ctx, self._lock)
        window.addEventListener(OptionsListener(self))
        self._view = OptionsView(ctx, window, TabHandler(self), self._listener, OptionsManager._restart, *self._model.getTabTitles())
        self._tab1 = OptionManager(ctx, self._view.getTab1(), 0, 'Driver')
        self._properties = PropertiesManager(ctx, self._view.getTab2(), self)
        self._view.setDrivers(self._model.getDrivers())
        print("OptionsManager.__init__()")
        self._disabled = False
        self._view.selectDriver(0)

    _restart = False

    def dispose(self):
        self._tab1.dispose()
        self._properties.dispose()
        self._view.dispose()
        self._disposed = True

    # TODO: One shot disabler handler
    def isHandlerEnabled(self):
        if self._disabled:
            self._disabled = False
            return False
        return True

# OptionsManager setter methods
    def activateTab2(self):
        self._view.removeTabListener(self._listener)
        self._model.setDriverVersions(self._tab1.getDriverService(), self.updateView)

    def saveSetting(self):
        saved = self._tab1.saveSetting()
        if self._model.saveSetting() or saved:
            print("OptionsManager.saveSetting() ***************************")
            OptionsManager._restart = True
            self._view.setRestart(True)

    def loadSetting(self):
        self._tab1.loadSetting()
        self._model.loadSetting()
        self._view.setDrivers(self._model.getDrivers())

    def updateView(self, versions):
        with self._lock:
            self.updateVersion(versions)

    def updateVersion(self, versions):
        with self._lock:
            if not self._disposed:
                driver = self._view.getDriver()
                if driver in versions:
                    self._view.setVersion(versions[driver])


    # Option2Dialog.xdl handler entries
    def setDriver(self, driver):
        print("OptionsManager.setDriver() Driver: %s - disabled: %s" % (driver, self._disabled))
        if self._disabled:
            self._disabled = False
            return
        group = self._view.getGroup()
        protocol, name, groups, version, updatable = self._model.getDriver(driver)
        self._view.setDriver(protocol, name, groups, version, updatable)
        index = groups.index(group) if group in groups else 0
        self._disabled = False
        self._view.selectGroup(index)

    def editDriver(self):
        self._view.enableDriverName(True)
        self._view.enableConfirm(False)
        self._view.setStep(2, OptionsManager._restart)

    def addDriver(self):
        print("OptionsManager.addDriver() 1")
        self._model.clearJarPath()
        self._dialog = DriverView(self._ctx, self._view.getTab2(), DialogHandler(self), 'DriverDialog')
        print("OptionsManager.addDriver() 2")
        if self._dialog.execute() == OK:
            subprotocol = self._dialog.getProtocol()
            name = self._dialog.getDriver()
            javaclass = self._dialog.getJavaClass()
            driver = self._model.addDriver(subprotocol, name, javaclass)
            drivers = self._model.getDrivers()
            self._view.setDrivers(drivers)
            self._view.selectDriver(drivers.index(driver))
            print("OptionsManager.addDriver() 3")
        self._dialog.dispose()
        self._dialog = None
        print("OptionsManager.addDriver() 4")

    def removeDriver(self):
        self._model.removeDriver(self._view.getDriver())
        self._view.setDrivers(self._model.getDrivers())
        self._view.selectDriver(0)

    def confirmDriver(self):
        driver = self._view.getDriver()
        name = self._view.getDriverName()
        self._model.updateDriverName(driver, name)
        self._view.enableDriverName(False)
        self._view.setStep(1, OptionsManager._restart)

    def cancelDriver(self):
        driver = self._view.getDriver()
        self._view.setDriverName(self._model.getDriverName(driver))
        self._view.enableDriverName(False)
        self._view.setStep(1, OptionsManager._restart)

    def updateDriverName(self, name):
        self._view.enableConfirm(self._model.isDriverNameValid(name))

    def updateArchive(self):
        archives = self._getArchives()
        if archives:
            driver = self._view.getDriver()
            self._model.updateArchive(driver, archives)

    def setGroup(self, group):
        print("OptionsManager.setGroup() Group: %s - disabled: %s" % (group, self._disabled))
        if self._disabled:
            self._disabled = False
            return
        driver = self._view.getDriver()
        property = self._properties.getPropertiesItem()
        self._properties.setProperties(*self._model.getProperties(driver, group, property))

    # PropertiesWindow.xdl handler entries
    def setProperty(self, property):
        print("OptionsManager.setProperty() Property: %s - disabled: %s" % (property, self._disabled))
        if self._disabled:
            self._disabled = False
            return
        driver = self._view.getDriver()
        group = self._view.getGroup()
        self._properties.setProperty(*self._model.getProperty(driver, group, property))

    def editProperty(self):
        self._model.setNew(False)
        self._properties.editProperty()

    def addProperty(self):
        self._model.setNew(True)
        self._properties.addProperty()

    def removeProperty(self):
        driver = self._view.getDriver()
        group = self._view.getGroup()
        property = self._properties.getPropertiesItem()
        self._model.removeProperty(driver, group, property)

    def setPropertyName(self, name):
        driver = self._view.getDriver()
        group = self._view.getGroup()
        self._properties.enableConfirm(self._model.isPropertyNameValid(driver, group, name))

    def confirmProperty(self):
        driver = self._view.getDriver()
        group = self._view.getGroup()
        new = self._properties.getPropertyName()
        if self._model.isNew():
            properties = self._model.addProperty(driver, group, new)
            index = len(properties) - 1
        else:
            old = self._properties.getPropertiesItem()
            properties = self._model.editProperty(driver, group, new, old)
            index = properties.index(new)
        self._properties.setProperties(properties, index, True)
        self._properties.exitEdit()

    def cancelProperty(self):
        self._properties.exitEdit()

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

# OptionsManager private methods
    def _getArchives(self):
        archives = ()
        fp = getFilePicker(self._ctx)
        fp.setDisplayDirectory(self._model.getPath())
        fp.setMultiSelectionMode(True)
        fp.appendFilter(g_jar, g_jar)
        fp.setCurrentFilter(g_jar)
        if fp.execute() == OK:
            archives = fp.getSelectedFiles()
        return archives

