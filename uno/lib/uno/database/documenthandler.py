#!
# -*- coding: utf_8 -*-

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

import unohelper

from com.sun.star.embed.ElementModes import SEEKABLEREAD
from com.sun.star.embed.ElementModes import READWRITE

from com.sun.star.util import XCloseListener

from .unotool import createService
from .unotool import getSimpleFile
from .unotool import getUriFactory
from .unotool import getUrlTransformer
from .unotool import parseUrl

from .dbconfig import g_protocol
from .dbconfig import g_options
from .dbconfig import g_shutdown

import traceback


class DocumentHandler(unohelper.Base,
                      XCloseListener):
    def __init__(self, ctx, storage, url):
        self._ctx = ctx
        self._folder = 'database'
        self._prefix = '.'
        self._suffix = '.lck'
        self._path, self._name = self._getDataBaseInfo(url)
        # FIXME: With OpenOffice getElementNames() return a String
        # FIXME: if storage has no elements.
        if storage.hasElements():
            self._openDataBase(storage)

    # XCloseListener
    def queryClosing(self, event, owner):
        if self._closeDataBase(event.Source):
            sf = getSimpleFile(self._ctx)
            if sf.isFolder(self._path):
                sf.kill(self._path)

    def notifyClosing(self, event):
        pass

    # XEventListener
    def disposing(self, event):
        pass

    # Document getter methods
    def getDocumentInfo(self, document, url):
        if document is None:
            document = self._getDocument(url)
        else:
            document.addCloseListener(self)
        return document.DataSource, self._getConnectionUrl()

    # Document private methods
    def _openDataBase(self, source):
        sf = getSimpleFile(self._ctx)
        for name in source.getElementNames():
            url = self._getFileUrl(name)
            if not sf.exists(url):
                if source.isStreamElement(name):
                    input = source.openStreamElement(name, SEEKABLEREAD).getInputStream()
                    sf.writeFile(url, input)
                    input.closeInput()
        source.dispose()

    def _getDataBaseInfo(self, location):
        transformer = getUrlTransformer(self._ctx)
        url = parseUrl(transformer, location)
        name = self._getDataBaseName(transformer, url)
        path = self._getDataBasePath(transformer, url, name)
        return path, name

    def _getDataBasePath(self, transformer, url, name):
        path = self._getDocumentPath(transformer, url)
        return '%s%s%s%s' % (path, self._prefix, name, self._suffix)

    def _getDocumentPath(self, transformer, url):
        path = parseUrl(transformer, url.Protocol + url.Path)
        return transformer.getPresentation(path, False)

    def _getDataBaseName(self, transformer, location):
        url = transformer.getPresentation(location, False)
        uri = getUriFactory(self._ctx).parse(url)
        name = uri.getPathSegment(uri.getPathSegmentCount() -1)
        return self._getDocumentName(name)

    def _getDocumentName(self, title):
        name, sep, extension = title.rpartition('.')
        return name

    def _getFileUrl(self, name):
        return '%s/%s' % (self._path, name)

    def _getDocument(self, url):
        service = 'com.sun.star.sdb.DatabaseContext'
        datasource = createService(self._ctx, service).createInstance()
        datasource.URL = url
        return datasource.DatabaseDocument

    def _getConnectionUrl(self):
        return '%s%s/%s%s%s' % (g_protocol, self._path, self._name, g_options, g_shutdown)

    def _closeDataBase(self, document):
        target = document.getDocumentSubStorage(self._folder, READWRITE)
        service = 'com.sun.star.embed.FileSystemStorageFactory'
        args = (self._path, READWRITE)
        source = createService(self._ctx, service).createInstanceWithArguments(args)
        for name in source.getElementNames():
            if source.isStreamElement(name):
                if target.hasByName(name):
                    target.removeElement(name)
                source.moveElementTo(name, target, name)
        empty = not source.hasElements()
        target.commit()
        target.dispose()
        source.dispose()
        document.store()
        return empty

