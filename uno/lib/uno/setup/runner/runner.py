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

from .cancel import CancelException

from ...unotool import getCallBack

from threading import Timer
import traceback


class Runner(unohelper.Base,
             XCallback):
    def __init__(self, ctx, check, progress, cancel):
        self._check = check
        self._total = check.total
        self._progress = progress
        self._cancel = cancel
        self._step = 0
        self._asyncCall = getCallBack(ctx)

    def start(self):
        if self._progress:
            self._progress.start(self._check.label1, self._check.total)
        Timer(0.1, self._call).start()

    def notify(self, data):
        try:
            if self._cancel.isSet():
                raise CancelException()

            if self._check.isExtended(self._total):
                self._progress.start(self._check.label2, self._check.total)
                self._progress.setValue(self._step)
                self._total = self._check.total

            resource, args, call, *kwargs = next(self._check.steps)
            self._step += 1
            if self._progress:
                self._progress.setText(self._check.resolver.resolveString(resource) % args)
                self._progress.setValue(self._step)
            call(*kwargs)
            Timer(0.1, self._call).start()

        except StopIteration:
            try:
                if self._progress:
                    self._progress.end()
                self._check.callback(True)
            except Exception:
                print("Runner.notify() StopIteration ERROR: %s" % traceback.format_exc())

        except CancelException:
            try:
                if self._progress:
                    self._progress.end()
                self._check.callback(False)
            except Exception:
                print("Runner.notify() CancelException ERROR: %s" % traceback.format_exc())

        except Exception as e:
            print("Runner.notify() ERROR: %s" % traceback.format_exc())
            self._check.callback(False)

    def _call(self):
       self._asyncCall.addCallback(self, None)

