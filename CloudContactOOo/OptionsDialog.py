#!
# -*- coding: utf_8 -*-

import uno
import unohelper

from com.sun.star.lang import XServiceInfo
from com.sun.star.awt import XContainerWindowEventHandler
from com.sun.star.awt import XDialogEventHandler
from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from unolib import getFileSequence
from unolib import getStringResource
from unolib import getResourceLocation
from unolib import getDialog
from unolib import createService

from cloudcontact import getDataSourceLocation
from cloudcontact import getDataSourceJavaInfo
from cloudcontact import getDataSourceUrl

from cloudcontact import getLoggerUrl
from cloudcontact import getLoggerSetting
from cloudcontact import setLoggerSetting
from cloudcontact import clearLogger
from cloudcontact import logMessage

from cloudcontact import g_extension
from cloudcontact import g_identifier
from cloudcontact import g_path
from cloudcontact import g_host

import traceback

# pythonloader looks for a static g_ImplementationHelper variable
g_ImplementationHelper = unohelper.ImplementationHelper()
g_ImplementationName = '%s.OptionsDialog' % g_identifier


class OptionsDialog(unohelper.Base,
                    XServiceInfo,
                    XContainerWindowEventHandler,
                    XDialogEventHandler):
    def __init__(self, ctx):
        self.ctx = ctx
        self.stringResource = getStringResource(self.ctx, g_identifier, g_extension, 'OptionsDialog')
        logMessage(self.ctx, INFO, "Loading ... Done", 'OptionsDialog', '__init__()')

    # XContainerWindowEventHandler, XDialogEventHandler
    def callHandlerMethod(self, dialog, event, method):
        handled = False
        if method == 'external_event':
            if event == 'ok':
                self._saveSetting(dialog)
                handled = True
            elif event == 'back':
                self._loadSetting(dialog)
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
        elif method == 'ViewData':
            self._viewData(dialog)
            handled = True
        return handled
    def getSupportedMethodNames(self):
        return ('external_event', 'ToggleLogger', 'EnableViewer', 'DisableViewer',
                'ViewLog', 'ClearLog', 'ViewData')

    def _loadSetting(self, dialog):
        self._loadLoggerSetting(dialog)

    def _saveSetting(self, dialog):
        self._saveLoggerSetting(dialog)

    def _toggleLogger(self, dialog, enabled):
        dialog.getControl('Label1').Model.Enabled = enabled
        dialog.getControl('ComboBox1').Model.Enabled = enabled
        dialog.getControl('OptionButton1').Model.Enabled = enabled
        control = dialog.getControl('OptionButton2')
        control.Model.Enabled = enabled
        self._toggleViewer(dialog, enabled and control.State)

    def _toggleViewer(self, dialog, enabled):
        dialog.getControl('CommandButton1').Model.Enabled = enabled

    def _viewLog(self, window):
        dialog = getDialog(self.ctx, g_extension, 'LogDialog', self, window.Peer)
        url = getLoggerUrl(self.ctx)
        dialog.Title = url
        self._setDialogText(dialog, url)
        dialog.execute()
        dialog.dispose()

    def _clearLog(self, dialog):
        try:
            clearLogger()
            logMessage(self.ctx, INFO, "ClearingLog ... Done", 'OptionsDialog', '_doClearLog()')
            url = getLoggerUrl(self.ctx)
            self._setDialogText(dialog, url)
        except Exception as e:
            msg = "Error: %s - %s" % (e, traceback.print_exc())
            logMessage(self.ctx, SEVERE, msg, "OptionsDialog", "_doClearLog()")

    def _setDialogText(self, dialog, url):
        length, sequence = getFileSequence(self.ctx, url)
        dialog.getControl('TextField1').Text = sequence.value.decode('utf-8')

    def _loadLoggerSetting(self, dialog):
        enabled, index, handler = getLoggerSetting(self.ctx)
        dialog.getControl('CheckBox1').State = int(enabled)
        self._setLoggerLevel(dialog.getControl('ComboBox1'), index)
        dialog.getControl('OptionButton%s' % handler).State = 1
        self._toggleLogger(dialog, enabled)

    def _setLoggerLevel(self, control, index):
        control.Text = self._getLoggerLevelText(control.Model.Name, index)

    def _getLoggerLevel(self, control):
        name = control.Model.Name
        for index in range(control.ItemCount):
            if self._getLoggerLevelText(name, index) == control.Text:
                break
        return index

    def _getLoggerLevelText(self, name, index):
        text = 'OptionsDialog.%s.StringItemList.%s' % (name, index)
        return self.stringResource.resolveString(text)

    def _saveLoggerSetting(self, dialog):
        enabled = bool(dialog.getControl('CheckBox1').State)
        index = self._getLoggerLevel(dialog.getControl('ComboBox1'))
        handler = dialog.getControl('OptionButton1').State
        setLoggerSetting(self.ctx, enabled, index, handler)

    def _viewData(self, dialog):
        dbcontext = createService(self.ctx, 'com.sun.star.sdb.DatabaseContext')
        url, error = getDataSourceUrl(self.ctx, dbcontext, g_host, g_identifier, False)
        if error is not None:
            return
        desktop = createService(self.ctx, 'com.sun.star.frame.Desktop')
        desktop.loadComponentFromURL(url, '_default', 0, ())

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
