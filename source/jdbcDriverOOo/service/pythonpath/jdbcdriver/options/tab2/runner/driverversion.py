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

from com.sun.star.awt import XCallback

from ....unotool import createService
from ....unotool import getCallBack

from ....jdbcdriver import g_service

from threading import Timer
import traceback


class DriverVersion(unohelper.Base,
                    XCallback):
    def __init__(self, ctx, callback, config, protocol):
        self._ctx = ctx
        self._config = config
        self._protocol = protocol
        self._driver = None
        self._versions = {}
        self._callback = callback
        self._asyncCall = getCallBack(ctx)
        self._steps = self._getCheckStep(ctx)

    def start(self):
        Timer(0.1, self._call).start()

    def notify(self, data):
        try:
            call, *args = next(self._steps)
            call(*args)
            Timer(0.1, self._call).start()
        except StopIteration:
            self._callback(self._versions)
        except Exception as e:
            print("DriverVersion.notify() ERROR: %s" % traceback.format_exc())
            self._callback(None)

    def stepInitDriver(self, ctx):
        self._driver = createService(ctx, g_service)

    def stepGetDriverVersion(self, protocol, url):
        print("DriverVersion.stepGetDriverVersion() protocol: %s - url: %s" % (protocol, self._protocol + url))
        connection = self._driver.connect(self._protocol + url, ())
        self._versions[protocol] = connection.getMetaData().getDriverVersion()
        connection.close()

    def _getCheckStep(self, ctx):
        yield self._getStepInitDriver(ctx)
        name = 'Properties/InMemoryDataBase/Value'
        if self._driver:
            config = self._config.getByName('Installed')
            for protocol in config.getElementNames():
                setting = config.getByName(protocol)
                if setting.hasByHierarchicalName(name):
                    url = setting.getByHierarchicalName(name)
                    if url:
                        yield self._getStepGetDriverVersion(protocol, url)

    def _call(self):
       self._asyncCall.addCallback(self, None)

    def _getStepInitDriver(self, ctx):
        return self.stepInitDriver, ctx

    def _getStepGetDriverVersion(self, protocol, url):
        return self.stepGetDriverVersion, protocol, url

