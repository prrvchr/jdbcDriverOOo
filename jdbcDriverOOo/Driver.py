#!
# -*- coding: utf_8 -*-

import uno
import unohelper

from com.sun.star.lang import XServiceInfo
from com.sun.star.sdbc import XDriver
from com.sun.star.sdbcx import XDataDefinitionSupplier
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

from cloudcontact import g_identifier
from cloudcontact import Connection
from cloudcontact import getDataSourceUrl
from cloudcontact import getDataSourceConnection
from cloudcontact import getDataBaseInfo
from cloudcontact import logMessage

import traceback

# pythonloader looks for a static g_ImplementationHelper variable
g_ImplementationHelper = unohelper.ImplementationHelper()
g_ImplementationName = '%s.Driver' % g_identifier


class Driver(unohelper.Base,
             XServiceInfo,
             XDataDefinitionSupplier,
             XDriver):

    __dataSource = None
    __usersPool = {}

    def __init__(self, ctx):
        self.ctx = ctx
        self._supportedProtocol = 'sdbc:hsqldb1:'
        self._supportedSubProtocols = ('test', 'tests')
        print("Driver.__init__()")

    def __del__(self):
        print("Driver.__del__()")

    @property
    def DataSource(self):
        if Driver.__dataSource is None:
            Driver.__dataSource = DataSource(self.ctx)
        return Driver.__dataSource

    # XDataDefinitionSupplier
    def getDataDefinitionByConnection(self, connection):
        return connection.getTables()
    def getDataDefinitionByURL(self, url, infos):
        connection = self.connect(url, infos)
        return self.getDataDefinitionByConnection(connection)

    # XDriver
    def connect(self, url, infos):
        try:
            protocols = url.strip().split(':')
            username, password = self._getUserCredential(infos)
            print("Driver.connect() 1 %s - %s - %s" % (username, password, url))
            if len(protocols) != 3 or not all(protocols):
                msg = "Invalide protocol: '%s'" % url
                raise self._getException('Protocol ERROR', 1001, msg, self)
            elif not self._isSupportedSubProtocols(protocols):
                msg = "Invalide subprotocol: '%s' are not supported\n" % protocols[2]
                msg += "Supported subprotocols are: %s" % self._getSupportedSubProtocols()
                raise self._getException('Protocol ERROR', 1002, msg, self)
            elif not username:
                msg = "You must provide a UserName!"
                raise self._getException('Authentication ERROR', 1003, msg, self)
            level = INFO
            #scheme = '%s-%s' % (protocols[1], self._supportedSubProtocols[1])
            scheme = self.DataSource.Provider.Host
            msg = "Driver for Scheme: %s loading ... " % scheme
            print("Driver.connect() 2 *****************")
            if not self.DataSource.isConnected():
                print("Driver.connect() 3 *****************")
                dbcontext = self.ctx.ServiceManager.createInstance('com.sun.star.sdb.DatabaseContext')
                path, error = getDataSourceUrl(self.ctx, dbcontext, scheme, g_identifier, False)
                if error:
                    msg = "DataBase Error: Could not initialize DataBase at URL: %s" % path
                    raise self._getException('DataBase ERROR', 1003, msg, self, error)
                if not self.DataSource.connect(dbcontext, path):
                    warning = self.DataSource.getWarnings()
                    self._getSupplierWarnings(self.DataSource, warning)
                    msg = "Could not connect to DataSource at URL: %s" % path
                    raise self._getException('DataBase ERROR', 1003, msg, self, warning)
                #mri = self.ctx.ServiceManager.createInstance('mytools.Mri')
                #mri.inspect(self.DataSource.Connection)
            user = self.DataSource.getUser(username)
            if user is None:
                user = User(self.ctx, self.DataSource, username)
                warning = self.DataSource.getWarnings()
                if warning:
                    self._getSupplierWarnings(self.DataSource, warning)
                    msg = "Could not retrive user: %s from DataSource: %s" % (username, scheme)
                    raise self._getException('DataBase ERROR', 1003, msg, self, warning)
                warning = user.getWarnings()
                if warning:
                    self._getSupplierWarnings(user, warning)
                    msg = "Setup Error: Could not initialize user: %s" % username
                    raise self._getException('Setup ERROR', 1003, msg, self, warning)
                if not user.Retrieved:
                    if not user.initialize(self.DataSource, username, password):
                        warning = user.getWarnings()
                        self._getSupplierWarnings(user, warning)
                        msg = "Could not retreive user %s from provider" % username
                        raise self._getException('Connection ERROR', 1003, msg, self, warning)
                if not self.DataSource.setUser(user, scheme, username, password):
                    self._getSupplierWarnings(self.DataSource, warning)
                    msg = "Could not connect user: %s to DataBase" % username
                    raise self._getException('DataBase ERROR', 1003, msg, self, warning)
                #mri = self.ctx.ServiceManager.createInstance('mytools.Mri')
                #config = getConfiguration(self.ctx, 'org.openoffice.Office.DataAccess')
                #mri.inspect(config)
                print("Driver.connect() 4 *****************")
            msg += "Done"
            logMessage(self.ctx, INFO, msg, 'Driver', 'connect()')
            #dbcontext = self.ctx.ServiceManager.createInstance('com.sun.star.sdb.DatabaseContext')
            #path = getDataSourceUrl(self.ctx, dbcontext, 'Template', g_identifier, False)
            #path1 = getDataSourceUrl(self.ctx, dbcontext, scheme, g_identifier, False)
            #datasource = dbcontext.getByName(path)
            #datasource1 = dbcontext.getByName(path1)
            print("Driver.connect() 5 %s" % user.Connection.isClosed())
            #return self.DataSource.Connection
            #con = self.DataSource.Connection
            connection = Connection(self.ctx, self.DataSource, user, protocols)
            #getDataBaseVersion(connection)
            #connection = Connection(self.DataSource.Connection, protocols, user.Account)
            #mri = self.ctx.ServiceManager.createInstance('mytools.Mri')
            #mri.inspect(datasource)
            #mri.inspect(datasource1)
            #mri.inspect(con)
            #settype = con.MetaData.supportsResultSetType(SCROLL_INSENSITIVE)
            #setcon = con.MetaData.supportsResultSetConcurrency(SCROLL_INSENSITIVE, READ_ONLY)
            #print("Connection support %s - %s - %s - %s" % (settype, SCROLL_SENSITIVE, setcon, READ_ONLY))
            return connection
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
        username = ''
        password = ''
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

    def _getSupplierWarnings(self, supplier, error):
        self._getWarnings(supplier, error)
        supplier.clearWarnings()

    def _getWarnings(self, supplier, error):
        warning = supplier.getWarnings()
        if warning:
            error.NextException = warning
            self._getWarnings(supplier, warning)

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
