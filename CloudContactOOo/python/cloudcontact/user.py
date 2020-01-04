#!
# -*- coding: utf_8 -*-

import uno
import unohelper

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE
from com.sun.star.ucb.ConnectionMode import OFFLINE
from com.sun.star.ucb.ConnectionMode import ONLINE
from com.sun.star.sdbc import XRestUser

from unolib import KeyMap
from unolib import g_oauth2
from unolib import createService

from .configuration import g_identifier
from .dbinit import getDataSourceUrl
from .dbtools import getDataSourceConnection
from .dbtools import getDataBaseConnection
from .dbtools import getWarning

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

    def getWarnings(self):
        if self._Warnings:
            return self._Warnings.pop(0)
        return None
    def clearWarnings(self):
        self._Warnings = []

    def _getRequest(self, url, name):
        request = createService(self.ctx, g_oauth2)
        if request:
            request.initializeSession(url, name)
        else:
            msg = "Service: %s is not available... Check your installed extensions!!!" % g_oauth2
            warning = getWarning('Setup ERROR', 1013, msg, self, None)
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
                        state = "DataBase ERROR"
                        code = 1014
                        msg = "ERROR: Can't insert User: %s in DataBase" % name
                else:
                    state = "Provider ERROR"
                    code = 1015
                    msg = "ERROR: User: %s does not exist at this Provider" % name
            else:
                state = "OffLine ERROR"
                code = 1013
                msg = "ERROR: Can't retrieve User: %s from provider: network is OffLine" % name
            warning = getWarning(state, code, msg, self, None)
            self._Warnings.append(warning)
            return False
        except Exception as e:
            print("User.initialize() ERROR: %s - %s" % (e, traceback.print_exc()))

    def setMetaData(self, metadata):
        self.MetaData = metadata

    def getConnection(self, scheme, password):
        url, error = getDataSourceUrl(self.ctx, scheme, g_identifier, False)
        if error is None:
            credential = self.getCredential(password)
            print("User.getConnection() 1 %s - %s" % credential)
            connection, error = getDataBaseConnection(self.ctx, url, scheme, *credential)
            if error is None:
                return connection
            else:
                state = "DataBase ERROR"
                code = 1017
                msg = "ERROR: Can't connect to new DataBase: %s" % scheme
        else:
            state = "DataBase ERROR"
            code = 1016
            msg = "ERROR: Can't create new DataBase: %s" % scheme
        warning = getWarning(state, code, msg, self, error)
        self._Warnings.append(warning)
        return False

    def getCredential(self, password):
        return self.People, password
