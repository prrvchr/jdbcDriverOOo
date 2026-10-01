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

from ..helper import checkPython
from ..helper import getPackageCount

from ...unotool import getPathSubstitution
from ...unotool import getResourceLocation
from ...unotool import getSimpleFile

from ...configuration import g_identifier

import traceback


class Python(Check):
    def __init__(self, ctx, callback):
        super().__init__(ctx, callback)
        self._installed = []
        self._missing = []
        self._url = None
        self._requirements = 'requirements.txt'
        self.total = 1
        self.label1 = self.resolver.resolveString(311)
        self.label2 = self.resolver.resolveString(312)
        self.steps = self._getCheckStep()

    def getHeader(self, **kwargs):
        return self.resolver.resolveString(321).format(**kwargs)

    def getResults(self, success):
        return self._getHeader(), self._getResult()

    def getLastPage(self):
        print("Python.getLastPage() ***********************************")

    def getModules(self):
        if len(self._missing):
            return self._missing
        return self._installed

    def callback(self, success):
        self._callback(self._getSuccess(success))

    def stepGetRequirementUrl(self):
        self._url = getResourceLocation(self._ctx, g_identifier, self._requirements)

    def stepGetPackageCount(self):
        self.total += getPackageCount(self._url)

    def stepAddInstalled(self, module):
        self._installed.append(module)

    def stepAddMissing(self, module):
        self._missing.append(module)

    def _getCheckStep(self):
        yield self._getStepGetRequirementUrl()
        if not getSimpleFile(self._ctx).exists(self._url):
            return
        self.total += 1
        yield self._getStepGetPackageCount()
        yield from checkPython(self._url, self._getStepAddInstalled, self._getStepAddMissing)

    def _getStepGetRequirementUrl(self):
        return 331, (), self.stepGetRequirementUrl

    def _getStepGetPackageCount(self):
        return 341, (), self.stepGetPackageCount

    def _getStepAddInstalled(self, module):
        return 351, (module, ), self.stepAddInstalled, module

    def _getStepAddMissing(self, module):
        return 361, (module, ), self.stepAddMissing, module

    def _getSuccess(self, success):
        return success and len(self._missing) == 0

    def _getHeader(self):
        code = 371 if len(self._missing) else 372
        return self.resolver.resolveString(code)

    def _getResult(self):
        if len(self._missing):
            return ', '.join(self._missing)
        return ', '.join(self._installed)

