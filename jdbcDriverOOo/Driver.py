#!
# -*- coding: utf_8 -*-

import uno
import unohelper

from com.sun.star.lang import XServiceInfo
from com.sun.star.sdbc import XDriver
from com.sun.star.sdbcx import XDataDefinitionSupplier
from com.sun.star.sdbcx import XCreateCatalog
from com.sun.star.sdbcx import XDropCatalog
from com.sun.star.sdbc import SQLException
from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE
from com.sun.star.sdbc.ResultSetType import SCROLL_INSENSITIVE
from com.sun.star.sdbc.ResultSetType import SCROLL_SENSITIVE
from com.sun.star.sdbc.ResultSetType import FORWARD_ONLY
from com.sun.star.sdbc.ResultSetConcurrency import READ_ONLY
from com.sun.star.sdbc.ResultSetConcurrency import UPDATABLE

from com.sun.star.uno import Exception as UnoException

from unolib import getConfiguration
from unolib import getResourceLocation
from unolib import getPropertyValueSet

from jdbcdriver import g_identifier
from jdbcdriver import g_path
from jdbcdriver import Connection
from jdbcdriver import getDataSourceUrl
from jdbcdriver import getDataSourceJavaInfo
from jdbcdriver import getDataSourceConnection
from jdbcdriver import getDataBaseInfo
from jdbcdriver import logMessage

import traceback

# pythonloader looks for a static g_ImplementationHelper variable
g_ImplementationHelper = unohelper.ImplementationHelper()
g_ImplementationName = '%s.Driver' % g_identifier


class Driver(unohelper.Base,
             XServiceInfo,
             XDataDefinitionSupplier,
             XCreateCatalog,
             XDropCatalog,
             XDriver):

    def __init__(self, ctx):
        self.ctx = ctx
        self._supportedProtocol = 'sdbc:hsqldb:'
        self._supportedSubProtocols = ('hsql', 'hsqls', 'http', 'https','mem', 'file', 'res')
        print("Driver.__init__()")

    def __del__(self):
        print("Driver.__del__()")

    # XDataDefinitionSupplier
    def getDataDefinitionByConnection(self, connection):
        print("Driver.getDataDefinitionByConnection()")
        return connection
    def getDataDefinitionByURL(self, url, infos):
        print("Driver.getDataDefinitionByURL()")
        connection = self.connect(url, infos)
        return self.getDataDefinitionByConnection(connection)

    # XCreateCatalog
    def createCatalog(self, info):
        print("Driver.createCatalog()")

    # XDropCatalog
    def dropCatalog(self, name, info):
        print("Driver.dropCatalog()")

    # XDriver
    def connect(self, url, infos):
        try:
            protocols = url.strip().split(':')
            user, password = self._getUserCredential(infos)
            print("Driver.connect() 1 %s - %s - %s" % (user, password, url))
            if len(protocols) != 4 or not all(protocols):
                msg = "Invalide protocol: '%s'" % url
                raise self._getException('Protocol ERROR', 1001, msg, self)
            elif not self._isSupportedSubProtocols(protocols):
                msg = "Invalide subprotocol: '%s' are not supported\n" % protocols[2]
                msg += "Supported subprotocols are: %s" % self._getSupportedSubProtocols()
                raise self._getException('Protocol ERROR', 1002, msg, self)
            print("Driver.connect() 2 *****************")
            manager = self.ctx.ServiceManager.createInstance('com.sun.star.sdbc.DriverManager')
            location = getResourceLocation(self.ctx, g_identifier, g_path)
            info = getDataSourceJavaInfo(location)
            if user is None:
                user = 'SA'
            else:
                info += getPropertyValueSet({'user', user})
                if password is not None:
                    info += getPropertyValueSet({'password', password})
            path = 'jdbc:%s' % ':'.join(protocols[1:])
            print("Driver.connect() 3 %s" % path)
            connection = manager.getConnectionWithInfo(path, info)
            #mri = self.ctx.ServiceManager.createInstance('mytools.Mri')
            #mri.inspect(self.DataSource.Connection)
            version = connection.getMetaData().getDriverVersion()
            print("Driver.connect() 4 %s" % version)
            return Connection(self.ctx, connection, protocols, user)
        except SQLException as e:
            raise e
        except Exception as e:
            print("Driver.connect() ERROR: %s - %s" % (e, traceback.print_exc()))

    def acceptsURL(self, url):
        print("Driver.acceptsURL() %s" % url)
        return url.startswith(self._supportedProtocol)

    def getPropertyInfo(self, url, infos):
        try:
            print("Driver.getPropertyInfo() %s" % url)
            for info in infos:
                print("Driver.getPropertyInfo():   %s - '%s'" % (info.Name, info.Value))
            drvinfo = []
            dbinfo = getDataBaseInfo()
            for info in dbinfo:
                drvinfo.append(self._getDriverPropertyInfo(info, dbinfo[info]))
            for info in infos:
                if info.Name not in dbinfo:
                    drvinfo.append(self._getDriverPropertyInfo(info.Name, info.Value))
            print("Driver.getPropertyInfo():\n")
            for info in drvinfo:
                print("Driver.getPropertyInfo():   %s - %s" % (info.Name, info.Value))
            return tuple(drvinfo)
        except Exception as e:
            print("Driver.getPropertyInfo() ERROR: %s - %s" % (e, traceback.print_exc()))

    def getMajorVersion(self):
        print("Driver.getMajorVersion()")
        return 1
    def getMinorVersion(self):
        print("Driver.getMinorVersion()")
        return 0

    def _getUserCredential(self, infos):
        username = None
        password = None
        for info in infos:
            if info.Name == 'user':
                username = info.Value.strip()
            elif info.Name == 'password':
                password = info.Value.strip()
            if username and password:
                break
        return username, password

    def _getSupportedSubProtocols(self):
        return ', '.join(self._supportedSubProtocols).title()

    def _isSupportedSubProtocols(self, protocols):
        return protocols[2].lower() in self._supportedSubProtocols

    def _getException(self, state, code, message, context=None, exception=None):
        error = SQLException()
        error.SQLState = state
        error.ErrorCode = code
        error.NextException = exception
        error.Message = message
        error.Context = context
        return error

    def _getDriverPropertyInfo(self, name, value):
        info = uno.createUnoStruct('com.sun.star.sdbc.DriverPropertyInfo')
        print("Driver._getDriverPropertyInfo() %s - %s - %s" % (name, value, type(value)))
        info.Name = name
        required = value is not None and not isinstance(value, tuple)
        info.IsRequired = required
        if required:
            info.Value = value
        info.Choices = ()
        return info

    # XServiceInfo
    def supportsService(self, service):
        return g_ImplementationHelper.supportsService(g_ImplementationName, service)
    def getImplementationName(self):
        return g_ImplementationName
    def getSupportedServiceNames(self):
        return g_ImplementationHelper.getSupportedServiceNames(g_ImplementationName)


g_ImplementationHelper.addImplementation(Driver,
                                         g_ImplementationName,
                                        (g_ImplementationName,
                                        'com.sun.star.sdbc.Driver',
                                        'com.sun.star.sdbcx.Driver'))
