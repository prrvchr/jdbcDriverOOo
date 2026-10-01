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

from ..unotool import createService
from ..unotool import executeDesktopDispatch
from ..unotool import getExtensionVersion
from ..unotool import getPropertyValueSet

import importlib
import io
import json
from packaging import tags as pkg_tags
from packaging.version import parse as pkg_parse
from packaging.requirements import Requirement
from packaging.utils import parse_wheel_filename
import re
import urllib.request
import zipfile
import traceback


def checkVersion(version, minimum):
    return pkg_parse(version) >= pkg_parse(minimum)

def showSetup(ctx, identifier, listener=None, /, **kwargs):
    url = f'vnd.sun.star.job:service={identifier}.Setup'
    executeDesktopDispatch(ctx, url, listener, **kwargs)

def checkExtension(ctx, identifier, data):
    version = getExtensionVersion(ctx, identifier)
    return version is not None and checkVersion(version, data[1])

def getJavaStatus(ctx):
    service = 'com.sun.star.comp.stoc.JavaVirtualMachine'
    jvm = createService(ctx, service)
    if jvm is None:
        return 4
    if jvm.isVMEnabled():
        return 0
    return 3

def getJavaVersion(ctx, extension, java, script):
    results = 2, java, java
    try:
        service = 'com.sun.star.script.provider.MasterScriptProviderFactory'
        factory = createService(ctx, service)
        provider = factory.createScriptProvider('')
        url = f'vnd.sun.star.script:{script}?language=Java&location=user:uno_packages/{extension}.oxt'
        macro = provider.getScript(url)
        if macro:
            result = macro.invoke((), (), ())[0]
            version = _parseJavaVersion(result)
            version = result
            if checkVersion(version, java):
                results = 0, java, version
            else:
                results = 1, java, version
    except Exception:
        print("helper.getJavaVersion() ERROR: %s" % traceback.format_exc())
        pass
    return results

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

def parseRequirements(url):
    for requirement in _iterRequirements(url):
        yield Requirement(requirement)

def getPackageCount(url):
    return sum(1 for _ in _iterRequirements(url))

def checkPython(url, addInstalled, addMissing, onError=None):
    if hasattr(importlib.metadata, 'packages_distributions'):
        modules = importlib.metadata.packages_distributions()
    else:
        modules = _getModules()
    for requirement in parseRequirements(url):
        try:
            package = _parseModuleName(requirement.name)
            for module, packages in modules.items():
                if package in [_parseModuleName(p) for p in packages]:
                    yield addInstalled(module)
                    break
            else:
                yield addMissing(package)
        except Exception as e:
            if onError is not None:
                onError(e)
            else:
                continue

def getPackageData(package, onError=None):
    data = None
    url = f'https://pypi.org/pypi/{package}/json'
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'LibreOffice Extension'})
        with urllib.request.urlopen(req) as response:
            data = json.loads(response.read().decode('utf-8'))
    except Exception as e:
        if onError:
            onError(e)
    return data

def getPackageUrl(data):
    tags = list(pkg_tags.sys_tags())
    releases = data.get('releases', {})
    stables = [v for v in releases.keys() if not pkg_parse(v).is_prerelease]
    versions = sorted(stables, key=pkg_parse, reverse=True)

    results = None, None
    for version in versions:
        files = releases[version]
        for release in files:
            if release['packagetype'] == 'bdist_wheel':
                try:
                    _, _, _, filetags = parse_wheel_filename(release['filename'])
                except Exception:
                    continue
                if filetags.intersection(tags):
                    results = release['url'], version
                    break
        if all(results):
            break
    return results

def installPackage(url, path, onError=None):
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'LibreOffice Extension'})
        with urllib.request.urlopen(req) as response:
            data = response.read()

        with zipfile.ZipFile(io.BytesIO(data)) as z:
            z.extractall(uno.fileUrlToSystemPath(path))
        return True
    except Exception as e:
        if onError:
            onError(e)
        return False

def checkConnection(ctx, source, connection, logger, new, warn=False):
    version = connection.getMetaData().getDriverVersion()
    if not checkVersion(version, g_version):
        connection.close()
        title, msg = _getExceptionMessage(logger, 511, g_extension2, version, g_version)
        if warn:
            _showWarning(ctx, title, msg)
        raise UnoException(msg, source)
    service = 'com.sun.star.sdb.Connection'
    interface = 'com.sun.star.sdbcx.XGroupsSupplier'
    if new and not _checkConnection(connection, service, interface):
        connection.close()
        title, msg = _getExceptionMessage(logger, 513, g_extension2, service, interface)
        if warn:
            _showWarning(ctx, title, msg)
        raise UnoException(msg, source)

def _iterRequirements(url):
    with open(uno.fileUrlToSystemPath(url), 'r', encoding='utf-8') as requirements:
        for line in requirements:
            line = line.strip()
            if not line or line.startswith('#'):
                continue
            yield line

def _getModules():
    modules = {}
    for dist in importlib.metadata.distributions():
        toplevel = dist.read_text('top_level.txt')
        if toplevel:
            for module in toplevel.splitlines():
                module = module.strip()
                if module:
                    if module not in modules:
                        modules[module] = []
                    modules[module].append(dist.metadata['Name'])
    return modules

def _parseJavaVersion(version):
    clean = version.replace('_', '.post')
    match = re.match(r'^([0-9.]+)(.*)$', clean)
    if match:
        basenum = match.group(1).strip(".")
        extra = match.group(2)
        if extra:
            extraclean = re.sub(r'[^a-zA-Z0-9.]', '', extra)
            if extraclean:
                clean = f'{basenum}+{extraclean}'
            else:
                clean = basenum
    return clean

def _parseModuleName(module):
    return module.lower().replace('-', '_')

