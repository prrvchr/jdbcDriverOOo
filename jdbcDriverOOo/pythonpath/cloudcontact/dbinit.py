#!
# -*- coding: utf_8 -*-


from unolib import KeyMap
from unolib import getResourceLocation
from unolib import getSimpleFile

from .dbconfig import g_path
from .dbqueries import getSqlQuery
from .dbtools import getTablesAndStatements
from .dbtools import getDataSourceCall
from .dbtools import getSequenceFromResult
from .dbtools import getDataFromResult
from .dbtools import getKeyMapFromResult
from .dbtools import registerDataSource
from .dbtools import executeQueries
from .dbtools import executeSqlQueries
from .dbtools import getDataSourceConnection
from .dbtools import createDataSource
from .dbtools import checkDataBase
from .dbtools import createStaticTable

import traceback


def getDataSourceUrl(ctx, dbcontext, dbname, plugin, register):
    error = None
    location = getResourceLocation(ctx, plugin, g_path)
    url = '%s/%s.odb' % (location, dbname)
    if not getSimpleFile(ctx).exists(url):
        datasource = createDataSource(dbcontext, location, dbname)
        error = _createDataBase(ctx, datasource)
        if error is None:
            datasource.DatabaseDocument.storeAsURL(url, ())
            if register:
                registerDataSource(dbcontext, dbname, url)
    return url, error

def _createDataBase(ctx, datasource):
    connection, error = getDataSourceConnection(datasource)
    if error is not None:
        return error
    error = checkDataBase(connection)
    if error is None:
        print("dbinit._createDataBase()")
        statement = connection.createStatement()
        createStaticTable(statement, _getStaticTables())
        tables, statements = getTablesAndStatements(statement)
        executeSqlQueries(statement, tables)
        _createPreparedStatement(ctx, datasource, statements)
        executeQueries(statement, _getQueries())
        _createDynamicView(statement)
        #mri = ctx.ServiceManager.createInstance('mytools.Mri')
        #mri.inspect(connection)
    connection.close()
    connection.dispose()
    return error

def _getTableColumns(connection, tables):
    columns = {}
    metadata = connection.MetaData
    for table in tables:
        columns[table] = _getColumns(metadata, table)
    return columns

def _getColumns(metadata, table):
    columns = []
    result = metadata.getColumns("", "", table, "%")
    while result.next():
        column = '"%s"' % result.getString(4)
        print("DbTools._getColumns() %s - %s" % (table, column))
        columns.append(column)
    return columns

def _createPreparedStatement(ctx, datasource, statements):
    queries = datasource.getQueryDefinitions()
    for name, sql in statements.items():
        if not queries.hasByName(name):
            query = ctx.ServiceManager.createInstance("com.sun.star.sdb.QueryDefinition")
            query.Command = sql
            queries.insertByName(name, query)
    #datasource.DatabaseDocument.store()
    #mri = ctx.ServiceManager.createInstance('mytools.Mri')
    #mri.inspect(datasource)

def _createDynamicView(statement):
    views, triggers = _getViewsAndTriggers(statement)
    executeSqlQueries(statement, views)
    for trigger in triggers:
        print("dbinit._createDynamicView(): %s" % trigger)

def _getViewsAndTriggers(statement):
    c1 = []
    s1 = []
    f1 = []
    queries = []
    triggers = []
    call = getDataSourceCall(statement.getConnection(), 'getViews')
    tables = getSequenceFromResult(statement.executeQuery(getSqlQuery('getViewName')))
    for table in tables:
        call.setString(1, table)
        result = call.executeQuery()
        while result.next():
            c2 = []
            s2 = []
            f2 = []
            trigger = {}
            data = getDataFromResult(result)
            view = data['View']
            ptable = data['PrimaryTable']
            pcolumn = data['PrimaryColumn']
            ftable = data['ForeignTable']
            fcolumn = data['ForeignColumn']
            labelid = data['LabelId']
            typeid = data['TypeId']
            c1.append('"%s"' % view)
            c2.append('"%s"' % pcolumn)
            c2.append('"Value"')
            s1.append('"%s"."Value"' % view)
            s2.append('"%s"."%s"' % (table, pcolumn))
            s2.append('"%s"."Value"' % table)
            f = 'LEFT JOIN "%s" ON "%s"."%s"="%s"."%s"' % (view, ftable, fcolumn, view, pcolumn)
            f1.append(f)
            f2.append('"%s"' % table)
            f = 'JOIN "Labels" ON "%s"."Label"="Labels"."Label" AND "Labels"."Label"=%s'
            f2.append(f % (table, labelid))
            if typeid:
                f = 'JOIN "Types" ON "%s"."Type"="Types"."Type" AND "Types"."Type"=%s'
                f2.append(f % (table, typeid))
            format = (view, ','.join(c2), ','.join(s2), ' '.join(f2))
            query = getSqlQuery('createView', format)
            print("dbtool._getCreateViewQueries(): 4 %s" % query)
            queries.append(query)
            triggers.append(getSqlQuery('createTriggerUpdateAddressBook', data))
    call.close()
    if queries:
        c1.insert(0, '"%s"' % pcolumn)
        s1.insert(0, '"%s"."%s"' % (ftable, fcolumn))
        f1.insert(0, 'JOIN "%s" ON "%s"."%s"="%s"."%s"' % (ftable, ptable, pcolumn, ftable, pcolumn))
        f1.insert(0, '"%s"' % ptable)
        f1.append('WHERE "%s"."%s"=CURRENT_USER' % (ptable, pcolumn))
        format = ('AddressBook', ','.join(c1), ','.join(s1), ' '.join(f1))
        query = getSqlQuery('createView', format)
        queries.append(query)
        queries.append( getSqlQuery('grantRole'))
        print("dbtool._getCreateViewQueries(): 5 %s" % query)
    return queries, triggers

def _getStaticTables():
    tables = ('Tables',
              'Types',
              'Columns',
              'TableType',
              'TableColumn',
              'Fields',
              'Labels',
              'TableLabel',
              'Settings')
    return tables

def _getQueries():
    return ('createRole', )
