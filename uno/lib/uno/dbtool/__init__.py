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

from .helper import checkConnection
from .helper import checkDataBase
from .helper import createDataSource
from .helper import createForeignKeys
from .helper import createIndexes
from .helper import createOdbDocument
from .helper import createRoleAndPrivileges
from .helper import createTables
from .helper import createUser
from .helper import createViews
from .helper import currentDateTime
from .helper import currentDateTimeInTZ
from .helper import currentUnoDateTime
from .helper import executeQueries
from .helper import executeSqlQueries
from .helper import getConnectionInfo
from .helper import getConnectionInfos
from .helper import getConnectionUrl
from .helper import getDataBaseConnection
from .helper import getDataBaseInfo
from .helper import getDataBaseUrl
from .helper import getDataBaseTables
from .helper import getDataBaseIndexes
from .helper import getDataBaseForeignKeys
from .helper import getDataFromResult
from .helper import getDataSource
from .helper import getDataSourceCall
from .helper import getDataSourceClassPath
from .helper import getDataSourceConnection
from .helper import getDataSourceLocation
from .helper import getDataSourceInfo
from .helper import getDataSourceJavaInfo
from .helper import getDateTimeInTZToString
from .helper import getDateTimeFromString
from .helper import getDateTimeToString
from .helper import getDictFromResult
from .helper import getDictSequenceFromResult
from .helper import getDriverConnection
from .helper import getDriverInfos
from .helper import getDriverPropertyInfo
from .helper import getDriverPropertyInfos
from .helper import getObjectFromResult
from .helper import getObjectSequenceFromResult
from .helper import getResultValue
from .helper import getRowDict
from .helper import getRowValue
from .helper import getSequenceFromResult
from .helper import getSqlException
from .helper import getTablePrivileges
from .helper import getUnoType
from .helper import getValueFromResult
from .helper import registerDataSource
from .helper import toUnoDateTime

from .dbinit import createStaticTables
from .dbinit import createStaticIndexes
from .dbinit import createStaticForeignKeys
from .dbinit import setStaticTable
from .dbinit import getTableNames
from .dbinit import getTables
from .dbinit import getIndexes
from .dbinit import getForeignKeys
from .dbinit import getPrivileges
