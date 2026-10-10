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

from .check import Check

from ..helper import installPackage
from ..helper import uninstallPackage

from ...unotool import getResourceLocation
from ...unotool import getSimpleFile

from ...configuration import g_identifier

import traceback


class Pypi(Check):
    def __init__(self, ctx, packages):
        super().__init__(ctx)
        self._installed = {}
        self._aborted = {}
        self._path = getResourceLocation(ctx, g_identifier, 'service/pythonpath')
        self.total = 0
        self.label1 = self.resolver.resolveString(411)
        self.label2 = self.resolver.resolveString(412)
        self.steps = self._getCheckStep(packages)

    def getHeader(self, **kwargs):
        return self.resolver.resolveString(421).format(**kwargs)

    def setReport(self, reports, success):
        if not success:
            reports.append(self._getReport(success))
        return self._getHeader(success), self._getResult(success), True

    def callback(self, callback, success=True, error=None):
        callback(self._getSuccess(success), error)

    def stepGetStepCount(self, packages):
        self.total += sum(i for i in self._getStepCount(packages))

    def stepUninstallPackage(self, package):
        uninstallPackage(package, self._path)

    def stepInstallPackage(self, package, version, url):
        if installPackage(url, self._path):
            self._installed[package] = version
        else:
            print("Pypi.stepInstallPackage() package: %s" % package)
            self._aborted[package] = version

    def _getCheckStep(self, packages):
        if not getSimpleFile(self._ctx).exists(self._path):
            return
        self.total += 1
        yield self._getStepGetStepCount(packages)
        for package, data in packages.items():
            version1, version2, url = data
            if version1 != version2:
                if version1:
                    yield self._getStepUninstallPackage(package)
                yield self._getStepInstallPackage(package, version2, url)

    def _getStepGetStepCount(self, packages):
        return 431, (), self.stepGetStepCount, packages

    def _getStepUninstallPackage(self, package):
        return 441, (package, ), self.stepUninstallPackage, package

    def _getStepInstallPackage(self, package, version, url):
        return 451, (package, ), self.stepInstallPackage, package, version, url

    def _getStepCount(self, packages):
        for package, data in packages.items():
            version1, version2, url = data
            if version1 != version2:
                yield 2 if version1 else 1

    def _getHeader(self, success):
        code = 461 if success else 462
        return self.resolver.resolveString(code)

    def _getReport(self, success):
        return self._getHeader(success), self._getResult(success), True

    def _getResult(self, success):
        modules = self._installed if success else self._aborted
        result = ', '.join(['%s version %s' % (module, version) for module, version in modules.items()])
        print("Pypi._getResult() result: %s" % result)
        return result

    def _getSuccess(self, success):
        return success and len(self._aborted) == 0

