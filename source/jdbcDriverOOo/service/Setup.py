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

import unohelper

from com.sun.star.lang import XServiceInfo
from com.sun.star.task import XAsyncJob

from jdbcdriver import SetupManager

from jdbcdriver import findFrame
from jdbcdriver import getJobArguments
from jdbcdriver import getJobArgument
from jdbcdriver import notifyDispatch

from jdbcdriver import g_agent
from jdbcdriver import g_extension
from jdbcdriver import g_identifier
from jdbcdriver import g_java
from jdbcdriver import g_protocol
from jdbcdriver import g_script
from jdbcdriver import g_service
from jdbcdriver import g_setup

import traceback


# pythonloader looks for a static g_ImplementationHelper variable
g_ImplementationHelper = unohelper.ImplementationHelper()
g_ImplementationName = 'io.github.prrvchr.jdbcDriverOOo.Setup'
g_ServiceNames = ('io.github.prrvchr.jdbcDriverOOo.Setup',
                  'com.sun.star.task.Job')


class Setup(unohelper.Base,
            XServiceInfo,
            XAsyncJob):
    def __init__(self, ctx):
        self._ctx = ctx

    # XAsyncJob
    def executeAsync(self, arguments, listener):
        try:
            kwargs = getJobArguments(arguments)
            frame = findFrame(self._ctx, g_setup)
            if frame:
                frame.getContainerWindow().toFront()
                dispatch = getJobArgument(kwargs)
                if dispatch:
                    notifyDispatch(self, dispatch)
            else:
                java = g_extension, g_java, g_script
                agent = g_service, g_protocol, g_agent
                manager = SetupManager(self._ctx, self, java=java, agent=agent, python=True, name=g_setup, **kwargs)
        except Exception as e:
            # FIXME: It is essential to notify LibreOffice of
            # FIXME: the Job's completion so as not to block its loading.
            print("Setup.executeAsync() ERROR: %s" % traceback.format_exc())
            pass
        finally:
            if listener is not None:
                listener.jobFinished(self, None)
        return None

    # XServiceInfo
    def supportsService(self, service):
        return g_ImplementationHelper.supportsService(g_ImplementationName, service)
    def getImplementationName(self):
        return g_ImplementationName
    def getSupportedServiceNames(self):
        return g_ImplementationHelper.getSupportedServiceNames(g_ImplementationName)


g_ImplementationHelper.addImplementation(Setup,
                                         g_ImplementationName,
                                         g_ServiceNames)

