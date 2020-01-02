#!
# -*- coding: utf_8 -*-

import uno
import unohelper

from com.sun.star.sdbc import SQLException
from com.sun.star.lang import XEventListener
from com.sun.star.frame import XTerminateListener
from com.sun.star.util import XCloseListener
from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE
from com.sun.star.sdb.CommandType import QUERY
from com.sun.star.ucb.ConnectionMode import ONLINE

from com.sun.star.sdbc import XRestDataSource

from unolib import KeyMap
from unolib import getResourceLocation
from unolib import getPropertyValueSet
from unolib import getPropertyValue
from unolib import parseDateTime

from .configuration import g_identifier
from .provider import Provider
from .dataparser import DataParser

from .dbconfig import g_path
from .dbqueries import getSqlQuery
from .dbinit import getDataSourceUrl
from .dbtools import getDataSourceJavaInfo
from .dbtools import getDataSourceLocation
from .dbtools import getDataBaseConnection
from .dbtools import getDataSourceConnection
from .dbtools import getKeyMapFromResult
from .dbtools import getDataSourceCall

import binascii
import traceback


class DataSource(unohelper.Base,
                 XTerminateListener,
                 XRestDataSource):
    def __init__(self, ctx):
        self.ctx = ctx
        self.Provider = Provider(self.ctx)
        self._Calls = {}
        self._Warnings = []
        self._Error = ''
        self._Statement = None
        self._FieldsMap = None
        self._PrimaryField = None
        self._PeopleIndex = None
        self._Types = None
        self._LabelIndex = None
        self._UsersPool = {}
        msg = "DataSource for Scheme: %s loading ... Done" % self.Provider.Host
        self.Logger.logp(INFO, 'DataSource', '__init__()', msg)
        print(msg)

    @property
    def IsValid(self):
        return not self.Error
    @property
    def Error(self):
        return self.Provider.Error if self.Provider and self.Provider.Error else self._Error
    @property
    def Connection(self):
        if self._Statement:
            return self._Statement.getConnection()
        return None
    @property
    def Logger(self):
        return self.Provider.Logger

    def getWarnings(self):
        if self._Warnings:
            return self._Warnings.pop(0)
        return None
    def clearWarnings(self):
        self._Warnings = []

    def isConnected(self):
        if self.Connection:
            return not self.Connection.isClosed()
        return False
    def connect(self, url):
        connection, error = getDataSourceConnection(self.ctx, url, self.Provider.Host)
        if error:
            self._Warnings.append(error)
            return False
        # Piggyback DataBase Connections (easy and clean ShutDown ;-) )
        self._Statement = connection.createStatement()
        desktop = 'com.sun.star.frame.Desktop'
        self.ctx.ServiceManager.createInstance(desktop).addTerminateListener(self)
        print("DataSource.connect() OK")
        return True

    # XTerminateListener
    def queryTermination(self, event):
        print("DataSource.queryTermination()")
        msg = "DataSource queryTermination: Scheme: %s ... " % self.Provider.Host
        if self._Statement is None:
            msg += "ERROR: database connection already dropped..."
        else:
            query = getSqlQuery('shutdown')
            self._Statement.execute(query)
            msg += "Done"
        print("DataSource.queryTermination() %s" % msg)
    def notifyTermination(self, event):
        pass

    def getUser(self, key):
        user = None
        if key in self._UsersPool:
            user = self._UsersPool[key]
            self.synchronize(user)
        return user

    def setUser(self, user, scheme, key, password):
        url, error = getDataSourceUrl(self.ctx, scheme, g_identifier, False)
        if error is not None:
            print("DataSource.setUser %s" % error)
            self._Warnings.append(error)
            return False
        credential = user.getCredential(password)
        print("DataSource.setUser() 1 %s - %s" % credential)
        connection, error = getDataSourceConnection(self.ctx, scheme, url, *credential)
        if error is not None:
            print("DataSource.setUser %s" % error)
            self._Warnings.append(error)
            return False
        version = connection.getMetaData().getDriverVersion()
        print("DataSource.setUser() 2 %s" % version)
        user.setConnection(connection)
        self._UsersPool[key] = user
        self.synchronize(user)
        return True

    # XRestDataSource
    def selectUser(self, account, retrieved=False):
        user = KeyMap()
        print("DataSource.selectUser() %s - %s" % (account, user))
        try:
            call = getDataSourceCall(self.Connection, 'getPerson')
            call.setString(1, account)
            result = call.executeQuery()
            if result.next():
                user = getKeyMapFromResult(result)
                retrieved = True
            call.close()
            print("DataSource.selectUser() %s - %s" % (account, user))
        except SQLException as e:
            self._Warnings.append(e)
        return user, retrieved

    def insertUser(self, user, account):
        data = KeyMap()
        resource = self.Provider.getUserId(user)
        call = getDataSourceCall(self.Connection, 'insertPerson')
        call.setString(1, resource)
        call.setString(2, account)
        row = call.executeUpdate()
        call.close()
        if row == 1:
            call = getDataSourceCall(self.Connection, 'getIdentity')
            result = call.executeQuery()
            if result.next():
                key = result.getLong(1)
                print("DataSource.insertUser(): %s" % key)
                data.insertValue('People', key)
                data.insertValue('Resource', resource)
                data.insertValue('Account', account)
                data.insertValue('Token', '')
                print("DataSource.insertUser() %s" % data.getValue('People'))
            call.close()
        return data

    def createUser(self, user, password):
        try:
            print("createUser 1")
            credential = user.getCredential(password)
            print("createUser 2 %s - %s" % credential)
            sql = getSqlQuery('createUser', credential)
            print("createUser 3 %s " % sql)
            created = self._Statement.executeUpdate(sql)
            sql = getSqlQuery('grantUser', credential[0])
            print("createUser 4 %s " % sql)
            created = self._Statement.executeUpdate(sql)
            print("createUser 5 %s" % created)
            return created == 0
        except Exception as e:
            print("DataSource.createUser() ERROR: %s - %s" % (e, traceback.print_exc()))

    def synchronize(self, user) :
        try:
            print("DataSource.synchronize() 1")
            if user.Request.isOffLine(self.Provider.Host):
                print("DataSource.synchronize() 2 OffLine")
                return True
            status = False
            timestamp = parseDateTime()
            parameter = self.Provider.getRequestParameter('getPeople', user.MetaData)
            parser = DataParser(self)
            map = self.getFieldsMap(False)
            pattern = self.getPrimaryField()
            enumerator = user.Request.getEnumeration(parameter, parser)
            while enumerator.hasMoreElements():
                response = enumerator.nextElement()
                status = response.IsPresent
                if status:
                    self._syncResponse(user, map, pattern, response.Value, timestamp)
            self._closeDataSourceCall()
            print("DataSource.synchronize() 3")
            return status
        except Exception as e:
            print("DataSource._syncPeople() ERROR: %s - %s" % (e, traceback.print_exc()))

    def _syncResponse(self, user, map, pattern, data, timestamp):
        if data.hasValue(pattern):
            self._mergeResource(user, map, pattern, data, timestamp)
        else:
            for key in data.getKeys():
                d = data.getValue(key)
                m = map.getValue(key).getValue('Type')
                self._mergeResponse(user, map, pattern, key, d, timestamp, m)

    def _mergeResponse(self, user, map, pattern, key, data, timestamp, method):
        print("DataSource._mergeResponse: %s" % (method, ))
        if method == 'Sequence':
            m = map.getValue(key).getValue('Table')
            for d in data:
                self._mergeResponse(user, map, pattern, key, d, timestamp, m)
        elif method == 'Field':
            self._updateField(user, key, data, timestamp)
        elif method == 'Header':
            pass
        elif data.hasValue(pattern):
            self._mergeResource(user, map, pattern, data, timestamp)

    def _mergeData(self, map, index, key, data, timestamp, method):
        if method == 'Sequence':
            m = map.getValue(key).getValue('Table')
            for d in data:
                self._mergeData(map, index, key, d, timestamp, m)
        elif method == 'Tables':
            t = self.getTypeIndex(key, data)
            for k in data.getKeys():
                if k == 'Type':
                    continue
                d = data.getValue(k)
                print("DataSource._mergeData: 1 %s - %s - %s - %s" % (key, t, k, d))
                self._mergeField(key, index, t, k, d, timestamp)
        elif method == 'Field':
            print("DataSource._mergeData: 2 %s - %s" % (key, data))

    def _mergeResource(self, user, map, pattern, data, timestamp):
        index = self.getPeopleIndex(user, data.getValue(pattern))
        for key in data.getKeys():
            if key == pattern:
                continue
            d = data.getValue(key)
            method = map.getValue(key).getValue('Type')
            self._mergeData(map, index, key, data.getValue(key), timestamp, method)
        print("DataSource._mergeResource: %s" % (data.getValue(pattern), ))

    def _mergeField(self, table, index, typ, field, value, timestamp):
        label = self.getLabelIndex(field)
        print("DataSource._mergeField: 1: %s - %s - %s - %s" % (value, index, label, typ))
        if label is None:
            return
        call = self._getPreparedCall('update' + table)
        call.setString(1, value)
        call.setTimestamp(2, timestamp)
        call.setLong(3, index)
        call.setLong(4, label)
        if typ is not None:
            call.setLong(5, typ)
        row = call.executeUpdate()
        #call.close()
        if row != 1:
            call = self._getPreparedCall('insert' + table)
            call.setString(1, value)
            call.setLong(2, index)
            call.setLong(3, label)
            if typ is not None:
                call.setLong(4, typ)
            row = call.executeUpdate()
            #call.close()
            print("DataSource._mergeField: 2: %s - %s - %s" % (value, field, label))

    def _updateField(self, user, key, value, timestamp):
        print("DataSource._updateField: %s - %s" % (key, value))
        call = self._getDataSourceCall('update' + key)
        call.setString(1, value)
        call.setTimestamp(2, timestamp)
        call.setLong(3, user.People)
        row = call.executeUpdate()
        if row and user.MetaData.hasValue(key):
            oldvalue = user.MetaData.getValue(key)
            user.MetaData.setValue(key, value)
            print("DataSource._updateField: %s - %s" % (oldvalue, user.MetaData.getValue(key)))
        #call.close()

    def getPeopleIndex(self, user, resource):
        if self._PeopleIndex is None:
            self._PeopleIndex = self._getPeopleIndex()
        if resource not in self._PeopleIndex:
            people = self._insertResource(user, resource)
            if people is not None:
                self._PeopleIndex[resource] = people
        else:
            people = self._PeopleIndex[resource]
        return people

    def _getPeopleIndex(self):
        map = {}
        call = getDataSourceCall(self.Connection, 'getPeopleIndex')
        result = call.executeQuery()
        while result.next():
            map[result.getString(1)] = result.getLong(2)
        call.close()
        return map

    def _insertResource(self, user, resource):
        people = None
        call = self._getDataSourceCall('insertPeople')
        call.setString(1, resource)
        row = call.executeUpdate()
        #call.close()
        if row == 1:
            call = self._getDataSourceCall('getIdentity')
            result = call.executeQuery()
            if result.next():
                people = result.getLong(1)
                call = self._getDataSourceCall('insertConnection')
                call.setLong(1, user.People)
                call.setLong(2, people)
                row = call.executeUpdate()
            #call.close()
        return people

    def getTypeIndex(self, key, data):
        if self._Types is None:
            self._Types = self._getTypes()
        if not data.hasValue('Type'):
            if key in self._Types['Default']:
                print("DataSource.getTypeIndex() %s - %s **********************" % (key, self._Types['Default'][key]))
                return self._Types['Default'][key]
            return None
        value = data.getValue('Type')
        if value in self._Types['Index']:
            return self._Types['Index'][value]
        print("DataSource.getTypeIndex() %s ******************************" % value)
        idx = self._insertType(value)
        if idx is not None:
            self._Types['Index'][value] = idx
        print("DataSource.getTypeIndex() %s ******************************" % idx)
        return idx

    def _getTypes(self):
        map = {}
        for method in ('Index','Default'):
            map[method] = {}
            call = getDataSourceCall(self.Connection, 'getTypes' + method)
            result = call.executeQuery()
            while result.next():
               map[method][result.getString(1)] = result.getLong(2)
            call.close()
        return map

    def _insertType(self, value):
        identity = None
        call = self._getDataSourceCall('insertTypes')
        call.setString(1, value)
        call.setString(2, value)
        row = call.executeUpdate()
        #call.close()
        if row == 1:
            call = self._getDataSourceCall('getIdentity')
            result = call.executeQuery()
            if result.next():
                identity = result.getLong(1)
            #call.close()
        return identity

    def getLabelIndex(self, value):
        if self._LabelIndex is None:
            self._LabelIndex = self._getLabelIndex()
        if value in self._LabelIndex:
            return self._LabelIndex[value]
        print("DataSource.getLabelIndex() %s ******************************" % value)
        return None

    def _getLabelIndex(self):
        map = {}
        call = getDataSourceCall(self.Connection, 'getLabelIndex')
        result = call.executeQuery()
        while result.next():
            map[result.getString(1)] = result.getLong(2)
        call.close()
        return map

    def getFieldsMap(self, reverse):
        if self._FieldsMap is None:
            self._FieldsMap = self._getFieldsMap()
        if reverse:
            map = KeyMap(**{i: {'Map': j, 'Type': k, 'Table': l} for i, j, k, l in self._FieldsMap})
        else:
            map = KeyMap(**{j: {'Map': i, 'Type': k, 'Table': l} for i, j, k, l in self._FieldsMap})
        return map

    def _getFieldsMap(self):
        map = []
        call = getDataSourceCall(self.Connection, 'getFieldsMap')
        r = call.executeQuery()
        while r.next():
            map.append((r.getString(1), r.getString(2), r.getString(3), r.getString(4)))
        call.close()
        return tuple(map)

    def getPrimaryField(self):
        if self._PrimaryField is None:
            self._PrimaryField = self._getPrimaryField()
        return self._PrimaryField

    def _getPrimaryField(self):
        primary = ''
        call = getDataSourceCall(self.Connection, 'getPrimaryField')
        r = call.executeQuery()
        while r.next():
            primary = r.getString(1)
        call.close()
        return primary

    def _closeDataSourceCall(self):
        for call in self._Calls.values():
            call.close()
        self._Calls = {}

    def _getPreparedCall(self, name):
        if name in self._Calls:
            return self._Calls[name]
        # TODO: cannot use: call = self.Connection.prepareCommand(name, QUERY)
        # TODO: it trow a: java.lang.IncompatibleClassChangeError
        query = self.Connection.getQueries().getByName(name).Command
        call = self.Connection.prepareCall(query)
        self._Calls[name] = call
        return call

    def _getDataSourceCall(self, name):
        if name in self._Calls:
            return self._Calls[name]
        call = getDataSourceCall(self.Connection, name)
        self._Calls[name] = call
        return call
