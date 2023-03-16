#!
# -*- coding: utf-8 -*-

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

import uno
import unohelper

from com.sun.star.logging.LogLevel import ALL
from com.sun.star.logging.LogLevel import OFF

from com.sun.star.logging import XLogHandler

from ..unotool import createService
from ..unotool import getResourceLocation
from ..unotool import getSimpleFile

from ..configuration import g_identifier


class LogHandler(unohelper.Base,
                 XLogHandler):
    def __init__(self, ctx, name, listener, level=ALL):
        encoding = 'UTF-8'
        self._encoding = encoding
        self._delimiter = '\n'
        self._formatter = createService(ctx, 'com.sun.star.logging.PlainTextFormatter').create()
        path = 'log/%s' % name
        self._url = getResourceLocation(ctx, g_identifier, path)
        self._sf = getSimpleFile(ctx)
        if not self._sf.exists(self._url):
            self._createLoggerFile()
        self._output = createService(ctx, 'com.sun.star.io.TextOutputStream')
        self._output.setEncoding(encoding)
        self._input = createService(ctx, 'com.sun.star.io.TextInputStream')
        self._input.setEncoding(encoding)
        self._openLoggerFile()
        self._listner = listener
        self._level = level
        self._listeners = []

# XLogHandler
    @property
    def Encoding(self):
        return self._encoding
    @Encoding.setter
    def Encoding(self, value):
        self._encoding = value
        self._output.setEncoding(value)

    @property
    def Formatter(self):
        return self._formatter
    @Formatter.setter
    def Formatter(self, value):
        self._formatter = value

    @property
    def Level(self):
        return self._level
    @Level.setter
    def Level(self, value):
        self._level = value

    def flush(self):
        pass

    def publish(self, record):
        # TODO: Need to do a callback with the record
        if self._level <= record.Level < OFF:
            msg = self._formatRecord(record)
            print("Handler.publish() Message: %s" % msg)
            self._output.writeString(msg)
            return True
        return False

# XComponent <- XLogHandler
    def dispose(self):
        event = uno.createUnoStruct('com.sun.star.lang.EventObject')
        event.Source = self
        for listener in self._listeners:
            listener.disposing(event)

    def addEventListener(self, listener):
        self._listeners.append(listener)

    def removeEventListener(self, listener):
        if listener in self._listeners:
            self._listeners.remove(listener)


    def _createLoggerFile(self):
        output = createService(self._ctx, 'com.sun.star.io.TextOutputStream')
        output.setEncoding(self._encoding)
        self._sf.writeFile(self._url, output)
        output.writeString(self._formatter.getHead())
        output.flush()
        output.closeOutput()

    def _openLoggerFile(self):
        stream = self._sf.openFileReadWrite(self._url)
        self._output.setOutputStream(stream.getOutputStream())
        self._input.setInputStream(stream.getInputStream())

    def _formatRecord(self, record):
        return self._delimiter + self._formater.format(record)
