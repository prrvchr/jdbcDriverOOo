#!
# -*- coding: utf_8 -*-

import uno
import unohelper

from com.sun.star.sdbc import SQLException
from com.sun.star.lang import XEventListener
from com.sun.star.frame import XTerminateListener
from com.sun.star.util import XCloseListener
from com.sun.star.sdbc import XRestDataSource
from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE
from com.sun.star.sdb.CommandType import QUERY
from com.sun.star.sdbc.DataType import VARCHAR

from unolib import KeyMap
from unolib import g_oauth2
from unolib import createService
from unolib import getResourceLocation
from unolib import getPropertyValueSet
from unolib import getPropertyValue
from unolib import parseDateTime
from unolib import unparseDateTime
from unolib import unparseTimeStamp

from .configuration import g_identifier
from .configuration import g_admin
from .configuration import g_group
from .configuration import g_db_timestamp
from .configuration import g_compact
from .provider import Provider
from .dataparser import DataParser
from .user import User
from .replicator import Replicator

from .dbconfig import g_path
from .dbqueries import getSqlQuery
from .dbinit import getDataSourceUrl
from .dbtools import getDataSourceJavaInfo
from .dbtools import getDataSourceLocation
from .dbtools import getDataBaseConnection
from .dbtools import getDataSourceConnection
from .dbtools import getKeyMapFromResult
from .dbtools import getKeyMapSequenceFromResult
from .dbtools import getKeyMapKeyMapFromResult
from .dbtools import getSequenceFromResult
from .dbtools import getDataSourceCall
from .dbtools import getSqlException
from .logger import logMessage
from .logger import getMessage

from collections import OrderedDict
from threading import Condition
from threading import Event
import traceback


class DataSource(unohelper.Base,
                 XTerminateListener,
                 XRestDataSource):
    def __init__(self, ctx, event):
        print("DataSource.__init__() 1")
        self.ctx = ctx
        self.Provider = Provider(self.ctx)
        self._Warnings = None
        self._Statement = None
        self._FieldsMap = {}
        self._UsersPool = {}
        self._CallsPool = OrderedDict()
        self._batchedCall = []
        self.event = event
        self.replicator = None
        self.count = 0
        print("DataSource.__init__() 2")

    @property
    def Connection(self):
        if self._Statement is not None:
            return self._Statement.getConnection()
        return None
    @property
    def Warnings(self):
        return self._Warnings
    @Warnings.setter
    def Warnings(self, warning):
        if warning is None:
            return
        warning.NextException = self._Warnings
        self._Warnings = warning

    def getWarnings(self):
        return self._Warnings
    def clearWarnings(self):
        self._Warnings = None

    def isConnected(self):
        print("DataSource.isConnected() 1")
        if self.Connection is not None and not self.Connection.isClosed():
            return True
        dbname = self.Provider.Host
        print("DataSource.isConnected() 2")
        url, self.Warnings = getDataSourceUrl(self.ctx, dbname, g_identifier, True)
        print("DataSource.isConnected() 3 %s" % url)
        if self.Warnings is not None:
            return False
        print("DataSource.isConnected() 4")
        connection, self.Warnings = getDataSourceConnection(self.ctx, url, dbname)
        if self.Warnings is not None:
            return False
        print("DataSource.isConnected() 5")
        # Piggyback DataBase Connections (easy and clean ShutDown ;-) )
        self._Statement = connection.createStatement()
        # Add a TerminateListener  which is responsible for the shutdown of the database
        desktop = 'com.sun.star.frame.Desktop'
        print("DataSource.isConnected() 6")
        self.ctx.ServiceManager.createInstance(desktop).addTerminateListener(self)
        print("DataSource.connect() OK")
        #mri = self.ctx.ServiceManager.createInstance('mytools.Mri')
        #mri.inspect(connection)
        return True

    # XTerminateListener
    def queryTermination(self, event):
        level = INFO
        msg = getMessage(self.ctx, 101, self.Provider.Host)
        print("DataSource.queryTermination() 1")
        self.event.set()
        self.replicator.join(30)
        print("DataSource.queryTermination() 2")
        if self.Connection is None or self.Connection.isClosed():
            level = SEVERE
            msg += getMessage(self.ctx, 103)
        else:
            compact = self.count >= g_compact
            query = getSqlQuery('shutdown', compact)
            print("DataSource.queryTermination() 3")
            self._Statement.execute(query)
            msg += getMessage(self.ctx, 102)
        logMessage(self.ctx, level, msg, 'DataSource', 'queryTermination()')
        print("DataSource.queryTermination() 4 - %s" % msg)
    def notifyTermination(self, event):
        pass

    # XRestDataSource
    def getUser(self, name, password):
        if name in self._UsersPool:
            user = self._UsersPool[name]
        else:
            user = User(self.ctx, self, name)
            if not self._initializeUser(user, name, password):
                return None
            self._UsersPool[name] = user
        # User has been initialized and the connection to the database is done...
        # We can start the database replication in a background task.
        if self.replicator is None or not self.replicator.is_alive():
            self.replicator = Replicator(self.ctx, self, self.event)
        else:
            self.replicator.event.clear()
        return user

    def setLoggingChanges(self, state):
        sql = getSqlQuery('loggingChanges', state)
        self._Statement.execute(sql)

    def saveChanges(self, compact=False):
        if self.count >= g_compact:
            compact = True
            self.count = 0
        sql = getSqlQuery('saveChanges', compact)
        self._Statement.execute(sql)

    def shutdownDataBase(self, compact=False):
        try:
            print("DataSource.shutdownDataBase() 1")
            level = INFO
            msg = getMessage(self.ctx, 101, self.Provider.Host)
            print("DataSource.shutdownDataBase() 2")
            if self.Connection is None or self.Connection.isClosed():
                print("DataSource.shutdownDataBase() 3")
                level = SEVERE
                msg += getMessage(self.ctx, 103)
            else:
                print("DataSource.shutdownDataBase() 4")
                compact = self.replicator.Compact
                query = getSqlQuery('shutdown', compact)
                print("DataSource.shutdownDataBase() 5")
                self._Statement.execute(query)
                print("DataSource.shutdownDataBase() 6")
                msg += getMessage(self.ctx, 102)
            logMessage(self.ctx, level, msg, 'DataSource', 'queryTermination()')
            print("DataSource.shutdownDataBase() %s" % msg)
        except Exception as e:
            print("datasource.shutdownDataBase() ERROR: %s - %s" % (e, traceback.print_exc()))

    def getUserFields(self):
        fields = []
        call = getDataSourceCall(self.Connection, 'getFieldNames')
        result = call.executeQuery()
        fields = getSequenceFromResult(result)
        call.close()
        print("DataSource.getUserFields() %s" % (fields, ))
        return tuple(fields)

    def getRequest(self, name):
        request = createService(self.ctx, g_oauth2)
        if request:
            request.initializeSession(self.Provider.Host, name)
        else:
            state = getMessage(self.ctx, 1003)
            msg = getMessage(self.ctx, 1105, g_oauth2)
            self.Warnings = getSqlException(state, 1105, msg, self)
        return request

    def selectUser(self, account):
        user = None
        print("DataSource.selectUser() %s - %s" % (account, user))
        try:
            call = getDataSourceCall(self.Connection, 'getPerson')
            call.setString(1, account)
            result = call.executeQuery()
            if result.next():
                user = getKeyMapFromResult(result)
            call.close()
            print("DataSource.selectUser() %s - %s" % (account, user))
        except SQLException as e:
            self.Warnings = e
        return user

    def getFieldsMap(self, method, reverse):
        if method not in self._FieldsMap:
            self._FieldsMap[method] = self._getFieldsMap(method)
        if reverse:
            map = KeyMap(**{i: {'Map': j, 'Type': k, 'Table': l} for i, j, k, l in self._FieldsMap[method]})
        else:
            map = KeyMap(**{j: {'Map': i, 'Type': k, 'Table': l} for i, j, k, l in self._FieldsMap[method]})
        return map

    def getUpdatedGroups(self, user, prefix):
        groups = None
        call = self.getDataSourceCall('selectUpdatedGroup')
        call.setString(1, prefix)
        call.setLong(2, user.People)
        call.setString(3, user.Resource)
        result = call.executeQuery()
        groups = getKeyMapKeyMapFromResult(result)
        return groups

    def truncatGroup(self, start):
        format = {'TimeStamp': unparseTimeStamp(start)}
        query = getSqlQuery('truncatGroup', format)
        self._Statement.execute(query)

    def createSynonym(self, user, name):
        format = {'Schema': user.Resource, 'View': name.title()}
        query = getSqlQuery('createSynonym', format)
        self._Statement.execute(query)

    def createGroupView(self, user, name, group):
        self.dropGroupView(user, name)
        query = self._getGroupViewQuery('create', user, name, group)
        self._Statement.execute(query)

    def dropGroupView(self, user, name):
        query = self._getGroupViewQuery('drop', user, name)
        self._Statement.execute(query)

    def _getGroupViewQuery(self, method, user, name, group=0):
        query = '%sGroupView' % method
        account, pwd = user.getCredential('')
        format = {'User': account,
                  'View': '%s.%s' % (user.Name, name.title()),
                  'Group': group}
        return getSqlQuery(query, format)

    def updateSyncToken(self, user, token, data, timestamp):
        value = data.getValue(token)
        call = self.getDataSourceCall('update%s' % token, True)
        call.setString(1, value)
        call.setTimestamp(2, timestamp)
        call.setLong(3, user.People)
        call.addBatch()
        return KeyMap(**{token: value})

    def mergePeople(self, user, resource, timestamp, deleted):
        call = self.getDataSourceCall('mergePeople', True)
        call.setString(1, 'people/')
        call.setString(2, resource)
        call.setLong(3, user.Group)
        call.setTimestamp(4, timestamp)
        call.setBoolean(5, deleted)
        call.addBatch()
        return (0, 1) if deleted else (1, 0)

    def mergePeopleData(self, table, resource, typename, label, value, timestamp):
        format = {'Table': table, 'Type': typename}
        call = self.getDataSourceCall(table, True, 'mergePeopleData', format)
        call.setString(1, 'people/')
        call.setString(2, resource)
        call.setString(3, label)
        call.setString(4, value)
        call.setTimestamp(5, timestamp)
        if typename is not None:
            call.setString(6, table)
            call.setString(7, typename)
        call.addBatch()
        return 1

    def mergeGroup(self, user, resource, name, timestamp, deleted):
        call = self.getDataSourceCall('mergeGroup', True)
        call.setString(1, 'contactGroups/')
        call.setLong(2, user.People)
        call.setString(3, resource)
        call.setString(4, name)
        call.setTimestamp(5, timestamp)
        call.setBoolean(6, deleted)
        call.addBatch()
        return (0, 1) if deleted else (1, 0)

    def mergeConnection(self, user, data, timestamp):
        separator = ','
        call = self.getDataSourceCall('mergeConnection', True)
        call.setString(1, 'contactGroups/')
        call.setString(2, 'people/')
        call.setString(3, data.getValue('Resource'))
        call.setTimestamp(4, timestamp)
        call.setString(5, separator)
        members = data.getDefaultValue('Connections', ())
        call.setString(6, separator.join(members))
        call.addBatch()
        print("datasource._mergeConnection() %s - %s" % (data.getValue('Resource'), len(members)))
        return len(members)

    def _getFieldsMap(self, method):
        map = []
        call = getDataSourceCall(self.Connection, 'getFieldsMap')
        call.setString(1, method)
        r = call.executeQuery()
        while r.next():
            map.append((r.getString(1), r.getString(2), r.getString(3), r.getString(4)))
        call.close()
        return tuple(map)

    def _initializeUser(self, user, name, password):
        if user.Request is None:
            return False
        if user.MetaData is not None:
            return True
        if self.Provider.isOnLine():
            data = self.Provider.getUser(user.Request, user)
            if data.IsPresent:
                user.MetaData = self._insertUser(data.Value, name)
                credential = user.getCredential(password)
                if self._createUser(*credential):
                    self.createGroupView(user, g_group, user.Group)
                    return True
                else:
                    state = getMessage(self.ctx, 1005)
                    code = 1106
                    msg = getMessage(self.ctx, code, name)
            else:
                state = getMessage(self.ctx, 1006)
                code = 1107
                msg = getMessage(self.ctx, code, name)
        else:
            state = getMessage(self.ctx, 1004)
            code = 1108
            msg = getMessage(self.ctx, code, name)
        self.Warnings = getSqlException(state, code, msg, self)
        return False

    def _insertUser(self, data, account):
        user = KeyMap()
        call = getDataSourceCall(self.Connection, 'insertUser')
        call.setString(1, self.Provider.getUserId(data))
        call.setString(2, account)
        call.setString(3, g_group)
        result = call.executeQuery()
        if result.next():
            user = getKeyMapFromResult(result)
        call.close()
        return user

    def _createUser(self, name, password):
        format = {'User': name, 'Password': password, 'Admin': g_admin}
        sql = getSqlQuery('createUser', format)
        status = self._Statement.executeUpdate(sql)
        return status == 0

    def getDataSourceCall(self, key, batched=False, name=None, format=None):
        if key not in self._CallsPool:
            name = key if name is None else name
            self._CallsPool[key] = getDataSourceCall(self.Connection, name, format)
        if batched and key not in self._batchedCall:
            self._batchedCall.append(key)
        return self._CallsPool[key]

    def getPreparedCall(self, name):
        if name not in self._CallsPool:
            # TODO: cannot use: call = self.Connection.prepareCommand(name, QUERY)
            # TODO: it trow a: java.lang.IncompatibleClassChangeError
            query = self.Connection.getQueries().getByName(name).Command
            self._CallsPool[name] = self.Connection.prepareCall(query)
        if name not in self._batchedCall:
            self._batchedCall.append(name)
        return self._CallsPool[name]

    def executeBatchCall(self):
        for name in self._batchedCall:
            self._CallsPool[name].executeBatch()
        self._batchedCall = []

    def closeDataSourceCall(self):
        for name in self._CallsPool:
            call = self._CallsPool[name]
            if name in self._batchedCall:
                call.executeBatch()
            call.close()
        self._CallsPool = OrderedDict()
        self._batchedCall = []
