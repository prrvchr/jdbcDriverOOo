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

from com.sun.star.logging.LogLevel import INFO
from com.sun.star.logging.LogLevel import SEVERE

from ..cancel import Cancel

from ..helper import checkAgent
from ..helper import checkExtensions
from ..helper import checkJava
from ..helper import checkPython

from ...unotool import deregisterStartupJob
from ...unotool import getConfiguration
from ...unotool import getPathSubstitution
from ...unotool import getResourceLocation
from ...unotool import getStringResource
from ...unotool import saveWindowPosition

from ...logger import getLogger

from ...configuration import g_basename
from ...configuration import g_defaultlog
from ...configuration import g_identifier

import importlib
from time import sleep
import os
import subprocess
import traceback


class SetupModel():
    def __init__(self, ctx, name, code, database, java, agent, python, extensions):
        self._ctx = ctx
        self._status = 0
        self._name = name
        self._code = code
        self._java = java
        self._agent = agent
        self._database = database
        self._python = python
        self._extensions = extensions
        self._dependencies = ()
        self._modules = []
        self._cancel = Cancel()
        self._position = 'SetupPosition'
        self._program = getPathSubstitution(ctx, '$(prog)')
        self._pythonpath = getResourceLocation(self._ctx, g_identifier, 'service/pythonpath')
        self._pip = self._pythonpath + '/pip/__main__.py'
        self._logger = getLogger(ctx, g_defaultlog, g_basename)
        self._config = getConfiguration(ctx, g_identifier, True)
        self._resolver = getStringResource(ctx, g_identifier, 'dialogs', 'SetupWindow')
        self._resources = {'Title': 'SetupWindow.Title',
                           'Header': 'SetupWindow.Label1.Label.%s',
                           'Dependency': 'SetupWindow.Label2.Label',
                           'Java': 'SetupWindow.Label5.Label',
                           'Text': 'SetupWindow.Label15.Label',
                           'Install': 'SetupWindow.Label18.Label'}
    def close(self):
        self._cancel.set()

    def getPage(self, step, **kwargs):
        return step, self._getHeader(step, **kwargs)

    def getViewData(self):
        return self._getDialogPosition(), self._getTitle()

    def savePosition(self, position):
        saveWindowPosition(self._config, position, self._position)

    def hasDataBase(self):
        return self._database is not None

    def getDataBase(self):
        return self._database

    def hasJava(self):
        return self._java is not None

    def hasPython(self):
        return self._python

    def hasExtensions(self):
        return len(self._extensions) != 0 

    def checkExtensions(self, maxProgress, progress):
        self._dependencies = checkExtensions(self._ctx, self._extensions, self._cancel, maxProgress, progress, self._getDependencyText)
        success = len(self._dependencies) == 0
        return success, self._getExtensionsResult(self._extensions.values() if success else self._dependencies)

    def checkJava(self, maxProgress, progress):
        code, minimum, version = checkJava(self._ctx, self._java, maxProgress, progress, self._getJavaText)
        success = self._isSuccess(code)
        if success and self._agent:
            success = checkAgent(self._ctx, *self._agent)
        return success, code, minimum, version

    def checkPython(self, maxProgress, progress):
        success, self._modules = checkPython(self._ctx, g_identifier, self._cancel, maxProgress, progress, self._getProgressText)
        return success, self._getRequirementsResult(self._modules)

    def installPackages(self, maxProgress, progress):
        size = len(self._modules) * 2
        maxProgress(size)
        i = 0
        modules = []
        isWindows = os.name == 'nt'
        info = self._getStartupInfo(isWindows)
        command = self._getPipCommand(isWindows)
        for module in self._modules:
            if self._cancel.isSet():
                break
            i += 1
            progress(self._getInstallText(module), i)
            message = self._pipInstall(info, command, module)
            i += 1
            progress(self._getInstallText(module), i)
            if message is None:
                importlib.invalidate_caches()
                self._log(INFO, self._code + 1, module)
            else:
                modules.append(module)
                self._log(SEVERE, self._code + 2, module, message)
            if i == size:
                sleep(1)
        success = len(modules) == 0
        return success, self._getRequirementsResult(self._modules if success else modules)

    def deregisterJob(self):
        deregisterStartupJob(self._ctx, self._name)

    def getRequiredExtension(self):
        return self._getExtensionsResult(self._dependencies)

    def getRequiredJava(self):
        extension, java, script = self._java
        return self._getJavaMessage(java)

    def _getPipCommand(self, isWindows):
        if isWindows:
            url = self._program + '/python.exe'
        else:
            url = self._program + '/python'
        executable = uno.fileUrlToSystemPath(url)
        pip = uno.fileUrlToSystemPath(self._pip)
        path = uno.fileUrlToSystemPath(self._pythonpath)
        return [executable, '-u', pip, 'install', '--target', path, '--only-binary=:all:', 'module']

    def _getStartupInfo(self, isWindows):
        info = None
        if isWindows:
            info = subprocess.STARTUPINFO()
            info.dwFlags |= subprocess.STARTF_USESHOWWINDOW
        return info

    def _pipInstall(self, info, command, module):
        msg = None
        try:
            command[-1] = module
            process = subprocess.Popen(command,
                                       stdout=subprocess.PIPE,
                                       stderr=subprocess.PIPE,
                                       startupinfo=info)
            stdout, stderr = process.communicate()
            if process.returncode != 0:
                msg = stderr.decode('utf-8', errors='ignore')
        except Exception as e:
            pass
        return msg

    def _isSuccess(self, code):
        return code == 0

    def _hasJavaVersion(self, code):
        return code < 2

    def _getRequirementsResult(self, modules):
        return ', '.join(modules)

    def _getExtensionsResult(self, extensions):
        return '\n'.join(['%s - version %s' % (name, version) for (name, version) in extensions])

    def _getJavaResult(self, code, minimum, version):
        return self._getJavaMessage(version if self._hasJavaVersion(code) else '...')

    def _getJavaMessage(self, version):
        return 'Java JDK version: ' + version

    def _log(self, level, code, *args):
        self._logger.logprb(level, 'SetupModel', 'installModules()', code, *args)

    def _getDialogPosition(self):
        return uno.createUnoStruct('com.sun.star.awt.Point', *self._config.getByName(self._position))

# SetupModel StringRessoure methods
    def _getTitle(self):
        return self._resolver.resolveString(self._resources.get('Title'))

    def _getHeader(self, step, **kwargs):
        resource = self._resources.get('Header') % step
        return self._resolver.resolveString(resource).format(**kwargs)

    def _getDependencyText(self, data):
        return self._resolver.resolveString(self._resources.get('Dependency')) % data

    def _getJavaText(self, version=''):
        return self._resolver.resolveString(self._resources.get('Java')) + version

    def _getProgressText(self, *args):
        return self._resolver.resolveString(self._resources.get('Text')) % args

    def _getInstallText(self, module):
        return self._resolver.resolveString(self._resources.get('Install')) + module

