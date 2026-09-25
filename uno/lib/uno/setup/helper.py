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

from ..unotool import checkVersion
from ..unotool import createService
from ..unotool import executeDesktopDispatch
from ..unotool import getExtensionVersion
from ..unotool import getPropertyValueSet
from ..unotool import getResourceLocation
from ..unotool import getSimpleFile

import importlib
from packaging.requirements import Requirement
import pkg_resources as pkgr
from time import sleep
import traceback
from xml.dom import minicompat


def showSetup(ctx, identifier, listener=None, /, **kwargs):
    url = f'vnd.sun.star.job:service={identifier}.Setup'
    executeDesktopDispatch(ctx, url, listener, **kwargs)

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
    if maxProgress:
        maxProgress(3)
    if progress and resolver:
        progress(resolver(), 1)
    code = _getJavaStatus(ctx)
    if progress and resolver:
        progress(resolver(), 2)
    if code > 0:
        minimum, version = _getDefaultVersion(*java)
    else:
        code, minimum, version = _getJavaVersion(ctx, *java)
    if progress and resolver:
        progress(resolver(version), 3)
        sleep(1)
    print("checkJava() java: %s - version: %s" % (minimum, version))
    return code, minimum, version

def checkAgent(ctx, service, url, agent):
    support = False
    driver = createService(ctx, service)
    if driver:
        properties = getPropertyValueSet({agent: True})
        for info in driver.getPropertyInfo(url, properties):
            if info.Name == agent:
                support = info.Value != 'false'
                break
    return support

def checkPython(ctx, identifier, cancel=None, maxProgress=None, progress=None, resolver=None):
    modules = []
    packages = []
    url = getResourceLocation(ctx, identifier, 'requirements.txt')
    if getSimpleFile(ctx).exists(url):
        _checkPackages(modules, packages, url, cancel, maxProgress, progress, resolver)
    success = len(modules) == 0
    return success, packages if success else modules

def _getJavaStatus(ctx):
    service = 'com.sun.star.comp.stoc.JavaVirtualMachine'
    jvm = createService(ctx, service)
    if jvm is None:
        return 4
    if jvm.isVMEnabled():
        return 0
    return 3

def _getDefaultVersion(extension, java, script):
    return java, java

def _getJavaVersion(ctx, extension, java, script):
    results = 2, java, java
    try:
        service = 'com.sun.star.script.provider.MasterScriptProviderFactory'
        factory = createService(ctx, service)
        provider = factory.createScriptProvider('')
        url = f'vnd.sun.star.script:{script}?language=Java&location=user:uno_packages/{extension}.oxt'
        macro = provider.getScript(url)
        if macro:
            result = macro.invoke((), (), ())
            version = result[0]
            if version:
                if checkVersion(version, java):
                    results = 0, java, version
                else:
                    results = 1, java, version
    except Exception:
        pass
    return results

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

