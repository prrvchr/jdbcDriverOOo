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

from ..runner import RunnerException

from ..unotool import checkVersion
from ..unotool import createService
from ..unotool import executeDesktopDispatch
from ..unotool import getExtensionVersion
from ..unotool import getPathSubstitution
from ..unotool import getPropertyValueSet
from ..unotool import hasInterface

from ..configuration import g_extension

import gzip
import importlib
import io
import json
import operator
import os
from packaging import tags as pkg_tags
from packaging.requirements import Requirement
from packaging.specifiers import SpecifierSet
from packaging.utils import parse_wheel_filename
from packaging.version import parse as pkg_parse
import re
import shutil
import sys
import urllib.request
from urllib.parse import urlparse
import zipfile
import traceback


OPERATORS = {'==': operator.eq,
             '!=': operator.ne,
             '>=': operator.ge,
             '<=': operator.le,
             '>':  operator.gt,
             '<':  operator.lt}

def canUpdatePackages():
    return sys.version_info >= (3, 10)

def showSetup(ctx, identifier, listener=None, /, **kwargs):
    url = f'vnd.sun.star.job:service={identifier}.Setup'
    executeDesktopDispatch(ctx, url, listener, **kwargs)

def checkExtension(ctx, identifier, _, minimum):
    version = getExtensionVersion(ctx, identifier)
    return version is not None and checkVersion(version, minimum)

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
            if checkVersion(version, java):
                results = 0, java, version
            else:
                results = 1, java, version
    except Exception as e:
        trace = traceback.format_exc()
        raise RunnerException(e, trace)
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

def isLinuxDistribution(ctx):
    if sys.platform != 'linux':
        return False
    try:
        url = getPathSubstitution(ctx, '$(prog)')
        path = os.path.realpath(uno.fileUrlToSystemPath(url)).lower()
        for entry in sys.path:
            if os.path.realpath(entry).lower().startswith(path):
                return False
    except Exception as e:
        trace = traceback.format_exc()
        raise RunnerException(e, trace)
    return True

def getInstalledPackages():
    packages = {}
    for dist in importlib.metadata.distributions():
        try:
            package = dist.metadata.get('Name')
            version = dist.version
            if not package or not version:
                continue
            packages[parsePackageName(package)] = version
        except Exception:
            continue
    return packages

def getPackageVersionData(requirement, version):
    data = None
    url = f'https://pypi.org/pypi/{requirement.name}/{version}/json'
    try:
        headers = {'User-Agent': f'LibreOffice {g_extension} Extension',
                   'Accept-Encoding': 'gzip'}
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req) as response:
            if response.info().get('Content-Encoding') == 'gzip':
                with gzip.GzipFile(fileobj=io.BytesIO(response.read())) as f:
                    binary = f.read()
            else:
                binary = response.read()
            data = json.loads(binary.decode('utf-8'))
    except Exception as e:
        trace = traceback.format_exc()
        raise RunnerException(e, trace)
    return data

def parsePackageVersionData(requirement, data):
    tags = set(pkg_tags.sys_tags())
    for f in data.get('urls', []):
        if f.get('filename', '').endswith('.whl'):
            try:
                _, version, _, filetags = parse_wheel_filename(f['filename'])
            except Exception:
                continue

            if filetags.intersection(tags):
                return str(version), f['url']
    return None, None

def getPackageSimpleData(requirement):
    data = None
    url = f'https://pypi.org/simple/{requirement.name}/'
    try:
        headers = {'User-Agent': f'LibreOffice {g_extension} Extension',
                   'Accept': 'application/vnd.pypi.simple.v1+json',
                   'Accept-Encoding': 'gzip'}
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req) as response:
            if response.info().get('Content-Encoding') == 'gzip':
                with gzip.GzipFile(fileobj=io.BytesIO(response.read())) as f:
                    binary = f.read()
            else:
                binary = response.read()
            data = json.loads(binary.decode('utf-8'))
    except Exception as e:
        trace = traceback.format_exc()
        raise RunnerException(e, trace)
    return data

def parsePackageSimpleData(requirement, update, data):
    releases = []
    tags = set(pkg_tags.sys_tags())

    for f in data['files']:
        if f.get('yanked', False):
            continue

        if f.get('filename', '').endswith('.whl'):
            try:
                _, version, _, filetags = parse_wheel_filename(f['filename'])
            except Exception:
                continue

            if version.is_prerelease and update < 2:
                continue

            if filetags.intersection(tags):
                requires = f.get('requires-python')
                marker = False
                evaluator = None
                if requires:
                    evaluator = SpecifierSet(requires)
                elif requirement.specifier and version in requirement.specifier:
                    if requirement.marker:
                        marker = True
                        evaluator = requirement.marker
                    else:
                        continue
                else:
                    continue

                releases.append({'url': f['url'],
                                 'version': str(version),
                                 'marker': marker,
                                 'evaluator': evaluator})
    releases.sort(key=lambda x: pkg_parse(x['version']), reverse=True)

    info = sys.version_info
    python = f'{info.major}.{info.minor}.{info.micro}'
    for release in releases:
        marker = release['marker']
        evaluator = release['evaluator']
        try:
            if marker:
                if evaluator.evaluate():
                    return release['version'], release['url']
            elif python in evaluator:
                return release['version'], release['url']
        except Exception:
            continue
    return None, None

def uninstallPackage(package, url):
    try:
        pythonpath = uno.fileUrlToSystemPath(url)
        if not os.path.exists(pythonpath):
            return

        roots = set()
        namespaces = set()
        depth = len(package.split('.'))

        dists = [d for d in importlib.metadata.distributions() if d.metadata['Name'] == package]
        for dist in dists:
            try:
                distinfo = None
                if dist._path:
                    distinfo = os.path.basename(str(dist._path))
                if distinfo:
                    shutil.rmtree(os.path.join(pythonpath, distinfo))
            except Exception:
                pass

            modules = dist.files
            if modules:
                for module in modules:
                    parts = module.parts
                    if not parts:
                        continue
                    if len(parts) == 1:
                        roots.add(parts[0])
                    else:
                        path = os.path.join(*parts[:depth])
                        roots.add(path)
                        if depth > 1:
                            namespaces.add(parts[0])

        for r in roots:
            root = os.path.join(pythonpath, r)
            if os.path.exists(root):
                if os.path.isdir(root):
                    shutil.rmtree(root)
                else:
                    os.remove(root)

        for ns in namespaces:
            parent = os.path.join(pythonpath, ns)
            if os.path.exists(parent) and os.path.isdir(parent):
                entries = [e for e in os.listdir(parent) if e != '__pycache__']
                if not entries:
                    shutil.rmtree(parent)

        importlib.invalidate_caches()

    except Exception as e:
        trace = traceback.format_exc()
        raise RunnerException(e, trace)

def installPackage(url, path):
    try:
        headers = {'User-Agent': f'LibreOffice {g_extension} Extension'}
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req) as response:
            data = response.read()

        with zipfile.ZipFile(io.BytesIO(data)) as z:
            z.extractall(uno.fileUrlToSystemPath(path))
        return True
    except Exception as e:
        trace = traceback.format_exc()
        raise RunnerException(e, trace)

def checkPython(url):
    packages = getInstalledPackages()
    for requirement in parseRequirements(url):
        try:
            package = parsePackageName(requirement.name)
            version1 = packages.get(package)
            if not version1:
                return False
            specs = _getRequirementSpecs(requirement)
            if not all(checkVersion(version1, version2, op) for op, version2 in specs):
                return False
        except Exception as e:
            return False
    return True

def parseRequirements(url):
    info = sys.version_info
    python = {'python_version': f'{info.major}.{info.minor}'}
    for requirement in _parseRequirements(url):
        if requirement.marker and not requirement.marker.evaluate(python):
            continue
        yield requirement

def parsePackageName(package):
    return package.lower().replace('-', '_')

def getRequirementversion(requirement, update=0):
    if requirement.specifier:
        for specifier in requirement.specifier:
            if specifier.operator == '==' or update == 0:
                return specifier.version

    if requirement.url:
        try:
            version = _parseVersionUrl(requirement.url)
            if version:
                return version
        except Exception:
            pass
    return None

def _getRequirementSpecs(requirement):
    specs = []
    if requirement.specifier:
        for specifier in requirement.specifier:
            op = OPERATORS.get(specifier.operator, operator.eq)
            specs.append((op, specifier.version))
        return specs

    if requirement.url:
        try:
            version = _parseVersionUrl(requirement.url)
            if version:
                specs.append((operator.eq, version))
                return specs
        except Exception:
            pass
    return specs

def _parseVersionUrl(url):
    parsed = urlparse(url)
    if parsed.scheme == "version":
        return parsed.netloc
    return None

def _parseRequirements(url):
    for requirement in _iterRequirements(url):
        try:
            yield Requirement(requirement)
        except Exception as e:
            continue

def _iterRequirements(url):
    with open(uno.fileUrlToSystemPath(url), 'r', encoding='utf-8') as requirements:
        for line in requirements:
            line = line.strip()
            if not line or line.startswith('#'):
                continue
            if '#' in line:
                line = line.split('#')[0].strip()
            yield line

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

