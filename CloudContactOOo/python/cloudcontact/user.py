#!
# -*- coding: utf_8 -*-

import uno
import unohelper

from com.sun.star.sdbc import SQLWarning
from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE
from com.sun.star.ucb.ConnectionMode import OFFLINE
from com.sun.star.ucb.ConnectionMode import ONLINE
from com.sun.star.sdbc import XRestUser

from unolib import KeyMap
from unolib import g_oauth2

import traceback


class User(unohelper.Base,
           XRestUser):
    def __init__(self, ctx, datasource, name):
        self.ctx = ctx
        self._Statement = None
        self._Warnings = []
        self.MetaData, self.Retrieved = datasource.selectUser(name, None)
        self.Request = self._getRequest(datasource.Provider.Host, name)

    @property
    def People(self):
        return self.MetaData.getDefaultValue('People', None)
    @property
    def Resource(self):
        return self.MetaData.getDefaultValue('Resource', None)
    @property
    def Account(self):
        return self.MetaData.getDefaultValue('Account', None)
    @property
    def Token(self):
        return self.MetaData.getDefaultValue('Token', None)
    @property
    def Connection(self):
        if self._Statement:
            return self._Statement.getConnection()
        return None

    def getWarnings(self):
        if self._Warnings:
            return self._Warnings.pop(0)
        return None
    def clearWarnings(self):
        self._Warnings = []

    def _getRequest(self, url, name):
        request = self.ctx.ServiceManager.createInstanceWithContext(g_oauth2, self.ctx)
        if request:
            request.initializeSession(url, name)
        else:
            msg = "Service: %s is not available... Check your installed extensions!!!" % g_oauth2
            warning = self._getWarning('Setup ERROR', 1013, msg, self, None)
            self._Warnings.append(warning)
        return request

    def initialize(self, datasource, name, password):
        try:
            print("User.initialize() 1")
            provider = datasource.Provider
            if not self.Request.isOffLine(provider.Host):
                print("User.initialize() 3")
                user = provider.getUser(self.Request, name)
                if user.IsPresent:
                    self.MetaData = datasource.insertUser(user.Value, name)
                    print("User.initialize() 4 %s" % (self.MetaData, ))
                    if datasource.createUser(self, password):
                        return True
                    else:
                        error = "DataBase ERROR"
                        no = 1014
                        msg = "ERROR: Can't insert User: %s in DataBase" % name
                else:
                    error = "Provider ERROR"
                    no = 1015
                    msg = "ERROR: User: %s does not exist at this Provider" % name
            else:
                error = "OffLine ERROR"
                no = 1013
                msg = "ERROR: Can't retrieve User: %s from provider: network is OffLine" % name
            warning = self._getWarning(error, no, msg, self, None)
            self._Warnings.append(warning)
            return False
        except Exception as e:
            print("User.initialize() ERROR: %s - %s" % (e, traceback.print_exc()))

    def setMetaData(self, metadata):
        self.MetaData = metadata

    def setConnection(self, connection):
        # Piggyback DataBase Connections (easy and clean ShutDown ;-) )
        self._Statement = connection.createStatement()

    def getCredential(self, password):
        return self.People, password

    def _getWarning(self, state, code, message, context=None, exception=None):
        warning = SQLWarning()
        warning.SQLState = state
        warning.ErrorCode = code
        warning.NextException = exception
        warning.Message = message
        warning.Context = context
        return warning
