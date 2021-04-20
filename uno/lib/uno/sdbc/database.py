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

import uno
import unohelper

from com.sun.star.document import XDocumentEventBroadcaster
from com.sun.star.document import XDocumentRecovery
from com.sun.star.document import XEventsSupplier
from com.sun.star.document import XStorageBasedDocument
from com.sun.star.document import XDocumentSubStorageSupplier

from com.sun.star.frame import XLoadable
from com.sun.star.frame import XModel2
from com.sun.star.frame import XTitle
from com.sun.star.frame import XTitleChangeBroadcaster
from com.sun.star.frame import XStorable
from com.sun.star.frame import XUntitledNumbers

from com.sun.star.lang import XServiceInfo

from com.sun.star.sdb import XOfficeDatabaseDocument

from com.sun.star.script.provider import XScriptProviderSupplier

from com.sun.star.ui import XUIConfigurationManagerSupplier

from com.sun.star.uno import XWeak
from com.sun.star.uno import XAdapter

from com.sun.star.util import XModifiable
from com.sun.star.util import XCloseable

from com.sun.star.view import XPrintable

from ..unolib import PropertySet

from ..unotool import getProperty

import traceback


class DataBase(unohelper.Base,
               XCloseable,
               XDocumentEventBroadcaster,
               XDocumentRecovery,
               XEventsSupplier,
               XLoadable,
               XModel2,
               XModifiable,
               XOfficeDatabaseDocument,
               XPrintable,
               XScriptProviderSupplier,
               XServiceInfo,
               XStorable,
               XStorageBasedDocument,
               XTitle,
               XTitleChangeBroadcaster,
               XUIConfigurationManagerSupplier,
               XUntitledNumbers,
               XWeak,
               PropertySet):
    def __init__(self, database, datasource):
        self._database = database
        self._datasource = datasource

    @property
    def Args(self):
        return self.getArgs()
    @Args.setter
    def Args(self, arguments):
        self.setArgs(arguments)
    @property
    def AvailableViewControllerNames(self):
        return self.getAvailableViewControllerNames()
    @property
    def CurrentController(self):
        return self.getCurrentController()
    @CurrentController.setter
    def CurrentController(self, controller):
        self.setCurrentController(controller)
    @property
    def CurrentSelection(self):
        return self.getCurrentSelection()
    @property
    def DataSource(self):
        # TODO: This wrapping is only there for the following lines:
        return self._datasource
    @property
    def DocumentStorage(self):
        return self.getDocumentStorage()
    @property
    def DocumentSubStoragesNames(self):
        return self.getDocumentSubStoragesNames()
    @property
    def Events(self):
        return self.getEvents()
    @property
    def FormDocuments(self):
        return self.getFormDocuments()
    @property
    def ImplementationId(self):
        return self.getImplementationId()
    @property
    def ImplementationName(self):
        return self.getImplementationName()
    @property
    def Location(self):
        return self.getLocation()
    @property
    def Modified(self):
        return self.getModified()
    @Modified.setter
    def Modified(self, modified):
        self.setModified(modified)
    @property
    def Printer(self):
        return self.getPrinter()
    @Printer.setter
    def Printer(self, printer):
        self.setPrinter(printer)
    @property
    def ReportDocuments(self):
        return self.getReportDocuments()
    @property
    def ScriptProvider(self):
        return self.getScriptProvider()
    @property
    def SupportedServiceNames(self):
        return self.getSupportedServiceNames()
    @property
    def Title(self):
        return self.getTitle()
    @Title.setter
    def Title(self, title):
        self.setTitle(title)
    @property
    def UIConfigurationManager(self):
        return self.getUIConfigurationManager()
    @property
    def URL(self):
        return self.getURL()
    @property
    def UntitledPrefix(self):
        return self.getUntitledPrefix()

# XCloseable
    def close(self, ownership):
        self._database.close(ownership)

# XCloseBroadcaster <- XCloseable
    def addCloseListener(self, listener):
        self._database.addCloseListener(listener)
    def removeCloseListener(self, listener):
        self._database.removeCloseListener(listener)

# XComponent <- XModel <- XModel2
    def addEventListener(self, listener):
        self._database.addEventListener(listener)
    def dispose(self):
        self._database.dispose()
    def removeEventListener(self, listener):
        self._database.removeEventListener(listener)

# XDocumentEventBroadcaster
    def addDocumentEventListener(self, listener):
        self._database.addDocumentEventListener(listener)
    def removeDocumentEventListener(self, listener):
        self._database.removeDocumentEventListener(listener)
    def notifyDocumentEvent(self, event, controller, supplement):
        self._database.notifyDocumentEvent(event, controller, supplement)

# XDocumentRecovery
    def recoverFromFile(self, location, salvaged, descriptor):
        self._database.recoverFromFile(location, salvaged, descriptor)
    def storeToRecoveryFile(self, location, descriptor):
        self._database.storeToRecoveryFile(location, descriptor)
    def wasModifiedSinceLastSave(self):
        return self._database.wasModifiedSinceLastSave()

# XDocumentSubStorageSupplier <- XOfficeDatabaseDocument
    def getDocumentSubStorage(self, storage, mode):
        return self._database.getDocumentSubStorage(storage, mode)
    def getDocumentSubStoragesNames(self):
        return self._database.getDocumentSubStoragesNames()

# XEventsSupplier
    def getEvents(self):
        return self._database.getEvents()

# XFormDocumentsSupplier <- XOfficeDatabaseDocument
    def getFormDocuments(self):
        return self._database.getFormDocuments()

# XLoadable
    def initNew(self):
        return self._database.initNew()
    def load(self, arguments):
        self._database.load(arguments)

# XModel <- XModel2
    def attachResource(self, url, arguments):
        return self._database.attachResource(url, arguments)
    def connectController(self, controller):
        self._database.connectController(controller)
    def disconnectController(self, controller):
        self._database.disconnectController(controller)
    def getArgs(self):
        return self._database.getArgs()
    def getCurrentController(self):
        return self._database.getCurrentController()
    def getCurrentSelection(self):
        return self._database.getCurrentSelection()
    def getURL(self):
        return self._database.getURL()
    def hasControllersLocked(self):
        return self._database.hasControllersLocked()
    def lockControllers(self):
        self._database.lockControllers()
    def setCurrentController(self, controller):
        self._database.setCurrentController(controller)
    def unlockControllers(self):
        self._database.unlockControllers()

# XModel2
    def createDefaultViewController(self, frame):
        return self._database.createDefaultViewController(frame)
    def createViewController(self, viewname, arguments, frame):
        return self._database.createViewController(viewname, arguments, frame)
    def getAvailableViewControllerNames(self):
        return self._database.getAvailableViewControllerNames()
    def getControllers(self):
        return self._database.getControllers()
    def setArgs(self, arguments):
        self._database.setArgs(arguments)

# XModifiable
    def isModified(self):
        return self._database.isModified()
    def setModified(self, modified):
        self._database.setModified(modified)

# XModifyBroadcaster <- XModifiable
    def addModifyListener(self, listener):
        self._database.addModifyListener(listener)
    def removeModifyListener(self, listener):
        self._database.removeModifyListener(listener)

# XPrintable
    def getPrinter(self):
        return self._database.getPrinter()
    def print(self, options):
        self._database.print(options)
    def setPrinter(self, printer):
        self._database.setPrinter(printer)

# XReportDocumentsSupplier <- XOfficeDatabaseDocument
    def getReportDocuments(self):
        return self._database.getReportDocuments()

# XScriptProviderSupplier
    def getScriptProvider(self):
        return self._database.getScriptProvider()

# XServiceInfo
    def supportsService(self, service):
        return self._database.supportsService(service)
    def getImplementationName(self):
        return self._database.getImplementationName()
    def getSupportedServiceNames(self):
        return self._database.getSupportedServiceNames()

# XStorable
    def getLocation(self):
        return self._database.getLocation()
    def hasLocation(self):
        return self._database.hasLocation()
    def isReadonly(self):
        return self._database.isReadonly()
    def store(self):
        self._database.store()
    def storeAsURL(self, url, arguments):
        self._database.storeAsURL(url, arguments)
    def storeToURL(self, url, arguments):
        self._database.storeToURL(url, arguments)

# XStorageBasedDocument
    def addStorageChangeListener(self, listener):
        self._database.addStorageChangeListener(listener)
    def getDocumentStorage(self):
        return self._database.getDocumentStorage()
    def loadFromStorage(self, storage, descriptor):
        self._database.loadFromStorage(storage, descriptor)
    def removeStorageChangeListener(self, listener):
        self._database.removeStorageChangeListener(listener)
    def storeToStorage(self, storage, descriptor):
        self._database.storeToStorage(storage, descriptor)
    def switchToStorage(self, storage):
        self._database.switchToStorage(storage)

# XTitle
    def getTitle(self):
        return self._database.getTitle()
    def setTitle(self, title):
        self._database.setTitle(title)

# XTitleChangeBroadcaster
    def addTitleChangeListener(self, listener):
        self._database.addTitleChangeListener(listener)
    def removeTitleChangeListener(self, listener):
        self._database.removeTitleChangeListener(listener)

# XUIConfigurationManagerSupplier
    def getUIConfigurationManager(self):
        return self._database.getUIConfigurationManager()

# XUntitledNumbers
    def getUntitledPrefix(self):
        return self._database.getUntitledPrefix()
    def leaseNumber(self, component):
        return self._database.leaseNumber(component)
    def releaseNumber(self, number):
        self._database.releaseNumber(number)
    def releaseNumberForComponent(self, component):
        self._database.releaseNumberForComponent(component)

# XWeak
    def queryAdapter(self):
        return self

    def _getPropertySetInfo(self):
        properties = {}
        unotype = '[]com.sun.star.beans.PropertyValue'
        properties['Args'] = getProperty('Args', unotype, BOUND)
        unotype = '[]string'
        properties['AvailableViewControllerNames'] = getProperty('AvailableViewControllerNames', unotype, READONLY)
        unotype = 'com.sun.star.frame.XController'
        properties['CurrentController'] = getProperty('CurrentController', unotype, BOUND)
        unotype = 'com.sun.star.uno.XInterface'
        properties['CurrentSelection'] = getProperty('CurrentSelection', unotype, READONLY)
        unotype = 'com.sun.star.sdbc.XDataSource'
        properties['DataSource'] = getProperty('DataSource', unotype, READONLY)
        unotype = 'com.sun.star.embed.XStorage'
        properties['DocumentStorage'] = getProperty('DocumentStorage', unotype, READONLY)
        unotype = '[]string'
        properties['DocumentSubStoragesNames'] = getProperty('DocumentSubStoragesNames', unotype, READONLY)
        unotype = 'com.sun.star.container.XNameReplace'
        properties['Events'] = getProperty('Events', unotype, READONLY)
        unotype = 'com.sun.star.container.XNameAccess'
        properties['FormDocuments'] = getProperty('FormDocuments', unotype, READONLY)
        unotype = '[]byte'
        properties['ImplementationId'] = getProperty('ImplementationId', unotype, READONLY)
        unotype = 'string'
        properties['ImplementationName'] = getProperty('ImplementationName', unotype, READONLY)
        unotype = 'string'
        properties['Location'] = getProperty('Location', unotype, READONLY)
        unotype = 'boolean'
        properties['Modified'] = getProperty('Modified', unotype, BOUND)
        unotype = '[]com.sun.star.beans.PropertyValue'
        properties['Printer'] = getProperty('Printer', unotype, BOUND)
        unotype = 'com.sun.star.container.XNameAccess'
        properties['ReportDocuments'] = getProperty('ReportDocuments', unotype, READONLY)
        unotype = 'com.sun.star.script.provider.XScriptProvider'
        properties['ScriptProvider'] = getProperty('ScriptProvider', unotype, READONLY)
        unotype = '[]string'
        properties['SupportedServiceNames'] = getProperty('SupportedServiceNames', unotype, READONLY)
        unotype = 'string'
        properties['Title'] = getProperty('Title', unotype, BOUND)
        unotype = 'com.sun.star.ui.XUIConfigurationManager'
        properties['UIConfigurationManager'] = getProperty('UIConfigurationManager', unotype, READONLY)
        unotype = 'string'
        properties['URL'] = getProperty('URL', unotype, READONLY)
        unotype = 'string'
        properties['UntitledPrefix'] = getProperty('UntitledPrefix', unotype, READONLY)
        return properties
