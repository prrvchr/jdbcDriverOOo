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

import uno

from .check import Check

from ..helper import getInstalledPackages
from ..helper import getPackageSimpleData
from ..helper import getPackageVersionData
from ..helper import getRequirementversion
from ..helper import isLinuxDistribution
from ..helper import parsePackageName
from ..helper import parsePackageSimpleData
from ..helper import parsePackageVersionData
from ..helper import parseRequirements

from ...unotool import getConfiguration
from ...unotool import getResourceLocation
from ...unotool import getSimpleFile

from ...configuration import g_identifier

import traceback


class Python(Check):
    def __init__(self, ctx):
        super().__init__(ctx)
        self._packages ={}
        self._path = getResourceLocation(ctx, g_identifier, 'requirements.txt')
        self._distro = False
        self._update = 0
        self._data = None
        self._url = None
        self._version = None
        self._success = True
        self.total = 0
        self.label1 = self.resolver.resolveString(311)
        self.label2 = self.resolver.resolveString(312)
        self.packages = {}
        self.steps = self._getCheckStep(ctx)

    def getHeader(self, **kwargs):
        return self.resolver.resolveString(321).format(**kwargs)

    def setReport(self, reports, success):
        return self._getHeader(success), self._getResult(success)

    def callback(self, callback, success=True, error=None):
        callback(success, self._success, error)

    def stepGetInstalledPackages(self, ctx):
        self._distro = isLinuxDistribution(ctx)
        self._update = getConfiguration(ctx, g_identifier).getByName('SetupUpdate')
        self._packages = getInstalledPackages()

    def stepGetStepCount(self):
        self.total += sum(i for i in self._getStepCount())

    def stepGetPackageVersionData(self, requirement, version):
        self._data = getPackageVersionData(requirement, version)

    def stepGetPackageSimpleData(self, requirement):
        self._data = getPackageSimpleData(requirement)

    def stepParsePackageVersionData(self, requirement):
        self._version, self._url = parsePackageVersionData(requirement, self._data)
        self._data = None

    def stepParsePackageSimpleData(self, requirement):
        self._version, self._url = parsePackageSimpleData(requirement, self._update, self._data)
        self._data = None

    def _getCheckStep(self, ctx):
        try:
            if not getSimpleFile(ctx).exists(self._path):
                return
            self.total += 2
            yield self._getStepGetInstalledPackages(ctx)
            yield self._getStepGetStepCount()
            for requirement, version1, version2 in self._parsePackages():
                print("Python._getCheckStep() requirement: %s - version1: %s - version2: %s" % (requirement.name, version1, version2))
                if requirement.url:
                    self.packages[requirement.name] = version1, version2, None
                    self._success &= version1 == version2
                elif self._distro and version1:
                    self.packages[requirement.name] = version1, version2, None
                    self._success &= version1 == version2
                elif not version1 or version1 != version2:
                    if version2:
                        yield self._getStepGetPackageVersionData(requirement, version2)
                        if self._data:
                            yield self._getStepParsePackageVersionData(requirement, version2)
                            if self._version and self._url:
                                self.packages[requirement.name] = version1, self._version, self._url
                                self._version = self._url = None
                                self._success = False
                            else:
                                print("Python._getCheckStep() 1 requirement: %s - version1: %s" % (requirement.name, version1))
                    else:
                        yield self._getStepGetPackageSimpleData(requirement)
                        if self._data:
                            yield self._getStepParsePackageSimpleData(requirement)
                            if self._version and self._url:
                                #print("Python._getCheckStep() version1: %s - version2: %s - url: %s" % (version1, self._version, self._url))
                                self.packages[requirement.name] = version1, self._version, self._url
                                self._success &= version1 == self._version
                                self._version = self._url = None
                            else:
                                print("Python._getCheckStep() 2 requirement: %s - version1: %s - version2: %s - url: %s" % (requirement.name, version1, self._version, self._url))
                                
                else:
                    self.packages[requirement.name] = version1, version2, None
        except Exception as e:
            print("Python._getCheckStep() ERROR: %s" % traceback.format_exc())

    def _getStepGetInstalledPackages(self, ctx):
        return 331, (), self.stepGetInstalledPackages, ctx

    def _getStepGetStepCount(self):
        return 341, (), self.stepGetStepCount

    def _getStepGetPackageVersionData(self, requirement, version):
        return 351, (requirement.name, version), self.stepGetPackageVersionData, requirement, version

    def _getStepGetPackageSimpleData(self, requirement):
        return 361, (requirement.name, ), self.stepGetPackageSimpleData, requirement

    def _getStepParsePackageVersionData(self, requirement, version):
        return 371, (requirement.name, version), self.stepParsePackageVersionData, requirement

    def _getStepParsePackageSimpleData(self, requirement):
        return 381, (requirement.name, ), self.stepParsePackageSimpleData, requirement

    def _getStepCount(self):
        for requirement, version1, version2 in self._parsePackages():
            if requirement.url or self._distro:
                continue
            if not version1 or version1 != version2:
                yield 2

    def _parsePackages(self):
        for requirement in parseRequirements(self._path):
            version1 = self._packages.get(parsePackageName(requirement.name))
            version2 = getRequirementversion(requirement, self._update)
            yield requirement, version1, version2

    def _getHeader(self, success):
        code = 391 if success else 392
        return self.resolver.resolveString(code)

    def _getResult(self, success):
        if success:
            modules = ('%s version %s' % (package, data[1]) for package, data in self.packages.items())
            result = ', '.join(modules)
            print("Python._getResult() installed result: %s" % result)
        else:
            modules = ('%s version %s' % (package, data[1]) for package, data in self.packages.items() if data[0] != data[1])
            result = ', '.join(modules)
            print("Python._getResult() missing result: %s" % result)
        return result

