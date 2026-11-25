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

from .helper import canUpdatePackages
from .helper import checkInternet
from .helper import checkVersion
from .helper import createMessageBox
from .helper import createService
from .helper import deregisterStartupJob
from .helper import executeDesktopDispatch
from .helper import executeDispatch
from .helper import executeFrameDispatch
from .helper import executeShell
from .helper import findFrame
from .helper import generateUuid
from .helper import getArgumentSet
from .helper import getCallBack
from .helper import getConfiguration
from .helper import getConnectionMode
from .helper import getConnector
from .helper import getContainerWindow
from .helper import getCurrentLocale
from .helper import getDateTime
from .helper import getDefaultPropertyValueSet
from .helper import getDesktop
from .helper import getDialogPosSize
from .helper import getDispatcher
from .helper import getDialog
from .helper import getDialogUrl
from .helper import getDocument
from .helper import getDriverManager
from .helper import getExceptionMessage
from .helper import getExtensionVersion
from .helper import getFilePicker
from .helper import getFileSequence
from .helper import getFileUrl
from .helper import getInteractionHandler
from .helper import getInterfaceNames
from .helper import getInterfaceTypes
from .helper import getJobArguments
from .helper import getLastNamedParts
from .helper import getLibreOfficeInfo
from .helper import getLibreOfficeVersion
from .helper import getMailMerge
from .helper import getMimeTypeFactory
from .helper import getMri
from .helper import getNamedValue
from .helper import getNamedValueSet
from .helper import getPathSettings
from .helper import getPathSubstitution
from .helper import getParentWindow
from .helper import getProperty
from .helper import getPropertyValue
from .helper import getPropertyValueSet
from .helper import getResourceLocation
from .helper import getRowSet
from .helper import getSequenceInputStream
from .helper import getSimpleFile
from .helper import getStreamSequence
from .helper import getStringResource
from .helper import getStringResourceWithLocation
from .helper import getSystemClipboard
from .helper import getTempFile
from .helper import getToolKit
from .helper import getTopWindow
from .helper import getTypeDetection
from .helper import getUriFactory
from .helper import getUriTranslator
from .helper import getUrl
from .helper import getUrlPresentation
from .helper import getUrlTransformer
from .helper import getWindowHeight
from .helper import getWindowPosSize
from .helper import hasFrameInterface
from .helper import hasInterface
from .helper import hasService
from .helper import hasStartupJob
from .helper import notifyDispatch
from .helper import parseDateTime
from .helper import parseUrl
from .helper import registerStartupJob
from .helper import saveWindowPosSize
from .helper import setDialogPosition
from .helper import setProgress
from .helper import setWindowHeight
from .helper import setWindowPosSize
from .helper import showMessageBox
from .helper import unparseDateTime
from .helper import unparseTimeStamp
