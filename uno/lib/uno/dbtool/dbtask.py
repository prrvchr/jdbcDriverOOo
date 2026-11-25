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

from ..unotool.resource import Resource

from ..runner.error import RunnerException

from ..runner.task import Task

from .dbinit import createStaticTables
from .dbinit import createStaticIndexes
from .dbinit import createStaticForeignKeys
from .dbinit import getForeignKeys
from .dbinit import getIndexes
from .dbinit import getTableNames
from .dbinit import getTables
from .dbinit import setStaticTable

from .helper import checkConnection
from .helper import cleanDataBaseFolder
from .helper import createForeignKeys
from .helper import createTables
from .helper import createIndexes
from .helper import createOdbDocument
from .helper import createRoleAndPrivileges
from .helper import createViews
from .helper import executeQueries
from .helper import getConnectionInfos
from .helper import getDataBaseForeignKeys
from .helper import getDataBaseIndexes
from .helper import getDataBaseTables
from .helper import getDriverConnection
from .helper import getDriverInfos

from ..unotool import checkVersion
from ..unotool import getSimpleFile

from ..dbconfig import g_catalog
from ..dbconfig import g_csv
from ..dbconfig import g_drvinfos
from ..dbconfig import g_schema
from ..dbconfig import g_version

import traceback


class DBTask(Task):
    def __init__(self, ctx, path, dbname, user, pwd, infos=None):
        super().__init__(ctx, 'dbtool')
        self._path = path
        self._url = path + dbname
        self._odb = path + dbname + '.odb'
        self._sf = getSimpleFile(ctx)
        self._exists = self._sf.exists(self._odb)
        self._success = False
        self._version = None
        self._statics = None
        self._tables = None
        self._connection = None
        self._connected = False
        self._created = False
        self._statement = None
        self.total = 3 if self._exists else 5
        self._resources = {1: Resource(10101), 2: Resource(10102)}
        self.steps = self.getSetupSteps(ctx, user, pwd, infos)

    def getPath(self):
        return self._odb

    def withCallback(self, callback):
        self._callback = callback
        return self

    def getHeader(self, **kwargs):
        return self._resolver.resolveString(10201).format(**kwargs)

    def setReport(self, reports, success):
        result = self._getResult()
        if not success:
            reports.append((result, '', False))
        return result, ''

    def callback(self, success=True, error=None):
        if self._callback:
            self._callback(self._getSuccess(success), error)

    def getSetupSteps(self, ctx, user, pwd, infos):
        if not self._exists:
            yield self._getStepGetStepCount(ctx)
            yield self._getStepCleanDataBaseFolder()
        yield self._getStepGetConnection(ctx, user, pwd, infos)
        yield self._getStepCheckConnection()
        if not self._exists:
            yield from self._getSetupSteps(ctx)
        yield self._getStepFinalize(ctx)

    def _getSetupSteps(self, ctx, count=False):
        raise NotImplementedError

    def stepGetStepCount(self, ctx):
        self.total += sum(1 for _ in self._getSetupSteps(ctx, True))

    def stepCleanDataBaseFolder(self):
        try:
            cleanDataBaseFolder(self._sf, self._odb, self._path)
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepGetConnection(self, ctx, user, pwd, infos):
        new = not self._exists
        try:
            if new:
                infos = getDriverInfos(ctx, self._url, g_drvinfos, infos=infos)
            self._connection = getDriverConnection(ctx, self._url, user=user, pwd=pwd, infos=infos)
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepCheckConnection(self):
        new = not self._exists
        if self._connection:
            try:
                self._version = self._connection.getMetaData().getDriverVersion()
                if not checkConnection(self._connection, self._version, g_version, new):
                    self._connection.close()
                    self._connection = None
                else:
                    self._connected = True
                    if new:
                        self._tables = self._connection.getTables()
                        self._statement = self._connection.createStatement()
                    else:
                        self._connection.close()
                        self._connection = None
                        self._success = True
            except Exception as e:
                trace = traceback.format_exc()
                raise RunnerException(e, trace)

    def stepCreateStaticTables(self, tables):
        try:
            success, statics = createStaticTables(g_catalog, g_schema, self._tables, tables)
            if success:
                self._statics = statics
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepCreateStaticIndexes(self):
        try:
            createStaticIndexes(g_catalog, g_schema, self._tables)
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepCreateStaticForeignKeys(self, foreignkeys):
        try:
            createStaticForeignKeys(g_catalog, g_schema, self._tables, foreignkeys)
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepSetStaticTable(self):
        try:
            setStaticTable(self._statement, self._statics, g_csv, True)
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepCreateTables(self):
        try:
            infos = getConnectionInfos(self._connection, 'AutoIncrementCreation', 'RowVersionCreation')
            createTables(self._tables, getDataBaseTables(self._connection, self._statement, getTables(), getTableNames(), *infos))
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepCreateIndexes(self):
        try:
            createIndexes(self._tables, getDataBaseIndexes(self._statement, getIndexes()))
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepCreateForeignKeys(self):
        try:
            createForeignKeys(self._tables, getDataBaseForeignKeys(self._statement, getForeignKeys()))
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepCreateRoleAndPrivileges(self, privileges):
        try:
            groups = self._connection.getGroups()
            createRoleAndPrivileges(self._tables, groups, privileges(self._statement))
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepCreateFunctions(self, ctx, functions, *options):
        try:
            executeQueries(ctx, self._statement, functions, 'create%s', *options)
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepCreateViews(self, ctx, views):
        try:
            createViews(self._connection.getViews(), views(ctx, self._connection))
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepCreateProcedures(self, ctx, procedures, *options):
        try:
            executeQueries(ctx, self._statement, procedures, 'create%s', *options)
        except Exception as e:
            trace = traceback.format_exc()
            raise RunnerException(e, trace)

    def stepFinalize(self, ctx):
        try:
            if self._statement:
                self._statement.close()
                self._statement = None
            if self._connection:
                if self._created:
                    createOdbDocument(ctx, self._connection, self._odb)
                    self._success = True
                self._connection.close()
                self._connection = None
        except Exception:
            pass

    def _getStepGetStepCount(self, ctx):
        return 10301, (), self.stepGetStepCount, ctx

    def _getStepCleanDataBaseFolder(self):
        return 10401, (), self.stepCleanDataBaseFolder

    def _getStepGetConnection(self, ctx, user, pwd, infos):
        return 10501, (), self.stepGetConnection, ctx, user, pwd, infos

    def _getStepCheckConnection(self):
        return 10601, (), self.stepCheckConnection

    def _getStepCreateStaticTables(self, tables=None):
        return 10701, (), self.stepCreateStaticTables, tables

    def _getStepCreateStaticIndexes(self):
        return 10801, (), self.stepCreateStaticIndexes

    def _getStepCreateStaticForeignKeys(self, foreignkeys=None):
        return 10901, (), self.stepCreateStaticForeignKeys, foreignkeys

    def _getStepSetStaticTable(self):
        return 11001, (), self.stepSetStaticTable

    def _getStepCreateTables(self):
        return 11101, (), self.stepCreateTables

    def _getstepCreateIndexes(self):
        return 11201, (), self.stepCreateIndexes

    def _getstepCreateForeignKeys(self):
        return 11301, (), self.stepCreateForeignKeys

    def _getstepCreateRoleAndPrivileges(self, privileges):
        return 11401, (), self.stepCreateRoleAndPrivileges, privileges

    def _getStepCreateFunctions(self, ctx, functions, *options):
        return 11501, (), self.stepCreateFunctions, ctx, functions, *options

    def _getStepCreateViews(self, ctx, views):
        return 11601, (), self.stepCreateViews, ctx, views

    def _getStepCreateProcedures(self, ctx, procedures, *options):
        return 11701, (), self.stepCreateProcedures, ctx, procedures, *options

    def _getStepFinalize(self, ctx):
        return 11801, (), self.stepFinalize, ctx

    def _getResult(self):
        if not self._connected:
            code = 11901 if checkVersion(self._version, g_version) else 11902
        elif self._success:
            code = 11903 if self._exists else 11904
        else:
            code = 11905
        return self._resolver.resolveString(code)

    def _getSuccess(self, success):
        return success and self._success
