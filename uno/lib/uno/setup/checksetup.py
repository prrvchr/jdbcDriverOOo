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

from ..unotool import getCallBack
from ..unotool import getSimpleFile

from .helper import checkAgent
from .helper import checkExtensions
from .helper import checkJava
from .helper import checkPython

from ..configuration import g_identifier

from threading import Timer
import traceback


class CheckSetup(unohelper.Base,
                 XCallback):
    def __init__(self, ctx, callback, database=None, java=None, agent=None, python=False, **extensions):
        self._ctx = ctx
        self._callback = callback
        self._requirements = '/requirements.txt'
        self._asyncCall = getCallBack(ctx)
        steps = []
        self._results = self._getDefaultResults(True)
        if extensions:
            steps.append((self._checkExtensions, extensions))
            self._results['extension'] = False
        if java:
            steps.append((self._checkJava, java, agent))
            self._results['java'] = False
        if agent:
            self._results['agent'] = False
        if database:
            steps.append((self._checkDataBase, database))
            self._results['database'] = False
        if python:
            steps.append((self._checkPython))
            self._results['python'] = False
        self._steps = iter(steps)

    def start(self):
        Timer(0.1, self._call).start()

    def notify(self, data):
        try:
            call, *args = next(self._steps)
            call(*args)
            Timer(0.1, self._call).start()
        except StopIteration:
            self._callback(**self._results)
        except Exception as e:
            self._callback(**self._getDefaultResults(False))

    def _call(self):
       self._asyncCall.addCallback(self, None)

    def _checkExtensions(self, extensions):
        result = checkExtensions(self._ctx, extensions)
        self._results['extension'] = len(result) == 0

    def _checkJava(self, java, agent):
        code, _, _ = checkJava(self._ctx, java)
        success = code == 0
        if success and agent:
            self._results['agent'] = checkAgent(self._ctx, *agent)
        self._results['java'] = success

    def _checkDataBase(self, database):
        self._results['database'] = getSimpleFile(self._ctx).exists(database)

    def _checkPython(self):
        modules, _ = checkPython(self._ctx, g_identifier)
        self._results['python'] = len(modules) == 0

    def _getDefaultResults(self, value):
        return {'extension': value, 'java': value, 'agent':value, 'database': value, 'python': value}

    @classmethod
    def isValid(cls, ctx, database=None, java=None, python=False, **extensions):
        code = None
        if extensions and len(checkExtensions(ctx, extensions)) > 0:
            return False
        if java:
            code, _, _ = checkJava(ctx, java)
            if code > 0:
                return False
        if database and not getSimpleFile(ctx).exists(database):
            return False
        if python:
            modules, _ = checkPython(ctx, g_identifier)
            if len(modules) > 0:
                return False
        return True

