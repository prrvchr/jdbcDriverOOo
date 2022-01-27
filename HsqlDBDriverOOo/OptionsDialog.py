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

import uno
import unohelper

from com.sun.star.lang import XServiceInfo
from com.sun.star.awt import XContainerWindowEventHandler
from com.sun.star.awt import XDialogEventHandler

from com.sun.star.uno import Exception as UnoException

from com.sun.star.ui.dialogs.ExecutableDialogResults import OK
from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from hsqldbdriver import createService
from hsqldbdriver import getDialog
from hsqldbdriver import getFilePicker
from hsqldbdriver import getFileSequence
from hsqldbdriver import getPathSettings
from hsqldbdriver import getResourceLocation
from hsqldbdriver import getSimpleFile
from hsqldbdriver import getStringResource
from hsqldbdriver import getUrl

from hsqldbdriver import clearLogger
from hsqldbdriver import getLoggerSetting
from hsqldbdriver import getLoggerUrl
from hsqldbdriver import getMessage
from hsqldbdriver import logMessage
from hsqldbdriver import setLoggerSetting
g_message = 'OptionsDialog'

from hsqldbdriver import g_extension
from hsqldbdriver import g_identifier
from hsqldbdriver import g_folder
from hsqldbdriver import g_jar

import os
import sys
import traceback

# pythonloader looks for a static g_ImplementationHelper variable
g_ImplementationHelper = unohelper.ImplementationHelper()
g_ImplementationName = '%s.OptionsDialog' % g_identifier


class OptionsDialog(unohelper.Base,
                    XServiceInfo,
                    XContainerWindowEventHandler,
                    XDialogEventHandler):

    def __init__(self, ctx):
        self._ctx = ctx
        self.stringResource = getStringResource(ctx, g_identifier, g_extension, 'OptionsDialog')
        msg = getMessage(ctx, g_message, 101)
        logMessage(ctx, INFO, msg, 'OptionsDialog', '__init__()')

    _updated = False

    # XContainerWindowEventHandler, XDialogEventHandler
    def callHandlerMethod(self, dialog, event, method):
        handled = False
        if method == 'external_event':
            if event == 'ok':
                self._saveSetting(dialog)
                handled = True
            elif event == 'back':
                self._reloadSetting(dialog)
                handled = True
            elif event == 'initialize':
                self._loadSetting(dialog)
                handled = True
        elif method == 'ToggleLogger':
            enabled = event.Source.State == 1
            self._toggleLogger(dialog, enabled)
            handled = True
        elif method == 'EnableViewer':
            self._toggleViewer(dialog, True)
            handled = True
        elif method == 'DisableViewer':
            self._toggleViewer(dialog, False)
            handled = True
        elif method == 'ViewLog':
            self._viewLog(dialog)
            handled = True
        elif method == 'ClearLog':
            self._clearLog(dialog)
            handled = True
        elif method == 'LogInfo':
            self._logInfo(dialog)
            handled = True
        elif method == 'Upload':
            self._upload(dialog)
            handled = True
        return handled
    def getSupportedMethodNames(self):
        return ('external_event', 'ToggleLogger', 'EnableViewer', 'DisableViewer',
                'ViewLog', 'ClearLog', 'LogInfo', 'Upload')

    def _loadSetting(self, dialog):
        self._loadLoggerSetting(dialog)
        self._loadVersion(dialog)

    def _loadVersion(self, dialog):
        if self._isUpdated():
            version = getMessage(self._ctx, g_message, 131)
        else:
            version = self._getDriverVersion()
        dialog.getControl('Label3').Text = version

    def _reloadSetting(self, dialog):
        self._loadLoggerSetting(dialog)

    def _saveSetting(self, dialog):
        self._saveLoggerSetting(dialog)

    def _toggleLogger(self, dialog, enabled):
        dialog.getControl('Label1').Model.Enabled = enabled
        dialog.getControl('ListBox1').Model.Enabled = enabled
        dialog.getControl('OptionButton1').Model.Enabled = enabled
        control = dialog.getControl('OptionButton2')
        control.Model.Enabled = enabled
        self._toggleViewer(dialog, enabled and control.State)

    def _toggleViewer(self, dialog, enabled):
        dialog.getControl('CommandButton1').Model.Enabled = enabled

    def _viewLog(self, window):
        dialog = getDialog(self._ctx, g_extension, 'LogDialog', self, window.Peer)
        url = getLoggerUrl(self._ctx)
        dialog.Title = url
        self._setDialogText(dialog, url)
        dialog.execute()
        dialog.dispose()

    def _clearLog(self, dialog):
        clearLogger()
        msg = getMessage(self._ctx, g_message, 111)
        logMessage(self._ctx, INFO, msg, 'OptionsDialog', '_clearLog()')
        url = getLoggerUrl(self._ctx)
        self._setDialogText(dialog, url)

    def _logInfo(self, dialog):
        version  = ' '.join(sys.version.split())
        msg = getMessage(self._ctx, g_message, 121, version)
        logMessage(self._ctx, INFO, msg, "OptionsDialog", "_logInfo()")
        path = os.pathsep.join(sys.path)
        msg = getMessage(self._ctx, g_message, 122, path)
        logMessage(self._ctx, INFO, msg, "OptionsDialog", "_logInfo()")
        url = getLoggerUrl(self._ctx)
        self._setDialogText(dialog, url)

    def _setDialogText(self, dialog, url):
        control = dialog.getControl('TextField1')
        length, sequence = getFileSequence(self._ctx, url)
        control.Text = sequence.value.decode('utf-8')
        selection = uno.createUnoStruct('com.sun.star.awt.Selection', length, length)
        control.setSelection(selection)

    def _loadLoggerSetting(self, dialog):
        enabled, index, handler = getLoggerSetting(self._ctx)
        dialog.getControl('CheckBox1').State = int(enabled)
        dialog.getControl('ListBox1').selectItemPos(index, True)
        dialog.getControl('OptionButton%s' % handler).State = 1
        self._toggleLogger(dialog, enabled)

    def _saveLoggerSetting(self, dialog):
        enabled = bool(dialog.getControl('CheckBox1').State)
        index = dialog.getControl('ListBox1').getSelectedItemPos()
        handler = dialog.getControl('OptionButton1').State
        setLoggerSetting(self._ctx, enabled, index, handler)

    def _getDriverVersion(self):
        try:
            service = '%s.sdbc.Driver' % g_identifier
            driver = createService(self._ctx, service)
            url = 'sdbc:hsqldb:mem:///dbversion'
            connection = driver.connect(url, ())
            version = connection.getMetaData().getDriverVersion()
            connection.close()
            return version
        except UnoException as e:
            msg = getMessage(self._ctx, g_message, 141, e.Message)
            logMessage(self._ctx, SEVERE, msg, 'OptionsDialog', '_getDriverVersion()')
        except Exception as e:
            msg = getMessage(self._ctx, g_message, 142, (e, traceback.print_exc()))
            logMessage(self._ctx, SEVERE, msg, 'OptionsDialog', '_getDriverVersion()')

    def _upload(self, dialog):
        path = getPathSettings(self._ctx).Work
        fp = getFilePicker(self._ctx)
        fp.setDisplayDirectory(path)
        fp.appendFilter(g_jar, '*.jar')
        fp.setCurrentFilter(g_jar)
        if fp.execute() == OK:
            url = getUrl(self._ctx, fp.getFiles()[0])
            if url.Name == g_jar:
                jar = '%s/%s' % (g_folder, g_jar)
                target = getResourceLocation(self._ctx, g_identifier, jar)
                getSimpleFile(self._ctx).copy(url.Main, target)
                self._setUpdated()
                self._loadVersion(dialog)


    def _isUpdated(self):
        return OptionsDialog._updated

    def _setUpdated(self):
        OptionsDialog._updated = True

    # XServiceInfo
    def supportsService(self, service):
        return g_ImplementationHelper.supportsService(g_ImplementationName, service)
    def getImplementationName(self):
        return g_ImplementationName
    def getSupportedServiceNames(self):
        return g_ImplementationHelper.getSupportedServiceNames(g_ImplementationName)


g_ImplementationHelper.addImplementation(OptionsDialog,                             # UNO object class
                                         g_ImplementationName,                      # Implementation name
                                        (g_ImplementationName,))                    # List of implemented services
