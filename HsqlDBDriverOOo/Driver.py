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

from com.sun.star.lang import XServiceInfo

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from com.sun.star.sdbc import SQLException
from com.sun.star.sdbc import XDriver

from com.sun.star.sdbcx import XCreateCatalog
from com.sun.star.sdbcx import XDataDefinitionSupplier
from com.sun.star.sdbcx import XDropCatalog

#from hsqldbdriver import Connection

from hsqldbdriver import createService
from hsqldbdriver import getUrlTransformer
from hsqldbdriver import parseUrl

from hsqldbdriver import getConnectionInfo
from hsqldbdriver import getDriverPropertyInfo
from hsqldbdriver import getDataSourceClassPath
from hsqldbdriver import getSqlException
from hsqldbdriver import g_class
from hsqldbdriver import g_identifier
from hsqldbdriver import g_protocol

from hsqldbdriver import logMessage
from hsqldbdriver import getMessage
g_message = 'Driver'

import traceback

# pythonloader looks for a static g_ImplementationHelper variable
g_ImplementationHelper = unohelper.ImplementationHelper()
g_ImplementationName = '%s.Driver' % g_identifier


class Driver(unohelper.Base,
             XCreateCatalog,
             XDataDefinitionSupplier,
             XDriver,
             XDropCatalog,
             XServiceInfo):

    def __init__(self, ctx):
        self._ctx = ctx
        self._supportedProtocol = 'sdbc:hsqldb:'
        self._subProtocolIndex = 2
        self._supportedSubProtocols = ('hsql', 'hsqls', 'http', 'https', 'mem', 'file', 'res')
        msg = getMessage(self._ctx, g_message, 101)
        logMessage(self._ctx, INFO, msg, 'Driver', '__init__()')

# XCreateCatalog
    def createCatalog(self, info):
        msg = getMessage(self._ctx, g_message, 161)
        logMessage(self._ctx, INFO, msg, 'Driver', 'createCatalog()')

# XDataDefinitionSupplier
    def getDataDefinitionByConnection(self, connection):
        msg = getMessage(self._ctx, g_message, 141)
        logMessage(self._ctx, INFO, msg, 'Driver', 'getDataDefinitionByConnection()')
        return connection
    def getDataDefinitionByURL(self, url, infos):
        msg = getMessage(self._ctx, g_message, 151, url)
        logMessage(self._ctx, INFO, msg, 'Driver', 'getDataDefinitionByURL()')
        connection = self.connect(url, infos)
        return self.getDataDefinitionByConnection(connection)

# XDriver
    def connect(self, url, infos):
        try:
            msg = getMessage(self._ctx, g_message, 111, url)
            logMessage(self._ctx, INFO, msg, 'Driver', 'connect()')
            path, has_option, option = url.strip().partition(';')
            protocols = path.split(':')
            if len(protocols) < 4 or not all(protocols):
                code = getMessage(self._ctx, g_message, 112)
                msg = getMessage(self._ctx, g_message, 113, url)
                raise getSqlException(code, 1001, msg, self)
            if not self._isSupportedSubProtocols(protocols):
                code = getMessage(self._ctx, g_message, 112)
                subprotocol = self._getSubProtocol(protocols)
                supported = self._getSupportedSubProtocols()
                msg = getMessage(self._ctx, g_message, 114, (subprotocol, supported))
                raise getSqlException(code, 1002, msg, self)
            transformer = getUrlTransformer(self._ctx)
            location = self._getUrl(transformer, protocols)
            if location is None:
                code = getMessage(self._ctx, g_message, 115)
                msg = getMessage(self._ctx, g_message, 116, url)
                raise getSqlException(code, 1003, msg, self)
            options = option.split(';') if has_option != '' else None
            user, password, classpath = self._getConnectionInfo(infos)
            datasource = self._getDataSource(transformer, location, options, classpath)
            connection = self._getConnection(datasource, url, user, password, classpath)
            version = connection.getMetaData().getDriverVersion()
            username = connection.getMetaData().getUserName()
            msg = getMessage(self._ctx, g_message, 117, (version, username))
            logMessage(self._ctx, INFO, msg, 'Driver', 'connect()')
            return connection
        except SQLException as e:
            raise e
        except Exception as e:
            msg = getMessage(self._ctx, g_message, 118, (e, traceback.print_exc()))
            logMessage(self._ctx, SEVERE, msg, 'Driver', 'connect()')

    def acceptsURL(self, url):
        accept = url.startswith(self._supportedProtocol)
        return accept

    def getPropertyInfo(self, url, infos):
        properties = ()
        if self.acceptsURL(url):
            properties = getDriverPropertyInfo()
        return properties

    def getMajorVersion(self):
        return 1
    def getMinorVersion(self):
        return 0

# XDropCatalog
    def dropCatalog(self, name, info):
        msg = getMessage(self._ctx, g_message, 171, name)
        logMessage(self._ctx, INFO, msg, 'Driver', 'dropCatalog()')

# XServiceInfo
    def supportsService(self, service):
        return g_ImplementationHelper.supportsService(g_ImplementationName, service)
    def getImplementationName(self):
        return g_ImplementationName
    def getSupportedServiceNames(self):
        return g_ImplementationHelper.getSupportedServiceNames(g_ImplementationName)

#Private method
    def _isSupportedSubProtocols(self, protocols):
        return self._getSubProtocol(protocols).lower() in self._supportedSubProtocols

    def _getSubProtocol(self, protocols):
        return protocols[self._subProtocolIndex]

    def _getSupportedSubProtocols(self):
        return ', '.join(self._supportedSubProtocols)

    def _getUrl(self, transformer, protocols):
        location = ':'.join(protocols[self._subProtocolIndex:])
        return parseUrl(transformer, location)

    def _getDataSource(self, transformer, url, options, path):
        service = 'com.sun.star.sdb.DatabaseContext'
        dbcontext = createService(self._ctx, service)
        datasource = dbcontext.createInstance()
        datasource.URL = self._getDataSourceUrl(transformer, url, options)
        datasource.Settings.JavaDriverClass = g_class
        if path is None:
            path = getDataSourceClassPath(self._ctx, g_identifier)
        datasource.Settings.JavaDriverClassPath = path
        return datasource

    def _getDataSourceUrl(self, transformer, url, options):
        location = g_protocol
        location += transformer.getPresentation(url, False)
        if options is not None:
            location += ';%s' % ';'.join(options)
        return location

    def _getConnectionInfo(self, infos):
        user = ''
        password = ''
        path = None
        for info in infos:
            if info.Name == 'user':
                user = info.Value.strip()
            elif info.Name == 'password':
                password = info.Value.strip()
            elif info.Name == 'JavaDriverClassPath':
                path = info.Value.strip()
        return user, password, path

    def _getConnection(self, datasource, url, user, password, classpath):
        connection = datasource.getIsolatedConnection(user, password)
        info = getConnectionInfo(user, password, classpath)
        datasource.Info = info
        datasource.URL = url
        # TODO: Now that we have the connection, we return a
        # TODO: com.sun.star.sdbc.Connection service wrapper
        # TODO: that provides an url with the <sdbc> protocol
        return Connection(self._ctx, connection, datasource, info, url)


g_ImplementationHelper.addImplementation(Driver,
                                         g_ImplementationName,
                                        ('com.sun.star.sdbc.Driver',
                                        'com.sun.star.sdbcx.Driver'))
