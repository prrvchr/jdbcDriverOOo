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

from ..helper import getExtensionName
from ..helper import getInstalledPackages
from ..helper import getOsLocation
from ..helper import parsePackageName

from ...unotool import createService
from ...unotool import getResourceLocation

from ...configuration import g_identifier

import importlib
import os
import traceback


class Conflict(Check):
    def __init__(self, ctx, packages, url):
        super().__init__(ctx)
        self._paths = {}
        self._packages = None
        self._location = None
        self._extensions = None
        self._results = {}
        self._parse = lambda x: x[1]
        dists = list(importlib.metadata.distributions())
        self.total = len(dists) + 1
        self.label1 = self.resolver.resolveString(511)
        self.label2 = self.resolver.resolveString(512)
        self.steps = self._getCheckStep(ctx, dists, packages, url)

    def getHeader(self, **kwargs):
        return self.resolver.resolveString(521).format(**kwargs)

    def setReport(self, reports, success):
        reports.append(self._getReport())
        return self._getHeader(), self._getResult(), True

    def callback(self, callback, success=True, error=None):
        callback(True, error)

    def stepInitialize(self, ctx, packages, url):
        if url is None:
            url = getResourceLocation(ctx, g_identifier)
        self._location = getOsLocation(url)
        if not packages:
            packages = getInstalledPackages(self._location)
            self._parse = lambda x: x
        self._packages = packages

    def stepGetPackages(self, name, dist):
        if name and parsePackageName(name) in self._packages:
            version = self._parse(self._packages[parsePackageName(name)])
            path = os.path.normpath(dist.locate_file(''))
            if not path.startswith(self._location):
                if dist.version != version:
                    if not path in self._paths:
                        self._paths[path] = []
                    self._paths[path].append((name, dist.version))

    def stepGetExtensions(self, ctx):
        service = '/singletons/com.sun.star.deployment.PackageInformationProvider'
        self._provider = ctx.getValueByName(service)
        self._extensions = self._provider.getExtensionList()

    def stepGetResults(self, identifier):
        location = getOsLocation(self._provider.getPackageLocation(identifier))
        for path in self._paths:
            if path.startswith(location):
                self._results[getExtensionName(location, identifier)] = self._paths[path]

    def _getCheckStep(self, ctx, dists, packages, url):
        yield self._getStepInitialize(ctx, packages, url)
        for dist in dists:
            yield self._getStepGetPackages(dist)
        if self._paths:
            self.total += 1
            yield self._getStepGetExtensions(ctx)
            if self._extensions:
                self.total += len(self._extensions)
                for identifier, version in self._extensions:
                    yield self._getStepGetResults(identifier)

    def _getStepInitialize(self, ctx, packages, url):
        return 531, (), self.stepInitialize, ctx, packages, url

    def _getStepGetPackages(self, dist):
        name = dist.metadata.get('Name')
        return 541, (name, ), self.stepGetPackages, name, dist

    def _getStepGetExtensions(self, ctx):
        return 551, (), self.stepGetExtensions, ctx

    def _getStepGetResults(self, identifier):
        return 561, (), self.stepGetResults, identifier

    def _getHeader(self):
        return self.resolver.resolveString(571)

    def _getReport(self):
        return self._getHeader(), self._getResult(), True

    def _getPackages(self, packages):
        return ', '.join(f'{pkg} version {ver}' for pkg, ver in packages)

    def _getResult(self):
        return '\n'.join(f'{ext}: {self._getPackages(pkgs)}' for ext, pkgs in self._results.items())

