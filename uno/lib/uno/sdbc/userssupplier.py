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

from com.sun.star.beans.PropertyAttribute import BOUND
from com.sun.star.beans.PropertyAttribute import READONLY

from com.sun.star.uno import XWeak
from com.sun.star.uno import XAdapter

from com.sun.star.sdbcx import XGroupsSupplier
from com.sun.star.sdbcx import XUser

from com.sun.star.container import XNameAccess
from com.sun.star.container import XIndexAccess
from com.sun.star.container import XEnumerationAccess
from com.sun.star.container import XElementAccess

from ..unolib import PropertySet

from ..unotool import getProperty

from ..dbtool import getSequenceFromResult
from ..dbtool import getKeyMapSequenceFromResult

from ..dbqueries import getSqlQuery

import traceback


class UsersSupplier(unohelper.Base,
                    XWeak,
                    XAdapter,
                    XNameAccess,
                    XIndexAccess,
                    XEnumerationAccess,
                    XElementAccess):
    def __init__(self, ctx, connection):
        print("DataContainer.__init__() 1")
        query = getSqlQuery(ctx, 'getUsers')
        result = connection.createStatement().executeQuery(query)
        users = getSequenceFromResult(result)
        #query = getSqlQuery(self._ctx, 'getPrivileges')
        #result = self._connection.createStatement().executeQuery(query)
        #privileges = getKeyMapSequenceFromResult(result)
        self._elements = {user: DataBaseUser(ctx, connection, user) for user in users}
        self._typename = 'string'
        print("DataContainer.__init__()")

    # XWeak
    def queryAdapter(self):
        print("DataContainer.queryAdapter()")
        return self

    # XAdapter
    def queryAdapted(self):
        print("DataContainer.queryAdapter()")
        return self
    def addReference(self, reference):
        pass
    def removeReference(self, reference):
        pass

    # XNameAccess
    def getByName(self, name):
        print("DataContainer.getByName() %s" % name)
        return self._elements[name]
    def getElementNames(self):
        elements = tuple(self._elements.keys())
        print("DataContainer.getElementNames() %s" % (elements, ))
        return elements
    def hasByName(self, name):
        print("DataContainer.hasByName() %s" % name)
        return name in self._elements

    # XIndexAccess
    def getCount(self):
        print("DataContainer.getCount()")
        return len(self._elements)
    def getByIndex(self, index):
        print("DataContainer.getByIndex() %s" % index)
        return None

    # XEnumerationAccess
    def createEnumeration(self):
        print("DataContainer.createEnumeration()")

    # XElementAccess
    def getElementType(self):
        print("DataContainer.getElementType()")
        return uno.getTypeByName(self._typename)
    def hasElements(self):
        print("DataContainer.hasElements()")
        return len(self._elements) != 0


class DataBaseUser(unohelper.Base,
                   XUser,
                   XWeak,
                   XAdapter,
                   XGroupsSupplier,
                   PropertySet):
    def __init__(self, ctx, connection, name):
        self._ctx = ctx
        self._connection = connection
        self.Name = name
        print("DataBaseUser.__init__() %s" % name)

    # XWeak
    def queryAdapter(self):
        print("DataBaseUser.queryAdapter()")
        return self

    # XAdapter
    def queryAdapted(self):
        print("DataBaseUser.queryAdapted()")
        return self
    def addReference(self, reference):
        pass
    def removeReference(self, reference):
        pass

    # XUser
    def changePassword(self, oldpwd, newpwd):
        print("DataBaseUser.changePassword()")
        query = getSqlQuery(self._ctx, 'changePassword', newpwd)
        print("DataBaseUser.changePassword() %s" % query)
        result = self._connection.createStatement().executeUpdate(query)
        print("DataBaseUser.changePassword() %s" % result)

    # XAuthorizable
    def getPrivileges(self, objname, objtype):
        print("DataBaseUser.getPrivileges() %s - %s" % (objname, objtype))
        pass
    def getGrantablePrivileges(self, objname, objtype):
        print("DataBaseUser.getGrantablePrivileges() %s - %s" % (objname, objtype))
        pass
    def grantPrivileges(self, objname, objtype, objprivilege):
        print("DataBaseUser.grantPrivileges() %s - %s - %s" % (objname, objtype, objprivilege))
        pass
    def revokePrivileges(self, objname, objtype, objprivilege):
        print("DataBaseUser.revokePrivileges() %s - %s - %s" % (objname, objtype, objprivilege))
        pass

    # XGroupsSupplier
    def getGroups(self):
        print("DataBaseUser.getGroups()")
        return None

    # XPropertySet
    def _getPropertySetInfo(self):
        properties = {}
        properties['Name'] = getProperty('Name', 'string', READONLY)
        return properties
