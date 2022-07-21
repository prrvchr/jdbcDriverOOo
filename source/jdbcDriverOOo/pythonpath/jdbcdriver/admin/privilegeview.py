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

import unohelper

from jdbcdriver import getDialog
from jdbcdriver import g_extension


class PrivilegeView(unohelper.Base):
    def __init__(self, ctx, catalog, schema, title, privileges, grantables, flags):
        self._dialog = getDialog(ctx, g_extension, 'PrivilegesDialog')
        self._dialog.Title = title
        self._getCatalog().Text = catalog
        self._getSchema().Text = schema
        for index, flag in flags.items():
            control = self._getPrivilege(index)
            control.State = flag == privileges & flag
            control.Model.Enabled = flag == grantables & flag

    def execute(self):
        return self._dialog.execute()

    def getPrivileges(self, flags):
        privileges = 0
        for index, flag in flags.items():
            control = self._getPrivilege(index)
            privileges += flag if control.State else 0
        return privileges

    def dispose(self):
        self._dialog.dispose()

    def _getCatalog(self):
        return self._dialog.getControl('Label2')

    def _getSchema(self):
        return self._dialog.getControl('Label4')

    def _getPrivilege(self, index):
        return self._dialog.getControl('CheckBox%s' % index)

