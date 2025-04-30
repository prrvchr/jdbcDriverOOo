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

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from com.sun.star.uno import Exception as UnoException

from ..helper import checkConfiguration

from ..logger import getLogger

from ..unotool import createService

from ..configuration import g_defaultlog
from ..configuration import g_basename
from ..configuration import g_service

import traceback


class OptionsModel():
    def __init__(self, ctx, url=None):
        self._ctx = ctx
        self._url = url

# OptionsModel getter methods
    def getDriverVersion(self, apilevel):
        version = 'N/A'
        try:
            checkConfiguration(self._ctx)
            if self._url is not None:
                driver = createService(self._ctx, g_service)
                # FIXME: If jdbcDriverOOo extension has not been installed then driver is None
                if driver is not None:
                    connection = driver.connect(self._url, ())
                    version = connection.getMetaData().getDriverVersion()
                    connection.close()
                    driver.dispose()
        except UnoException as e:
            logger = getLogger(self._ctx, g_defaultlog, g_basename)
            logger.logprb(SEVERE, 'OptionsModel', 'getDriverVersion', 102, g_service, apilevel, e.Message)
        return version

