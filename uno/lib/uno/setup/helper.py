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

from com.sun.star.beans import PropertyValue
from emailer import jdbcdriver

from ..jdbcdriver import checkDriverService

from ..unotool import checkVersion
from ..unotool import createService
from ..unotool import getDriverManager
from ..unotool import getExtensionVersion
from ..unotool import getResourceLocation
from ..unotool import getSimpleFile

import importlib
import os
from packaging.requirements import Requirement
import pkg_resources as pkgr
import re
import subprocess
from time import sleep
import traceback


def checkExtensions(ctx, extensions, cancel=None, maxProgress=None, progress=None, resolver=None):
    i = 0
    dependencies = []
    if maxProgress:
        maxProgress(len(extensions))
    for identifier, data in extensions.items():
        if cancel and cancel.isSet():
            break
        if progress and resolver:
            i += 1
            progress(resolver(data), i)
        if not _checkExtension(ctx, identifier, data):
            dependencies.append(data)
        if progress:
            sleep(1)
    return dependencies

def checkJava(ctx, java, maxProgress=None, progress=None, resolver=None):
    success = False
    if maxProgress:
        maxProgress(2)
    if progress and resolver:
        progress(resolver(), 1)
    success, version = _getJavaVersion(ctx, *java)
    if progress and resolver:
        progress(resolver(version), 2)
        sleep(1)
    print("checkJava() version: %s" % version)
    return success, version

def _getJavaVersion(ctx, identifier, jar, java, module):
    jvm = createService(ctx, 'com.sun.star.comp.stoc.JavaVirtualMachine')
    if jvm is None:
        print("_getJavaVersion() no java 1")
        return False, java

    if not jvm.isVMEnabled():
        print("_getJavaVersion() no java enabled 2")
        return False, java

    print("_getJavaVersion() 3 java enable: %s" % jvm.isVMEnabled())

    try:
        url = getResourceLocation(ctx, identifier, jar)
        loader = createService(ctx, 'com.sun.star.loader.URLClassLoader')
        loader.initialize((url, ))
        info = loader.loadClass(module)
        version = info.getVersion()
        print("_getJavaVersion() 4 version: %s" % version)
        return True, version
    except Exception:
        print("_getJavaVersion() 5 ERROR: %s" % traceback.format_exc())
    print("_getJavaVersion() 6 java incorrect version")
    return False, java

def checkJava1(java, maxProgress=None, progress=None, resolver=None):
    success = False
    if maxProgress:
        maxProgress(2)
    if progress and resolver:
        progress(resolver(), 1)
    version = _getJavaVersion()
    if progress and resolver:
        progress(resolver(version), 2)
        sleep(1)
    success = version is not None and checkVersion(version, java)
    return success, version if success else java

def _getJavaVersion1():
    version = None
    try:
        result = subprocess.run(['java', '-version'],
                                 stdout=subprocess.PIPE,
                                 stderr=subprocess.PIPE,
                                 text=True,
                                 check=True)
        line = result.stderr.splitlines()[0]
        match = re.search(r'"([^"]+)"', line)
        if match:
            version = match.group(1)
    except Exception:
        pass
    return version

def checkPython(ctx, identifier, cancel=None, maxProgress=None, progress=None, resolver=None):
    modules = []
    packages = []
    url = getResourceLocation(ctx, identifier, 'requirements.txt')
    if getSimpleFile(ctx).exists(url):
        _checkPackages(modules, packages, url, cancel, maxProgress, progress, resolver)
    success = len(modules) == 0
    return success, packages if success else modules

def _checkExtension(ctx, identifier, data):
    version = getExtensionVersion(ctx, identifier)
    return version is not None and checkVersion(version, data[1])

def _checkPackages(modules, packages, url, cancel, maxProgress, progress, resolver):
    with open(uno.fileUrlToSystemPath(url)) as requirements:
        for requirement in pkgr.parse_requirements(requirements):
            if cancel and cancel.isSet():
                break
            packages.append(requirement.project_name)
    i = 0
    size = len(packages)
    if maxProgress:
        maxProgress(size)
    for package in packages:
        if cancel and cancel.isSet():
            break
        i += 1
        module = _getInstallModule(modules, package, cancel)
        if progress and resolver:
            progress(resolver(package, module), i)
            if i == size:
                sleep(1)

def _getInstallModule(modules, package, cancel):
    target = _parseModuleName(Requirement(package).name)
    for module, packages in importlib.metadata.packages_distributions().items():
        if cancel and cancel.isSet():
            break
        if target in [_parseModuleName(p) for p in packages]:
            return module
    else:
        modules.append(target)
    return target

def _parseModuleName(module):
    return module.lower().replace('-', '_')

